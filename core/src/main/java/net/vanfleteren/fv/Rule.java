package net.vanfleteren.fv;

import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Option;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents a validation rule that can be applied to a value.
 *
 * <p>A Rule is essentially a {@link java.util.function.Predicate} that returns a {@link Validation}
 * object containing either the valid value or a structured {@link ErrorMessage}.
 *
 * <h3>Example: Defining and using a simple rule</h3>
 * <pre>{@code
 * // 1. Define a rule using a predicate and an error message key
 * Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "string.cannot.be.empty");
 *
 * // 2. Use the rule to validate a value
 * Validation<String> result = notEmpty.test("hello");
 *
 * // 3. Handle the result (functional approach with pattern matching)
 * String message = switch (result) {
 *     case Validation.Valid(var value) -> "Success: " + value;
 *     case Validation.Invalid(var errors) -> "Errors: " + errors.map(ErrorMessage::message).mkString(", ");
 * };
 *
 * // 4. Handle the result (classical approach)
 * if (result.isValid()) {
 *     System.out.println("Valid value: " + result.getOrElseThrow());
 * } else {
 *     result.errors().forEach(err -> System.err.println("Error: " + err.message()));
 * }
 *
 * System.out.println(message);
 * }</pre>
 *
 * @param <T> the type of the value to be validated.
 */
@FunctionalInterface
public interface Rule<T> extends MappingRule<T, T> {

    /**
     * Tests the given value against the rule.
     * <pre>{@code
     * // 1. A rule that validates if a String is not empty
     * Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "not.empty");
     *
     * // 2. Successful validation: String "hello" -> Valid
     * Validation<String> success = notEmpty.test("hello");
     * // Returns Valid("hello")
     *
     * // 3. Failed validation: String "" -> Invalid
     * Validation<String> failure = notEmpty.test("");
     * // Returns Invalid(ErrorMessage("not.empty"))
     * }</pre>
     *
     * @param value the value to be validated.
     * @return a {@link Validation} object indicating the result of the test.
     */
    Validation<T> test(T value);

    /**
     * Creates a {@link Rule} from the given predicate and error message key.
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. Create a rule using a predicate and an error key
     * Rule<Integer> positive = Rule.of(i -> i > 0, "must.be.positive");
     *
     * // 2. Usage
     * positive.test(10); // Returns Valid(10)
     * positive.test(-5); // Returns Invalid(ErrorMessage("must.be.positive"))
     * }</pre>
     *
     * @param predicate    the predicate to test values against.
     * @param errorKey     the error message key to use if the predicate returns {@code false}.
     * @param <T>          the type of the value to be validated.
     * @return a new {@link Rule} instance.
     */
    static <T> Rule<T> of(Predicate<T> predicate, String errorKey) {
        return of(predicate, ErrorMessage.of(errorKey));
    }

