#!/usr/bin/env bash

set -Eeuo pipefail
trap cleanup SIGINT SIGTERM ERR EXIT

VERSION=0.0.1

script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd -P)
readonly script_dir
cd "${script_dir}"

usage() {
  cat <<EOF
USAGE:
    $(basename "${BASH_SOURCE[0]}") [FLAGS] [version]

FLAGS:
    -l, --latest    Use 'latest' for tagging the image
    -p, --podman    Use podman instead of docker
    -h, --help      Prints help information
    -v, --version   Prints version information
    --no-color      Uses plain text output

ARGS:
    version         WildFly version >=10 as <major>[.<minor>]
                    If not present, the WildFly development image will be pushed.
EOF
  exit
}

cleanup() {
  trap - SIGINT SIGTERM ERR EXIT
}

setup_colors() {
  if [[ -t 2 ]] && [[ -z "${NO_COLOR-}" ]] && [[ "${TERM-}" != "dumb" ]]; then
    NOFORMAT='\033[0m' RED='\033[0;31m' GREEN='\033[0;32m' ORANGE='\033[0;33m' BLUE='\033[0;34m' PURPLE='\033[0;35m' CYAN='\033[0;36m' YELLOW='\033[1;33m'
  else
    # shellcheck disable=SC2034
    NOFORMAT='' RED='' GREEN='' ORANGE='' BLUE='' PURPLE='' CYAN='' YELLOW=''
  fi
}

msg() {
  echo >&2 -e "${1-}"
}

die() {
  local msg=$1
  local code=${2-1} # default exit status 1
  msg "$msg"
  exit "$code"
}

version() {
  msg "${BASH_SOURCE[0]} $VERSION"
  exit 0
}

parse_params() {
  LATEST=false
  DOCKER=docker

  while :; do
    case "${1-}" in
    -l | --latest) LATEST=true ;;
    -p | --podman) DOCKER=podman ;;
    -h | --help) usage ;;
    -v | --version) version ;;
    --no-color) NO_COLOR=1 ;;
    -?*) die "Unknown option: $1" ;;
    *) break ;;
    esac
    shift
  done

  ARGS=("$@")
  if [[ ${#ARGS[@]} -eq 0 ]]; then
    WILDFLY_VERSION=development
    TAG=quay.io/halconsole/wildfly-development
    TAG_DOMAIN=quay.io/halconsole/wildfly-domain-development
    TAG_VERSION=latest
  else
    WILDFLY_VERSION=${ARGS[0]}
    [[ $WILDFLY_VERSION =~ ^([0-9]{2})(\.([0-9]{1}))?$ ]] || die "Illegal WildFly version: '$WILDFLY_VERSION'. Please use <major>[.<minor>] with mandatory major >= 10 and optional minor >= 0 and <= 9"

    WILDFLY_MAJOR_VERSION=${BASH_REMATCH[1]}
    [[ "${WILDFLY_MAJOR_VERSION}" -lt "10" ]] && die "Illegal major WildFly version: '$WILDFLY_MAJOR_VERSION'. Must be >= 10"

    WILDFLY_MINOR_VERSION=${BASH_REMATCH[3]:-0}
    [[ "${WILDFLY_MINOR_VERSION}" -lt "0" ]] && die "Illegal minor WildFly version: '$WILDFLY_MINOR_VERSION'. Must be >= 0"
    [[ "${WILDFLY_MINOR_VERSION}" -gt "9" ]] && die "Illegal major WildFly version: '$WILDFLY_MINOR_VERSION'. Must be <= 9"

    TAG=quay.io/halconsole/wildfly
    TAG_DOMAIN=quay.io/halconsole/wildfly-domain
    if [[ "$LATEST" == "true" ]]; then
      TAG_VERSION=latest
    else
      TAG_VERSION=$WILDFLY_MAJOR_VERSION.$WILDFLY_MINOR_VERSION.0.Final
    fi
  fi

  return 0
}

parse_params "$@"
setup_colors

# Requires a valid configuration in ~/.docker/config.json
${DOCKER} login quay.io

msg
msg "Push WildFly ${CYAN}standalone${NOFORMAT} ${YELLOW}${WILDFLY_VERSION}${NOFORMAT}"
${DOCKER} push "${TAG}:${TAG_VERSION}"

msg
msg "Push WildFly ${CYAN}standalone${NOFORMAT} ${YELLOW}${WILDFLY_VERSION}${NOFORMAT}"
${DOCKER} push "${TAG_DOMAIN}:${TAG_VERSION}"
