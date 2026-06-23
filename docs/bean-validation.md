# Jakarta Bean Validation Integration

The `jakarta-validation` module bridges FV rules into Jakarta Bean Validation (BV), so any
BV-aware framework — Spring `@Validated`, JPA, Quarkus, etc. — can trigger FV rules through
the standard `@Valid` mechanism.

When a rule produces an `Invalid` result, each FV error is converted to a BV
`ConstraintViolation`. The FV error key becomes the violation message, and the FV path
(including list indices like `items[2].price`) is mapped to BV property nodes.

Because `@FvRule` is a standard BV constraint, FV and BV validation unify naturally: a class
validated by a hand-written `Rule<T>` can freely reference a class validated via `@FvRule`, and
vice versa. You can adopt either style per class and mix them within the same object graph.

## Dependency

```xml
<dependency>
  <groupId>be.iffy.fv</groupId>
  <artifactId>jakarta-validation</artifactId>
  <version>2.1.0</version>
</dependency>
```

## Basic usage

Annotate your types with `@FvRule`, pointing each at a `public static` field that holds the
rule. Use `@Valid` on nested fields to cascade validation — each type's rule runs independently
and the violations are combined.

```java
@FvRule(on = Address.class, field = "RULE")
record Address(String street, String city) {

    public static final Rule<Address> RULE = Rule.all(
        strings.notBlank().on(Address::street),
        strings.notBlank().on(Address::city)
    );
}

@FvRule(on = Person.class, field = "RULE")
record Person(String name, int age, @Valid Address address) {

    public static final Rule<Person> RULE = Rule.all(
        strings.minLength(2).on(Person::name),
        ints.atLeast(18).on(Person::age)
    );
}
```

```java
@Service
@Validated
public class EnrollmentService {

    public void enroll(@Valid Person person) {
        // only reached when both person and address are valid
    }
}
```

Passing an invalid `Person` to `enroll()` throws a standard `ConstraintViolationException`.
Violations from `Person.RULE` appear at `name` and `age`; violations from `Address.RULE`
appear at `address.street` and `address.city`. Each rule stays responsible for its own type —
BV wires them together through `@Valid`.

## Choosing how to point at the rule

`@FvRule` supports three ways to specify which rule to run. Exactly one must be set.

### Static field reference (recommended)

Point directly at a `public static` field of type `Rule` on any class. This is the natural FV
idiom: rules are plain static constants, and the annotation is just a pointer — no wrapper class
or interface required.

```java
@FvRule(on = Person.class, field = "RULE")
record Person(String name, int age) {

    public static final Rule<Person> RULE = Rule.all(
        strings.minLength(2).on(Person::name),
        ints.atLeast(18).on(Person::age)
    );
}
```

