#!/bin/bash

# Script to build and deploy a snapshot version.
#
# Prerequisites
#   - Clean git status (no uncommitted changes)
#   - Docker is up and running
#
# What it does
#   - Build and deploy w/ profiles prod, theme-hal and docker

ROOT=$PWD
BRANCH=$(git symbolic-ref -q HEAD)
BRANCH=${BRANCH##refs/heads/}
BRANCH=${BRANCH:-HEAD}

# Prerequisites
if ! git diff --no-ext-diff --quiet --exit-code; then
    echo "You have uncommitted changes in the current branch."
    exit -1
fi
if ! docker info > /dev/null 2>&1; then
    echo "Docker not running. Please start docker before running this script."
    exit -1
fi

# Deploying console
mvn clean deploy -pl \!yarn -P prod,theme-hal,docker

echo
echo
echo "+-----------------------------+"
echo "|                             |"
echo "|  HAL successfully deployed  |"
echo "|                             |"
echo "+-----------------------------+"
echo
