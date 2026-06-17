package be.iffy.fv.dsl;

import be.iffy.fv.*;
import be.iffy.fv.dsl.impl.*;
import be.iffy.fv.rules.BooleanRules;
import be.iffy.fv.rules.ObjectRules;
import be.iffy.fv.rules.collections.*;
import be.iffy.fv.rules.functional.OptionRules;
import be.iffy.fv.rules.functional.OptionalRules;
import be.iffy.fv.rules.numbers.*;
import be.iffy.fv.rules.text.StringOps;
import be.iffy.fv.rules.text.StringRules;
import be.iffy.fv.rules.time.*;
import io.vavr.*;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.jetbrains.annotations.Contract;

import java.util.Objects;
import java.util.function.Function;

/**
 * Entry point for the functional validation API.
 * This class provides static factory methods to create and execute validations.
 * <p>
 * Implementation note: this class mostly acts as entry point to smaller, specialised DSL classes
 * or to delegate to other classes as a way to reduce imports for the library user.
 */
public final class DSL {

    //region Rules

    public static final ObjectRules objects = ObjectRules.objects;

    public static final BooleanRules booleans = BooleanRules.booleans;

    public static final StringRules strings = StringRules.strings;

    public static final StringOps stringOps = StringOps.stringOps;

    public static final BigDecimalRules bigDecimals = BigDecimalRules.bigDecimals;

    public static final BigIntegerRules bigIntegers = BigIntegerRules.bigIntegers;

    public static final DoubleRules doubles = DoubleRules.doubles;

    public static final FloatRules floats = FloatRules.floats;

    public static final IntegerRules ints = IntegerRules.ints;

    public static final LongRules longs = LongRules.longs;

    public static final DurationRules durations = DurationRules.durations;

    public static final InstantRules instants = InstantRules.instants;

    public static final LocalDateRules localDates = LocalDateRules.localDates;

    public static final LocalDateTimeRules localDateTimes = LocalDateTimeRules.localDateTimes;

    public static final LocalTimeRules localTimes = LocalTimeRules.localTimes;

    public static final YearMonthRules yearMonths = YearMonthRules.yearMonths;

    public static final ZonedDateTimeRules zonedDateTimes = ZonedDateTimeRules.zonedDateTimes;

    public static final VavrListRules vavrLists = VavrListRules.vavrLists;

    public static final ListRules lists = ListRules.lists;

    public static final CollectionRules collections = CollectionRules.collections;

    public static final SetRules sets = SetRules.sets;

    public static final VavrSetRules vavrSets = VavrSetRules.vavrSets;

    public static final VavrMapRules vavrMaps = VavrMapRules.vavrMaps;

    public static final MapRules maps = MapRules.maps;

    public static final OptionRules options = OptionRules.options;

    public static final OptionalRules optionals = OptionalRules.optionals;

    //endregion

    //region Other

    /**
     * A tiny DSL for helping to define Rules that transform their input before running their actual logic on it.
     * Usage would look like this:
     * <pre>
     * {@code
     * ...
     * Rule<String> originalRule = after(StringOps.trim()).is(strings.length(5));
     * ...
     * }
     * </pre>
     */
    @Contract(pure = true)
    public static <T> AfterDSL<T> after(Transformation<T> transformation) {
        return new AfterDSL<>(transformation);
    }

    //endregion

    //region Combine Rules

    /**
     * Combines two Rules into a builder that can map all valid values or accumulate all errors.
     */
    @Contract(pure = true)
    public static <T, R1, R2> RuleCombiners.CombineBuilder2<T, R1, R2> combine(Function<? super T, Validation<R1>> r1, Function<? super T, Validation<R2>> r2) {
        return RuleCombiners.combine(r1, r2);
    }

    /**
     * Combines three Rules into a builder that can map all valid values or accumulate all errors.
     */
    @Contract(pure = true)
    public static <T, R1, R2, R3> RuleCombiners.CombineBuilder3<T, R1, R2, R3> combine(Function<? super T, Validation<R1>> r1, Function<? super T, Validation<R2>> r2, Function<? super T, Validation<R3>> r3) {
        return RuleCombiners.combine(r1, r2, r3);
    }

    /**
     * Combines four Rules into a builder that can map all valid values or accumulate all errors.
     */
    @Contract(pure = true)
    public static <T, R1, R2, R3, R4> RuleCombiners.CombineBuilder4<T, R1, R2, R3, R4> combine(Function<? super T, Validation<R1>> r1, Function<? super T, Validation<R2>> r2, Function<? super T, Validation<R3>> r3, Function<? super T, Validation<R4>> r4) {
        return RuleCombiners.combine(r1, r2, r3, r4);
    }

