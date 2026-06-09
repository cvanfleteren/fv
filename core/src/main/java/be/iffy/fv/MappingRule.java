package be.iffy.fv;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
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
 * The mapping can either succeed (producing a {@link Validation.Valid} R) or fail (producing an {@link Validation.Invalid} with error details).
 *
 */
@FunctionalInterface
public interface MappingRule<T, R> extends Function<T, Validation<R>> {

    /**
     * Evaluates the input against this rule, transforming it from type T to type R.
     *
     * @param value the value to be processed by this {@link MappingRule}
     * @return a {@link Validation} instance representing the outcome: either a {@link Validation.Valid}
     * with the successfully transformed value or a {@link Validation.Invalid} containing the errors encountered during
     * mapping or validation.
     */
    Validation<R> test(T value);

    @Override
    default Validation<R> apply(T value) {
        return test(value);
    }

    /**
     * Creates a new MappingRule that applies the given mapper function to the input.
     * If the mapper throws a {@link ValidationException}, the returned validation will be invalid with the same errors
     * as the thrown exception.
     * If the mapper throws any other exceptions, the rule will fail with the specified error message.
     */
    static <T, R> MappingRule<T, R> of(Function<? super T, ? extends R> mapper, String errorKey) {
        return of(mapper, ErrorMessage.of(errorKey));
    }

    /**
     * Creates a MappingRule from a function that returns a Try.
     * <p>
     * The tryProvider itself is invoked directly. If the tryProvider throws before returning a Try,
     * that exception is propagated. Only failures represented as Try.Failure are converted into Invalid.
     * <p>
     * If the Try fails with {@link ValidationException}, its errors are preserved, otherwise the provided error message is used.
     */
    static <T, R> MappingRule<T, R> ofTry(Function<? super T, ? extends Try<? extends R>> tryProvider, ErrorMessage errorMessage) {
        Objects.requireNonNull(tryProvider, "tryProvider cannot be null");
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        return input -> {
            if (input == null) {
                return Validation.invalid("must.not.be.null");
            }
            Try<? extends R> _try = Objects.requireNonNull(tryProvider.apply(input), "tryProvider cannot return null Try");
            return _try.fold(
                    t -> {
                        if (t instanceof ValidationException ve) {
                            return Validation.invalid(ve.errors());
                        } else {
                            return Validation.invalid(errorMessage);
                        }
                    },
                    Validation::valid
            );
        };
    }

    /**
     * Creates a MappingRule from a function that returns a Try.
     * <p>
     * The tryProvider itself is invoked directly. If the tryProvider throws before returning a Try,
     * that exception is propagated. Only failures represented as Try.Failure are converted into Invalid.
     * <p>
     * If the Try fails with {@link ValidationException}, its errors are preserved, otherwise the provided error message is used.
     */
    static <T, R> MappingRule<T, R> ofTry(Function<? super T, ? extends Try<? extends R>> tryProvider, String errorKey) {
        return ofTry(tryProvider, ErrorMessage.of(errorKey));
    }

    /**
     * Creates a new MappingRule that applies the given mapper function to the input.
     * If the throwingMapper throws an exception, the rule will fail with the specified error message.
     * If the throwingMapper throws {@link ValidationException}, the rule will fail with its errors.
     */
    static <T, R> MappingRule<T, R> of(Function<? super T, ? extends R> throwingMapper, ErrorMessage errorMessage) {
        Objects.requireNonNull(throwingMapper, "mapper cannot be null");
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        return ofTry(input -> Try.of(() -> throwingMapper.apply(input)), errorMessage);
    }

    /**
     * Creates an explicit {@link MappingRule} from a function that has the same signature.
     * Use this to easily treat existing functions as MappingRules.
     */
    static <T, R> MappingRule<T, R> of(Function<? super T, ? extends Validation<? extends R>> validationFunction) {
        Objects.requireNonNull(validationFunction, "validationFunction cannot be null");
        return input -> Validation.narrow(
                Objects.requireNonNull(validationFunction.apply(input), "validationFunction cannot return null Validation")
        );
    }

