. ~/.configs/registry.config

git pull origin
cd src/app && \
podman build -t registry.localhost/game:latest .

podman login -u ${REGISTRY_USERNAME} -p ${REGISTRY_PASSWORD} registry.localhost
podman push registry.localhost/game:latest

kubectl scale --replicas=0 deploy/nginx
kubectl scale --replicas=0 deploy/backend

kubectl scale --replicas=1 deploy/backend
kubectl scale --replicas=1 deploy/nginx
