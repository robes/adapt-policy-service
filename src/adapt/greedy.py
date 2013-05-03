'''
Created on Apr 11, 2013

@author: schuler
'''
import web
import json
import threading
from copy import deepcopy
from urlparse import urlparse
from policy import Policy, MalformedTransfer, TransferNotFound, NotAllowed, PolicyError

MAX_STREAMS     = 36 # Max streams per-pairwise endpoints
DEFAULT_STREAMS = 8  # Default streams allocated per new request
MIN_STREAMS     = 0  # Minimum streams allocated when over-allocated

class Greedy(Policy):
    '''The greedy policy manager.'''
    
    def __init__(self):
        self.lock = threading.Lock()
        self.next_transfer_id = 0L
        self.transfers = {}
        self.resources = {}
        self.max_streams = MAX_STREAMS
        self.default_streams = DEFAULT_STREAMS
        self.min_streams = MIN_STREAMS
    
    
    def dump(self):
        return (self.transfers, self.resources)
    
    
    def make_resources_key(self, transfer):
        '''Create the key out of the source and destination hostnames.
        
        Raises ValueError, if cannot parse hostname from URL.
        '''
        if not 'source' in transfer:
            raise MalformedTransfer("No transfer source")
        if not 'destination' in transfer:
            raise MalformedTransfer("No transfer destination")
        
        srchost = urlparse(transfer.source).hostname
        dsthost = urlparse(transfer.destination).hostname
        if srchost is None:
            raise MalformedTransfer("No hostname in URL: " + transfer.source)
        elif dsthost is None:
            raise MalformedTransfer("No hostname in URL: " + transfer.destination)
        
        return srchost + "::" + dsthost
    
    
    def all(self):
        '''Returns a dictionary of all transfer resources, keyed on transfer id.'''
        with self.lock:
            web.debug("all")
            return deepcopy(self.transfers)
    
    
    def add(self, transfer):
        '''Adds a transfer resource to the greedly policy manager.
        
        Allocates the default streams, if available, the balance if not, 
        and the minimum if below threshold.
        
        A 'transfer' dictionary is expected.
        '''
        with self.lock:
            web.debug("add: " + str(transfer))
            key = self.make_resources_key(transfer)
            
            # Add transfer to collection and increment counter
            transfer.id = self.next_transfer_id
            self.transfers[self.next_transfer_id] = transfer
            self.next_transfer_id += 1L
            
            # Allocate as many streams as possible up to the default
            if key in self.resources:
                available = self.resources[key]
                if available >= self.default_streams:
                    transfer.streams = self.default_streams
                    self.resources[key] -= self.default_streams
                elif available == 0:
                    transfer.streams = self.min_streams
                else:
                    transfer.streams = available
                    self.resources[key] = 0
            else:
                transfer.streams = self.default_streams
                self.resources[key] = self.max_streams - self.default_streams
            
            if transfer.streams == self.default_streams:
                transfer.threshold = True
            else:
                transfer.threshold = False

            return deepcopy(transfer)
    
    
    def get(self, transferId):
        '''Returns a transfer dictionary.
        
        A valid integer 'transferId' is expected.
        '''
        with self.lock:
            if not self.transfers.has_key(transferId):
                raise TransferNotFound()
            else:
                return deepcopy(self.transfers[transferId])
    
    def update(self, transferId, transfer):
        '''Updates a transfer and returns the current state of it.
        
        A valid integer 'transferId' is expected.
        
        A valid 'transfer' dictionary is expected. If streams > 0, the policy
        module will attempt to allocate min(transfer.streams, available streams).
        If streams < 0 or not specified, the policy module will attempt to allocate
        min(default streams, available streams).
        '''
        with self.lock:
            if not self.transfers.has_key(transferId):
                raise TransferNotFound()
            if not 'streams' in transfer or transfer.streams < 0:
                transfer.streams = self.default_streams
            
            # Make sure the src/dst match the original
            original = self.transfers[transferId]
            if transfer.source != original.source or \
                transfer.destination != original.destination:
                raise NotAllowed("Cannot change source or destination endpoitns during an update")
            
            # Allocate as many streams as possible up to the default
            key = self.make_resources_key(transfer)
            if key not in self.resources:
                raise PolicyError("No allocation record for these endpoints (key="+key+")")
            
            available = self.resources[key]
            requested = transfer.streams
            delta = requested - original.streams
            
            if available >= delta:
                web.debug("Granting %s streams" % delta)
                original.streams += delta
                self.resources[key] -= delta
            elif available > 0:
                web.debug("Granting %s streams" % available)
                original.streams += available
                self.resources[key] = 0
            else:
                web.debug("No streams available to allocate")
            
            if original.streams == self.default_streams:
                original.threshold = True
            else:
                original.threshold = False
            
            return deepcopy(original)
    
    def remove(self, transferId):
        '''Removes a transfer and frees up allocated resources.
        
        A valid integer 'transferId' is expected.
        '''
        with self.lock:
            if not self.transfers.has_key(transferId):
                raise TransferNotFound()
            
            transfer = self.transfers[transferId]
            del self.transfers[transferId]
            
            # "Return" the resources to the pool
            key = self.make_resources_key(transfer)
            self.resources[key] += transfer.streams
            if self.resources[key] > self.max_streams:
                self.resources[key] = self.max_streams


