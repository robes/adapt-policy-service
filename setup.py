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

from distutils.core import setup

setup(
    name='policy-service',
    version='0.1-dev',
    description='The Adapt Policy Service',
    long_description=open('README.rst').read(),
    url='http://www.isi.edu/~schuler/adapt.html',
    download_url='http://www.isi.edu/~schuler/static/policy-service-0.1-dev.tar.gz',
    keywords='grid, hpc, file fransfer',
    maintainer='adapt dev list',
    maintainer_email='adapt@hpcrdm.lbl.gov',
    packages=['adapt'],
    scripts=['sbin/policy-service'],
    requires=['web.py (>= 0.37)'],
    license='Apache License, Version 2.0',
    classifiers=[
        'Development Status :: 3 - Alpha',
        'Environment :: Web Environment',
        'Intended Audience :: Developers',
        'Intended Audience :: System Administrators',
        'License :: OSI Approved :: Apache Software License',
        'Operating System :: POSIX :: Linux',
        'Programming Language :: Python',
        'Programming Language :: Python :: 2.6',
        'Programming Language :: Python :: 2.7',
        'Topic :: System :: Distributed Computing',
        'Topic :: System :: Networking'
    ])
