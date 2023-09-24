. ~/.configs/registry.config

git pull origin

echo -e '\033[1mCreating backend resources for kubernetes...\033[0m'
for microservice in game-service task-game
do
	cd ~/party-game/backend/$microservice && \
	mvn clean install -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true -Dmaven.resolver.transport=wagon -DskipTests && \
	sed -i 's/thehuginn.com/localhost/g' target/kubernetes/kubernetes.yml && \
	kubectl apply -f target/kubernetes/kubernetes.yml
done

echo -e '\033[1mRolling out update for frontend...\033[0m'
cd ~/party-game/frontend && \
kubectl delete -f frontend.yaml && \
kubectl apply -f frontend.yaml

cd ~/party-game/kubernetes && \
kubectl delete -f nginx.yaml && \
kubectl apply -f nginx.yaml

cd ~/party-game/population/tasks && git pull origin

echo -e '\033[1mWaiting for pods to activate for 10 seconds...\033[0m'
sleep 10
echo -e '\033[1mPopulating tasks...\033[0m'
cd ~/party-game/population && python3.11 populate.py
