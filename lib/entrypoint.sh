#!/usr/bin/env sh

set -e

pushd /github/workspace
git diff 71f74d92c60738db7257230ec7cfb8de681ac6c3 e7abf5cfd9fbb849c973790dc21c333717df4b2f
popd

cd /action/lib

clojure --report stderr run.clj /github/workspace
