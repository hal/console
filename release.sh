#!/bin/bash

# Script to build, deploy and publish HAL to branch 'gh-pages'.
# Should be executed after a new version was tagged.
#
# Prerequisites
#   - Clean git status (no uncommitted changes)
#   - Docker is up and running
#
# What it does
#   1. Build and deploy w/ profiles docker, prod, release, theme-hal and yarn
#   2. Publish the compiled GWT app to branch 'gh-pages'

ROOT=$PWD
BRANCH=$(git symbolic-ref -q HEAD)
BRANCH=${BRANCH##refs/heads/}
BRANCH=${BRANCH:-HEAD}

# Prerequisites
if ! git diff --no-ext-diff --quiet --exit-code; then
    echo "Cannot publish to gh-pages. You have uncommitted changes in the current branch."
    exit -1
fi
if ! docker info > /dev/null 2>&1; then
    echo "Docker not running. Please start docker before running this script."
    exit -1
fi

# Deploying
mvn clean deploy -P docker,prod,release,theme-hal,yarn

# Publishing to gh-pages
rm -rf /tmp/console
cd /tmp/
git clone -b gh-pages --single-branch git@github.com:hal/console.git
cd console
rm -rf *.gif *.html *.ico *.js *.png *.txt css deferredjs fonts img js previews
cp -R ${ROOT}/app/target/hal-console-*/hal/ .
date > .build
git add --all
git commit -am "Update console"
git push -f origin gh-pages
cd ${ROOT}

echo
echo
echo "+-----------------------------+"
echo "|                             |"
echo "|  HAL successfully released  |"
echo "|                             |"
echo "+-----------------------------+"
echo