    /**
     * Composes this MappingRule with another MappingRule using "short-circuiting and" logic.
     * The combined rule is successful only if both this and the other rule are successful.
     * If this rule fails, the evaluation stops and the other rule is not evaluated.
     * <p>
     * This rule first applies the current rule to the input. If successful, it applies the next rule
     * (the argument to this method) to the result of the first rule.
     *
     * @param rule the rule to apply after this rule if this rule is successful.
     * @return a composed {@link MappingRule} that applies both rules in sequence.
     */
    default <Z> MappingRule<T, Z> then(Function<? super R, ? extends Validation<Z>> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return (T input) -> this.test(input).flatMap(rule::apply);
    }

    /**
     * Applies a mapping function to the result of this {@link MappingRule}.
     *
     * @param mapper the function to apply to the result of this rule if the input passes the test.
     * @return a new {@link MappingRule} that applies the mapping function to the result.
     */
    default <Z> MappingRule<T, Z> map(Function<? super R, ? extends Z> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return (T input) -> this.test(input).map(mapper);
    }

    /**
     * Maps the result of this rule to a constant value, ignoring the underlying value.
     *
     * @param value the constant value to map to.
     * @return a new MappingRule that maps the result of this rule to the specified constant value.
     */
    default <Z> MappingRule<T, Z> mapTo(Z value) {
        return this.map(ignored -> value);
    }

    /**
     * Composes this rule with another rule using "or" logic.
     * The combined rule is successful if either this or the other rule is successful.
     * If both rules fail, their errors are combined.
     * The fallback rule is evaluated only when this rule fails.
     *
     * @param other the other rule to compose with.
     */
    @SuppressWarnings("unchecked")
    default MappingRule<T, R> orElse(Function<? super T, ? extends Validation<R>> other) {
        Objects.requireNonNull(other, "other rule cannot be null");
        return input -> {
            Validation<R> first = this.test(input);
            if (first.isValid()) {
                return first;
            }

            Validation<R> second = Objects.requireNonNull(other.apply(input), "other cannot return null Validation");
            if (second.isValid()) {
                return second;
            }

            return Validation.invalid(first.errors().appendAll(second.errors()));
        };
    }

    /**
     * Returns a new {@link MappingRule} that first applies this rule, and if the input is invalid, falls back to the other rule.
     * If both rules fail, only the errors of the fallback rule are returned.
     * The fallback rule is evaluated only when this rule fails.
     */
    default <Z> MappingRule<T, R> recoverWith(Function<? super T, ? extends Validation<R>> other) {
        Objects.requireNonNull(other, "other rule cannot be null");
        return input -> {
            Validation<R> first = this.test(input);
            if (first.isValid()) {
                return first;
            }

            return Objects.requireNonNull(other.apply(input), "other cannot return null Validation");
        };
    }

    /**
     * Returns a new {@link MappingRule} that, when invalid, uses the passed errorKey as single ErrorMessage.
     */
    default MappingRule<T, R> withErrorKey(String errorKey) {
        Objects.requireNonNull(errorKey, "errorKey cannot be null");
        return input -> this.test(input).mapErrors(ignore -> List.of(ErrorMessage.of(errorKey)));
    }

    /**
     * Turns this rule (back) into a {@link Predicate}.
     */
    default <S extends T> Predicate<S> toPredicate() {
        return value -> test(value).isValid();
    }

    /**
     * Lifts a {@link MappingRule} so it applies to a {@link List} of T instead of a single T.
     * If the List is empty, the List is considered valid.
     */
    default MappingRule<List<T>, List<R>> liftToVavrList() {
        return values -> {
            List<Validation<R>> validations = values.map(this::test);
            // Validation.sequence already adds the [index] path segment, so we don't do it here.
            return Validation.transpose(validations);
        };
    }

    /**
     * Lifts a {@link MappingRule} so it applies to a {@link java.util.List} of T instead of a single T.
     * If the List is empty, the List is considered valid.
     */
    default MappingRule<java.util.List<T>, java.util.List<R>> liftToList() {
        return values -> {
            java.util.List<Validation<R>> validations = values.stream().map(this::test).toList();
            // Validation.sequence already adds the [index] path segment, so we don't do it here.
            return Validation.transpose(validations);
        };
    }

