#!/bin/sh
set -ex
echo "PWD: $(pwd)"
(cd docker/ && cp -r ../target/vcr vcr)
