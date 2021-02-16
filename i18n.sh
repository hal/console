#!/bin/bash

ROOT="$PWD"
SCRIPT=`basename $0`
ARGS=$#
COMMAND=$1
SUB_COMMAND=$2

function usage {
    echo -e "Usage: $SCRIPT <help|clean|info|zip>\n"
    echo -e "Zanata wrapper script to push and pull the translatable resources to and from Zanata.\n"
    echo " help                        Shows this help"
    echo " clean                       Removes all temporary files"
    echo " info                        Displays details about the translatable resources such as"
    echo "                             number of constants, messages and preview files."
    echo " zip <all|bundles|previews>  Creates a zip file in target/i18n containing the specified"
    echo "                             content."
    exit 1
}

function clean {
    rm -rf target/i18n
}

function info {
    find resources/src/main/resources/org/jboss/hal/resources -name "Constants*.properties" | xargs wc -l
    find resources/src/main/resources/org/jboss/hal/resources -name "Messages*.properties" | xargs wc -l
    find resources/src/main/resources/org/jboss/hal/resources/previews -name "*.html" | xargs wc -l
}

function verifySubCommand {
    if [[ ${ARGS} -ne 2 ]]
    then
        usage
    fi
    case ${SUB_COMMAND} in
        "all")
            ;;
        "bundles")
            ;;
        "previews")
            ;;
        *)
            usage
            ;;
    esac
}

function zipCommand {
    if [[ "$SUB_COMMAND" == "all" ]]
    then
        copyBundles
        copyPreviews
        zipAll
    elif [[ "$SUB_COMMAND" == "bundles" ]]
    then
        copyBundles
        zipBundles
    elif [[ "$SUB_COMMAND" == "previews" ]]
    then
        copyPreviews
        zipPreviews
    else
        usage
    fi
}

function copyBundles() {
    mkdir -p target/i18n/bundles
    cp resources/src/main/resources/org/jboss/hal/resources/Constants*.properties target/i18n/bundles
    cp resources/src/main/resources/org/jboss/hal/resources/Messages*.properties target/i18n/bundles
}

function copyPreviews() {
    mkdir -p target/i18n/previews
    cp -R resources/src/main/resources/org/jboss/hal/resources/previews/ target/i18n/previews
}

function zipAll() {
    cd target/i18n
    zip -r hal-all-i18n.zip bundles previews
}

function zipBundles() {
    cd target/i18n
    zip -r hal-bundles-i18n.zip bundles
}

function zipPreviews() {
    cd target/i18n
    zip -r hal-previews-i18n.zip previews
}

# Check
if [[ ${ARGS} -eq 0 ]]
then
  usage
fi

# and parse arguments
case ${COMMAND} in
    "help")
        usage
        ;;
    "clean")
        clean
        ;;
    "info")
        info
        ;;
    "zip")
        verifySubCommand
        zipCommand
        ;;
    *)
        usage
        ;;
esac
