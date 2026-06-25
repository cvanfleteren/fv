# Changelog

## [Unreleased]
### Added

- add DSL.notNull(value, field) as a shorthand for validateThat(...).isNotNull()
- add assertInvalid(Runnable) for testing code that throws ValidationException

### Changed,

### Deprecated,

### Removed,

### Fixed,

### Security


## [2.0.0] - 2026-06-24
### Added
- `spring-web` module: Spring Boot autoconfiguration that maps `ValidationException` to HTTP 422
  Problem Details responses, handles `@RequestBody` constructor failures and `@RequestParam`/`@PathVariable`
  converter type mismatches, and supports returning `Validation<T>` directly from controller methods.

### Changed,

- Breaking: MappingRule and Rule no longer extend Function, but RuleLike. This removes the inherited `andThen` method, 
which was very confusing if you called it expecting to be able to combine with a Rule/MappingRule.
- Breaking: the `after(Transformation)` methods no longer return a Rule, but a MappingRule. The previous behavior was 
misleading at best, wrong at worst. Combining Rules created with after would in some cases lead to the result of the 
transformation not being passed further.
- Breaking: `strings.asEnum` is no longer case-insensitive, but now behaves the same as the Enum.valueOf method. Use 
`strings.asEnumIgnoreCase` if you want to ignore case. The same applies to `strings.canBeEnum`.
- Breaking: `ValidationFactory#_try` is renamed to `attempt`.
- Breaking: renamed /deleted / added methods in `StringOps` and `StringRules`, changed error keys.
- Breaking: renamed `AssertDSL` -> `AssertThatDSL` and `ValidationDSL` -> `ValidateThatDSL`, but these shouldn't have been 
used directly.

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
[2.0.0]: https://github.com/cvanfleteren/fv/compare/v1.1.0...v2.0.0
[1.1.0]: https://github.com/cvanfleteren/fv/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/cvanfleteren/fv/releases/tag/v1.0.0