class Transfer:
    def POST(self, transferId=None):
        '''POST /transfer
        
        Not allowed to POST /transfer/{ID}.
        '''
        if transferId:
            raise web.NoMethod()
        
        raw = web.data()
        try:
            parsed = json.loads(raw)
            transfer = web.storify(parsed)
            transfer = policy.add(transfer)
            return json.dumps(transfer)
        except MalformedTransfer as e:
            msg = "Bad request body in POST to transfer/ resource: " + str(e)
            web.debug(msg)
            raise web.BadRequest(msg)
    
    def GET(self, transferId):
        ''' GET /transfer/[ID]
        
        Returns all or one transfer representation.
        '''
        if not transferId:
            transfers = policy.all()
            return json.dumps(transfers)
        
        try:
            transferId = int(transferId)
            transfer = policy.get(transferId)
            return json.dumps(transfer)
        except TransferNotFound:
            msg = "Cannot GET transfer resource transfer/"+str(transferId)+". Resource not found."
            web.debug(msg)
            raise web.BadRequest(msg)
        
    def PUT(self, transferId):
        ''' PUT /transfer/{ID}
        
        Updates one transfer.
        '''
        if not transferId:
            msg = "Cannot PUT to the transfer resource without specifying a valid transfer resource ID in the path transfer/{ID}"
            web.debug(msg)
            raise web.BadRequest(msg)
        
        try:
            transferId = int(transferId)
        except ValueError as e:
            msg = "Invalid transfer id: " + str(e)
            web.debug(msg)
            raise web.BadRequest(msg)
        
        try:
            raw = web.data()
            parsed = json.loads(raw)
            transfer = web.storify(parsed)
            transfer = policy.update(transferId, transfer)
            return json.dumps(transfer)
        except TransferNotFound:
            msg = "Cannot PUT to transfer resource transfer/"+str(transferId)+". Resource not found."
            web.debug(msg)
            raise web.BadRequest(msg)
        except MalformedTransfer as e:
            msg = "Bad request body in PUT to transfer/ resource: " + str(e)
            web.debug(msg)
            raise web.BadRequest(msg)
        except NotAllowed as e:
            msg = "Bad request body in PUT to transfer/ resource: " + str(e)
            web.debug(msg)
            raise web.BadRequest(msg)
        except PolicyError as e:
            msg = "Internal server error: " + str(e)
            web.debug(msg)
            raise web.InternalError(msg)
        
    def DELETE(self, transferId):
        ''' DELETE /transfer/{ID}
        
        Deletes one transfer resource.
        '''
        if not transferId:
            msg = "Cannot DELETE the transfer resource without specifying a valid transfer resource ID in the path transfer/{ID}"
            web.debug(msg)
            raise web.BadRequest(msg)
        
        try:
            transferId = int(transferId)
            policy.remove(transferId)
        except TransferNotFound:
            msg = "Cannot DELETE transfer resource transfer/"+str(transferId)+". Resource not found."
            web.debug(msg)
            raise web.BadRequest(msg)


class Dump:
    def GET(self):
        ''' GET /dump
        
        Dumps the state of the policy module. For debug purpose only.
        '''
        (transfers, resources) = policy.dump()
        dump = web.Storage()
        dump.transfers = transfers
        dump.resources = resources
        return json.dumps(dump)


policy = Greedy()

urls = (
    '/transfer/(.*)', 'Transfer',
    '/transfer', 'Transfer',
    '/dump', 'Dump'
)

web.config.debug = False
app = web.application(urls, globals())


if __name__ == "__main__":
    app.run()
