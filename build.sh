. ~/.configs/registry.config
. ~/.configs/github.config

git reset --hard origin/master
git pull origin

echo -e '\033[1mCreating and installing backend resources for kubernetes...\033[0m'

cd ~/party-game/backend && \
mvn -U clean install -DskipTests -Dquarkus.kubernetes.deploy=true -Dquarkus.kubernetes-client.api-server-url=thehuginn.com:6443 && \

echo -e '\033[1mRolling out update for frontend...\033[0m'
kubectl rollout restart deployment svelte-frontend

rm -rf ~/party-game/population/tasks && mkdir ~/party-game/population/tasks
cd ~/party-game/population/tasks && git clone https://The-Huginn:${GITHUB_TOKEN}@github.com/The-Huginn/party-game-tasks.git .

echo -e '\033[1mWaiting for pods to activate for 20 seconds...\033[0m'
sleep 20
echo -e '\033[1mPopulating tasks...\033[0m'
cd ~/party-game/population && python3.11 populate.py
