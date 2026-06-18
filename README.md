[![Maven Central](https://img.shields.io/maven-central/v/be.iffy.fv/fv-parent.svg?label=Maven%20Central)](https://central.sonatype.com/search?q=be.iffy.fv)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

# FV — Functional Validation

FV is a lightweight, type-safe, functional library for validating and transforming data in Java 21+. It lets you both check that a value meets your rules and turn it into something else (e.g. parsing a String into a LocalDate), with a focus on immutability, side-effect-free functions, and seamless integration with [Vavr](https://www.vavr.io/).

The library encourages **"Validation at the Edge"**: your domain objects (like Java Records) are always in a valid state because they validate and convert their inputs during construction. 
It's just as useful for validating and mapping incoming data like DTOs or request payloads into trusted domain objects, or for checking business rules later on, independently of object construction.

## Quick start

```java
import static be.iffy.fv.dsl.DSL.*;

Rule<String> validUsername = strings.minLength(3).and(strings.alphaNumeric());

Validation<String> result = validUsername.apply("bob");
result.isValid(); // true

Validation<String> failure = validUsername.apply("!!");
failure.isInvalid(); // true
failure.errors();    // List<ErrorMessage>: ["must.have.min.length", "must.be.alphanumeric"]
```

## Table of contents

- [Getting the project](#getting-the-project)
- [What is it for?](#what-is-it-for)
- [Core concepts](#core-concepts)
- [Constructor validation examples](#constructor-validation-examples)
- [Mapping and validating a DTO into a Domain object with `MappingRule`](#mapping-and-validating-a-dto-into-a-domain-object-with-mappingrule)
- [Nested validation paths](#nested-validation-paths)
- [Available rules](#available-rules)
- [Inspecting errors](#inspecting-errors)
- [Wrapping other types with `Validation.from()`](#wrapping-other-types-with-validationfrom)
- [Testing with `assertThatValidation`](#testing-with-assertthatvalidation)
- [More recipes](#more-recipes)
- [License](#license)

---

## Getting the project

FV is published on **Maven Central** under the `be.iffy.fv` group id. The easiest way to get started is to add
`dsl` (it pulls in `core` and `rules` transitively, giving you the fluent `validateThat`/`assertThat`/`asserting`
entry points plus the full set of ready-made rules):

```xml
<dependency>
    <groupId>be.iffy.fv</groupId>
    <artifactId>dsl</artifactId>
    <version>0.9.2</version>
</dependency>
```

If you only need the core `Validation`/`Rule`/`MappingRule` types without the DSL or any rules, depend on `core`
directly instead, optionally adding `rules` for the prebuilt rule library:

```xml
<dependency>
    <groupId>be.iffy.fv</groupId>
    <artifactId>core</artifactId>
    <version>0.9.2</version>
</dependency>
<!-- Optional: a large collection of predefined rules for common types -->
<dependency>
    <groupId>be.iffy.fv</groupId>
    <artifactId>rules</artifactId>
    <version>0.9.2</version>
</dependency>
```

The project is split into several modules, so you only pull in what you need:

| Module    | Purpose                                                                                  |
|-----------|-------------------------------------------------------------------------------------------|
| `core`    | Core datatypes: `Validation`, `Rule`, `MappingRule`, `ErrorMessage`. Minimal dependencies (Vavr only). |
| `rules`   | A large set of ready-made `Rule`/`MappingRule` instances for Strings, numbers, collections, dates, `Optional`/`Option`/`Either`, etc. |
| `dsl`     | The fluent `DSL` class (`validateThat`, `assertThat`, `asserting`, `validating`, ...) for readable validation code. Depends on `core` and `rules`. |
| `assertj` | AssertJ integration (`assertThatValidation(...)`) for clean test assertions.             |

---

## What is it for?

Use FV when you want to:

* Guarantee that a domain object can never exist in an invalid state (validate inside the constructor).
* Validate and parse incoming data (e.g. DTOs, request bodies, CSV rows) into typed, validated domain objects,
  collecting **all** errors instead of stopping at the first one.
* Compose small, reusable, named validation rules instead of scattering `if`-checks across the codebase.

---

## Core concepts

* **`Rule<T>`** — a check on a value of type `T`. If valid, it returns the exact same instance; if invalid, it
  returns an `Invalid` result with one or more `ErrorMessage`s. Think of it as a `Predicate<T>` that explains
  itself.
* **`MappingRule<T, R>`** — validates **and transforms** a value, e.g. parsing a `String` into a `LocalDate`. If
  parsing or validation fails, you get an `Invalid` result; otherwise a `Valid<R>` with the transformed value.
* **`Validation<T>`** — an applicative functor representing either `Valid<T>` or `Invalid` (one or more errors).
  Unlike `Either`, it **accumulates** errors when you combine several validations together.

`core` ships no rules by default — `rules` provides the reusable ones, but you can always make your own:

```java
Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "string.cannot.be.empty");
```

---

## Constructor validation examples

All DSL examples assume:

```java
import static be.iffy.fv.dsl.DSL.*;
```

### 1. Transform, then validate a single field

`assertThat(value, name).after(transformation).is(rule)` normalizes a value (e.g. trims it) before validating it,
and returns the **transformed** value — or throws a `ValidationException` if it's invalid.

```java
public record Username(String value) {
    public Username {
        // trims the input, then checks the trimmed value is at least 3 characters
        value = assertThat(value, "value").after(stringOps.trim()).is(strings.minLength(3));
    }
}

new Username("  bob  "); // value == "bob"
new Username(" a ");     // throws ValidationException: value.must.have.min.length
```

### 2. Validating multiple fields with error accumulation

`asserting(...)` takes several validations, runs all of them, and either returns a `Tuple` of the (possibly
transformed) values, or throws a `ValidationException` containing **all** the accumulated errors at once.
`PropertySelector` (a method reference like `Person::name`) is used instead of a `String` so renames stay
refactor-safe.

```java
public record Person(String name, int age) {
    public Person {
        var values = asserting(
                validateThat(name, Person::name).is(strings.minLength(3)),
                validateThat(age, Person::age).is(ints.atLeast(18))
        );
        name = values._1;
        age = values._2;
    }
}

new Person("Al", 16);
// throws ValidationException with BOTH:
//   name.must.have.min.length
//   age.must.be.at.least
```

---

## Mapping and validating a DTO into a Domain object with `MappingRule`

A common use case is turning a "dumb" DTO (e.g. all-`String` fields from a form or JSON payload) into a validated
domain object. Because the `age` field needs to change type (`String` → `int`), this is a job for a
`MappingRule`: `strings.asInteger()` parses the string, and `.then(ints.positive())` validates the parsed result.

```java
record PersonDto(String name, String age) {}
record Person(String name, int age) {}

MappingRule<String, Integer> validAge = strings.asInteger().then(ints.positive());

Validation<Person> toPerson(PersonDto dto) {
    return validating(
            validateThat(dto.name(), "name").is(strings.minLength(3)),
            validateThat(dto.age(), "age").is(validAge)
    ).map(Person::new);
}

toPerson(new PersonDto("Alice", "34"));
// Valid(Person("Alice", 34))

toPerson(new PersonDto("Al", "-5"));
// Invalid([name.must.have.min.length, age.must.be.positive])
```

`validating(...).map(...)` never throws: it returns a `Validation<Person>` you can inspect with `isValid()` /
`errors()`. If you'd rather fail fast with an exception (e.g. inside a constructor), use `asserting(...)` instead
of `validating(...).map(...)`, exactly as in the constructor examples above.

---

## Nested validation paths

When you validate an object that itself contains other validated objects (or a list of them), the error paths
nest automatically — each level of `validateThat(..., "name")` (or a `PropertySelector`) prefixes the paths
produced underneath it, and list elements get an `[index]` segment. This makes it easy to pinpoint exactly where,
in a deeply nested payload, something went wrong.

```java
record Address(String street, String city) {}
record Customer(String name, Address address) {}
record Order(Customer customer, List<BigDecimal> lineAmounts) {}

Validation<Address> validateAddress(Address address) {
    return validating(
            validateThat(address.street(), "street").is(strings.notBlank()),
            validateThat(address.city(), "city").is(strings.notBlank())
    ).map(Address::new);
}

Validation<Customer> validateCustomer(Customer customer) {
    return validating(
            validateThat(customer.name(), "name").is(strings.notBlank()),
            validateThat(customer.address(), "address").is(this::validateAddress)
    ).map(Customer::new);
}

Validation<Order> validateOrder(Order order) {
    return validating(
            validateThat(order.customer(), "customer").is(this::validateCustomer),
            validateThatList(order.lineAmounts(), "lineAmounts").eachIs(bigDecimals.positive()).validate()
    ).map(Order::new);
}

// Order(Customer("", Address("", "Brussels")), List.of(new BigDecimal("-5")))
// -> Invalid with paths:
//      customer.name.must.not.be.blank
//      customer.address.street.must.not.be.blank
//      lineAmounts[0].must.be.positive
```

For a real-world example with several levels of nesting (records inside records, `Optional` fields, and lists of
transactions), see
[`QueueMessage`](testing/src/test/java/be/iffy/fv/test/examples/QueueMessage.java) and its test,
[`QueueMessageMapperTest`](testing/src/test/java/be/iffy/fv/test/examples/QueueMessageMapperTest.java), which
produces paths like `debtor.address.street.must.not.be.blank` and
`transactions[0].amount.value.must.be.positive`.

---

## Available rules

`rules` ships these rule collections (all accessible as static fields via `import static be.iffy.fv.dsl.DSL.*;`,
e.g. `strings`, `ints`, `lists`, ...):

### Strings (`strings`)
Parsing/mapping: `asInteger`, `asLong`, `asDouble`, `asFloat`, `asBigInteger`, `asBigDecimal`, `asBoolean`,
`asUUID`, `asURL`, `asURI`, `asLocalDate`, `asLocalDateTime`, `asInstant`, `asEnum` / `canBeEnum`, `substring`,
`take` / `drop` / `takeRight` / `dropRight`, `splitAt`.

Checks: `notEmpty`, `notBlank`, `trimmed`, `singleLine`, `noWhitespace`, `uppercase` / `lowercase`, `minLength` /
`maxLength` / `lengthBetween` / `length`, `startsWith` / `endsWith` (+ `IgnoreCase` variants), `contains` /
`doesNotContain` / `containsPattern` (+ `IgnoreCase` variants), `isIn` / `notIn`, `matches`, `equalsIgnoreCase` /
`notEqualsIgnoreCase`, `alpha`, `alphaNumeric` / `alphaNumericUnicode`, `onlyDigits` / `onlyUnicodeDigits`,
`hexadecimal`, `base64` / `base64UrlSafe`, `looksLikeEmailAddress`.

### Numbers (`ints`, `longs`, `doubles`, `floats`, `bigIntegers`, `bigDecimals`)
Sign checks: `positive`, `nonNegative`, `negative`, `nonPositive`, `zero`, `nonZero`.

Range checks (shared by all comparable rules, e.g. dates too): `between`, `betweenExclusive`, `greaterThan`,
`atLeast`, `lessThan`, `atMost`.

### Booleans (`booleans`)
`isTrue`, `isFalse`, `notNull`.

### Objects (`objects`)
`notNull`, `asString`, `isInstanceOf`, `construct(...)` (build an object via a constructor/factory function, with
error handling).

### Collections (`lists`, `sets`, `maps`, and Vavr equivalents)
`notEmpty` / `empty`, `minSize` / `maxSize` / `sizeEquals` / `sizeBetween`, `noNullElements`, `allMatch` /
`allMatchRule`, `noneMatch` / `noneMatchRule`, `anyMatch`, `contains` / `containsAll` / `containsAnyOf`,
`uniqueBy` / `allUnique`, `validateValuesWith`.

### Optional / Option / Either (`optionals`, `options`, `eithers`)
`required`, `matches`, `contains`, `notEmpty` / `empty` (for `Optional`/`Option`); `isRight` / `isLeft`,
`validateLeftWith` / `validateRightWith` (for `Either`).

### Time (`localDates`, `localDateTimes`, `localTimes`, `zonedDateTimes`, `instants`, `yearMonths`, `durations`)
`isBefore`, `isAfter`, `isPast`, `isFuture`, `isToday` (where applicable to the type), plus the shared comparable
range checks listed under Numbers above. `isPast`/`isFuture`/`isToday` are evaluated against a `java.time.Clock`
(defaulting to the system clock), and each time-rules class exposes a factory taking a custom `Clock` (e.g.
`localDates(myClock)`) so you can test time-dependent validations deterministically.

---

## Inspecting errors

Every `Invalid` result carries a `List<ErrorMessage>`. Each `ErrorMessage` has an `errorKey`, a `path`, and a map
of `parameters` you can use to build a localized message. For quick debugging, use `formatted()`:

```java
Validation<String> result = strings.lengthBetween(3, 10).apply("hi");

if (result.isInvalid()) {
    System.out.println(result.errors().head().formatted());
    // must.have.length.between:{min:3,max:10}
}
```

---

## Wrapping other types with `Validation.from()`

When integrating with code that uses `Try`, `Optional`, `Either`, or constructors that throw,
`Validation.from()` gives you a `ValidationFactory` to bridge those types into a `Validation`.

The most common case — calling a constructor or factory that throws `ValidationException` on bad input — is
available directly as `Validation.catching(supplier)`:

```java
// Validated domain object whose constructor throws ValidationException on bad input
Validation<Username> u = Validation.catching(() -> new Username(rawInput));
```

For other cases, `Validation.from()` covers arbitrary-exception suppliers, `Try`, `Optional`, `Option`, and `Either`:

```java
// Wraps any exception as Invalid with the given error key
Validation<URL> url = Validation.from().catchingAll(() -> new URL(input), "invalid.url");

// From a Vavr Try you already have
Validation<Integer> n = Validation.from()._try(Try.of(() -> Integer.parseInt(input)), "invalid.number");
```

See the [Exception Interop](faq.md#exception-interop) section of the FAQ for full details
(`Option`, `Either`, `catching` vs `catchingAll` vs `_try`, and more).

---

## Testing with `assertThatValidation`

The `assertj` module adds AssertJ-style assertions for `Validation<T>`, so your tests read naturally and you don't
have to manually unwrap `Valid`/`Invalid` results.

```java
import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;

Validation<Person> result = toPerson(new PersonDto("Alice", "34"));

assertThatValidation(result)
        .isValid()
        .satisfies(person -> assertThat(person.age()).isEqualTo(34));

Validation<Person> invalidResult = toPerson(new PersonDto("Al", "-5"));

assertThatValidation(invalidResult)
        .isInvalid()
        .hasErrorMessages("name.must.have.min.length", "age.must.be.positive");
```

`isInvalid()` also gives you `errorKeys()`, `errorMessages()`, `formattedMessages()`, and `hasErrorCount(...)` for
more targeted assertions.

---

## More recipes

This README only scratches the surface. See **[faq.md](faq.md)** for recipes covering rule composition
(`and`, `then`, `or`, `xor`, `fallback`), null-safety, validating `Optional`/`Option`/lists/maps, enums,
cross-field validation, exception handling (`Validation.from()`, `mapCatching`, `flatMapCatchingAll`), and more.

---

## License

This project is licensed under the Apache 2.0 License.
