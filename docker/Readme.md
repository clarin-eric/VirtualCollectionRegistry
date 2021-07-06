## Docker

* `docker-compose.yml`: Main config with vcr webapp and mariadb database services
* `postgres.yml`: Postgresql database service
* `migration.yml`: Migration script to migrate data from mariadb to postgresql (both services must be running)
* `graphql.yml`:  Hasura graphql and webhook authentication services.

### Examples

#### Running the VCR

Basic configuration based on mariadb:
```
docker-compose up
```

#### Running the VCR with graphql API

Postgresql and graphql services enabled:
```
docker-compose \
    -f docker-compose.yml \
    -f postgres.yml \
    -f graphql.yml \
    up
```

TODO: how to switch context.xml config? How to generate certificates?

`generate_cert.bash`: create CA, generate certificate for authentication webhook and install hasura and webhook services.

## Graphql

### Server

* Hasura: https://hasura.io
  * Authentication ([link](https://hasura.io/docs/latest/graphql/core/auth/authentication/index.html))
  * Server configuration ([link](https://hasura.io/docs/latest/graphql/core/deployment/graphql-engine-flags/reference.html#server-flag-reference))

### Clients

* Apollo
  * Client ([link](https://www.apollographql.com/apollo-client/))