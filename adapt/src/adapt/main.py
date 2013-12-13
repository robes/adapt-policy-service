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

import sys
import os
import json
import web

import service
from config import config, load_config, default_config_filename
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
    prog = os.path.basename(args[0])
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
            config.logging.debug = True
        elif arg.startswith(__OPT_IPADDR):
            opt_ipaddr = arg[len(__OPT_IPADDR):]
        elif arg.startswith(__OPT_PORT):
            opt_port = arg[len(__OPT_PORT):]
        else:
            _usage(prog)
            return 1
    
    # Reconstruct sys.argv for web.py: ['prog', 'ipaddr:port']
    sys.argv = [args[0], ':'.join([e for e in [opt_ipaddr, opt_port] if e])]
    
    try:
        load_config(opt_config_filename)
    except ValueError, err:
        print >> sys.stderr, ('ERROR: ' + str(err))
        return 2
    
    if opt_debug:
        config.logging.debug = True
    if config.logging.debug:
        web.config.debug = True
        print >> sys.stderr, ('ARGS: ' + str(args))
        print >> sys.stderr, ('CONFIG: ' + str(json.dumps(config)))
        
    if opt_print_config:
        print json.dumps(config, indent=2)
        return 0
    
    if config.ssl.ssl_enabled:
        from web.wsgiserver import CherryPyWSGIServer
        CherryPyWSGIServer.ssl_certificate = config.ssl.ssl_certificate
        CherryPyWSGIServer.ssl_private_key = config.ssl.ssl_private_key
    
    # dynamically load the policy implementation
    try:
        policy_class = _dynamic_load(config.policy.policy_class)
        service.policy = policy_class(**config.policy)
    except Exception, e:
        print >> sys.stderr, 'ERROR: Unable to load policy class: ' \
                               + config.policy.policy_class
        print >> sys.stderr, 'ERROR: Cause: ' + str(e)
        return 2
    
    # web.py urls
    urls = (
        '/transfer/(.*)', service.Transfer,
        '/transfer', service.Transfer,
        '/dump', service.Dump
    )
    app = web.application(urls, globals())
    app.run()


def _dynamic_load(policyname):
    """Dynamically loads a module or class.
    
       The 'policyname' specifies the module or class and is expected to
       include '.' path separators, however, (sub)packages are not required.
    """
    names = policyname.split('.')
    policy = __import__(names[0])
    for name in names[1:]:
        policy = getattr(policy, name)
    return policy


def _usage(prog):
    print """
usage: %(prog)s [options...]

Run the Policy Service.

  options:  --help             (print this message and quit)
            --debug            (more verbose logging)
            --print-config     (print the configuration and quit)
            --config=<file>    (load configuration from <file>;
                                default: %(default)s)
            --ipaddr=<ipaddr>  (listen on <ipaddr>)
            --port=<port>      (listen on <port>)

Exit status:

  0  for success
  1  for usage error
  2  for configuration error

""" % dict(prog=prog, default=default_config_filename)
