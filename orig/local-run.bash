#!/bin/bash

(cd docker/vcr-local-dev && docker-compose up | grep -v tomcat8_localhost_access)
