# Frequently Asked Questions (FAQ)

Welcome to the FAQ for the Iffy Functional Validation (FV) library. If you have a question not answered here, please feel free to open an issue or reach out to the maintainers.

### Table of Contents
- [What is the difference between a `Rule` and a `MappingRule`?](#what-is-the-difference-between-a-rule-and-a-mappingrule)
- [I have an Invalid validation, how can I know what was wrong?](#i-have-an-invalid-validation-how-can-i-know-what-was-wrong)
- [How do I combine multiple rules?](#how-do-i-combine-multiple-rules)
- [Are rules null-safe by default?](#are-rules-null-safe-by-default)
- [Null seems to be an invalid value by default, but I really like null, how can I accept it but still validate if it's not null?](#null-seems-to-be-an-invalid-value-by-default-but-i-really-like-null-how-can-i-accept-it-but-still-validate-if-its-not-null)
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
- [I have some type whose constructor throws an exception, how can I make a Validation for this type?](#i-have-some-type-whose-constructor-throws-an-exception-how-can-i-make-a-validation-for-this-type)
- [I have a Validation, but I want to add an extra check on the value](#i-have-a-validation-but-i-want-to-add-an-extra-check-on-the-value)
- [If I have for example a Rule for Number, can I use it to also validate BigDecimals?](#if-i-have-for-example-a-rule-for-number-can-i-use-it-to-also-validate-a-subtype-like-bigdecimal)

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

### I have an Invalid validation, how can I know what was wrong?

When a validation fails, it returns an **`Invalid`** object. You can access the errors using the `getErrors()` method, which returns a `List<ErrorMessage>`.

Each **`ErrorMessage`** contains:
*   **`errorKey`**: A unique string identifying the type of error (e.g., `"must.have.length.between"`).
*   **`paths`**: The path to the field that failed validation.
*   **`parameters`**: A `Map<String, Object>` containing dynamic values that can be used to format a human-readable message.

#### The `formatted()` method

For quick debugging or logging, you can use the **`formatted()`** method. It returns a string that combines the path, the error key, and all parameters in a standardized format: `path.to.field.error.key:{param1:value1,param2:value2}`.

#### Example: Inspecting parameters and using `formatted()`

```java
import be.iffy.fv.Validation;
import be.iffy.fv.ErrorMessage;
import static be.iffy.fv.rules.text.StringRules.strings;

Validation<String> result = strings.lengthBetween(3, 10).test("hi");

if (result.isInvalid()) {
    ErrorMessage error = result.getErrors().head();
    
    // Accessing individual components
    System.out.println("Error Key: " + error.getErrorKey()); // must.have.length.between
    System.out.println("Min length: " + error.getParameters().get("min").get()); // 3
    System.out.println("Max length: " + error.getParameters().get("max").get()); // 10

    // Using formatted() for a quick overview
    System.out.println("Formatted: " + error.formatted());
    // Output: must.have.length.between:{min:3,max:10}
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

### Null seems to be an invalid value by default, but I really like null, how can I accept it but still validate if it's not null?

By default, all rules in the library consider `null` to be an invalid value. This is a design choice to encourage safer, more explicit handling of optionality and make implementing rules easier.

However, if you have a scenario where `null` is a perfectly valid state, but you still want to apply validation rules if a value *is* present, you can use **`Rule.nullOk(someOtherRule)`**.

The `nullOk` method wraps another rule. If the input is `null`, it returns a `Valid(null)`. If the input is not null, it applies the wrapped rule.

#### Example: Optional string validation

```java
Rule<String> optionalDescription = Rule.nullOk(strings.minLength(10));

// Returns Valid(null)
Validation<String> result1 = optionalDescription.test(null);

// Returns Invalid (too short)
Validation<String> result2 = optionalDescription.test("too short");

// Returns Valid("a very long description")
Validation<String> result3 = optionalDescription.test("a very long description");
```

> [!IMPORTANT]
> Be careful when combining a `nullOk` rule with other rules using `.and()` or similar methods. If the other rule doesn't handle `null`, you might still end up with a validation error (or a `NullPointerException` if that rule is not null-safe). It is often best to keep `nullOk` as the "outermost" wrapper.  
> So use something like `Rule.nullOk(strings.minLength(10).and(strings.startsWith("A")))` instead of `Rule.nullOk(strings.minLength(10)).and(strings.startsWith("A"))`.
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
We need a MappingRule because we'll be changing the type of Validation from Optional<T> to T.

Unlike `liftToOptional()`, which returns valid for empty optionals, these methods will return an `Invalid` result with the error key `must.not.be.empty` if the optional is empty. If the optional contains a value, it applies the rule to that value and—importantly—**extracts** the value from the container.

```java
Rule<String> minLengthRule = strings.minLength(5);
MappingRule<Optional<String>, String> mandatoryRule = MappingRule.requiredOptional(minLengthRule);

mandatoryRule.test(Optional.empty()); // Invalid (must.not.be.empty)
mandatoryRule.test(Optional.of("abc")); // Invalid (must be at least 5)
Validation<String> result = mandatoryRule.test(Optional.of("abcdef")); // Valid("abcdef")
```

There is also an alternative syntax available using a predefined Rule in OptionRules:

```java
MappingRule<Optional<String>, String> mandatoryAndMinLength = options.required(strings.minLength(4));
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

---

### I have some type whose constructor throws an exception, how can I make a Validation for this type?

If you want to validate a type constructed using a method or constructor that can throw an exception (checked or unchecked), you can use Vavr's **`Try`** in combination with **`Validation.from(Try)`**.

This allows you to wrap the potentially failing construction in a `Try`, and then convert it into a `Validation` object which fits perfectly into the rest of the library's ecosystem.

#### Example: Validating a URL

Since `new URL(String)` throws a checked `MalformedURLException`, it's a perfect candidate for this approach.

```java
import be.iffy.fv.Validation;
import io.vavr.control.Try;
import java.net.URL;

public Validation<URL> validateUrl(String input) {
    return Validation.from(
        Try.of(() -> new URL(input)),
        "invalid.url" // Error key to use if Try fails
    );
}
```
Note: there's a built-in MappingRule<String, URL> asURL() in StringRules for this.

In this example:
1. `Try.of(...)` attempts to create the `URL`. If an exception is thrown, it captures it in a `Failure` state.
2. `Validation.from(tryResult, "invalid.url")` converts the `Try` into a `Validation`. 
3. If the `Try` was a `Success`, you get a `Valid<URL>`.
4. If the `Try` was a `Failure`, you get an `Invalid` result with the error key `"invalid.url"`.

---

### I have a Validation, but I want to add an extra check on the value

If you already have a `Validation<T>` object and you want to apply an additional `Rule<T>` to its value (if it's valid), you can use the **`refine()`** method.

This is particularly useful when you've already performed some initial validation or transformation and want to "refine" the result with further constraints.

#### Example: Refining a validation

```java
import be.iffy.fv.Validation;
import static be.iffy.fv.rules.text.StringRules.strings;

Validation<String> initialValidation = ...;

// Only if initialValidation is Valid, check if the string is also an email
Validation<String> refinedValidation = initialValidation.refine(strings.email());
```

In this case:
1. If `initialValidation` is `Invalid`, `refine()` does nothing and returns the original `Invalid` result.
2. If `initialValidation` is `Valid`, the `strings.email()` rule is applied to the value.
3. If the email rule passes, you get a `Valid` result with the original value.
4. If the email rule fails, you get an `Invalid` result containing the error from the email rule.

#### Filtering with predicates

If you just want to check a simple condition without creating a full `Rule` object, you can also use **`filter()`**, which uses `refine` internally:

```java
Validation<String> filtered = initialValidation.filter(
    s -> s.startsWith("A"), 
    "must.start.with.A"
);
```

---

### If I have for example a Rule for Number, can I use it to also validate a subtype like BigDecimal?

Yes! Because `Rule<T>` is contravariant in its type parameter (meaning it can handle any subtype of `T`), you can use a rule defined for a supertype to validate a subtype. 

This is particularly useful for combining general rules with type-specific ones.

#### Example: Combining a Number rule with a BigDecimal rule

```java
Rule<Number> isPositive = Rule.of(n -> n.doubleValue() > 0, "must.be.positive");
Rule<BigDecimal> isMinusFortyTwo = Rule.of(b -> b.compareTo(new BigDecimal("-42")) == 0, "must.be.minus.forty.two");

Rule<BigDecimal> combined = isMinusFortyTwo.or(isPositive);
```
```