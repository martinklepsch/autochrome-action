#!/usr/bin/env sh

set -e

cd /action/lib
clojure run.clj $1
