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
    $(basename "${BASH_SOURCE[0]}") [FLAGS] [version]

FLAGS:
    -l, --latest    Marks the specified version as 'latest'
    -p, --podman    Uses podman instead of docker
    -h, --help      Prints help information
    -v, --version   Prints version information
    --no-color      Uses plain text output

ARGS:
    version         WildFly version >=10 as <major>[.<minor>]
                    If not present, the WildFly development image will be build.
EOF
  exit
}

cleanup() {
  trap - SIGINT SIGTERM ERR EXIT
  rm -rf wildfly-*.tar.gz &> /dev/null
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
  DEVELOPMENT=false
  DOCKER=docker
  LATEST=false
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
    DEVELOPMENT=true
  else
    WILDFLY_VERSION=${ARGS[0]}
    [[ $WILDFLY_VERSION =~ ^([0-9]{2})(\.([0-9]{1}))?$ ]] || die "Illegal WildFly version: '$WILDFLY_VERSION'. Please use <major>[.<minor>] with mandatory major >= 10 and optional minor >= 0 and <= 9"

    WILDFLY_MAJOR_VERSION=${BASH_REMATCH[1]}
    [[ "${WILDFLY_MAJOR_VERSION}" -lt "10" ]] && die "Illegal major WildFly version: '$WILDFLY_MAJOR_VERSION'. Must be >= 10"

    WILDFLY_MINOR_VERSION=${BASH_REMATCH[3]:-0}
    [[ "${WILDFLY_MINOR_VERSION}" -lt "0" ]] && die "Illegal minor WildFly version: '$WILDFLY_MINOR_VERSION'. Must be >= 0"
    [[ "${WILDFLY_MINOR_VERSION}" -gt "9" ]] && die "Illegal major WildFly version: '$WILDFLY_MINOR_VERSION'. Must be <= 9"
  fi

  return 0
}

parse_params "$@"
setup_colors

if [[ "${DEVELOPMENT}" == "true" ]]; then
  #
  # Development build
  #
  TAG=quay.io/halconsole/wildfly-development
  TAG_DOMAIN=quay.io/halconsole/wildfly-domain-development
  WILDFLY_CODEBASE="/tmp/wildfly-$(date '+%Y%m%d')"
  WILDFLY_REPO=https://github.com/wildfly/wildfly.git
  if [[ -d "${WILDFLY_CODEBASE}" ]]; then
    msg
    msg "Use WildFly build in ${CYAN}${WILDFLY_CODEBASE}${NOFORMAT}"
    cd "${WILDFLY_CODEBASE}"
    WILDFLY_VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate -Dexpression=project.version -q -DforceStdout)
    cp "${WILDFLY_CODEBASE}/dist/target/wildfly-${WILDFLY_VERSION}.tar.gz" "${script_dir}"
    cd "${script_dir}"
  else
    msg
    msg "Clone and build codebase from ${CYAN}${WILDFLY_REPO}${NOFORMAT}"
    git clone ${WILDFLY_REPO} "${WILDFLY_CODEBASE}"
    cd "${WILDFLY_CODEBASE}"
    WILDFLY_VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate -Dexpression=project.version -q -DforceStdout)
    mvn \
  -Dcheckstyle.skip \
  -Denforcer.skip \
  -Dfindbugs.skip \
  -Dformatter.skip \
  -Dgmaven.execute.skip \
  -Dimpsort.skip \
  -Dlicense.skip \
  -Dmaven.javadoc.skip \
  -Dpmd.skip \
  -DskipITs \
  -DskipTests \
  -Dgalleon.offline=false \
  install
    cp "dist/target/wildfly-${WILDFLY_VERSION}.tar.gz" "${script_dir}"
    cd "${script_dir}"
  fi

  msg
  msg "Build WildFly ${CYAN}standalone${NOFORMAT} ${YELLOW}${WILDFLY_VERSION}${NOFORMAT}"
  ${DOCKER} build \
    --platform linux/amd64 \
    --build-arg WILDFLY_VERSION="${WILDFLY_VERSION}" \
    --file src/main/docker/Dockerfile-standalone-development \
    --tag "${TAG}" \
    .

  msg
  msg "Build WildFly ${CYAN}domain${NOFORMAT} ${YELLOW}${WILDFLY_VERSION}${NOFORMAT}"
  ${DOCKER} build \
    --platform linux/amd64 \
    --build-arg WILDFLY_VERSION="${WILDFLY_VERSION}" \
    --file src/main/docker/Dockerfile-domain-development \
    --tag "${TAG_DOMAIN}" \
    .

  cleanup
else
  #
  # Release build
  #
  BASE=quay.io/wildfly/wildfly
  TAG=quay.io/halconsole/wildfly
  TAG_DOMAIN=quay.io/halconsole/wildfly-domain
  WILDFLY_VERSION=$WILDFLY_MAJOR_VERSION.$WILDFLY_MINOR_VERSION.0.Final
  if [[ "$LATEST" == "true" ]]; then
    TAG_VERSION=latest
  else
    TAG_VERSION=$WILDFLY_VERSION
  fi

  # Images for WildFly 23 and below are in Docker Hub
  if [[ "$WILDFLY_MAJOR_VERSION" -lt "24" ]]; then
    BASE=docker.io/jboss/wildfly
  fi
  # Use JDK17 for for WildFly 27 and above
  if [[ "$WILDFLY_MAJOR_VERSION" -gt "26" ]]; then
    WILDFLY_VERSION=$WILDFLY_VERSION-jdk17
  fi

  msg
  msg "Build WildFly ${CYAN}standalone${NOFORMAT} ${YELLOW}${WILDFLY_VERSION}${NOFORMAT}"
  ${DOCKER} build \
    --platform linux/amd64 \
    --build-arg WILDFLY_VERSION="${WILDFLY_VERSION}" \
    --build-arg DOCKER_BASE="${BASE}" \
    --file src/main/docker/Dockerfile-standalone \
    --tag "${TAG}:${TAG_VERSION}" \
    .

  msg
  msg "Build WildFly ${CYAN}domain${NOFORMAT} ${YELLOW}${WILDFLY_VERSION}${NOFORMAT}"
  ${DOCKER} build \
    --platform linux/amd64 \
    --build-arg WILDFLY_VERSION="${WILDFLY_VERSION}" \
    --build-arg DOCKER_BASE="${BASE}" \
    --file src/main/docker/Dockerfile-domain \
    --tag "${TAG_DOMAIN}:${TAG_VERSION}" \
    .
fi
