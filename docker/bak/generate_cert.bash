#!/bin/bash

CADIR="ca"
CASUBJ="/C=NL/ST=Gelderland/L=Nijmegen/O=CLARIN ERIC/OU=IT Department/CN=local_ca"
DIR="cert"
SUBJ="/C=NL/ST=Gelderland/L=Nijmegen/O=CLARIN ERIC/OU=IT Department/CN=auth"

generate_ca() {
  if [ ! -d "${CADIR}" ]; then
    echo "No CA found, generating new CA"
    mkdir -p "${CADIR}"
    openssl genrsa -des3 -passout 'pass:caxxxxx' -out "${CADIR}/myCA.key" 2048
    openssl req -x509 -new -nodes -passin 'pass:caxxxxx' -key "${CADIR}/myCA.key" -sha256 -days 1825 -subj "${CASUBJ}" -out "${CADIR}/myCA.pem"
    # Generate extension file to ensure a x509 v3 certificate is generated
    echo "authorityKeyIdentifier=keyid,issuer" > "${CADIR}/v3.ext"
    echo "basicConstraints=CA:FALSE" >> "${CADIR}/v3.ext"
    echo "keyUsage = digitalSignature, nonRepudiation, keyEncipherment, dataEncipherment" >> "${CADIR}/v3.ext"
  fi
}

generate() {
    mkdir -p "${DIR}"
    if [ ! -f "${DIR}/server.crt" ] || [ ! -f "${DIR}/server.key" ]; then
        echo "Generating new certificate in ${DIR}."
        openssl genrsa -des3 -passout 'pass:xxxxx' -out "${DIR}/server.pass.key" 2048
        openssl rsa -passin 'pass:xxxxx' -in "${DIR}/server.pass.key" -out "${DIR}/server.key"
        rm "${DIR}/server.pass.key"
        openssl req -new -key "${DIR}/server.key" -out "${DIR}/server.csr" -subj "${SUBJ}"
        #openssl x509 -req -days 365 -in "${DIR}/server.csr" -signkey "${DIR}/server.key" -out "${DIR}/server.crt"
        openssl x509 -req -days 825 -in "${DIR}/server.csr" \
          -CA "${CADIR}/myCA.pem" -CAkey "${CADIR}/myCA.key" -CAcreateserial -passin 'pass:caxxxxx' \
          -extfile "${CADIR}/v3.ext" \
          -out "${DIR}/server.crt"


        rm "${DIR}/server.csr"

        echo ""
        echo "Inspect certificate via:"
        echo "  openssl x509 -in ${DIR}/server.crt -text -noout"
        echo ""
    fi
}

generate_ca
generate

