package be.iffy.fv.dsl;

import be.iffy.fv.*;
import be.iffy.fv.dsl.impl.*;
import io.vavr.*;
import io.vavr.collection.Iterator;
import io.vavr.collection.List;
import io.vavr.control.Option;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Entry point for the functional validation API.
 * This class provides static factory methods to create and execute validations.
 *
 * <h2>Example: Constructor validation using {@code assertAllValid}</h2>
 * {@snippet file = "be/iffy/fv/dsl/DSLSnippets.java" region = "assert-all-tuple-example"}
 */
public class DSL {

    public static <T> AfterDSL<T> after(be.iffy.fv.Transformation<T> transformation) {
        return new AfterDSL<>(transformation);
    }

    public static <T> AfterDSL<T> after(Supplier<be.iffy.fv.Transformation<T>> transformation) {
        return new AfterDSL<>(transformation.get());
    }

    public static <T1, T2> ValidatingDSL.ValidatingBuilder2<T1, T2> validating(Validation<T1> v1, Validation<T2> v2) {
        return new ValidatingDSL.ValidatingBuilder2<>(v1, v2);
    }

    public static <T1, T2, T3> ValidatingDSL.ValidatingBuilder3<T1, T2, T3> validating(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3) {
        return new ValidatingDSL.ValidatingBuilder3<>(v1, v2, v3);
    }

    public static <T1, T2, T3, T4> ValidatingDSL.ValidatingBuilder4<T1, T2, T3, T4> validating(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4) {
        return new ValidatingDSL.ValidatingBuilder4<>(v1, v2, v3, v4);
    }

    public static <T1, T2, T3, T4, T5> ValidatingDSL.ValidatingBuilder5<T1, T2, T3, T4, T5> validating(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4, Validation<T5> v5) {
        return new ValidatingDSL.ValidatingBuilder5<>(v1, v2, v3, v4, v5);
    }

    public static <T1, T2, T3, T4, T5, T6> ValidatingDSL.ValidatingBuilder6<T1, T2, T3, T4, T5, T6> validating(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4, Validation<T5> v5, Validation<T6> v6) {
        return new ValidatingDSL.ValidatingBuilder6<>(v1, v2, v3, v4, v5, v6);
    }

    public static <T1, T2, T3, T4, T5, T6, T7> ValidatingDSL.ValidatingBuilder7<T1, T2, T3, T4, T5, T6, T7> validating(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4, Validation<T5> v5, Validation<T6> v6, Validation<T7> v7) {
        return new ValidatingDSL.ValidatingBuilder7<>(v1, v2, v3, v4, v5, v6, v7);
    }

    public static <T1, T2, T3, T4, T5, T6, T7, T8> ValidatingDSL.ValidatingBuilder8<T1, T2, T3, T4, T5, T6, T7, T8> validating(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4, Validation<T5> v5, Validation<T6> v6, Validation<T7> v7, Validation<T8> v8) {
        return new ValidatingDSL.ValidatingBuilder8<>(v1, v2, v3, v4, v5, v6, v7, v8);
    }

    /**
     * Build a Validation that asserts that the valid is {@link be.iffy.fv.Validation.Valid}, throwing a {@link ValidationException} otherwise.
     */
    public static <T> AssertDSL<T> assertThat(T value, String name) {
        return new AssertDSL<>(value, name);
    }

    /**
     * Build a Validation that asserts that the valid is {@link be.iffy.fv.Validation.Valid}, throwing a {@link ValidationException} otherwise.
     */
    public static <T, Z> AssertDSL<T> assertThat(T value, PropertySelector<Z, T> selector) {
        return new AssertDSL<>(value, selector.getPropertyName());
    }

