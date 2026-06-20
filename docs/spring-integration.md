# Spring Boot Integration

The `spring-web` module integrates FV with Spring Boot. It automatically maps `ValidationException`
to a structured HTTP error response, so you get consistent, machine-readable validation errors
without any boilerplate in your controllers.

## Getting started

Add the `spring-web` dependency to your project:

```xml
<dependency>
    <groupId>be.iffy.fv</groupId>
    <artifactId>spring-web</artifactId>
    <version>1.1.0</version>
</dependency>
```

That's all. Spring Boot's auto-configuration picks up the exception handler automatically.

## What you get

Any `ValidationException` thrown anywhere in the call stack of a Spring MVC request — in a
controller method, a service, or a validated domain constructor — is caught and turned into an
HTTP **422 Unprocessable Entity** response in [Problem Details](https://www.rfc-editor.org/rfc/rfc9457)
format (`application/problem+json`).

## Response format

```json
{
  "type": "https://github.com/cvanfleteren/fv/problems/validation-failed",
  "title": "Validation Failed",
  "status": 422,
  "detail": "Validation failed with 2 error(s)",
  "errors": [
    { "key": "min.length",       "path": "name",  "parameters": { "min": 3 } },
    { "key": "must.not.be.blank","path": "email", "parameters": {} }
  ]
}
```

Each entry in `errors` has three fields:

| Field | Description |
|-------|-------------|
| `key` | The raw error key (e.g. `"min.length"`). Stable identifier — suitable for i18n message lookups on the client. |
| `path` | Dot-separated path to the invalid field (e.g. `"order.customer.name"`, or `"items[2].price"` for list elements). Empty string when the error is not attached to a specific field. |
| `parameters` | Constraint values that were part of the rule, if any (e.g. `{"min": 3, "max": 100}`). Useful for building user-facing messages without hardcoding values. |

All errors across the entire payload are collected at once — no stopping at the first failure.

## End-to-end example

Given a domain record that validates its own constructor:

```java
import static be.iffy.fv.dsl.DSL.*;

record CreateUserRequest(String name, String email) {}

record User(String name, String email) {
    public User {
       asserting(
           validateThat(name,  User::name ).is(strings.minLength(3)),
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
    { "key": "min.length",        "path": "name",  "parameters": { "min": 3 } },
    { "key": "must.not.be.blank", "path": "email", "parameters": {} }
  ]
}
```

The same approach works when using `validating(...)` in a service layer and calling
`getOrElseThrow()` on the result — any `ValidationException` that propagates up to the
dispatcher is caught by the handler.

## Customizing the handler

Because the handler is registered with `@ConditionalOnMissingBean`, you can replace it entirely
by defining your own bean. The auto-configured one is then skipped:

```java
@RestControllerAdvice
public class MyValidationExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<MyErrorBody> handle(ValidationException ex) {
        // your own response shape, status code, headers, etc.
    }
}
```

If you only want to change the HTTP status code or add headers while keeping the Problem Details
format, extend `ValidationExceptionHandler` and override `handleValidationException`.

## WebFlux

This module supports the Spring MVC (servlet) stack only. WebFlux support is not yet available.
