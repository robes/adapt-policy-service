#!/bin/sh

BASEURL="http://localhost:8080"
HEADER="Content-Type: application/json"

ID=$1
STREAMS=$2

if [ -z ${STREAMS} ]
  then
    JSON="{\"id\":\"${ID}\",\"source\":\"gsiftp://localhost:8080/abc\",\"destination\":\"gsiftp://localhost:8080/xyz\"}"
  else
    JSON="{\"id\":\"${ID}\",\"streams\":${STREAMS},\"source\":\"gsiftp://localhost:8080/abc\",\"destination\":\"gsiftp://localhost:8080/xyz\"}"
fi

curl --request PUT --data "${JSON}" --header "${HEADER}" "${BASEURL}/transfer/${ID}"

echo
