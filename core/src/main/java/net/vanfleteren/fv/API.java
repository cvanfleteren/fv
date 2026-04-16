package net.vanfleteren.fv;

import io.vavr.*;
import io.vavr.collection.Iterator;
import io.vavr.collection.List;

import java.util.Objects;

/**
 * Entry point for the functional validation API.
 * This class provides static factory methods to create and execute validations.
 *
 * <h2>Example: Constructor validation using {@code assertAllValid}</h2>
 * <pre>{@code
 * public record Person(String name, int age) {
 *     private static final Rule<String> minLength = Rule.of(s -> s.length() > 3, "too.short");
 *     private static final Rule<Integer> minAge = Rule.of(i -> i >= 18, "too.young");
 *
 *     public Person {
 *         // validate and assign results (e.g. if you want to trim the name)
 *         // will throw ValidationException if any validation fails
 *         var values = assertAllValid(
 *                 validateThat(name, "name").map(String::trim).is(minLength),
 *                 validateThat(age, "age").is(minAge)
 *         );
 *         name = values._1;
 *         age = values._2;
 *     }
 * }
 * }</pre>
 */
public class API {


    /**
     * Asserts that all provided validations are valid, otherwise throws a {@link ValidationException} with all errors.
     * This method is useful in constructors or at the boundaries of your application where you want to ensure
     * that data is valid before proceeding.
     *
     * @param validations the validations to check.
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
     * @param v1   the first validation.
     * @param v2   the second validation.
     * @param <T1> type of the first value.
     * @param <T2> type of the second value.
     * @return a {@link Tuple2} containing the valid values.
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
     * @param v1   the first validation.
     * @param v2   the second validation.
     * @param v3   the third validation.
     * @param <T1> type of the first value.
     * @param <T2> type of the second value.
     * @param <T3> type of the third value.
     * @return a {@link Tuple3} containing the valid values.
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
     * @param v1   the first validation.
     * @param v2   the second validation.
     * @param v3   the third validation.
     * @param v4   the fourth validation.
     * @param <T1> type of the first value.
     * @param <T2> type of the second value.
     * @param <T3> type of the third value.
     * @param <T4> type of the fourth value.
     * @return a {@link Tuple4} containing the valid values.
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
     * @param v1   the first validation.
     * @param v2   the second validation.
     * @param v3   the third validation.
     * @param v4   the fourth validation.
     * @param v5   the fifth validation.
     * @param <T1> type of the first value.
     * @param <T2> type of the second value.
     * @param <T3> type of the third value.
     * @param <T4> type of the fourth value.
     * @param <T5> type of the fifth value.
     * @return a {@link Tuple5} containing the valid values.
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
     * @param v1   the first validation.
     * @param v2   the second validation.
     * @param v3   the third validation.
     * @param v4   the fourth validation.
     * @param v5   the fifth validation.
     * @param v6   the sixth validation.
     * @param <T1> type of the first value.
     * @param <T2> type of the second value.
     * @param <T3> type of the third value.
     * @param <T4> type of the fourth value.
     * @param <T5> type of the fifth value.
     * @param <T6> type of the sixth value.
     * @return a {@link Tuple6} containing the valid values.
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
     * @param v1   the first validation.
     * @param v2   the second validation.
     * @param v3   the third validation.
     * @param v4   the fourth validation.
     * @param v5   the fifth validation.
     * @param v6   the sixth validation.
     * @param v7   the seventh validation.
     * @param <T1> type of the first value.
     * @param <T2> type of the second value.
     * @param <T3> type of the third value.
     * @param <T4> type of the fourth value.
     * @param <T5> type of the fifth value.
     * @param <T6> type of the sixth value.
     * @param <T7> type of the seventh value.
     * @return a {@link Tuple7} containing the valid values.
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
     * @param v1   the first validation.
     * @param v2   the second validation.
     * @param v3   the third validation.
     * @param v4   the fourth validation.
     * @param v5   the fifth validation.
     * @param v6   the sixth validation.
     * @param v7   the seventh validation.
     * @param v8   the eighth validation.
     * @param <T1> type of the first value.
     * @param <T2> type of the second value.
     * @param <T3> type of the third value.
     * @param <T4> type of the fourth value.
     * @param <T5> type of the fifth value.
     * @param <T6> type of the sixth value.
     * @param <T7> type of the seventh value.
     * @param <T8> type of the eighth value.
     * @return a {@link Tuple8} containing the valid values.
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

    /**
     * Starts a validation process for a collection of values.
     *
     * @param values the values to validate.
     * @param <T>    the type of the values.
     * @return a {@link ValidateAllDSL} instance.
     */
    public static <T> ValidateAllDSL<T> validateAll(Iterable<T> values) {
        return new ValidateAllDSL<>(values);
    }

