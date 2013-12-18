# 
# Copyright 2013 University of Southern California
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#    http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
"""
The greedy policy manager.
"""

import web
import threading
from copy import deepcopy
from urlparse import urlparse

from config import config
from policy import Policy, MalformedTransfer, TransferNotFound, NotAllowed, PolicyError

__all__ = ["Greedy"]


class Greedy(Policy):
    '''The greedy policy manager.'''
    
    def __init__(self, **kwargs):
        '''Initialize the Greedy policy manager.
        
        Keyword Arguments:
        
        Parameter 'min_streams' sets the minimum streams allocated when the 
        'per_hosts_max_streams' limit has been reached.
        
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
        self.initial_streams = kwargs.get('initial_streams', 8)
        self.update_incr_streams = kwargs.get('update_incr_streams', 8)
        self.max_streams = kwargs.get('max_streams', 8)
        self.per_hosts_max_streams = kwargs.get('per_hosts_max_streams', 36)
        self.min_streams = kwargs.get('min_streams', 0)
    
    
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
            if config.debug:
                web.debug("all")
            return deepcopy(self.transfers)
    
    
    def add(self, transfer):
        '''Adds a transfer resource to the greedly policy manager.
        
        Allocates the default streams, if available, the balance if not, 
        and the minimum if below threshold.
        
        A 'transfer' dictionary is expected.
        '''
        with self.lock:
            if config.debug:
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
                    self.resources[key] -= self.min_streams
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
                if config.debug:
                    web.debug("Granting %s streams" % delta)
                original.streams += delta
                self.resources[key] -= delta
            elif available > 0:
                if config.debug:
                    web.debug("Granting %s streams" % available)
                original.streams += available
                self.resources[key] = 0
            else:
                if config.debug:
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
