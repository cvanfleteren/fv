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

## [1.0.0] - 2026-06-19
### Added
- Initial release with core, rules, dsl, assertj modules

[Unreleased]: https://github.com/cvanfleteren/fv/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/cvanfleteren/fv/releases/tag/v1.0.0
