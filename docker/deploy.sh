#!/bin/bash

set -e

SCRIPT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )
echo "SCRIPT_DIR=${SCRIPT_DIR}"

DIR="${SCRIPT_DIR}/webapps/ROOT"

undeploy() {
    echo "Undeploying ${DIR}"
    rm -rf "${DIR}"
    sleep 15
}

deploy() {
    echo "Deploying ${DIR}"
    mkdir -p "${DIR}"
    (
        cd "${DIR}" 
        ARCHIVE=$(find "../../../target" -name "*.war")
        unzip -q ${ARCHIVE}
    )
}

main() {
    undeploy
    deploy
}

main

