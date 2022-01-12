#!/bin/bash

# Builds the HAL standalone native binary

mvn package -Pnative -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true
