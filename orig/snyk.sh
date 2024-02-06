#!/bin/bash
TOKEN="8569fa87-261f-4dba-b6af-180e8ef415eb"

#docker run -it \
#    -e "SNYK_TOKEN=${TOKEN}" \
#    -e "MONITOR=true" \
#    -v "$(pwd):/project:delegated" \
#    -v "/Users/wilelb/.m2:/home/node/.m2:delegated" \
#  snyk/snyk-cli:maven-3.5.4 test --org=clarin-eric

time docker run -it \
    -e "SNYK_TOKEN=${TOKEN}" \
    -e "MONITOR=true" \
    -v "$(pwd):/project:delegated" \
    -v "/Users/wilelb/.m2:/home/node/.m2:delegated" \
  snyk/snyk-cli:maven-3.5.4 test
