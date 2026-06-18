package be.iffy.fv;

import be.iffy.fv.Validation.Invalid;
import io.vavr.collection.List;
import io.vavr.collection.Stream;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents a validation rule that can be applied to a value.
 *
 * <p>A Rule is essentially like a {@link java.util.function.Predicate} that returns a {@link Validation} instead of a boolean.
 * <p>
 * When making your own Rules, keep the following in mind:
 * <ul>
 *     <li>
 *         Rules are <em>not automatically nullSafe</em> (meaning the value they test might be null), but a Rule created with the
 *         {@code Rule.of(...)} factories will never pass the null to the predicate.
 *     </li>
 *     <li>
 *         Rules are <em>not supposed to change their input</em>, the value in the returned {@link be.iffy.fv.Validation.Valid} should be the <em>same instance</em> as the input.
 *          To change the input (type or value), use a {@link MappingRule}
 *      </li>
 *     <li>
 *         If you need to transform the input, but keep the type, you can look at using a {@link Transformation}, for example like this (using the DSL)
 *     {@snippet :
 *     Rule<String> originalRule = after(stringOps.trim()).is(strings.maxLength(2));
 *     Validation<String> v = originalRule.apply("  12  "); // Valid
 *}
 *     </li>
 * </ul>
 *
 */
@FunctionalInterface
public interface Rule<T> extends Function<T, Validation<T>> {

    /**
     * Tests the given value against the rule. If the value passes the test,
     * a {@link Validation.Valid} containing the exact same instance will be returned.
     *
     * @param value the value to be validated.
     * @return a {@link Validation} object indicating the result of the test.
     */
    @Override
    Validation<T> apply(T value);

    //region Factory methods

    /**
     * Creates a {@link Rule} from the given predicate and error message key.
     * If the Predicate resolves to {@code true}, the Rule is considered {@link be.iffy.fv.Validation.Valid}
     */
    static <T> Rule<T> of(Predicate<? super T> predicate, String errorKey) {
        return of(predicate, ErrorMessage.of(errorKey));
    }

