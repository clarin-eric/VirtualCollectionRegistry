#!/usr/bin/env bash

alpha_instance="https://alpha-collections.clarin.eu"
beta_instance="https://beta-collections.clarin.eu"
prod_instance="https://collections.clarin.eu"

#Submission endpoint
name="Endpoint%20test%20collection"
uri1="https%3A%2F%2Fwww.clarin.eu%2Fstatus"
uri1="https%3A%2F%2Fwww.clarin.eu%2Fwrong"

red=$'\e[1;31m'
grn=$'\e[1;32m'
yel=$'\e[1;33m'
blu=$'\e[1;34m'
mag=$'\e[1;35m'
cyn=$'\e[1;36m'
end=$'\e[0m'

function checkHttpStatucCode {
  responseStatusCode="${1}"
  expectedStatusCode="${2}"

  if [ "${responseStatusCode}" != "${expectedStatusCode}" ]; then
    printf "${red}FAILED${end}\n\t\tReason: HTTP %s != %s\n" "${responseStatusCode}" "${expectedStatusCode}"
    return 1
  fi
  printf "${grn}OK${end} (HTTP %s)\n" "${expectedStatusCode}"
  return 0
}

function assertHttpStatusCode {
  url="${1}${2}"
  accept="${3}"
  expectedStatusCode="${4}"

  if [ "${accept}" == "" ]; then
      accept="text/html"
  fi

  responseStatusCode=$(curl -s -o /dev/null -I -w "%{http_code}" -H "accept: ${accept}" "${url}")
  printf "\t%-50s: " "HTTP response on ${2}"
  checkHttpStatucCode "${responseStatusCode}" "${expectedStatusCode}"
}

function assertHttpStatusCodeFollowRedirect {
  url="${1}${2}"
  accept="${3}"
  expectedStatusCode="${4}"

  if [ "${accept}" == "" ]; then
      accept="text/html"
  fi

  responseStatusCode=$(curl -L -s -o /dev/null -I -w "%{http_code}" -H "accept: ${accept}" "${url}")
  printf "\t%-50s: " "HTTP response on ${2}"
  checkHttpStatucCode "${responseStatusCode}" "${expectedStatusCode}"
}

function assertHttpStatusCodeSubmitData {
  url="${1}${2}"
  expectedStatusCode="${3}"
  contentType={4}
  data=${5}

  responseStatusCode=$(curl -s -o /dev/null -w "%{http_code}" -H "Content-Type: ${contentType}" --data-binary "${data}" "${url}")
  printf "\t%-50s: " "HTTP response (data) on ${2}"
  checkHttpStatucCode "${responseStatusCode}" "${expectedStatusCode}"
}

function assertJsonContent {
  url="${1}${2}"
  jqExpression="${3}"

  printf "\t%-50s: " "API endpoint in ${2}:"
  response=$(curl -s -H 'accept: application/json' "${url}" | jq -r "${jqExpression}")
  if [ "${response}" != "${instance}/service/" ]; then
    printf "${red}FAILED${end}\n\t\tReason: %s != %s\n" "${response}" "${instance}/service/"
    return 1
  else
    printf "${grn}OK${end}\n"
  fi
}

function test {
  instance="${1}"

  printf "Running test suite on: %s\n" "${instance}"

  #SAML authentication
  assertHttpStatusCode "${instance}" "/Shibboleth.sso/Login" "" "302"

  #Submission endpoint
  assertHttpStatusCode "${instance}" "/submit/extensional" "" "302"
  assertHttpStatusCodeSubmitData "${instance}" "/submit/extensional" "302" "application/x-www-form-urlencoded" "name=${name}&resrouceUri=${uri1}&resrouceUri=${uri2}"

  #API endpoint
  assertHttpStatusCode "${instance}" "/swagger.json" "" "200"
  assertJsonContent "${instance}" "/swagger.json" ".servers[0].url"

  assertHttpStatusCode "${instance}" "/service/v1/collections" "text/xml" "200"
  assertHttpStatusCode "${instance}" "/service/v1/collections" "application/xml" "200"
  assertHttpStatusCode "${instance}" "/service/v1/collections" "application/json" "200"
  assertHttpStatusCode "${instance}" "/service/v1/collections" "application/unsupported" "406"

  assertHttpStatusCodeFollowRedirect "${instance}" "/service/v1/collections/1000" "application/json" "200"
  assertHttpStatusCodeFollowRedirect "${instance}" "/service/v1/collections/1005" "application/json" "403"
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