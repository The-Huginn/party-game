podman run -it --name mongo --env MONGO_INITDB_ROOT_USERNAME=admin --env MONGO_INITDB_ROOT_PASSWORD=admin -p 127.0.0.1:27017:27017 mongo:latest
