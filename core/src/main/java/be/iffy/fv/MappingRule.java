package be.iffy.fv;

import io.vavr.Function1;
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
public interface MappingRule<T, R> {

    /**
     * Evaluates the input against this rule, transforming it from type T to type R.
     * {@snippet file = "be/iffy/fv/MappingRuleSnippets.java" region = "test-example"}
     *
     * @param value the value to be processed by this {@link MappingRule}
     * @return a {@link Validation} instance representing the outcome: either a {@link Validation.Valid}
     * with the successfully transformed value or a {@link Validation.Invalid} containing the errors encountered during
     * mapping or validation.
     */
    Validation<R> test(T value);

    /**
     * Creates a new MappingRule that applies the given mapper function to the input.
     * If the mapper throws an exception, the rule will fail with the specified error message.
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/MappingRuleSnippets.java" region = "of-string-example"}
     */
    static <T, R> MappingRule<T, R> of(Function<? super T, ? extends R> mapper, String errorKey) {
        return of(mapper, ErrorMessage.of(errorKey));
    }

    /**
     * Creates a new MappingRule that applies the given mapper function to the input.
     * If the mapper throws an exception, the rule will fail with the specified error message.
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/MappingRuleSnippets.java" region = "of-string-example"}
     */
    static <T> MappingRule<T, T> of(Transformation<T> transformation) {
        return of(transformation::apply, ErrorMessage.of("transformation.failed"));
    }

    /**
     * Creates a new MappingRule that applies the given mapper function to the input.
     * If the mapper returns a {@link Try.Failure}, the rule will fail with the specified error key.
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/MappingRuleSnippets.java" region = "of-try-error-example"}
     */
    static <T, R> MappingRule<T, R> ofTry(Function<? super T, ? extends Try<? extends R>> mapper, ErrorMessage errorMessage) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        return input -> {
            if(input == null) {
                return Validation.invalid("must.not.be.null");
            }
            Try<? extends R> _try = mapper.apply(input);
            return _try.fold(
                    t -> {
                        if(t instanceof ValidationException ve) {
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
     * Creates a new MappingRule that applies the given mapper function to the input.
     * If the mapper returns a {@link Try.Failure}, the rule will fail with the specified error key.
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/MappingRuleSnippets.java" region = "of-try-string-example"}
     */
    static <T, R> MappingRule<T, R> ofTry(Function<? super T, ? extends Try<? extends R>> mapper, String errorKey) {
        return ofTry(mapper, ErrorMessage.of(errorKey));
    }

    /**
     * Creates a new MappingRule that applies the given mapper function to the input.
     * If the mapper throws an exception, the rule will fail with the specified error message.
     * If the mapper throws ValidationException, the rule will fail with its errors.
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/MappingRuleSnippets.java" region = "of-error-example"}
     */
    static <T, R> MappingRule<T, R> of(Function<? super T, ? extends R> throwingMapper, ErrorMessage errorMessage) {
        Objects.requireNonNull(throwingMapper, "mapper cannot be null");
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        return ofTry(input -> Try.of(() -> throwingMapper.apply(input)), errorMessage);
    }

    /**
     * Creates an explicit {@link MappingRule} from a function that has the same signature.
     * Use this to easily treat existing functions as MappingRules.
     *
     * @param validationFunction The function that converts an input of type T to a validation object of type R.
     */
    static <T, R> MappingRule<T, R> of(Function<? super T, ? extends Validation<R>> validationFunction) {
        return validationFunction::apply;
    }

    /**
     * Composes this MappingRule with another MappingRule using "short-circuiting and" logic.
     * The combined rule is successful only if both this and the other rule are successful.
     * If this rule fails, the evaluation stops and the other rule is not evaluated.
     * <p>
     * This rule first applies the current rule to the input. If successful, it applies the next rule
     * (the argument to this method) to the result of the first rule.
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/MappingRuleSnippets.java" region = "and-then-example"}
     *
     * @param rule the rule to apply after this rule if this rule is successful.
     * @return a composed {@link MappingRule} that applies both rules in sequence.
     */
    default <Z> MappingRule<T, Z> andThen(MappingRule<? super R, ? extends Z> rule) {
        return (T input) -> this.test(input).flatMap(rule::test);
    }

    /**
     * Applies a mapping function to the result of this {@link MappingRule}.
     *
     * @param mapper the function to apply to the result of this rule if the input passes the test.
     * @return a new {@link MappingRule} that applies the mapping function to the result.
     */
    default <Z> MappingRule<T, Z> map(Function<? super R, ? extends Z> mapper) {
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
     * If both rules fail, only the errors of the fallback rule are returned.
     * The fallback rule is evaluated only when this rule fails.
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/MappingRuleSnippets.java" region = "recover-with-example"}
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
     *
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/MappingRuleSnippets.java" region = "lift-to-vavrlist-example"}
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
     *
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/MappingRuleSnippets.java" region = "lift-to-vavrlist-example"}
     */
    default MappingRule<java.util.List<T>, java.util.List<R>> liftToList() {
        return values -> {
            java.util.List<Validation<R>> validations = values.stream().map(this::test).toList();
            // Validation.sequence already adds the [index] path segment, so we don't do it here.
            return Validation.transpose(validations);
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
     * {@snippet file = "be/iffy/fv/MappingRuleSnippets.java" region = "lift-to-option-example"}
     *
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
     * {@snippet file = "be/iffy/fv/MappingRuleSnippets.java" region = "lift-to-optional-example"}
     *
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
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/MappingRuleSnippets.java" region = "lift-to-vavrmap-example"}
     *
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
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/MappingRuleSnippets.java" region = "lift-to-vavrmap-extractor-example"}
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
                return Validation.valid(validAndInvalid._1.toMap(Tuple2::_1, t -> t._2.getOrElseThrow()));
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
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/MappingRuleSnippets.java" region = "lift-to-map-example"}
     *
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
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/MappingRuleSnippets.java" region = "lift-to-map-extractor-example"}
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
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/MappingRuleSnippets.java" region = "with-example"}
     *
     * @param selector a function that extracts a value of type V from an input of type T
     * @param rule     the rule to be applied to the extracted value
     * @return a new {@link MappingRule} that tests the applied selector and rule combination
     */
    static <T, V, R> MappingRule<T, R> with(Function<T, V> selector, MappingRule<? super V, ? extends R> rule) {
        Objects.requireNonNull(selector, "selector cannot be null");
        Objects.requireNonNull(rule, "rule cannot be null");
        return input -> Validation.narrow(rule.test(selector.apply(input)));
    }
}
