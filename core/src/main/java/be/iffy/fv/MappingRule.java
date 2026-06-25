package be.iffy.fv;

import be.iffy.fv.Validation.Invalid;
import io.vavr.collection.List;
import io.vavr.control.Try;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static be.iffy.fv.Validation.invalid;

/**
 * Represents a rule for mapping an input of type T to an output of type R,
 * with built-in validation support.
 * The mapping can either succeed (producing a {@link Validation.Valid} R) or fail (producing an {@link Invalid} with error details).
 */
@FunctionalInterface
public interface MappingRule<T, R> extends RuleLike<T, Validation<R>> {

    /**
     * Evaluates the input against this rule, transforming it from type T to type R.
     *
     * @param value the value to be processed by this {@link MappingRule}
     * @return a {@link Validation} instance representing the outcome: either a {@link Validation.Valid}
     * with the successfully transformed value or a {@link Invalid} containing the errors encountered during
     * mapping or validation.
     */
    @Override
    @Contract(pure = true)
    Validation<R> apply(@Nullable T value);

    //region factory methods

    /**
     * Creates an explicit {@link MappingRule} from a function that has the same signature.
     * Use this to easily treat existing functions as Validations.
     */
    static <T, R> MappingRule<T, R> of(RuleLike<? super T, ? extends Validation<? extends R>> validationFunction) {
        if (validationFunction instanceof MappingRule) {
            return (MappingRule<T, R>) validationFunction;
        }
        Objects.requireNonNull(validationFunction, "validationFunction cannot be null");
        return input -> {
            if (input == null) {
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
                return Validation.valid(
                    Objects.requireNonNull(throwingMapper.apply(input), "throwingMapper cannot return null")
                );
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
     * that exception is propagated. Only failures represented as {@code Try.Failure} are converted into Invalid.
     * <p>
     * If the Try fails with {@link ValidationException}, its errors are preserved, otherwise the provided error message is used.
     */
    static <T, R> MappingRule<T, R> fromTry(Function<? super T, ? extends Try<? extends R>> tryProvider, String errorKey) {
        Objects.requireNonNull(errorKey, "errorKey cannot be null");
        return fromTry(tryProvider, ErrorMessage.of(errorKey));
    }

    /**
     * Creates a MappingRule from a function that returns a Try.
     * <p>
     * The tryProvider itself is invoked directly. If the tryProvider throws before returning a Try,
     * that exception is propagated. Only failures represented as {@code Try.Failure} are converted into Invalid.
     * <p>
     * If the Try fails with {@link ValidationException}, its errors are preserved, otherwise the provided error message is used.
     */
    static <T, R> MappingRule<T, R> fromTry(Function<? super T, ? extends Try<? extends R>> tryProvider, ErrorMessage errorMessage) {
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        return fromTry(tryProvider, (input, e) -> errorMessage);
    }

    /**
     * Creates a MappingRule from a function that returns a Try.
     * <p>
     * The tryProvider itself is invoked directly. If the tryProvider throws before returning a Try,
     * that exception is propagated. Only failures represented as {@code Try.Failure} are converted into Invalid.
     * <p>
     * If the Try fails with {@link ValidationException}, its errors are preserved, otherwise the provided error message is used.
     */
    static <T, R> MappingRule<T, R> fromTry(Function<? super T, ? extends Try<? extends R>> tryProvider, BiFunction<? super T, Throwable, ErrorMessage> errorMessageMaker) {
        Objects.requireNonNull(tryProvider, "tryProvider cannot be null");
        Objects.requireNonNull(errorMessageMaker, "errorMessageMaker cannot be null");
        return input -> {
            if (input == null) {
                return Invalid.notNull();
            }
            Try<? extends R> result = Objects.requireNonNull(tryProvider.apply(input), "tryProvider cannot return null Try");
            return result.fold(
                t -> {
                    if (t instanceof ValidationException ve) {
                        return invalid(ve.errors());
                    } else {
                        return invalid(errorMessageMaker.apply(input, t));
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
    default MappingRule<T, R> fallback(RuleLike<? super T, ? extends Validation<R>> fallback) {
        Objects.requireNonNull(fallback, "fallback rule cannot be null");
        return MappingRule.of(input -> {
            Validation<R> first = this.apply(input);
            if (first.isValid()) {
                return first;
            }

            return Objects.requireNonNull(fallback.apply(input), "fallback cannot return null Validation");
        });
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
    default <Z> MappingRule<T, Z> then(RuleLike<? super R, ? extends Validation<? extends Z>> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return MappingRule.of((T input) ->
            this.apply(input).flatMap(rule)
        );
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
    default MappingRule<T, R> or(RuleLike<? super T, ? extends Validation<? extends R>> other) {
        Objects.requireNonNull(other, "other rule cannot be null");
        return MappingRule.of(input -> {
            Validation<R> first = this.apply(input);
            if (first.isValid()) {
                return first;
            }

            Validation<R> second = (Validation<R>) Objects.requireNonNull(other.apply(input), "other cannot return null Validation");
            if (second.isValid()) {
                return second;
            }

            return invalid(first.errors().appendAll(second.errors()));
        });
    }

    /**
     * Shorthand for RuleCombiners.combine(this, other);
     */
    default <R2> RuleCombiners.CombineBuilder2<T, R, R2> combine(RuleLike<? super T, Validation<R2>> other) {
        return RuleCombiners.combine(this, other);
    }

    //endregion


    //region modifiers

    /**
     * Applies the specified {@link MappingRule} to the result of applying the selector function to the input. Aka <code>contramap</code>.
     *
     * @param selector a function that extracts a value of type V from an input of type T
     */
    default <V> MappingRule<V, R> on(PropertySelector<? super V, ? extends T> selector) {
        return MappingRule.on(selector, this);
    }

    /**
     * Applies the specified {@link MappingRule} to the result of applying the selector function to the input. Aka <code>contramap</code>.
     *
     * @param selector a function that extracts a value of type V from an input of type T
     * @param rule     the rule to be applied to the extracted value
     * @return a new {@link MappingRule} that tests the applied selector and rule combination
     */
    static <T, V, R> MappingRule<T, R> on(PropertySelector<? super T, ? extends V> selector, RuleLike<? super V, ? extends Validation<? extends R>> rule) {
        Objects.requireNonNull(selector, "selector cannot be null");
        Objects.requireNonNull(rule, "rule cannot be null");
        return MappingRule.of(input ->
            Validation.narrow(
                Objects.requireNonNull(
                    rule.apply(selector.apply(input)).at(selector.getPropertyName()),
                    "rule cannot return null Validation"
                )
            ));
    }

    /**
     * Returns a new {@link MappingRule} that, when invalid, uses the passed errorKey as single ErrorMessage.
     */
    default MappingRule<T, R> withErrorKey(String errorKey) {
        Objects.requireNonNull(errorKey, "errorKey cannot be null");
        return MappingRule.of(input ->
            this.apply(input)
                .mapErrors(ignore ->
                    List.of(ErrorMessage.of(errorKey))
                )
        );
    }

    /**
     * Applies a mapping function to the result of this {@link MappingRule}.
     * The mapping function must be total.
     */
    default <Z> MappingRule<T, Z> map(Function<? super R, ? extends Z> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return MappingRule.of((T input) ->
            this.apply(input).map(mapper)
        );
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

    /**
     * Converts this MappingRule into a {@link Predicate} that tests whether
     * the given input satisfies the rule's conditions.
     */
    default <S extends T> Predicate<S> toPredicate() {
        return value -> apply(value).isValid();
    }

    /**
     * Lift a MappingRule by giving you access to the MappingRuleLifter, allowing you to lift this MappingRule into many other types.
     */
    default MappingRuleLifter<T, R> lift() {
        return new MappingRuleLifter<>(this);
    }
}
