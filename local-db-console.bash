#!/bin/bash

docker exec -ti vcr-local-dev_database_1 sh -c 'mysql --user=${MYSQL_USER} --password=${MYSQL_PASSWORD} ${MYSQL_DATABASE}'
