. .env
podman login -u ${REGISTRY_USERNAME} -p ${REGISTRY_PASSWORD}

cd frontend && \
podman build -t registry.thehuginn.com/party-game/svelte-frontend . && \
podman push registry.thehuginn.com/party-game/svelte-frontend:latest

cd ../backend/game-service && \
mvn clean install -Dquarkus.container-image.username=${REGISTRY_USERNAME} -Dquarkus.container-image.password=${REGISTRY_PASSWORD} -Dquarkus.container-image.push=true

cd ../task-game && \
mvn clean install -Dquarkus.container-image.username=${REGISTRY_USERNAME} -Dquarkus.container-image.password=${REGISTRY_PASSWORD} -Dquarkus.container-image.push=true
