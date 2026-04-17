package net.vanfleteren.fv;

import io.vavr.collection.HashMap;
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
 * <h2>Example: Defining and using a simple rule</h2>
 * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="rule-example"}
 *
 * @param <T> the type of the value to be validated.
 */
@FunctionalInterface
public interface Rule<T> extends MappingRule<T, T> {

    /**
     * Tests the given value against the rule. If the value passes the test,
     * a {@link net.vanfleteren.fv.Validation.Valid} containing the exact same instance will be returned.
     * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="test-example"}
     *
     * @param value the value to be validated.
     * @return a {@link Validation} object indicating the result of the test.
     */
    Validation<T> test(T value);

    /**
     * Creates a {@link Rule} from the given predicate and error message key.
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="of-string-example"}
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
     * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="of-error-example"}
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
     * Composes this rule with another rule using "short-circuiting and" logic.
     * The combined rule is successful only if both this and the other rule are successful.
     * If this rule fails, the evaluation stops and the other rule is not evaluated.
     * <p>
     * If you want to evaluate both rules and accumulate their errors, use {@link #andAlso(Rule)}.
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="and-example"}
     *
     * @param other the other rule to compose with.
     * @param <S>   the target type.
     * @return a new {@link Rule} instance.
     * @throws NullPointerException if {@code other} is null.
     */
    default <S extends T> Rule<S> and(Rule<? super S> other) {
        Objects.requireNonNull(other, "other rule cannot be null");
        return input -> test(input).flatMap(v -> other.test(input).map(ignored -> input));
    }

    /**
     * Composes this rule with another rule using "non-short-circuiting and" logic.
     * The combined rule is successful only if both this and the other rule are successful.
     * If both rules fail, their errors are combined.
     * <p>
     * If you want to stop evaluation after the first failure, use {@link #and(Rule)}.
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="and-also-example"}
     *
     * @param other the other rule to compose with.
     * @param <S>   the target type.
     * @return a new {@link Rule} instance.
     * @throws NullPointerException if {@code other} is null.
     */
    default <S extends T> Rule<S> andAlso(Rule<? super S> other) {
        Objects.requireNonNull(other, "other rule cannot be null");
        // map back to original input so we're protected against other returning an incompatible value
        return input -> Validation.mapN(test(input), other.test(input), (v, o) -> v).map(ignore -> input);
    }

    /**
     * Composes this rule with another rule using "or" logic.
     * The combined rule is successful if either this or the other rule is successful.
     * If both rules fail, their errors are combined.
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="or-example"}
     *
     * @param other the other rule to compose with.
     * @param <S>   the target type.
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

            Validation<S> second = other.test(input).map(ignore -> input);
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
     * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="negate-string-example"}
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
     * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="negate-error-example"}
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
     * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="only-if-predicate-example"}
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
     * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="only-if-supplier-example"}
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
     * Returns a new {@link Rule} that first applies this rule, and if the input is invalid, falls back to the {@code other} rule.
     * Like {@link MappingRule#recoverWith}, but the fallback is a {@link Rule}.
     * The difference with {@link #or(Rule)} is that only the errors of the {@code other} Rule will be returned if both fail.
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="recover-with-rule-example"}
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
     * Lifts this {@link Rule} so it applies to a {@link List} of T instead of a single T.
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="lift-to-list-example"}
     */
    @Override
    default Rule<List<T>> liftToList() {
        return values -> MappingRule.super.liftToList().test(values);
    }

    /**
     * Lifts this {@link Rule} so it applies to a {@link java.util.List} of T instead of a single T.
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="lift-to-jlist-example"}
     */
    @Override
    default Rule<java.util.List<T>> liftToJList() {
        return values -> MappingRule.super.liftToJList().test(values);
    }

    /**
     * Lifts this {@link Rule} so it applies to an {@link Option} of T.
     * <p>
     * Semantics:
     * - None =&gt; {@code valid(None)} (nothing to validate)
     * - Some(x) =&gt; validate x, and return {@code valid(Some(x))} or {@code invalid(errors)}
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="lift-to-option-example"}
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
     * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="lift-to-optional-example"}
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
     * - If the Map is empty, the map is considered valid.
     * - Each value in the map is validated, and the resulting validations are collected.
     * - If any validation fails, the entire map is considered invalid.
     * - If all validations pass, the map is considered valid.
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="lift-to-map-example"}
     *
     * @param <K> the key type.
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
     * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="lift-to-map-extractor-example"}
     *
     * @param keyExtractor the function to extract a path segment from the key.
     * @param <K>          the key type.
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
     * Lifts this {@link Rule} so it applies to a {@link java.util.Map} of K to T.
     * <p>
     * Be careful, the key {@code value.toString()} will be used as part of the path segment.
     * Make sure to have a key that has a meaningful string representation for this.
     * If you can't guarantee this, use the version of {@link #liftToJMap(Function)} that takes a keyExtractor function instead.
     * <p>
     * Semantics:
     * - If the Map is empty, the map is considered valid.
     * - Each value in the map is validated, and the resulting validations are collected.
     * - If any validation fails, the entire map is considered invalid.
     * - If all validations pass, the map is considered valid.
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="lift-to-jmap-example"}
     *
     * @param <K> the key type.
     */
    @Override
    default <K> Rule<java.util.Map<K, T>> liftToJMap() {
        return liftToJMap(Objects::toString);
    }

    /**
     * Lifts this {@link Rule} so it applies to a {@link java.util.Map} of K to T.
     * <p>
     * Behaves the same as {@link #liftToJMap()}, but uses the keyExtractor function to generate the path segment.
     * <p>
     * Semantics:
     * - If any validation fails, the entire map is considered invalid.
     * - If all validations pass, the map is considered valid.
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="lift-to-jmap-extractor-example"}
     *
     * @param keyExtractor the function to extract a path segment from the key.
     * @param <K>          the key type.
     */
    @Override
    default <K> Rule<java.util.Map<K, T>> liftToJMap(Function<K, Object> keyExtractor) {
        // this version can work a bit more efficiently since we know we can return
        // the original map if all entries are valid
        // as the values cannot change type in a Rule (as opposed to a MappingRule)
        return Rule.notNull().and(map -> {
            Seq<Validation<T>> validations = HashMap.ofAll(map).map(tuple ->
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
        });
    }

    /**
     * Composes two rules using "non-short-circuiting and" logic.
     * The combined rule is successful only if both rules are successful.
     * If both rules fail, the errors are combined.
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="both-example"}
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
     * Composes multiple rules using "non-short-circuiting and" logic.
     * The combined rule is successful only if all rules are successful.
     * If multiple rules fail, all errors are combined.
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="all-example"}
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
     * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="at-least-one-of-example"}
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
     * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="required-option-example"}
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
     * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="required-optional-example"}
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
     * {@snippet file="net/vanfleteren/fv/RuleSnippets.java" region="with-example"}
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
