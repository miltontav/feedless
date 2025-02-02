# Server core

## Podman Notes
podman pull docker.io/postgis/postgis:15-3.4-alpine


## OpenNLP models
https://opennlp.apache.org/models.html

## Database
In root directory
```
cp .env.sample .env
podman-compose up -d postgis
```

## Run
gradle bootRun --args='--spring.profiles.active=saas,dev'
