# Jakarta Bean Validation Integration

Full feature reference for the `jakarta-validation` module. For setup, the dependency declaration, and basic `@Valid` / `@Validated` usage, start with the [module README](../jakarta-validation/README.md).

When a rule produces an `Invalid` result, each FV error is converted to a BV `ConstraintViolation`. The FV error key becomes the violation message, and the FV path (including list indices like `items[2].price`) is mapped to BV property nodes.

Because these annotations are standard BV constraints, FV and BV validation unify naturally: a class validated by a hand-written `Rule<T>` can freely reference a class validated via one of these annotations, and vice versa. You can adopt either style per class and mix them within the same object graph.

## Annotation Reference

### `@FvStaticRule`

Points directly at a `static` field of type `Rule` on any class. This is the natural FV idiom: rules are plain static constants, and the annotation is just a pointer. When the rule lives on the annotated type itself, `on` can be omitted:

```java
@FvStaticRule(field = "RULE")
record Person(String name, int age) {

    public static final Rule<Person> RULE = Rule.all(
        strings.minLength(2).on(Person::name),
        ints.atLeast(18).on(Person::age)
    );
}
```

When the rule lives on a different class, supply `on` to point at that class:

```java
@FvStaticRule(on = PersonRules.class, field = "VALIDATE")
record Person(String name, int age) {}

public class PersonRules {
    public static final Rule<Person> VALIDATE = Rule.all(
        strings.minLength(2).on(Person::name),
        ints.atLeast(18).on(Person::age)
    );
}
```

