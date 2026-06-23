# Spring Boot Integration

## Getting started

Add the `spring-web` dependency to your project:

```xml
<dependency>
  <groupId>be.iffy.fv</groupId>
  <artifactId>spring-web</artifactId>
  <version>2.0.0</version>
</dependency>
```

That's all. Spring Boot's autoconfiguration picks up the exception and returns value handlers automatically.

## What you get

The `spring-web` module integrates FV with Spring MVC through autoconfiguration: add the dependency and the following 
four handlers are registered, covering every path where a `ValidationException` can surface in a request:

**Exception handler**: any `ValidationException` thrown anywhere in the call stack of a Spring MVC request (like in a
controller method, a service, a validated domain constructor, ...) is caught and turned into an
HTTP **422 Unprocessable Entity** response in [Problem Details](https://www.rfc-editor.org/rfc/rfc9457)
format (`application/problem+json`).

**Deserialization unwrapping**: when a self-validating type is used as a `@RequestBody` parameter, its constructor runs
during Jackson deserialization. If it throws `ValidationException`, the handler unwraps it and returns the same 
422 Problem Details body. Genuinely malformed requests (bad JSON, wrong type, etc.) are unaffected and still produce a 400.

**Converter unwrapping**: when a `@RequestParam` or `@PathVariable` uses a custom Spring
[`Converter`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/convert/converter/Converter.html)
that throws `ValidationException`, the handler unwraps it and returns the same 422 Problem Details body. See [Using validated types as request parameters](#using-validated-types-as-request-parameters) for setup.

**Return value handler**: controller methods that return `Validation<T>` are handled natively.
A `Valid<T>` result serializes `T` as the normal response body (as if the method had declared `T`
directly). An `Invalid` result produces the same HTTP 422 Problem Details body as the exception
path, without you having to call `.getOrElseThrow()` explicitly. This mirrors how Spring MVC
handles Controllers that return `Optional<T>`.

## Response format

```json
{
  "type": "https://github.com/cvanfleteren/fv/problems/validation-failed",
  "title": "Validation Failed",
  "status": 422,
  "detail": "Validation failed with 2 error(s)",
  "errors": [
    {
      "key": "must.have.min.length",
      "path": "name",
      "parameters": {
        "min": 3
      }
    },
    {
      "key": "must.not.be.blank",
      "path": "email",
      "parameters": {}
    }
  ]
}
```

Each entry in `errors` has three fields:

| Field        | Description                                                                                                                                                                       |
|--------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `key`        | The raw error key (e.g. `"min.length"`). Stable identifier that is suitable for i18n message lookups on the client.                                                               |
| `path`       | Dot-separated path to the invalid field (e.g. `"order.customer.name"`, or `"items[2].price"` for list elements). Empty string when the error is not attached to a specific field. |
| `parameters` | Constraint values that were part of the rule, if any (e.g. `{"min": 3, "max": 100}`). Useful for building user-facing messages without hardcoding values.                         |

All errors across the entire payload are accumulated, as is the default behavior for FV.

## End-to-end example

Given a domain record that validates its own constructor:

```java
import static be.iffy.fv.dsl.DSL.*;

record CreateUserRequest(String name, String email) {
}

record User(String name, String email) {
  public User {
    asserting(
      validateThat(name, User::name).is(strings.minLength(3)),
      validateThat(email, User::email).is(strings.notBlank())
    );
  }
}
```

A controller that constructs the domain object from the request body:

```java
@RestController
@RequestMapping("/users")
class UserController {

  @PostMapping
  public User create(@RequestBody CreateUserRequest req) {
    return new User(req.name(), req.email());  // throws ValidationException if invalid
  }
}
```

`POST /users` with body `{"name": "Al", "email": ""}` produces:

```json
{
  "type": "https://github.com/cvanfleteren/fv/problems/validation-failed",
  "title": "Validation Failed",
  "status": 422,
  "detail": "Validation failed with 2 error(s)",
  "errors": [
    {
      "key": "min.length",
      "path": "name",
      "parameters": {
        "min": 3
      }
    },
    {
      "key": "must.not.be.blank",
      "path": "email",
      "parameters": {}
    }
  ]
}
```

The same approach works when using `validating(...)` in a service layer and calling
`getOrElseThrow()` on the result — any `ValidationException` that propagates up to the
dispatcher is caught by the handler.

If the domain type validates its own constructor you can also use it directly as the `@RequestBody`
type, removing the separate DTO entirely:

```java
@RestController
@RequestMapping("/users")
class UserController {

  @PostMapping
  public User create(@RequestBody User user) {  // constructor runs during deserialization
    return userRepository.save(user);
  }
}
```

Jackson calls the constructor while deserializing the request body. If it throws, the handler
unwraps the exception and returns the same 422 Problem Details response shown above.

Alternatively, you can return `Validation<T>` directly from the controller without calling
`getOrElseThrow()`:

```java
@RestController
@RequestMapping("/users")
class UserController {

  @PostMapping
  public Validation<User> create(@RequestBody CreateUserRequest req) {
    return validating(
      validateThat(req.name(), "name").is(strings.minLength(3)),
      validateThat(req.email(), "email").is(strings.notBlank())
    ).map(User::new);
  }
}
```

A `Valid<User>` response serializes the `User` as JSON with HTTP 200. An `Invalid` response
produces the same HTTP 422 Problem Details body shown above. The return value handler converts
it before Spring attempts to serialize the `Validation` wrapper itself.

## Using validated types as request parameters

To use a validated type as a `@RequestParam` or `@PathVariable`, Spring needs a registered
`Converter<String, YourType>`. Implement the interface and annotate the converter with `@Component`. 
Spring Boot picks it up automatically:

```java
@Component
class ValidatedIdConverter implements Converter<String, ValidatedId> {
    @Override
    public ValidatedId convert(String source) {
        return new ValidatedId(source);  // throws ValidationException if invalid
    }
}
```

The `ValidatedId` constructor throws `ValidationException` when the value is invalid. Spring catches
that and rethrows it as a `TypeMismatchException`, which the exception handler unwraps into the
same 422 Problem Details response.

Your controller declares the parameter as the validated type directly; Spring resolves the converter
automatically:

```java
@GetMapping("/things/{id}")
public Thing get(@PathVariable ValidatedId id) { ... }

@GetMapping("/things")
public List<Thing> search(@RequestParam ValidatedId id) { ... }
```

Passing an invalid value (e.g. `GET /things/x`) produces the same 422 Problem Details body as any
other validation failure. To opt out of this unwrapping, set `fv.spring.handle-type-mismatch=false`.

## Configuration

| Property | Default | Description |
|---|---|---|
| `fv.spring.status-code` | `422` | HTTP status code returned for all validation failures. |
| `fv.spring.handle-type-mismatch` | `true` | When `false`, `@RequestParam` and `@PathVariable` converter failures that wrap a `ValidationException` fall through to Spring's default 400 handling instead of producing a Problem Details body. |

```properties
# Use 400 Bad Request instead of 422 for validation errors
fv.spring.status-code=400

# Opt out of unwrapping ValidationException from converter type mismatches, so ValidationExceptions that happen with
# @PathVariable or @RequestParam will revert back to default Spring behavior. @RequestBody that throw will still 
# result in a ProblemDetail with a statusCode of `fv.spring.status-code`.

fv.spring.handle-type-mismatch=false
```

## Customizing the exception handler

There are two levels of customization, from lightest to heaviest:

**1. Change the status code only** — use `fv.spring.status-code`. No Java needed.

**2. Change the response body, headers, or status code** — provide a `ValidationResponseFactory`
bean. It is used by all four error paths (thrown exceptions, `@RequestBody`, `@RequestParam`/`@PathVariable`, and `Validation.Invalid` return values):

```java
@Bean
ValidationResponseFactory validationResponseFactory(FvSpringWebProperties properties) {
  return (ex, headers, request) -> ResponseEntity
      .status(properties.statusCode())
      .body(Map.of("violations", ex.errors().map(e -> e.key()).toJavaList()));
}
```

Providing this bean suppresses the autoconfigured `DefaultValidationResponseFactory`.

## WebFlux

This module supports the Spring MVC (servlet) stack only. WebFlux support is not yet available.