    /**
     * Combines five Rules into a builder that can map all valid values or accumulate all errors.
     */
    @Contract(pure = true)
    public static <T, R1, R2, R3, R4, R5> RuleCombiners.CombineBuilder5<T, R1, R2, R3, R4, R5> combine(Function<? super T, Validation<R1>> r1, Function<? super T, Validation<R2>> r2, Function<? super T, Validation<R3>> r3, Function<? super T, Validation<R4>> r4, Function<? super T, Validation<R5>> r5) {
        return RuleCombiners.combine(r1, r2, r3, r4, r5);
    }

    /**
     * Combines six Rules into a builder that can map all valid values or accumulate all errors.
     */
    @Contract(pure = true)
    public static <T, R1, R2, R3, R4, R5, R6> RuleCombiners.CombineBuilder6<T, R1, R2, R3, R4, R5, R6> combine(Function<? super T, Validation<R1>> r1, Function<? super T, Validation<R2>> r2, Function<? super T, Validation<R3>> r3, Function<? super T, Validation<R4>> r4, Function<? super T, Validation<R5>> r5, Function<? super T, Validation<R6>> r6) {
        return RuleCombiners.combine(r1, r2, r3, r4, r5, r6);
    }

    /**
     * Combines seven Rules into a builder that can map all valid values or accumulate all errors.
     */
    @Contract(pure = true)
    public static <T, R1, R2, R3, R4, R5, R6, R7> RuleCombiners.CombineBuilder7<T, R1, R2, R3, R4, R5, R6, R7> combine(Function<? super T, Validation<R1>> r1, Function<? super T, Validation<R2>> r2, Function<? super T, Validation<R3>> r3, Function<? super T, Validation<R4>> r4, Function<? super T, Validation<R5>> r5, Function<? super T, Validation<R6>> r6, Function<? super T, Validation<R7>> r7) {
        return RuleCombiners.combine(r1, r2, r3, r4, r5, r6, r7);
    }

    /**
     * Combines eight Rules into a builder that can map all valid values or accumulate all errors.
     */
    @Contract(pure = true)
    public static <T, R1, R2, R3, R4, R5, R6, R7, R8> RuleCombiners.CombineBuilder8<T, R1, R2, R3, R4, R5, R6, R7, R8> combine(Function<? super T, Validation<R1>> r1, Function<? super T, Validation<R2>> r2, Function<? super T, Validation<R3>> r3, Function<? super T, Validation<R4>> r4, Function<? super T, Validation<R5>> r5, Function<? super T, Validation<R6>> r6, Function<? super T, Validation<R7>> r7, Function<? super T, Validation<R8>> r8) {
        return RuleCombiners.combine(r1, r2, r3, r4, r5, r6, r7, r8);
    }

    //endregion

    //region validating / validateThat / validateThatList

    /**
     * Combines multiple Validations, allowing you to map / flatMap their values if all are Valid.
     *
     * {@snippet :
     * Validation<Order> order = validating(
     *         validateThat(name, "name").is(strings.notBlank()),
     *         validateThat(price, "price").is(bigDecimals.positive())
     *     ).mapTo((n, p) -> new Order(n, p));
     * }
     */
    @Contract(pure = true)
    public static <T1, T2> ValidatingDSL.ValidatingBuilder2<T1, T2> validating(Validation<T1> v1, Validation<T2> v2) {
        return new ValidatingDSL.ValidatingBuilder2<>(v1, v2);
    }

    /**
     * Like {@link #validating(Validation, Validation)} but with 3 Validations.
     */
    @Contract(pure = true)
    public static <T1, T2, T3> ValidatingDSL.ValidatingBuilder3<T1, T2, T3> validating(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3) {
        return new ValidatingDSL.ValidatingBuilder3<>(v1, v2, v3);
    }

    /**
     * Like {@link #validating(Validation, Validation)} but with 4 Validations.
     */
    @Contract(pure = true)
    public static <T1, T2, T3, T4> ValidatingDSL.ValidatingBuilder4<T1, T2, T3, T4> validating(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4) {
        return new ValidatingDSL.ValidatingBuilder4<>(v1, v2, v3, v4);
    }

    /**
     * Like {@link #validating(Validation, Validation)} but with 5 Validations.
     */
    @Contract(pure = true)
    public static <T1, T2, T3, T4, T5> ValidatingDSL.ValidatingBuilder5<T1, T2, T3, T4, T5> validating(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4, Validation<T5> v5) {
        return new ValidatingDSL.ValidatingBuilder5<>(v1, v2, v3, v4, v5);
    }

    /**
     * Like {@link #validating(Validation, Validation)} but with 6 Validations.
     */
    @Contract(pure = true)
    public static <T1, T2, T3, T4, T5, T6> ValidatingDSL.ValidatingBuilder6<T1, T2, T3, T4, T5, T6> validating(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4, Validation<T5> v5, Validation<T6> v6) {
        return new ValidatingDSL.ValidatingBuilder6<>(v1, v2, v3, v4, v5, v6);
    }

