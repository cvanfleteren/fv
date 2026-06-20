# Spring Boot Integration

The `spring-web` module integrates FV with Spring Boot. It:

- automatically maps `ValidationException` to a structured HTTP error response, so you get consistent, machine-readable
  validation errors without any boilerplate in your controllers.
- automatically handles controllers that return `Validation<T>`, resulting in a 'normal' response body for `Valid<T>`  
  and the same problem detail you get as when a `ValidationException` is thrown.

## Getting started

Add the `spring-web` dependency to your project:

```xml
<dependency>
  <groupId>be.iffy.fv</groupId>
  <artifactId>spring-web</artifactId>
  <version>1.1.0</version>
</dependency>
```

That's all. Spring Boot's autoconfiguration picks up the exception and return value handlers automatically.

## What you get

Two things are registered automatically:

**Exception handler** - any `ValidationException` thrown anywhere in the call stack of a Spring MVC request (like in a
controller method, a service, a validated domain constructor, ...) is caught and turned into an
HTTP **422 Unprocessable Entity** response in [Problem Details](https://www.rfc-editor.org/rfc/rfc9457)
format (`application/problem+json`).

**Deserialization unwrapping** - when a self-validating domain object is used directly as a
`@RequestBody` parameter, its constructor runs during Jackson deserialization before the controller
method is entered. The `ValidationException` gets wrapped by Jackson and rethrown by Spring as an
`HttpMessageNotReadableException`. The exception handler unwraps it so you still get the same
422 Problem Details body. Genuinely malformed requests (bad JSON, wrong type, etc.) are unaffected
and still produce a 400.

**Return value handler** - controller methods that return `Validation<T>` are handled natively.
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

Each entry in `errors` has three fields:

| Field        | Description                                                                                                                                                                       |
|--------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `key`        | The raw error key (e.g. `"min.length"`). Stable identifier that is suitable for i18n message lookups on the client.                                                               |
| `path`       | Dot-separated path to the invalid field (e.g. `"order.customer.name"`, or `"items[2].price"` for list elements). Empty string when the error is not attached to a specific field. |
| `parameters` | Constraint values that were part of the rule, if any (e.g. `{"min": 3, "max": 100}`). Useful for building user-facing messages without hardcoding values.                         |

All errors across the entire payload are collected at once — no stopping at the first failure.

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
record User(String name, String email) {
  public User {
    asserting(
      validateThat(name, User::name).is(strings.minLength(3)),
      validateThat(email, User::email).is(strings.notBlank())
    );
  }
}

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
produces the same HTTP 422 Problem Details body shown above — the return value handler converts
it before Spring attempts to serialize the `Validation` wrapper itself.

## Customizing the exception handler

Because the exception handler is registered with `@ConditionalOnMissingBean`, you can replace it entirely
by defining your own bean. The autoconfigured one is then skipped:

```java
@RestControllerAdvice
public class MyValidationExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<MyErrorBody> handle(ValidationException ex) {
    // your own response shape, status code, headers, etc.
  }
}
```

If you only want to change the Problem Details shape while keeping the 422 status and the
deserialization-unwrapping behaviour, extend `ValidationExceptionHandler` and override
`toProblemDetail`. Both the direct-throw path and the deserialization-unwrap path go through it.

```java
@Bean
public ValidationExceptionHandler validationExceptionHandler() {
  return new ValidationExceptionHandler() {
    @Override
    protected ProblemDetail toProblemDetail(ValidationException ex) {
      ProblemDetail pd = ProblemDetail.forStatus(400);
      pd.setTitle("Bad Request");
      // more custom mapping ...
      return pd;
    }
  };
}
```

## WebFlux

This module supports the Spring MVC (servlet) stack only. WebFlux support is not yet available.
