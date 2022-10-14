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
    $(basename "${BASH_SOURCE[0]}") [FLAGS] [version] [parameters]

FLAGS:
    -d, --domain    Starts in domain mode
    -p, --podman    Uses podman instead of docker
    -h, --help      Prints help information
    -v, --version   Prints version information
    --no-color      Uses plain text output

ARGS:
    version         WildFly version >=10 as <major>[.<minor>]
                    If not present, the WildFly development image will be started.
    parameters      Parameters passed to standalone.sh | domain.sh
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
  DOCKER=docker
  MODE=standalone

  while :; do
    case "${1-}" in
    -d | --domain) MODE=domain ;;
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
    use_development
    WILDFLY_PARAM="$*"
  else
    WILDFLY_VERSION=${ARGS[0]}
    if [[ $WILDFLY_VERSION =~ ^([0-9]{2})(\.([0-9]{1}))?$ ]]; then
      WILDFLY_MAJOR_VERSION=${BASH_REMATCH[1]}
      [[ "${WILDFLY_MAJOR_VERSION}" -lt "10" ]] && die "Illegal major WildFly version: '$WILDFLY_MAJOR_VERSION'. Must be >= 10"

      WILDFLY_MINOR_VERSION=${BASH_REMATCH[3]:-0}
      [[ "${WILDFLY_MINOR_VERSION}" -lt "0" ]] && die "Illegal minor WildFly version: '$WILDFLY_MINOR_VERSION'. Must be >= 0"
      [[ "${WILDFLY_MINOR_VERSION}" -gt "9" ]] && die "Illegal major WildFly version: '$WILDFLY_MINOR_VERSION'. Must be <= 9"

      HTTP_PORT=$([[ "$WILDFLY_MINOR_VERSION" -eq "0" ]] && echo "80${WILDFLY_MAJOR_VERSION}" || echo "8${WILDFLY_MAJOR_VERSION}${WILDFLY_MINOR_VERSION}")
      MGMT_PORT=$([[ "$WILDFLY_MINOR_VERSION" -eq "0" ]] && echo "99${WILDFLY_MAJOR_VERSION}" || echo "9${WILDFLY_MAJOR_VERSION}${WILDFLY_MINOR_VERSION}")
      IMAGE_NAME=${WILDFLY_VERSION}
      TAG=quay.io/halconsole/wildfly
      TAG_DOMAIN=quay.io/halconsole/wildfly-domain
      TAG_VERSION=$WILDFLY_MAJOR_VERSION.$WILDFLY_MINOR_VERSION.0.Final
      shift
      WILDFLY_PARAM="$*"
    else
      use_development
      WILDFLY_PARAM="$*"
    fi
  fi

  return 0
}

use_development() {
    HTTP_PORT=8080
    MGMT_PORT=9990
    IMAGE_NAME=development
    TAG=quay.io/halconsole/wildfly-development
    TAG_DOMAIN=quay.io/halconsole/wildfly-domain-development
    TAG_VERSION=latest
}

parse_params "$@"
setup_colors

msg "Start WildFly ${CYAN}${MODE}${NOFORMAT} ${YELLOW}${IMAGE_NAME}${NOFORMAT} using"
msg "    ${PURPLE}${HTTP_PORT}${NOFORMAT} for HTTP endpoint and"
msg "    ${PURPLE}${MGMT_PORT}${NOFORMAT} for management endpoint"

# Please don't put double quotes around ${WILDFLY_PARAM-}
if [[ "${MODE}" == "standalone" ]]; then
  ${DOCKER} run \
    --platform linux/amd64 \
    --rm \
    --name="hal-wildfly-${IMAGE_NAME}" \
    --publish="${HTTP_PORT}:8080" \
    --publish="${MGMT_PORT}:9990" \
    "${TAG}:${TAG_VERSION}" ${WILDFLY_PARAM-}
elif [[ "${MODE}" == "domain" ]]; then
  ${DOCKER} run \
    --platform linux/amd64 \
    --rm \
    --name="hal-wildfly-domain-${IMAGE_NAME}" \
    --publish="${HTTP_PORT}:8080" \
    --publish="${MGMT_PORT}:9990" \
    "${TAG_DOMAIN}:${TAG_VERSION}" ${WILDFLY_PARAM-}
else
  die "No operation mode (standalone|domain) given!"
fi
