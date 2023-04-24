# Party Game

Hi, welcome to my small project, which I used as opportunity to learn doing many things from scratch.
Including selfhosting on my own old PC not running Fedora Server 38.

I got this idea from playing a drinking game once (for the first time) and having great time. But it had its flaws such as running out of battery on one's phone and then the game would end for everybody. Furthermore, having tasks which noone likes does not help.

So in short (without many details) this is how it started and then I have added a new mode, decided to learn more stuff and it eventually turned into my small pet project.

With which I am not done yet!

## How to setup own instance

Please note, you will need to rewrite a bit more things especially domain name, paths etc. to ensure it will work in your environment.

## K8s

* I am using k3s instance with build in traefik for handling routing and TLS/SSL termination.

## Registry

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

## Certificates TLS/SSL

* Run following command to retrieve cert-manager `kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.11.0/cert-manager.yaml`
* Clone cloudflare repo <https://github.com/cloudflare/origin-ca-issuer.git>
* Follow the instructions in the repo
* You can use kubernetes/certificates/yaml
* Add new entries to ingress if needed.
