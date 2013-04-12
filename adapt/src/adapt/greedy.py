'''
Created on Apr 11, 2013

@author: schuler
'''
import web
import json

urls = (
    '/transfer/(.*)', 'GetTransfer',
    '/transfer', 'CreateTransfer'
)
app = web.application(urls, globals())

class GetTransfer:
    def GET(self, transferid):
        if not transferid:
            raise web.BadRequest('You must call \'transfer/#identifier, where \'#identifier\' is the numeric identifier of the transfer.\'')
        
        transfer = {'id':transferid}
        return json.dumps(transfer)
    
class CreateTransfer:
    def POST(self):
        d = web.data()
        print d
        print json.loads(d)
        x = web.input()
        print x

if __name__ == "__main__":
    app.run()
