#!/usr/bin/env bash
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
    -h, --help      Prints help information
    -v, --version   Prints version information
    --no-color      Uses plain text output

ARGS:
    version         WildFly version >=10 as <major>[.<minor>]
                    If not present, the WildFly development image will be used.
    parameters      Parameters passed to jboss-cli.sh
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
  while :; do
    case "${1-}" in
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
    CLI_PARAM="$*"
  else
    WILDFLY_VERSION=${ARGS[0]}
    if [[ $WILDFLY_VERSION =~ ^([0-9]{2})(\.([0-9]{1}))?$ ]]; then
      WILDFLY_MAJOR_VERSION=${BASH_REMATCH[1]}
      [[ "${WILDFLY_MAJOR_VERSION}" -lt "10" ]] && die "Illegal major WildFly version: '$WILDFLY_MAJOR_VERSION'. Must be >= 10"

      WILDFLY_MINOR_VERSION=${BASH_REMATCH[3]:-0}
      [[ "${WILDFLY_MINOR_VERSION}" -lt "0" ]] && die "Illegal minor WildFly version: '$WILDFLY_MINOR_VERSION'. Must be >= 0"
      [[ "${WILDFLY_MINOR_VERSION}" -gt "9" ]] && die "Illegal major WildFly version: '$WILDFLY_MINOR_VERSION'. Must be <= 9"

      MGMT_PORT=$([[ "$WILDFLY_MINOR_VERSION" -eq "0" ]] && echo "99${WILDFLY_MAJOR_VERSION}" || echo "9${WILDFLY_MAJOR_VERSION}${WILDFLY_MINOR_VERSION}")
      WILDFLY_VERSION=$WILDFLY_MAJOR_VERSION.$WILDFLY_MINOR_VERSION.0.Final
      shift
      CLI_PARAM="$*"
    else
      use_development
      CLI_PARAM="$*"
    fi
  fi

  return 0
}

use_development() {
  MGMT_PORT=9990
  WILDFLY_VERSION=development
}

parse_params "$@"
setup_colors

WF_CORE_VERSION=18.1.2.Final
CLI_JAR_URL=https://repo1.maven.org/maven2/org/wildfly/core/wildfly-cli/$WF_CORE_VERSION/wildfly-cli-$WF_CORE_VERSION-client.jar
CLI_XML_URL=https://raw.githubusercontent.com/wildfly/wildfly-core/main/core-feature-pack/common/src/main/resources/content/bin/jboss-cli.xml

[[ -x "$(command -v java)" ]] || die "Java not found"
[[ -f "${TMPDIR}/cli.xml" ]] || curl -s "${CLI_XML_URL}" --output "${TMPDIR}/cli.xml"
[[ -f "${TMPDIR}/cli.jar" ]] || curl -s "${CLI_JAR_URL}" --output "${TMPDIR}/cli.jar"

msg "Connect to WildFly ${CYAN}${WILDFLY_VERSION}${NOFORMAT} CLI on port ${YELLOW}${MGMT_PORT}"

# Please don't put double quotes around ${CLI_PARAM-}
java -Djboss.cli.config="${TMPDIR}/cli.xml" -jar "${TMPDIR}/cli.jar" \
  --user=admin \
  --password=admin \
  --controller="localhost:${MGMT_PORT}" \
  --connect ${CLI_PARAM-}
