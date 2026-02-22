package net.vanfleteren.fv;

import io.vavr.*;
import io.vavr.collection.Iterator;
import io.vavr.collection.List;

import java.util.Objects;

public class API {


    /**
     * Asserts that all provided validations are valid, otherwise throws a ValidationException with all errors.
     */
    public static void assertAllValid(Validation<?>... validations) {
        Iterator<ErrorMessage> it = Iterator.of(validations).flatMap(Validation::errors);
        if(! it.isEmpty()) {
            throw new ValidationException(it.toList());
        }
    }

    //region assertAllValid with Tuples
    public static <T1, T2> Tuple2<T1, T2> assertAllValid(Validation<T1> v1, Validation<T2> v2) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        return Validation.mapN(v1, v2, Tuple::of).getOrElseThrow();
    }

    public static <T1, T2, T3> Tuple3<T1, T2, T3> assertAllValid(
            Validation<T1> v1, Validation<T2> v2, Validation<T3> v3
    ) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        return Validation.mapN(v1, v2, v3, Tuple::of).getOrElseThrow();
    }

    public static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> assertAllValid(
            Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4
    ) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        Objects.requireNonNull(v4, "v4 validation cannot be null");
        return Validation.mapN(v1, v2, v3, v4, Tuple::of).getOrElseThrow();
    }

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

    public static <T> ValidateAllDSL<T> validateAll(Iterable<T> values) {
        return new ValidateAllDSL<>(values);
    }

    public static class ValidateAllDSL<T> {
        private final Iterable<T> values;

        public ValidateAllDSL(Iterable<T> values) {
            this.values = Objects.requireNonNull(values);
        }

        public Validation<List<T>> areAll(Rule<? super T> rule) {
            Objects.requireNonNull(rule, "Rule cannot be null");
            return Validation.sequence(List.ofAll(values).map(v -> Validation.narrowSuper(rule.test(v))));
        }
    }

    public static <T> ValidationDSL<T> validateThat(T value) {
        return new ValidationDSL<>(value);
    }

    public static <T> ValidationDSL<T> validateThat(T value,String name) {
        return new ValidationDSL<>(value,name);
    }

    public static class ValidationDSL<T> {

        private final Validation<T> validation;
        private String name = "";

        public ValidationDSL(T value) {
            this.validation = Validation.valid(value);
        }

        public ValidationDSL(T value, String name) {
            this.validation = Validation.valid(value);
            this.name = name;
        }

        private ValidationDSL(Validation<T> validation, String name) {
            this.validation = Objects.requireNonNull(validation, "validation cannot be null");
            this.name = name;
        }

        public <Z> ValidationDSL<Z> map(Function1<T, Z> mapper) {
            return new ValidationDSL<>(validation.mapCatching(mapper), name);
        }

        public Validation<T> is(Rule<? super T> rule) {
            Objects.requireNonNull(rule, "rule cannot be null");
            return validation
                    .flatMap(v -> Validation.narrowSuper(Rule.notNull().and(rule).test(v).at(name)));
        }
    }

}
