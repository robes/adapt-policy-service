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
import json
import sys

import service
from config import config, load_config
from greedy import Greedy

__all__ = ['main']


__OPT_HELP='--help'
__OPT_DEBUG='--debug'
__OPT_PRINT_CONFIG='--print-config'
__OPT_CONFIG='--config='
__OPT_IPADDR='--ipaddr='
__OPT_PORT='--port='


def main():
    """Main routine to run the Policy Service.
    
       This routine processes the 'sys.argv' command line arguments.
    """
    opt_debug = False
    opt_print_config = False
    opt_config_filename = None
    opt_ipaddr = None
    opt_port = None
    
    # Process argument list
    args = sys.argv
    prog = args[0]
    for arg in args[1:]:
        if arg in (__OPT_HELP):
            _usage(prog)
            return 0
        elif arg in (__OPT_PRINT_CONFIG):
            opt_print_config = True
        elif arg.startswith(__OPT_CONFIG):
            opt_config_filename = arg[len(__OPT_CONFIG):]
        elif arg in (__OPT_DEBUG):
            opt_debug = True
        elif arg.startswith(__OPT_IPADDR):
            opt_ipaddr = arg[len(__OPT_IPADDR):]
        elif arg.startswith(__OPT_PORT):
            opt_port = arg[len(__OPT_PORT):]
        else:
            _usage(prog)
            return 1
    
    # Reconstruct sys.argv for web.py: ['prog', 'ipaddr:port']
    sys.argv = [args[0], ':'.join([e for e in [opt_ipaddr, opt_port] if e])]
    
    load_config(opt_config_filename)
    if opt_print_config:
        print json.dumps(config, indent=2)
    
    if opt_debug:
        config.logging.debug = True
    if config.logging.debug:
        web.config.debug = True
        print >> sys.stderr, ('args: ' + str(args))
        print >> sys.stderr, ('config: ' + str(json.dumps(config)))
    
    if config.ssl.ssl_enabled:
        from web.wsgiserver import CherryPyWSGIServer
        CherryPyWSGIServer.ssl_certificate = config.ssl.ssl_certificate
        CherryPyWSGIServer.ssl_private_key = config.ssl.ssl_private_key
    
    #TODO: get arguments from cmdline, then set adapt.config.policy (and
    #  future system-wide config parameters) then continue
    service.policy = Greedy(**config.policy)
    
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

Run the Policy Service.

  options:  --help             (print this message and quit)
            --debug            (more verbose logging)
            --print-config     (print the configuration and quit)
            --config=<file>    (load configuration from <file>)
            --ipaddr=<ipaddr>  (listen on <ipaddr>)
            --port=<port>      (listen on <port>)

Exit status:

  0  for success
  1  for usage error

""" % dict(prog=prog)
