'''
Created on Apr 11, 2013

@author: schuler
'''
import web
import json
import threading
import copy

class Greedy:
    '''The greedy policy manager.'''
    
    def __init__(self):
        self.lock = threading.Lock()
        self.transfers = []
    
    def all(self):
        with self.lock:
            web.debug("all")
            return copy.deepcopy(self.transfers)
    
    def add(self, transfer):
        with self.lock:
            web.debug("add: " + str(transfer))
            transfer["id"] = len(self.transfers)
            self.transfers.append(transfer)
            return copy.deepcopy(transfer)
    
    def get(self, id):
        with self.lock:
            web.debug("get: " + str(id))
            if id < len(self.transfers):
                return copy.deepcopy(self.transfers[id])
            else:
                return None
    
    def update(self, id, transfer):
        with self.lock:
            web.debug("update: " + str(id) + ": " + str(transfer))
            if id < len(self.transfers):
                self.transfers[id] = transfer
                return copy.deepcopy(transfer)
            else:
                return None
    
    def remove(self, id):
        with self.lock:
            web.debug("remove: " + str(id))
            if id < len(self.transfers):
                self.transfers[id] = None


class Transfer:
        
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
            d = web.data()
            transfer = json.loads(d)
            policy.update(id, transfer)
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


class CreateTransfer:
    def POST(self):
        d = web.data()
        try:
            transfer = json.loads(d)
            transfer = policy.add(transfer)
            return json.dumps(transfer)
        except ValueError as e:
            msg = "Invalid json request body: " + str(e)
            web.debug(msg)
            raise web.BadRequest(msg)


urls = (
    '/transfer/(.*)', 'Transfer',
    '/transfer', 'CreateTransfer'
)
policy = Greedy()
app = web.application(urls, globals())


if __name__ == "__main__":
    app.run()
