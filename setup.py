'''
Created on Apr 11, 2013

@author: schuler
'''
from distutils.core import setup
setup(name='policy-adapt',
      version='0.0',
      description='Adaptive transfer policy service',
      packages=['adapt'],
      package_dir={'': 'src'},
      #scripts=['bin/adapt-greedy']
      )
