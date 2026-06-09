package be.iffy.fv;

import be.iffy.fv.Validation.Invalid;
import io.vavr.Tuple;
import io.vavr.Tuple2;
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
 * <p>
 * When making your own Rules, keep the following in mind:
 * <ul>
 *     <li>
 *         Rules are <em>not automatically nullSafe</em> (meaning the value they test might be null), but a Rule created with the
 *         {@code Rule.of(Predicate, ...)} factories will never pass the null to the predicate.
 *     </li>
 *     <li>Rules are <em>not supposed to change their input</em>, the value in the returned {@link be.iffy.fv.Validation.Valid} should be the <em>same instance</em> as the input. To change the input (type or value), use a {@link MappingRule}</li>
 * </ul>
 *
 */
@FunctionalInterface
public interface Rule<T> extends MappingRule<T, T> {

    /**
     * Tests the given value against the rule. If the value passes the test,
     * a {@link Validation.Valid} containing the exact same instance will be returned.
     *
     * @param value the value to be validated.
     * @return a {@link Validation} object indicating the result of the test.
     */
    Validation<T> test(T value);

    /**
     * Creates a {@link Rule} from the given predicate and error message key.
     * If the Predicate resolves to {@code true}, the Rule is considered {@link be.iffy.fv.Validation.Valid}
     */
    static <T> Rule<T> of(Predicate<? super T> predicate, String errorKey) {
        return of(predicate, ErrorMessage.of(errorKey));
    }

    /**
     * Make a {@code Rule<T>} from a function that shares the same signature.
     */
    static <T> Rule<T> of(Function<? super T, ? extends Validation<? extends T>> ruleLikeFunction) {
        if(ruleLikeFunction instanceof Rule) {
            // no need to wrap if the function is already a Rule
            @SuppressWarnings("unchecked")
            Rule<T> alreadyRule = (Rule<T>) ruleLikeFunction;
            return alreadyRule;
        }

        Objects.requireNonNull(ruleLikeFunction, "ruleLikeFunction cannot be null");
        return input -> {
            if(input == null) {
                return Invalid.notNull();
            }
            return Validation.narrow(
                    Objects.requireNonNull(ruleLikeFunction.apply(input),"ruleLikeFunction cannot return null Validation")
            );
        };
    }

