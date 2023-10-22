. .env
podman login -u ${REGISTRY_USERNAME} -p ${REGISTRY_PASSWORD} registry.thehuginn.com

cd frontend && \
podman build -t registry.thehuginn.com/party-game/svelte-frontend . && \
podman push registry.thehuginn.com/party-game/svelte-frontend:latest && \
cd ../

# for common in common-game common-exposed-service
# do
# 	cd backend/$common && mvn clean install && \
#     cd ../../
# done

# for microservice in game-service task-game
# do
# 	cd backend/$microservice && \
#     mvn clean install -Dquarkus.container-image.username=${REGISTRY_USERNAME} -Dquarkus.container-image.password=${REGISTRY_PASSWORD} -Dquarkus.container-image.push=true && \
#     cd ../../
# done

cd backend && \
mvn clean install -Dquarkus.container-image.username=${REGISTRY_USERNAME} -Dquarkus.container-image.password=${REGISTRY_PASSWORD} -Dquarkus.container-image.push=true -Dquarkus.container-image.insecure=true -Djib.httpTimeout=60000 && \
cd ../