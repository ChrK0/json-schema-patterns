#!/bin/bash

docker build -t patterns_img .
docker run patterns_img
container=$(docker ps -a -q --filter ancestor=patterns_img)
docker cp "$container":/home/repro/json-schema-patterns/results_"$(<ECMAScript2Brics/commithash)" ../results_"$(<ECMAScript2Brics/commithash)"