# Frequently Asked Questions (FAQ)

Welcome to the FAQ for the Iffy Functional Validation (FV) library. If you have a question not answered here, please feel free to open an issue or reach out to the maintainers.

### Table of Contents
- [What is the difference between a `Rule` and a `MappingRule`?](#what-is-the-difference-between-a-rule-and-a-mappingrule)
- [How do I combine multiple rules?](#how-do-i-combine-multiple-rules)
- [Are rules null-safe by default?](#are-rules-null-safe-by-default)
- [How can I validate a property of an object?](#how-can-i-validate-a-property-of-an-object)
- [Can I use my own error messages?](#can-i-use-my-own-error-messages)
- [How can I easily make my own rule, e.g., for checking that a string is a palindrome?](#how-can-i-easily-make-my-own-rule-eg-for-checking-that-a-string-is-a-palindrome)
- [How can I check that my optional value meets a Rule when it is not empty (but empty is also allowed)?](#how-can-i-check-that-my-optional-value-meets-a-rule-when-it-is-not-empty-but-empty-is-also-allowed)
- [How can I check that my optional value meets a Rule when it is not empty (but this time empty is NOT allowed)?](#how-can-i-check-that-my-optional-value-meets-a-rule-when-it-is-not-empty-but-this-time-empty-is-not-allowed)
- [I have a List of String, and I want to check that each entry meets a Rule](#i-have-a-list-of-string-and-i-want-to-check-that-each-entry-meets-a-rule)
- [Can I also validate Maps?](#can-i-also-validate-maps)
- [I have a String, and want to make sure it's a valid value for a given Enum](#i-have-a-string-and-want-to-make-sure-its-a-valid-value-for-a-given-enum)
- [I have a Rule for a certain type (e.g., Amount), and now I have another type Transaction that wraps Amount, can I reuse the Amount rule?](#i-have-a-rule-for-a-certain-type-eg-amount-and-now-i-have-another-type-transaction-that-wraps-amount-can-i-reuse-the-amount-rule)
- [How can I make sure my record or class is created with valid values?](#how-can-i-make-sure-my-record-or-class-is-created-with-valid-values)
- [Do I need to use Strings to name the values I'm validating?](#do-i-need-to-use-strings-to-name-the-values-im-validating)
- [In my constructor, I want to be liberal with my input, and only validate the value after changing it](#in-my-constructor-i-want-to-be-liberal-with-my-input-and-only-validate-the-value-after-changing-it)
- [Ok, but can I do the same when defining a Rule?](#ok-but-can-i-do-the-same-when-defining-a-rule)
- [Ok, but I want to transform multiple fields in my constructor, how do I get their transformed values?](#ok-but-i-want-to-transform-multiple-fields-in-my-constructor-how-do-i-get-their-transformed-values)


---

### What is the difference between a `Rule` and a `MappingRule`?

In short: A **`Rule<T>`** validates a value without changing it, while a **`MappingRule<T, R>`** validates a value and transforms it into another type or value.

*   **`Rule<T>`**:
    *   Think of it as a `Predicate<T>` that returns a `Validation<T>`.
    *   If the validation succeeds, it returns the **exact same instance** that was passed in.
    *   It is used when you want to check if a value meets certain criteria (e.g., "is not null", "is positive", "matches a regex").
    *   `Rule<T>` actually extends `MappingRule<T, T>`, which means every `Rule` is also a `MappingRule`.

*   **`MappingRule<T, R>`**:
    *   Think of it as a `Function<T, R>` that returns a `Validation<R>`.
    *   It is used when validation and transformation are coupled. For example, parsing a `String` into an `Integer` or a `LocalDate`.
    *   If the parsing or any subsequent validation fails, it returns an `Invalid` result with error messages.
    *   If it succeeds, it returns a `Valid` result containing the transformed value.

**When to use which?**
Use a `Rule` for simple checks on a value. Use a `MappingRule` when you need to change the type of the value or when you want to "materialize" a validated object from a raw input.

---

### How do I combine multiple rules?

You can combine rules using several composition methods:

*   **`and(Rule)`**: Short-circuiting "and". If the first rule fails, the second one is not even executed.
*   **`andAlso(Rule)`**: Non-short-circuiting "and". Both rules are executed, and if both fail, their errors are combined.
*   **`or(Rule)`**: If the first rule succeeds, the result is valid. If it fails, it tries the second rule. If both fail, errors are combined.
*   **`Rule.any(Rule...)`**: a more general or, acception multiple rules. As long as a single rule passes, the result is valid. If all fail, errors are combined. 
*   **`Rule.all(Rule...)`**: Combines multiple rules. All must pass, and it collects all errors from failing rules.

---

### Are rules null-safe by default?

Most factory methods like `Rule.of(Predicate, ...)` include a null check. If the input is `null`, they will return a `Validation.invalid("must.not.be.null")` without calling your predicate.

However, if you implement the `Rule` interface directly or use certain combinators, you should be aware of nullability. It is generally recommended to start your validation chain with `Rule.notNull()` if you expect a non-null value.

---

### How can I validate a property of an object?

You can use the `Rule.with(selector, rule)` or `MappingRule.with(selector, rule)` methods. This allows you to "focus" a rule on a specific part of an object.

```java
Rule<User> userRule = Rule.with(User::getEmail, emailRule);
```

---

### Can I use my own error messages?

Yes! While many built-in rules use standard keys like `must.not.be.null`, you can always override the error message or key using `.withErrorKey("my.custom.key")` or by providing an `ErrorMessage` object during rule creation.

---

### How can I easily make my own rule, e.g., for checking that a string is a palindrome?

The easiest way to create a custom rule is by using the `Rule.of(Predicate, ErrorKey)` factory method. This method automatically handles null-safety (returning `must.not.be.null` if the input is null) so you can focus on your logic.

```java
Rule<String> palindromeRule = Rule.of(
    s -> new StringBuilder(s).reverse().toString().equalsIgnoreCase(s),
    "must.be.palindrome"
);
```

For more complex rules, you can also implement the `Rule<T>` functional interface directly.

---

### How can I check that my optional value meets a Rule when it is not empty (but empty is also allowed)?

You can use the `liftToOptional()` method (for `java.util.Optional`) or `liftToOption()` (for Vavr `Option`). 

These methods "lift" a rule that works on a single value to work on an optional container. If the container is empty, the rule is considered **valid**. If it contains a value, the original rule is applied to that value.

```java
Rule<String> minLengthRule = strings.minLength(5);
Rule<Optional<String>> optionalRule = minLengthRule.liftToOptional();

optionalRule.test(Optional.empty()); // Valid
optionalRule.test(Optional.of("abc")); // Invalid (must be at least 5)
optionalRule.test(Optional.of("abcdef")); // Valid
```

---

### How can I check that my optional value meets a Rule when it is not empty (but this time empty is NOT allowed)?

If you want to ensure that an `Optional` is **not empty** AND its value satisfies a certain rule, you can use the `MappingRule.requiredOptional(Rule)` or `MappingRule.requiredOption(Rule)` methods.

Unlike `liftToOptional()`, which returns valid for empty optionals, these methods will return an `Invalid` result with the error key `must.not.be.empty` if the optional is empty. If the optional contains a value, it applies the rule to that value and—importantly—**extracts** the value from the container.

```java
Rule<String> minLengthRule = strings.minLength(5);
MappingRule<Optional<String>, String> mandatoryRule = MappingRule.requiredOptional(minLengthRule);

mandatoryRule.test(Optional.empty()); // Invalid (must.not.be.empty)
mandatoryRule.test(Optional.of("abc")); // Invalid (must be at least 5)
Validation<String> result = mandatoryRule.test(Optional.of("abcdef")); // Valid("abcdef")
```

This is particularly useful when you want to "unwrap" a validated value from an optional as part of a larger validation pipeline.

If you don't want to extract the value and prefer to keep working with a `Rule<Optional<T>>`, you can use `optionals.contains(Rule<T> rule)`

```java
Rule<Optional<String>> mandatoryRule = optionals.contains(strings.length(5));

mandatoryRule.test(Optional.empty()); // Invalid (must.not.be.empty)
mandatoryRule.test(Optional.of("abc")); // Invalid (must be length 5)
mandatoryRule.test(Optional.of("abcde")); // Valid(Optional.of("abcde"))
```

---

### I have a List of String, and I want to check that each entry meets a Rule

The easiest way to validate every element in a list is to use the `liftToList()` method (for `java.util.List`) or `liftToVavrList()` (for Vavr `List`).

When you lift a `Rule<T>`, you get a `Rule<List<T>>` that applies the original rule to each element. If any element fails, the whole list is considered invalid, and the errors will include the index of the failing element (e.g., `[0].must.not.be.empty`).

```java
Rule<String> notEmpty = strings.notEmpty();
Rule<List<String>> allNotEmpty = notEmpty.liftToList();

allNotEmpty.test(List.of("abc", "")); // Invalid ([1].must.not.be.empty)
```

If you are using the `CollectionRules` or `VavrCollectionRules` classes, you can also use `allMatchRule(Rule)`:

```java
Rule<List<String>> allNotEmpty = collections.allMatchRule(strings.notEmpty());
```

Both approaches are equivalent, as `allMatchRule` is a convenience wrapper around `liftToList()`.

---

### Can I also validate Maps?

Yes! Similar to lists, you can lift a `Rule<T>` or `MappingRule<T, R>` to work on maps using `liftToMap()` (for `java.util.Map`) or `liftToVavrMap()` (for Vavr `Map`).

When you lift a rule to a map, it applies the rule to every **value** in the map. The map **key** is used to create a path segment for any error messages, so you can easily identify which entry failed.

```java
Rule<String> notEmpty = strings.notEmpty();
Rule<Map<String, String>> mapRule = notEmpty.liftToMap();

Map<String, String> input = Map.of("key1", "value1", "key2", "");
mapRule.test(input); // Invalid (key2.must.not.be.empty)
```

#### Customizing the Path Segment
By default, `liftToMap()` uses `key.toString()` for the path. If your keys are complex objects or you want a different naming convention, you can provide a `keyExtractor` function:

```java
Rule<User> userRule = ...;
// Use the user's ID as the path segment in case of errors
Rule<Map<Long, User>> mapRule = userRule.liftToMap(id -> "user_" + id);

mapRule.test(Map.of(1, user1, 2, user2));
```

---

### I have a String, and want to make sure it's a valid value for a given Enum

You can use the `isEnum(Class<E> clazz)` method found in `ObjectRules`. This method returns a `MappingRule<String, E>` that validates if the input string matches one of the enum constants and, if successful, transforms it into that enum instance.

```java
enum Status { OPEN, CLOSED }

MappingRule<String, Status> rule = objects.isEnum(Status.class);

rule.test("OPEN");    // Valid(Status.OPEN)
rule.test("UNKNOWN"); // Invalid (must.be.valid.enum.value)
```

If the validation fails, it uses the error key `must.be.valid.enum.value` and provides the invalid input as a parameter named `value`.  
If you only want to check if the String represents a valid enum value, but keep the String, use `objects.canBeEnum` instead. 


---

### I have a Rule for a certain type (e.g., Amount), and now I have another type Transaction that wraps Amount, can I reuse the Amount rule?

Yes, you can easily reuse rules for wrapped types or properties using `Rule.with(selector, rule)` or the more fluent `rule.given(selector)`.

#### Using `Rule.with`
This static method is great for building rules from the outside:

```java
Rule<Amount> amountRule = AmountRules.isPositive();
Rule<Transaction> transactionRule = Rule.with(Transaction::getAmount, amountRule);
```

#### Using `rule.given`
This instance method allows you to "lift" an existing rule to work on a parent object:

```java
Rule<Amount> amountRule = AmountRules.isPositive();
Rule<Transaction> transactionRule = amountRule.given(Transaction::getAmount);
```

Both methods create a `Rule<Transaction>` that extracts the `Amount` from a `Transaction` and applies the `amountRule` to it. If the validation fails, the error will be reported at the `amount` path (or whatever the selector represents).

---

### How can I make sure my record or class is created with valid values?

There are two main approaches to ensure your objects are always in a valid state: validating **before** calling the constructor or validating **inside** the constructor.

#### 1. Validating Before Creation (Materialization)
You can use `Validation.mapN` to combine several independent validations and, if all pass, call your constructor or a factory method. This is the recommended "functional" approach as it returns a `Validation<Target>` rather than throwing an exception.

```java
record Person(String name, int age) {}

// use the Rule API directly:
Validation<String> nameV = strings.minLength(3).test(dto.name()).at("name");
Validation<Integer> ageV = ints.min(18).test(dto.age()).at("age");
// or using the DSL
Validation<String> nameV = validateThat(dto.name(),Person::name).is(strings.minLength(3));
Validation<Integer> ageV = validateThat(dto.age(),Person::age).is(ints.min(18));
        
// now combine the Validations into a Person if both are Valid 
Validation<Person> personV = Validation.mapN(nameV, ageV, Person::new);
```

There is an alternative, still experimental syntax in `ValidatingDSL` that looks like this:

```java
Validation<Person> personV = validating(
        validateThat(dto.name(),Person::name).is(strings.minLength(3)),
        validateThat(dto.age(),Person::age).is(ints.min(18))
    ).map(Person::new);
```



#### 2. Validating Inside the Constructor (Fail-Fast)
If you prefer your constructor to throw a `ValidationException` when given invalid values (common in DDD or when using Java Records), you can use the `assertAllValid` method from the `DSL` class.

```java
import static be.iffy.fv.dsl.DSL.*;

record Person(String name, int age) {
    public Person {
        assertAllValid(
            validateThat(name, "name").is(strings.minLength(3)),
            validateThat(age, "age").is(ints.min(18))
        );
    }
}
```

If any of the validations fail, `assertAllValid` throws a `ValidationException` containing all the collected error messages.

---

### Do I need to use Strings to name the values I'm validating?

No! While you can always use a plain `String` to name a value (which becomes the path segment in error messages), the library also supports **`PropertySelector`**.

A `PropertySelector` is a functional interface that allows you to use **method references** to identify properties. The library then automatically extracts the property name from the method reference.

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

You can use `PropertySelector` in many places, including `validateThat`, `assertThat`, and when focusing rules with `Rule.with()` or `rule.given()`.

---

### In my constructor, I want to be liberal with my input, and only validate the value after changing it

You can use `assertThat(value,"field").map(transformation)` to transform a value before applying rules to it. This is useful when you want to normalize input (like trimming strings or converting case) and then validate the result.

#### Example: Trimming and checking length

```java
import static be.iffy.fv.dsl.DSL.*;

record Username(String value) {
    public Username {
        // assertThat returns the valid value or throws a ValidationException otherwise
        value = assertThat(value,"value").map(String::trim).is(strings.minLength(3)); 
    }
}
```

In this example:
1. The input `value` is trimmed.
2. The trimmed value is checked against `minLength(3)`.
3. If it fails, a `ValidationException` is thrown.
4. If it succeeds, `assertThat` returns the **trimmed** value, which is then assigned to the field.
5. If the input value was null, the String::trim method would have never been called.

---

### Ok, but I want to transform multiple fields in my constructor, how do I get their transformed values?

When you have multiple fields that need transformation and validation, you can use **`assertAllValid`** with multiple arguments. It will return a Vavr **`Tuple`** containing all the transformed values if they are all valid, or throw a `ValidationException` with all accumulated errors.
The assignment doesn't look super nice, but it's the best we can do without java having explicit support for something like tuple assignment.

#### Example: Normalizing multiple fields

```java
import static be.iffy.fv.dsl.DSL.*;
import io.vavr.Tuple2;

record User(String username, String email) {
    public User {
        // using a var makes this better :)
        Tuple2<String, String> values = assertAllValid(
            assertThat(username, "username").map(String::trim).is(strings.minLength(3)),
            assertThat(email, "email").map(String::toLowerCase).is(strings.email())
        );

        this.username = values._1; // is trimmed
        this.email = values._2; // is lowercased
    }
}
```

In this case:
1. Both `username` and `email` are transformed (trimmed and lowercased respectively).
2. The rules are applied to the transformed values.
3. If any check fails, a `ValidationException` is thrown containing all errors.
4. If all succeed, a `Tuple2` is returned containing the trimmed username and the lowercased email.

The library supports `assertAllValid` for up to 8 validations, returning `Tuple2` through `Tuple8`.

---

### Ok, but can I do the same when defining a Rule?

Yes! If you want to create a reusable `Rule` that includes a transformation step, you can use the **`after()`** syntax.

#### Example: A rule that trims and then checks length

```java
import static be.iffy.fv.dsl.DSL.*;

Rule<String> trimmedMinLength3 = after(String::trim).is(strings.minLength(3));
```

This is very similar to the `validateThat(value).map(...)` syntax used in constructors, but it allows you to package the transformation and the validation into a single `Rule` object.

#### Technical Note: Rule vs MappingRule

Technically, any rule that transforms its input (like one using `after`) should be a `MappingRule`. However, the `after(...).is(...)` DSL returns a **`Rule`** for convenience. 

This allows you to use the resulting rule anywhere a standard `Rule` is expected, while still benefiting from the internal transformation. If you need to expose the transformed value to the rest of a validation chain, you would typically use `MappingRule` explicitly.
Using a MappingRule for this case would look something like this: 

```java
MappingRule<String, String> trimmedMinLength3 = MappingRule.of(String::trim, "can.not.fail").andThen(minLength);
```