    /**
     * Like {@link #validating(Validation, Validation)} but with 7 Validations.
     */
    @Contract(pure = true)
    public static <T1, T2, T3, T4, T5, T6, T7> ValidatingDSL.ValidatingBuilder7<T1, T2, T3, T4, T5, T6, T7> validating(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4, Validation<T5> v5, Validation<T6> v6, Validation<T7> v7) {
        return new ValidatingDSL.ValidatingBuilder7<>(v1, v2, v3, v4, v5, v6, v7);
    }

    /**
     * Like {@link #validating(Validation, Validation)} but with 8 Validations.
     */
    @Contract(pure = true)
    public static <T1, T2, T3, T4, T5, T6, T7, T8> ValidatingDSL.ValidatingBuilder8<T1, T2, T3, T4, T5, T6, T7, T8> validating(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4, Validation<T5> v5, Validation<T6> v6, Validation<T7> v7, Validation<T8> v8) {
        return new ValidatingDSL.ValidatingBuilder8<>(v1, v2, v3, v4, v5, v6, v7, v8);
    }

    /**
     * Starts a validation process for a single value.
     */
    @Contract(pure = true)
    public static <T> ValidationDSL<T> validateThat(T value) {
        return new ValidationDSL<>(value, Option.none());
    }

    /**
     * Starts a validation process for a single value with a logical name.
     * The name will be prepended to any error messages.
     *
     * @param name the name of the value (e.g., field name).
     */
    @Contract(pure = true)
    public static <T> ValidationDSL<T> validateThat(T value, String name) {
        return new ValidationDSL<>(value, Option.of(name));
    }

    /**
     * Starts a validation process for a single value with a logical name.
     * The PropertySelector will get converted to a name and will be prepended to any error messages.
     *
     * @param name a selector for the name of the value (e.g., Field::name).
     */
    @Contract(pure = true)
    public static <S, T> ValidationDSL<T> validateThat(T value, PropertySelector<S, T> name) {
        Objects.requireNonNull(name, "name cannot be null");
        return new ValidationDSL<>(value, Option.of(name.getPropertyName()));
    }

    /**
     * Helps with validating a List of values, allowing you to define Rules on the list or in the elements in the list.
     */
    @Contract(pure = true)
    public static <T> VListValidationDSL<T, T> validateThatList(List<T> value, String name) {
        return new VListValidationDSL<>(value, name);
    }

    /**
     * Helps with validating a List of values, allowing you to define Rules on the list or in the elements in the list.
     */
    @Contract(pure = true)
    public static <S, T> VListValidationDSL<T, T> validateThatList(List<T> value, PropertySelector<S, List<T>> name) {
        Objects.requireNonNull(name, "name cannot be null");
        return new VListValidationDSL<>(value, name.getPropertyName());
    }

    /**
     * Helps with validating a List of values, allowing you to define Rules on the list or in the elements in the list.
     */
    @Contract(pure = true)
    public static <T> JListValidationDSL<T, T> validateThatList(java.util.List<T> value, String name) {
        return new JListValidationDSL<>(value, name);
    }

    /**
     * Helps with validating a List of values, allowing you to define Rules on the list or in the elements in the list.
     */
    @Contract(pure = true)
    public static <S, T> JListValidationDSL<T, T> validateThatList(java.util.List<T> value, PropertySelector<S, java.util.List<T>> name) {
        Objects.requireNonNull(name, "name cannot be null");
        return new JListValidationDSL<>(value, name.getPropertyName());
    }

    //endregion

    //region asserting / assertThat

    /**
     * Build a Validation that asserts that the valid is {@link be.iffy.fv.Validation.Valid} and returns its value, throwing a {@link ValidationException} otherwise.
     */
    @Contract(pure = true)
    public static <T> AssertDSL<T> assertThat(T value, String name) {
        return new AssertDSL<>(value, Option.of(name));
    }

    /**
     * Build a Validation that asserts that the valid is {@link be.iffy.fv.Validation.Valid} and returns its value, throwing a {@link ValidationException} otherwise.
     */
    @Contract(pure = true)
    public static <T> AssertDSL<T> assertThat(T value) {
        return new AssertDSL<>(value, Option.none());
    }

    /**
     * Build a Validation that asserts that the valid is {@link be.iffy.fv.Validation.Valid} and returns its value, throwing a {@link ValidationException} otherwise.
     */
    @Contract(pure = true)
    public static <T, Z> AssertDSL<T> assertThat(T value, PropertySelector<Z, T> selector) {
        Objects.requireNonNull(selector, "selector cannot be null");
        return new AssertDSL<>(value, Option.of(selector.getPropertyName()));
    }

