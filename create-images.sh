. .env
podman login -u ${REGISTRY_USERNAME} -p ${REGISTRY_PASSWORD} registry.thehuginn.com

cd frontend && \
podman build -t registry.thehuginn.com/party-game/svelte-frontend . && \
podman push registry.thehuginn.com/party-game/svelte-frontend:latest

cd ../backend/ && \
mvn clean install -Dquarkus.container-image.username=${REGISTRY_USERNAME} -Dquarkus.container-image.password=${REGISTRY_PASSWORD} -Dquarkus.container-image.push=true
