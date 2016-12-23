#!/bin/bash

# Script to build, deploy and push HAL.next to branch 'gh-pages'

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
mvn -q clean install -Dgwt.skipCompilation -Pdev > /dev/null 2>&1
stop_spinner $?

start_spinner "Compiling app..."
cd app
mvn -q clean install -P prod,theme-hal > /dev/null 2>&1
cd ${ROOT}
stop_spinner $?

start_spinner "Publishing to gh-pages..."
rm -rf /tmp/hal.next
cd /tmp/
git clone -b gh-pages --single-branch git@github.com:hal/hal.next.git > /dev/null 2>&1
cd hal.next
rm -rf *.gif *.html *.ico *.js *.png *.txt css deferredjs fonts img js previews
cp -R ${ROOT}/app/target/hal-app-*/hal/ .
date > .build
git add --all > /dev/null 2>&1
git commit -am "Update hal.next" > /dev/null 2>&1
git push -f origin gh-pages > /dev/null 2>&1
cd ${ROOT}
stop_spinner $?

echo
echo "HAL.next successfully published to branch gh-pages."
echo "Please visit https://hal.github.io/hal.next/"
echo