    /**
     * DSL class for validating a collection of values.
     *
     * @param <T> the type of the values.
     */
    public static class ValidateAllDSL<T> {
        private final Iterable<T> values;

        public ValidateAllDSL(Iterable<T> values) {
            this.values = Objects.requireNonNull(values);
        }

        /**
         * Validates that all values in the collection satisfy the given rule.
         * The result is a validation containing the list of valid values, or all errors encountered.
         *
         * @param rule the rule to apply to each value.
         * @return a {@link Validation} of the collection.
         */
        public Validation<List<T>> areAll(Rule<? super T> rule) {
            Objects.requireNonNull(rule, "Rule cannot be null");
            return Validation.sequence(List.ofAll(values).map(v -> Validation.narrowSuper(rule.test(v))));
        }
    }

    /**
     * Starts a validation process for a single value.
     *
     * @param value the value to validate.
     * @param <T>   the type of the value.
     * @return a {@link ValidationDSL} instance.
     */
    public static <T> ValidationDSL<T> validateThat(T value) {
        return new ValidationDSL<>(value);
    }

    /**
     * Starts a validation process for a single value with a logical name.
     * The name will be prepended to any error messages.
     *
     * @param value the value to validate.
     * @param name  the name of the value (e.g. field name).
     * @param <T>   the type of the value.
     * @return a {@link ValidationDSL} instance.
     */
    public static <T> ValidationDSL<T> validateThat(T value, String name) {
        return new ValidationDSL<>(value, name);
    }

    public static <T> ListValidationDSL<T> validateThatList(List<T> value, String name) {
        return new ListValidationDSL<>(value, name);
    }

    /**
     * DSL class for validating a single value.
     *
     * @param <T> the type of the value.
     */
    public static class ValidationDSL<T> {

        private final Validation<T> validation;
        private String name = "";

        public ValidationDSL(T value) {
            this.validation = Validation.valid(value);
        }

        public ValidationDSL(T value, String name) {
            if (value == null) {
                this.validation = (Validation<T>) Validation.invalid(ErrorMessage.of("must.not.be.null")).at(name);
            } else {
                this.validation = Validation.valid(value).at(name);
            }
            this.name = name;
        }

        private ValidationDSL(Validation<T> validation, String name) {
            this.validation = Objects.requireNonNull(validation, "validation cannot be null");
            this.name = name;
        }

        /**
         * Maps the value being validated using the provided mapper function.
         * If the current validation is already invalid, the mapper is not applied.
         *
         * @param mapper the function to apply.
         * @param <Z>    the result type of the mapping.
         * @return a new {@link ValidationDSL} with the mapped value.
         */
        public <Z> ValidationDSL<Z> map(Function1<T, Z> mapper) {
            return new ValidationDSL<>(validation.mapCatching(mapper), name);
        }

        /**
         * Maps the value being validated using the provided mapper function.
         * If the current validation is already invalid, the mapper is not applied.
         *
         * @param mapper the function to apply.
         * @param <Z>    the result type of the mapping.
         * @return a new {@link ValidationDSL} with the mapped value.
         */
        public <Z> Validation<Z> mapsTo(Function1<T, Z> mapper) {
            return this.validation.mapCatching(mapper).at(name);
        }

        /**
         * Validates that the value satisfies the given rule.
         * If the value is {@code null}, an error "must.not.be.null" is automatically added.
         *
         * @param rule the rule to check.
         * @return a {@link Validation} result.
         */
        public Validation<T> is(Rule<? super T> rule) {
            Objects.requireNonNull(rule, "rule cannot be null");
            return validation
                    .flatMap(v -> Validation.narrowSuper(Rule.notNull().and(rule).test(v).at(name)));
        }

        /**
         * Validates that the value satisfies the given rule.
         * If the value is {@code null}, an error "must.not.be.null" is automatically added.
         *
         * @param rule the rule to check.
         * @return a {@link Validation} result.
         */
        public <R> Validation<R> is(MappingRule<? super T, ? extends R> rule) {
            Objects.requireNonNull(rule, "rule cannot be null");
            return validation
                    .flatMap(v -> Validation.narrowSuper(MappingRule.<T>notNull().andThen(rule).test(v).at(name)));
        }

        /**
         * Validates that the value satisfies the given rule.
         * If the value is {@code null}, an error "must.not.be.null" is automatically added.
         *
         * @param rule the rule to check.
         * @return a {@link Validation} result.
         */
        public ValidationDSL<T> passes(Rule<T> rule) {
            if (validation.isValid()) {
                return new ValidationDSL<>(rule.test(validation.getOrElseThrow()).at(this.name), this.name);
            } else {
                return this;
            }
        }
    }

