Here is a start at a new lightweight policy service. I attempted to conform to the previous RESTful interface as much as possible, or as much as seemed reasonable. This is a prototype that will improve. It is written in python using the web.py framework, which is one of the lightest web frameworks out there.

So far, the only requirement is to have the python "web.py" package installed. I plan to package web.py with our code and automatically add it to the PYTHONPATH env variable so that the end user will not have to do anything except download our package, untar it, and run the command.

But at this moment you need to supply web.py, which can be done either by:
1. yum install python-webpy (requires sudo);
2. pip install web.py (requires sudo);
3. downloading web.py, unpacking it, and either installing it (requires sudo) or setting your PYTHONPATH appropriately.

Limitations:
* It keeps state in memory only
* It only supports steam allocations, but not rate allocations
* The restful interface has changed a bit from the previous java version, but I think this is a simplification 
* It probably doesn't work exactly like it used to
* It probably doesn't work exactly like it is supported to (!) and needs fixing based on feedback

Configuration:
* The only configuration parameters are some global variables at the top of the greedy.py file.
* MAX_STREAMS = the maximum streams to allocate per-pairwise hosts
* MIN_STREAMS = the minimum streams to allocate once the allocations are all given out
* DEFAULT_STREAMS = the default streams to give to a new request, if available

Once you have web.py installed, you can:
* run the service by entering: python greedy.py
* stop it by hitting: ^C
* learn how to use it and run examples by running bash scripts in the test/ subdirectory

The whole service fits in one python file, greedy.py, of about 220+ lines of code (counting white spaces).
