# Changelog

## [Unreleased]
### Added
- `spring-web` module: Spring Boot autoconfiguration that maps `ValidationException` to HTTP 422
  Problem Details responses, handles `@RequestBody` constructor failures and `@RequestParam`/`@PathVariable`
  converter type mismatches, and supports returning `Validation<T>` directly from controller methods.

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


## [1.0.0] - 2026-06-19
### Added
- Initial release with core, rules, dsl, assertj modules

[Unreleased]: https://github.com/cvanfleteren/fv/compare/v1.1.0...HEAD
[1.1.0]: https://github.com/cvanfleteren/fv/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/cvanfleteren/fv/releases/tag/v1.0.0
