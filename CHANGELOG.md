# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- [HAL-1542](https://issues.redhat.com/browse/HAL-1542): Replace Grunt with Parcel
- [HAL-1590](https://issues.redhat.com/browse/HAL-1590): Add form for OCSP in Elytron Trust Manager
- [HAL-1591](https://issues.redhat.com/browse/HAL-1591): Add 'global directory' to EE settings
- [HAL-1593](https://issues.redhat.com/browse/HAL-1593): Configure the certificate authority used by a certificate-authority-account
- [HAL-1594](https://issues.redhat.com/browse/HAL-1594): Add section for evidence decoders in Elytron
- [HAL-1596](https://issues.redhat.com/browse/HAL-1596): Expose runtime attributes of jms-bridge
- [HAL-1623](https://issues.redhat.com/browse/HAL-1623): Upgrade JavaScript dependencies
- [HAL-1623](https://issues.redhat.com/browse/HAL-1623): Upgrade JavaScript dependencies 
- [HAL-1709](https://issues.redhat.com/browse/HAL-1709): Add option to start stopped servers in suspended mode
- [HAL-1769](https://issues.redhat.com/browse/HAL-1769): remove unneeded validation from credential reference
- Update developer related documentation 

### Upgrades

0bde28026 Merge pull request #535 from hal/dependabot/maven/org.mockito-mockito-core-4.3.1
90a3d6e4b Merge pull request #515 from hal/dependabot/maven/version.elemental-1.1.0
21873619a Bump mockito-core from 2.18.3 to 4.3.1
823bab0e6 Configure ingore rules for dependabot
65b17d309 Merge pull request #533 from hal/dependabot/maven/com.google.gwt-gwt-2.9.0
8d8b5beb9 Upgrade GWT to 2.9.0 and Elemental to 1.1.0
68c228960 Bump gwt from 2.8.2 to 2.9.0
0f464d6f4 Merge pull request #521 from hal/dependabot/npm_and_yarn/app/jquery-3.6.0
4db4f4648 Merge pull request #532 from hal/dependabot/maven/net.revelc.code.formatter-formatter-maven-plugin-2.17.1
0ef4391d3 Bump formatter-maven-plugin from 2.17.0 to 2.17.1
a5cf24c11 Merge pull request #531 from hal/dependabot/maven/com.google.auto.service-auto-service-1.0.1
2ac6b0b97 Merge pull request #525 from hal/dependabot/npm_and_yarn/app/parcel/transformer-less-2.3.2
d9033e408 Bump jquery from 3.4.1 to 3.6.0 in /app
13c3d380b Bump @parcel/transformer-less from 2.3.1 to 2.3.2 in /app
6b3324d1b Merge pull request #513 from hal/dependabot/github_actions/battila7/get-version-action-2.3.0
0c7688cdc Bump auto-service from 1.0-rc4 to 1.0.1
bd8aba827 Merge pull request #516 from hal/dependabot/github_actions/JamesIves/github-pages-deploy-action-4.2.5
bdd7b6810 Merge pull request #529 from hal/dependabot/maven/version.slf4j-1.7.36
b6630a75b Merge pull request #527 from hal/dependabot/npm_and_yarn/app/parcel-2.3.2
e05fc0a5c Bump parcel from 2.3.1 to 2.3.2 in /app
de890bec6 Bump version.slf4j from 1.7.25 to 1.7.36
2fcc7ac40 Merge pull request #522 from hal/dependabot/maven/junit-junit-4.13.2
e4783f6ee Merge pull request #523 from hal/dependabot/maven/org.sonatype.plugins-nexus-staging-maven-plugin-1.6.11
70b40f08a Fix EAP build
09748e37c Merge pull request #524 from hpehl/HAL-1542
e7a90d029 (origin/HAL-1542, HAL-1542) HAL-1542: Migration to Parcel
2270d4481 Bump nexus-staging-maven-plugin from 1.6.8 to 1.6.11
0dcfd8151 Bump junit from 4.13.1 to 4.13.2
36b219d51 Bump version.elemental from 1.0.0-RC1 to 1.1.0
1161985bd Bump JamesIves/github-pages-deploy-action from 4.2.3 to 4.2.5
85d867ad6 Bump battila7/get-version-action from 2.2.1 to 2.3.0

## [3.5.10] - 2022-02-16

### Updated

- Update README.md
- Update CONTRIBUTING.md

### Removed

- Remove test workflow for native binaries

## [3.5.9] - 2022-02-16

### Changed

- Update README.md

## [3.5.8] - 2022-02-16

### Upgrades

- Upgrade Node to 16.14.0

### Fixed

- Fix native build

## [3.5.7] - 2022-02-16

### Fixed 

- Fix native build
- Fix release workflow

## [3.5.6] - 2022-02-16

### Added

- Publish HAL standalone container as `:<version>` and `:latest` if `<version>` is the latest version.

### Changed

- Verify versions in release script
- Optimise Git operations in release script

### Fixed 

- Fix native binary generation

## [3.5.5] - 2022-02-15

### Added

- Use PAT for release workflow

### Changed 

- Optimise release script

## [3.5.4] - 2022-02-15

### Added

- Native build for Linux, macOS and Windows
- Attach native binaries to GitHub releases

### Fixed

- Fix release workflow

## [3.5.3] - 2022-02-15

### Added 

- Add verification question in release script

### Fixed

- Fix release script

## [3.5.2] - 2022-02-15

### Changed

- Update documentation

### Fixed

- Fix release script
- Fix release workflow

## [3.5.1] - 2022-02-15

### Added 

- Add release script

### Fixed

- Fix release workflow

## [3.5.0] - 2022-02-14

### Added

- Add maven plugins to verify codebase & source code:
  - Enforcer rules
  - Checkstyle rules
  - Eclipse code formatter definitions
  - License check
  - Common import statement order
- Add GitHub workflows to verify and release HAL
- [HAL-1767](https://issues.redhat.com/browse/HAL-1767): Add active thread count
- [HAL-1766](https://issues.redhat.com/browse/HAL-1766): Support direct connect URL for standalone mode

### Changed

- Update documentation

### Fixed

- [HAL-1772](https://issues.redhat.com/browse/HAL-1772): Fix adding messaging servers

<!--
## Template

### Added

- for new features

### Changed

- for changes in existing functionality

### Upgrades

- for dependency upgrades

### Deprecated

- for soon-to-be removed features

### Removed

- for now removed features

### Fixed

- for any bug fixes

### Security

- in case of vulnerabilities
-->

[Unreleased]: https://github.com/hal/console/compare/v3.5.10.Final...HEAD
[3.5.10]: https://github.com/hal/console/compare/v3.5.9...v3.5.10.Final
[3.5.9]: https://github.com/hal/console/compare/v3.5.8...v3.5.9
[3.5.8]: https://github.com/hal/console/compare/v3.5.7...v3.5.8
[3.5.7]: https://github.com/hal/console/compare/v3.5.6...v3.5.7
[3.5.6]: https://github.com/hal/console/compare/v3.5.5...v3.5.6
[3.5.5]: https://github.com/hal/console/compare/v3.5.4...v3.5.5
[3.5.4]: https://github.com/hal/console/compare/v3.5.3...v3.5.4
[3.5.3]: https://github.com/hal/console/compare/v3.5.2...v3.5.3
[3.5.2]: https://github.com/hal/console/compare/v3.5.1...v3.5.2
[3.5.1]: https://github.com/hal/console/compare/v3.5.0...v3.5.1
[3.5.0]: https://github.com/hal/console/compare/vTemplate...v3.5.0
