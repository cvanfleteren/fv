[![Maven Central](https://img.shields.io/maven-central/v/be.iffy.fv/fv-parent.svg?label=Maven%20Central)](https://central.sonatype.com/search?q=be.iffy.fv)

FV is a lightweight, type-safe, and functional validation library for Java 21+. It is designed with a focus on immutability, side-effect-free functions, and seamless integration with [Vavr](https://www.vavr.io/).

The library encourages **"Validation at the Edge"**, ensuring that your domain objects (like Java Records) are always in a valid state by validating them during construction.
But it can also be used to validate business rules later on.

---

### Maven Coordinates

To use FV in your project, add the following dependencies to your `pom.xml`:

```xml
<dependency>
    <groupId>be.iffy.fv</groupId>
    <artifactId>fv-core</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
<!-- Optional but highly recommended: predefined rules for common types -->
<dependency>
    <groupId>be.iffy.fv</groupId>
    <artifactId>fv-rules</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

---

# Validation

A small Java 21 validation library for writing reusable, composable domain rules.

The library revolves around three concepts:

- `Rule<T>`: a reusable validation rule for values of type `T`.
- `Validation<T>`: a result that is either valid and contains a value, or invalid and contains one or more `ErrorMessage`s.
- `ValidationException`: the exception thrown by the assertion-style API when validation fails.

Most code should statically import the fluent API:

```java
import static be.liantis.vetstrak.ddd.validation.API.*;
import static be.liantis.vetstrak.ddd.validation.Rules.*;
```

## Installation

This project is a Maven library:

```xml
<dependency>
    <groupId>be.liantis.vetstrak.ddd</groupId>
    <artifactId>validation</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

It requires Java 21.

## Quick Start

Use `assertThat(...).is(...)` when invalid input should throw a `ValidationException` immediately:

```java
String name = assertThat("Alice", "name")
        .is(strings().notBlank);
```

Use `validateThat(...).is(...)` when you want a `Validation<T>` result instead of an exception:

```java
Validation<String> result = validateThat("", "name")
        .is(strings().notBlank);

boolean valid = result.valid(); // false
List<ErrorMessage> errors = result.errors();
```

The error message includes the field name when one is provided:

```java
Validation<String> result = validateThat("", "name")
        .is(strings().notBlank);

String message = result.errors().head().getMessage();
// "name.cannot.be.blank"
```

## Throwing Validation

`assertThat` returns the original value when validation succeeds:

```java
String reference = assertThat("INV-2026-0001", "reference")
        .is(strings().startsWith("INV-"));
```

When validation fails, it throws `ValidationException`:

```java
try {
    assertThat("", "firstName").is(strings().notBlank);
} catch (ValidationException e) {
    e.getMessage(); // "[firstName.cannot.be.blank]"
    e.getErrors();  // Vavr List<ErrorMessage>
}
```

You can assert non-null values directly:

```java
Customer customer = assertThat(possibleCustomer, "customer")
        .isNotNull();
```

## Non-Throwing Validation

`validateThat` returns `Validation<T>`:

```java
Validation<Integer> age = validateThat(17, "age")
        .is(ints().isGreaterThanOrEqualTo(18));

if (!age.valid()) {
    age.errors().forEach(error -> log.warn(error.getMessage()));
}
```

Retrieve a value with a fallback:

```java
int safeAge = validateThat(ageInput, "age")
        .is(ints().isBetweenInclusive(0, 120))
        .getOrElse(() -> 0);
```

Throw only at the boundary:

```java
Integer age = validateThat(ageInput, "age")
        .is(ints().isBetweenInclusive(0, 120))
        .getOrElseThrow();
```

Convert to other types:

```java
Optional<String> optional = validateThat(email, "email")
        .is(strings().contains("@"))
        .toOptional();

Try<String> tried = validateThat(email, "email")
        .is(strings().contains("@"))
        .toTry();

Option<String> option = validateThat(email, "email")
        .is(strings().contains("@"))
        .toOption();
```

## Built-In Rules

### Strings

```java
assertThat("Alice", "name").is(strings().notBlank);
assertThat("abc", "code").is(strings().notEmpty);
assertThat("12345", "postalCode").is(strings().numeric);
assertThat("BE0123456789", "vatNumber").is(strings().startsWith("BE"));
assertThat("invoice.pdf", "fileName").is(strings().endsWith(".pdf"));
assertThat("ABC123", "code").is(strings().alphaNumeric);
assertThat("abc@example.com", "email").is(strings().contains("@"));
assertThat("abc", "code").is(strings().length(3));
assertThat("Alice", "name").is(strings().minLength(2));
assertThat("Alice", "name").is(strings().maxLength(50));
assertThat("Alice", "name").is(strings().isBetween(2, 50));
assertThat("ACTIVE", "status").is(strings().equalTo("ACTIVE"));
assertThat("active", "status").is(strings().equalToIgnoreCase("ACTIVE"));
assertThat("draft", "status").is(strings().notEqualTo("deleted"));
```

Pattern matching:

```java
Pattern ibanPattern = Pattern.compile("[A-Z]{2}[0-9]{2}[A-Z0-9]+");

Validation<String> iban = validateThat("BE68539007547034", "iban")
        .is(strings().matches(ibanPattern));
```

### Integers

```java
assertThat(10, "quantity").is(ints().isPositive);
assertThat(1, "quantity").is(ints().isStrictlyPositive);
assertThat(-1, "balance").is(ints().isNegative);
assertThat(-5, "delta").is(ints().isStrictlyNegative);
assertThat(5, "rating").is(ints().isEqualTo(5));
assertThat(18, "age").is(ints().isGreaterThanOrEqualTo(18));
assertThat(17, "discount").is(ints().isLessThan(100));
assertThat(50, "percentage").is(ints().isBetweenInclusive(0, 100));
assertThat(10, "amount").is(ints().min(1));
assertThat(10, "amount").is(ints().max(99));
```

### Longs

```java
assertThat(42L, "id").is(longs().isStrictlyPositive);
assertThat(100L, "sequence").is(longs().isGreaterThan(0L));
assertThat(50L, "score").is(longs().isBetweenInclusive(0L, 100L));
```

### BigDecimals

```java
BigDecimal amount = new BigDecimal("19.99");

assertThat(amount, "amount").is(bigDecimals().isStrictlyPositive);
assertThat(amount, "amount").is(bigDecimals().isGreaterThan(BigDecimal.ZERO));
assertThat(amount, "amount").is(bigDecimals().isBetweenInclusive(
        BigDecimal.ZERO,
        new BigDecimal("100.00")
));
```

### Objects

```java
assertThat(customer, "customer").is(objects().notNull());
assertThat("EUR", "currency").is(objects().oneOf("EUR", "USD", "GBP"));
assertThat("ARCHIVED", "status").is(objects().notOneOf("DELETED", "BLOCKED"));
assertThat("BE", "country").is(objects().isEqualTo("BE"));
```

Validate a conversion:

```java
Validation<Integer> parsed = objects().canConvertTo("123", Integer::parseInt);
```

### Optionals

```java
Optional<String> phoneNumber = Optional.of("+32123456789");

assertThat(phoneNumber, "phoneNumber")
        .is(strings().startsWith("+32"));
```

An empty optional is valid when using a normal rule through `assertThat(Optional<T>)`:

```java
Optional<String> phoneNumber = Optional.empty();

assertThat(phoneNumber, "phoneNumber")
        .is(strings().startsWith("+32")); // valid: no value to check
```

`assertThat(Optional<T>).is(rule)` only validates present values. Use `objects().required()` directly when the optional itself must be present:

```java
Validation<Optional<String>> required = objects().<String>required()
        .test(Optional.of("alice@example.com"))
        .atField("email");
```

Use optional-specific rules directly against the optional value:

```java
Validation<Optional<?>> result = optionals().isEmpty()
        .test(Optional.empty())
        .atField("middleName");

Validation<Optional<String>> notEmpty = optionals().<String>isNotEmpty()
        .test(Optional.of("Alice"))
        .atField("firstName");
```

### Collections And Lists

```java
List<String> tags = java.util.List.of("urgent", "customer");

assertThat(tags, "tags").is(collections().nonEmptyList());
assertThat(tags, "tags").is(strings().notBlank.list());
```

The `Rule<T>.list()` adapter validates every element and reports failing indexes:

```java
Validation<java.util.List<String>> result = validateThat(java.util.List.of("A", "", "C"), "codes")
        .is(strings().notBlank.list());

result.errors().map(ErrorMessage::getMessage);
// contains "codes[1].cannot.be.blank"
```

## Composing Rules

Use `and` to stop at the first failing rule:

```java
Rule<String> customerCode = strings().notBlank
        .and(strings().startsWith("CUS-"))
        .and(strings().maxLength(20));

assertThat("CUS-123", "customerCode").is(customerCode);
```

Use `Rules.all(...)` to collect all rule failures:

```java
Rule<String> password = all(
        strings().minLength(12),
        strings().contains("@"),
        strings().contains("!")
);

Validation<String> result = validateThat("short", "password").is(password);
// may contain multiple errors
```

Use `or` or `either` when one of two rules may pass:

```java
Rule<String> startsWithCustomerOrSupplier = either(
        strings().startsWith("CUS-"),
        strings().startsWith("SUP-")
);

assertThat("SUP-123", "relationCode").is(startsWithCustomerOrSupplier);
```

Use `Rules.atLeastOne(...)` for more than two alternatives:

```java
Rule<String> acceptedReference = atLeastOne(
        strings().startsWith("INV-"),
        strings().startsWith("CN-"),
        strings().startsWith("PO-")
);

assertThat("PO-2026-0001", "reference").is(acceptedReference);
```

Use `Rules.when(...)` for conditional validation:

```java
record Registration(String country, String nationalNumber) {}

Rule<Registration> belgianNationalNumberRequired = when(
        registration -> "BE".equals(registration.country()),
        given(Registration::nationalNumber).is(strings().notBlank)
);

assertThat(new Registration("BE", "12345678901")).is(belgianNationalNumberRequired);
```

Override a rule's error message with `describe`:

```java
Rule<String> businessEmail = strings().contains("@liantis.be")
        .describe("must.be.a.liantis.email.address");

assertThat("alice@example.com", "email").is(businessEmail);
```

## Validating Records And Domain Objects

Use `given(...)` to apply a rule to a property of a larger object. The property name is derived from the method reference.

```java
record Customer(String name, String email, int age) {}

Rule<Customer> validCustomer = all(
        given(Customer::name).is(strings().notBlank),
        given(Customer::email).is(strings().contains("@")),
        given(Customer::age).is(ints().isGreaterThanOrEqualTo(18))
);

Validation<Customer> result = validateThat(new Customer("", "wrong", 17))
        .is(validCustomer);

result.errors().map(ErrorMessage::getMessage);
// contains:
// "name.cannot.be.blank"
// "email.must.contain.@"
// "age.must.be.greater.than.or.equal.to.18"
```

You can also pass a method reference as the field name when validating a single property:

```java
record Customer(String email) {}

Customer customer = new Customer("wrong");

Validation<String> result = validateThat(customer.email(), Customer::email)
        .is(strings().contains("@"));

result.errors().head().getMessage();
// "email.must.contain.@"
```

## Collecting Multiple Field Validations

Use `assertAll(...)` when you want to validate several values and throw once with all errors:

```java
record CreateCustomerCommand(String name, String email, int age) {}

void handle(CreateCustomerCommand command) {
    assertAll(
            validateThat(command.name(), "name").is(strings().notBlank),
            validateThat(command.email(), "email").is(strings().contains("@")),
            validateThat(command.age(), "age").is(ints().isGreaterThanOrEqualTo(18))
    );

    // all values are valid here
}
```

Build a domain object only when every value is valid:

```java
record Customer(String name, String email, int age) {}

Validation<Customer> customer = Validation.map(
        validateThat(command.name(), "name").is(strings().notBlank),
        validateThat(command.email(), "email").is(strings().contains("@")),
        validateThat(command.age(), "age").is(ints().isGreaterThanOrEqualTo(18)),
        Customer::new
);
```

Use `Validation.flatMap(...)` when creating the final value can also fail validation:

```java
Validation<Customer> customer = Validation.flatMap(
        validateThat(command.name(), "name").is(strings().notBlank),
        validateThat(command.email(), "email").is(strings().contains("@")),
        (name, email) -> Customer.create(name, email)
);
```

## Transform Before Validating

Use `after(...).is(...)` to transform input before checking it:

```java
Rule<String> normalizedPhoneNumber = after(transformations().strings().onlyDigits)
        .is(strings().length(10));

assertThat("0475 / 12 34 56", "phoneNumber")
        .is(normalizedPhoneNumber);
```

Common string transformations:

```java
assertThat("  Alice  ", "name")
        .is(after(transformations().strings().trim).is(strings().notBlank));

assertThat("BE 0123 456 789", "vatNumber")
        .is(after(transformations().strings().deleteWhitespace).is(strings().startsWith("BE")));

assertThat("abc-123", "code")
        .is(after(transformations().strings().onlyDigitsAndLetters).is(strings().alphaNumeric));

assertThat("alice", "name")
        .is(after(transformations().strings().capitalize).is(strings().startsWith("A")));
```

Common `BigDecimal` transformation:

```java
assertThat(new BigDecimal("-10.00"), "amount")
        .is(after(transformations().bigDecimals().abs).is(bigDecimals().isPositive));
```

You can also transform an existing rule:

```java
Rule<String> trimmedNotBlank = strings().notBlank
        .transform(transformations().strings().trim);

assertThat("  Alice  ", "name").is(trimmedNotBlank);
```

## Lists Of Domain Objects

Validate a list of objects by validating each element and flipping the list of validations into one validation:

```java
record Line(String description, int quantity) {}
record Order(List<Line> lines) {}

Rule<Line> validLine = all(
        given(Line::description).is(strings().notBlank),
        given(Line::quantity).is(ints().isStrictlyPositive)
);

List<Validation<Line>> lineValidations = order.lines().stream()
        .map(line -> validateThat(line).is(validLine))
        .toList();

Validation<List<Line>> validLines = Validation.flip(lineValidations, Order::lines);
```

When a line fails, the error message contains the list field and index:

```java
validLines.errors().map(ErrorMessage::getMessage);
// example: "lines[2].quantity.must.be.strictly.positive"
```

For a plain list without an owning property:

```java
Validation<List<String>> codes = Validation.flip(
        List.of(
                validateThat("A").is(strings().notBlank),
                validateThat("").is(strings().notBlank)
        )
);

codes.errors().map(ErrorMessage::getMessage);
// contains "[1].cannot.be.blank"
```

## Creating Custom Rules

A `Rule<T>` is just a function from `T` to `Validation<T>`:

```java
Rule<String> email = input -> Validation.of(
        input,
        value -> value != null && value.contains("@"),
        "must.be.valid.email"
);

assertThat("alice@example.com", "email").is(email);
```

Custom rule with a richer check:

```java
Rule<String> noDisposableEmail = input -> {
    boolean disposable = input.endsWith("@example.test") || input.endsWith("@mailinator.com");
    return Validation.of(input, !disposable, "must.not.be.disposable.email");
};
```

Custom rule for a domain object:

```java
record Period(LocalDate startDate, LocalDate endDate) {}

Rule<Period> validPeriod = period -> Validation.of(
        period,
        !period.endDate().isBefore(period.startDate()),
        "endDate.must.be.after.or.equal.to.startDate"
);

assertThat(new Period(start, end), "period").is(validPeriod);
```

## Error Messages

Errors are represented by `ErrorMessage`:

```java
ErrorMessage error = ErrorMessage.of("cannot.be.blank", "");
```

Field names are prepended to the message:

```java
validateThat("", "name")
        .is(strings().notBlank)
        .errors()
        .head()
        .getMessage();
// "name.cannot.be.blank"
```

Nested field names are built when using `given(...)`:

```java
record Address(String postalCode) {}
record Customer(Address address) {}

Rule<Customer> rule = given(Customer::address)
        .is(given(Address::postalCode).is(strings().length(4)));

Validation<Customer> result = validateThat(new Customer(new Address("12")))
        .is(rule);

result.errors().head().getMessage();
// "address.postalCode.must.have.length.4"
```

Indexes are included for list validation:

```java
validateThat(List.of("A", ""), "codes")
        .is(strings().notBlank.list())
        .errors()
        .head()
        .getMessage();
// "codes[1].cannot.be.blank"
```

## Working With `Validation<T>`

Map a valid value:

```java
Validation<String> normalized = validateThat("alice@example.com", "email")
        .is(strings().contains("@"))
        .map(String::toLowerCase);
```

Flat-map into another validation:

```java
Validation<CustomerId> customerId = validateThat(rawId, "customerId")
        .is(strings().startsWith("CUS-"))
        .flatMap(CustomerId::create);
```

Fold both outcomes into one value:

```java
String response = validateThat(email, "email")
        .is(strings().contains("@"))
        .fold(
                errors -> "Invalid: " + errors.map(ErrorMessage::getMessage).mkString(", "),
                validEmail -> "Accepted: " + validEmail
        );
```

Run side effects:

```java
validateThat(command.email(), "email")
        .is(strings().contains("@"))
        .peek(validEmail -> log.info("Valid email {}", validEmail))
        .peekErrors(errors -> errors.forEach(error -> log.warn(error.getMessage())));
```

Use an alternative validation:

```java
Validation<String> reference = validateThat(input, "reference")
        .is(strings().startsWith("INV-"))
        .orElse(() -> validateThat(input, "reference").is(strings().startsWith("PO-")));
```

Create validations from existing types:

```java
Validation<String> fromOptional = Validation.from(optionalName, () -> "name.is.required");
Validation<String> fromVavrOption = Validation.from(vavrOption, () -> "value.is.required");
Validation<Integer> fromTry = Validation.from(Try.of(() -> Integer.parseInt(raw)));
Validation<Customer> fromSupplier = Validation.from(() -> loadCustomer(command.customerId()));
```

## Testing Support

The `be.liantis.vetstrak.ddd.validation.support.ValidationAssert` class provides AssertJ helpers.

```java
import static be.liantis.vetstrak.ddd.validation.support.ValidationAssert.*;

class CustomerValidationTest {

    @Test
    void validatesCustomer() {
        Validation<Customer> result = validateThat(customer)
                .is(validCustomer);

        assertValid(result)
                .extracting(Customer::name)
                .isEqualTo("Alice");
    }

    @Test
    void reportsInvalidEmail() {
        Validation<Customer> result = validateThat(customerWithInvalidEmail)
                .is(validCustomer);

        assertInvalid(result)
                .hasMessage("email.must.contain.@")
                .hasErrorCount(1);
    }

    @Test
    void catchesValidationException() {
        assertInvalid(() -> assertThat("", "name").is(strings().notBlank))
                .hasMessage("name.cannot.be.blank");
    }
}
```

## Recommended Patterns

Define reusable rules close to the domain concept they protect:

```java
final class CustomerRules {
    static final Rule<String> name = strings().notBlank
            .and(strings().maxLength(100));

    static final Rule<String> email = strings().notBlank
            .and(strings().contains("@"))
            .and(strings().maxLength(320));

    static final Rule<Customer> customer = all(
            given(Customer::name).is(name),
            given(Customer::email).is(email)
    );
}
```

Use non-throwing validation inside domain construction:

```java
record Customer(String name, String email) {
    static Validation<Customer> create(String name, String email) {
        return Validation.map(
                validateThat(name, Customer::name).is(CustomerRules.name),
                validateThat(email, Customer::email).is(CustomerRules.email),
                Customer::new
        );
    }
}
```

Use throwing assertions at application boundaries:

```java
void handle(CreateCustomerCommand command) {
    Customer customer = Customer.create(command.name(), command.email())
            .getOrElseThrow();

    repository.save(customer);
}
```

## API Overview

Main entry points:

- `API.assertThat(value).is(rule)`: validate and throw on failure.
- `API.validateThat(value).is(rule)`: validate and return `Validation<T>`.
- `API.assertAll(validations...)`: throw once with all errors.
- `API.given(methodReference).is(rule)`: adapt a property rule to an object rule.
- `API.after(transformation).is(rule)`: transform before validation.
- `API.either(rule1, rule2)`: pass if either rule passes.

Rule composition:

- `rule.and(otherRule)`: run the next rule only when the previous rule passed.
- `rule.or(otherRule)`: pass when either rule passes.
- `rule.list()`: validate every list element.
- `rule.optional()`: validate only present optional values.
- `rule.nullSafe()`: fail with `cannot.be.null` for null input.
- `rule.describe("message")`: replace returned errors with a custom message.
- `rule.given(methodReference)`: apply a rule to a property.
- `rule.transform(function)`: transform input before validating.

Result handling:

- `validation.valid()`
- `validation.errors()`
- `validation.getOrElse(...)`
- `validation.getOrElseThrow()`
- `validation.map(...)`
- `validation.flatMap(...)`
- `validation.fold(...)`
- `validation.toOptional()`
- `validation.toTry()`
- `validation.toOption()`
- `validation.toStream()`
---

### License
This project is licensed under the Apache 2.0 License.