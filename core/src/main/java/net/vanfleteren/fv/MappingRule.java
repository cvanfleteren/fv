package net.vanfleteren.fv;

import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Option;
import io.vavr.control.Try;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents a rule for mapping an input of type T to an output of type R,
 * with built-in validation support.
 * The mapping can either succeed (producing a {@link net.vanfleteren.fv.Validation.Valid} R) or fail (producing an {@link net.vanfleteren.fv.Validation.Invalid} with error details).
 *
 * @param <T> the type of input to be mapped
 * @param <R> the type of output after successful mapping
 */
@FunctionalInterface
public interface MappingRule<T, R> {

    /**
     * Evaluates the input against this rule, transforming it from type T to type R.
     * {@snippet file="net/vanfleteren/fv/MappingRuleSnippets.java" region="test-example"}
     *
     * @param value the value to be processed by this {@link MappingRule}
     * @return a {@link Validation} instance representing the outcome: either a {@link net.vanfleteren.fv.Validation.Valid}
     *         with the successfully transformed value or the errors encountered during
     *         mapping or validation
     */
    Validation<R> test(T value);

    /**
     * Creates a new MappingRule that applies the given mapper function to the input.
     * If the mapper throws an exception, the rule will fail with the specified error message.
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/MappingRuleSnippets.java" region="of-string-example"}
     *
     * @param <T>      the type of input to be mapped
     * @param <R>      the type of output after mapping
     * @param mapper   the function that maps T to R
     * @param errorKey the errorKey to use if the mapping fails.
     * @return a new {@link MappingRule} that applies the mapper and validates the result
     */
    static <T, R> MappingRule<T, R> of(Function<T, R> mapper, String errorKey) {
        return of(mapper, ErrorMessage.of(errorKey));
    }