    /**
     * Lifts the current mapping rule to operate on the content of {@link Option} containers.
     * Empty Options (None) are considered to be valid.
     */
    default MappingRule<Option<T>, Option<R>> liftToOption() {
        return opt -> opt
                .map(v -> this.test(v).map(Option::of))
                .getOrElse(() -> Validation.valid(Option.none()));
    }

    /**
     * Lifts the current mapping rule to operate on the content of {@link Optional} containers.
     * Empty Optionals are considered to be valid.
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
     * If you can't guarantee this, use the version of {@link #liftToVavrMap(Function)} that takes a keyExtractor function instead.
     * <p>
     * Semantics:
     * - Each value in the map is validated, and the resulting validations are collected.
     * - If any validation fails, the entire map is considered invalid.
     * - If all validations pass, the map is considered valid.
     */
    default <K> MappingRule<Map<K, T>, Map<K, R>> liftToVavrMap() {
        return liftToVavrMap(Objects::toString);
    }

    /**
     * Lifts this {@link MappingRule} so it applies to a {@link Map} of K to T.
     * <p>
     * Behaves the same as {@link #liftToVavrMap()}, but uses the keyExtractor function to generate the path segment.
     * <p>
     * Semantics:
     * - If the Map is empty, the map is considered valid.
     * - Each value in the map is validated, and the resulting validations are collected.
     * - If any validation fails, the entire map is considered invalid.
     * - If all validations pass, the map is considered valid.
     *
     * @param keyExtractor the function to extract a path segment from the key.
     */
    default <K> MappingRule<Map<K, T>, Map<K, R>> liftToVavrMap(Function<K, Object> keyExtractor) {
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
     * Lifts this {@link MappingRule} so it applies to a {@link java.util.Map} of K to T.
     * <p>
     * Be careful, the key {@code key.toString()} will be used as part of the path segment.
     * Make sure to have a key that has a meaningful string representation for this.
     * If you can't guarantee this, use the version of {@link #liftToMap(Function)} that takes a keyExtractor function instead.
     * <p>
     * Semantics:
     * - Each value in the map is validated, and the resulting validations are collected.
     * - If any validation fails, the entire map is considered invalid.
     * - If all validations pass, the map is considered valid.
     */
    default <K> MappingRule<java.util.Map<K, T>, java.util.Map<K, R>> liftToMap() {
        return liftToMap(Objects::toString);
    }

    /**
     * Lifts this {@link MappingRule} so it applies to a {@link java.util.Map} of K to T.
     * <p>
     * Behaves the same as {@link #liftToMap()}, but uses the keyExtractor function to generate the path segment.
     * <p>
     * Semantics:
     * - If the Map is empty, the map is considered valid.
     * - Each value in the map is validated, and the resulting validations are collected.
     * - If any validation fails, the entire map is considered invalid.
     * - If all validations pass, the map is considered valid.
     *
     * @param keyExtractor the function to extract a path segment from the key.
     */
    default <K> MappingRule<java.util.Map<K, T>, java.util.Map<K, R>> liftToMap(Function<K, Object> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor cannot be null");
        return value -> liftToVavrMap(keyExtractor).test(HashMap.ofAll(value)).map(Map::toJavaMap);
    }

    /**
     * Returns a MappingRule that validates the input is not null.
     * <p>
     * Error key: "must.not.be.null"
     *
     * @return a MappingRule that returns valid input only if it's not null
     */
    static <T> MappingRule<T, T> notNull() {
        return input ->
                input == null ? Validation.invalid("must.not.be.null") : Validation.valid(input);
    }

    /**
     * Applies the specified {@link MappingRule} to the result of applying the selector function to the input. Aka <code>contramap</code>.
     *
     * @param selector a function that extracts a value of type V from an input of type T
     * @param rule     the rule to be applied to the extracted value
     * @return a new {@link MappingRule} that tests the applied selector and rule combination
     */
    static <T, V, R> MappingRule<T, R> with(Function<T, V> selector, Function<? super V, ? extends Validation<? extends R>> rule) {
        Objects.requireNonNull(selector, "selector cannot be null");
        Objects.requireNonNull(rule, "rule cannot be null");
        return input -> Validation.narrow(
                Objects.requireNonNull(rule.apply(selector.apply(input)), "rule cannot return null Validation")
        );
    }
}
