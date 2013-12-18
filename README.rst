====================
ADAPT POLICY SERVICE
====================
--------------------
README
--------------------

INTRODUCTION
============
The Adapt Policy Service (PS) is a web service that manages transfer request 
resource allocations. In a distributed computing environment, file transfers 
between hosts depend on the availability of network resources, more 
specifically we only consider those resources most pertinent to file transfer 
performance. The most common resources allocations involve TCP buffer sizes at 
the client or the host, which is limited by available memory on the host, or 
parallel streams, which simply put is the number of parallel TCP connections 
used to transfer data between hosts. The PS helps transfer clients to cooperate
by issuing resource allocations. It is up to the transfer clients to cooperate 
by limiting their network utilization according to the request allocation.

USAGE
=====

This section covers basic usage and is intended for all users.

Installation
------------

The Policy Service requires Python 2.6+ and has been tested on Python 2.6 and 
2.7 on CentOS 6.x distributions of the Linux operating system. In addition to 
Python, the ``web.py`` Python web framework (see webpy.org_) is required.

To find out which version of Python you have installed use the following 
command. ::

	$ python --version

The following steps may be used in order to install the Adapt Policy Service 
and its prerequisites. Note that some of the following steps require 
administrative privileges (i.e., on Linux, the ability to run the ``sudo`` 
command).

1. Install ``web.py``, using one of the following options:

  a. (for Fedora or CentOS users) run ``yum install python-webpy``
     
  b. (for users of ``easy_install``) run ``easy_install web.py``
     
  c. (for all others) download the `web.py tarball`_, unpack it, and run 
     ``python setup.py install``. *If you do not have administrative 
     privileges*, unpack the tarball and set your ``PYTHONPATH`` to the 
     expanded directory. ::
     
     $ wget http://webpy.org/static/web.py-0.37.tar.gz
     $ tar -zxf web.py-0.37.tar.gz
     $ cd web.py-0.37
     $ sudo python setup.py install

2. Install the policy service

  a. (for users of ``easy_install``) run ``easy_install policy-service``
     (**NOT SUPPORTED YET**)
     
  b. (for all users) download the `policy service tarball`_, unpack it, 
     and run ``python setup.py install``. *If you do not have administrative 
     privileges*, unpack the tarball, set your ``PYTHONPATH`` to the expanded
     directory, and set your ``PATH`` to the ``sbin`` subdirectory. ::

     $ wget http://tbd.isi.edu/static/policy-service-0.10.tar.gz
     $ tar -zxf policy-service-0.10.tar.gz
     $ cd policy-service-0.10
     $ sudo python setup.py install

Running The Service
-------------------

- **Start** the Policy Service by running the following command. ::

  $ policy-service

- **Stop** the Policy Service with ``CTRL-C`` (i.e., ``^C``).

Limitations
-----------

- *Process does not detach*: the service currently does not detach and run
  in the background as a *daemon* process.

- *In memory state*: the service's state is retained in memory only. Therefore
  state is not maintained between service restarts.

- *Steam allocations only*: the default policy implementation (the ``Greedy`` 
  policy) only supports ``stream`` allocations.

- *CherryPy only*: although the service is built on ``web.py`` and as such is 
  compliant with the ``WSGI`` service side interface, the current implementation 
  requires a multithreaded web server, such as ``CherryPy``.


CONFIGURATION
=============

This section covers the configuration file and its parameters. It is intended 
for advanced users and system administrators.

The Configuration File
----------------------

At startup, the Policy Service may load the service configuration from a file. 
If no configuration file is found, the service will run with preset defaults.
To find the default location for your service, run the following command. ::

	$ policy-service --help

The installation process does not install a configuration file. An easy way to
create a new configuration file is to print the current configuration and save 
it in a file. To do this, for a Linux or UNIX shell, run the following command. ::

	$ policy-service --print-config > `policy-service --default-config`

The default configuration file contents will look similar to the following. ::

	{
	  "debug": false, 
	  "audit": false, 
	  "policy": {
	    "policy_class": "adapt.greedy.Greedy", 
	    "per_hosts_max_streams": 36, 
	    "initial_streams": 8, 
	    "update_incr_streams": 8, 
	    "max_streams": 8, 
	    "min_streams": 0
	  }, 
	  "ssl": {
	    "ssl_enabled": false, 
	    "ssl_private_key": "/path/to/ssl_private_key", 
	    "ssl_certificate": "/path/to/ssl_certificate"
	  }
	}