    /**
     * Creates a {@link Rule} from the given predicate and {@link ErrorMessage}.
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. Create a rule using a predicate and an ErrorMessage
     * ErrorMessage error = ErrorMessage.of("must.be.positive");
     * Rule<Integer> positive = Rule.of(i -> i > 0, error);
     *
     * // 2. Usage
     * positive.test(10); // Returns Valid(10)
     * positive.test(-5); // Returns Invalid(ErrorMessage("must.be.positive"))
     * }</pre>
     *
     * @param predicate    the predicate to test values against.
     * @param errorMessage the error message to use if the predicate returns {@code false}.
     * @param <T>          the type of the value to be validated.
     * @return a new {@link Rule} instance.
     */
    static <T> Rule<T> of(Predicate<T> predicate, ErrorMessage errorMessage) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        return value -> predicate.test(value) ? Validation.valid(value) : Validation.invalid(errorMessage);
    }

    /**
     * Composes this rule with another rule using "and" logic.
     * The combined rule is successful only if both this and the other rule are successful.
     * If this rule fails, the evaluation stops and the other rule is not evaluated.
     * <p>
     * If you want to evaluate both rules and accumulate their errors, use {@link #andAlso(Rule)}.
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. Two rules to be combined
     * Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "not.empty");
     * Rule<String> atLeast5 = Rule.of(s -> s.length() >= 5, "at.least.5");
     *
     * // 2. Chain them: stop at the first failure
     * Rule<String> combined = notEmpty.and(atLeast5);
     *
     * // 3. Usage
     * combined.test("hello"); // Returns Valid("hello")
     * combined.test("");      // Returns Invalid("not.empty")
     * combined.test("abc");   // Returns Invalid("at.least.5")
     * }</pre>
     *
     * @param other the other rule to compose with.
     * @param <S>   the target type.
     * @return a new {@link Rule} instance.
     * @throws NullPointerException if {@code other} is null.
     */
    @SuppressWarnings("unchecked")
    default <S extends T> Rule<S> and(Rule<? super S> other) {
        Objects.requireNonNull(other, "other rule cannot be null");
        return value -> test(value).flatMap(v -> (Validation<S>) other.test(value));
    }

    /**
     * Composes this rule with another rule using "non-short-circuiting and" logic.
     * The combined rule is successful only if both this and the other rule are successful.
     * If both rules fail, their errors are combined.
     * <p>
     * If you want to stop evaluation after the first failure, use {@link #and(Rule)}.
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. Two rules to be combined
     * Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "not.empty");
     * Rule<String> atLeast5 = Rule.of(s -> s.length() >= 5, "at.least.5");
     *
     * // 2. Combine them: collect all failures
     * Rule<String> combined = notEmpty.andAlso(atLeast5);
     *
     * // 3. Usage
     * combined.test(""); // Returns Invalid("not.empty", "at.least.5")
     * }</pre>
     *
     * @param other the other rule to compose with.
     * @param <S>   the target type.
     * @return a new {@link Rule} instance.
     * @throws NullPointerException if {@code other} is null.
     */
    @SuppressWarnings("unchecked")
    default <S extends T> Rule<S> andAlso(Rule<? super S> other) {
        Objects.requireNonNull(other, "other rule cannot be null");
        return value -> Validation.mapN((Validation<S>) test(value), (Validation<S>) other.test(value), (v, o) -> v);
    }

    /**
     * Composes this rule with another rule using "or" logic.
     * The combined rule is successful if either this or the other rule is successful.
     * If both rules fail, their errors are combined.
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. Two rules to be combined with OR logic
     * Rule<String> startsWithA = Rule.of(s -> s.startsWith("A"), "must.start.with.A");
     * Rule<String> startsWithB = Rule.of(s -> s.startsWith("B"), "must.start.with.B");
     *
     * // 2. Combine them
     * Rule<String> combined = startsWithA.or(startsWithB);
     *
     * // 3. Usage
     * combined.test("Apple"); // Returns Valid("Apple")
     * combined.test("Banana"); // Returns Valid("Banana")
     * combined.test("Cherry"); // Returns Invalid("must.start.with.A", "must.start.with.B")
     * }</pre>
     *
     * @param other the other rule to compose with.
     * @param <S>   the target type.
     * @return a new {@link Rule} instance.
     * @throws NullPointerException if {@code other} is null.
     */
    @SuppressWarnings("unchecked")
    default <S extends T> Rule<S> or(Rule<? super S> other) {
        Objects.requireNonNull(other, "other rule cannot be null");
        return input -> {
            Validation<S> first = (Validation<S>) test(input);
            if (first.isValid()) {
                return first;
            }

            Validation<S> second = (Validation<S>) other.test(input);
            if (second.isValid()) {
                return second;
            }

            return Validation.invalid(first.errors().appendAll(second.errors()));
        };
    }

    /**
     * Negates this rule. The caller must provide the error message key to use when the negated rule fails.
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. A rule that checks if a string starts with "A"
     * Rule<String> startsWithA = Rule.of(s -> s.startsWith("A"), "must.start.with.A");
     *
     * // 2. Negate it: now it validates that it does NOT start with "A"
     * Rule<String> doesNotStartWithA = startsWithA.negate("must.not.start.with.A");
     *
     * // 3. Usage
     * doesNotStartWithA.test("Banana"); // Returns Valid("Banana")
     * doesNotStartWithA.test("Apple");  // Returns Invalid(ErrorMessage("must.not.start.with.A"))
     * }</pre>
     *
     * @param negatedErrorKey the error message key to use if negation fails.
     * @return a negated {@link Rule}.
     * @throws NullPointerException if {@code negatedErrorKey} is null.
     */
    default Rule<T> negate(String negatedErrorKey) {
        Objects.requireNonNull(negatedErrorKey, "negatedErrorKey cannot be null");
        return negate(ErrorMessage.of(negatedErrorKey));
    }

    /**
     * Negates this rule. The caller must provide the {@link ErrorMessage} to use when the negated rule fails.
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. A rule that checks if a string starts with "A"
     * Rule<String> startsWithA = Rule.of(s -> s.startsWith("A"), "must.start.with.A");
     *
     * // 2. Negate it using an ErrorMessage
     * ErrorMessage error = ErrorMessage.of("must.not.start.with.A");
     * Rule<String> doesNotStartWithA = startsWithA.negate(error);
     *
     * // 3. Usage
     * doesNotStartWithA.test("Banana"); // Returns Valid("Banana")
     * doesNotStartWithA.test("Apple");  // Returns Invalid(ErrorMessage("must.not.start.with.A"))
     * }</pre>
     *
     * @param negatedError the error message to use if negation fails.
     * @return a negated {@link Rule}.
     * @throws NullPointerException if {@code negatedError} is null.
     */
    default Rule<T> negate(ErrorMessage negatedError) {
        Objects.requireNonNull(negatedError, "negatedError cannot be null");
        return value -> {
            Validation<T> original = this.test(value);
            return original.isValid()
                    ? Validation.invalid(negatedError)
                    : Validation.valid(value);
        };
    }

    /**
     * Applies a conditional rule.
     * <p>
     * Usage example:
     * <pre>{@code
     * // Only validate if the string is not empty
     * Rule<String> minLength = Rule.of(s -> s.length() >= 5, "too.short");
     * Rule<String> whenNotEmpty = minLength.onlyIf(s -> !s.isEmpty());
     *
     * whenNotEmpty.test("");      // Returns Valid("")
     * whenNotEmpty.test("abc");   // Returns Invalid("too.short")
     * whenNotEmpty.test("abcde"); // Returns Valid("abcde")
     * }</pre>
     *
     * @param condition the condition to test, must not be null
     * @return a rule that tests the condition. If the condition is true, the original rule is applied.
     * If the condition is false, the value is considered valid by default.
     */
    default Rule<T> onlyIf(Predicate<T> condition) {
        Objects.requireNonNull(condition, "condition cannot be null");
        return value -> {
            if (condition.test(value)) {
                return this.test(value);
            }
            return Validation.valid(value);
        };
    }

    /**
     * Applies a conditional rule.
     * <p>
     * Usage example:
     * <pre>{@code
     * // Only validate if a certain flag is enabled
     * Rule<String> rule = Rule.of(s -> s.length() >= 5, "too.short");
     * Rule<String> conditional = rule.onlyIf(() -> config.isValidationEnabled());
     *
     * // If isValidationEnabled() returns false:
     * conditional.test("abc"); // Returns Valid("abc")
     * }</pre>
     *
     * @param condition the condition to test, must not be null
     * @return a rule that tests the condition. If the condition is true, the original rule is applied.
     * If the condition is false, the value is considered valid by default.
     */
    default Rule<T> onlyIf(Supplier<Boolean> condition) {
        Objects.requireNonNull(condition, "condition cannot be null");
        return value -> {
            if (condition.get()) {
                return this.test(value);
            }
            return Validation.valid(value);
        };
    }


    /**
     * Returns a new {@link Rule} that first applies this rule, and if the input is invalid, falls back to the other rule.
     * Like {@link MappingRule#recoverWith}, but the fallback is a {@link Rule}.
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. Try to validate as admin, otherwise try as guest
     * Rule<String> adminOnly = Rule.of(s -> s.equals("admin"), "not.admin");
     * Rule<String> guestOk = Rule.of(s -> s.equals("guest"), "not.guest");
     *
     * // 2. Use recoverWithRule to fall back
     * Rule<String> combined = adminOnly.recoverWithRule(guestOk);
     *
     * // 3. Usage
     * combined.test("admin"); // Returns Valid("admin")
     * combined.test("guest"); // Returns Valid("guest")
     * combined.test("other"); // Returns Invalid("not.guest")
     * }</pre>
     *
     * @param other the other rule to use as a fallback if this rule fails
     * @return a new {@link Rule} that first applies this rule, and if the input is invalid, falls back to the other rule
     */
    default <S extends T> Rule<S> recoverWithRule(Rule<? super S> other) {
        Objects.requireNonNull(other, "other rule cannot be null");
        return input -> {
            Validation<T> first = this.test(input);
            if (first.isValid()) {
                return (Validation<S>)first;
            }

            return Validation.narrowSuper(other.test(input));
        };
    }

    /**
     * Lifts a {@link Rule} so it applies to a {@link List} of T instead of a single T.
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. Define a rule for a single element
     * Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "must.not.be.empty");
     *
     * // 2. Lift it to apply to a list
     * Rule<List<String>> listRule = notEmpty.liftToList();
     *
     * // 3. Usage
     * listRule.test(List.of("a", "b")); // Returns Valid(List("a", "b"))
     * listRule.test(List.of("a", ""));  // Returns Invalid(ErrorMessage("must.not.be.empty").atIndex(1))
     * }</pre>
     *
     * @return a new {@link Rule} instance.
     */
    @Override
    default Rule<List<T>> liftToList() {
        return values -> MappingRule.super.liftToList().test(values);
    }

    /**
     * Lifts this {@link Rule} so it applies to an {@link Option} of T.
     * <p>
     * Semantics:
     * - None =&gt; {@code valid(None)} (nothing to validate)
     * - Some(x) =&gt; validate x, and return {@code valid(Some(x))} or {@code invalid(errors)}
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. Define a rule for a single element
     * Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "must.not.be.empty");
     *
     * // 2. Lift it to apply to an Option
     * Rule<Option<String>> optionRule = notEmpty.liftToOption();
     *
     * // 3. Usage
     * optionRule.test(Option.some("a")); // Returns Valid(Some("a"))
     * optionRule.test(Option.none());     // Returns Valid(None)
     * optionRule.test(Option.some(""));  // Returns Invalid("must.not.be.empty")
     * }</pre>
     *
     * @see Rule#requiredOption(Rule) 
     * @see MappingRule#requiredOption(MappingRule) 
     * @return a new {@link Rule} instance.
     */
    @Override
    default Rule<Option<T>> liftToOption() {
        return value -> MappingRule.super.liftToOption().test(value);
    }

    /**
     * Lifts this {@link Rule} so it applies to an {@link java.util.Optional} of T.
     * <p>
     * Semantics:
     * - empty =&gt; {@code valid(Optional.empty)} (nothing to validate)
     * - not empty =&gt; validate x, and return {@code valid(Optional.of(x))} or {@code invalid(errors)}
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. Define a rule for a single element
     * Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "must.not.be.empty");
     *
     * // 2. Lift it to apply to an Optional
     * Rule<Optional<String>> optionalRule = notEmpty.liftToOptional();
     *
     * // 3. Usage
     * optionalRule.test(Optional.of("a")); // Returns Valid(Optional("a"))
     * optionalRule.test(Optional.empty());   // Returns Valid(Optional.empty)
     * optionalRule.test(Optional.of(""));  // Returns Invalid("must.not.be.empty")
     * }</pre>
     *
     * @see Rule#requiredOptional(Rule)
     * @see MappingRule#requiredOptional(MappingRule)
     * @return a new {@link Rule} instance.
     */
    @Override
    default Rule<Optional<T>> liftToOptional() {
        return value -> MappingRule.super.liftToOptional().test(value);
    }

    /**
     * Lifts this {@link Rule} so it applies to a {@link Map} of K to T.
     * <p>
     * Be careful, the key {@code value.toString()} will be used as part of the path segment.
     * Make sure to have a key that has a meaningful string representation for this.
     * If you can't guarantee this, use the version of {@link #liftToMap(Function)} that takes a keyExtractor function instead.
     * <p>
     * Semantics:
     * - Each value in the map is validated, and the resulting validations are collected.
     * - If any validation fails, the entire map is considered invalid.
     * - If all validations pass, the map is considered valid.
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. Define a rule for a single element
     * Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "must.not.be.empty");
     *
     * // 2. Lift it to apply to a map
     * Rule<Map<String, String>> mapRule = notEmpty.liftToMap();
     *
     * // 3. Usage
     * mapRule.test(Map.of("key1", "val1")); // Returns Valid(Map("key1", "val1"))
     * mapRule.test(Map.of("key1", ""));     // Returns Invalid(ErrorMessage("must.not.be.empty").atIndex("key1"))
     * }</pre>
     *
     * @param <K> the key type.
     * @return a new {@link Rule} instance.
     */
    @Override
    default <K> Rule<Map<K, T>> liftToMap() {
        return liftToMap(Objects::toString);
    }


    /**
     * Lifts this {@link Rule} so it applies to a {@link Map} of K to T.
     * <p>
     * Behaves the same as {@link #liftToMap()}, but uses the keyExtractor function to generate the path segment.
     * <p>
     * Semantics:
     * - If any validation fails, the entire map is considered invalid.
     * - If all validations pass, the map is considered valid.
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. Define a rule for a single element
     * Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "must.not.be.empty");
     *
     * // 2. Lift it to apply to a map with a custom key extractor for the error path
     * Rule<Map<Integer, String>> mapRule = notEmpty.liftToMap(key -> "item-" + key);
     *
     * // 3. Usage
     * mapRule.test(Map.of(1, "")); // Returns Invalid(ErrorMessage("must.not.be.empty").atIndex("item-1"))
     * }</pre>
     *
     * @param keyExtractor the function to extract a path segment from the key.
     * @param <K>          the key type.
     * @return a new {@link Rule} instance.
     */
    @Override
    default <K> Rule<Map<K, T>> liftToMap(Function<K, Object> keyExtractor) {
        // this version can work a bit more efficiently since we know we can return
        // the original map if all entries are valid
        // as the values cannot change type in a Rule (as opposed to a MappingRule)
        return map -> {
            Seq<Validation<T>> validations = map.map(tuple ->
                    this.test(tuple._2)
                            .mapErrors(errors ->
                                    errors.map(e -> e.atIndex(keyExtractor.apply(tuple._1)))
                            )
            );

            var validAndInvalid = validations.partition(Validation::isValid);
            if (validAndInvalid._2.nonEmpty()) {
                return Validation.invalid(validAndInvalid._2.flatMap(Validation::errors).toList());
            } else {
                return Validation.valid(map);
            }
        };
    }

    /**
     * Composes two rules using "non-short-circuiting and" logic.
     * The combined rule is successful only if both rules are successful.
     * If both rules fail, the errors are combined.
     * <p>
     * Usage example:
     * <pre>{@code
     * Rule<String> minLength = Rule.of(s -> s.length() >= 3, "too.short");
     * Rule<String> containsAt = Rule.of(s -> s.contains("@"), "missing.at");
     * Rule<String> both = Rule.both(minLength, containsAt);
     *
     * both.test("a"); // Returns Invalid("too.short", "missing.at")
     * }</pre>
     *
     * @see Rule#andAlso(Rule) 
     * @param first  the first rule.
     * @param second the second rule.
     * @param <T>    the type of the value to be validated.
     * @return a new {@link Rule} instance.
     */
    static <T> Rule<T> both(Rule<? super T> first, Rule<? super T> second) {
        Objects.requireNonNull(first, "first rule cannot be null");
        Objects.requireNonNull(second, "second rule cannot be null");
        return Rule.narrow(first).andAlso(second);
    }

    /**
     * Composes multiple rules using "non-short-circuiting and" logic.
     * The combined rule is successful only if all rules are successful.
     * If multiple rules fail, all errors are combined.
     * <p>
     * Usage example:
     * <pre>{@code
     * Rule<String> rule1 = Rule.of(s -> s.length() >= 3, "too.short");
     * Rule<String> rule2 = Rule.of(s -> s.contains("@"), "missing.at");
     * Rule<String> rule3 = Rule.of(s -> s.endsWith(".com"), "wrong.domain");
     * Rule<String> all = Rule.all(rule1, rule2, rule3);
     *
     * all.test("a"); // Returns Invalid("too.short", "missing.at", "wrong.domain")
     * }</pre>
     *
     * @param rules the rules to combine.
     * @param <T>   the type of the value to be validated.
     * @return a new {@link Rule} instance.
     */
    @SafeVarargs
    static <T> Rule<T> all(Rule<? super T>... rules) {
        Objects.requireNonNull(rules, "rules cannot be null");
        List<Rule<? super T>> ruleList = List.of(rules);
        return value -> {
            List<Validation<T>> validations = ruleList.map(rule -> Rule.<T>narrow(rule).test(value));
            List<ErrorMessage> errors = validations
                    .filter(v -> !v.isValid())
                    .flatMap(Validation::errors);

            return errors.isEmpty()
                    ? Validation.valid(value)
                    : Validation.invalid(errors);
        };
    }

    /**
     * Composes multiple rules using "at least one of" logic.
     * The combined rule is successful if at least one of the rules is successful.
     * If all rules fail, all errors are combined.
     * <p>
     * Usage example:
     * <pre>{@code
     * Rule<String> hasAt = Rule.of(s -> s.contains("@"), "missing.at");
     * Rule<String> hasDot = Rule.of(s -> s.contains("."), "missing.dot");
     * Rule<String> combined = Rule.atLeastOneOf(hasAt, hasDot);
     *
     * combined.test("abc"); // Returns Invalid("missing.at", "missing.dot")
     * combined.test("a.b"); // Returns Valid("a.b")
     * }</pre>
     *
     * @param rules the rules to combine.
     * @param <T>   the type of the value to be validated.
     * @return a new {@link Rule} instance.
     */
    @SafeVarargs
    static <T> Rule<T> atLeastOneOf(Rule<? super T>... rules) {
        Objects.requireNonNull(rules, "rules cannot be null");
        List<Rule<? super T>> ruleList = List.of(rules);
        return value -> {
            List<Validation<T>> validations = ruleList.map(rule -> Rule.<T>narrow(rule).test(value));
            Option<Validation<T>> firstValid = validations.find(Validation::isValid);

            if (firstValid.isDefined()) {
                return firstValid.get();
            } else {
                return Validation.invalid(validations.flatMap(Validation::errors));
            }
        };
    }

    /**
     * Narrows a {@code Rule<? super T>} to a {@code Rule<T>}.
     *
     * @param rule The rule to narrow.
     * @param <T>  The target type.
     * @return The narrowed rule.
     */
    @SuppressWarnings("unchecked")
    static <T> Rule<T> narrow(Rule<? super T> rule) {
        return (Rule<T>) rule;
    }

    /**
     * Returns a {@link Rule} that validates the input is not null.
     * <p>
     * Error key: "must.not.be.null"
     *
     * @param <T> the type of input
     * @return a Rule that returns valid input only if it's not null
     */
    static <T> Rule<T> notNull() {
        return MappingRule.<T>notNull()::test;
    }

    /**
     * Creates a new {@link Rule} that always returns a valid result for any non-null input.
     *
     * @param <T> the type of object being validated
     * @return a rule that always returns a valid result
     */
    static <T> Rule<T> ok() {
        return Validation::valid;
    }

    /**
     * Returns a {@link MappingRule} that checks if the input {@link Option} is defined and then applies the given rule to its value.
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. A rule that checks if a string is not empty
     * Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "not.empty");
     *
     * // 2. A rule that requires the Option to be present before applying the rule
     * MappingRule<Option<String>, String> requiredString = Rule.requiredOption(notEmpty);
     *
     * // 3. Usage
     * requiredString.test(Option.of("hello")); // Returns Valid("hello")
     * requiredString.test(Option.none());      // Returns Invalid("must.not.be.empty")
     * }</pre>
     *
     * @param <T>  the type of the value inside the {@link Option}
     * @param rule the rule to apply to the value inside the {@link Option}
     * @return a new {@link MappingRule} that validates the option and applies the given rule to its value
     */
    static <T> MappingRule<Option<T>, T> requiredOption(Rule<T> rule) {
        return MappingRule.requiredOption(rule);
    }

    /**
     * Returns a MappingRule that checks if the input {@link Optional} is defined and applies the given rule to its value.
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. A rule that checks if a string is not empty
     * Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "not.empty");
     *
     * // 2. A rule that requires the Optional to be present before applying the rule
     * MappingRule<Optional<String>, String> requiredString = Rule.requiredOptional(notEmpty);
     *
     * // 3. Usage
     * requiredString.test(Optional.of("hello")); // Returns Valid("hello")
     * requiredString.test(Optional.empty());      // Returns Invalid("must.not.be.empty")
     * }</pre>
     *
     * @param <T>  the type of the value inside the {@link Optional}
     * @param rule the rule to apply to the value inside the {@link Optional}
     * @return a new MappingRule that validates the option and applies the given rule to its value
     */
    static <T> MappingRule<Optional<T>, T> requiredOptional(Rule<T> rule) {
        return MappingRule.requiredOptional(rule);
    }

    /**
     * Applies the specified {@link Rule} to the result of applying the selector function to the input. Aka <code>contramap</code>.
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. Define a rule for a property
     * Rule<String> nameRule = Rule.of(s -> s.length() > 3, "too.short");
     *
     * // 2. Apply it to a property of a record
     * record User(String name) {}
     * Rule<User> userRule = Rule.with(User::name, nameRule);
     *
     * // 3. Usage
     * userRule.test(new User("Joe")); // Returns Invalid("too.short")
     * userRule.test(new User("Alice")); // Returns Valid(User("Alice"))
     * }</pre>
     *
     * @param <T>      the type of the input to be tested
     * @param <V>      the type of the result produced by the selector function
     * @param selector a function that extracts a value of type V from an input of type T
     * @param rule     the rule to be applied to the extracted value
     * @return a new rule that tests the applied selector and rule combination
     */
    static <T, V> Rule<T> with(Function<T, V> selector, Rule<V> rule) {
      return input -> rule.test(selector.apply(input)).map(ignore -> input);
    }

}
