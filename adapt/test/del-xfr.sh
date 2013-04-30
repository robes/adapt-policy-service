#!/bin/sh

BASEURL="http://localhost:8080"
HEADER="Content-Type: application/json"

curl -XDELETE "${BASEURL}/transfer/$1"

echo
