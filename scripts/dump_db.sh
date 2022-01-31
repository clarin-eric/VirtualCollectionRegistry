#!/bin/bash

container_name=${1}

#Remove any mysqldb specific syntax from the sql dump
#https://stackoverflow.com/a/3813164
cleanup_sql_for_hsqldb() {
  #Remove all mysql SET commands
  sed -i -e 's|/\*.*\*/;||g' ${1}
  #Remove all backticks
  sed -i -e 's|`||g' ${1}
  #Replace any tinyint with boolean (the corresponding hsqldb type)
  sed -i -e 's|TINYINT|boolean|g' ${1}
  sed -i -e 's|tinyint|boolean|g' ${1}
  sed -i -e 's|longtext|LONGVARCHAR|g' ${1}
  sed -i -e 's|text|LONGVARCHAR|g' ${1}

  sed -i -e 's|bigint(.*)|bigint|g' ${1}
  sed -i -e 's|int(.*)|bigint|g' ${1}
  sed -i -e 's|AUTO_INCREMENT|IDENTITY|g' ${1}
  sed -i -e 's|) .*;|);|g' ${1}
  sed -i -e 's|UNIQUE KEY .* (\(.*\))|CONSTRAINT \1 UNIQUE|g' ${1}



#UNIQUE KEY name (name)

  #Convert foreign key definitions
  sed -i -e 's|CONSTRAINT .* FOREIGN KEY|FOREIGN KEY|g' ${1}
#  sed -i -e 's|PRIMARY KEY|PRIMARY|g' ${1}
  sed -i -e 's|^[ \t]*KEY .*||g' ${1}
#  sed -i -e 's|PRIMARY|PRIMARY KEY|g' ${1}

  sed -i -e 's|UNLOCK TABLES.*||g' ${1}
  sed -i -e 's|LOCK TABLES.*||g' ${1}

}

#https://dev.mysql.com/doc/refman/5.7/en/mysqldump-definition-data-dumps.html
docker exec -ti "${container_name}" sh -c 'mysqldump -u ${MYSQL_USER} --password=${MYSQL_PASSWORD} ${MYSQL_DATABASE}  --no-data --routines --events > /tmp/vcr_dump_def.sql'
docker exec -ti "${container_name}" sh -c 'mysqldump -u ${MYSQL_USER} --password=${MYSQL_PASSWORD} ${MYSQL_DATABASE}  --no-create-db --no-create-info > /tmp/vcr_dump_data.sql'
docker cp "${container_name}:/tmp/vcr_dump_def.sql" vcr_dump_def.sql
docker cp "${container_name}:/tmp/vcr_dump_data.sql" vcr_dump_data.sql

cleanup_sql_for_hsqldb "vcr_dump_def.sql"
cleanup_sql_for_hsqldb "vcr_dump_data.sql"