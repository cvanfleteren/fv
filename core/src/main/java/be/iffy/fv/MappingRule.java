package be.iffy.fv;

import be.iffy.fv.Validation.Invalid;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import io.vavr.control.Try;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static be.iffy.fv.Validation.invalid;

/**
 * Represents a rule for mapping an input of type T to an output of type R,
 * with built-in validation support.
 * The mapping can either succeed (producing a {@link Validation.Valid} R) or fail (producing an {@link Invalid} with error details).
 *
 */
@FunctionalInterface
public interface MappingRule<T, R> extends  ValidationOperator<T, R> {

    /**
     * Evaluates the input against this rule, transforming it from type T to type R.
     *
     * @param value the value to be processed by this {@link MappingRule}
     * @return a {@link Validation} instance representing the outcome: either a {@link Validation.Valid}
     * with the successfully transformed value or a {@link Invalid} containing the errors encountered during
     * mapping or validation.
     */
    Validation<R> test(T value);

    @Override
    default Validation<R> apply(T value) {
        return test(value);
    }

    //region factory methods

    /**
     * Creates an explicit {@link MappingRule} from a function that has the same signature.
     * Use this to easily treat existing functions as ValidationCombiners.
     */
    static <T, R> MappingRule<T, R> of(Function<? super T, ? extends Validation<? extends R>> validationFunction) {
        Objects.requireNonNull(validationFunction, "validationFunction cannot be null");
        return input -> {
            if(input == null) {
                return Invalid.notNull();
            }
            return Validation.narrow(
                    Objects.requireNonNull(
                            validationFunction.apply(input),
                            "validationFunction cannot return null Validation"
                    )
            );
        };
    }

    /**
     * Creates a new MappingRule that applies the given mapper function to the input.
     * If the throwingMapper throws an exception, the rule will fail with the specified error message.
     * If the throwingMapper throws {@link ValidationException}, the rule will fail with its errors.
     */
    static <T, R> MappingRule<T, R> catching(Function<? super T, ? extends R> throwingMapper, String errorKey) {
        return catching(throwingMapper, ErrorMessage.of(errorKey));
    }

    /**
     * Creates a new MappingRule that applies the given mapper function to the input.
     * If the throwingMapper throws an exception, the rule will fail with the specified error message.
     * If the throwingMapper throws {@link ValidationException}, the rule will fail with its errors.
     */
    static <T, R> MappingRule<T, R> catching(Function<? super T, ? extends R> throwingMapper, ErrorMessage errorMessage) {
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        return catching(throwingMapper, (input, exception) -> errorMessage);
    }

