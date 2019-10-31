"""`appengine_config` gets loaded when starting a new application instance."""
import os
import sys
import logging
from google.appengine.ext import vendor
# add `lib` subdirectory to `sys.path`, so our `main` module can load
# third-party libraries.
vendor.add(os.path.join(os.path.dirname(os.path.realpath(__file__)), 'lib'))
logging.info(os.environ.get('SERVER_SOFTWARE', ''))
if not os.environ.get('SERVER_SOFTWARE', '').startswith('Google App Engine'):
    if os.name == 'nt' and not os.environ.get('SERVER_SOFTWARE', '') == "":
        os.name = None
        sys.platform = ''
