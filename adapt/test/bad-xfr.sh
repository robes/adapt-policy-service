#!/bin/sh

BASEURL="http://localhost:8080"
HEADER="Content-Type: application/json"

curl --data @bad-xfr.json --header "${HEADER}" "${BASEURL}/transfer"

echo
