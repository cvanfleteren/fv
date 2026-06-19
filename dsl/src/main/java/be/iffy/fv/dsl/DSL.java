package be.iffy.fv.dsl;

import be.iffy.fv.*;
import be.iffy.fv.dsl.impl.*;
import be.iffy.fv.rules.BooleanRules;
import be.iffy.fv.rules.ObjectRules;
import be.iffy.fv.rules.collections.*;
import be.iffy.fv.rules.functional.EitherRules;
import be.iffy.fv.rules.functional.OptionRules;
import be.iffy.fv.rules.functional.OptionalRules;
import be.iffy.fv.rules.numbers.*;
import be.iffy.fv.rules.text.StringOps;
import be.iffy.fv.rules.text.StringRules;
import be.iffy.fv.rules.time.*;
import io.vavr.*;
import io.vavr.collection.Iterator;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.jetbrains.annotations.Contract;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Entry point for the functional validation DSL.
 * <p>
 * Add a single static import and you have access to all rule namespaces ({@code strings},
 * {@code ints}, {@code localDates}, …) and all the methods below:
 * <pre>{@code
 * import static be.iffy.fv.dsl.DSL.*;
 * }</pre>
 *
 * <h2>Choosing the right method</h2>
 *
 * <table border="1">
 *   <caption>Choosing the right method</caption>
 *   <tr><th></th><th>Single field</th><th>Multiple fields (accumulates all errors)</th></tr>
 *   <tr><th>Throws {@link ValidationException} on failure</th>
 *       <td>{@link #assertThat}</td><td>{@link #asserting}</td></tr>
 *   <tr><th>Returns {@link Validation} (no throw)</th>
 *       <td>{@link #validateThat}</td><td>{@link #validating}</td></tr>
 * </table>
 *
 * <h2>{@code assertThat} — single field, throws on invalid</h2>
 * Use inside a constructor when you want the field to be normalised (e.g. trimmed) before
 * the check, and you want the constructor to throw if the value is invalid.
 * {@snippet :
 * record Username(String value) {
 *     public Username {
 *         // trims first, then checks length — throws ValidationException if invalid
 *         value = assertThat(value, "value")
 *                 .after(stringOps.trim())
 *                 .is(strings.minLength(3));
 *     }
 * }
 *}
 *
 * <h2>{@code asserting} — multiple fields, throws on invalid</h2>
 * Use inside a constructor when you need to validate several fields and want
 * <em>all</em> errors collected before throwing.
 * {@snippet :
 * record Person(String name, int age) {
 *     public Person {
 *         asserting(
 *                 validateThat(name, Person::name).is(strings.minLength(3)),
 *                 validateThat(age,  Person::age).is(ints.atLeast(18))
 *         );
 *     }
 * }
 * // new Person("Al", 16) throws with BOTH name.must.have.min.length AND age.must.be.at.least
 *}
 *
 * <h2>{@code validateThat} — single field, returns {@link Validation}</h2>
 * Use in a service or mapper when you want to inspect or combine the result rather than throw.
 * {@snippet :
 * Validation<String> result = validateThat(rawName, "name")
 *         .after(stringOps.trim())
 *         .is(strings.minLength(3));
 *
 * if (result.isInvalid()) {
 *     result.errors(); // List<ErrorMessage>
 * }
 *}
 *
 * <h2>{@code validating} — multiple fields, returns {@link Validation}</h2>
 * Use in a service or mapper to validate and combine several fields without throwing.
 * {@snippet :
 * record PersonDto(String name, String age) {}
 * record Person(String name, int age) {}
 *
 * Validation<Person> toPerson(PersonDto dto) {
 *     return validating(
 *             validateThat(dto.name(), "name").is(strings.minLength(3)),
 *             validateThat(dto.age(),  "age").is(strings.asInteger().then(ints.positive()))
 *     ).map(Person::new);
 * }
 * // toPerson(new PersonDto("Al", "-5")) → Invalid([name.must.have.min.length, age.must.be.positive])
 *}
 *
 * <h2>{@code validating} vs {@code combine} — one-shot result vs reusable rule</h2>
 * Both accumulate errors across multiple validations, but they differ in what they return
 * and when the input is consumed:
 * <ul>
 *   <li>{@link #validating} takes already-evaluated {@link Validation} objects and immediately
 *       combines them into a single {@link Validation} result. Use it inline when you are
 *       validating a specific value right now.</li>
 *   <li>{@link #combine} takes <em>functions</em> that each map the same input {@code T} to a
 *       {@link Validation}, and returns a reusable {@link MappingRule}{@code <T, R>} that can
 *       be stored and applied to any number of inputs later. Use it when you want to define a
 *       composite rule once and apply it in multiple places.</li>
 * </ul>
 * {@snippet :
 * record PersonDto(String name, String age) {}
 * record Person(String name, int age) {}
 *
 *
 * // combine → builds a reusable MappingRule; the dto is not consumed yet
 * MappingRule<PersonDto, Person> toPersonRule = combine(
 *         strings.minLength(3).on(PersonDTO::name),
 *         strings.asInteger().then(ints.positive()).on(PersonDto::age)
 * ).map(Person::new);
 *
 * Validation<Person> result = toPersonRule.apply(someDto); // apply whenever needed
 *
 * // validating → evaluates immediately; the fields are already in hand
 * Validation<Person> result2 = validating(
 *         validateThat(someDto.name(), "name").is(strings.minLength(3)),
 *         validateThat(someDto.age(),  "age").is(strings.asInteger().then(ints.positive()))
 * ).map(Person::new);
 *}
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

    public static final OffsetDateTimeRules offsetDateTimes = OffsetDateTimeRules.offsetDateTimes;

    public static final OffsetTimeRules offsetTimes = OffsetTimeRules.offsetTimes;

    public static final LocalTimeRules localTimes = LocalTimeRules.localTimes;

    public static final YearRules years = YearRules.years;

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

    public static <L,R> EitherRules<L,R> eithers() {
        return EitherRules.eithers();
    }

    //endregion

    //region Other

    /**
     * Create a Validation from code that might throw {@link ValidationException}.
     * @see Validation#catching(Supplier)
     */
    public static <T> Validation<T> catching(Supplier<T> supplier) {
        return Validation.catching(supplier);
    }

    /**
     * Returns the ValidationFactory, allowing you to create Validations from many other types.
     */
    public static ValidationFactory from() {
        return Validation.from();
    }

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
     *<p>
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
     * Starts a builder for validating a {@link List} of values.
     * <p>
     * Unlike {@link #validateThat}, this returns a <em>builder</em>, not a {@link Validation} directly.
     * Chain {@code .is(...)} to add list-level rules and {@code .eachIs(...)} to validate (and optionally
     * transform) each element. Always call {@code .validate()} at the end to obtain the
     * {@code Validation<List<E>>} result:
     * {@snippet :
     * Validation<List<Integer>> result = validateThatList(order.lineAmounts(), "lineAmounts")
     *         .is(vavrLists.notEmpty())
     *         .eachIs(strings.asInteger().then(ints.positive()))
     *         .validate();
     * }
     * <p>
     * {@code eachIs} transforms the element type ({@code T → R}), so a subsequent {@code .is()} call
     * operates on the transformed list type. Compose multiple element-level rules with
     * {@link MappingRule#then then(...)} rather than chaining separate {@code eachIs} calls.
     *
     * @param value the list to validate
     * @param name  the logical name used as a prefix in error paths (e.g. {@code "lineAmounts"})
     */
    @Contract(pure = true)
    public static <T> VListValidationDSL<T, T> validateThatList(List<T> value, String name) {
        return new VListValidationDSL<>(value, name);
    }

    /**
     * Like {@link #validateThatList(List, String)} but derives the name from a method reference,
     * keeping it refactor-safe (e.g. {@code Order::lineAmounts}).
     */
    @Contract(pure = true)
    public static <S, T> VListValidationDSL<T, T> validateThatList(List<T> value, PropertySelector<S, List<T>> name) {
        Objects.requireNonNull(name, "name cannot be null");
        return new VListValidationDSL<>(value, name.getPropertyName());
    }

    /**
     * Like {@link #validateThatList(List, String)} but for a {@link java.util.List} instead of a Vavr {@link List}.
     */
    @Contract(pure = true)
    public static <T> JListValidationDSL<T, T> validateThatList(java.util.List<T> value, String name) {
        return new JListValidationDSL<>(value, name);
    }

    /**
     * Like {@link #validateThatList(java.util.List, String)} but derives the name from a method reference,
     * keeping it refactor-safe (e.g. {@code Order::lineAmounts}).
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
     * Asserts the provided validations and throws a ValidationException if any errors are found.
     * This method iterates through the given validations, collects their error messages,
     * and ensures that all validations are non-null.
     *
     * @throws ValidationException if any validation is invalid.
     */
    public static void asserting(Validation<?>... validations) throws ValidationException {
        Objects.requireNonNull(validations, "validations is required");

        List<ErrorMessage> errors = Iterator.of(validations)
            .map(v -> Objects.requireNonNull(v, "each validation is required"))
            .flatMap(Validation::errors)
            .toList();

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
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