The field name is validated eagerly at startup — a typo or missing field is caught before the
first request (see [Startup validation](#startup-validation)).

The rule can live on a separate class when you want to keep the type clean or share rules across
modules:

```java
@FvRule(on = PersonRules.class, field = "VALIDATE")
record Person(String name, int age) {}

public class PersonRules {
    public static final Rule<Person> VALIDATE = Rule.all(
        strings.minLength(2).on(Person::name),
        ints.atLeast(18).on(Person::age)
    );
}
```

### Rule class

Point at a class that implements `Rule` and has a public no-arg constructor.
BV instantiates it once per validator lifecycle.

```java
@FvRule(Person.Validator.class)
record Person(String name, int age) {

    public static class Validator implements Rule<Person> {
        private static final Rule<Person> IMPL = Rule.all(
            strings.minLength(2).on(Person::name),
            ints.atLeast(18).on(Person::age)
        );

        @Override
        public Validation<Person> apply(Person p) { return IMPL.apply(p); }
    }
}
```

Use this when the validator class needs to implement `Rule` directly (e.g., it is also used
programmatically by other rules).

### RuleProvider

Point at a class that implements `RuleProvider<T>` — a factory that returns the rule via
`provide()`. The class does not need to implement `Rule` itself. Useful when the class serves
as a namespace for multiple related rules.

```java
@FvRule(provider = Person.Rules.class)
record Person(String name, int age) {

    public static class Rules implements RuleProvider<Person> {
        private static final Rule<Person> PERSON = Rule.all(
            strings.minLength(2).on(Person::name),
            ints.atLeast(18).on(Person::age)
        );

        // could define other Person-related rules here too

        @Override
        public Rule<Person> provide() { return PERSON; }
    }
}
```

The provider class must also have a public no-arg constructor.

## Validating nested objects

For nested objects, you have two options.

**Option 1 — explicit FV composition.** The root rule calls the nested rule directly using
`Rule.on()`:

```java
@FvRule(on = Shipment.class, field = "RULE")
record Shipment(String trackingNumber, Person recipient) {
    static final Rule<Shipment> RULE = Rule.all(
        strings.minLength(5).on(Shipment::trackingNumber),
        Person.RULE.on(Shipment::recipient)
    );
}
```

This is pure FV — no BV annotations on the nested type are needed. All validation is expressed
in one place, which makes the rule easy to test and reason about in isolation.

**Option 2 — BV `@Valid` cascade.** Place `@Valid` on the nested field and let BV trigger each
type's own `@FvRule` independently:

```java
@FvRule(on = Shipment.class, field = "RULE")
record Shipment(String trackingNumber, @Valid Person recipient) {
    static final Rule<Shipment> RULE = strings.minLength(5).on(Shipment::trackingNumber);
}
```

`Shipment.RULE` does not reference `Person`'s rule — BV wires them together through `@Valid`.
This keeps each rule focused on its own type and is the natural choice when the nested type is
also validated independently elsewhere.

Both options produce the same violations for `new Shipment("TRK", new Person("A", 16))`:

| Property path    | Error key              |
|------------------|------------------------|
| `trackingNumber` | `must.have.min.length` |
| `recipient.name` | `must.have.min.length` |
| `recipient.age`  | `must.be.at.least`     |

### Collections

`@Valid` on a type argument cascades into each element of the collection, with the element
index included in the violation path:

```java
record Roster(List<@Valid Person> members) {}
```

Validating a `Roster` whose second member is invalid produces violations at
`members[1].name` and `members[1].age`. This is standard BV cascade behaviour — no
FV-specific configuration required.

## Error messages and i18n

FV error keys become BV constraint violation messages in the form `{error.key}`, which is the
standard BV message template notation. You can resolve these keys to human-readable strings
through the normal BV message interpolation mechanism by adding a
`ValidationMessages.properties` file to your classpath:

```properties
# src/main/resources/ValidationMessages.properties
must.have.min.length=Must be at least {min} character(s)
must.be.at.least=Must be at least {min}
must.not.be.blank=Must not be blank
```

When Hibernate Validator is used, FV's error parameters (like `{min}` or `{max}`) are
automatically forwarded to HV's message interpolation context, so placeholders in your
messages resolve correctly. Other BV implementations receive the bare `{error.key}` template
without parameter substitution.

The annotation-level `message()` attribute required by the BV spec is intentionally not
honored: because a single `@FvRule` can produce multiple violations with distinct error keys,
there is no meaningful single template that covers them all — use `ValidationMessages.properties`
for per-key overrides instead.

## Null handling

A null value is treated as valid — `@FvRule` passes through nulls without invoking the rule.
This follows the standard BV convention. Pair with `@NotNull` if null should be rejected:

```java
public void enroll(@Valid @NotNull Person person) { ... }
```

## Combining with standard BV constraints

`@FvRule` composes naturally with standard BV annotations. All constraints are evaluated
together and their violations are accumulated:

```java
@Service
@Validated
public class EnrollmentService {

    public void enrollWithDegrees(@Valid Person person, @Max(2) int degrees) { ... }
}
```

An invalid `Person` and a `degrees` value over 2 produce three violations simultaneously —
two from `@FvRule` and one from `@Max`.

## Startup validation

When Spring Boot is on the classpath, FV automatically scans the application's base packages
at startup for types annotated with `@FvRule` and eagerly validates each annotation's
configuration. Misconfiguration — a wrong field name, a missing no-arg constructor, a provider
that returns null — is reported immediately when the application starts, before any request is
handled, rather than the first time that type is validated.

All problems are collected before throwing, so a single startup failure lists every
misconfigured type at once:

```
IllegalStateException: @FvRule misconfiguration detected at startup — fix the following before the application can start:
  - com.example.Order: No field 'RULES' found on com.example.Order
  - com.example.Product: Cannot instantiate Rule class com.example.Product$Validator — ensure it has a public no-arg constructor.
```

## Configuration

| Property | Default | Description |
|---|---|---|
| `fv.rule.startup-scan.enabled` | `true` | Set to `false` to skip the `@FvRule` classpath scan at startup. |

```properties
# Disable startup scanning (e.g. to speed up test contexts where misconfiguration is not a concern)
fv.rule.startup-scan.enabled=false
```
