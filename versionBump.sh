#!/bin/sh

# Use this script to bump the version accross all POMs.

PROGNAME=`basename "$0"`

if [ "$#" -ne 1 ]; then
    echo "Illegal number of arguments. Use '$PROGNAME <version>'"
else
    mvn versions:set -DnewVersion=$1
    sed -i.versionsBackup "s/\"version\": \".*\",$/\"version\": \"$1\",/" app/bower.json
    sed -i.versionsBackup "s/\"version\": \".*\",$/\"version\": \"$1\",/" app/package.json
    sed -i.versionsBackup "s/\"version\": \".*\",$/\"version\": \"$1\",/" npm/package.json
    sed -i.versionsBackup "s/name=\"hal\.version\" value=\".*\"/\name=\"hal.version\" value=\"$1\"/" app/src/main/module.gwt.xml
    find . -name *.versionsBackup -exec rm {} \;
fi
