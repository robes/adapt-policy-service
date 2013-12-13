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
The policy service configuration.

The 'audit' parameter enables/disables additional request audit logging.

The 'debug' parameter enables/disables debug logging mode.

The 'ssl_enabled' parameter enables/disables HTTPS.

The 'ssl_certificate' and 'ssl_private_key' must be set to the paths of a 
valid host certificate and key. The files must be owned by the same user
that runs the policy service.

The 'policy' configuration parameter should be set to a properly initialized
policy manager, a sub-class of adapt.policy.Policy.

The 'policy_defaults' define the defaults settings passed to the policy
implementation.
"""

import os
import json
import web

__all__ = ['config', 'load_config', 'default_config_filename']

default_config_filename = '~/policyservice.conf'

__logging_defaults = dict(audit=False, 
                debug=False)

__ssl_defaults = dict(ssl_enabled=False,
            ssl_certificate = "/path/to/ssl_certificate",
            ssl_private_key = "/path/to/ssl_private_key")

__policy_defaults = dict(policy_class='adapt.greedy.Greedy',
               min_streams=0, 
               initial_streams=8, 
               update_incr_streams=8, 
               max_streams=8, 
               per_hosts_max_streams=36)

config = web.Storage()
config.logging = web.storify(__logging_defaults)
config.ssl = web.storify(__ssl_defaults)
config.policy = web.storify(__policy_defaults)

def load_config(filename=None):
    """Load configuration from file and merge with defaults.
    
       The 'filename' of the configuration file.
    """
    # load config file
    if filename and filename.startswith('~'):
        filename = os.path.expanduser(filename)
    if not filename or not os.path.exists(filename):
        filename = os.path.expanduser(default_config_filename)
        if not os.path.exists(filename):
            return
    
    if config.logging.debug:
        web.debug("Loading configuration from: " + filename)
    
    input = json.load(file(filename))

    # merge config
    for topic in input:
        if topic not in config:
            continue
        for key in input[topic]:
            if key not in input[topic]:
                continue
            config[topic][key] = input[topic][key]
