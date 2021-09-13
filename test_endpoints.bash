#!/usr/bin/env bash

alpha_instance="https://alpha-collections.clarin.eu"
beta_instance="https://beta-collections.clarin.eu"
prod_instance="https://collections.clarin.eu"

#Submission endpoint
name="Endpoint%20test%20collection"
uri1="https%3A%2F%2Fwww.clarin.eu%2Fstatus"
uri1="https%3A%2F%2Fwww.clarin.eu%2Fwrong"



function assertHttpStatusCode {
  url="${1}${2}"
  expectedStatusCode="${3}"
  responseStatusCode=$(curl -s -o /dev/null -I -w "%{http_code}" "${url}")
  printf "\t%-50s: " "HTTP response on ${2}"
  if [ "${responseStatusCode}" != "${expectedStatusCode}" ]; then
    printf "FAILED (HTTP %s != %s)\n" "${responseStatusCode}" "${expectedStatusCode})"
    return 1
  fi
  printf "OK (HTTP %s)\n" "${expectedStatusCode}"
  return 0
}



function test {
  instance="${1}"

  printf "Running test suite on: %s\n" "${instance}"

  #Submission endpoint
  assertHttpStatusCode "${instance}" "/submit/extensional" "302"

  #curl -L -i "${instance}/submit/extensional" \
  #  --data-binary "name=${name}&resrouceUri=${uri1}&resrouceUri=${uri2}" \
  #  -H "Content-Type: application/x-www-form-urlencoded"

  #API endpoint

  assertHttpStatusCode "${instance}" "/swagger.json" "200"

  printf "\t%-50s: " "API endpoint in /swagger.json"
  response=$(curl -s "${instance}/swagger.json" | jq '.servers[0].url')
  if [ "${response}" != "%{instance/service}" ]; then
    printf "FAILED\n"
    return 1
  else
    printf "OK\n"
  fi


  #
  #
  #curl -X 'GET' \
  #  'http://alpha-collections.clarin.euservice/v1/collections?offset=0&count=-1' \
  #  -H 'accept: text/xml'
  #
  #  application/xml
  #  application/json
  #
  #
}

alpha=0
beta=0
production=0

while [[ $# -gt 0 ]]
do
key="$1"
case $key in
    -a|--alpha)
        alpha=1
        ;;
    -b|--beta)
        beta=1
        ;;
    -p|--production)
        production=1
        ;;
    *)
        echo "Unkown option: $key"
        exit 1
        ;;
esac
shift # past argument or value
done

if [ "${alpha}" == "1" ]; then
  test "${alpha_instance}"
fi
if [ "${beta}" == "1" ]; then
  test "${beta_instance}"
fi
if [ "${production}" == "1" ]; then
  test "${prod_instance}"
fi