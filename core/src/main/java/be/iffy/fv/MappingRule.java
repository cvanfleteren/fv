package be.iffy.fv;

import be.iffy.fv.Validation.Invalid;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;

import java.util.Objects;
import java.util.Optional;
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
    default <Z> MappingRule<T, Z> then(Function<? super R, ? extends Validation<? extends Z>> rule) {
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
    default MappingRule<T, R> orElse(Function<? super T, ? extends Validation<? extends R>> other) {
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

    /**
     * Returns a new {@link MappingRule} that first applies this rule, and if the input is invalid, falls back to the other rule.
     * If both rules fail, only the errors of the fallback rule are returned.
     * The fallback rule is evaluated only when this rule fails.
     */
    default MappingRule<T, R> recoverWith(Function<? super T, ? extends Validation<R>> other) {
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
        return input ->
                this.test(input)
                        .mapErrors(ignore -> List.of(ErrorMessage.of(errorKey)));
    }

    /**
     * Lifts a {@link MappingRule} so it applies to a {@link List} of T instead of a single T.
     * If the List is empty, the List is considered valid.
     */
    default MappingRule<List<T>, List<R>> liftToVavrList() {
        return MappingRules.fromValidation(ValidationOperator.super.liftToVavrList());
    }

    /**
     * Lifts a {@link MappingRule} so it applies to a {@link java.util.List} of T instead of a single T.
     * If the List is empty, the List is considered valid.
     */
    default MappingRule<java.util.List<T>, java.util.List<R>> liftToList() {
        return MappingRules.fromValidation(ValidationOperator.super.liftToList());
    }

    /**
     * Lifts the current mapping rule to operate on the content of {@link Option} containers.
     * Empty Options (None) are considered to be valid.
     */
    default MappingRule<Option<T>, Option<R>> liftToOption() {
        return MappingRules.fromValidation(ValidationOperator.super.liftToOption());
    }

    /**
     * Lifts the current mapping rule to operate on the content of {@link Optional} containers.
     * Empty Optionals are considered to be valid.
     */
    default MappingRule<Optional<T>, Optional<R>> liftToOptional() {
        return MappingRules.fromValidation(ValidationOperator.super.liftToOptional());
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
        return MappingRules.fromValidation(ValidationOperator.super.liftToVavrMap());
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
        return MappingRules.fromValidation(ValidationOperator.super.liftToVavrMap(keyExtractor));
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
        return MappingRules.fromValidation(ValidationOperator.super.liftToMap());
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
       return MappingRules.fromValidation(ValidationOperator.super.liftToMap(keyExtractor));
    }

}
