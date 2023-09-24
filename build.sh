. ~/.configs/registry.config

git pull origin

echo "Creating backend resources for kubernetes..."
for microservice in game-service task-game
do
	cd ~/party-game/backend/$microservice && \
	mvn clean install -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true -Dmaven.resolver.transport=wagon -DskipTests && \
	sed -i 's/thehuginn.com/localhost/g' target/kubernetes/kubernetes.yml && \
	kubectl apply -f target/kubernetes/kubernetes.yml
done

echo "Rolling out update for frontend..."
kubectl rollout restart deployment svelte-frontend

cd ~/party-game/population/tasks && git pull origin

sleep 3
echo "Populating tasks..."
cd ~/party-game/population && python3.11 populate.py
