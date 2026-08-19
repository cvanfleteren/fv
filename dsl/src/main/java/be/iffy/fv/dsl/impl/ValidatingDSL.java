package be.iffy.fv.dsl.impl;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import be.iffy.fv.Validations;
import io.vavr.*;
import org.jetbrains.annotations.Contract;

import java.util.Objects;

/**
 * A small DSL that's basically just a wrapper around {@link Validations#combine}.
 */
public final class ValidatingDSL {

    /**
     * Creates a {@link Rule} for a {@link Tuple3} that fails unless the three elements satisfy the specified
     * predicate.
     * <p>
     * Error key: the {@code errorKey} passed as argument.
     *
     * @param predicate the predicate to apply to the three elements of the tuple.
     * @param errorKey  the error key to use if the predicate returns {@code false}.
     */
    public static <T1, T2, T3> Rule<Tuple3<T1, T2, T3>> satisfies(
            Function3<? super T1, ? super T2, ? super T3, Boolean> predicate, String errorKey) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return Rule.of(
                t -> predicate.apply(t._1, t._2, t._3),
                errorKey
        );
    }

    /**
     * Creates a {@link Rule} for a {@link Tuple3} that fails unless the three elements satisfy the specified
     * predicate.
     * <p>
     * Error key: the {@code errorMessage} passed as argument.
     *
     * @param predicate    the predicate to apply to the three elements of the tuple.
     * @param errorMessage the error message to use if the predicate returns {@code false}.
     */
    public static <T1, T2, T3> Rule<Tuple3<T1, T2, T3>> satisfies(
            Function3<? super T1, ? super T2, ? super T3, Boolean> predicate, ErrorMessage errorMessage) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return Rule.of(
                t -> predicate.apply(t._1, t._2, t._3),
                errorMessage
        );
    }

    /**
     * Combine multiple Validations, allowing you to map or flatMap over their values.
     */
    public static final class ValidatingBuilder2<T1, T2> {
        private final Validation<T1> v1;
        private final Validation<T2> v2;

        public ValidatingBuilder2(Validation<T1> v1, Validation<T2> v2) {
            this.v1 = Objects.requireNonNull(v1);
            this.v2 = Objects.requireNonNull(v2);
        }

        /**
         * Maps the values of the passed Validations if they are {@link Validation.Valid}
         */
        @Contract(pure = true)
        public <T> Validation<T> map(Function2<T1, T2, T> mapper) {
            return Validations.combine(v1, v2).map(mapper);
        }

        /**
         * FlatMaps the values of the passed Validations if they are {@link Validation.Valid}
         */
        @Contract(pure = true)
        public <T> Validation<T> flatMap(Function2<T1, T2, Validation<? extends T>> mapper) {
            return Validations.combine(v1, v2).flatMap(mapper);
        }

        /**
         * Combines the values of the passed Validations into a {@link Tuple2} and validates it against the
         * given rule. Only runs if both Validations are already {@link Validation.Valid}.
         */
        @Contract(pure = true)
        public Validation<Tuple2<T1, T2>> is(Rule<? super Tuple2<T1, T2>> rule) {
            Objects.requireNonNull(rule, "rule cannot be null");
            return map(Tuple::of).flatMap(rule.narrow());
        }
    }

    /**
     * Combine multiple Validations, allowing you to map or flatMap over their values.
     */
    public static final class ValidatingBuilder3<T1, T2, T3> {
        private final Validation<T1> v1;
        private final Validation<T2> v2;
        private final Validation<T3> v3;

        public ValidatingBuilder3(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3) {
            this.v1 = Objects.requireNonNull(v1);
            this.v2 = Objects.requireNonNull(v2);
            this.v3 = Objects.requireNonNull(v3);
        }

        /**
         * Maps the values of the passed Validations if they are {@link Validation.Valid}
         */
        @Contract(pure = true)
        public <T> Validation<T> map(Function3<T1, T2, T3, T> mapper) {
            return Validations.combine(v1, v2, v3).map(mapper);
        }

        /**
         * FlatMaps the values of the passed Validations if they are {@link Validation.Valid}
         */
        @Contract(pure = true)
        public <T> Validation<T> flatMap(Function3<T1, T2, T3, Validation<? extends T>> mapper) {
            return Validations.combine(v1, v2, v3).flatMap(mapper);
        }

        /**
         * Combines the values of the passed Validations into a {@link Tuple3} and validates it against the
         * given rule. Only runs if all three Validations are already {@link Validation.Valid}.
         */
        @Contract(pure = true)
        public Validation<Tuple3<T1, T2, T3>> is(Rule<? super Tuple3<T1, T2, T3>> rule) {
            Objects.requireNonNull(rule, "rule cannot be null");
            return map(Tuple::of).flatMap(rule.narrow());
        }
    }

    /**
     * Combine multiple Validations, allowing you to map or flatMap over their values.
     */
    public static final class ValidatingBuilder4<T1, T2, T3, T4> {
        private final Validation<T1> v1;
        private final Validation<T2> v2;
        private final Validation<T3> v3;
        private final Validation<T4> v4;

        public ValidatingBuilder4(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4) {
            this.v1 = Objects.requireNonNull(v1);
            this.v2 = Objects.requireNonNull(v2);
            this.v3 = Objects.requireNonNull(v3);
            this.v4 = Objects.requireNonNull(v4);
        }

        /**
         * Maps the values of the passed Validations if they are {@link Validation.Valid}
         */
        @Contract(pure = true)
        public <T> Validation<T> map(Function4<T1, T2, T3, T4, T> mapper) {
            return Validations.combine(v1, v2, v3, v4).map(mapper);
        }

        /**
         * FlatMaps the values of the passed Validations if they are {@link Validation.Valid}
         */
        @Contract(pure = true)
        public <T> Validation<T> flatMap(Function4<T1, T2, T3, T4, Validation<? extends T>> mapper) {
            return Validations.combine(v1, v2, v3, v4).flatMap(mapper);
        }

        /**
         * Combines the values of the passed Validations into a {@link Tuple4} and validates it against the
         * given rule. Only runs if all four Validations are already {@link Validation.Valid}.
         */
        @Contract(pure = true)
        public Validation<Tuple4<T1, T2, T3, T4>> is(Rule<? super Tuple4<T1, T2, T3, T4>> rule) {
            Objects.requireNonNull(rule, "rule cannot be null");
            return map(Tuple::of).flatMap(rule.narrow());
        }
    }

    /**
     * Combine multiple Validations, allowing you to map or flatMap over their values.
     */
    public static final class ValidatingBuilder5<T1, T2, T3, T4, T5> {
        private final Validation<T1> v1;
        private final Validation<T2> v2;
        private final Validation<T3> v3;
        private final Validation<T4> v4;
        private final Validation<T5> v5;

        public ValidatingBuilder5(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4, Validation<T5> v5) {
            this.v1 = Objects.requireNonNull(v1);
            this.v2 = Objects.requireNonNull(v2);
            this.v3 = Objects.requireNonNull(v3);
            this.v4 = Objects.requireNonNull(v4);
            this.v5 = Objects.requireNonNull(v5);
        }

        /**
         * Maps the values of the passed Validations if they are {@link Validation.Valid}
         */
        @Contract(pure = true)
        public <T> Validation<T> map(Function5<T1, T2, T3, T4, T5, T> mapper) {
            return Validations.combine(v1, v2, v3, v4, v5).map(mapper);
        }

        /**
         * FlatMaps the values of the passed Validations if they are {@link Validation.Valid}
         */
        @Contract(pure = true)
        public <T> Validation<T> flatMap(Function5<T1, T2, T3, T4, T5, Validation<? extends T>> mapper) {
            return Validations.combine(v1, v2, v3, v4, v5).flatMap(mapper);
        }
    }

    /**
     * Combine multiple Validations, allowing you to map or flatMap over their values.
     */
    public static final class ValidatingBuilder6<T1, T2, T3, T4, T5, T6> {
        private final Validation<T1> v1;
        private final Validation<T2> v2;
        private final Validation<T3> v3;
        private final Validation<T4> v4;
        private final Validation<T5> v5;
        private final Validation<T6> v6;

        public ValidatingBuilder6(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4, Validation<T5> v5, Validation<T6> v6) {
            this.v1 = Objects.requireNonNull(v1);
            this.v2 = Objects.requireNonNull(v2);
            this.v3 = Objects.requireNonNull(v3);
            this.v4 = Objects.requireNonNull(v4);
            this.v5 = Objects.requireNonNull(v5);
            this.v6 = Objects.requireNonNull(v6);
        }

        /**
         * Maps the values of the passed Validations if they are {@link Validation.Valid}
         */
        @Contract(pure = true)
        public <T> Validation<T> map(Function6<T1, T2, T3, T4, T5, T6, T> mapper) {
            return Validations.combine(v1, v2, v3, v4, v5, v6).map(mapper);
        }

        /**
         * FlatMaps the values of the passed Validations if they are {@link Validation.Valid}
         */
        @Contract(pure = true)
        public <T> Validation<T> flatMap(Function6<T1, T2, T3, T4, T5, T6, Validation<? extends T>> mapper) {
            return Validations.combine(v1, v2, v3, v4, v5, v6).flatMap(mapper);
        }
    }

    /**
     * Combine multiple Validations, allowing you to map or flatMap over their values.
     */
    public static final class ValidatingBuilder7<T1, T2, T3, T4, T5, T6, T7> {
        private final Validation<T1> v1;
        private final Validation<T2> v2;
        private final Validation<T3> v3;
        private final Validation<T4> v4;
        private final Validation<T5> v5;
        private final Validation<T6> v6;
        private final Validation<T7> v7;

        public ValidatingBuilder7(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4, Validation<T5> v5, Validation<T6> v6, Validation<T7> v7) {
            this.v1 = Objects.requireNonNull(v1);
            this.v2 = Objects.requireNonNull(v2);
            this.v3 = Objects.requireNonNull(v3);
            this.v4 = Objects.requireNonNull(v4);
            this.v5 = Objects.requireNonNull(v5);
            this.v6 = Objects.requireNonNull(v6);
            this.v7 = Objects.requireNonNull(v7);
        }

        /**
         * Maps the values of the passed Validations if they are {@link Validation.Valid}
         */
        @Contract(pure = true)
        public <T> Validation<T> map(Function7<T1, T2, T3, T4, T5, T6, T7, T> mapper) {
            return Validations.combine(v1, v2, v3, v4, v5, v6, v7).map(mapper);
        }

        /**
         * FlatMaps the values of the passed Validations if they are {@link Validation.Valid}
         */
        @Contract(pure = true)
        public <T> Validation<T> flatMap(Function7<T1, T2, T3, T4, T5, T6, T7, Validation<? extends T>> mapper) {
            return Validations.combine(v1, v2, v3, v4, v5, v6, v7).flatMap(mapper);
        }
    }

    /**
     * Combine multiple Validations, allowing you to map or flatMap over their values.
     */
    public static final class ValidatingBuilder8<T1, T2, T3, T4, T5, T6, T7, T8> {
        private final Validation<T1> v1;
        private final Validation<T2> v2;
        private final Validation<T3> v3;
        private final Validation<T4> v4;
        private final Validation<T5> v5;
        private final Validation<T6> v6;
        private final Validation<T7> v7;
        private final Validation<T8> v8;

       public ValidatingBuilder8(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4, Validation<T5> v5, Validation<T6> v6, Validation<T7> v7, Validation<T8> v8) {
            this.v1 = Objects.requireNonNull(v1);
            this.v2 = Objects.requireNonNull(v2);
            this.v3 = Objects.requireNonNull(v3);
            this.v4 = Objects.requireNonNull(v4);
            this.v5 = Objects.requireNonNull(v5);
            this.v6 = Objects.requireNonNull(v6);
            this.v7 = Objects.requireNonNull(v7);
            this.v8 = Objects.requireNonNull(v8);
        }

        /**
         * Maps the values of the passed Validations if they are {@link Validation.Valid}
         */
        @Contract(pure = true)
        public <T> Validation<T> map(Function8<T1, T2, T3, T4, T5, T6, T7, T8, T> mapper) {
            return Validations.combine(v1, v2, v3, v4, v5, v6, v7, v8).map(mapper);
        }

        /**
         * FlatMaps the values of the passed Validations if they are {@link Validation.Valid}
         */
        @Contract(pure = true)
        public <T> Validation<T> flatMap(Function8<T1, T2, T3, T4, T5, T6, T7, T8, Validation<? extends T>> mapper) {
            return Validations.combine(v1, v2, v3, v4, v5, v6, v7, v8).flatMap(mapper);
        }
    }

}
