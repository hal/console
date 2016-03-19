#!/bin/bash

# Script to build, deploy and push HAL.Next to branch 'gh-pages'

ROOT=$PWD
BRANCH=$(git symbolic-ref -q HEAD)
BRANCH=${BRANCH##refs/heads/}
BRANCH=${BRANCH:-HEAD}

if ! git diff --no-ext-diff --quiet --exit-code; then
    echo "Cannot publish to gh-pages. You have uncommitted changes in the current branch."
    exit -1
fi

source "$ROOT/spinner.sh"

start_spinner "Building hal.next..."
mvn -q clean install -Dgwt.skipCompilation
stop_spinner $?

start_spinner "Compiling app..."
cd app
mvn -q clean install
cd ${ROOT}
stop_spinner $?

start_spinner "Publishing to gh-pages..."
rm -rf /tmp/hal
mv app/target/hal-app-*/hal /tmp/
git checkout gh-pages > /dev/null 2>&1
git reset --hard origin/gh-pages > /dev/null 2>&1
rm -rf *.png *.gif *.ico *.txt *.html *.js
rm -rf css deferredjs fonts img js
mv /tmp/hal/ .
date > .build
git add --all > /dev/null 2>&1
git commit -am "Update hal.next" > /dev/null 2>&1
git push -f origin gh-pages > /dev/null 2>&1
git checkout ${BRANCH} > /dev/null 2>&1
stop_spinner $?

echo "\nHAL.Next successfully published to branch gh-pages."
