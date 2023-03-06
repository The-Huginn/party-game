worker_tmp_dir = '/dev/shm'
workers = 3
worker_connections = 1000
timeout = 10
bind = '0.0.0.0:5000'
accesslog = '-'
errorlog = '-'
loglevel='debug'

def pre_request(worker, req):
    if req.path == '/health' or req.path == '/ready':
        return
    worker.log.debug("%s %s" % (req.method, req.path))