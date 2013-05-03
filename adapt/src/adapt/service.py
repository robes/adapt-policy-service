'''
Created on May 3, 2013

@author: schuler
'''
import web
import json
from policy import MalformedTransfer, TransferNotFound, NotAllowed, PolicyError
from greedy import Greedy
from config import policy

__all__ = ["Transfer", "Dump"]

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


#policy = Greedy()

urls = (
    '/transfer/(.*)', 'Transfer',
    '/transfer', 'Transfer',
    '/dump', 'Dump'
)

web.config.debug = False
app = web.application(urls, globals())


if __name__ == "__main__":
    #TODO: get arguments from cmdline, then set adapt.config.policy (and
    #  future system-wide config parameters) then continue
    policy = Greedy()
    
    app.run()
