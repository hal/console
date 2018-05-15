#!/bin/bash

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