Configuration Parameters
------------------------

``debug``
    a flag to enable debug logging.
 
``audit``
    a flag to enable extended audit logging, in addition to the standard web 
    access logging.

``ssl``
    configuration section for ``SSL`` parameters.
     
``ssl_enabled``
    a flag to enable ``SSL`` for the ``HTTPS`` protocol.
 
``ssl_private_key``
    path to the private key file. *Note*: must be owned by the user that 
    launches the ``policy-service``.
 
``ssl_certificate``
    path to the certificate file. *Note*: must be owned by the user that 
    launches the ``policy-service``.

``policy``
    configuration section for policy parameters.
 
``policy_class``
    full package and classname for the policy implementation. **Note**: the 
    package must be resolvable on the ``PYTHONPATH``.


Aside from the ``policy_class``, all other parameters under the ``policy`` 
category are passed directly to the policy implementation. The following 
parameters are specific to the default policy implementation 
(``adapt.greedy.Greedy``).


``per_hosts_max_streams``
    the maximum total aggregate number of streams allocated between any two 
    pair of hosts.
 
``initial_streams``
    the initial stream allocation per request.
 
``update_incr_streams``
    the update increment for streams allocations.
    
``max_streams``
    the maximum steams allocated for a transfer resource.
 
``min_steams``
    the minimum streams allocation which are issued when the 
    ``per_hosts_max_streams`` has been reached. This can be used so that 
    transfer clients get at least some minimum number of steams rather than 
    starving a client of resources.


INTERFACE
=========

The following section covers the interface and protocol of the Policy Service. 
It is intended for deverlopers that wish to implement clients that will 
interact with the Policy Service.

Design
------

The PS is a web service and conforms to the REST architecture and protocol 
style. It is implemented on the Web.py framework and runs on the CherryPy web 
server. It supports JSON message bodies for resource representations.

Web Service
-----------

As a RESTful web service, the PS is defined by the definitions of its 
resources, representations, and supported methods.

Resources
~~~~~~~~~

The Policy Service supports a single formally defined resource, the 
``Transfer`` resource, which represents a transfer allocation request. A client
requests an allocation of transfer resources (here we must use the term 
*resource* again but in this case to mean the network resources, not to be 
confused with the RESTful sense of a resource). The PS maintains state about 
the client requests as ``Transfer`` resources (in the RESTful sense).

Representations
~~~~~~~~~~~~~~~

The ``Transfer`` resource has a JSON *representation*. This means that the PS 
accepts and returns a JSON representation of a ``Transfer`` resource during 
client requests. ::

	{
	  "id": "integer",
	  "source": "url",
	  "destination": "url",
	  "streams": "integer"
	}

During certain operations, some of the fields are unnecessary, such as the ``id``
field when initially requesting the transfer allocation. Also, ``streams`` is not
necessary during the initial request as it is usually determined by the PS.

In some operations the representation of the resource is not one transfer but
instead a list of transfer resources. These have the same representation except
that they are wrapped in a JSON dictionary keyed by the ``id``. ::

	{
	  "0": {
	    <transfer resource body>
	  },
	  "N": {
	    <transfer resource body>
	  }
	}

Methods
~~~~~~~

-  **CREATE TRANSFER**
 
   HTTP: ``POST /transfer`` sending transfer representation in ``body``
 
   This operation will create a new transfer allocation. It will invoke the 
   policy logic to determine what and how many resources are available for
   the requesting client. In the body, the ``source`` and ``destination`` must be 
   specified, but the ``id`` should not be specified. If successful, the ``body`` 
   of the response will be a transfer representation with the ``id`` and the 
   allocation parameters (e.g., ``streams``) filled in.
    
-  **LIST ALL TRANSFERS**
 
   HTTP: ``GET /transfer`` (empty ``body``)
    
   This operation will return all of the transfer allocations in the PS. They 
   will be returned as a JSON list of transfer representations in the ``body`` 
   of the response.

-  **GET A TRANSFER**
 
   HTTP: ``GET /transfer/{ID}`` where ``{ID}`` is a valid transfer resource 
   identifier.
 
   This operation will return a single transfer allocation from the PS. 
   It will be returned as a JSON transfer representation in the ``body`` of the 
   response. Error ``404 NOT FOUND`` will be returned if there is no resource 
   with ``id`` matching ``{ID}``.

