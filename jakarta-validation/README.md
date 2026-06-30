# Jakarta Validation Module for Functional Validation

Bridge FV rules into Jakarta Bean Validation so you can validate domain types at Spring method boundaries using `@Valid` and `@Validated`, while writing validation logic in plain Functional Validation.

## Dependency

```xml
<dependency>
    <groupId>be.iffy.fv</groupId>
    <artifactId>jakarta-validation</artifactId>
    <version>2.1.0</version>
</dependency>
```

Make sure Jakarta Bean Validation (e.g. Hibernate Validator) and Spring Boot are on the classpath.

## Annotations

Three annotations are available, each finding the rule in a different way:

| Annotation      | Where the rule lives                                   | Spring needed? |
|-----------------|--------------------------------------------------------|----------------|
| `@FvStaticRule` | A `public static` field on any class (recommended)     | No             |
| `@FvRule`       | A class implementing `Rule` or `RuleProvider`          | No             |
| `@FvRuleBean`   | A Spring bean implementing `Rule` or `RuleProvider`    | Yes            |

## Quick Start

### 1. Annotate your type

The simplest path: point `@FvStaticRule` at a `static` field of type `Rule<T>`. Both `on` (the class that declares the field) and `field` (the field name) are required:

```java
@FvStaticRule(on = Person.class, field = "RULE")
record Person(String name, int age) {

    public static final Rule<Person> RULE = Rule.all(
        strings.minLength(2).on(Person::name),
        ints.atLeast(18).on(Person::age)
    );
}
```

The rule may live on any class, not just the annotated type:

```java
@FvStaticRule(on = PersonRules.class, field = "VALIDATE")
record Person(String name, int age) {}
```

### 2. Validate at method boundaries

Annotate your service with `@Validated` and use `@Valid` on parameters to trigger validation:

```java
@Service
@Validated
public class EnrollmentService {

    public void enroll(@Valid Person person) {
        // only reached when person is valid
    }
}
```

An invalid `Person` throws a `ConstraintViolationException`. Each FV error becomes a `ConstraintViolation` with the FV error key as the violation message.

## `@FvRule`

When you prefer a class over a static field, implement `Rule` directly:

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

BV instantiates the class once per validator lifecycle via its public no-arg constructor. Implement `RuleProvider` instead of `Rule` when the class serves as a namespace for multiple related rules.

## `@FvRuleBean`

When the rule needs Spring-injected dependencies, register it as a Spring bean:

```java
@FvRuleBean(Order.Validator.class)
record Order(String ref, List<LineItem> items) {

    @Component
    @RequiredArgsConstructor
    public static class Validator implements Rule<Order> {

        private final PricingService pricingService;

        @Override
        public Validation<Order> apply(Order order) {
            return Rule.all(
                strings.notBlank().on(Order::ref),
                Rule.of(o -> pricingService.isWithinBudget(o.items()), "order.over.budget")
            ).apply(order);
        }
    }
}
```

The bean is looked up by type from the Spring `BeanFactory`. Using this annotation outside a Spring context throws an `IllegalArgumentException` at validation time.

## Startup Validation

A Spring Boot auto-configuration scans your application's base packages at startup and validates every `@FvStaticRule`, `@FvRule`, and `@FvRuleBean` annotation eagerly, before any request is handled. A typo in a field name or a missing no-arg constructor is caught immediately on startup rather than at the first validation call.

---

For the complete feature reference (nested objects, collections, return value validation, composed annotations, repeating annotations, error messages, and configuration) see the [full documentation](../docs/bean-validation.md).
