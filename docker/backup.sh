#!/bin/bash

docker exec -ti vcr-local-dev_database_1 sh -c 'mysqldump --user=${MYSQL_USER} --password=${MYSQL_PASSWORD} ${MYSQL_DATABASE}' > vcr_1.6.0.sql
