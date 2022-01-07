#!/bin/bash

# Builds the HAL standalone image
#
# Prerequisites
#   - Native binary has been built: ./build-native.sh
#
# Parameters
#   1. HAL version (optional)


REPO=quay.io/halconsole/hal

if [[ "$#" -ne 1 ]]; then
    docker build \
      --file src/main/docker/Dockerfile.native \
      --tag $REPO \
      .
else
    docker build \
      --file src/main/docker/Dockerfile.native \
      --tag $REPO:$1 \
      .
fi
