#!/bin/sh

BASEURL="http://localhost:8080"
HEADER="Content-Type: application/json"

ID=$1
JSON="{\"id\":\"${ID}\",\"properties\":{\"adjusted_streams\":\"3\"},\"source\":\"http://localhost:8080/abc\",\"destination\":\"http://localhost:8080/xyz\"}"

curl --request PUT --data ${JSON} --header "${HEADER}" "${BASEURL}/transfer/${ID}"

echo
