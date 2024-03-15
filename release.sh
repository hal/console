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


# -------------------------------------------------------
#
# Creates a new release:
#   - bump to release version
#   - update changelog
#   - commit & push changes
#   - create & push tag (trigger GitHub release workflow)
#   - bump to next snapshot version
#   - commit & push changes
#
# -------------------------------------------------------

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
    $(basename "${BASH_SOURCE[0]}") [FLAGS] <release-version> <next-version>

FLAGS:
    -h, --help          Prints help information
    -v, --version       Prints version information
    --no-color          Uses plain text output

ARGS:
    <release-version>   The release version (as semver)
    <next-snapshot>     The next snapshot version  (as semver)
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
  [[ ${#ARGS[@]} -eq 2 ]] || die "Missing release and/or snapshot version"
  RELEASE_VERSION=${ARGS[0]}
  NEXT_VERSION=${ARGS[1]}
  return 0
}

is_semver() {
    local version
    version="$1"
    if [[ ! ${version} =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
        return 1
    fi
}

version_gt() { test "$(echo "$@" | tr " " "\n" | sort -V | head -n 1)" != "$1"; }

parse_params "$@"
setup_colors

FINAL_VERSION="${RELEASE_VERSION}.Final"
GIT_REMOTES=("origin" "upstream")
SNAPSHOT_VERSION="${NEXT_VERSION}-SNAPSHOT"
TAG="v${RELEASE_VERSION}"
WORKFLOW_URL="https://github.com/hal/console/actions/workflows/release.yml"

is_semver "${RELEASE_VERSION}" || die "Release version is not a semantic version"
is_semver "${NEXT_VERSION}" || die "Next version is not a semantic version"
version_gt "${NEXT_VERSION}" "${RELEASE_VERSION}" || die "Next version must be greater than release version"
git diff-index --quiet HEAD || die "You have uncommitted changes"
[[ $(git tag -l "${TAG}") ]] && die "Tag ${TAG} already defined"

msg ""
msg "Codebase is ready to be released."
msg ""
msg "If you decide to continue, this script will "
msg ""
msg "   1. Bump the version to ${CYAN}${FINAL_VERSION}${NOFORMAT}"
msg "   2. Update the ${CYAN}changelog${NOFORMAT} (there should already be entries in the ${CYAN}Unreleased${NOFORMAT} section!)"
msg "   3. Create a tag for ${CYAN}${TAG}${NOFORMAT}"
msg "   4. ${CYAN}Commit${NOFORMAT} and ${CYAN}push${NOFORMAT} to upstream (which will trigger the ${CYAN}release workflow${NOFORMAT} at GitHub)"
msg "   5. Bump the version to ${CYAN}${SNAPSHOT_VERSION}${NOFORMAT}"
msg "   6. ${CYAN}Commit${NOFORMAT} and ${CYAN}push${NOFORMAT} to upstream"
msg ""
echo "Do you wish to continue?"
select yn in "Yes" "No"; do
    case $yn in
        Yes ) break;;
        No ) die "Aborted ";;
    esac
done

msg ""
./versionBump.sh "${FINAL_VERSION}"
msg "Update changelog"
mvn --quiet -DskipModules keepachangelog:release &> /dev/null
# Maven plugin keepachangelog:release uses the project version from the POM (1.2.3.Final),
# but action mindsers/changelog-reader-action in release.yml requires a semantic version (1.2.3).
# So replace [1.2.3.Final] -> [1.2.3] and v1.2.3.Final -> v1.2.3
sed -E -i '' -e 's/\[([0-9]+\.[0-9]+\.[0-9]+)\.Final\]/[\1]/g' -e 's/v([0-9]+\.[0-9]+\.[0-9]+)\.Final/v\1/g' CHANGELOG.md
msg "Push changes"
git commit --quiet -am "Release ${RELEASE_VERSION}"
for remote in "${GIT_REMOTES[@]}"; do
  git push --quiet "${remote}" 3.6.x &> /dev/null
  msg "    ${YELLOW}✓${NOFORMAT} ${remote}"
done
msg "Push tag"
git tag "${TAG}"
for remote in "${GIT_REMOTES[@]}"; do
  git push --quiet --tags "${remote}" 3.6.x &> /dev/null
  msg "    ${YELLOW}✓${NOFORMAT} ${remote}"
done
./versionBump.sh "${SNAPSHOT_VERSION}"
msg "Push changes"
git commit --quiet -am "Next is ${NEXT_VERSION}"
for remote in "${GIT_REMOTES[@]}"; do
  git push --quiet "${remote}" 3.6.x &> /dev/null
  msg "    ${YELLOW}✓${NOFORMAT} ${remote}"
done
msg "Done. Watch the release workflow at ${WORKFLOW_URL}"