    /**
     * DSL class for validating a single value.
     *
     * @param <T> the type of the value.
     */
    public static class ListValidationDSL<T> {
        private final List<T> value;
        private final Validation<List<T>> validation;
        private final String name;

        public ListValidationDSL(List<T> value) {
            this(value, "");
        }

        public ListValidationDSL(List<T> value, String name) {
            this.value = value;
            this.name = name;
            if (value == null) {
                this.validation = Validation.<List<T>>invalid(ErrorMessage.of("must.not.be.null")).at(name);
            } else {
                this.validation = Validation.valid(value).at(name);
            }
        }

        private ListValidationDSL(Validation<List<T>> validation, List<T> value, String name) {
            this.validation = Objects.requireNonNull(validation, "validation cannot be null");
            this.value = value;
            this.name = name;
        }

        /**
         * Maps the value being validated using the provided mapper function.
         * If the current validation is already invalid, the mapper is not applied.
         *
         * @param mapper the function to apply.
         * @param <Z>    the result type of the mapping.
         * @return a new {@link ListValidationDSL} with the mapped value.
         */
        public <Z> ListValidationDSL<Z> map(Function1<? super T, Z> mapper) {
            return new ListValidationDSL<>(
                    validation.mapCatching(l -> l.map(mapper)),
                    value != null ? value.map(mapper) : null,
                    name
            );
        }

        /**
         * Maps the value being validated using the provided mapper function.
         * If the current validation is already invalid, the mapper is not applied.
         *
         * @param mapper the function to apply.
         * @param <Z>    the result type of the mapping.
         * @return a {@link Validation} of the mapped list.
         */
        public <Z> Validation<List<Z>> mapsTo(Function1<? super T, Z> mapper) {
            return this.validation.mapCatching(l -> l.map(mapper));
        }

        /**
         * Validates that all elements satisfy the given rule.
         *
         * @param rule the rule to apply to each element.
         * @return a {@link Validation} result.
         */
        public Validation<List<T>> is(Rule<? super T> rule) {
            Objects.requireNonNull(rule, "rule cannot be null");
            return Validation.narrowSuper(validation
                    .flatMap(v -> Validation.sequence(v.map(rule::test))));
        }

        /**
         * Validates that the list satisfies the given rule and all elements satisfy the element rule.
         *
         * @param listRule    the rule for the list itself.
         * @param elementRule the rule for each element.
         * @return a {@link Validation} result.
         */
        public Validation<List<T>> is(Rule<? super List<T>> listRule, Rule<? super T> elementRule) {
            return satisfying(listRule).each(elementRule).validation;
        }

        /**
         * Validates that the list satisfies the given rule and all elements satisfy the element rule,
         * then maps each element.
         *
         * @param listRule    the rule for the list itself.
         * @param elementRule the rule for each element.
         * @param mapper      the function to map each element.
         * @param <Z>         the target type.
         * @return a {@link Validation} result.
         */
        public <Z> Validation<List<Z>> is(Rule<? super List<T>> listRule, Rule<? super T> elementRule, Function1<? super T, Z> mapper) {
            return satisfying(listRule).each(elementRule).mapsTo(mapper);
        }

        /**
         * Validates that all elements satisfy the given rule.
         * This method is non-short-circuiting and will collect errors even if the list is already invalid.
         *
         * @param rule the rule to apply to each element.
         * @return a {@link ListValidationDSL} for chaining.
         */
        public ListValidationDSL<T> each(Rule<? super T> rule) {
            if (value == null) {
                return this;
            }
            Validation<List<T>> ruleValidation = Rule.<T>narrow(rule).liftToList().test(value).at(this.name);
            return new ListValidationDSL<>(combine(validation, ruleValidation), value, this.name);
        }

        /**
         * Validates that the list satisfies the given rule.
         * This method is non-short-circuiting and will collect errors even if the list is already invalid.
         *
         * @param rule the rule for the list.
         * @return a {@link ListValidationDSL} for chaining.
         */
        public ListValidationDSL<T> passes(Rule<? super List<T>> rule) {
            if (value == null) {
                return this;
            }
            Validation<List<T>> ruleValidation = Rule.<List<T>>narrow(rule).test(value).at(this.name);
            return new ListValidationDSL<>(combine(validation, ruleValidation), value, this.name);
        }

        /**
         * Alias for {@link #passes(Rule)}.
         *
         * @param rule the rule for the list.
         * @return a {@link ListValidationDSL} for chaining.
         */
        public ListValidationDSL<T> satisfying(Rule<? super List<T>> rule) {
            return passes(rule);
        }

        private static <T> Validation<T> combine(Validation<T> v1, Validation<T> v2) {
            return Validation.mapN(v1, v2, (a, b) -> a);
        }
    }

}
