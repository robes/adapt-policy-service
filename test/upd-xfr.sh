#!/bin/sh

ID=$1
JSON="{\"id\":\"${ID}\",\"properties\":{\"adjusted_streams\":\"3\"},\"source\":\"http://localhost:8080/abc\",\"destination\":\"http://localhost/xyz\"}"

curl --request PUT --data ${JSON} --header "Content-Type: application/json" http://localhost:8080/policy-adaptws-0.0.3/transfer/${ID}

echo
