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
 * The mapping can either succeed (producing a valid R) or fail (producing error details).
 *
 * @param <T> the type of input to be mapped
 * @param <R> the type of output after successful mapping
 */
public interface MappingRule<T, R> {


    /**
     * Tests the given value against the mapping rule.
     *
     * @param value the input to be validated and transformed according to the rule's mapper function
     * @return a Validation instance representing the result of applying the rule to the value,
     * either containing the successfully transformed output (R) or error details
     * if the mapping failed or the transformed result is invalid
     */
    Validation<R> test(T value);

    /**
     * Creates a new MappingRule that applies the given mapper function to the input.
     * If the mapper throws an exception, the rule will fail with the specified error key.
     *
     * @param <T>      the type of input to be mapped
     * @param <R>      the type of output after mapping
     * @param mapper   the function that maps T to R
     * @param errorKey the errorKey to use if the mapping fails.
     * @return a new {@link MappingRule} that applies the mapper and validates the result
     */
    static <T, R> MappingRule<T, R> of(Function1<T, R> mapper, String errorKey) {
       return of(mapper, ErrorMessage.of(errorKey));
    }

    /**
     * Creates a new MappingRule that applies the given mapper function to the input.
     * If the mapper throws an exception, the rule will fail with the specified error key.
     *
     * @param <T>      the type of input to be mapped
     * @param <R>      the type of output after mapping
     * @param mapper   the function that maps T to R
     * @param errorKey the errorKey to use if the mapping fails.
     * @return a new {@link MappingRule} that applies the mapper and validates the result
     */
    static <T, R> MappingRule<T, R> ofTry(Function1<T, Try<R>> mapper, String errorKey) {
        return input -> {
            Try<R> _try = mapper.apply(input);
            return _try.fold(
                    t -> Validation.invalid(ErrorMessage.of(errorKey)),
                    Validation::valid
            );
        };
    }
    /**
     * Creates a new MappingRule that applies the given mapper function to the input.
     * If the mapper throws an exception, the rule will fail with the specified error key.
     *
     * @param <T>      the type of input to be mapped
     * @param <R>      the type of output after mapping
     * @param throwingMapper   the function that maps T to R
     * @param errorMessage the error message to use if the mapping fails.
     * @return a new {@link MappingRule} that applies the mapper and validates the result
     */
    static <T, R> MappingRule<T, R> of(Function1<T, R> throwingMapper, ErrorMessage errorMessage) {
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
     *
     * @param <Z>  the type of output from the next rule after transformation.
     * @param rule the rule to apply after this rule if this rule is successful.
     * @return a composed MappingRule that represents a shortcut-if-this rule.
     */
    default <Z> MappingRule<T, Z> andThen(MappingRule<? super R, ? extends Z> rule) {
        return (T input) -> this.test(input).flatMap(rule::test);
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
     * Be careful, the key {@code value.toString()} will be used as part of the path segment.
     * Make sure to have a key that has a meaningful string representation for this.
     * If you can't guarantee this, use the version of {@link #liftToMap(Function1)} that takes a keyExtractor function instead.
     * <p>
     * Semantics:
     * - Each value in the map is validated, and the resulting validations are collected.
     * - If any validation fails, the entire map is considered invalid.
     * - If all validations pass, the map is considered valid.
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
     *
     * @param keyExtractor the function to extract a path segment from the key.
     * @param <K>          the key type.
     * @return a new {@link MappingRule} instance.
     */
    default <K> MappingRule<Map<K, T>, Map<K, R>> liftToMap(Function1<K, Object> keyExtractor) {
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
     *
     * @param <T> the type of the value inside the {@link Option}
     * @param <R> the type of the result of the mapping rule
     * @param rule the mapping rule to apply to the value inside the {@link Option}
     * @return a new {@link MappingRule} that validates the option and applies the given rule to its value
     */
    static <T, R> MappingRule<Option<T>, R> requiredOption(MappingRule<T, R> rule) {
        return rule.liftToOption().andThen(opt -> opt.fold(() -> Validation.invalid("must.not.be.empty"), Validation::valid));
    }

    /**
     * Returns a MappingRule that checks if the input {@link Optional} is defined and applies the given rule to its value.
     *
     * @param <T> the type of the value inside the {@link Optional}
     * @param <R> the type of the result of the mapping rule
     * @param rule the mapping rule to apply to the value inside the {@link Optional}
     * @return a new MappingRule that validates the option and applies the given rule to its value
     */
    static <T, R> MappingRule<Optional<T>, R> requiredOptional(MappingRule<T, R> rule) {
        return rule.liftToOptional().andThen(opt -> opt.map(Validation::valid).orElseGet(() -> Validation.invalid("must.not.be.empty")));
    }


    /**
     * Returns a MappingRule that validates the input is not null.
     *<p>
     * Error key: "must.not.be.null"
     *
     * @param <T> the type of input and output
     * @return a MappingRule that returns valid input only if it's not null
     */
    static <T> MappingRule<T, T> notNull() {
        return input -> input == null ? Validation.invalid("must.not.be.null") : Validation.valid(input);
    }

    /**
     * Applies the specified {@link MappingRule} to the result of applying the selector function to the input. Aka <code>contraMap</code>.
     *
     * @param <T> the type of the input to be tested
     * @param <V> the type of the result produced by the selector function
     * @param <R> the type of the validation produced by the rule
     * @param selector a function that extracts a value of type V from an input of type T
     * @param rule the rule to be applied to the extracted value
     * @return a new {@link MappingRule} that tests the applied selector and rule combination
     */
    static <T, V, R> MappingRule<T, R> with(Function<T,V> selector, MappingRule<V,R> rule) {
        return input -> rule.test(selector.apply(input));
    }
}
