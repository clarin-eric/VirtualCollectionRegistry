#!/usr/bin/env bash

run_sql() {
  sql="${1}"
  cname="vcr-local-dev_database_1"

  printf "%s\n" "${sql}"
  docker exec -i "${cname}" \
    sh -c 'mysql --user=${MYSQL_USER} --password=${MYSQL_PASSWORD} ${MYSQL_DATABASE}' <<< "${sql}"
}

#Clear all tables
printf "Clearing all tables\n"
run_sql "truncate table resource;"
run_sql "truncate table keyword;"
run_sql "truncate table creator;"
run_sql "truncate table pid;"
run_sql "truncate table api_key;"
run_sql "delete from virtualcollection;"
run_sql "delete from vcr_user;"
