#!/bin/bash

if [ -z "$1" ]
  then
    commit="main"
  else
    commit=$1
fi

rm -rf ECMAScript2Brics
git clone --no-checkout https://github.com/sdbs-uni-p/ECMAScript2Brics.git
cd ECMAScript2Brics || return
git config core.sparseCheckout true
{ echo "src/main/*"; echo "LICENSE"; echo "pom.xml"; echo ".gitignore"; }>> .git/info/sparse-checkout
git checkout "$commit"
hash=$(git show -s --format=%H)
echo -n "${hash:0:7}" > commithash
git apply ../PatternStatistics/check-anchors-inside.patch
rm -rf .git