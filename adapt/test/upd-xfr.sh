#!/bin/sh

BASEURL="http://localhost:8080"
HEADER="Content-Type: application/json"

ID=$1
STREAMS=$2

JSON="{\"id\":\"${ID}\",\"streams\":${STREAMS},\"source\":\"http://localhost:8080/abc\",\"destination\":\"http://localhost:8080/xyz\"}"

curl --request PUT --data ${JSON} --header "${HEADER}" "${BASEURL}/transfer/${ID}"

echo