    /**
     * Creates a new MappingRule that applies the given mapper function to the input.
     * If the throwingMapper throws an exception, the rule will fail with an {@link ErrorMessage} created by the provided maker.
     * If the throwingMapper throws {@link ValidationException}, the rule will fail with its errors.
     */
    static <T, R> MappingRule<T, R> catching(Function<? super T, ? extends R> throwingMapper, BiFunction<? super T, Exception, ErrorMessage> errorMessageMaker) {
        Objects.requireNonNull(throwingMapper, "mapper cannot be null");
        Objects.requireNonNull(errorMessageMaker, "errorMessageMaker cannot be null");
        return input -> {
            if (input == null) {
                return Invalid.notNull();
            }
            try {
                return Validation.valid(throwingMapper.apply(input));
            } catch (ValidationException ve) {
                return invalid(ve.errors());
            } catch (Exception e) {
                return invalid(
                        Objects.requireNonNull(
                                errorMessageMaker.apply(input, e),
                                "errorMessageMaker result cannot be null"
                        )
                );
            }
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
    static <T, R> MappingRule<T, R> fromTry(Function<? super T, ? extends Try<? extends R>> tryProvider, String errorKey) {
        return fromTry(tryProvider, ErrorMessage.of(errorKey));
    }

    /**
     * Creates a MappingRule from a function that returns a Try.
     * <p>
     * The tryProvider itself is invoked directly. If the tryProvider throws before returning a Try,
     * that exception is propagated. Only failures represented as Try.Failure are converted into Invalid.
     * <p>
     * If the Try fails with {@link ValidationException}, its errors are preserved, otherwise the provided error message is used.
     */
    static <T, R> MappingRule<T, R> fromTry(Function<? super T, ? extends Try<? extends R>> tryProvider, ErrorMessage errorMessage) {
        Objects.requireNonNull(tryProvider, "tryProvider cannot be null");
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        return input -> {
            if (input == null) {
                return Invalid.notNull();
            }
            Try<? extends R> _try = Objects.requireNonNull(tryProvider.apply(input), "tryProvider cannot return null Try");
            return _try.fold(
                    t -> {
                        if (t instanceof ValidationException ve) {
                            return invalid(ve.errors());
                        } else {
                            return invalid(errorMessage);
                        }
                    },
                    Validation::valid
            );
        };
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
                input == null ? Invalid.notNull() : Validation.valid(input);
    }

    //endregion

    //region combinators

    /**
     * Returns a new {@link MappingRule} that first applies this rule, and if the input is invalid, falls back to the fallback rule.
     * If both rules fail, only the errors of the fallback rule are returned.
     * The fallback rule is evaluated only when this rule fails.
     * <p>
     * Short-circuiting, not accumulating.
     */
    default MappingRule<T, R> fallback(Function<? super T, ? extends Validation<R>> fallback) {
        Objects.requireNonNull(fallback, "fallback rule cannot be null");
        return input -> {
            Validation<R> first = this.test(input);
            if (first.isValid()) {
                return first;
            }

            return Objects.requireNonNull(fallback.apply(input), "fallback cannot return null Validation");
        };
    }

    /**
     * Composes this MappingRule with another MappingRule using "short-circuiting and" logic.
     * The combined rule is successful only if both this and the other rule are successful.
     * If this rule fails, the evaluation stops and the other rule is not evaluated.
     * <p>
     * This rule first applies the current rule to the input. If successful, it applies the next rule
     * (the argument to this method) to the result of the first rule.
     * <p>
     * Short-circuiting, not accumulating.
     */
    default <Z> MappingRule<T, Z> then(Function<? super R, ? extends Validation<? extends Z>> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return (T input) -> this.test(input).flatMap(rule::apply);
    }

    /**
     * Composes this rule with another rule using "or" logic.
     * The combined rule is successful if either this or the other rule is successful.
     * If both rules fail, their errors are combined.
     * The fallback rule is evaluated only when this rule fails.
     * <p>
     * Short-circuiting, accumulating.
     */
    @SuppressWarnings("unchecked")
    default MappingRule<T, R> or(Function<? super T, ? extends Validation<? extends R>> other) {
        Objects.requireNonNull(other, "other rule cannot be null");
        return input -> {
            Validation<R> first = this.test(input);
            if (first.isValid()) {
                return first;
            }

            Validation<R> second = (Validation<R>) Objects.requireNonNull(other.apply(input), "other cannot return null Validation");
            if (second.isValid()) {
                return second;
            }

            return invalid(first.errors().appendAll(second.errors()));
        };
    }

    default <R2> RuleCombiners.CombineBuilder2<T, R, R2> combine(Function<? super T, Validation<R2>> other) {
        return RuleCombiners.combine(this, other);
    }

    //endregion

    //region Lifts

    /**
     * Lifts a {@link MappingRule} so it applies to a {@link List} of T instead of a single T.
     * If the List is empty, the List is considered valid.
     */
    default MappingRule<List<T>, List<R>> liftToVavrList() {
        return of(ValidationOperator.super.liftToVavrList());
    }

    /**
     * Lifts a {@link MappingRule} so it applies to a {@link java.util.List} of T instead of a single T.
     * If the List is empty, the List is considered valid.
     */
    default MappingRule<java.util.List<T>, java.util.List<R>> liftToList() {
        return of(ValidationOperator.super.liftToList());
    }

    /**
     * Lifts the current mapping rule to operate on the content of {@link Option} containers.
     * Empty Options (None) are considered to be valid.
     */
    default MappingRule<Option<T>, Option<R>> liftToOption() {
        return of(ValidationOperator.super.liftToOption());
    }

    /**
     * Lifts the current mapping rule to operate on the content of {@link Optional} containers.
     * Empty Optionals are considered to be valid.
     */
    default MappingRule<Optional<T>, Optional<R>> liftToOptional() {
        return of(ValidationOperator.super.liftToOptional());
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
        return of(ValidationOperator.super.liftToVavrMap());
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
        return of(ValidationOperator.super.liftToVavrMap(keyExtractor));
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
        return of(ValidationOperator.super.liftToMap());
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
        return of(ValidationOperator.super.liftToMap(keyExtractor));
    }

    //endregion

    //region modifiers

    /**
     * Applies the specified {@link MappingRule} to the result of applying the selector function to the input. Aka <code>contramap</code>.
     *
     * @param selector a function that extracts a value of type V from an input of type T
     */
    default <V> MappingRule<V, R> using(Function<? super V, ? extends T> selector) {
       return MappingRule.using(selector, this);
    }

    /**
     * Applies the specified {@link MappingRule} to the result of applying the selector function to the input. Aka <code>contramap</code>.
     *
     * @param selector a function that extracts a value of type V from an input of type T
     * @param rule     the rule to be applied to the extracted value
     * @return a new {@link MappingRule} that tests the applied selector and rule combination
     */
    static <T, V, R> MappingRule<T, R> using(Function<? super T, ? extends V> selector, Function<? super V, ? extends Validation<? extends R>> rule) {
        Objects.requireNonNull(selector, "selector cannot be null");
        Objects.requireNonNull(rule, "rule cannot be null");
        return input -> Validation.narrow(
                Objects.requireNonNull(
                        rule.apply(selector.apply(input)),
                        "rule cannot return null Validation"
                )
        );
    }

    /**
     * Returns a new {@link MappingRule} that, when invalid, uses the passed errorKey as single ErrorMessage.
     */
    default MappingRule<T, R> withErrorKey(String errorKey) {
        return MappingRule.of(ValidationOperator.super.withErrorKey(errorKey));
    }

    /**
     * Applies a mapping function to the result of this {@link MappingRule}.
     * The mapping function must be total.
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

    //endregion
}