    /**
     * Creates a new MappingRule that applies the given mapper function to the input.
     * If the mapper returns a {@link Try.Failure}, the rule will fail with the specified error key.
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/MappingRuleSnippets.java" region="of-try-error-example"}
     *
     * @param <T>          the type of input to be mapped
     * @param <R>          the type of output after mapping
     * @param mapper       the function that maps T to R, returning a Try
     * @param errorMessage the errorMessage to use if the mapping fails.
     * @return a new {@link MappingRule} that applies the mapper and validates the result
     */
    static <T, R> MappingRule<T, R> ofTry(Function<T, Try<R>> mapper, ErrorMessage errorMessage) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        return input -> {
            Try<R> _try = mapper.apply(input);
            return _try.fold(
                    t -> Validation.invalid(errorMessage),
                    Validation::valid
            );
        };
    }

    /**
     * Creates a new MappingRule that applies the given mapper function to the input.
     * If the mapper returns a {@link Try.Failure}, the rule will fail with the specified error key.
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/MappingRuleSnippets.java" region="of-try-string-example"}
     *
     * @param <T>      the type of input to be mapped
     * @param <R>      the type of output after mapping
     * @param mapper   the function that maps T to R
     * @param errorKey the errorKey to use if the mapping fails.
     * @return a new {@link MappingRule} that applies the mapper and validates the result
     */
    static <T, R> MappingRule<T, R> ofTry(Function<T, Try<R>> mapper, String errorKey) {
        return ofTry(mapper, ErrorMessage.of(errorKey));
    }

    /**
     * Creates a new MappingRule that applies the given mapper function to the input.
     * If the mapper throws an exception, the rule will fail with the specified error message.
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/MappingRuleSnippets.java" region="of-error-example"}
     *
     * @param <T>            the type of input to be mapped
     * @param <R>            the type of output after mapping
     * @param throwingMapper the function that maps T to R
     * @param errorMessage   the error message to use if the mapping fails.
     * @return a new {@link MappingRule} that applies the mapper and validates the result
     */
    static <T, R> MappingRule<T, R> of(Function<T, R> throwingMapper, ErrorMessage errorMessage) {
        Objects.requireNonNull(throwingMapper, "mapper cannot be null");
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        return input -> {
            Option<R> result = Function1.lift(throwingMapper).apply(input);
            return result.fold(
                    () -> Validation.invalid(errorMessage),
                    value -> Validation.valid(value)
            );
        };
    }

    /**
     * Returns a composed MappingRule that represents a shortcut-if-this rule.
     * This rule first applies the current rule to the input, and if successful,
     * applies the next rule (the argument to this method) to the result of the first rule.
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/MappingRuleSnippets.java" region="and-then-example"}
     *
     * @param <Z>  the type of output from the next rule after transformation.
     * @param rule the rule to apply after this rule if this rule is successful.
     * @return a composed MappingRule that represents a shortcut-if-this rule.
     */
    default <Z> MappingRule<T, Z> andThen(MappingRule<? super R, ? extends Z> rule) {
        return (T input) -> this.test(input).flatMap(rule::test);
    }

    /**
     * Applies a mapping function to the result of this {@link MappingRule}.
     *
     * @param <Z> the type of the result after applying the mapping function.
     * @param mapper the function to apply to the result of this rule if the input passes the test.
     * @return a new {@link MappingRule} that applies the mapping function to the result.
     */
    default <Z> MappingRule<T, Z> map(Function<R, Z> mapper){
        return (T input) -> this.test(input).map(mapper);
    }

    /**
     * Maps the result of this rule to a constant value, ignoring the underlying value.
     *
     * @param <Z> the type of the mapped value.
     * @param value the constant value to map to.
     * @return a new MappingRule that maps the result of this rule to the specified constant value.
     */
    default <Z> MappingRule<T, Z> mapTo(Z value){
        return this.map(ignored -> value);
    }

    /**
     * Composes this rule with another rule using "or" logic.
     * The combined rule is successful if either this or the other rule is successful.
     * If both rules fail, their errors are combined.
     *
     * @param other the other rule to compose with.
     * @param <S>   the target type.
     * @return a new {@link MappingRule} instance.
     * @throws NullPointerException if {@code other} is null.
     */
    @SuppressWarnings("unchecked")
    default <S> MappingRule<T, S> orElse(MappingRule<? super T, ? extends S> other) {
        Objects.requireNonNull(other, "other rule cannot be null");
        return input -> {
            Validation<S> first = (Validation<S>) this.test(input);
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
     * Returns a new {@link MappingRule} that first applies this rule, and if the input is invalid, falls back to the other rule.
     * <p>
     * Usage example:
     * <pre>{@code
     * // 1. A rule that maps the string "A" to 1
     * MappingRule<String, Integer> ruleA = s ->
     *     "A".equals(s) ? Validation.valid(1) : Validation.invalid("not.A");
     *
     * // 2. A rule that maps the string "B" to 2
     * MappingRule<String, Integer> ruleB = s ->
     *     "B".equals(s) ? Validation.valid(2) : Validation.invalid("not.B");
     *
     * // 3. Use recoverWith to try ruleA, and fall back to ruleB if ruleA fails
     * MappingRule<String, Integer> combined = ruleA.recoverWith(ruleB);
     *
     * // 4. Usage
     * Validation<Integer> validA = combined.test("A"); // Returns Valid(1)
     * Validation<Integer> validB = combined.test("B"); // Returns Valid(2)
     * Validation<Integer> invalid = combined.test("C"); // Returns Invalid("not.B")
     * }</pre>
     *
     * @param <S> the type of valid output produced by the other rule
     * @param other the other rule to use as a fallback if this rule fails
     * @return a new MappingRule that first applies this rule, and if the input is invalid, falls back to the other rule
     */
    default <S> MappingRule<T, S> recoverWith(MappingRule<? super T, S> other) {
        Objects.requireNonNull(other, "other rule cannot be null");
        return input -> {
            Validation<R> first = this.test(input);
            if (first.isValid()) {
                return (Validation<S>) first;
            }

            return Validation.narrow(other.test(input));
        };
    }

    /**
     * Turns this rule (back) into a {@link Predicate}.
     *
     * @param <S> the target type.
     * @return a {@link Predicate} instance.
     */
    default <S extends T> Predicate<S> toPredicate() {
        return value -> test(value).isValid();
    }

    /**
     * Lifts a {@link MappingRule} so it applies to a {@link List} of T instead of a single T.
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/MappingRuleSnippets.java" region="lift-to-list-example"}
     *
     * @return a new {@link MappingRule} instance.
     */
    default MappingRule<List<T>, List<R>> liftToList() {
        return values -> {
            List<Validation<R>> validations = values.map(this::test);
            // Validation.sequence already adds the [index] path segment, so we don't do it here.
            return Validation.sequence(validations);
        };
    }

    /**
     * Lifts this {@link MappingRule} so it applies to an {@link Option} of T.
     * <p>
     * Semantics:
     * - None =&gt; {@code valid(None)} (nothing to validate)
     * - Some(x) =&gt; validate x, and return {@code valid(Some(x))} or {@code invalid(errors)}
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/MappingRuleSnippets.java" region="lift-to-option-example"}
     *
     * @return a new {@link MappingRule} instance.
     */
    default MappingRule<Option<T>, Option<R>> liftToOption() {
        return opt -> opt
                .map(v -> this.test(v).map(Option::of))
                .getOrElse(() -> Validation.valid(Option.none()));
    }

    /**
     * Lifts this {@link MappingRule} so it applies to an {@link java.util.Optional} of T.
     * <p>
     * Semantics:
     * - empty =&gt; {@code valid(Optional.empty)} (nothing to validate)
     * - not empty =&gt; validate x, and return {@code valid(Optional.of(x))} or {@code invalid(errors)}
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/MappingRuleSnippets.java" region="lift-to-optional-example"}
     *
     * @return a new {@link MappingRule} instance.
     */
    default MappingRule<Optional<T>, Optional<R>> liftToOptional() {
        return opt -> opt
                .map(v -> this.test(v).map(Optional::of))
                .orElse(Validation.valid(Optional.empty()));
    }

    /**
     * Lifts this {@link MappingRule} so it applies to a {@link Map} of K to T.
     * <p>
     * Be careful, the key {@code key.toString()} will be used as part of the path segment.
     * Make sure to have a key that has a meaningful string representation for this.
     * If you can't guarantee this, use the version of {@link #liftToMap(Function)} that takes a keyExtractor function instead.
     * <p>
     * Semantics:
     * - Each value in the map is validated, and the resulting validations are collected.
     * - If any validation fails, the entire map is considered invalid.
     * - If all validations pass, the map is considered valid.
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/MappingRuleSnippets.java" region="lift-to-map-example"}
     *
     * @param <K> the key type.
     * @return a new {@link MappingRule} instance.
     */
    default <K> MappingRule<Map<K, T>, Map<K, R>> liftToMap() {
        return liftToMap(Objects::toString);
    }

    /**
     * Lifts this {@link MappingRule} so it applies to a {@link Map} of K to T.
     * <p>
     * Behaves the same as {@link #liftToMap()}, but uses the keyExtractor function to generate the path segment.
     * <p>
     * Semantics:
     * - Each value in the map is validated, and the resulting validations are collected.
     * - If any validation fails, the entire map is considered invalid.
     * - If all validations pass, the map is considered valid.
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/MappingRuleSnippets.java" region="lift-to-map-extractor-example"}
     *
     * @param keyExtractor the function to extract a path segment from the key.
     * @param <K>          the key type.
     * @return a new {@link MappingRule} instance.
     */
    default <K> MappingRule<Map<K, T>, Map<K, R>> liftToMap(Function<K, Object> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor cannot be null");
        return map -> {
            Seq<Tuple2<K, Validation<R>>> validations = map.map(tuple ->
                    Tuple.of(tuple._1, this.test(tuple._2).mapErrors(errors ->
                            errors.map(e -> e.atIndex(keyExtractor.apply(tuple._1)))
                    ))
            );

            var validAndInvalid = validations.partition(t -> t._2.isValid());
            if (validAndInvalid._2.nonEmpty()) {
                return Validation.invalid(validAndInvalid._2.flatMap(t -> t._2.errors()).toList());
            } else {
                return Validation.valid(validAndInvalid._1.toMap(Tuple2::_1, t -> t._2.getOrElseThrow()));
            }
        };
    }

    /**
     * Returns a {@link MappingRule} that checks if the input {@link Option} is defined and then applies the given rule to its value.
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/MappingRuleSnippets.java" region="required-option-example"}
     *
     * @param <T>  the type of the value inside the {@link Option}
     * @param <R>  the type of the result of the mapping rule
     * @param rule the mapping rule to apply to the value inside the {@link Option}
     * @return a new {@link MappingRule} that validates the option and applies the given rule to its value
     */
    static <T, R> MappingRule<Option<T>, R> requiredOption(MappingRule<T, R> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return rule.liftToOption().andThen(opt -> opt.fold(() -> Validation.invalid("must.not.be.empty"), Validation::valid));
    }

    /**
     * Returns a {@link MappingRule} that checks if the input {@link Optional} is defined and then applies the given rule to its value.
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/MappingRuleSnippets.java" region="required-optional-example"}
     *
     * @param <T>  the type of the value inside the {@link Optional}
     * @param <R>  the type of the result of the mapping rule
     * @param rule the mapping rule to apply to the value inside the {@link Optional}
     * @return a new {@link MappingRule} that validates the optional and applies the given rule to its value
     */
    static <T, R> MappingRule<Optional<T>, R> requiredOptional(MappingRule<T, R> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return rule.liftToOptional().andThen(opt -> opt.map(Validation::valid).orElseGet(() -> Validation.invalid("must.not.be.empty")));
    }

    /**
     * Returns a MappingRule that validates the input is not null.
     * <p>
     * Error key: "must.not.be.null"
     *
     * @param <T> the type of input and output
     * @return a MappingRule that returns valid input only if it's not null
     */
    static <T> MappingRule<T, T> notNull() {
        return input -> input == null ? Validation.invalid("must.not.be.null") : Validation.valid(input);
    }

    /**
     * Applies the specified {@link MappingRule} to the result of applying the selector function to the input. Aka <code>contramap</code>.
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/MappingRuleSnippets.java" region="with-example"}
     *
     * @param <T>      the type of the input to be tested
     * @param <V>      the type of the result produced by the selector function
     * @param <R>      the type of the output produced by the rule
     * @param selector a function that extracts a value of type V from an input of type T
     * @param rule     the rule to be applied to the extracted value
     * @return a new {@link MappingRule} that tests the applied selector and rule combination
     */
    static <T, V, R> MappingRule<T, R> with(Function<T, V> selector, MappingRule<? super V, ? extends R> rule) {
        return input -> Validation.narrow(rule.test(selector.apply(input)));
    }
}
