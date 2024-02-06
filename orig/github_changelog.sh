#!/bin/sh

HELP=0
MILESTONE=""
REPO=""

#
# Process script arguments
#
while [[ $# -gt 0 ]]
do
key="$1"
case $key in
    -m)
        MILESTONE="${2}"
	shift
        ;;
    -r)
        REPO="${2}"
	shift
        ;;
    -h|--help)
        HELP=1
        ;;
    *)
        echo "Unkown option: $key"
        HELP=1
        ;;
esac
shift # past argument or value
done


if [ "${REPO}" == "" ]; then
	echo "REPO (-r <value>) is required"
	exit 1
fi

if [ "${MILESTONE}" == "" ]; then
        echo "MILESTONE (-m <value>) is required"
        exit 1
fi

# This uses jq; see https://stedolan.github.io/jq/
# Use 'brew install jq' on Mac

STATE="all"

MILESTONE_URI="https://api.github.com/repos/clarin-eric/${REPO}/milestones/${MILESTONE}"
ISSUES_URI="https://api.github.com/repos/clarin-eric/${REPO}/issues?milestone=${MILESTONE}&state=${STATE}"

curl -s "${MILESTONE_URI}" | jq -r ".title"
curl -s "$ISSUES_URI" | jq -r ".[] | (\"* \" + .title), \"    <\" + .html_url + \">\""

