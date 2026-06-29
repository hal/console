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
# Generates i18n resource bundles for a translation service:
#   - Full ZIP: all properties files and HTML previews
#   - Delta ZIP: new/changed English base strings and
#     previews since a given tag or commit
#
# -------------------------------------------------------

set -Eeuo pipefail
trap cleanup SIGINT SIGTERM ERR EXIT

VERSION=0.0.1
STAGING_DIR=""

# Change into the script's directory
# Using relative paths is safe!
script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd -P)
readonly script_dir
cd "${script_dir}"

RESOURCES_BASE="resources/src/main/resources/org/jboss/hal/resources"
OUTPUT_DIR="target/i18n"

usage() {
  cat <<EOF
USAGE:
    $(basename "${BASH_SOURCE[0]}") [FLAGS] <tag-or-commit>

FLAGS:
    -h, --help          Prints help information
    -v, --version       Prints version information
    --no-color          Uses plain text output

ARGS:
    <tag-or-commit>     A git tag or commit hash to diff against (e.g., v3.7.14)

OUTPUT:
    ${OUTPUT_DIR}/hal-i18n-full-YYYY-MM-DD.zip     All i18n resources
    ${OUTPUT_DIR}/hal-i18n-delta-<tag>.zip          Changes since <tag-or-commit>
EOF
  exit
}

cleanup() {
  trap - SIGINT SIGTERM ERR EXIT
  if [[ -n "${STAGING_DIR}" && -d "${STAGING_DIR}" ]]; then
    rm -rf "${STAGING_DIR}"
  fi
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
  [[ ${#ARGS[@]} -eq 1 ]] || die "Missing tag or commit reference. See --help."
  TAG_REF=${ARGS[0]}
  return 0
}

check_preconditions() {
  command -v git >/dev/null 2>&1 || die "git is required but not found"
  command -v zip >/dev/null 2>&1 || die "zip is required but not found"
  [[ -f "pom.xml" ]] || die "Must be run from the project root (pom.xml not found)"
  [[ -d ".git" ]] || die "Not a git repository"
  git rev-parse --verify "${TAG_REF}^{commit}" >/dev/null 2>&1 \
    || die "Tag or commit '${TAG_REF}' not found in git history"
  [[ -d "${RESOURCES_BASE}" ]] || die "Resources directory not found: ${RESOURCES_BASE}"
}

prepare_output_dir() {
  rm -rf "${OUTPUT_DIR}"
  mkdir -p "${OUTPUT_DIR}"
}

generate_full_zip() {
  local staging
  staging=$(mktemp -d)
  STAGING_DIR="${staging}"

  mkdir -p "${staging}/properties"
  cp "${RESOURCES_BASE}"/*.properties "${staging}/properties/"

  cp -R "${RESOURCES_BASE}/previews" "${staging}/previews"

  local zip_name="hal-i18n-full-$(date +%Y-%m-%d).zip"
  (cd "${staging}" && zip -r -q "${script_dir}/${OUTPUT_DIR}/${zip_name}" .)

  local prop_count html_count
  prop_count=$(find "${staging}/properties" -name '*.properties' | wc -l | tr -d ' ')
  html_count=$(find "${staging}/previews" -name '*.html' | wc -l | tr -d ' ')

  rm -rf "${staging}"
  STAGING_DIR=""

  msg "  ${GREEN}Created${NOFORMAT} ${OUTPUT_DIR}/${zip_name}"
  msg "    ${prop_count} properties files, ${html_count} HTML preview files"
  FULL_ZIP="${zip_name}"
}

generate_delta_zip() {
  local staging
  staging=$(mktemp -d)
  STAGING_DIR="${staging}"

  local has_content=false
  local delta_key_count=0

  # Extract new/changed keys from English base properties
  mkdir -p "${staging}/properties"
  for base_file in Constants.properties Messages.properties; do
    local diff_lines
    diff_lines=$(git diff "${TAG_REF}"..HEAD -- "${RESOURCES_BASE}/${base_file}" \
      | grep '^+[a-zA-Z]' \
      | sed 's/^+//' || true)

    if [[ -n "${diff_lines}" ]]; then
      echo "${diff_lines}" > "${staging}/properties/${base_file}"
      local count
      count=$(echo "${diff_lines}" | wc -l | tr -d ' ')
      delta_key_count=$((delta_key_count + count))
      has_content=true
    fi
  done

  # Find changed English-only HTML previews (exclude translation suffixes)
  local changed_previews
  changed_previews=$(git diff --name-only "${TAG_REF}"..HEAD -- "${RESOURCES_BASE}/previews/" \
    | grep -v '_fr\.html$' \
    | grep -v '_ja\.html$' \
    | grep -v '_zh_cn\.html$' || true)

  local preview_count=0
  if [[ -n "${changed_previews}" ]]; then
    while IFS= read -r file; do
      if [[ -f "${file}" ]]; then
        local rel_path
        rel_path="${file#"${RESOURCES_BASE}/previews/"}"
        local subdir
        subdir=$(dirname "${rel_path}")
        mkdir -p "${staging}/previews/${subdir}"
        cp "${file}" "${staging}/previews/${subdir}/"
        preview_count=$((preview_count + 1))
        has_content=true
      fi
    done <<< "${changed_previews}"
  fi

  if [[ "${has_content}" == false ]]; then
    msg "  ${CYAN}No changes${NOFORMAT} since ${TAG_REF} - delta ZIP not created"
    rm -rf "${staging}"
    STAGING_DIR=""
    return
  fi

  # Sanitize tag for filename (replace / with -)
  local safe_tag
  safe_tag=$(echo "${TAG_REF}" | tr '/' '-')
  local zip_name="hal-i18n-delta-${safe_tag}.zip"
  (cd "${staging}" && zip -r -q "${script_dir}/${OUTPUT_DIR}/${zip_name}" .)

  rm -rf "${staging}"
  STAGING_DIR=""

  msg "  ${GREEN}Created${NOFORMAT} ${OUTPUT_DIR}/${zip_name}"
  msg "    ${delta_key_count} changed/new property keys, ${preview_count} changed HTML preview files"
  DELTA_ZIP="${zip_name}"
}

print_summary() {
  msg ""
  msg "${GREEN}Summary${NOFORMAT}"
  msg "─────────────────────────────────────────"
  if [[ -n "${FULL_ZIP-}" ]]; then
    local size
    size=$(du -h "${OUTPUT_DIR}/${FULL_ZIP}" | cut -f1 | tr -d ' ')
    msg "  Full:  ${OUTPUT_DIR}/${FULL_ZIP} (${size})"
  fi
  if [[ -n "${DELTA_ZIP-}" ]]; then
    local size
    size=$(du -h "${OUTPUT_DIR}/${DELTA_ZIP}" | cut -f1 | tr -d ' ')
    msg "  Delta: ${OUTPUT_DIR}/${DELTA_ZIP} (${size})"
  fi
  msg ""
}

parse_params "$@"
setup_colors

msg ""
msg "Generating i18n resource bundles..."
msg "  Reference: ${CYAN}${TAG_REF}${NOFORMAT}"
msg ""

check_preconditions
prepare_output_dir

FULL_ZIP=""
DELTA_ZIP=""

msg "Full ZIP (all resources):"
generate_full_zip

msg "Delta ZIP (changes since ${TAG_REF}):"
generate_delta_zip

print_summary