    /**
     * Creates a {@link Rule} from the given predicate and {@link ErrorMessage}.
     * If the Predicate resolves to {@code true}, the Rule is considered {@link be.iffy.fv.Validation.Valid}
     *
     * @param predicate    the predicate to test values against.
     * @param errorMessage the error message to use if the predicate returns {@code false}.
     */
    static <T> Rule<T> of(Predicate<? super T> predicate, ErrorMessage errorMessage) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        return value -> {
            if (value == null) {
                return Invalid.notNull();
            } else {
                return predicate.test(value) ? Validation.valid(value) : Validation.invalid(errorMessage);
            }
        };
    }

    /**
     * Composes this rule with another rule using "short-circuiting and" logic.
     * The combined rule is successful only if both this and the other rule are successful.
     * If this rule fails, the evaluation stops and the other rule is not evaluated.
     * <p>
     * If you want to evaluate both rules and accumulate their errors, use {@link #andAlso(Function)}.
     *
     * @see #andAlso(Function)
     * @see #then(Function)
     */
    default <S extends T> Rule<S> and(Function<? super S, ? extends Validation<?>> other) {
        Objects.requireNonNull(other, "other rule cannot be null");
        return input ->
            test(input).flatMap(v ->
                    other.apply(input).map(ignored -> input)

            );
    }

    /**
     * Composes this rule with another rule using "non-short-circuiting and" logic.
     * The combined rule is successful only if both this and the other rule are successful.
     * If both rules fail, their errors are combined.
     * <p>
     * If you want to stop evaluation after the first failure, use {@link #and(Function)}.
     *
     * @see #and(Function)
     */
    default <S extends T> Rule<S> andAlso(Function<? super S, ? extends Validation<?>> other) {
        Objects.requireNonNull(other, "other rule cannot be null");
        // map back to original input so we're protected against other returning an incompatible value
        return input ->
            Validation.mapN(test(input), other.apply(input), (v, o) -> v).map(ignore -> input);
    }

    /**
     * Composes this rule with another rule using "or" logic.
     * The combined rule is successful if either this or the other rule is successful.
     * If both rules fail, their errors are combined.
     * Both rules are evaluated at most once, and the other rule is only evaluated when this rule fails.
     */
    @SuppressWarnings("unchecked")
    default <S extends T> Rule<S> or(Function<? super S, ? extends Validation<?>> other) {

        Objects.requireNonNull(other, "other rule cannot be null");
        return input -> {
            Validation<S> first = (Validation<S>) test(input);
            if (first.isValid()) {
                return first;
            }

            Validation<S> second = other.apply(input).map(ignore -> input);
            if (second.isValid()) {
                return second;
            }

            return Validation.invalid(first.errors().appendAll(second.errors()));
        };
    }

    /**
     * Composes this rule with another using XOR logic.
     * Successful only if exactly one of the rules is successful.
     */
    @SuppressWarnings("unchecked")
    default <S extends T> Rule<S> xor(Function<? super S, ? extends Validation<?>> other, String errorKey) {
        Objects.requireNonNull(other, "other rule cannot be null");
        Objects.requireNonNull(errorKey, "errorKey cannot be null");
        return input -> {
            boolean v1Valid = this.test(input).isValid();
            boolean v2Valid = other.apply(input).isValid();
            if (v1Valid ^ v2Valid) {
                return Validation.valid(input);
            }
            return Validation.invalid(errorKey);
        };
    }

    /**
     * Negates this rule. The caller must provide the error message key to use when the negated rule fails.
     *
     * @param negatedErrorKey the error message key to use if negation fails.
     */
    default Rule<T> negate(String negatedErrorKey) {
        Objects.requireNonNull(negatedErrorKey, "negatedErrorKey cannot be null");
        return negate(ErrorMessage.of(negatedErrorKey));
    }

    /**
     * Negates this rule. The caller must provide the {@link ErrorMessage} to use when the negated rule fails.
     *
     * @param negatedError the error message to use if negation fails.
     */
    default Rule<T> negate(ErrorMessage negatedError) {
        Objects.requireNonNull(negatedError, "negatedError cannot be null");
        return input -> {
            Validation<T> original = this.test(input);
            return original.isValid()
                    ? Validation.invalid(negatedError)
                    : Validation.valid(input);
        };
    }

    /**
     * Applies a conditional rule.
     *
     * @return a rule that tests the condition. If the condition is true, the original rule is applied.
     * If the condition is false, the value is considered valid by default.
     */
    default Rule<T> onlyIf(Predicate<? super T> condition) {
        Objects.requireNonNull(condition, "condition cannot be null");
        return input -> {
            if(input == null) {
                return Invalid.notNull();
            }
            if (condition.test(input)) {
                return this.test(input);
            }
            return Validation.valid(input);
        };
    }

    /**
     * Applies a conditional rule.
     *
     * @return a rule that tests the condition. If the condition is true, the original rule is applied.
     * If the condition is false, the value is considered valid by default.
     */
    default Rule<T> onlyIf(Supplier<Boolean> condition) {
        Objects.requireNonNull(condition, "condition cannot be null");
        return input -> {
            if (condition.get()) {
                return this.test(input);
            }
            return Validation.valid(input);
        };
    }

    /**
     * Returns a new {@link Rule} that first applies this rule, and if the input is invalid, falls back to the {@code other} rule.
     * Like {@link MappingRule#recoverWith}, but the fallback is a {@link Rule}.
     * The difference with {@link #or(Function)} is that only the errors of the {@code other} Rule will be returned if both fail.
     * The fallback rule is evaluated only when this rule fails.
     *
     * @param other the other rule to use as a fallback if this rule fails
     */
    default <S extends T> Rule<S> recoverWithRule(Rule<? super S> other) {
        Objects.requireNonNull(other, "other rule cannot be null");
        return input -> {
            Validation<T> first = this.test(input);
            if (first.isValid()) {
                return (Validation<S>) first;
            }

            return Validation.narrowSuper(other.test(input));
        };
    }

    /**
     * Returns a new {@link Rule} that, when invalid, uses the passed errorKey as single ErrorMessage.
     */
    default Rule<T> withErrorKey(String errorKey) {
        Objects.requireNonNull(errorKey, "errorKey cannot be null");
        return input ->
            this.test(input).mapErrors(ignore -> List.of(ErrorMessage.of(errorKey)));
    }

    /**
     * Lifts this {@link Rule} so it applies to a {@link List} of T instead of a single T.
     */
    @Override
    default Rule<List<T>> liftToVavrList() {
        return values -> MappingRule.super.liftToVavrList().test(values);
    }

    /**
     * Lifts this {@link Rule} so it applies to a {@link java.util.List} of T instead of a single T.
     */
    @Override
    default Rule<java.util.List<T>> liftToList() {
        return values -> MappingRule.super.liftToList().test(values);
    }

    /**
     * Lifts this {@link Rule} so it applies to an {@link Option} of T.
     * <p>
     * Semantics:
     * - None =&gt; {@code valid(None)} (nothing to validate)
     * - Some(x) =&gt; validate x, and return {@code valid(Some(x))} or {@code invalid(errors)}
     *
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
     * If you can't guarantee this, use the version of {@link #liftToVavrMap(Function)} that takes a keyExtractor function instead.
     * <p>
     * Semantics:
     * - If the Map is empty, the map is considered valid.
     * - Each value in the map is validated, and the resulting validations are collected.
     * - If any validation fails, the entire map is considered invalid.
     * - If all validations pass, the map is considered valid.
     */
    @Override
    default <K> Rule<Map<K, T>> liftToVavrMap() {
        return liftToVavrMap(Objects::toString);
    }

    /**
     * Lifts this {@link Rule} so it applies to a {@link Map} of K to T.
     * <p>
     * Behaves the same as {@link #liftToVavrMap()}, but uses the keyExtractor function to generate the path segment.
     * <p>
     * Semantics:
     * - If any validation fails, the entire map is considered invalid.
     * - If all validations pass, the map is considered valid.
     *
     * @param keyExtractor the function to extract a path segment from the key.
     */
    @Override
    default <K> Rule<Map<K, T>> liftToVavrMap(Function<K, Object> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor cannot be null");
        return map -> {
            Seq<Tuple2<K, Validation<T>>> validations = map.map(tuple ->
                    Tuple.of(tuple._1, this.test(tuple._2).mapErrors(errors ->
                            errors.map(e -> e.atIndex(keyExtractor.apply(tuple._1)))
                    ))
            );

            var validAndInvalid = validations.partition(t -> t._2.isValid());
            if (validAndInvalid._2.nonEmpty()) {
                return Validation.invalid(validAndInvalid._2.flatMap(t -> t._2.errors()).toList());
            } else {
                return Validation.valid(
                        validAndInvalid._1.toMap(
                                Tuple2::_1,
                                t ->
                                        t._2.getOrElseThrow()
                        )
                );
            }
        };
    }

    /**
     * Lifts this {@link Rule} so it applies to a {@link java.util.Map} of K to T.
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
     */
    @Override
    default <K> Rule<java.util.Map<K, T>> liftToMap() {
        return liftToMap(Objects::toString);
    }

    /**
     * Lifts this {@link Rule} so it applies to a {@link java.util.Map} of K to T.
     * <p>
     * Behaves the same as {@link #liftToMap()}, but uses the keyExtractor function to generate the path segment.
     * <p>
     * Semantics:
     * - If any validation fails, the entire map is considered invalid.
     * - If all validations pass, the map is considered valid.
     *
     * @param keyExtractor the function to extract a path segment from the key.
     */
    @Override
    default <K> Rule<java.util.Map<K, T>> liftToMap(Function<K, Object> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor cannot be null");
        return Rule.notNull().and(
                map -> {
                    Seq<Tuple2<K, Validation<T>>> validations = HashMap.ofAll(map).map(tuple ->
                            Tuple.of(tuple._1, this.test(tuple._2).mapErrors(errors ->
                                    errors.map(e -> e.atIndex(keyExtractor.apply(tuple._1)))
                            ))
                    );

                    var validAndInvalid = validations.partition(t -> t._2.isValid());
                    if (validAndInvalid._2.nonEmpty()) {
                        return Validation.invalid(validAndInvalid._2.flatMap(t -> t._2.errors()).toList());
                    } else {
                        return Validation.valid(
                                validAndInvalid._1.toMap(
                                        Tuple2::_1,
                                        t ->
                                                t._2.getOrElseThrow()
                                ).toJavaMap()
                        );
                    }
                }
        );
    }

    /**
     * Lift a Rule to work on a type V instead of T. You need to supply a Function that can get a V from the T.
     *
     * @see Rule#with(Function, Function)
     */
    default <V> Rule<V> given(Function<V, T> selector) {
        return Rule.with(selector, this);
    }

    /**
     * Composes two rules using "non-short-circuiting and" logic.
     * The combined rule is successful only if both rules are successful.
     * If both rules fail, the errors are combined.
     *
     * @see #andAlso(Function)
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
     * If no rules are passed, the value is considered to be valid.
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
     * If all rules fail, all errors from all rules are combined.
     * If no rules are passed, an {@link IllegalArgumentException} is thrown.
     */
    @SafeVarargs
    static <T> Rule<T> any(Rule<? super T>... rules) {
        Objects.requireNonNull(rules, "rules cannot be null");
        if(rules.length == 0) {
            throw new IllegalArgumentException("rules cannot be empty");
        }

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
     */
    @SuppressWarnings("unchecked")
    static <T> Rule<T> narrow(Rule<? super T> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return (Rule<T>) rule;
    }

    /**
     * Returns a {@link Rule} that validates the input is not null.
     * <p>
     * Error key: {@code must.not.be.null}
     *
     */
    static <T> Rule<T> notNull() {
        return input ->
                input == null ? Validation.invalid("must.not.be.null") : Validation.valid(input);
    }

    /**
     * Creates a new {@link Rule} that always returns a valid result for any non-null input.
     * <p>
     * Error key: {@code must.not.be.null} if input was null.
     */
    static <T> Rule<T> ok() {
        return input ->
                input == null ? Validation.invalid("must.not.be.null") : Validation.valid(input);
    }

    /**
     * Applies the specified {@link Rule} to the result of applying the selector function to the input.
     * Be careful, even if T and V are the same type, the returned value will be the original input, not the value retrieved from the selector.
     *
     * @param selector a function that extracts a value of type V from an input of type T
     */
    static <T, V> Rule<T> with(Function<? super T, ? extends V> selector, Function<? super V, ? extends Validation<? extends V>> rule) {
        Objects.requireNonNull(selector, "selector cannot be null");
        Objects.requireNonNull(rule, "rule cannot be null");
        return input ->
                Objects.requireNonNull(
                        rule.apply(selector.apply(input)),"rule cannot return a null Validation"
                ).map(ignore -> input);
    }
    
    /**
     * Only apply the Rule when condition evaluates to {@code true}, return a Valid otherwise without evaluating the Rule.
     */
    static <T> Rule<T> when(boolean condition, Function<? super T, ? extends Validation<T>> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return Rule.of(rule).onlyIf(() -> condition);
    }

    /**
     * Selects and returns one of the provided rules based on the given condition. As opposed to {@link #when(boolean, Function)}, there's always
     * a Rule beinng applied.
     *
     * @param condition a boolean determining which rule to select; if true, the first rule is chosen, otherwise the fallback rule
     */
    static <T> Rule<T> choose(boolean condition, Function<? super T, ? extends Validation<T>> rule, Function<? super T, ? extends Validation<T>> fallback) {
        Objects.requireNonNull(rule, "rule cannot be null");
        Objects.requireNonNull(fallback, "fallback cannot be null");
        return condition ? Rule.of(rule) : Rule.of(fallback);
    }

}
