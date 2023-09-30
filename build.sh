. ~/.configs/registry.config
. ~/.configs/github.config

git pull origin

echo -e '\033[1mInstalling common modules for microservices...\033[0m'
for common in common-game
do
	cd ~/party-game/backend/$common && mvn clean install
done

echo -e '\033[1mCreating backend resources for kubernetes...\033[0m'
for microservice in game-service task-game
do
	cd ~/party-game/backend/$microservice && \
	mvn clean install -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true -Dmaven.resolver.transport=wagon -DskipTests && \
	sed -i 's/thehuginn.com/localhost/g' target/kubernetes/kubernetes.yml && \
	kubectl apply -f target/kubernetes/kubernetes.yml
done

echo -e '\033[1mRolling out update for frontend...\033[0m'
kubectl rollout restart deployment svelte-frontend

rm -rf ~/party-game/population/tasks && mkdir ~/party-game/population/tasks
cd ~/party-game/population/tasks && git clone https://The-Huginn:${GITHUB_TOKEN}@github.com/The-Huginn/party-game-tasks.git .

echo -e '\033[1mWaiting for pods to activate for 20 seconds...\033[0m'
sleep 20
echo -e '\033[1mPopulating tasks...\033[0m'
cd ~/party-game/population && python3.11 populate.py
