#!/bin/sh

# Use this script to bump the version accross all POMs.

PROGNAME=`basename "$0"`

if [ "$#" -ne 2 ]; then
    echo "Illegal number of arguments. Use '$PROGNAME <maven version> <semantic version>'"
else
    mvn versions:set -Pdocker -DnewVersion=$1
    sed -i.versionsBackup "s/.*/$1/" version.txt
    sed -i.versionsBackup "s/version: '.*',$/version: '$2',/" app/Gruntfile.js
    sed -i.versionsBackup "s/\"version\": \".*\",$/\"version\": \"$2\",/" app/package.json
    sed -i.versionsBackup "s/name=\"hal\.version\" value=\".*\"/name=\"hal.version\" value=\"$1\"/" app/src/main/module.gwt.xml
    sed -i.versionsBackup "s/\"version\": \".*\",$/\"version\": \"$2\",/" yarn/src/main/resources/hal-console/package.json
    sed -i.versionsBackup "s/\"version\": \".*\",$/\"version\": \"$2\",/" yarn/src/main/resources/hal-edk/package.json
    find . -name "*.versionsBackup" -exec rm {} \;
fi
