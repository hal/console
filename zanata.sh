#!/usr/bin/env bash

ROOT="$PWD"
SCRIPT=`basename $0`
ARGS=$#
COMMAND=$1
SUB_COMMAND=$2

function usage {
    echo -e "Usage: $SCRIPT <help|clean|info|push|pull>\n"
    echo -e "Zanata wrapper script to push and pull the translatable resources to and from Zanata.\n"
    echo " help                        Shows this help"
    echo " clean                       Removes all temporary files"
    echo " info                        Displays details about the translatable resources such as"
    echo "                             number of constants, messages and preview files."
    echo " push <all|bundles|previews> Pushes the specified resources to Zanata."
    echo "                             The resources are first copied to target/zanata/push"
    echo " pull <all|bundles|previews> Pulls the specified resources from Zanata."
    echo "                             The resources are pulled to target/zanata/pull"
    exit 1
}

function clean {
    rm -rf target/zanata
}

function info {
    find resources/src/main/resources/org/jboss/hal/resources -name "Constants*.properties" | xargs wc -l
    find resources/src/main/resources/org/jboss/hal/resources -name "Messages*.properties" | xargs wc -l
    find resources/src/main/resources/org/jboss/hal/resources/previews -name "*.html" | xargs wc -l
}

function verifyPushPull {
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

function push {
    if [[ "$SUB_COMMAND" == "all" ]]
    then
        pushBundles
        pushPreviews
    elif [[ "$SUB_COMMAND" == "bundles" ]]
    then
        pushBundles
    elif [[ "$SUB_COMMAND" == "previews" ]]
    then
        pushPreviews
    else
        usage
    fi
}

function pushBundles {
    mkdir -p target/zanata/push/bundles
    cp resources/src/main/zanata/bundles/zanata.xml target/zanata/push/bundles
    cp resources/src/main/resources/org/jboss/hal/resources/Constants*.properties target/zanata/push/bundles
    cp resources/src/main/resources/org/jboss/hal/resources/Messages*.properties target/zanata/push/bundles
    cd target/zanata/push/bundles
    zanata-cli push --batch-mode
    cd "${ROOT}"
}

function pushPreviews {
    mkdir -p target/zanata/push/previews
    cp resources/src/main/zanata/previews/zanata.xml target/zanata/push/previews
    cp -R resources/src/main/resources/org/jboss/hal/resources/previews/ target/zanata/push/previews
    cd target/zanata/push/previews
    zanata-cli push --batch-mode --file-types "HTML[html]"
    cd "${ROOT}"
}

function pull {
    if [[ "$SUB_COMMAND" == "all" ]]
    then
        pullBundles
        pullPreviews
    elif [[ "$SUB_COMMAND" == "bundles" ]]
    then
        pullBundles
    elif [[ "$SUB_COMMAND" == "previews" ]]
    then
        pullPreviews
    else
        usage
    fi
}

function pullBundles {
    mkdir -p target/zanata/pull/bundles
    cp resources/src/main/zanata/bundles/zanata.xml target/zanata/push/bundles
    cd target/zanata/pull/bundles
    zanata-cli pull --batch-mode
    cd "${ROOT}"
}

function pullPreviews {
    mkdir -p target/zanata/pull/previews
    cp resources/src/main/zanata/previews/zanata.xml target/zanata/push/previews
    cd target/zanata/pull/previews
    zanata-cli pull --batch-mode
    cd "${ROOT}"
}

# Verify Zanata client is available
command -v zanata-cli >/dev/null 2>&1 || { echo >&2 "Zanata client not found. Follow the instructions at http://zanata-client.readthedocs.org/en/latest/#zanata-command-line-client to install the client."; exit 1; }

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
    "push")
        verifyPushPull
        push
        ;;
    "pull")
        verifyPushPull
        pull
        ;;
    *)
        usage
        ;;
esac