    /**
     * Asserts that all provided validations are valid, otherwise throws a {@link ValidationException} with all errors.
     * This method is useful in constructors or at the boundaries of your application where you want to ensure
     * that data is valid before proceeding
     * <p>
     * <b>Example:</b>
     * {@snippet file = "be/iffy/fv/dsl/DSLSnippets.java" region = "assert-all-valid-example"}
     *
     * @throws ValidationException if any validation is invalid.
     */
    public static void assertAllValid(Validation<?>... validations) {
        Iterator<ErrorMessage> it = Iterator.of(validations).flatMap(Validation::errors);
        if (!it.isEmpty()) {
            throw new ValidationException(it.toList());
        }
    }

    //region assertAllValid with Tuples

    /**
     * Asserts that two validations are valid and returns their values as a {@link Tuple2}.
     * If any validation is invalid, a {@link ValidationException} is thrown.
     *
     * @throws ValidationException if any validation is invalid.
     */
    public static <T1, T2> Tuple2<T1, T2> assertAllValid(Validation<T1> v1, Validation<T2> v2) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        return Validation.mapN(v1, v2, Tuple::of).getOrElseThrow();
    }

    /**
     * Asserts that three validations are valid and returns their values as a {@link Tuple3}.
     *
     * @throws ValidationException if any validation is invalid.
     */
    public static <T1, T2, T3> Tuple3<T1, T2, T3> assertAllValid(
            Validation<T1> v1, Validation<T2> v2, Validation<T3> v3
    ) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        return Validation.mapN(v1, v2, v3, Tuple::of).getOrElseThrow();
    }

    /**
     * Asserts that four validations are valid and returns their values as a {@link Tuple4}.
     *
     * @throws ValidationException if any validation is invalid.
     */
    public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> assertAllValid(
            Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4
    ) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        Objects.requireNonNull(v4, "v4 validation cannot be null");
        return Validation.mapN(v1, v2, v3, v4, Tuple::of).getOrElseThrow();
    }

    /**
     * Asserts that five validations are valid and returns their values as a {@link Tuple5}.
     *
     * @throws ValidationException if any validation is invalid.
     */
    public static <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5> assertAllValid(
            Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4, Validation<T5> v5
    ) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        Objects.requireNonNull(v4, "v4 validation cannot be null");
        Objects.requireNonNull(v5, "v5 validation cannot be null");
        return Validation.mapN(v1, v2, v3, v4, v5, Tuple::of).getOrElseThrow();
    }

    /**
     * Asserts that six validations are valid and returns their values as a {@link Tuple6}.
     *
     * @throws ValidationException if any validation is invalid.
     */
    public static <T1, T2, T3, T4, T5, T6> Tuple6<T1, T2, T3, T4, T5, T6> assertAllValid(
            Validation<T1> v1, Validation<T2> v2, Validation<T3> v3,
            Validation<T4> v4, Validation<T5> v5, Validation<T6> v6
    ) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        Objects.requireNonNull(v4, "v4 validation cannot be null");
        Objects.requireNonNull(v5, "v5 validation cannot be null");
        Objects.requireNonNull(v6, "v6 validation cannot be null");
        return Validation.mapN(v1, v2, v3, v4, v5, v6, Tuple::of).getOrElseThrow();
    }

    /**
     * Asserts that seven validations are valid and returns their values as a {@link Tuple7}.
     *
     * @throws ValidationException if any validation is invalid.
     */
    public static <T1, T2, T3, T4, T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7> assertAllValid(
            Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4,
            Validation<T5> v5, Validation<T6> v6, Validation<T7> v7
    ) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        Objects.requireNonNull(v4, "v4 validation cannot be null");
        Objects.requireNonNull(v5, "v5 validation cannot be null");
        Objects.requireNonNull(v6, "v6 validation cannot be null");
        Objects.requireNonNull(v7, "v7 validation cannot be null");
        return Validation.mapN(v1, v2, v3, v4, v5, v6, v7, Tuple::of).getOrElseThrow();
    }

    /**
     * Asserts that eight validations are valid and returns their values as a {@link Tuple8}.
     *
     * @throws ValidationException if any validation is invalid.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> assertAllValid(
            Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4,
            Validation<T5> v5, Validation<T6> v6, Validation<T7> v7, Validation<T8> v8
    ) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        Objects.requireNonNull(v4, "v4 validation cannot be null");
        Objects.requireNonNull(v5, "v5 validation cannot be null");
        Objects.requireNonNull(v6, "v6 validation cannot be null");
        Objects.requireNonNull(v7, "v7 validation cannot be null");
        Objects.requireNonNull(v8, "v8 validation cannot be null");
        return Validation.mapN(v1, v2, v3, v4, v5, v6, v7, v8, Tuple::of).getOrElseThrow();
    }
    //endregion


    public static <T> Validation<T> notNull(T value, String name) {
        return validateThat(value, name).is(Rule.notNull());
    }

    public static <T, V> Validation<T> notNull(T value, PropertySelector<V, T> selector) {
        return validateThat(value, selector).is(Rule.notNull());
    }

    /**
     * For any given {@code MappingRule<T, R>}, returns a MappingRule that can work on an {@code MappingRule<Optional<T>, Optional<R>>} instead.
     * An empty {@link Optional} is considered to be valid.
     *
     * @see be.iffy.fv.rules.functional.OptionalRules#required()
     */
    public static <T, R> MappingRule<Optional<T>, Optional<R>> optional(MappingRule<T, R> rule) {
        return rule.liftToOptional();
    }

    /**
     * For any given {@code MappingRule<T, R>}, returns a MappingRule that can work on an {@code MappingRule<Optional<T>, Optional<R>>} instead.
     * An empty {@link Optional} is considered to be valid.
     *
     * @see be.iffy.fv.rules.functional.OptionalRules#required()
     */
    public static <T> Rule<Optional<T>> optional(Rule<T> rule) {
        return rule.liftToOptional();
    }

    /**
     * For any given {@code MappingRule<T, R>}, returns a MappingRule that can work on an {@code MappingRule<Option<T>, Option<R>>} instead.
     * An empty {@link Option} is considered to be valid.
     *
     * @see be.iffy.fv.rules.functional.OptionRules#required()
     */
    public static <T, R> MappingRule<Option<T>, Option<R>> option(MappingRule<T, R> rule) {
        return rule.liftToOption();
    }

    /**
     * For any given {@code MappingRule<T, R>}, returns a MappingRule that can work on an {@code MappingRule<Option<T>, Option<R>>} instead.
     * An empty {@link Option} is considered to be valid.
     *
     * @see be.iffy.fv.rules.functional.OptionRules#required()
     */
    public static <T> Rule<Option<T>> option(Rule<T> rule) {
        return rule.liftToOption();
    }

    /**
     * Starts a validation process for a collection of values.
     *
     * @return a {@link ValidateAllDSL} instance.
     */
    public static <T> ValidateAllDSL<T> validateAll(Iterable<T> values) {
        return new ValidateAllDSL<>(values);
    }

    /**
     * Starts a validation process for a single value.
     *
     * @return a {@link ValidationDSL} instance.
     */
    public static <T> ValidationDSL<T> validateThat(T value) {
        return new ValidationDSL<>(value);
    }

    /**
     * Starts a validation process for a single value with a logical name.
     * The name will be prepended to any error messages.
     *
     * @param name the name of the value (e.g. field name).
     */
    public static <T> ValidationDSL<T> validateThat(T value, String name) {
        return new ValidationDSL<>(value, name);
    }

    public static <ANY, T> ValidationDSL<T> validateThat(T value, PropertySelector<ANY, T> name) {
        return new ValidationDSL<>(value, name.getPropertyName());
    }

    public static <T> VListValidationDSL<T, T> validateThatList(List<T> value, String name) {
        return new VListValidationDSL<>(value, name);
    }

    public static <T> JListValidationDSL<T, T> validateThatList(java.util.List<T> value, String name) {
        return new JListValidationDSL<>(value, name);
    }

}
