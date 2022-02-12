#!/bin/bash
#
#  Copyright 2022 Red Hat
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#


ROOT="$PWD"
SCRIPT=`basename $0`
ARGS=$#
COMMAND=$1

function usage {
    echo -e "Usage: $SCRIPT <help|less|html|i18n|mbui>\n"
    echo -e "Refresh resources for GWT devmode.\n"
    echo " help     Shows this help"
    echo " less     Compile LESS stylesheets"
    echo " html     Update HTML snippets"
    echo " i18n     Process i18n resource bundles"
    echo " mbui     Regenerate MBUI resources"
    exit 1
}

function less {
    node_modules/.bin/grunt css
}

function html {
    mvn generate-resources -Dcheckstyle.skip=true
}

function i18n {
    cd ../resources
    mvn package
    cd -
    mvn generate-resources -Dcheckstyle.skip=true
}

function mbui {
    mvn compile -Dcheckstyle.skip=true
}

if [[ ${ARGS} -eq 0 ]]
then
  usage
fi

case ${COMMAND} in
    "help")
        usage
        ;;
    "less")
        less
        ;;
    "html")
        html
        ;;
    "i18n")
        i18n
        ;;
    "mbui")
        mbui
        ;;
    *)
        usage
        ;;
esac
