#!/bin/sh
curl --data @new-xfr.json --header "Content-Type: application/json" http://localhost:8080/policy-adaptws-0.0.3/transfer
echo
