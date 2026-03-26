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
     *
     * @param value the value to be validated.
     * @return a {@link Validation} object indicating the result of the test.
     */
    Validation<T> test(T value);

    /**
     * Creates a {@link Rule} from the given predicate and error message key.
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
     *
     * @param negatedErrorKey the error message key to use if negation fails.
     * @return a negated {@link Rule}.
     * @throws NullPointerException if {@code negatedErrorKey} is null.
     */
    default Rule<T> not(String negatedErrorKey) {
        Objects.requireNonNull(negatedErrorKey, "negatedErrorKey cannot be null");
        return not(ErrorMessage.of(negatedErrorKey));
    }

    /**
     * Negates this rule. The caller must provide the {@link ErrorMessage} to use when the negated rule fails.
     *
     * @param negatedError the error message to use if negation fails.
     * @return a negated {@link Rule}.
     * @throws NullPointerException if {@code negatedError} is null.
     */
    default Rule<T> not(ErrorMessage negatedError) {
        Objects.requireNonNull(negatedError, "negatedError cannot be null");
        return value -> {
            Validation<T> original = this.test(value);
            return original.isValid()
                    ? Validation.invalid(negatedError)
                    : Validation.valid(value);
        };
    }

    /**
     * Negates this rule and derives the negated error from the original rule's first error message.
     * Useful if you want conventions like prefixing keys, or to preserve args.
     *
     * @param errorMapper the mapper function to transform the fallback error message.
     * @return a negated {@link Rule}.
     */
    default Rule<T> not(Function<ErrorMessage, ErrorMessage> errorMapper) {
        Objects.requireNonNull(errorMapper, "errorMapper cannot be null");
        return value -> {
            Validation<T> original = this.test(value);
            if (original.isValid()) {
                // original passed => negation fails; we need an error
                // we don't have one, so we manufacture it from the original rule's error "template"
                // NOTE: since Rule doesn't expose its "default" ErrorMessage, we use a conservative default key.
                // If you want richer behavior, prefer not(String)/not(ErrorMessage) or extend Rule to expose metadata.
                ErrorMessage fallback = ErrorMessage.of("must.not.satisfy.rule");
                return Validation.invalid(errorMapper.apply(fallback));
            }
            return Validation.valid(value);
        };
    }

    /**
     *Applies a conditional rule.
     *
     * @param condition the condition to test, must not be null
     * @return a rule that tests the condition. If the condition is true, the original rule is applied.
     * If the condition is false, the value is considered valid by default.
     */
    default Rule<T> when(Predicate<T> condition) {
        Objects.requireNonNull(condition, "condition cannot be null");
        return value -> {
            if (condition.test(value)) {
                return this.test(value);
            }
            return Validation.valid(value);
        };
    }

    /**
     *Applies a conditional rule.
     *
     * @param condition the condition to test, must not be null
     * @return a rule that tests the condition. If the condition is true, the original rule is applied.
     * If the condition is false, the value is considered valid by default.
     */
    default Rule<T> when(Supplier<Boolean> condition) {
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
     *
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
     *
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
     *
     * @param <K> the key type.
     * @return a new {@link Rule} instance.
     */
    @Override
    default <K> Rule<Map<K, T>> liftToMap() {
        return value -> MappingRule.super.<K>liftToMap().test(value);
    }


    /**
     * Lifts this {@link Rule} so it applies to a {@link Map} of K to T.
     * <p>
     * Behaves the same as {@link #liftToMap()}, but uses the keyExtractor function to generate the path segment.
     * <p>
     * Semantics:
     * - Each value in the map is validated, and the resulting validations are collected.
     * - If any validation fails, the entire map is considered invalid.
     * - If all validations pass, the map is considered valid.
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
     * Composes two rules using "non short circuiting and" logic.
     * The combined rule is successful only if both rules are successful.
     * If both rules fail, the errors are combined.
     *
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
     * Composes multiple rules using "non short circuiting and" logic.
     * The combined rule is successful only if all rules are successful.
     * If multiple rules fail, all errors are combined.
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
     * Creates a new {@link Rule} that always returns a valid result for any input.
     *
     * @param <T> the type of object being validated
     * @return a rule that always returns a valid result
     */
    static <T> Rule<T> ok() {
        return Validation::valid;
    }

    /**
     * Returns a {@link MappingRule} that checks if the input {@link Option} is defined and then applies the given rule to its value.
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
     *
     * @param <T>  the type of the value inside the {@link Optional}
     * @param rule the rule to apply to the value inside the {@link Optional}
     * @return a new MappingRule that validates the option and applies the given rule to its value
     */
    static <T> MappingRule<Optional<T>, T> requiredOptional(Rule<T> rule) {
        return MappingRule.requiredOptional(rule);
    }

    /**
     * Applies the specified {@link Rule} to the result of applying the selector function to the input. Aka <code>contraMap</code>.
     * <p>
     * Usage example:
     * <pre>{@code
     * record User(String name) {}
     * Rule<String> nameRule = Rule.of(s -> s.length() > 3, "too.short");
     * Rule<User> userRule = Rule.with(User::name, nameRule);
     *
     * userRule.test(new User("Joe")); // returns Invalid("too.short")
     * userRule.test(new User("Alice")); // returns Valid(User("Alice"))
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