    /**
     * Make a {@code Rule<T>} from a function that shares the same signature.
     * Rule semantics will be enforced, so the ruleLikeFunction won't be able to return another value.
     */
    static <T> Rule<T> of(Function<? super T, ? extends Validation<? extends T>> ruleLikeFunction) {
        Objects.requireNonNull(ruleLikeFunction, "ruleLikeFunction cannot be null");

        if (ruleLikeFunction instanceof Rule) {
            // no need to wrap if the function is already a Rule
            @SuppressWarnings("unchecked")
            Rule<T> alreadyRule = (Rule<T>) ruleLikeFunction;
            return alreadyRule;
        }

        return input -> {
            if (input == null) {
                return Invalid.notNull();
            }
            return Validation.narrow(
                // protect ourselves against misbehaving Rules by returning the input, as per contract.
                Objects.requireNonNull(ruleLikeFunction.apply(input), "ruleLikeFunction cannot return null Validation").mapTo(input)
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
     * Returns a {@link Rule} that validates the input is not null.
     * <p>
     * Error key: {@code must.not.be.null}
     */
    static <T> Rule<T> notNull() {
        return input ->
            input == null ? Invalid.notNull() : Validation.valid(input);
    }

    //endregion

    //region combinators

    /**
     * Composes this rule with another rule using "non-short-circuiting and" logic.
     * The combined rule is successful only if both this and the other rule are successful.
     * If both rules fail, their errors are combined.
     * <p>
     * Use this for independent rules where you want all errors reported at once.
     * Use {@link #then(Rule)} instead when the second rule depends on the first succeeding.
     * <p>
     * Non-short-circuiting, accumulating.
     */
    default <S extends T> Rule<S> and(Function<? super S, ? extends Validation<?>> other) {
        Objects.requireNonNull(other, "other rule cannot be null");
        // map back to original input so we're protected against other returning an incompatible value
        return input ->
            Validations.combine(
                    apply(input),
                    other.apply(input)
                )
                .map((v, o) -> input);
    }

    /**
     * Composes multiple rules using "non-short-circuiting and" logic.
     * The combined rule is successful only if all rules are successful.
     * Errors of all failing rules are combined.
     * If no rules are passed, the value is considered to be valid if it is non-null.
     * <p>
     * Non-short-circuiting, accumulating.
     */
    @SafeVarargs
    static <T> Rule<T> all(Function<? super T, ? extends Validation<T>>... rules) {
        Objects.requireNonNull(rules, "rules cannot be null");
        List.of(rules).forEach(rule -> Objects.requireNonNull(rule,"rule cannot be null"));

        return value -> {
            if (value == null) {
                return Invalid.notNull();
            }
            List<Validation<T>> validations = List.of(rules).map(rule ->
                Objects.requireNonNull(rule.apply(value),"rule cannot return null Validation")
            );
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
     * From the moment a single successful Rule is found, the other rules will not be evaluated anymore.
     * If all rules fail, all errors from all rules are combined.
     * If no rules are passed, an {@link IllegalArgumentException} is thrown.
     * <p>
     * Short-circuiting, accumulating.
     */
    @SafeVarargs
    static <T> Rule<T> any(Function<? super T, ? extends Validation<T>>... rules) {
        Objects.requireNonNull(rules, "rules cannot be null");
        if (rules.length == 0) {
            throw new IllegalArgumentException("rules cannot be empty");
        }
        Stream.of(rules).forEach(r -> Objects.requireNonNull(r, "rules cannot be null"));

        return value -> {
            if (value == null) {
                return Invalid.notNull();
            }

            // we use a Stream of Lazy to ensure each rule is applied at most once per validation run
            Stream<io.vavr.Lazy<Validation<T>>> lazyValidations = Stream.of(rules)
                    .map(rule -> io.vavr.Lazy.of(() -> rule.apply(value)));

            return lazyValidations
                    .map(io.vavr.Lazy::get)
                    .find(Validation::isValid)
                    .getOrElse(() ->
                            Validation.invalid(lazyValidations.flatMap(l -> l.get().errors()).toList())
                    );
        };
    }

    /**
     * Returns a new {@link Rule} that first applies this rule, and if the input is invalid, falls back to the {@code other} rule.
     * The difference with {@link #or(Function)} is that only the errors of the {@code other} Rule will be returned if both fail.
     * The fallback rule is evaluated only when this rule fails.
     * <p>
     * Short-circuiting, not accumulating.
     */
    default Rule<T> fallback(Function<? super T, ? extends Validation<T>> other) {
        Objects.requireNonNull(other, "other rule cannot be null");
        return input -> {
            if (input == null) {
                return Invalid.notNull();
            }

            Validation<T> first = this.apply(input);
            if (first.isValid()) {
                return first;
            }

            // make sure we stick to the Rule contract and return the original input
            return Validation.narrowSuper(other.apply(input).map(ignored -> input));
        };
    }

    /**
     * Composes this rule with another rule using "or" logic.
     * The combined rule is successful if either this or the other rule is successful.
     * If both rules fail, their errors are combined.
     * The other rule is only evaluated if this rule fails.
     * <p>
     * Short-circuiting, accumulating
     */
    default <S extends T> Rule<S> or(Function<? super S, ? extends Validation<?>> other) {
        Objects.requireNonNull(other, "other rule cannot be null");
        return input -> {
            if (input == null) {
                return Invalid.notNull();
            }

            Validation<S> first = this.<S>narrow().apply(input);
            if (first.isValid()) {
                return first;
            }
            //make sure we stick to the Rule contract and return the original input
            Validation<S> second = other.apply(input).map(ignore -> input);
            if (second.isValid()) {
                return second;
            }

            return Validation.invalid(first.errors().appendAll(second.errors()));
        };
    }

    /**
     * Composes this rule with another rule in sequence, short-circuiting on failure.
     * The combined rule succeeds only if both pass. If this rule fails, {@code other} is not evaluated.
     * <p>
     * Use this when {@code other} depends on this rule succeeding first (e.g. {@code notNull().then(minLength(3))}).
     * Use {@link #and(Function)} instead when the two rules are independent and you want all errors reported.
     * <p>
     * Short-circuiting, not accumulating. Returns a {@link Rule} (not a {@link MappingRule}).
     */
    default Rule<T> then(Rule<? super T> other) {
        Objects.requireNonNull(other, "other rule cannot be null");
        return input ->
            apply(input).flatMap(v ->
                // map back to original input so we're protected against other returning an incompatible value
                other.apply(input).map(ignored -> input)
            );
    }

    /**
     * Pass the result of this Rule to the given mapping function.
     * <p>
     * Short-circuiting, not accumulating.
     */
    default <R> MappingRule<T, R> then(Function<? super T, ? extends Validation<? extends R>> ruleLikeFunction) {
        return input ->
            apply(input)
                .refine(MappingRule.of(ruleLikeFunction));
    }

    /**
     * Composes this rule with another using XOR logic.
     * Successful only if exactly one of the rules is successful.
     * Both rules will always be evaluated.
     * <p>
     * Non-short-circuiting, non-accumulating
     */
    default <S extends T> Rule<S> xor(Function<? super S, ? extends Validation<?>> other, String errorKey) {
        Objects.requireNonNull(errorKey, "errorKey cannot be null");
        return xor(other, ErrorMessage.of(errorKey));
    }

    /**
     * Composes this rule with another using XOR logic.
     * Successful only if exactly one of the rules is successful.
     * Both rules will always be evaluated.
     * <p>
     * Non-short-circuiting, non-accumulating
     */
    default <S extends T> Rule<S> xor(Function<? super S, ? extends Validation<?>> other, ErrorMessage errorMessage) {
        Objects.requireNonNull(other, "other cannot be null");
        Objects.requireNonNull(errorMessage, "errorKey cannot be null");
        return input -> {
            if (input == null) {
                return Invalid.notNull();
            }

            boolean v1Valid = this.apply(input).isValid();
            boolean v2Valid = other.apply(input).isValid();
            if (v1Valid ^ v2Valid) {
                return Validation.valid(input);
            }
            return Validation.invalid(errorMessage);
        };
    }

    /**
     * Composes multiple rules using "exactly one" logic.
     * Successful only if exactly one of the rules is successful.
     * All rules will always be evaluated.
     * Requires at least two rules; throws {@link IllegalArgumentException} otherwise.
     * <p>
     * Non-short-circuiting, non-accumulating.
     *
     * @param errorKey the error key to use if the validation fails.
     */
    @SafeVarargs
    static <T> Rule<T> exactlyOne(String errorKey, Function<? super T, ? extends Validation<T>>... rules) {
        Objects.requireNonNull(errorKey, "errorKey cannot be null");
        return exactlyOne(ErrorMessage.of(errorKey), rules);
    }

    /**
     * Composes multiple rules using "exactly one" logic.
     * Successful only if exactly one of the rules is successful.
     * All rules will always be evaluated.
     * Requires at least two rules; throws {@link IllegalArgumentException} otherwise.
     * <p>
     * Non-short-circuiting, non-accumulating.
     *
     * @param errorMessage the error message to use if the validation fails.
     */
    @SafeVarargs
    static <T> Rule<T> exactlyOne(ErrorMessage errorMessage, Function<? super T, ? extends Validation<T>>... rules) {
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        Objects.requireNonNull(rules, "rules cannot be null");
        if (rules.length < 2) {
            throw new IllegalArgumentException("exactlyOne requires at least 2 rules");
        }
        List.of(rules).forEach(rule -> Objects.requireNonNull(rule, "rule cannot be null"));

        return value -> {
            if (value == null) {
                return Invalid.notNull();
            }
            int validCount = List.of(rules).count(rule -> rule.apply(value).isValid());
            return validCount == 1
                ? Validation.valid(value)
                : Validation.invalid(errorMessage);
        };
    }

    //endregion


    //region modifiers

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
            Validation<T> original = this.apply(input);
            return original.fold(
                invalid -> Validation.valid(input),
                valid -> Validation.invalid(negatedError)
            );
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
            if (input == null) {
                return Invalid.notNull();
            }
            if (condition.test(input)) {
                return this.apply(input);
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
            if (input == null) {
                return Invalid.notNull();
            }

            boolean shouldRun = Objects.requireNonNull(condition.get(), "condition result cannot be null");
            if (shouldRun) {
                return this.apply(input);
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
    default Rule<T> onlyIf(boolean condition) {
        return input -> {
            if (input == null) {
                return Invalid.notNull();
            }

            if (condition) {
                return this.apply(input);
            }
            return Validation.valid(input);
        };
    }

    /**
     * Returns a new {@link Rule} that, when invalid, uses the passed errorKey as single ErrorMessage.
     */
    default Rule<T> withErrorKey(String errorKey) {
        Objects.requireNonNull(errorKey, "errorKey cannot be null");
        return input ->
            this.apply(input).mapErrors(ignore -> List.of(ErrorMessage.of(errorKey)));
    }

    /**
     * Converts this Rule into a {@link Predicate} that tests whether
     * the given input satisfies the rule's conditions.
     */
    default <S extends T> Predicate<S> toPredicate() {
        return value -> apply(value).isValid();
    }

    /**
     * Lift a Rule to work on a type V instead of T. You need to supply a Function that can get a T from a V.
     */
    default <V> Rule<V> on(PropertySelector<V, T> selector) {
        return Rule.on(selector, this);
    }

    /**
     * Applies the specified {@link Rule} to the result of applying the selector function to the input.
     * Be careful, even if T and V are the same type, the returned value will be the original input, not the value retrieved from the selector.
     *
     * @param selector a function that extracts a value of type V from an input of type T
     */
    static <T, V> Rule<T> on(PropertySelector<? super T, ? extends V> selector, Function<? super V, ? extends Validation<? extends V>> rule) {
        Objects.requireNonNull(selector, "selector cannot be null");
        Objects.requireNonNull(rule, "rule cannot be null");
        return input ->
            Objects.requireNonNull(
                    rule.apply(selector.apply(input)),
                    "rule cannot return a null Validation"
                )
                .map(ignore -> input)
                .at(selector.getPropertyName());
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
     * a Rule being applied.
     *
     * @param condition a boolean determining which rule to select; if true, the first rule is chosen, otherwise the second rule
     */
    static <T> Rule<T> choose(boolean condition, Function<? super T, ? extends Validation<T>> first, Function<? super T, ? extends Validation<T>> second) {
        Objects.requireNonNull(first, "first cannot be null");
        Objects.requireNonNull(second, "second cannot be null");
        return condition ? Rule.of(first) : Rule.of(second);
    }

    //endregion


    /**
     * Lift a Rule by giving you access to the RuleLifter, allowing you to lift this Rule into many other types.
     */
    default RuleLifter<T> lift() {
        return new RuleLifter<>(this);
    }

    /**
     * Narrows the current rule to a more specific subtype.
     * This is possible because a Rule that can validate a type can also validate all possible subtypes.
     * <p>
     * So you can for example use this to treat a {@code Rule<Number>} as a {@code Rule<Integer>}.
     * Example:
     * {@snippet :
     *   Rule<Number> isPositive = Rule.of(n -> n.doubleValue() > 0, "must.be.positive");
     *   Rule<Integer> isMinusFortyTwo = Rule.of(b -> b == -42, "must.be.minus.forty.two");
     *   Rule<Integer> combined = isMinusFortyTwo.fallback(isPositive.narrow());
     *}
     *
     * @param <S> the subtype of T to narrow the rule to
     * @return a new Rule instance narrowed to the specified subtype S
     */
    @SuppressWarnings("unchecked")
    default <S extends T> Rule<S> narrow() {
        return (Rule<S>) this;
    }
}
