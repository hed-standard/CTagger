# Changelog

All notable changes to CTagger will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [4.0.0] - 2025-11-19

### Changed
- **IMPORTANT**: Downgraded Java bytecode target from 17 to 8 for MATLAB compatibility
  - CTagger now works with MATLAB R2017b and newer (which include Java 8)
  - Addresses compatibility issue with MATLAB versions prior to R2024a
- Updated build configuration to target Java 8 while building with modern tooling
- Reverted `URI().toURL()` to `URL()` constructor for Java 8 compatibility
- Reverted `lowercase()` to `toLowerCase()` for Java 8 compatibility
- Updated documentation to reflect Java 8+ requirements

### Added
- Semantic versioning (4.0.0)
- Version metadata embedded in JAR manifest
- VERSION file for version tracking
- GitHub Actions workflows:
  - Continuous Integration (build and test)
  - Automated releases with JAR artifacts
  - CodeQL security scanning
- Build status badges in README
- Comprehensive README with download, usage, and build instructions
- CHANGELOG.md following Keep a Changelog format

### Fixed
- Removed hardcoded Windows-specific JAVA_HOME from gradle.properties
- Fixed deprecated Gradle syntax for Java 9+ compatibility
- Cleaned up 12+ compiler warnings (removed unused variables, unnecessary null assertions)
- Fixed URL typo in HED service endpoint
- Corrected HED validation API parameters (sidecar_string, check_for_warnings)

### Infrastructure
- Upgraded Gradle from 6.9 to 8.5
- Upgraded Kotlin from 1.5.21 to 1.9.25
- Updated Gson from 2.8.7 to 2.10.1 (security fix)
- Updated Jsoup from 1.11.2 to 1.17.2 (security fix)
- Removed JCenter repository (deprecated)
- Organized documentation and images into structured directories

### Documentation
- Reorganized repository documentation
- Moved old docs to status/old_docs for archival
- Updated CTagger Guide with current Java requirements
- Changed repository references from VisLab to hed-standard

### Runtime Requirements
- **Java Runtime Environment (JRE) 8 or higher**

### Build Requirements
- **Java JDK 8 or higher** (tested with JDK 21)
- **Gradle 8.5+** (wrapper included)

---

## Historical Context

Prior to version 4.0.0, CTagger required Java 17. This release downgrades the Java requirement to version 8 to ensure compatibility with older MATLAB installations (R2017b+) while maintaining modern build tooling and security updates.

[4.0.0]: https://github.com/hed-standard/ctagger/releases/tag/v4.0.0
