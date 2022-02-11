#!/usr/bin/env bash
#
#  Copyright 2022 Red Hat
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#


# --------------------------------------------------
#
# Build and optionally push HAL standalone
# native binary and container
#
# --------------------------------------------------

set -Eeuo pipefail
trap cleanup SIGINT SIGTERM ERR EXIT

VERSION=0.0.1

# Change into the script's directory
# Using relative paths is safe!
script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd -P)
readonly script_dir
cd "${script_dir}"

usage() {
  cat <<EOF
USAGE:
    $(basename "${BASH_SOURCE[0]}") [FLAGS] [<tag>]

FLAGS:
    -h, --help          Prints help information
    -v, --version       Prints version information
    --no-color          Uses plain text output
    --push              Whether to push the container to quay.io

ARGS:
    <tag>               Image tag ('latest' is used by default)
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
  PUSH=false
  TAG=latest
  while :; do
    case "${1-}" in
    -h | --help) usage ;;
    -v | --version) version ;;
    --no-color) NO_COLOR=1 ;;
    --push) PUSH=true ;;
    -?*) die "Unknown option: $1" ;;
    *) break ;;
    esac
    shift
  done

  ARGS=("$@")
  if [[ ${#ARGS[@]} -eq 1 ]]; then
    TAG=${ARGS[0]}
  fi
  return 0
}

parse_params "$@"
setup_colors

[ -x podman ] && die "Podman not available"
# Requires a valid configuration in ~/.docker/config.json
podman login quay.io
mvn package \
    --projects standalone \
    --also-make \
    -P native,prod \
    -Dquarkus.native.container-runtime=podman \
    -Dquarkus.native.remote-container-build=true \
    -Dquarkus.container-image.build=true \
    -Dquarkus.container-image.push=$PUSH \
    -Dquarkus.container-image.image=quay.io/halconsole/hal:$TAG
