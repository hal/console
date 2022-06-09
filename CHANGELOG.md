# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [3.6.0] - 2022-06-09

### Added

- [HAL-1779](https://issues.redhat.com/browse/HAL-1779): Replace 'discovery-group' and 'broadcast-group' resources
- [HAL-1739](https://issues.redhat.com/browse/HAL-1739): Add support for new encrypted expression resources
- [HAL-1709](https://issues.redhat.com/browse/HAL-1709): Add option to start stopped servers in suspended mode
- [HAL-1596](https://issues.redhat.com/browse/HAL-1596): Expose JMS bridge statistics about messages that have been processed
- [HAL-1595](https://issues.redhat.com/browse/HAL-1595): Configure support for TLS 1.3
- [HAL-1594](https://issues.redhat.com/browse/HAL-1594): Configure enhanced mapping of X509Certificate to the underlying identity
- [HAL-1593](https://issues.redhat.com/browse/HAL-1593): Configure the certificate authority used by a certificate-authority-account
- [HAL-1592](https://issues.redhat.com/browse/HAL-1592): Configure Enhanced Audit Logging
- [HAL-1591](https://issues.redhat.com/browse/HAL-1591): Configure module libraries to all deployments running in a server
- [HAL-1590](https://issues.redhat.com/browse/HAL-1590): Configure SSL Certificate revocation using OCSP

### Changed

- [HAL-1783](https://issues.redhat.com/browse/HAL-1783): Replace Flow implementation based on JS Promise API
- [HAL-1782](https://issues.redhat.com/browse/HAL-1783): Upgrade Elemental2 and Elemento
- [HAL-1779](https://issues.redhat.com/browse/HAL-1779): Replace `discovery-group` and `broadcast-group` resources
- Adjust license URLs

### Fixed

- [HAL-1791](https://issues.redhat.com/browse/HAL-1791): Fix EJB view after deprecation of attribute 'cluster' in `/subsystem=ejb3/service=remote`
- Fix item monitor
- Fix error handling in `PropertiesOperations`

### Removed

- [HAL-1785](https://issues.redhat.com/browse/HAL-1785): Remove management column
- [HAL-1758](https://issues.redhat.com/browse/HAL-1758): Remove legacy security

### Upgrades

- Bump Ace editor from 1.4.14 to 1.5.3
- Bump Clipboard from 2.0.10 to 2.0.11
- Bump D3 from 5.9.7 to 5.16.0
- Bump Datatables.net from 1.11.5 to 1.12.1
- Bump Datatables.net keytable from 2.6.4 to 2.7.0
- Bump Datatables.net select from 1.3.4 to 1.4.0
- Bump Elemental2 from 1.0.0-RC1 to 1.1.0
- Bump Elemento from 0.9.6 to 1.0.10
- Bump Guava from 25.0 to 31.1
- Bump Moment.js from 2.29.1 to 2.29.3
- Bump Mockito from 4.4.0 to 4.6.1
- Bump Parcel from 2.3.2 to 2.6.0
- Bump PouchDB from 7.2.2 to 7.3.0
- Bump Quarkus from 2.7.5.Final to 2.9.2.Final

## [3.5.12] - 2022-03-18

### Added

- [HAL-1415](https://issues.redhat.com/browse/HAL-1415): Add a global refresh icon to the header
- [HAL-1733](https://issues.redhat.com/browse/HAL-1733): Treat 'installed-driver-list' and 'find-non-progressing-operation' as read-only operations
- [HAL-1776](https://issues.redhat.com/browse/HAL-1776): I18n for data tables
- Add missing `i18n` maven profile
- Add code scanning workflow
- Add label builder rule for 'OpenAPI'

### Changed

- [HAL-1777](https://issues.redhat.com/browse/HAL-1777): Replace ZeroClipboard with clipboardjs

### Fixed

- [HAL-1778](https://issues.redhat.com/browse/HAL-1778): Fix various i18n issues
- Fix left margin for recording icon in footer
- Fix human readable duration

### Removed

- [HAL-1775](https://issues.redhat.com/browse/HAL-1775): Remove annotations and documentation for extension API

### Upgrades

- Bump Datatables.net from 1.11.4 to 1.11.5
- Bump Quarkus from 2.7.2.Final to 2.7.5.Final
- Bump Mockito from 4.3.1 to 4.4.0

## [3.5.11] - 2022-02-24

### Added

- [HAL-1590](https://issues.redhat.com/browse/HAL-1590): Add form for OCSP in Elytron Trust Manager
- [HAL-1591](https://issues.redhat.com/browse/HAL-1591): Add 'global directory' to EE settings
- [HAL-1593](https://issues.redhat.com/browse/HAL-1593): Configure the certificate authority used by a certificate-authority-account
- [HAL-1594](https://issues.redhat.com/browse/HAL-1594): Add section for evidence decoders in Elytron
- [HAL-1596](https://issues.redhat.com/browse/HAL-1596): Expose runtime attributes of jms-bridge
- [HAL-1709](https://issues.redhat.com/browse/HAL-1709): Add option to start stopped servers in suspended mode

### Changed

- [HAL-1542](https://issues.redhat.com/browse/HAL-1542): Replace Grunt with Parcel
- [HAL-1623](https://issues.redhat.com/browse/HAL-1623): Upgrade JavaScript dependencies
- Update developer related documentation 

### Fixed

- [HAL-1769](https://issues.redhat.com/browse/HAL-1769): Remove unneeded validation from credential reference
- [HAL-1774](https://issues.redhat.com/browse/HAL-1774): Adding global modules in EE fails

### Upgrades

- Bump JUnit from 4.13.1 to 4.13.2
- Bump Mockito from 2.18.3 to 4.3.1
- Bump SLF4J from 1.7.25 to 1.7.36
- Bump GWT from 2.8.2 to 2.9.0
- Bump Auto-Service from 1.0-rc4 to 1.0.1
- Bump jQuery from 3.4.1 to 3.6.0
- Bump Parcel from 2.3.1 to 2.3.2

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

### Fixed

- for any bug fixes

### Security

- in case of vulnerabilities

### Deprecated

- for soon-to-be removed features

### Removed

- for now removed features

### Upgrades

- for dependency upgrades
-->

[Unreleased]: https://github.com/hal/console/compare/v3.6.0...HEAD
[3.6.0]: https://github.com/hal/console/compare/v3.5.12...v3.6.0
[3.5.12]: https://github.com/hal/console/compare/v3.5.11...v3.5.12
[3.5.11]: https://github.com/hal/console/compare/v3.5.10...v3.5.11
[3.5.10]: https://github.com/hal/console/compare/v3.5.9...v3.5.10
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
