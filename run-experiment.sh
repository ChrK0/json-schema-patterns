#!/bin/bash

source=$1
commit_hash=$(<ECMAScript2Brics/commithash)
rm -rf "results_$commit_hash"
java -jar pattern-statistics.jar "$source" "results_$commit_hash"