The field name is validated eagerly at startup — a typo or missing field is caught before the first request (see [Startup Validation](#startup-validation)).

### `@FvRule`

Points at a class that implements `Rule` or `RuleProvider` and has a public no-arg constructor. BV instantiates it once per validator lifecycle:

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

Use `RuleProvider` when the class serves as a namespace for multiple related rules and does not need to implement `Rule` directly:

```java
@FvRule(Person.Rules.class)
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

### `@FvRuleBean`

Points at a class registered as a Spring bean. Unlike `@FvRule`, the class does not need a public no-arg constructor: Spring creates and injects it, so it can receive constructor-injected or `@Autowired` dependencies:

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

Startup validation covers `@FvRuleBean` too: if the bean type cannot be found in the application context at startup, the missing bean is reported immediately along with any other misconfigured rules.

## Null Handling

A null value is treated as valid — the FV rule is not invoked for nulls. This follows the standard BV convention. Pair with `@NotNull` if null should be rejected:

```java
public void enroll(@Valid @NotNull Person person) { ... }
```

## Validating Nested Objects

For nested objects, two options are available.

**Option 1 — explicit FV composition.** The root rule calls the nested rule directly using `Rule.on()`:

```java
@FvStaticRule(on = Shipment.class, field = "RULE")
record Shipment(String trackingNumber, Person recipient) {
    static final Rule<Shipment> RULE = Rule.all(
        strings.minLength(5).on(Shipment::trackingNumber),
        Person.RULE.on(Shipment::recipient)
    );
}
```

This is pure FV — no BV annotations on the nested type are needed. All validation is expressed in one place, which makes the rule easy to test and reason about in isolation.

**Option 2 — BV `@Valid` cascade.** Place `@Valid` on the nested field and let BV trigger each type's own annotation independently:

```java
@FvStaticRule(on = Shipment.class, field = "RULE")
record Shipment(String trackingNumber, @Valid Person recipient) {
    static final Rule<Shipment> RULE = strings.minLength(5).on(Shipment::trackingNumber);
}
```

`Shipment.RULE` does not reference `Person`'s rule — BV wires them together through `@Valid`. This keeps each rule focused on its own type and is the natural choice when the nested type is also validated independently elsewhere.

Both options produce the same violations for `new Shipment("TRK", new Person("A", 16))`:

| Property path    | Error key              |
|------------------|------------------------|
| `trackingNumber` | `must.have.min.length` |
| `recipient.name` | `must.have.min.length` |
| `recipient.age`  | `must.be.at.least`     |

### Collections

`@Valid` on a type argument cascades into each element of the collection, with the element index included in the violation path:

```java
record Roster(List<@Valid Person> members) {}
```

Validating a `Roster` whose second member is invalid produces violations at `members[1].name` and `members[1].age`. This is standard BV cascade behaviour — no FV-specific configuration required.

## Return Value Validation

All three annotations support `ElementType.METHOD`, which lets you validate the return value of a method. Spring's `@Validated` AOP intercepts the call and runs the constraint against what the method returned:

```java
@Service
@Validated
public class PersonService {

    @FvRule(Person.Validator.class)
    public Person findById(long id) {
        return repository.findById(id); // validated after the method returns
    }
}
```

If the returned object violates the rule, a `ConstraintViolationException` is thrown with violations whose paths include `<return value>` as a path node — for example, `findById.<return value>.name`.

This is the mirror of parameter validation: `@Valid` on a parameter validates the input before the method runs; the FV annotation on the method itself validates the output after it returns.

## Error Messages and i18n

FV error keys become BV constraint violation messages in the form `{error.key}`, which is the standard BV message template notation. Resolve these keys to human-readable strings by adding a `ValidationMessages.properties` file to your classpath:

```properties
# src/main/resources/ValidationMessages.properties
must.have.min.length=Must be at least {length} character(s)
must.be.at.least=Must be at least {min}
must.not.be.blank=Must not be blank
```

When Hibernate Validator is used, FV's error parameters (like `{min}` or `{max}`) are automatically forwarded to HV's message interpolation context, so placeholders in your messages resolve correctly. Other BV implementations receive the bare `{error.key}` template without parameter substitution.

The annotation-level `message()` attribute required by the BV spec is intentionally not honored: because a single annotation can produce multiple violations with distinct error keys, there is no meaningful single template that covers them all — use `ValidationMessages.properties` for per-key overrides instead.

## Combining with Standard BV Constraints

The FV annotations compose naturally with standard BV annotations. A type can carry both BV constraints and an FV rule — all constraints are evaluated together and their violations are accumulated:

```java
@FvStaticRule(field = "RULE")
record Order(@NotBlank String reference, @Min(1) int quantity) {

    public static final Rule<Order> RULE = Rule.all(
        strings.startsWith("ORD-").on(Order::reference),
        ints.atLeast(1).on(Order::quantity)
    );
}
```

The same applies at method boundaries — BV annotations and FV annotations on the same method call all run together:

```java
@Service
@Validated
public class EnrollmentService {

    public void enrollWithDegrees(@Valid Person person, @Max(2) int degrees) { ... }
}
```

An invalid `Person` and a `degrees` value over 2 produce three violations simultaneously: two from `@FvStaticRule` and one from `@Max`.

## Composed Annotations

Any of the three annotations can be used as a meta-annotation to create a composed annotation — a project-specific shorthand that is self-documenting and hides the implementation detail:

```java
@FvRule(Person.Validator.class)
@Constraint(validatedBy = {})
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPerson {
    String message() default "";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

Then use `@ValidPerson` exactly where you would have written `@FvRule(Person.Validator.class)`:

```java
@ValidPerson
record Person(String name, int age) { ... }
```

```java
public void enroll(@Valid @ValidPerson Person person) { ... }
```

BV discovers the `@FvRule` meta-annotation on `@ValidPerson` and invokes the validator transparently. Violations, paths, groups, and message interpolation all work identically to using the original annotation directly.

`@FvStaticRule` requires an explicit `on` when used as a meta-annotation (the rule holder cannot be inferred from an annotation type declaration):

```java
@FvStaticRule(on = Person.class, field = "RULE")
@Constraint(validatedBy = {})
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPerson { ... }
```

## Repeating Annotations

All three annotations are `@Repeatable`, so multiple instances can be stacked on a single element. This is useful when validation logic for a type comes from more than one rule — for example, a core module supplies a base rule and a feature module adds additional constraints:

```java
@FvRule(Product.BaseValidator.class)
@FvRule(Product.InventoryValidator.class)
record Product(String code, int stock) {

    static class BaseValidator implements Rule<Product> {
        private static final Rule<Product> IMPL = strings.minLength(3).on(Product::code);
        @Override public Validation<Product> apply(Product p) { return IMPL.apply(p); }
    }

    static class InventoryValidator implements Rule<Product> {
        private static final Rule<Product> IMPL = ints.atLeast(1).on(Product::stock);
        @Override public Validation<Product> apply(Product p) { return IMPL.apply(p); }
    }
}
```

BV runs each repeated annotation independently and accumulates all violations. Mixing annotation types also works: `@FvRule`, `@FvStaticRule`, and `@FvRuleBean` can all appear together on the same element, since they are already different annotation types.

## Startup Validation

When Spring Boot is on the classpath, FV automatically scans the application's base packages at startup for types annotated with `@FvRule`, `@FvStaticRule`, or `@FvRuleBean` and eagerly validates each annotation's configuration. Misconfiguration — a wrong field name, a missing no-arg constructor, a provider that returns null — is reported immediately when the application starts, before any request is handled.

All problems are collected before throwing, so a single startup failure lists every misconfigured type at once:

```
IllegalStateException: FV rule annotation misconfiguration detected at startup - fix the following before the application can start:
  - com.example.Order: No field 'RULES' found on com.example.Order
  - com.example.Product: Cannot instantiate com.example.Product$Validator - ensure it has a public no-arg constructor.
```

## Configuration

| Property                       | Default | Description                                                         |
|--------------------------------|---------|---------------------------------------------------------------------|
| `fv.rule.startup-scan.enabled` | `true`  | Set to `false` to skip the FV annotation classpath scan at startup. |
