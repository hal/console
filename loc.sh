#!/bin/bash

# Script to count the LoC in the codebase
# Uses 'cloc' (https://github.com/AlDanial/cloc)

# Please note:
# To get the most accurate results, generated files are removed before 'cloc' is executed.

ROOT=$PWD
source "$ROOT/spinner.sh"

start_spinner "Cleanup..."
mvn -q clean > /dev/null 2>&1
cd app
grunt clean > /dev/null 2>&1
cd ${ROOT}
stop_spinner $?

echo
cloc . --exclude-dir=.idea,bower_components,node,node_modules,target --exclude-ext=iml
