<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# MerLoc IntelliJ Plugin Changelog

## [Unreleased]
### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [0.0.7] - 2022-12-12
### Added

### Changed
- Upgrade to MerLoc Java runtime `0.0.12`

### Deprecated

### Removed

### Fixed

### Security

## [0.0.6] - 2022-11-30
### Added
- Add optional API key config while connecting to broker

### Changed
- Upgrade to MerLoc Java runtime `0.0.11`

## [0.0.5] - 2022-09-27
### Fixed
- Upgrade to MerLoc Java runtime `0.0.10` to fix update of missing `AWS_SECRET_ACCESS_KEY` environment variable at each invocation

## [0.0.4] - 2022-09-26
### Fixed
- Upgrade to MerLoc Java runtime `0.0.9` to fix reflection based object and class field access issue on JDK 17 by accessing with Unsafe instead

## [0.0.3] - 2022-09-24
### Fixed
- Upgrade to MerLoc Java runtime `0.0.8` to handle error during initialization of phone-home service if network interface couldn't be detected for localhost

## [0.0.2] - 2022-09-01
### Added
- Initial release