# Changelog

## [NEXT] - TBD

### Added
- `ValidatingDSL.ValidatingBuilder2#is(Rule<Tuple2<T1, T2>>)` and `#is(String, Rule<Tuple2<T1, T2>>)`, allowing
  cross-field validation on `validating(v1, v2)` without manually combining the values into a `Tuple2` first.
- `ComparableRules#strictlyOrdered()`, a reusable `Rule<Tuple2<T, T>>` failing unless the first element is
  strictly before the second, exposed via a new `PairRules` class (`DSL.pairs`).
- `PairRules`: added `ordered()`, `strictlyDescending()`, `descending()` ordering variants, `equal()`/`notEqual()`
  equality checks, and a generic `satisfies(BiPredicate<T1, T2>, errorKey/ErrorMessage)` for arbitrary two-argument
  invariants between the two elements of a pair.
- `ValidatingDSL.ValidatingBuilder3#is(Rule<Tuple3<T1, T2, T3>>)` and `ValidatingBuilder4#is(Rule<Tuple4<T1, T2, T3, T4>>)`,
  extending the `.is(rule)` cross-field shortcut to three and four fields.
- `DSL#satisfies(Function3<T1, T2, T3, Boolean>, errorKey/ErrorMessage)`, a general `Rule<Tuple3<T1, T2, T3>>`
  factory for arbitrary three-argument invariants, for use with `validating(v1, v2, v3).is(...)`.

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [2.1.0] - 2026-08-xx

### Added
- `spring-web` module: Spring Boot autoconfiguration that maps `ValidationException` to HTTP 422
  Problem Details responses, handles `@RequestBody` constructor failures and `@RequestParam`/`@PathVariable`
  converter type mismatches, and supports returning `Validation<T>` directly from controller methods.
- `jakarta-validation` module: Support for Jakarta Validation that allows you to combine have Rules validated with @Valid.  See docs/bean-validation.md for more info.
- added new rules to MapRules/VavrMapRules: min/max size, doesNotContainKey(s), empty, allMatch, noneMatch, anyMatch, ...
- added `anyOf` in core `Validation` and the `DSL`, allowing you to specify at least one Validation must be Valid.
- added `onError` on `Validation`, allowing for easy override of the ErrorMessages if invalid.

### Fixed

- some documentation issues

### Security

## [2.0.1] - 2026-06-25
### Added

- add DSL.notNull(value, field) as a shorthand for validateThat(...).isNotNull()
- add assertInvalid(Runnable) for testing code that throws ValidationException
- StringRules: add doesNotStart/EndWith(IgnoreCase)


## [2.0.0] - 2026-06-24

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


## [1.1.0] - 2026-06-22
### Added

- Support for combining Transformations using `Transformation#andThen(Transformation<T>)` or the static
`Transformation#sequence(Transformation<T>... )` method, allowing you to combine multiple Transformations sequentially.
- Support for defining multiple Transformations in the assertThat/validateThat/after DSL classes.


## [1.0.0] - 2026-06-19
### Added
- Initial release with core, rules, dsl, assertj modules

[Unreleased]: https://github.com/cvanfleteren/fv/compare/v1.1.0...HEAD
[2.0.0]: https://github.com/cvanfleteren/fv/compare/v2.0.0...v2.0.1
[2.0.0]: https://github.com/cvanfleteren/fv/compare/v1.1.0...v2.0.0
[1.1.0]: https://github.com/cvanfleteren/fv/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/cvanfleteren/fv/releases/tag/v1.0.0
