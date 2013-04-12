'''
Created on Apr 11, 2013

@author: schuler
'''
from distutils.core import setup
setup(name='isi-adapt',
      version='0.0',
      description='ISI Adaptive transfer policy service',
      packages=['adapt'],
      package_dir={'': 'src'},
      #package_data={'tagfiler.iobox': ['sql/*.sql']},
      scripts=['bin/adapt-greedy']
      )
