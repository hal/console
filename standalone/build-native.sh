#!/bin/bash

# Builds the HAL standalone native binary

mvn package \
  -Pnative \
  -Dquarkus.container-image.build=true \
  -Dquarkus.native.remote-container-build=true \
  -Dquarkus.native.container-runtime=podman