-  **UPDATE A TRANSFER**
 
   HTTP: ``PUT /transfer/{ID}`` where ``{ID}`` is a valid transfer resource 
   identifier and the ``body`` of the message is a JSON transfer resource 
   representation.
    
   This operation allows the client to send a request to the PS asking it to 
   update an existing transfer allocation. This operation is typically used 
   when a client wishes to increase its resource allocations (e.g., increase 
   the number of streams that have been allocated to it) from its initial 
   allocation. The operation returns the revised allocation in the form of a
   JSON transfer representation in the ``body`` of the response. Error ``404 NOT 
   FOUND`` will be returned if there is no resource with ``id`` matching ``{ID}``.

-  **DELETE A TRANSFER**
 
   HTTP: ``DELETE /transfer/{ID}`` where ``{ID}`` is a valid transfer resource 
   identifier.
 
   This operation deletes a transfer resource from the PS. The PS returns the 
   allocated resources to the pool of available resources. Error ``404 NOT 
   FOUND`` will be returned if there is no resource with ``id`` matching ``{ID}``. 

-  **DUMP INTERNAL STATE** (*DEBUG ONLY*)
 
   HTTP: ``GET /dump``
 
   This operation is for *debug purposes only*. It returns the internal state 
   of the PS. The representation is not formally defined because of the 
   diagnostic nature of this operation.

Samples
-------

The best way to learn how to use the service is to run simple HTTP(S) client 
commands off of the commandline. In the source tarball, several scripts that
use the ``curl`` command are included. See the ``samples`` subdirectory. *Note* 
that the following examples assume that the sample scripts are run from the 
same host as the ``policy-service`` is running on.

To create a transfer, run ``create-new-transfer.sh`` and look inside of 
``new-transfer-body.json`` to see what was sent in the body.

Now, take a look at the newly created transfer by running ``get-transfer.sh``. 
This will return the complete list (albeit just one) of transfer resources
in the service.

To update the new transfer, run ``update-transfer.sh 0`` where the ``0`` gets 
concatenated with the base ``URL`` in order to direct the update to the ``0th`` 
indexed transfer request.

Now, dump the state of the service using the ``dump-state.sh`` script. You will 
notice that the state includes the aggregate resource allocations, and not 
just the listing of transfer requests.

To delete the transfer, run ``delete-transfer.sh 0`` where again the ``0`` is 
used to specify the ``0th`` transfer in the service.

Finally, to get familiar with *what not to do*, take a look at ``malformed.json``
and run ``error.sh``.


DEVELOPING POLICIES
===================

*This section is intended for advanced developers that wish to implement new 
resource allocation policies.*

The PS was designed to be extended with custom Policy implementations. The 
``policy`` module in the ``adapt`` package defines the interface for policies.

To create new policies, one must first implement a class that inherits from 
the ``adapt.policy.Policy`` class. The behavior of its methods must conform to
the ``docstring``s of the ``Policy`` class's methods and must only throw those 
exceptions as defined in the ``adapt.policy`` module.

As described in the *configuration* section, the ``policy_class`` parameter tells
the policy service which policy to use. *Note* that the policy class must be 
loadable from the ``PYTHONPATH``. All additional parameters from the ``policy`` 
section of the configuration are passed to the constructor of the 
``policy_class`` as keyword arguments.

To learn more about the ``adapt.policy.Policy`` interface, inspect its 
``docstrings``. The following sequence can be followed. ::

	$ python
	Python 2.6.6 (r266:84292, Jul 10 2013, 22:48:45) 
	[GCC 4.4.7 20120313 (Red Hat 4.4.7-3)] on linux2
	Type "help", "copyright", "credits" or "license" for more information.
	>>> import adapt
	>>> help(adapt.policy.Policy)
	
	...docstrings printed here...

Similarly, ``help(adapt.policy)`` will print the ``docstrings`` for the 
``adapt.policy`` module, which includes the listing of exceptions defined in 
the module.

.. _webpy.org: http://webpy.org
.. _`web.py tarball`: http://webpy.org/static/web.py-0.37.tar.gz
.. _`policy service tarball`: http://tbd.isi.edu/static/policy-service-0.1.tar.gz
