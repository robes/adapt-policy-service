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
The REST service handlers for use with the web.py framework.
"""

import web
import json

from config import config
from policy import MalformedTransfer, TransferNotFound, NotAllowed, PolicyError

__all__ = ['Transfer', 'Dump']

policy = None

class Transfer:
    '''RESTful web handler for Transfer resources.'''
    
    def POST(self, transferId=None):
        '''POST /transfer
        
        Not allowed to POST /transfer/{ID}.
        '''
        if transferId:
            raise web.NoMethod()
        
        try:
            raw = web.data()
            if config.audit:
                web.debug("Request Body: " + str(raw))
            parsed = json.loads(raw)
            transfer = web.storify(parsed)
            transfer = policy.add(transfer)
            return json.dumps(transfer)
        except MalformedTransfer as e:
            msg = "Bad request body in POST to transfer/ resource: " + str(e)
            web.debug(msg)
            raise web.BadRequest(msg)
    
    def GET(self, transferId=None):
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
            if config.audit:
                web.debug("Request Body: " + str(raw))
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
    '''Diagnostics for Transfer service.'''
    
    def GET(self):
        ''' GET /dump
        
        Dumps the state of the policy module. For debug purpose only.
        '''
        (transfers, resources) = policy.dump()
        dump = web.Storage()
        dump.transfers = transfers
        dump.resources = resources
        return json.dumps(dump)
