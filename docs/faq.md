# Frequently Asked Questions (FAQ)

Welcome to the FAQ for the Iffy Functional Validation (FV) library. If you have a question not answered here, please
feel free to open an issue or reach out to the maintainers.

### Table of Contents

**Core Concepts**
- [What is the difference between a `Rule` and a `MappingRule`?](#what-is-the-difference-between-a-rule-and-a-mappingrule)
- [Whats with the RuleLike<? super T, ? extends Validation<R>> signatures?](#whats-with-the-function-super-t--extends-validationr-signatures)
- [Do I need to use Strings to name the values I'm validating?](#do-i-need-to-use-strings-to-name-the-values-im-validating)
- [Are rules null-safe by default?](#are-rules-null-safe-by-default)
- [How do I create a Validation directly from a nullable value?](#how-do-i-create-a-validation-directly-from-a-nullable-value)

**Errors and Diagnostics**
- [I have an Invalid validation, how can I know what was wrong?](#i-have-an-invalid-validation-how-can-i-know-what-was-wrong)
- [Can I use my own error messages?](#can-i-use-my-own-error-messages)
- [How can I easily make my own rule, e.g., for checking that a string is a palindrome?](#how-can-i-easily-make-my-own-rule-eg-for-checking-that-a-string-is-a-palindrome)

**Combining Rules**
- [How do I combine multiple rules?](#how-do-i-combine-multiple-rules)
- [How can I negate an existing rule?](#how-can-i-negate-an-existing-rule)
- [If a validation fails, can I provide a fallback value or another rule to try?](#if-a-validation-fails-can-i-provide-a-fallback-value-or-another-rule-to-try)
- [How can I apply a rule only if a certain condition is met?](#how-can-i-apply-a-rule-only-if-a-certain-condition-is-met)
- [If I have for example a Rule for Number, can I use it to also validate a subtype like BigDecimal?](#if-i-have-for-example-a-rule-for-number-can-i-use-it-to-also-validate-a-subtype-like-bigdecimal)
- [I have a Rule for a certain type (e.g., Amount), and now I have another type Transaction that wraps Amount, can I reuse the Amount rule?](#i-have-a-rule-for-a-certain-type-eg-amount-and-now-i-have-another-type-transaction-that-wraps-amount-can-i-reuse-the-amount-rule)
- [I want a single reusable rule that validates and transforms the same input in multiple ways — how?](#i-want-a-single-reusable-rule-that-validates-and-transforms-the-same-input-in-multiple-ways--how)
- [How do I transform the result of a MappingRule?](#how-do-i-transform-the-result-of-a-mappingrule)
- [How do I get a standard Java `Predicate` from a `Rule`?](#how-do-i-get-a-standard-java-predicate-from-a-rule)

**Containers: Optional, List, Set, Map**
- [How can I check that my optional value meets a Rule when it is not empty (but empty is also allowed)?](#how-can-i-check-that-my-optional-value-meets-a-rule-when-it-is-not-empty-but-empty-is-also-allowed)
- [How can I check that my optional value meets a Rule when it is not empty (but this time empty is NOT allowed)?](#how-can-i-check-that-my-optional-value-meets-a-rule-when-it-is-not-empty-but-this-time-empty-is-not-allowed)
- [I have a List of things, and I want to check that each entry meets a Rule](#i-have-a-list-of-things-and-i-want-to-check-that-each-entry-meets-a-rule)
- [Can I also validate Sets?](#can-i-also-validate-sets)
- [Can I also validate Maps?](#can-i-also-validate-maps)
- [I have a List<Validation<T>>, how can I turn it into a Validation<List<T>>?](#i-have-a-listvalidationt-how-can-i-turn-it-into-a-validationlistt)
- [I have a String, and want to make sure it's a valid value for a given Enum](#i-have-a-string-and-want-to-make-sure-its-a-valid-value-for-a-given-enum)
- [How does this library work with standard Java collections vs Vavr collections?](#how-does-this-library-work-with-standard-java-collections-vs-vavr-collections)
- [Can I validate Vavr `Either` values?](#can-i-validate-vavr-either-values)

**Building Validated Objects**
- [How can I make sure my record or class is created with valid values?](#how-can-i-make-sure-my-record-or-class-is-created-with-valid-values)
- [In my constructor, I want to be liberal with my input, and only validate the value after changing it](#in-my-constructor-i-want-to-be-liberal-with-my-input-and-only-validate-the-value-after-changing-it)
- [Ok, but I want to transform multiple fields in my constructor, how do I get their transformed values?](#ok-but-i-want-to-transform-multiple-fields-in-my-constructor-how-do-i-get-their-transformed-values)
- [Ok, but can I do the same when defining a Rule?](#ok-but-can-i-do-the-same-when-defining-a-rule)
- [How do I perform cross-field validation where one field's validation depends on another?](#how-do-i-perform-cross-field-validation-where-one-fields-validation-depends-on-another)

**Exception Interop**
- [I have some type whose constructor throws an exception, how can I make a Validation for this type?](#i-have-some-type-whose-constructor-throws-an-exception-how-can-i-make-a-validation-for-this-type)
- [What types can I turn into a Validation?](#what-types-can-i-turn-into-a-validation)
- [What's the difference between methods like map and mapCatching? What does catchingAll mean?](#whats-the-difference-between-methods-like-map-and-mapcatching-what-does-catchingall-mean)

**Working with Validation Results**
- [I have a Validation, but I want to add an extra check on the value](#i-have-a-validation-but-i-want-to-add-an-extra-check-on-the-value)
- [I want to perform a side effect (like logging) only if a validation is successful.](#i-want-to-perform-a-side-effect-like-logging-only-if-a-validation-is-successful)
- [How do I consume a Validation result into a plain value or handle both branches?](#how-do-i-consume-a-validation-result-into-a-plain-value-or-handle-both-branches)

**Time and Date Validation**
- [What date and time rule namespaces are available?](#what-date-and-time-rule-namespaces-are-available)
- [How do I control "now" in time rules (e.g., in tests)?](#how-do-i-control-now-in-time-rules-eg-in-tests)

**String Transformations (`stringOps`)**
- [What string transformations are available to use with `after()`?](#what-string-transformations-are-available-to-use-with-after)

**Testing**
- [How do I write clean assertions on Validation results in tests?](#how-do-i-write-clean-assertions-on-validation-results-in-tests)

**Pattern Matching**
- [Can I use Java pattern matching (switch) on a Validation?](#can-i-use-java-pattern-matching-switch-on-a-validation)

---

## Core Concepts

### What is the difference between a `Rule` and a `MappingRule`?

In short: A **`Rule<T>`** validates a value without changing it, while a **`MappingRule<T, R>`** validates a value and
transforms it into another type or value.

* **`Rule<T>`**:
    * Think of it as a `Predicate<T>` that returns a `Validation<T>` instead of a boolean.
    * If the validation succeeds, it returns the **exact same instance** that was passed in.
    * It is used when you want to check if a value meets certain criteria (e.g., "is not null", "is positive", "matches
      a regex").

* **`MappingRule<T, R>`**:
    * Think of it as a `Function<T, R>` that returns a `Validation<R>`.
    * It is used when validation and transformation are coupled. For example, parsing a `String` into an `Integer` or a
      `LocalDate`.
    * If the parsing or any subsequent validation fails, it returns an `Invalid` result with error messages.
    * If it succeeds, it returns a `Valid` result containing the transformed value.

**When to use which?**
Use a `Rule` for simple checks on a value. Use a `MappingRule` when you need to change the type of the value or when you
want to "materialize" a validated object from a raw input.

You'll notice that lots of methods take functions with a signature like `RuleLike<? super T, ? extends Validation<R>>`.
This is the generalized signature for both `Rule` and `MappingRule`, but for Rule T and R are the same type.

---

### Whats with the RuleLike<? super T, ? extends Validation<R>> signatures?

That signature is the more generic signature of a `Rule<T>` or `MappingRule<T, R>`.  
By using this generic function signature in the DSL and other composition methods, the library becomes much more
flexible:

* **Interoperability**: You can pass not only `Rule` or `MappingRule` instances but also any method reference or lambda
  that matches the signature (e.g., `this::myCustomValidation`).
* **Reduced Ceremony**: You don't always have to wrap your logic in a `Rule.of(...)` if you already have a function that
  returns a `Validation`.

Both `Rule<T>` and `MappingRule<T, R>` actually extend this functional interface, which is why they can be used wherever
this signature is expected.

---

### Do I need to use Strings to name the values I'm validating?

No! While you can always use a plain `String` to name a value (which becomes the path segment in error messages), the
library also supports **`PropertySelector`**.

A `PropertySelector` is a functional interface that allows you to use **method references** to identify properties. The
library then automatically extracts the property name from the method reference.

#### Why use `PropertySelector`?

- **Type-safety**: You get compiler checks and IDE support (autocompletion, refactoring).
- **Less duplication**: No need to repeat field names as strings.
- **Automatic naming**: It handles `get` and `is` prefixes and record component names automatically.

#### Examples

**Using Strings:**

```java
Validation<String> v = validateThat(user.getName(), "name").is(strings.notEmpty());
// Error path: "name"
```

**Using `PropertySelector`:**

```java
Validation<String> v = validateThat(user.getName(), User::getName).is(strings.notEmpty());
// Error path: "name" (extracted from getName)

Validation<Boolean> v2 = validateThat(user.isActive(), User::isActive).is(booleans.isTrue());
// Error path: "active" (extracted from isActive)

// For Java Records:
Validation<String> v3 = validateThat(person.email(), Person::email).is(strings.looksLikeEmailAddress());
// Error path: "email" (extracted from email)
```

You can use `PropertySelector` in many places, including `validateThat`, `assertThat`, and when focusing rules with
`Rule.on()` or `rule.on()`.

---

### Are rules null-safe by default?

Most factory methods like `Rule.of(Predicate, ...)` include a null check. If the input is `null`, they will return a
`Validation.invalid("must.not.be.null")` without calling your predicate.

However, if you implement the `Rule` interface directly or use certain combinators, you should be aware of nullability.
It is generally recommended to start your validation chain with `Rule.notNull()` if you expect a non-null value.

---

### How do I create a Validation directly from a nullable value?

Use `Validation.fromNullable(value)`. It returns `Valid(value)` if the value is non-null, or
`Invalid("must.not.be.null")` if it is null:

```java
Validation<String> v1 = Validation.fromNullable("hello"); // Valid("hello")
Validation<String> v2 = Validation.fromNullable(null);    // Invalid([must.not.be.null])
```

This is a convenient shorthand for bridging nullable APIs into the validation world without having to write your own
null check.

---

## Errors and Diagnostics

### I have an Invalid validation, how can I know what was wrong?

When a validation fails, it returns an **`Invalid`** object. You can access the errors using the `errors()` method,
which returns a `List<ErrorMessage>`.

Each **`ErrorMessage`** contains:

* **`errorKey`**: A unique string identifying the type of error (e.g., `"must.have.length.between"`).
* **`paths`**: The path to the field that failed validation.
* **`parameters`**: A `Map<String, Object>` containing dynamic values that can be used to format a human-readable
  message.

#### The `formatted()` method

For quick debugging or logging, you can use the **`formatted()`** method. It returns a string that combines the path,
the error key, and all parameters in a standardized format: `path.to.field.error.key:{param1:value1,param2:value2}`.

#### Example: Inspecting parameters and using `formatted()`

```java
import be.iffy.fv.Validation;
import be.iffy.fv.ErrorMessage;

import static be.iffy.fv.rules.text.StringRules.strings;

Validation<String> result = strings.lengthBetween(3, 10).apply("hi");

if(result.isInvalid()){
    ErrorMessage error = result.errors().head();
    
    // Accessing individual components
    System.out.println("Error Key: "+error.errorKey()); // must.have.length.between
    System.out.println("Min length: "+error.parameters().get("min").get()); // 3
    System.out.println("Max length: "+error.parameters().get("max").get()); // 10
    
    // Using formatted() for a quick overview
    System.out.println("Formatted: "+error.formatted()); // Output: must.have.length.between:{min:3,max:10}
}
```

#### Where are the parameters and error keys documented?

The parameters available in the `ErrorMessage` are documented in the **Javadoc** of the rule factory methods.

For example, the Javadoc for `strings.lengthBetween(min, max)` explicitly states:

> **Parameters:**
> - `min`: the minimum allowed length (`int`)
> - `max`: the maximum allowed length (`int`)

By checking the Javadoc of the rules you are using, you can know exactly which keys to expect in the parameters map.
It's the same for the error key, it is also documented in the javadoc.

---

### Can I use my own error messages?

Yes! While many built-in rules use standard keys like `must.not.be.null`, you can always override the error message or
key using `.withErrorKey("my.custom.key")` or by providing an `ErrorMessage` object during rule creation.  
You can also use change the errors on a Validation with `Validation.mapErrors`.

---

### How can I easily make my own rule, e.g., for checking that a string is a palindrome?

The easiest way to create a custom rule is by using the `Rule.of(Predicate, String)` factory method. This method
automatically handles null-safety (returning `must.not.be.null` if the input is null) so you can focus on your logic.

```java
Rule<String> palindromeRule = Rule.of(
        s -> new StringBuilder(s).reverse().toString().equalsIgnoreCase(s),
        "must.be.palindrome"
);
```

For more complex rules, you can also implement the `Rule<T>` functional interface directly.

---

## Combining Rules

### How do I combine multiple rules?

You can combine rules using several composition methods:

#### Rule combinators

| Method                         | Behavior                                                                            | Short-circuiting | Error handling                                |
|--------------------------------|:------------------------------------------------------------------------------------|------------------|-----------------------------------------------|
| and(other)                     | AND; always runs both                                                               | No               | Accumulating (combine errors)                 |
| all(rules...)                  | AND over many; all must pass                                                        | No               | Accumulating (combine all errors)             |
| any(rules...)                  | OR over many; succeeds on first rule that passes                                    | Yes              | Accumulating if all fail                      |
| fallback(other)                | Fallback; uses other only if this fails; if both fail, keep only other's errors     | Yes              | Not accumulating (only fallback's errors)     |
| or(other)                      | OR; uses other only if this fails; if both fail, combine errors                     | Yes              | Accumulating                                  |
| then(rule)                     | AND in sequence; applies rule to the valid value; returns `Rule<T>`                 | Yes              | Not accumulating                              |
| then(mappingRule)              | AND in sequence; applies mappingRule to the valid value; returns `MappingRule<T,R>` | Yes              | Not accumulating                              |
| xor(other, errorKey)           | Exactly one of two must pass; evaluates both                                        | No               | Non-accumulating (single errorKey on failure) |
| exactlyOne(errorKey, rules...) | Exactly one of N must pass; evaluates all                                           | No               | Non-accumulating (single errorKey on failure) |

#### MappingRule combinators

| Method             | Behavior                                                                              | Short-circuiting | Error handling                            |
|--------------------|---------------------------------------------------------------------------------------|------------------|-------------------------------------------|
| fallback(fallback) | Fallback; uses fallback only if this fails; if both fail, keep only fallback's errors | Yes              | Not accumulating (only fallback's errors) |
| then(rule)         | AND in sequence; applies rule to successful mapped result                             | Yes              | Not accumulating                          |
| or(other)          | OR; uses other only if this fails; if both fail, combine errors                       | Yes              | Accumulating                              |
| combine(other)     | Start builder to combine multiple MappingRules                                        | No               | Accumulating across combined results      |

#### What is the difference between `and()`, `then(rule)`, and `Rule.all()`?

These all express "both rules must pass", but differ in how errors are collected and whether both rules always run:

1. **`ruleA.and(ruleB)` (accumulating):**
    * Both rules are **always executed**.
    * If both fail, the result contains **all errors** from both.
    * Use this for independent rules where you want to report as many problems as possible at once.

2. **`ruleA.then(ruleB)` (short-circuiting):**
    * If `ruleA` fails, `ruleB` is **not executed**.
    * The result contains only the errors from `ruleA`.
    * Use this in two situations:
        1. `ruleB` *depends on* `ruleA` succeeding — e.g. `notNull().then(minLength(5))` (calling `minLength` on `null` would throw).
        2. `ruleA` failing makes `ruleB`'s error *redundant or confusing* — even if `ruleB` could technically run, its error adds no useful signal when `ruleA` already failed.
           For example, a BIC code with the wrong length will also fail a format regex, but telling the user both at once is noise. Once the length is correct, the format check becomes meaningful:
           ```java
           Rule<String> validBic = any(length(8), length(11))
               .withErrorKey("length.must.be.8.or.11")
               .then(followsBicPattern);
           ```
    * Returns a `Rule<T>`, unlike `.then(mappingRule)` which returns a `MappingRule<T, R>`.

3. **`Rule.all(ruleA, ruleB, ...)`:**
    * Like `and()`, executes all rules and collects all errors.
    * More readable when combining three or more rules.

---

### How can I negate an existing rule?

If you have a rule and want to check for the exact opposite, you can use **`negate()`**. You must provide a new error
key or `ErrorMessage` for the negated case.

```java
Rule<String> startsWithH = strings.startsWith("H");
Rule<String> mustNotStartWithH = startsWithH.negate("must.not.start.with.h");

mustNotStartWithH.apply("Hello"); // Invalid (must.not.start.with.h)
mustNotStartWithH.apply("World"); // Valid
```

---

### If a validation fails, can I provide a fallback value or another rule to try?

Yes! You can use **`fallback(other)`** on both `Rule` and `MappingRule`. This method allows you to specify a fallback
rule (or transformation) that is only executed if the initial one fails.

This is particularly useful for migration scenarios (e.g., trying to parse a new format, then falling back to an old
one) or for providing default values when an optional field is invalid.

#### Example: Recovering with a fallback rule

```java
Rule<String> primaryRule = strings.minLength(10);
Rule<String> fallbackRule = strings.startsWith("D");

Rule<String> combined = primaryRule.fallback(fallbackRule);

combined.apply("too short");         // Invalid (must start with D)
combined.apply("DEFAULT");           // Valid, too short but starts with D
combined.apply("long enough string"); // Valid
```

#### Example: Recovering with a transformation

If you are using `MappingRule`, you can use `fallback` to provide a fallback transformation:

```java
MappingRule<String, Integer> parseNew = strings.asInteger(); // parses "123" -> 123
MappingRule<String, Integer> parseOld = ...; // some other logic

MappingRule<String, Integer> rule = parseNew.fallback(parseOld);
```

> [!NOTE]
> The difference between `fallback` and `or()` is that `fallback` only returns the errors from the **fallback**
> rule if both fail, whereas `or()` combines the errors from both.

---

### How can I apply a rule only if a certain condition is met?

Sometimes you only want to validate something if a specific condition is true. The library provides several ways to do
this depending on what the condition is based on.

#### 1. Using `Rule.when(boolean, Rule)`

If you have a simple boolean flag or condition known at the time of validation, use `Rule.when`. If the condition is
`false`, the rule is skipped and the result is always `Valid`.

```java
boolean includeAdvancedChecks = ...;
Rule<String> rule = Rule.when(includeAdvancedChecks, strings.minLength(10));
```

#### 2. Using `Rule.choose(boolean, Rule, Rule)`

If you want to apply one rule if a condition is true, and another if it's false, use `Rule.choose`.

```java
boolean isNewUser = ...;
Rule<String> passwordRule = Rule.choose(isNewUser, strings.minLength(12), strings.minLength(8));
```

#### 3. Using `rule.onlyIf(Predicate<? super T>)`

If the condition depends on the **value being validated**, use `onlyIf` with a `Predicate`. The rule will only be
executed if the predicate matches the value.

```java
// Only check the length if the string starts with "A"
Rule<String> rule = strings.minLength(10).onlyIf(s -> s.startsWith("A"));
```

#### 4. Using `rule.onlyIf(Supplier<Boolean>)`

You can also use a `Supplier<Boolean>` for dynamic conditions that might change during the application lifecycle.

```java
Rule<String> rule = strings.minLength(10).onlyIf(() -> config.isValidationEnabled());
```

---

### If I have for example a Rule for Number, can I use it to also validate a subtype like BigDecimal?

Yes! Because `Rule<T>` is contravariant in its type parameter (meaning it can handle any subtype of `T`), you can use a
rule defined for a supertype to validate a subtype.

This is particularly useful for combining general rules with type-specific ones.

#### Example: Combining a Number rule with a BigDecimal rule

```java
Rule<Number> isPositive = Rule.of(n -> n.doubleValue() > 0, "must.be.positive");
Rule<BigDecimal> isMinusFortyTwo = Rule.of(b -> b.compareTo(new BigDecimal("-42")) == 0, "must.be.minus.forty.two");

Rule<BigDecimal> combined = isMinusFortyTwo.or(isPositive);
```

---

### I have a Rule for a certain type (e.g., Amount), and now I have another type Transaction that wraps Amount, can I reuse the Amount rule?

Yes, you can easily reuse rules for wrapped types or properties using `Rule.on(selector, rule)` or the more fluent
`rule.on(selector)`.

#### Using `Rule.on`

This static method is great for building rules from the outside:

```java
Rule<Amount> amountRule = AmountRules.isPositive();
Rule<Transaction> transactionRule = Rule.on(Transaction::getAmount, amountRule);
```

#### Using `rule.on`

This instance method allows you to "lift" an existing rule to work on a parent object:

```java
Rule<Amount> amountRule = AmountRules.isPositive();
Rule<Transaction> transactionRule = amountRule.on(Transaction::getAmount);
```

Both methods create a `Rule<Transaction>` that extracts the `Amount` from a `Transaction` and applies the `amountRule`
to it. If the validation fails, the error will be reported at the `amount` path (or whatever the selector represents).

---

### I want a single reusable rule that validates and transforms the same input in multiple ways — how?

Use `DSL.combine(rule1, rule2, ...).map(mapper)`. It takes multiple rule functions that all accept the same input type
`T`, accumulates their results (like `validating`), and combines the successful values via the mapper into a reusable
`MappingRule<T, R>`.

Unlike `validating(...)`, which evaluates immediately on values you already have, `combine(...)` builds a **reusable
rule** you can store and apply later:

```java
record PersonDto(String name, String age) {}
record Person(String name, int age) {}

// Define the rule once
MappingRule<PersonDto, Person> toPersonRule = combine(
        strings.minLength(3).on(PersonDto::name),
        strings.asInteger().then(ints.positive()).on(PersonDto::age)
).map(Person::new);

// Apply wherever needed
Validation<Person> result = toPersonRule.apply(dto);
```

If any rule fails, errors from all failing rules are accumulated into a single `Invalid`. See the
[`validating` vs `combine` section](#what-is-the-difference-between-validatethat-assertthat-validating-and-asserting)
for a full comparison.

---

### How do I transform the result of a MappingRule?

Use `MappingRule.map(Function)` to post-map the successful output type. This is useful for chaining transformations
after validation:

```java
MappingRule<String, Integer> doubled = strings.asInteger().map(i -> i * 2);

doubled.apply("5");   // Valid(10)
doubled.apply("abc"); // Invalid (must.be.integer)
```

You can also use `mapTo(constant)` to replace the successful result with a fixed value regardless of what the rule
produced.

---

### How do I get a standard Java `Predicate` from a `Rule`?

Use `rule.toPredicate()`. This bridges a `Rule<T>` into a standard Java `Predicate<T>`, which is useful when
integrating with APIs that expect a predicate (e.g., stream filters):

```java
Predicate<String> nonEmpty = strings.notEmpty().toPredicate();

List.of("hello", "", "world").stream()
        .filter(nonEmpty)
        .toList(); // ["hello", "world"]
```

`MappingRule` also has `toPredicate()`, returning `true` when the rule produces a `Valid` result.

---

## Containers: Optional, List, Set, Map

### How can I check that my optional value meets a Rule when it is not empty (but empty is also allowed)?

You can use the `lift().toOptional()` method (for `java.util.Optional`) or `lift().toOption()` (for Vavr `Option`).

These methods "lift" a rule that works on a single value to work on an optional container. If the container is empty,
the rule is considered **valid**. If it contains a value, the original rule is applied to that value.

```java
Rule<String> minLengthRule = strings.minLength(5);
Rule<Optional<String>> optionalRule = minLengthRule.lift().toOptional();

// or alternatively with the OptionalRules:
Rule<Optional<String>> fromOptionals = optionals.contains(strings.minLength(5));

optionalRule.apply(Optional.empty()); // Valid
optionalRule.apply(Optional.of("abc")); // Invalid (must be at least 5)
optionalRule.apply(Optional.of("abcdef")); // Valid
```

---

### How can I check that my optional value meets a Rule when it is not empty (but this time empty is NOT allowed)?

If you want to ensure that an `Optional` is **not empty** AND its value satisfies a certain rule, you can use the
`OptionalRules.required(MappingRule)` or `OptionRules.required(MappingRule)` methods.  
We need a MappingRule because we'll be changing the type of Validation from Optional<T> to T.

Unlike `lift().toOptional()`, which returns valid for empty optionals, these methods will return an `Invalid` result with
the error key `must.not.be.empty` if the optional is empty. If the optional contains a value, it applies the rule to
that value and **extracts** the value from the container.

```java
Rule<String> minLengthRule = strings.minLength(5);
MappingRule<Optional<String>, String> mandatoryAndMinLength = optionals.required(minLengthRule);

mandatoryRule.apply(Optional.empty()); // Invalid (must.not.be.empty)
mandatoryRule.apply(Optional.of("abc")); // Invalid (must be at least 5)
mandatoryRule.apply(Optional.of("abcdef")); // Valid("abcdef")
```

If you don't want to extract the value and prefer to keep working with a `Rule<Optional<T>>`, you can use
`optionals.contains(Rule<T> rule)`

```java
Rule<Optional<String>> mandatoryRule = optionals.contains(strings.length(5));

mandatoryRule.apply(Optional.empty()); // Invalid (must.not.be.empty)
mandatoryRule.apply(Optional.of("abc")); // Invalid (must be length 5)
mandatoryRule.apply(Optional.of("abcde")); // Valid(Optional.of("abcde"))
```

---

### I have a List of things, and I want to check that each entry meets a Rule

The easiest way is with the `validateThatList` dsl, you'd use something like:

```java
 Validation<List<Integer>> v = validateThatList(values, "values")
        .is(lists.notEmpty())
        .eachIs(ints.positive())
        .validate();
```

This guarantees you have a non-empty list of positive integers.

Working directly on a Rule, you can use the `lift().toList()` method (for `java.util.List`) or `lift().toVavrList()` (for Vavr
`List`).
When you lift a `Rule<T>`, you get a `Rule<List<T>>` that applies the original rule to each element. If any element
fails, the whole list is considered invalid, and the errors will include the index of the failing element (e.g.,
`[0].must.not.be.empty`).

```java
Rule<String> notEmpty = strings.notEmpty();
Rule<List<String>> allNotEmpty = notEmpty.lift().toList();

allNotEmpty.apply(List.of("abc", "")); // Invalid ([1].must.not.be.empty)
```

If you are using the `ListRules` or `VavrListRules` classes, you can also use `allMatchRule(Rule)`:

```java
Rule<List<String>> allNotEmpty = lists.allMatchRule(strings.notEmpty());
```

All these approaches are equivalent, as they are convenience wrappers around `lift().toList()`.

---

### Can I also validate Sets?

Yes! The `sets` namespace (for `java.util.Set`) and `vavrSets` (for Vavr `io.vavr.collection.Set`) offer the same
rules as `lists`: `notEmpty()`, `empty()`, `minSize(n)`, `maxSize(n)`, `sizeEquals(n)`, `sizeBetween(min, max)`,
`noNullElements()`, `allMatch(Predicate)`, `allMatchRule(Rule)`, `noneMatch(Predicate)`, `anyMatch(Predicate)`,
`contains(element)`, `containsAll(elements)`, `containsAnyOf(candidates)`, `uniqueBy(keyExtractor, label)`, and
`validateValuesWith(Rule)`.

```java
Rule<Set<String>> atLeastTwo = sets.minSize(2);
Rule<Set<String>> allNonBlank = sets.allMatchRule(strings.notBlank());

atLeastTwo.apply(Set.of("a"));       // Invalid (must.have.min.size)
atLeastTwo.apply(Set.of("a", "b"));  // Valid
```

> [!NOTE]
> If your `Set` does not have a fixed iteration order (e.g. a plain `HashSet`), the index in error path segments is
> non-deterministic across runs. Use a `LinkedHashSet` or `TreeSet` if you need stable error paths.

---

### Can I also validate Maps?

Yes! Similar to lists, you can lift a `Rule<T>` or `MappingRule<T, R>` to work on maps using `lift().toMap()` (for
`java.util.Map`) or `lift().toVavrMap()` (for Vavr `Map`).

When you lift a rule to a map, it applies the rule to every **value** in the map. The map **key** is used to create a
path segment for any error messages, so you can identify which entry failed.

```java
Rule<String> notEmpty = strings.notEmpty();
Rule<Map<String, String>> mapRule = notEmpty.lift().toMap();

Map<String, String> input = Map.of("key1", "value1", "key2", "");
mapRule.apply(input); // Invalid (key2.must.not.be.empty)
```

#### Customizing the Path Segment

By default, `lift().toMap()` uses `key.toString()` for the path. If your keys are complex objects, or you want a different
naming convention, you can provide a `keyExtractor` function:

```java
Rule<User> userRule = ...;
// Use the user's ID as the path segment in case of errors
Rule<Map<Long, User>> mapRule = userRule.lift().toMap(mapKey -> "user_" + mapKey);
mapRule.apply(Map.of(1, user1, 2,user2)); // Invalid (user_1.must.be...)
```

---

### I have a List<Validation<T>>, how can I turn it into a Validation<List<T>>?

When you have a collection of validations, and you want to combine them into a single validation containing a list of all
successful values (or all accumulated errors if any fail), you can use **`Validations.sequence()`**.

This operation is often called "sequence" in other functional programming libraries.

#### Example: Sequencing a List

```java
List<Validation<String>> validations = List.of(
        Validation.valid("A"),
        Validation.valid("B")
);

Validation<List<String>> result = Validations.sequence(validations);
// result is Valid(["A", "B"])
```

If any of the validations in the list are `Invalid`, the resulting validation will be `Invalid` and will contain **all**
the errors from all the invalid entries.

#### Sequencing Optionals and Options

The `sequence` method is also available for `java.util.Optional` and Vavr `Option`. It allows you to flip the
container:

* **`Optional<Validation<T>>`** → **`Validation<Optional<T>>`**
* **`Option<Validation<T>>`** → **`Validation<Option<T>>`**

This is useful when you have an optional validation step, and you want to treat an empty container as a successful
validation of "nothing".

```java
Optional<Validation<String>> optionalV = someOptional.map(this::validateContent);//returns a Validation.valid("hello")
Validation<Optional<String>> result = Validations.sequence(optionalV);
// result is Valid(Optional["hello"])

Optional<Validation<String>> emptyV = Optional.empty();
Validation<Optional<String>> resultEmpty = Validations.sequence(emptyV);
// resultEmpty is Valid(Optional.empty())
```

---

### I have a String, and want to make sure it's a valid value for a given Enum

You can use the `asEnum(Class<E> clazz)` method found in `StringRules`. This method returns a `MappingRule<String, E>`
that validates if the input string matches one of the enum constants (case-insensitive) and, if successful, transforms
it into that enum instance.

```java
enum Status {OPEN, CLOSED}

MappingRule<String, Status> rule = strings.asEnum(Status.class);

rule.apply("open");    // Valid(Status.OPEN)
rule.apply("UNKNOWN"); // Invalid (must.be.valid.enum.value)
```

If the validation fails, it uses the error key `must.be.valid.enum.value` and provides the invalid input as a parameter
named `value`.  

If you only want to check if the String represents a valid enum value, but keep the String, use `strings.canBeEnum`
instead.

#### Using a Custom Enum Provider

There's also a variant that takes an `enumProvider` function, allowing you to choose how to
look up an enum instance from the given string. This is useful when you want to look up an enum by a code,
a database ID, or any other property instead of its name.

```java
enum Status {
    OPEN("O"), 
    CLOSED("C");

    private final String code;
    Status(String code) { this.code = code; }
    
    public static Status fromCode(String code) {
        return Arrays.stream(values())
            .filter(s -> s.code.equalsIgnoreCase(code))
            .findFirst()
            .orElse(null); // Return null to trigger must.be.valid.enum.value
    }
}

MappingRule<String, Status> rule = strings.asEnum(Status.class, Status::fromCode);

rule.apply("O"); // Valid(Status.OPEN)
rule.apply("X"); // Invalid (must.be.valid.enum.value)
```

---

### How does this library work with standard Java collections vs Vavr collections?

The library is built on top of **Vavr**, but it provides excellent support for both standard Java types and Vavr types.

* **Inputs:** Most methods accept standard Java types (e.g., `java.util.List`, `java.util.Optional`).
* **Transformations:** You can lift rules to work on either type via `lift()`:
    * Use `lift().toList()` for `java.util.List` vs `lift().toVavrList()` for `io.vavr.collection.List`.
    * Use `lift().toOptional()` for `java.util.Optional` vs `lift().toOption()` for `io.vavr.control.Option`.
* **Results:** The library internally uses Vavr types for error collection (`io.vavr.collection.List<ErrorMessage>`). If
  you need a standard Java list of errors, you can use `javaErrors()`.

We recommend using Vavr collections in your domain logic where possible for better functional integration, but the
library does not force you to do so in your APIs.

---

### Can I validate Vavr `Either` values?

Yes! Use the `eithers()` method (note: it is a method, not a field, because it is generic over `L` and `R`). It
returns an `EitherRules<L, R>` with the following rules:

| Method                    | Behaviour                                                      |
|---------------------------|----------------------------------------------------------------|
| `isRight()`               | Fails if the Either is Left                                    |
| `isLeft()`                | Fails if the Either is Right                                   |
| `isRight(rule)`           | Fails if Left, or if Right but the right value fails the rule  |
| `isLeft(rule)`            | Fails if Right, or if Left but the left value fails the rule   |
| `validateRightWith(rule)` | Applies rule to the Right value; passes silently if it is Left |
| `validateLeftWith(rule)`  | Applies rule to the Left value; passes silently if it is Right |

```java
import static be.iffy.fv.dsl.DSL.*;
import io.vavr.control.Either;

Either<String, Integer> right = Either.right(42);
Either<String, Integer> left  = Either.left("error");

eithers().isRight().apply(right); // Valid
eithers().isRight().apply(left);  // Invalid (must.be.right)

Rule<Either<String, Integer>> positiveRight = eithers().isRight(ints.positive());
positiveRight.apply(Either.right(-1)); // Invalid (must.be.positive)
positiveRight.apply(Either.right(5));  // Valid
positiveRight.apply(Either.left("x")); // Invalid (must.be.right)
```

---

## Building Validated Objects

### How can I make sure my record or class is created with valid values?

There are two main approaches to ensure your objects are always in a valid state: validating **before** calling the
constructor or validating **inside** the constructor.

#### 1. Validating Before Creation (Materialization)

You can use `Validations.combine` to combine several independent validations and, if all pass, call your constructor or a
factory method. This is the recommended "functional" approach as it returns a `Validation<Target>` rather than throwing
an exception.

```java
record Person(String name, int age) {
}

// use the Rule API directly:
Validation<String> nameV = strings.minLength(3).apply(dto.name()).at("name");
Validation<Integer> ageV = ints.min(18).apply(dto.age()).at("age");
// or using the DSL
Validation<String> nameV = validateThat(dto.name(), Person::name).is(strings.minLength(3));
Validation<Integer> ageV = validateThat(dto.age(), Person::age).is(ints.min(18));

// now combine the Validations into a Person if both are Valid 
Validation<Person> personV = Validations.combine(nameV, ageV).map(Person::new);
```

There is an alternative syntax using `DSL` that looks like this:

```java
Validation<Person> personV = validating(
        validateThat(dto.name(), Person::name).is(strings.minLength(3)),
        validateThat(dto.age(), Person::age).is(ints.min(18))
).map(Person::new);
```

#### 2. Validating Inside the Constructor (Fail-Fast)

If you prefer your constructor to throw a `ValidationException` when given invalid values (common in DDD or when using
Java Records), you can use the `asserting` method from the `DSL` class.

```java
import static be.iffy.fv.dsl.DSL.*;

record Person(String name, int age) {
    public Person {
        asserting(
                validateThat(name, "name").is(strings.minLength(3)),
                validateThat(age, "age").is(ints.min(18))
        );
    }
}
```

If any of the validations fail, `asserting` throws a `ValidationException` containing all the collected error
messages.

---

### In my constructor, I want to be liberal with my input, and only validate the value after changing it

You can use `assertThat(value,"field").after(transformation)` to transform a value before applying rules to it. This is
useful when you want to normalize input (like trimming strings or converting case) and then validate the result.

#### Example: Trimming and checking length

```java
import static be.iffy.fv.dsl.DSL.*;

record Username(String value) {
    public Username {
        // assertThat returns the valid value or throws a ValidationException otherwise
        value = assertThat(value, "value").after(stringOps.trim()).is(strings.minLength(3));
    }
}
```

In this example:

1. The input `value` is trimmed.
2. The trimmed value is checked against `minLength(3)`.
3. If it fails, a `ValidationException` is thrown.
4. If it succeeds, `assertThat` returns the **trimmed** value, which is then assigned to the field.
5. If the input value was null, the String::trim method would have never been called.

Note: it is important that the Transformation you pass to after is null-safe and does not fail. It is allowed to return null.

---

### Ok, but I want to transform multiple fields in my constructor, how do I get their transformed values?

When you have multiple fields that need transformation and validation, you can use **`asserting`** with multiple
arguments. It will return a Vavr **`Tuple`** containing all the transformed values if they are all valid, or throw a
`ValidationException` with all accumulated errors.
The assignment doesn't look super nice, but it's the best we can do without java having explicit support for something
like tuple assignment.  
You can also still use multiple seperate `x = assertThat(...)` statements, but then you lose error accumulation.

#### Example: Normalizing multiple fields

```java
import static be.iffy.fv.dsl.DSL.*;

import io.vavr.Tuple2;

record User(String username, String email) {
    public User {
        // using a var makes this better :)
        Tuple2<String, String> values = asserting(
                validateThat(username, "username").after(stringOps.trim()).is(strings.minLength(3)),
                validateThat(email, "email").after(stringOps.toLowerCase()).is(strings.looksLikeEmailAddress())
        );

        username = values._1; // is trimmed
        email = values._2; // is lowercased
    }
}
```

In this case:

1. Both `username` and `email` are transformed (trimmed and lowercased respectively).
2. The rules are applied to the transformed values.
3. If any check fails, a `ValidationException` is thrown containing all errors.
4. If all succeed, a `Tuple2` is returned containing the trimmed username and the lowercased email.

The library supports `asserting` for up to 8 validations, returning `Tuple2` through `Tuple8`.

---

### Ok, but can I do the same when defining a Rule?

Yes! If you want to create a reusable `Rule` that includes a transformation step, you can use the **`after()`** syntax.

#### Example: A rule that trims and then checks length

```java
import static be.iffy.fv.dsl.DSL.*;

Rule<String> trimmedMinLength3 = after(stringOps.trim()).is(strings.minLength(3));
```

This is very similar to the `validateThat(value).after(...)` syntax used in constructors, but it allows you to package the
transformation and the validation into a single `Rule` object.

#### Technical Note: Rule vs MappingRule

Technically, any rule that transforms its input (like one using `after`) should be a `MappingRule`. However, the
`after(...).is(...)` DSL returns a **`Rule`** for convenience.

This allows you to use the resulting rule anywhere a standard `Rule` is expected, while still benefiting from the
internal transformation. If you need to expose the transformed value to the rest of a validation chain, you would
typically use `MappingRule` explicitly.
Using a MappingRule for this case would look something like this:

```java
MappingRule<String, String> trimmedMinLength3 = MappingRule.catching(String::trim, "can.not.fail").then(minLength);
```

---


### How do I perform cross-field validation where one field's validation depends on another?

For cross-field validation, you typically have two options:

#### 1. Validating at the Object Level

Create a `Rule` for the object itself that looks at multiple fields.

```java
Rule<Period> validPeriod = Rule.of(
        p -> p.getStart().isBefore(p.getEnd()),
        ErrorMessage.of("start.must.be.before.end", "end")
);

Validation<Period> v = validateThat(period).is(validPeriod);
```

#### 2. Using `flatMap` or `refine` on the result of `Validations.combine` or `DSL.validating`

After combining multiple validated fields, you can apply an additional check on the resulting object:

```java
LocalDate start = LocalDate.of(2026,1,30);
LocalDate end = LocalDate.of(2026,1,29);

Validation<LocalDate> result = validating(
  validateThat(start).isNotNull(),
  validateThat(end).isNotNull()
).flatMap((s, e) ->
  validateThat(s,"start").is(localDates.isBefore(end))
); 
// result is an Invalid with "start.must.be.before:{limit:2026-01-29}"
```

---

### What is the difference between `validateThat`, `assertThat`, `validating`, and `asserting`?

These four DSL entry points cover two axes: **single value vs. combined** and **functional (returns `Validation`) vs. asserting (throws on failure)**.

| Method                      | Takes                | Returns              | Throws? |
|-----------------------------|----------------------|----------------------|---------|
| `validateThat(value, name)` | a single value       | `Validation<T>`      | no      |
| `assertThat(value, name)`   | a single value       | the value `T`        | yes     |
| `validating(v1, v2, …)`     | `Validation` objects | `Validation<mapped>` | no      |
| `asserting(v1, v2, …)`      | `Validation` objects | `Tuple` of values    | yes     |

#### `validateThat` — functional, single value

Use when you want to validate one value and keep the result as a `Validation` object for later combination.
Returns `Validation<T>`; never throws.

```java
Validation<String> nameV = validateThat(dto.name(), Person::name).is(strings.minLength(3));
```

#### `assertThat` — throwing, single value

Use inside a constructor (or any code that should fail-fast) when you only have one field to validate, optionally
with a transformation step via `.after(...)`.
Returns the (possibly transformed) value `T` directly, or throws `ValidationException`.

```java
value = assertThat(value, "value").after(stringOps.trim()).is(strings.minLength(3));
```

#### `validating` — functional, multiple values

Use to combine several `Validation` objects into one without throwing. Errors from all fields are accumulated.
`.map(…)` on the result builds the target object if all validations succeed.
Returns `Validation<MappedType>`; never throws.

```java
Validation<Person> personV = validating(
        validateThat(dto.name(), Person::name).is(strings.minLength(3)),
        validateThat(dto.age(), Person::age).is(ints.min(18))
).map(Person::new);
```

#### `asserting` — throwing, multiple values

Use inside a constructor to validate multiple fields at once with full error accumulation. Returns a `Tuple` of the
(possibly transformed) values; throws `ValidationException` with **all** errors if any validation fails.

```java
Tuple2<String, String> t = asserting(
        validateThat(username, "username").after(stringOps.trim()).is(strings.minLength(3)),
        validateThat(email, "email").after(stringOps.toLowerCase()).is(strings.email())
);
username = t._1;
email = t._2;
```

**Rule of thumb:** prefer the `validating` / `validateThat` pair when you want a purely functional result
(`Validation`). Use `asserting` / `assertThat` when you are inside a constructor and want the library to throw for
you.

---

## Exception Interop

### I have some type whose constructor throws an exception, how can I make a Validation for this type?

If you want to validate a type constructed using a method or constructor that can throw an exception (checked or
unchecked), you can use Vavr's **`Try`** in combination with **`Validation.from()._try(...)`**.

This allows you to wrap the potentially failing construction in a `Try`, and then convert it into a `Validation` object
which fits perfectly into the rest of the library's ecosystem.

#### Example: Validating a URL

Since `new URL(String)` throws a checked `MalformedURLException`, it's a perfect candidate for this approach.

```java
import be.iffy.fv.Validation;
import io.vavr.control.Try;

import java.net.URL;

public Validation<URL> validateUrl(String input) {
    return Validation.from()._try(
            Try.of(() -> new URL(input)),
            "invalid.url" // Error key to use if Try fails
    );
}
```

Note: there's a built-in MappingRule<String, URL> asURL() in StringRules for this.

In this example:

1. `Try.of(...)` attempts to create the `URL`. If an exception is thrown, it captures it in a `Failure` state.
2. `Validation.from()._try(tryResult, "invalid.url")` converts the `Try` into a `Validation`.
3. If the `Try` was a `Success`, you get a `Valid<URL>`.
4. If the `Try` was a `Failure`, you get an `Invalid` result with the error key `"invalid.url"`.

#### When the constructor throws `ValidationException`

If the constructor you're calling is itself a validated domain object (i.e. it throws `ValidationException` on
bad input), you can use the simpler `Validation.catching(supplier)` shorthand:

```java
// Username constructor uses assertThat/asserting internally — throws ValidationException on invalid input
Validation<Username> u = Validation.catching(() -> new Username(rawInput));
```

This is equivalent to `Validation.from().catching(supplier)` but without the `from()` call.

---

### What types can I turn into a Validation?

The library can't anticipate every type you'll need to validate, so `Validation.from()` returns a **`ValidationFactory`**
that converts other common "result" types into a `Validation`. This is the main entry point for interoperability with
code that doesn't already speak in `Validation`s.

`ValidationFactory` currently supports:

* **`Supplier<T>`** via `catching(...)` and `catchingAll(...)` — for code that throws exceptions instead of returning a result type.
* **`Try<T>`** via `_try(...)` — for code that already returns a Vavr `Try`.
* **`Option<T>`** via `option(...)` — for Vavr's optional type.
* **`Optional<T>`** via `optional(...)` — for the standard Java optional type.
* **`Either<L, R>`** via `either(...)` — for Vavr's either type, converting the `Left` into an `ErrorMessage`.

#### `catching` vs `catchingAll` vs `_try`

These three are easy to confuse because they all deal with "things that might fail", but they differ in what they accept and what they catch:

* **`catching(Supplier<T>)`** takes a `Supplier` and runs it immediately. If it throws a `ValidationException`, that
  exception's errors become the `Invalid` result. Any *other* exception (including a `NullPointerException` from a
  `null` result) is **not caught** and propagates to the caller. Use this when you trust the supplier to either
  succeed, return non-null, or throw `ValidationException` on purpose. Because this is the most common case when
  constructing `Validation`s manually (e.g. calling a validated domain-object constructor), it is also available
  directly as `Validation.catching(supplier)` — a shorthand for `Validation.from().catching(supplier)`.
* **`catchingAll(Supplier<T>, ...)`** also takes a `Supplier`, but catches *any* `Exception` (not just
  `ValidationException`) and converts it into an `Invalid`, using either a fixed `ErrorMessage`/error key or a
  `Function<Exception, ErrorMessage>` you provide. This is the one to reach for when calling into code you don't
  control and don't want a stray exception to escape as a Java exception. It's also the most dangerous of the three,
  since it can silently swallow bugs like an unexpected `NullPointerException` — only use it when you're sure you want
  every exception treated as a validation failure.
* **`_try(Try<T>, ...)`** doesn't run anything itself — you hand it an already-evaluated Vavr `Try`. If the `Try` is a
  `Failure` wrapping a `ValidationException`, its errors are preserved; otherwise the provided `ErrorMessage`/error key
  is used. There's also a no-argument overload, `_try(Try<T>)`, which uses the error key `failed.from.try` (with a
  `message` parameter) for non-`ValidationException` failures, and treats a `Try` that succeeds with `null` as invalid
  with `"must.not.be.null"`. Prefer `_try` when you're working with an API that already returns `Try`, or when you want
  to build the `Try` yourself (e.g. with `Try.of(...)`) so you control exactly what gets caught.

```java
import be.iffy.fv.Validation;
import io.vavr.control.Try;

// catching: only ValidationException is converted, everything else propagates
Validation<Integer> a = Validation.from().catching(() -> Integer.parseInt(input));

// catchingAll: every Exception becomes an Invalid with the given error key
Validation<Integer> b = Validation.from().catchingAll(() -> Integer.parseInt(input), "invalid.number");

// _try: you build the Try yourself, then convert it
Validation<Integer> c = Validation.from()._try(Try.of(() -> Integer.parseInt(input)), "invalid.number");
```

See also [I have some type whose constructor throws an exception, how can I make a Validation for this
type?](#i-have-some-type-whose-constructor-throws-an-exception-how-can-i-make-a-validation-for-this-type) and
[What's the difference between methods like map and mapCatching? What does catchingAll
mean?](#whats-the-difference-between-methods-like-map-and-mapcatching-what-does-catchingall-mean) for more on these
exception-catching variants.

---

### What's the difference between methods like map and mapCatching? What does catchingAll mean?

The standard **`map`** and **`flatMap`** methods follow pure functional semantics: they expect the mapping function to
be successful. If the function throws an exception (even a `ValidationException`), it will propagate up the call stack.

#### `mapCatching` and `flatMapCatching`

These methods are specifically designed to bridge the gap between "fail-fast" validation (which throws
`ValidationException`) and "accumulating" validation (which returns `Validation` objects).

If the function passed to `mapCatching` or `flatMapCatching` throws a **`ValidationException`**, the exception is
caught, and its errors are automatically converted into an `Invalid` result. This allows you to use methods that throw
exceptions inside a functional chain without manually catching them.

```java
Validation<User> userV = validateThat(dto).is(userRule);

// If User constructor throws ValidationException, mapCatching captures it
Validation<ProcessedUser> processedV = userV.mapCatching(user -> new ProcessedUser(user));
```

> [!NOTE]
> Other exceptions (like `RuntimeException` or `NullPointerException`) are **not** caught by these methods and will
> still be rethrown.

#### `Validation.from().catchingAll(...)` and `flatMapCatchingAll`

If you need to catch **any** exception (not just `ValidationException`), you can use **`Validation.from().catchingAll(...)`**
or **`flatMapCatchingAll`** (instance method).

These methods take an additional error message (or an error key) to use when an unexpected `RuntimeException` occurs.

- If the operation succeeds, it returns `Valid`.
- If a `ValidationException` is thrown, it returns `Invalid` with the errors from the exception.
- If any other `RuntimeException` is thrown, it returns `Invalid` with the provided fallback error message.

```java
// Using the factory method to create a Validation from a throwing supplier
Validation<URL> urlV = Validation.from().catchingAll(
                () -> new URL(inputString),
                "invalid.url"
        );

// Using the instance method to chain a potentially throwing operation
Validation<Integer> result = someValidation.flatMapCatchingAll(
        s -> Validation.valid(Integer.parseInt(s)),
        ErrorMessage.of("must.be.a.number")
);
```

#### `MappingRule.fromTry` and `MappingRule.catching`

Similar to `mapCatching`, you can create rules that handle exceptions using `MappingRule.fromTry` (for functions returning
a Vavr `Try`) or `MappingRule.catching(throwingMapper, errorMessage)` (for functions that might throw an exception).

These are useful when you want to encapsulate the exception handling logic directly inside a reusable `Rule` or
`MappingRule`.

---

## Working with Validation Results

### I have a Validation, but I want to add an extra check on the value

If you already have a `Validation<T>` object and you want to apply an additional `Rule<T>` or `MappingRule<T, R>` to its value (if it's valid),
you can use the **`refine()`** method.

This is particularly useful when you've already performed some initial validation or transformation and want to "refine"
the result with further constraints.

#### Example: Refining a validation

```java
import be.iffy.fv.Validation;

import static be.iffy.fv.rules.text.StringRules.strings;

Validation<String> initialValidation = ...;

// Only if initialValidation is Valid, check if the string is also an email
Validation<String> refinedValidation = initialValidation.refine(strings.looksLikeEmailAddress());
```

In this case:

1. If `initialValidation` is `Invalid`, `refine()` does nothing and returns the original `Invalid` result.
2. If `initialValidation` is `Valid`, the `strings.email()` rule is applied to the value.
3. If the email rule passes, you get a `Valid` result with the original value.
4. If the email rule fails, you get an `Invalid` result containing the error from the email rule.

#### Filtering with predicates

If you just want to check a simple condition without creating a full `Rule` object, you can also use **`filter()`**,
which uses `refine` internally:

```java
Validation<String> filtered = initialValidation.filter(
        s -> s.startsWith("A"),
        "must.start.with.A"
);
```

---

### I want to perform a side effect (like logging) only if a validation is successful.

You can use the **`peek()`** method on a `Validation` object. It allows you to provide a `Consumer` that will be
executed only if the validation is `Valid`. The `peek()` method returns the original `Validation` object, so you can
continue the chain.

```java
Validation<User> result = validateUser(dto)
        .peek(user -> logger.info("Successfully validated user: {}", user.getId()))
        .refine(extraSecurityCheck);
```

If you want to perform actions in both cases (success and failure), you can use `whenValid(Consumer)` and
`whenInvalid(Consumer)`.

---

### How do I consume a Validation result into a plain value or handle both branches?

There are several ways to extract a value or act on the result of a `Validation<T>`.

#### `fold(whenInvalid, whenValid)` — handle both branches in one call

`fold` takes a function for the invalid case and a function for the valid case, and always returns a plain value `R`:

```java
String message = validateThat(input, "name").is(strings.minLength(3))
        .fold(
                errors -> "Validation failed: " + errors.map(ErrorMessage::errorKey).mkString(", "),
                name   -> "Hello, " + name + "!"
        );
```

#### `getOrElse(default)` — unwrap with a fallback

Returns the valid value, or the provided default if invalid:

```java
String value = validateThat(input, "name").is(strings.notEmpty())
        .getOrElse("anonymous");
```

#### `orElse(otherValidation)` — substitute another Validation on failure

If the current Validation is `Invalid`, `orElse` returns the alternative `Validation` instead:

```java
Validation<String> result = validateThat(input, "name").is(strings.notBlank())
        .orElse(Validation.valid("default"));
```

#### `toOptional()` / `toOption()` — convert to Optional / Option

Returns `Optional.of(value)` / `Option.of(value)` on success, and `Optional.empty()` / `Option.none()` on failure:

```java
Optional<String> opt = validateThat(input, "name").is(strings.notEmpty()).toOptional();
opt.ifPresent(name -> System.out.println("Got: " + name));
```

---

## Time and Date Validation

### What date and time rule namespaces are available?

The DSL exposes a rule namespace for each major `java.time` type:

| Namespace         | Validates                  |
|-------------------|----------------------------|
| `localDates`      | `java.time.LocalDate`      |
| `localDateTimes`  | `java.time.LocalDateTime`  |
| `localTimes`      | `java.time.LocalTime`      |
| `offsetDateTimes` | `java.time.OffsetDateTime` |
| `offsetTimes`     | `java.time.OffsetTime`     |
| `zonedDateTimes`  | `java.time.ZonedDateTime`  |
| `instants`        | `java.time.Instant`        |
| `years`           | `java.time.Year`           |
| `yearMonths`      | `java.time.YearMonth`      |
| `durations`       | `java.time.Duration`       |

All namespaces provide the standard comparable operations shared with the numeric rules: `between(min, max)`,
`betweenExclusive(min, max)`, `greaterThan(limit)`, `atLeast(limit)`, `lessThan(limit)`, `atMost(limit)`.

For types that have a notion of "now" (`localDates`, `localDateTimes`, `offsetDateTimes`, `offsetTimes`, `zonedDateTimes`, `instants`) you also get
temporal checks:

```java
localDates.isBefore(LocalDate.of(2030, 1, 1))   // must.be.before
localDates.isAfter(LocalDate.of(2000, 1, 1))    // must.be.after
localDates.isPast()                              // must be before today
localDates.isFuture()                            // must be after today
localDates.isToday()                             // must equal today (localDates only)
localDates.isLeapYear()                          // must be a leap year (localDates only)
```

---

### How do I control "now" in time rules (e.g., in tests)?

By default, the singleton fields (`localDates`, `localDateTimes`, `instants`, etc.) use the JVM system clock. To
override "now" — useful in tests — call the static factory method with a `java.time.Clock`:

```java
Clock fixed = Clock.fixed(Instant.parse("2024-01-15T12:00:00Z"), ZoneOffset.UTC);

LocalDateRules testDates = LocalDateRules.localDates(fixed);
// Same pattern for the other namespaces:
// LocalDateTimeRules.localDateTimes(fixed), InstantRules.instants(fixed), etc.

Rule<LocalDate> mustBePast = testDates.isPast();
mustBePast.apply(LocalDate.of(2023, 6, 1)); // Valid   (before 2024-01-15)
mustBePast.apply(LocalDate.of(2025, 1, 1)); // Invalid (after 2024-01-15)
```

---

## String Transformations (`stringOps`)

### What string transformations are available to use with `after()`?

The `stringOps` namespace contains pre-built `Transformation<String>` functions. They are all null-safe: a `null`
input passes through as `null` without throwing.

| Method                            | Example                                                                                            |
|-----------------------------------|----------------------------------------------------------------------------------------------------|
| `trim()`                          | `" hello " → "hello"`                                                                              |
| `stripNewlines()`                 | `"hello\nworld" → "hello world"`                                                                   |
| `collapseWhitespace()`            | `" a \n\t b" → " a b"`                                                                             |
| `normalizeSpace()`                | `"  a \n\t b  " → "a b"`                                                                           |
| `keep(CharCategory...)`           | `keep(ASCII_DIGITS).apply("abc123") → "123"`, `keep(LETTERS, SPACE).apply("Hello 42!") → "Hello "` |
| `strip(CharCategory...)`          | `strip(ASCII_DIGITS).apply("abc123") → "abc"`, `strip(WHITESPACE).apply(" a b ") → "ab"`           |
| `toLowercase()`                   | `"HeLLo" → "hello"` (Locale.ROOT)                                                                  |
| `toLowercase(locale)`             | locale-aware lowercase                                                                             |
| `toUppercase()`                   | `"HeLLo" → "HELLO"` (Locale.ROOT)                                                                  |
| `toUppercase(locale)`             | locale-aware uppercase                                                                             |
| `removeCharacters(chars)`         | `removeCharacters("-").apply("a-b-c") → "abc"`                                                     |
| `replaceAll(regex, replacement)`  | standard regex replacement                                                                         |
| `keepChars(allowed)`              | `keepChars("abc").apply("xaxbxc") → "abc"`                                                         |
| `stripDiacritics()`               | `"Café" → "Cafe"`                                                                                  |
| `stripControlChars()`             | removes control chars, zero-width spaces, BOM, etc.                                                |
| `truncate(maxLen)`                | hard cut, surrogate-pair safe                                                                      |
| `truncateWithEllipsis(maxLen)`    | cut + append `…`                                                                                   |

Available `CharCategory` values: `ASCII_DIGITS`, `ASCII_LETTERS`, `ASCII_WHITESPACE`, `DIGITS`, `LETTERS`, `MARKS`, `ASCII_PUNCTUATION`, `PUNCTUATION`, `SPACE`, `WHITESPACE`.

Use them with `after()` in both the DSL and when defining reusable rules:

```java
// Inline in a constructor
value = assertThat(value, "value")
        .after(stringOps.normalizeSpace())
        .is(strings.minLength(3));

// As a reusable Rule
MappingRule<String,String> cleanName = after(stringOps.normalizeSpace()).is(strings.minLength(3));
```

#### Using your own transformation functions

`Transformation<T>` is just a `@FunctionalInterface` — any method reference or lambda that takes a `T` and returns a
`T` works. The only requirement is that it is **null-safe**: when the input is `null`, the function must return `null`
rather than throw (null-checking is handled by the rule that follows, not by the transformation).

For example, most methods in Apache Commons Lang's `StringUtils` are already null-safe and slot in directly:

```java
import org.apache.commons.lang3.StringUtils;

value = assertThat(value, "value")
        .after(StringUtils::stripAccents)
        .is(strings.minLength(3));

MappingRule<String,String> abbreviate = after(s -> StringUtils.abbreviate(s, 20)).is(strings.notBlank());
```

#### Combining multiple transformations

You can apply several transformations in sequence using the `after()` varargs overload, `Transformation.sequence()`, or the `andThen()` method:

```java
// Varargs in the DSL (most common)
value = assertThat(value, "value")
        .after(stringOps.normalizeSpace(), stringOps.toLowercase())
        .is(strings.minLength(3));

// Build a reusable composed Transformation
Transformation<String> normalize = Transformation.sequence(
    stringOps.normalizeSpace(),
    stringOps.toLowercase(),
    StringUtils::stripAccents
);

// Or chain with andThen
Transformation<String> normalize = stringOps.normalizeSpace()
        .andThen(stringOps.toLowercase());
```

Null-safety is preserved: if the input is `null`, all transformations pass `null` through without throwing.

---

## Testing

### How do I write clean assertions on Validation results in tests?

Add the `assertj` module to your test dependencies:

```xml
<dependency>
    <groupId>be.iffy.fv</groupId>
    <artifactId>assertj</artifactId>
    <version>...</version>
    <scope>test</scope>
</dependency>
```

Then static-import the entry points:

```java
import static be.iffy.fv.assertj.ValidationAssert.*;
```

#### Asserting a valid result

`assertThatValidation(v).isValid()` returns an AssertJ `ObjectAssert<T>` so you can chain further checks on the
unwrapped value:

```java
assertThatValidation(strings.minLength(3).apply("hello"))
        .isValid()
        .isEqualTo("hello");
```

Or use the static shorthand `assertValid(v)` which goes straight to the `ObjectAssert<T>`.

#### Asserting an invalid result

`assertThatValidation(v).isInvalid()` returns an `InvalidValidationAssert` with dedicated helpers:

```java
assertThatValidation(strings.minLength(5).apply("hi"))
        .isInvalid()
        .hasErrorKeys("must.have.min.length")
        .hasErrorCount(1);
```

Available methods on `InvalidValidationAssert`:

| Method                        | Description                                                    |
|-------------------------------|----------------------------------------------------------------|
| `hasErrorKeys(String...)`     | checks that the given error keys are present                   |
| `hasErrorMessages(String...)` | checks full path-qualified error messages                      |
| `hasErrorCount(int)`          | exact number of errors                                         |
| `hasFormattedMessage(String)` | checks a specific `formatted()` string                         |
| `errorKeys()`                 | returns `ListAssert<String>` for custom assertions on keys     |
| `errorMessages()`             | returns `ListAssert<String>` for custom assertions on messages |
| `formattedMessages()`         | returns `ListAssert<String>` on `formatted()` strings          |
| `errors()`                    | returns `ListAssert<ErrorMessage>` for full control            |

#### Asserting that a constructor throws `ValidationException`

When the code under test uses `assertThat` / `asserting` internally and throws on bad input:

```java
assertInvalid(() -> new Username(""))
        .hasErrorKeys("must.not.be.empty");
```

---

## Pattern Matching

### Can I use Java pattern matching (switch) on a Validation?

Yes. `Validation<T>` is a `sealed interface` with exactly two implementations — `Validation.Valid<T>` and
`Validation.Invalid<T>` — both of which are Java `record`s. Java 21 pattern-matching switch works directly and the
compiler knows the two cases are exhaustive, so no `default` branch is needed:

```java
String message = switch (validation) {
    case Validation.Valid<String>(var value) ->
            "Hello, " + value + "!";
    case Validation.Invalid<String>(var errors) ->
            "Errors: " + errors.map(ErrorMessage::errorKey).mkString(", ");
};
```

You can also use `instanceof` for a simple type check without deconstruction:

```java
if (validation instanceof Validation.Valid<String> v) {
    process(v.value());
}
```
