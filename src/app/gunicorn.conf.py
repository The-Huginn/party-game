worker_tmp_dir = '/dev/shm'
workers = 3
worker_connections = 1000
timeout = 10
bind = '0.0.0.0:5000'
accesslog = '-'
errorlog = '-'
loglevel='debug'

import logging
from gunicorn import glogging


class CustomGunicornLogger(glogging.Logger):

    def setup(self, cfg):
        super().setup(cfg)

        # Add filters to Gunicorn logger
        logger = logging.getLogger("gunicorn.access")
        logger.addFilter(HealthCheckFilter())

class HealthCheckFilter(logging.Filter):
    def filter(self, record):
        return 'GET /health' not in record.getMessage() and 'GET /ready' not in record.getMessage()

accesslog = '-'
logger_class = CustomGunicornLogger

def pre_request(worker, req):
    if req.path == '/health' or req.path == '/ready':
        return
    worker.log.debug("%s %s" % (req.method, req.path))