#!/bin/sh

BASEURL="http://localhost:8080"
HEADER="Content-Type: application/json"

curl --request DELETE "${BASEURL}/transfer/$1"

echo
