'''
Created on Apr 11, 2013

@author: schuler
'''
import web
import json

urls = (
    '/transfer/(.*)', 'Transfer',
    '/transfer', 'CreateTransfer'
)
app = web.application(urls, globals())

class Transfer:
    def GET(self, transferid):
        if not transferid:
            transfers = [{"id":123}]
            return json.dumps(transfers)
        
        try:
            transfer = {'id':int(transferid)}
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
        
        d = web.data()
        try:
            transfer = json.loads(d)
            web.debug(json.loads(d))
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
        except ValueError as e:
            msg = "Not a valid transfer id: " + str(e)
            web.debug(msg)
            raise web.BadRequest(msg)
    
class CreateTransfer:
    def POST(self):
        d = web.data()
        try:
            transfer = json.loads(d)
            web.debug(json.loads(d))
            return d
        except ValueError as e:
            msg = "Invalid json request body: " + str(e)
            web.debug(msg)
            raise web.BadRequest(msg)

if __name__ == "__main__":
    app.run()
