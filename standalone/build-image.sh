#!/bin/bash

# Builds the HAL standalone image
#
# Prerequisites
#   - Native binary has been built: ./build-native.sh
#
# Parameters
#   1. HAL version


VERSION=$1
REPO=quay.io/halconsole/hal
BINARY=target/hal-standalone-$VERSION-runner


# Prerequisites
if [[ "$#" -ne 1 ]]; then
    echo "Illegal number of parameters. Please use $0 <version>"
    exit 1
fi
if [[ ! -f "$FILE" ]]; then
    echo "No native binary found at $BINARY"
    exit 1
fi


docker build \
  --file src/main/docker/Dockerfile.native \
  --tag $REPO:$VERSION \
  .
