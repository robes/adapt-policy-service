'''
Created on Apr 11, 2013

@author: schuler
'''
import web
import threading
from copy import deepcopy
from urlparse import urlparse
from policy import Policy, MalformedTransfer, TransferNotFound, NotAllowed, PolicyError

__all__ = ["Greedy"]


class Greedy(Policy):
    '''The greedy policy manager.'''
    
    def __init__(self, min_streams=0, initial_streams=8, update_incr_streams=8, max_streams=8, per_hosts_max_streams=36):
        '''Initialize the Greedy policy manager.
        
        Parameter 'min_streams' sets the minimum streams allocated when the 
        'max_streams' limit has been reached.
        
        Parameter 'initial_streams' sets the initial streams allocated per new 
        request.

        Parameter 'update_incr_streams' set the increment added to an update
        request's stream allocation.

        Parameter 'max_streams' sets the max streams per-pairwise endpoints.

        Parameter 'per_hosts_max_streams' sets the per hosts (i.e., per pair of
        host endpoints) max streams.
        '''
        self.lock = threading.Lock()
        self.next_transfer_id = 0L
        self.transfers = {}
        self.resources = {}
        self.initial_streams = initial_streams
        self.update_incr_streams = update_incr_streams
        self.max_streams = max_streams
        self.per_hosts_max_streams = per_hosts_max_streams
        self.min_streams = min_streams
        if min_streams > 0:
            # 'min_streams' as implemented in this class, has a bug in it
            # on 'remove()' if a minimum allocation was made the streams
            # will get added into the allocations log, thus temporarily
            # and incorrectly increasing the allocations available between 
            # the hosts
            raise NotImplementedError("Sorry 'min_streams' has not yet been implemented")
    
    
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
                if available >= self.initial_streams:
                    transfer.streams = self.initial_streams
                    self.resources[key] -= self.initial_streams
                elif available == 0:
                    transfer.streams = self.min_streams
                else:
                    transfer.streams = available
                    self.resources[key] = 0
            else:
                # This is the first allocation for these hosts, that we know of
                transfer.streams = self.initial_streams
                self.resources[key] = self.per_hosts_max_streams - self.initial_streams
            
            if transfer.streams == self.max_streams:
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
                
            # Get resource allocations key
            key = self.make_resources_key(transfer)
            if key not in self.resources:
                # This should not ever happen... it would be a bug if it did
                raise PolicyError("No allocation record for these endpoints (key="+key+")")
            
            # Make sure the src/dst match the original
            original = self.transfers[transferId]
            if transfer.source != original.source or \
                transfer.destination != original.destination:
                raise NotAllowed("Cannot change source or destination endpoitns during an update")
            
            # Set streams request to:
            #   -- requested streams if set by caller
            #   -- current + update_increment if not set by caller
            #   -- or initial streams if currently allocated to 0
            if not 'streams' in transfer or transfer.streams < 0:
                if original.streams == 0:
                    transfer.streams = self.initial_streams
                else:
                    transfer.streams = original.streams + self.update_incr_streams
            
            # Limit request attempt to the max_streams
            if transfer.streams > self.max_streams:
                transfer.streams = self.max_streams
            
            # Limit allocation up to available streams
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
            
            if original.streams == self.max_streams:
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
            if self.resources[key] > self.per_hosts_max_streams:
                self.resources[key] = self.per_hosts_max_streams

