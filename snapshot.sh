#!/bin/bash
#
#  Copyright 2022 Red Hat
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#


# Script to build and deploy a snapshot version.
#
# Prerequisites
#   - Clean git status (no uncommitted changes)
#   - Docker is up and running
#
# What it does
#   1. Build and deploy w/ profiles docker, prod, and theme-hal

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
mvn clean deploy -P docker,prod,theme-hal

echo
echo
echo "+-----------------------------+"
echo "|                             |"
echo "|  HAL successfully deployed  |"
echo "|                             |"
echo "+-----------------------------+"
echo
