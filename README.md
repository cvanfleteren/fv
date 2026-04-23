[![Maven Central](https://img.shields.io/maven-central/v/net.vanfleteren.fv/fv-parent.svg?label=Maven%20Central)](https://central.sonatype.com/search?q=net.vanfleteren.fv)

FV is a lightweight, type-safe, and functional validation library for Java 21+. It is designed with a focus on immutability, side-effect-free functions, and seamless integration with [Vavr](https://www.vavr.io/).

The library encourages **"Validation at the Edge"**, ensuring that your domain objects (like Java Records) are always in a valid state by validating them during construction.
But it can also be used to validate business rules later on.

---

### Maven Coordinates

To use FV in your project, add the following dependencies to your `pom.xml`:

```xml
<dependency>
    <groupId>net.vanfleteren.fv</groupId>
    <artifactId>fv-core</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
<!-- Optional: predefined rules for common types -->
<dependency>
    <groupId>net.vanfleteren.fv</groupId>
    <artifactId>fv-rules</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

---

### Core vs. Rules Module

*   **`fv-core`**: The heartbeat of the library. It contains the `Validation` functor, the `Rule` interface, and the fluent `API` entry point. It has minimal dependencies (primarily Vavr).
*   **`fv-rules`**: A collection of reusable `Rule` instances for common Java types (Strings, Integers, Collections, BigDecimals, etc.), allowing you to compose complex validations quickly.

---

### Key Concepts

#### `Rule<T>`
A functional interface representing a check on a value of type `T`. Rules can be easily composed:
```java
import static net.vanfleteren.fv.rules.StringRules.strings;

Rule<String> myRule = strings().minLength(3)
    .and(strings().contains("@").or(strings().contains("|")));
```

#### `Validation<T>`
An applicative functor that represents either a **Valid** value or an **Invalid** result containing one or more `ErrorMessage` objects. Unlike a standard `Either`, `Validation` accumulates *all* errors instead of stopping at the first one.

fv-core doesn't have rules by default, but the fv-rules modules has ships lots of pre-made, reusable rules for common types.
You can also easli make your own rules using a construct like

```java
Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "string.cannot.be.empty");
```

---

### Fluent DSL Examples

The `net.vanfleteren.fv.dsl.DSL` class provides a readable way to define validations.

#### 1. Constructor Validation
Use `assertAllValid` inside a Java Record constructor to ensure that only valid objects can ever be instantiated.

```java
import static net.vanfleteren.fv.dsl.DSL.*;

public record User(String username, int age) {
    public User {
        var values = assertAllValid(
            validateThat(username, "username").map(String::trim).is(StringRules.minLength(3)),
            validateThat(age, "age").is(IntegerRules.min(18))
        );
        this.username = values._1(); // Use the trimmed value from the validation chain
    }
}
```
If validation fails, a `ValidationException` is thrown, containing all accumulated errors.

#### 2. Validation without Exceptions
If you prefer a pure functional approach without throwing exceptions:

```java
Validation<User> userV = Validation.mapN(
    validateThat(dto.name(), "name").is(strings().minLength(3)),
    validateThat(dto.age(), "age").is(ints().min(18)),
    User::new
);

if (userV.isValid()) {
    User user = userV.get();
} else {
    List<ErrorMessage> errors = userV.errors();
}
```

#### 3. Error Path Nesting
Errors automatically track their location, which is useful for nested object structures:

```java
Validation<Address> addressV = Validation.from(() -> new Address(dto.street()))
    .at("address"); 
// If it fails, error messages will look like "address.street.cannot.be.blank"
```
Tip: you can use the lombok `FieldNameConstants` annotation to have Lombok generate String constants for field names for you, so you can have 
refactoring friendly and typesafe error messages when using `at`.

---

### Functional Style & Vavr
FV is built from the ground up to be functional:
*   **No Nulls**: `null` values are treated as invalid by default in the DSL.
*   **Immutability**: All core types (`Validation`, `Rule`, `ErrorMessage`) are immutable.
*   **Vavr Powered**: Uses Vavr's `List`, `Map`, `Tuple`, and `Option` for a robust functional experience.

---

### Testing Support
The project includes **AssertJ** integrations to make writing tests for your validations clean and expressive:

```java
import static net.vanfleteren.fv.assertj.ValidationAssert.assertThatValidation;

@Test
void validateUser_whenAgeIsTooLow_shouldHaveError() {
    Validation<User> result = validateUser(invalidDto);

    assertThatValidation(result)
        .isInvalid()
        .hasErrorMessages("age.too.young");
}
```

---

### License
This project is licensed under the Apache 2.0 License.