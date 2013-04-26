'''
Created on Apr 11, 2013

@author: schuler
'''
import web
import json
import threading
from copy import deepcopy
from urlparse import urlparse

MAX_STREAMS     = 36 # Max streams per-pairwise endpoints
DEFAULT_STREAMS = 8  # Default streams allocated per new request
MIN_STREAMS     = 0  # Minimum streams allocated when over-allocated

class Greedy:
    '''The greedy policy manager.'''
    
    def __init__(self):
        self.lock = threading.Lock()
        self.next_transfer_id = 0L
        self.transfers = {}
        self.allocations = {}
        self.max_streams = MAX_STREAMS
        self.default_streams = DEFAULT_STREAMS
        self.min_streams = MIN_STREAMS
    
    
    def dump(self):
        return (self.transfers, self.allocations)
    
    
    def make_allocation_key(self, transfer):
        '''Create the key out of the source and destination hostnames.
        
        Raises ValueError, if cannot parse hostname from URL.
        '''
        if not 'source' in transfer:
            raise ValueError("No transfer source")
        if not 'destination' in transfer:
            raise ValueError("No transfer destination")
        
        srchost = urlparse(transfer.source).hostname
        dsthost = urlparse(transfer.destination).hostname
        if srchost is None:
            raise ValueError("No hostname in URL: " + transfer.source)
        elif dsthost is None:
            raise ValueError("No hostname in URL: " + transfer.destination)
        
        return srchost + "::" + dsthost
    
    
    def all(self):
        with self.lock:
            web.debug("all")
            return deepcopy(self.transfers)
    
    
    def add(self, transfer):
        with self.lock:
            web.debug("add: " + str(transfer))
            key = self.make_allocation_key(transfer)
            
            # Add transfer to collection and increment counter
            transfer.id = self.next_transfer_id
            self.transfers[self.next_transfer_id] = transfer
            self.next_transfer_id += 1L
            
            # Allocate as many streams as possible up to the default
            if key in self.allocations:
                available = self.allocations[key]
                if available >= self.default_streams:
                    transfer.streams = self.default_streams
                    self.allocations[key] -= self.default_streams
                elif available == 0:
                    transfer.streams = self.min_streams
                else:
                    transfer.streams = available
                    self.allocations[key] = 0
            else:
                transfer.streams = self.default_streams
                self.allocations[key] = self.max_streams - self.default_streams
            
            return deepcopy(transfer)
    
    
    def get(self, id):
        with self.lock:
            web.debug("get: " + str(id))
            if not self.transfers.has_key(id):
                raise web.NotFound("No such transfer")
            else:
                return deepcopy(self.transfers[id])
    
    def update(self, id, transfer):
        with self.lock:
            web.debug("update: " + str(id) + ": " + str(transfer))
            if not self.transfers.has_key(id):
                raise web.NotFound("No such transfer")
            if transfer.streams < 0:
                raise web.BadRequest("Cannot request negative streams")
            
            # Make sure the src/dst match the original
            original = self.transfers[id]
            if transfer.source != original.source or \
                transfer.destination != original.destination:
                raise web.Conflict("Transfer source/destination must not change")
            
            # Allocate as many streams as possible up to the default
            key = self.make_allocation_key(transfer)
            if key not in self.allocations:
                raise web.Conflict("Internal error: No allocations for this key")
            
            available = self.allocations[key]
            requested = transfer.streams
            delta = requested - original.streams
            
            if available >= delta:
                web.debug("Granting %s streams" % delta)
                original.streams += delta
                self.allocations[key] -= delta
            elif available > 0:
                web.debug("Granting %s streams" % available)
                original.streams += available
                self.allocations[key] = 0
            else:
                raise web.BadRequest("No streams available to allocate")
            
            return deepcopy(original)
    
    def remove(self, id):
        with self.lock:
            web.debug("remove: " + str(id))
            if not self.transfers.has_key(id):
                raise web.NotFound("No such transfer")
            
            transfer = self.transfers[id]
            del self.transfers[id]
            
            # "Return" the allocations to the pool
            key = self.make_allocation_key(transfer)
            self.allocations[key] += transfer.streams
            if self.allocations[key] > self.max_streams:
                self.allocations[key] = self.max_streams


class Transfer:
    def POST(self, transferid=None):
        '''POST /transfer
        
        Not allowed to POST /transfer/{ID}.
        '''
        if transferid:
            raise web.NoMethod()
        
        raw = web.data()
        try:
            parsed = json.loads(raw)
            transfer = web.storify(parsed)
            transfer = policy.add(transfer)
            return json.dumps(transfer)
        except ValueError as e:
            msg = "Bad request body: " + str(e)
            web.debug(msg)
            raise web.BadRequest(msg)
    
    def GET(self, transferid):
        if not transferid:
            transfers = policy.all()
            return json.dumps(transfers)
        
        try:
            id = int(transferid)
            transfer = policy.get(id)
            return json.dumps(transfer)
        except ValueError as e:
            msg = "Not a valid transfer id: " + str(e)
            web.debug(msg)
            raise web.BadRequest(msg)
        
    def PUT(self, transferid):
        if not transferid:
            msg = "No transfer id"
            web.debug(msg)
            raise web.BadRequest(msg)
        
        try:
            id = int(transferid)
        except ValueError as e:
            msg = "Not a valid transfer id: " + str(e)
            web.debug(msg)
            raise web.BadRequest(msg)
        
        try:
            raw = web.data()
            parsed = json.loads(raw)
            transfer = web.storify(parsed)
            transfer = policy.update(id, transfer)
            return json.dumps(transfer)
        except ValueError as e:
            msg = "Invalid json request body: " + str(e)
            web.debug(msg)
            raise web.BadRequest(msg)
        
    def DELETE(self, transferid):
        if not transferid:
            msg = "No transfer id"
            web.debug(msg)
            raise web.BadRequest(msg)
        
        try:
            id = int(transferid)
            policy.remove(id)
        except ValueError as e:
            msg = "Not a valid transfer id: " + str(e)
            web.debug(msg)
            raise web.BadRequest(msg)


class Dump:
    def GET(self):
        (transfers, allocations) = policy.dump()
        dump = web.Storage()
        dump.transfers = transfers
        dump.allocations = allocations
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
