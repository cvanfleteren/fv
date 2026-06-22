# Changelog

## [Unreleased]
### Added

### Changed,

### Deprecated,

### Removed,

### Fixed,

### Security


## [1.1.0] - 2026-06-22
### Added

- Support for combining Transformations using `Transformation#andThen(Transformation<T>)` or the static
`Transformation#sequence(Transformation<T>... )` method, allowing you to combine multiple Transformations sequentially.
- Support for defining multiple Transformations in the assertThat/validateThat/after DSL classes.

### Changed,

### Deprecated,

### Removed,

### Fixed,

### Security

## [1.0.0] - 2026-06-19
### Added
- Initial release with core, rules, dsl, assertj modules

[Unreleased]: https://github.com/cvanfleteren/fv/compare/v1.1.0...HEAD
[1.1.0]: https://github.com/cvanfleteren/fv/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/cvanfleteren/fv/releases/tag/v1.0.0
