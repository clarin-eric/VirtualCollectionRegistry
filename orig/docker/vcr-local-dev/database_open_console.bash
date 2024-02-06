#!/usr/bin/env bash
cname="vcr-local-dev_database_1"
docker-compose exec "database" \
    sh -c 'mysql --user=${MYSQL_USER} --password=${MYSQL_PASSWORD} ${MYSQL_DATABASE}'
