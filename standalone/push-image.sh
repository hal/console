#!/bin/bash

# Pushes the HAL standalone image to quay.io
#
# Prerequisites
#   - Image has been built: ./build-image.sh
# 
# Parameters
#   1. HAL version (optional)

REPO=quay.io/halconsole/hal
VERSION=${1-latest}

# This requires a valid configuration in ~/.docker/config.json
docker login quay.io
docker push $REPO:$VERSION