    /**
     * Asserts that this validation is valid and returns its value.
     * If the validation is invalid, a {@link ValidationException} is thrown.
     *
     * @throws ValidationException if any validation is invalid.
     */
    public static <T> T asserting(Validation<T> v1) throws ValidationException {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        return v1.getOrElseThrow();
    }

    /**
     * Asserts that two validations are valid and returns their values as a {@link Tuple2}.
     * If any validation is invalid, a {@link ValidationException} is thrown.
     *
     * @throws ValidationException if any validation is invalid.
     */
    public static <T1, T2> Tuple2<T1, T2> asserting(Validation<T1> v1, Validation<T2> v2) throws ValidationException {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        return Validations.combine(v1, v2).map(Tuple::of).getOrElseThrow();
    }

    /**
     * Asserts that three validations are valid and returns their values as a {@link Tuple3}.
     *
     * @throws ValidationException if any validation is invalid.
     */
    public static <T1, T2, T3> Tuple3<T1, T2, T3> asserting(
        Validation<T1> v1, Validation<T2> v2, Validation<T3> v3
    ) throws ValidationException {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        return Validations.combine(v1, v2, v3).map(Tuple::of).getOrElseThrow();
    }

    /**
     * Asserts that four validations are valid and returns their values as a {@link Tuple4}.
     *
     * @throws ValidationException if any validation is invalid.
     */
    public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> asserting(
        Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4
    ) throws ValidationException {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        Objects.requireNonNull(v4, "v4 validation cannot be null");
        return Validations.combine(v1, v2, v3, v4).map(Tuple::of).getOrElseThrow();
    }

    /**
     * Asserts that five validations are valid and returns their values as a {@link Tuple5}.
     *
     * @throws ValidationException if any validation is invalid.
     */
    public static <T1, T2, T3, T4, T5> Tuple5<T1, T2, T3, T4, T5> asserting(
        Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4, Validation<T5> v5
    ) throws ValidationException {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        Objects.requireNonNull(v4, "v4 validation cannot be null");
        Objects.requireNonNull(v5, "v5 validation cannot be null");
        return Validations.combine(v1, v2, v3, v4, v5).map(Tuple::of).getOrElseThrow();
    }

    /**
     * Asserts that six validations are valid and returns their values as a {@link Tuple6}.
     *
     * @throws ValidationException if any validation is invalid.
     */
    public static <T1, T2, T3, T4, T5, T6> Tuple6<T1, T2, T3, T4, T5, T6> asserting(
        Validation<T1> v1, Validation<T2> v2, Validation<T3> v3,
        Validation<T4> v4, Validation<T5> v5, Validation<T6> v6
    ) throws ValidationException {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        Objects.requireNonNull(v4, "v4 validation cannot be null");
        Objects.requireNonNull(v5, "v5 validation cannot be null");
        Objects.requireNonNull(v6, "v6 validation cannot be null");
        return Validations.combine(v1, v2, v3, v4, v5, v6).map(Tuple::of).getOrElseThrow();
    }

    /**
     * Asserts that seven validations are valid and returns their values as a {@link Tuple7}.
     *
     * @throws ValidationException if any validation is invalid.
     */
    public static <T1, T2, T3, T4, T5, T6, T7> Tuple7<T1, T2, T3, T4, T5, T6, T7> asserting(
        Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4,
        Validation<T5> v5, Validation<T6> v6, Validation<T7> v7
    ) throws ValidationException {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        Objects.requireNonNull(v4, "v4 validation cannot be null");
        Objects.requireNonNull(v5, "v5 validation cannot be null");
        Objects.requireNonNull(v6, "v6 validation cannot be null");
        Objects.requireNonNull(v7, "v7 validation cannot be null");
        return Validations.combine(v1, v2, v3, v4, v5, v6, v7).map(Tuple::of).getOrElseThrow();
    }

    /**
     * Asserts that eight validations are valid and returns their values as a {@link Tuple8}.
     *
     * @throws ValidationException if any validation is invalid.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8> Tuple8<T1, T2, T3, T4, T5, T6, T7, T8> asserting(
        Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4,
        Validation<T5> v5, Validation<T6> v6, Validation<T7> v7, Validation<T8> v8
    ) throws ValidationException {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        Objects.requireNonNull(v4, "v4 validation cannot be null");
        Objects.requireNonNull(v5, "v5 validation cannot be null");
        Objects.requireNonNull(v6, "v6 validation cannot be null");
        Objects.requireNonNull(v7, "v7 validation cannot be null");
        Objects.requireNonNull(v8, "v8 validation cannot be null");
        return Validations.combine(v1, v2, v3, v4, v5, v6, v7, v8).map(Tuple::of).getOrElseThrow();
    }
    //endregion

}
