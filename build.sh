. ~/.configs/registry.config

git pull origin
cd backend/game-service && \
mvn clean install -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true -Dmaven.resolver.transport=wagon && \
sed -i 's/thehuginn.com/localhost/g' target/kubernetes/kubernetes.yml && \
kubectl apply -f target/kubernetes/kubernetes.yml

#podman login -u ${REGISTRY_USERNAME} -p ${REGISTRY_PASSWORD} registry.localhost
#podman push registry.localhost/game:latest

#kubectl scale --replicas=0 deploy/nginx
#kubectl scale --replicas=0 deploy/backend

#kubectl scale --replicas=1 deploy/backend
#kubectl scale --replicas=1 deploy/nginx
