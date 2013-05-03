TRANSFER POLICY SERVICE


INTRODUCTION

Here is a start at a new lightweight policy service. I attempted to conform to the previous RESTful interface as much as possible, or as much as seemed reasonable. This is a prototype that will improve. It is written in python using the web.py framework, which is one of the lightest web frameworks out there.

INTERFACE

RESTful methods to the policy service.

* POST /transfer
    Creates a transfer resource.
    
* GET /transfer
    Returns a JSON representation of a list of transfer resources.

* GET /transfer/{ID}
    Returns a JSON representation of a transfer resource.

* PUT /transfer/{ID}
    Updates a transfer resource by sending a JSON representation of a transfer resource.

* DELETE /transfer/{ID}
    Deletes a transfer resource.

* GET /dump
    This is for debug purposes only. Dumps the states of the policy service.


Transfer resource representation (in JSON):

-----
{
 "id": integer,
 "source": url,
 "destination": url,
 "streams": integer
}
-----

When getting a list of transfer resources (in JSON):

-----
[ {transfer-resource},... ]
-----

INSTALLATION

So far, the only requirement is to have the python "web.py" package installed. I plan to package web.py with our code and automatically add it to the PYTHONPATH env variable so that the end user will not have to do anything except download our package, untar it, and run the command.

But at this moment you need to supply web.py, which can be done either by:
1. yum install python-webpy (requires sudo);
2. pip install web.py (requires sudo);
3. downloading web.py, unpacking it, and either installing it (requires sudo) or setting your PYTHONPATH appropriately.

LIMITATIONS

* It keeps state in memory only
* It only supports stream allocations, but not rate allocations
* Configuration is a hack

CONFIGURATION

* Right now, the way to configure the service is to (a) first see adapt.greedy.Greedy.__init__() to understand the three parameters that affect the Greedy policy, then (b) edit adapt.service.__main__() to initialize the Greedy() policy manager as desired.

RUNNING

Once you have web.py installed, you can:
* add the 'adapt' module to your PYTHONPATH
* run the service by entering: python service.py
* stop it by hitting: ^C

TESTING

Learn how to use it and run examples by running bash scripts in the test/ subdirectory
