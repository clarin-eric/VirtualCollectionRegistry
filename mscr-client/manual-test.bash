#!/usr/bin/env bash

set -ex

#Load api propertier from file
API_URL=$(grep "api.url" ~/.config/mscr/config.properties | sed 's/api.url=//g')
TOKEN=$(grep "api.key" ~/.config/mscr/config.properties | sed 's/api.key=//g')


SOURCE_SCHEMA="mscr:schema:76d163d7-8e62-49d0-b273-0874f8eb068a"
TARGET_SCHEMA="mscr:schema:84ae53c0-ca68-42af-843a-54330cef85e4"
XSLT_FILE="/Users/wilelb/Code/work/clarin/git/vcr-fc4e/crosswalks/tei_minimal_to_dc.xslt"

FORM_DATA=$(printf '{"format":"XSLT","description":{},"label":{"en":"tei minimal to dc"},"languages":["en"],"status":"VALID","state":"PUBLISHED","sourceSchema":"%s","targetSchema":"%s","versionLabel":"1","visibility":"PUBLIC","organizations":[]}' "${SOURCE_SCHEMA}" "${TARGET_SCHEMA}")

echo "Submission data: ${FORM_DATA}"

curl -X "PUT" \
  --header 'Authorization: Bearer '"${TOKEN}" \
  --form "metadata=${FORM_DATA}" \
  --form "file=@/Users/wilelb/Code/work/clarin/git/vcr-fc4e/crosswalks/tei_minimal_to_dc.xslt" \
  ${API_URL}crosswalkFull
