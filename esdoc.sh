#!/bin/bash

# Script to build and deploy the JavaScript API to branch 'esdoc'.
# Should be executed after a new version was tagged.
#
# Prerequisites
#   - Clean git status (no uncommitted changes)
#
# What it does
#   1. Build the JavaScript API
#   2. Publish the doc to branch 'gh-pages' (only if parameter 'deploy' was specified)

ROOT=$PWD
CHANGES=$(git diff-index --name-only HEAD --)

# Prerequisites
if [ "$1" == "deploy" ] && [ -n "$CHANGES" ]; then
    echo "Cannot publish to esdoc. You have uncommitted changes in the current branch."
    exit -1
fi

cd app
mvn process-sources
grunt esdoc

if [ "$1" == "deploy" ]; then
    rm -rf /tmp/console
    cd /tmp/
    git clone -b esdoc --single-branch git@github.com:hal/console.git
    cd console
    rm -rf *.json *.html ast class css file image manual script user
    cp -R ${ROOT}/app/target/esdoc/ .
    date > .build
    git add --all
    git commit -am "Update JavaScrip API"
    git push -f origin esdoc
    cd ${ROOT}
fi
