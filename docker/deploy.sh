#!/bin/bash

WAR_FILE=$(find target -name "*.war" | head -n 1)

if [ "${WAR_FILE}" == "" ]; then
    echo "No war file found"
    exit 1
fi

echo "Deploying: ${WAR_FILE}"
curl --upload-file "${WAR_FILE}" --user "script:script" "http://localhost:8081/manager/text/deploy?path=/vcr&update=true"
