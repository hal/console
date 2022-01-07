#!/bin/bash

# Pushes the HAL standalone image to quay.io
#
# Parameters
#   1. HAL version


VERSION=$1
REPO=quay.io/halconsole/hal


# Prerequisites
if [[ "$#" -ne 1 ]]; then
    echo "Illegal number of parameters. Please use $0 <version>"
    exit 1
fi


# This requires a valid configuration in ~/.docker/config.json
docker login quay.io
docker push $REPO:$VERSION
