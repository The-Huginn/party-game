# Party Game

Hi, welcome to my small project , which I used as opportunity to learn doing many things from scratch 🚀.
Including selfhosting on my own old PC now running Fedora Server 38, microservices architecture, separate frontend framework svelte and the whole DevOps and updates thing 🤯.

I got this idea from playing a drinking game once 🍺 (for the first time) and having great time 🥳. But it had its flaws such as running out of battery on one's phone and then the game would end for everybody. Furthermore, having tasks which noone likes does not help.

So in short this project has eventually turned into my small pet project 🐶.

With which I am not done yet! 🥳🚀

## Running development mode

After cloning this project you would need to run all microservices as backend and then the frontend. Please ensure you are all the [dependencies](#dependencies) install.

### Running quarkus microservices

All backend sources can be found under `backend` folder. You would first need to build all the dependencies 🚀
> `cd backend && mvn clean install`

Then to run each backend microservice you would need to create separate terminal sessions and run in ech folder (`game-service`, `task-game`, `assignment-game`) the following
> `mvn clean quarkus:dev`

Which will run each microservice on different port and having common container database.
> Please note: running all microservices with `testcontainers.reuse` would result in working only single backend [mode](#modes), as the database schema is always overriden

However you would need to create your own tasks for all [modes](#modes), which can be found under section [Populating tasks](#populating-tasks)

### Modes

* `task-game`
* `assignment-game`

### Populating tasks

#### `task-game`

Due to more configurable parameters you will need to create your own json files under `population/tasks/`, where you can currently find a single `template.json` template for a task with all options.
> Note for defining a basic task you actually need to define only `payload.task.content` but other options are advised to be filled in as well.

Then the following json files should have the following structure:

```json
{
    "category": {
        "payload": {
            "name": "My Own Category",
            "description": "Very descriptive description of My Own Category"
        },
        "translations": [
            {
                "locale": "sk",
                "name": "Moja Vlastná Kategória",
                "description": "Veľmi užitočný popis Mojej Vlastnej Kategórie"
            }
        ]
    },
    "tasks": [
        <<template.json>>,
        <<template.json>>
    ]
}
```

where `<<template.json>>` is of type defined in `template.json` file.

#### `assignment-game`

Create your own `import.sql` file with the following structure of tasks:

```sql
INSERT INTO AbstractTask(id, taskType) VALUES(0, 1);
INSERT INTO TaskText(id, locale, content) VALUES(0, 'en', 'These are necessary rules 🥰');
INSERT INTO LocaleTaskText(taskText_id, taskText_locale, locale, content) VALUES(0, 'en', 'sk', 'Toto sú povinné pravidlá 🥰');

INSERT INTO AbstractTask(id, taskType) VALUES(1, 1);
INSERT INTO TaskText(id, locale, content) VALUES(1, 'en', 'My first task 😍');
INSERT INTO LocaleTaskText(taskText_id, taskText_locale, locale, content) VALUES(1, 'en', 'sk', 'Môj prvý task 😍');
```

where the first task is expected to be task explaining rules, thus not a real task. Note you will have to increase the first value as it is the `id` for the task, such as for `AbstractTask` it would be `VALUES(id, 1)` and then for the corresponding `TaskText` and `LocaleTaskText` it would be `VALUES(id)`.

### Running frontend service

You would need to move to `frontend` folder. Here run the following:
> `npm run dev -- --host`

Omit `-- -host` part if you do not wish to expose the game on your IP address. Note you will not be able to connect to your website from another device.

## Dependencies

For running [Quarkus](https://quarkus.io/guides/maven-tooling) app you would need:

* JDK 17, I strongly recommend using [SDKMAN](https://sdkman.io/)
* Maven version at least 3.9.3, also available in [SDKMAN](https://sdkman.io/)
* docker for running [Quarkus devservices](https://quarkus.io/guides/dev-services) or podman, but you would need little bit more [configuration](https://quarkus.io/guides/podman)

For runinng the frontend hosted on node server

* Downloading node.js [runtime](https://svelte.dev/blog/svelte-for-new-developers#installing-node-js)
* We are using [svelte](https://svelte.dev/) as frontend framework

## Hosting your own instance

Please note, you will need to rewrite a bit more things especially domain name, paths etc. to ensure it will work in your environment.

### K8s

* I am using k3s instance with build in traefik for handling routing and TLS/SSL termination.

### Registry

* Add DNS entry for (usually `/etc/hosts`) `registry.localhost` to point to your public IP
* Configure insecure container registry for your container runtime to accept `registry.localhost`.\
    For my setup I add the following into `/etc/containers/registries.conf`\
    `[[registry]]`\
    `location = "registry.localhost"`\
    `insecure = true`
* Configure mirror for kubernetes for localhost registry\
    For my setup I add the following into `/etc/rancher/k3s/registries.yaml`\
    `mirrors:`\
    &emsp;`registry.localhost:`\
    &emsp;&emsp;`endpoint:`\
    &emsp;&emsp;&emsp;`- "http://registry.localhost"`\

    &emsp;`configs:`\
    &emsp;&emsp;`"registry.localhost":`\
    &emsp;&emsp;&emsp;`auth:`\
    &emsp;&emsp;&emsp;&emsp;`username: XXXXXX`\
    &emsp;&emsp;&emsp;&emsp;`password: XXXXXX`
* Please note you should create user in `registry` PersistentVolume and use the corresponding credentials for mirror
* To apply changes run following `sudo systemctl restart k3s.service`

### Cloudflare tunnel

Deploy the `cloudflare.yaml` resource into your cluster. Please update value in start up arguments to fill in your token `--token={token}` for the tunnel.
You can use a single tunnel and in the web UI you can manage the routing between hostnames and your kubernetes services.

### Certificates TLS/SSL

If you are not using [Cloudflare tunnels](#cloudflare-tunnel), then you will need to manage your certificates for HTTPS communication between your server and Cloudflare proxy.

* Run following command to retrieve cert-manager `kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.11.0/cert-manager.yaml`
* Clone cloudflare repo <https://github.com/cloudflare/origin-ca-issuer.git>
* Follow the instructions in the repo
* You can use kubernetes/certificates/yaml
* Add new entries to ingress if needed.

### Creating token for quarkus

If you would like to deploy directly to your cluster by running `mvn clean install` you will need to setup token for authentication. That can be achieved by the following commands.

1. `kubectl create sa quarkus`
2. `kubectl create clusterrolebinding quarkus-binding --clusterrole quarkus-cluster --serviceaccount default:quarkus`

Then for getting a token run the following `kubectl create token quarkus`
