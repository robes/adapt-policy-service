#!/usr/bin/env python
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

The 'policy' configuration parameter should be set to a properly initialized
policy manager, a sub-class of adapt.policy.Policy.

The 'ssl' parameter enables/disables HTTPS.

The 'ssl_certificate' and 'ssl_private_key' must be set to the paths of a 
valid host certificate and key. The files must be owned by the same user
that runs the policy service.
"""

policy = None

ssl = False
ssl_certificate = "/path/to/ssl_certificate"
ssl_private_key = "/path/to/ssl_private_key"
