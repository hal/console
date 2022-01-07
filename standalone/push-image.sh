#!/bin/bash

# Pushes the HAL standalone image to quay.io
#
# Prerequisites
#   - Image has been built: ./build-image.sh
# 
# Parameters
#   1. HAL version (optional)

REPO=quay.io/halconsole/hal

# This requires a valid configuration in ~/.docker/config.json
docker login quay.io
if [[ "$#" -ne 1 ]]; then
    docker push $REPO
else
    docker push $REPO:$1
fi
