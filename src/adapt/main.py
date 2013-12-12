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
The main and supporting routines for invocing the Policy Service from the 
command line.
"""

import web

import service
import config
from greedy import Greedy

__all__ = ['main']

def main(argv):
    """Main routine to run the Policy Service.
    
       The 'argv' argument should come from the system command line 
       arguments.
    """
    
    # Process argument list
    prog = argv[0]
    for arg in argv[1:]:
        if arg in ('-h', '--help'):
            _usage()
    
    if config.ssl:
        from web.wsgiserver import CherryPyWSGIServer
        CherryPyWSGIServer.ssl_certificate = config.ssl_certificate
        CherryPyWSGIServer.ssl_private_key = config.ssl_private_key
    
    #TODO: get arguments from cmdline, then set adapt.config.policy (and
    #  future system-wide config parameters) then continue
    config.policy = Greedy(**config.policy_defaults)
    
    ## web.py urls
    urls = (
        '/transfer/(.*)', service.Transfer,
        '/transfer', service.Transfer,
        '/dump', service.Dump
    )
    app = web.application(urls, globals())
    
    app.run()

def _usage(prog):
    print """
usage: %(prog)s [options...] [<config filename>]

Run this program to invoke the Policy Service.

  options:  --help                (print this message and quit)
            <config filename>     (load the configuration from
                                   this file)

Exit status:

  0  for success
  1  for usage error

""" % dict(prog=prog)
