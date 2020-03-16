#!/usr/bin/env sh

set -e

cd /action/lib
clojure --report stderr run.clj /github/workspace
