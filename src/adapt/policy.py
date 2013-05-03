'''
Created on May 3, 2013

@author: schuler
'''

__all__ = ["Policy", "PolicyException", "MalformedTransfer",
            "TransferNotFound", "NotAllowed", "PolicyError"]

class PolicyException(Exception):
    '''Base exception class should not be used directly.'''
    def __init__(self, msg):
        super(PolicyException, self).__init__(msg)

class MalformedTransfer(PolicyException):
    '''Indicates malformed transfer object.'''
    def __init__(self, msg="Malformed transfer object"):
        super(PolicyException, self).__init__(msg)

class TransferNotFound(PolicyException):
    '''Could not find the transfer object.'''
    def __init__(self, msg="Transfer object not found"):
        super(PolicyException, self).__init__(msg)

class NotAllowed(PolicyException):
    '''Operation not allowed.
    
    Examples include, attempting to change the source or destination of a 
    transfer resource allocation.
    '''
    def __init__(self, msg="Not allowed"):
        super(PolicyException, self).__init__(msg)

class PolicyError(Exception):
    '''Indicates an internal error has occurred within the policy module.'''
    def __init__(self, msg="An internal error has occurred"):
        super(PolicyException, self).__init__(msg)

class Policy:
    '''The policy manager.'''
    
    def add(self, transfer):
        '''Adds a transfer resource to the policy manager.
        
        A 'transfer' dictionary is expected.
        
        Raises 'MalformedTransfer' if the transfer dictionary does not contain
        a 'source' or 'destination' key.
        
        Returns the 'transfer' dictionary with its resource allocations set.
        '''
        raise NotImplementedError
    
    def get(self, transferId):
        '''Returns a transfer dictionary.
        
        A valid integer 'transferId' is expected.
        
        Raises 'TransferNotFound' if transferId does not map to a transfer 
        object.
        
        Returns the 'transfer' dictionary.
        '''
        raise NotImplementedError
    
    def update(self, transferId, transfer):
        '''Updates a transfer and returns the current state of it.
        
        A valid integer 'transferId' is expected.
        
        A valid 'transfer' dictionary is expected.
          transfer['streams'] > 0 : the client wants to adjust to the new level.
          transfer['streams'] < 0 : the client wants as many as it can get.
        
        Raises 'TransferNotFound' if transferId does not map to a transfer 
        object.
        
        Raises 'MalformedTransfer' if the transfer dictionary does not contain
        a 'source' or 'destination' key.
        
        Raises 'NotAllowed' if the client attempts to alter the transfer 
        in any way other than its resource allocations.
        
        Returns the 'transfer' dictionary.
        '''
        raise NotImplementedError
    
    def remove(self, transferId):
        '''Removes a transfer and frees up allocated resources.
        
        A valid integer 'transferId' is expected.
        
        Raises 'TransferNotFound' if transferId does not map to a transfer 
        object.
        '''
        raise NotImplementedError
    
    def all(self):
        '''Returns a dictionary of all transfer resources, keyed on transfer id.'''
        raise NotImplementedError
    
    def dump(self):
        '''Dumps the state of the policy.
        
        The policy manager does not specify the output of this function. It is
        left up to the policy implementation to decide what state it will dump.
        It is expected that the output of dump will be used for diagnostic 
        purposes only.'''
        raise NotImplementedError
