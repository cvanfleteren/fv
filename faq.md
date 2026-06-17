# Frequently Asked Questions (FAQ)

Welcome to the FAQ for the Iffy Functional Validation (FV) library. If you have a question not answered here, please
feel free to open an issue or reach out to the maintainers.

### Table of Contents

**Core Concepts**
- [What is the difference between a `Rule` and a `MappingRule`?](#what-is-the-difference-between-a-rule-and-a-mappingrule)
- [Whats with the Function<? super T, ? extends Validation<R>> signatures?](#whats-with-the-function-super-t--extends-validationr-signatures)
- [Do I need to use Strings to name the values I'm validating?](#do-i-need-to-use-strings-to-name-the-values-im-validating)
- [Are rules null-safe by default?](#are-rules-null-safe-by-default)

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

**Containers: Optional, List, Map**
- [How can I check that my optional value meets a Rule when it is not empty (but empty is also allowed)?](#how-can-i-check-that-my-optional-value-meets-a-rule-when-it-is-not-empty-but-empty-is-also-allowed)
- [How can I check that my optional value meets a Rule when it is not empty (but this time empty is NOT allowed)?](#how-can-i-check-that-my-optional-value-meets-a-rule-when-it-is-not-empty-but-this-time-empty-is-not-allowed)
- [I have a List of things, and I want to check that each entry meets a Rule](#i-have-a-list-of-things-and-i-want-to-check-that-each-entry-meets-a-rule)
- [Can I also validate Maps?](#can-i-also-validate-maps)
- [I have a List<Validation<T>>, how can I turn it into a Validation<List<T>>?](#i-have-a-listvalidationt-how-can-i-turn-it-into-a-validationlistt)
- [I have a String, and want to make sure it's a valid value for a given Enum](#i-have-a-string-and-want-to-make-sure-its-a-valid-value-for-a-given-enum)
- [How does this library work with standard Java collections vs Vavr collections?](#how-does-this-library-work-with-standard-java-collections-vs-vavr-collections)

**Building Validated Objects**
- [How can I make sure my record or class is created with valid values?](#how-can-i-make-sure-my-record-or-class-is-created-with-valid-values)
- [In my constructor, I want to be liberal with my input, and only validate the value after changing it](#in-my-constructor-i-want-to-be-liberal-with-my-input-and-only-validate-the-value-after-changing-it)
- [Ok, but I want to transform multiple fields in my constructor, how do I get their transformed values?](#ok-but-i-want-to-transform-multiple-fields-in-my-constructor-how-do-i-get-their-transformed-values)
- [Ok, but can I do the same when defining a Rule?](#ok-but-can-i-do-the-same-when-defining-a-rule)

**Exception Interop**
- [I have some type whose constructor throws an exception, how can I make a Validation for this type?](#i-have-some-type-whose-constructor-throws-an-exception-how-can-i-make-a-validation-for-this-type)
- [What types can I turn into a Validation?](#what-types-can-i-turn-into-a-validation)
- [What's the difference between methods like map and mapCatching? What does catchingAll mean?](#whats-the-difference-between-methods-like-map-and-mapcatching-what-does-catchingall-mean)

**Working with Validation Results**
- [I have a Validation, but I want to add an extra check on the value](#i-have-a-validation-but-i-want-to-add-an-extra-check-on-the-value)
- [I want to perform a side effect (like logging) only if a validation is successful.](#i-want-to-perform-a-side-effect-like-logging-only-if-a-validation-is-successful)
- [How do I perform cross-field validation where one field's validation depends on another?](#how-do-i-perform-cross-field-validation-where-one-field-s-validation-depends-on-another)

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

You'll notice that lots of methods take functions with a signature like `Function<? super T, ? extends Validation<R>>`.
This is the generalized signature for both `Rule` and `MappingRule`, but for Rule T and R are the same type.

---

### Whats with the Function<? super T, ? extends Validation<R>> signatures?

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

| Method                 | Behavior                                                                        | Short-circuiting | Error handling                                |
|------------------------|:--------------------------------------------------------------------------------|------------------|-----------------------------------------------|
| and(other)             | AND; runs other only if this is valid                                           | Yes              | Not accumulating                              |
| andAlso(other)         | AND; always runs both                                                           | No               | Accumulating (combine errors)                 |
| all(rules...)          | AND over many; all must pass                                                    | No               | Accumulating (combine all errors)             |
| any(rules...)          | OR over many; succeeds on first rule that passes                                | Yes              | Accumulating if all fail                      |
| fallback(other)        | Fallback; uses other only if this fails; if both fail, keep only other's errors | Yes              | Not accumulating (only fallback's errors)     |
| or(other)              | OR; uses other only if this fails; if both fail, combine errors                 | Yes              | Accumulating                                  |
| then(ruleLikeFunction) | On success, refine into a MappingRule via ruleLikeFunction                      | Yes              | Not accumulating                              |
| xor(other, errorKey)   | Exactly one must pass; evaluates both                                           | No               | Non-accumulating (single errorKey on failure) |

#### MappingRule combinators

| Method             | Behavior                                                                              | Short-circuiting | Error handling                            |
|--------------------|---------------------------------------------------------------------------------------|------------------|-------------------------------------------|
| fallback(fallback) | Fallback; uses fallback only if this fails; if both fail, keep only fallback's errors | Yes              | Not accumulating (only fallback's errors) |
| then(rule)         | AND in sequence; applies rule to successful mapped result                             | Yes              | Not accumulating                          |
| or(other)          | OR; uses other only if this fails; if both fail, combine errors                       | Yes              | Accumulating                              |
| combine(other)     | Start builder to combine multiple MappingRules                                        | No               | Accumulating across combined results      |

#### What is the difference between `and()`, `andAlso()`, and `Rule.all()`?

These three all express "both rules must pass", but they differ in execution flow and error collection:

1. **`ruleA.and(ruleB)` (Short-circuiting):**
    * If `ruleA` fails, `ruleB` is **not executed**.
    * The result contains only the errors from `ruleA`.
    * Use this when `ruleB` depends on `ruleA` (e.g., `notNull().and(minLength(5))`).

2. **`ruleA.andAlso(ruleB)` (Non-short-circuiting):**
    * Both rules are **always executed**.
    * If both fail, the result contains **all errors** from both.
    * Use this when you want to report as many problems as possible to the user at once.

3. **`Rule.all(ruleA, ruleB, ...)`:**
    * Similar to `andAlso()`, it executes all rules and collects all errors.
    * It is often more readable when combining more than two rules.

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
MappingRule<String, Integer> parseNew = strings.asInt(); // parses "123" -> 123
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

## Containers: Optional, List, Map

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

By default, `lift().toMap()` uses `key.toString()` for the path. If your keys are complex objects or you want a different
naming convention, you can provide a `keyExtractor` function:

```java
Rule<User> userRule = ...;
// Use the user's ID as the path segment in case of errors
Rule<Map<Long, User>> mapRule = userRule.lift().toMap(mapKey -> "user_" + mapKey);
mapRule.apply(Map.of(1, user1, 2,user2)); // Invalid (user_1.must.be...)
```

---

### I have a List<Validation<T>>, how can I turn it into a Validation<List<T>>?

When you have a collection of validations and you want to combine them into a single validation containing a list of all
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

This is useful when you have an optional validation step and you want to treat an empty container as a successful
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
lookup an enum instance from the given string. This is useful when you want to lookup an enum by a code,
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
                validateThat(email, "email").after(stringOps.toLowerCase()).is(strings.email())
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
  succeed, return non-null, or throw `ValidationException` on purpose.
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

#### 2. Using `flatMap` or `refine` on the result of `Validations.combine`

After combining multiple validated fields, you can apply an additional check on the resulting object.

```java
Validation<Period> periodV = Validations.combine(startV, endV).map(Period::new)
        .refine(Rule.of(p -> p.getStart().isBefore(p.getEnd()), "start.must.be.before.end"));
```

or using the DSL:

```java
LocalDate start = LocalDate.now();
LocalDate end = LocalDate.now().plusDays(1);

validating(
        validateThat(start).isNotNull(),
        validateThat(end).isNotNull()
)
        .map(Period::new)
        .refine(Rule.of(
                p -> p.start.isBefore(p.end),
                "start.must.be.before.end"
        ));
```
