package be.iffy.fv.dsl.impl;

import io.vavr.*;
import be.iffy.fv.Validation;

import java.util.Objects;

/**
 * A small DSL that's basically just a wrapper around {@link Validation#combine}.
 */
public final class ValidatingDSL {

    public static class ValidatingBuilder2<T1, T2> {
        final Tuple2<Validation<T1>, Validation<T2>> vs;

        public ValidatingBuilder2(Validation<T1> v1, Validation<T2> v2) {
            this.vs = Tuple.of(Objects.requireNonNull(v1), Objects.requireNonNull(v2));
        }

        /**
         * Maps the values of the passed Validations if they are {@link Validation.Valid}
         *
         * @param mapper a function that takes the inputs of types T1, and T2 and produces a result of type T
         
         * @return a {@code Validation<T>} containing the transformed result, or an {@link Validation.Invalid} if either validation fails
         */
        public <T> Validation<T> map(Function2<T1, T2, T> mapper) {
            return Validation.combine(vs._1, vs._2).map(mapper);
        }

        /**
         * FlatMaps the values of the passed Validations if they are {@link Validation.Valid}
         *
         * @param mapper a function that takes two inputs of types T1, and T2 and produces a {@code Validation} of type T
         
         * @return a {@code Validation<T>} containing the transformed result, or an {@link Validation.Invalid} if any of the input validations fail
         */
        public <T> Validation<T> flatMap(Function2<T1, T2, Validation<? extends T>> mapper) {
            return Validation.combine(vs._1, vs._2).flatMap(mapper);
        }
    }

    public static class ValidatingBuilder3<T1, T2, T3> {
        final Tuple3<Validation<T1>, Validation<T2>, Validation<T3>> vs;

        public ValidatingBuilder3(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3) {
            this.vs = Tuple.of(Objects.requireNonNull(v1), Objects.requireNonNull(v2), Objects.requireNonNull(v3));
        }

        /**
         * Maps the values of the passed Validations if they are {@link Validation.Valid}
         *
         * @param mapper a function that takes the inputs of types T1, T2, and T3 and produces a result of type T
         
         * @return a {@code Validation<T>} containing the transformed result, or an {@link Validation.Invalid} if either validation fails
         */
        public <T> Validation<T> map(Function3<T1, T2, T3, T> mapper) {
            return Validation.combine(vs._1, vs._2, vs._3).map(mapper);
        }

        /**
         * FlatMaps the values of the passed Validations if they are {@link Validation.Valid}
         *
         * @param mapper a function that takes three inputs of types T1, T2, and T3 and produces a {@code Validation} of type T
         
         * @return a {@code Validation<T>} containing the transformed result, or an {@link Validation.Invalid} if any of the input validations fail
         */
        public <T> Validation<T> flatMap(Function3<T1, T2, T3, Validation<? extends T>> mapper) {
            return Validation.combine(vs._1, vs._2, vs._3).flatMap(mapper);
        }
    }

    public static class ValidatingBuilder4<T1, T2, T3, T4> {
        final Tuple4<Validation<T1>, Validation<T2>, Validation<T3>, Validation<T4>> vs;

        public ValidatingBuilder4(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4) {
            this.vs = Tuple.of(
                Objects.requireNonNull(v1),
                Objects.requireNonNull(v2),
                Objects.requireNonNull(v3),
                Objects.requireNonNull(v4)
            );
        }

        /**
         * Maps the values of the passed Validations if they are {@link Validation.Valid}
         *
         * @param mapper a function that takes the inputs of types T1, T2, T3, and T4 and produces a result of type T
         
         * @return a {@code Validation<T>} containing the transformed result, or an {@link Validation.Invalid} if either validation fails
         */
        public <T> Validation<T> map(Function4<T1, T2, T3, T4, T> mapper) {
            return Validation.combine(vs._1, vs._2, vs._3, vs._4).map(mapper);
        }

        /**
         * FlatMaps the values of the passed Validations if they are {@link Validation.Valid}
         *
         * @param mapper a function that takes four inputs of types T1, T2, T3, and T4 and produces a {@code Validation} of type T
         
         * @return a {@code Validation<T>} containing the transformed result, or an {@link Validation.Invalid} if any of the input validations fail
         */
        public <T> Validation<T> flatMap(Function4<T1, T2, T3, T4, Validation<? extends T>> mapper) {
            return Validation.combine(vs._1, vs._2, vs._3, vs._4).flatMap(mapper);
        }
    }

    public static class ValidatingBuilder5<T1, T2, T3, T4, T5> {
        final Tuple5<Validation<T1>, Validation<T2>, Validation<T3>, Validation<T4>, Validation<T5>> vs;

        public ValidatingBuilder5(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4, Validation<T5> v5) {
            this.vs = Tuple.of(
                Objects.requireNonNull(v1),
                Objects.requireNonNull(v2),
                Objects.requireNonNull(v3),
                Objects.requireNonNull(v4),
                Objects.requireNonNull(v5)
            );
        }

        /**
         * Maps the values of the passed Validations if they are {@link Validation.Valid}
         *
         * @param mapper a function that takes the inputs of types T1, T2, T3, T4, and T5 and produces a result of type T
         
         * @return a {@code Validation<T>} containing the transformed result, or an {@link Validation.Invalid} if either validation fails
         */
        public <T> Validation<T> map(Function5<T1, T2, T3, T4, T5, T> mapper) {
            return Validation.combine(vs._1, vs._2, vs._3, vs._4, vs._5).map(mapper);
        }

        /**
         * FlatMaps the values of the passed Validations if they are {@link Validation.Valid}
         *
         * @param mapper a function that takes five inputs of types T1, T2, T3, T4, and T5 and produces a {@code Validation} of type T
         
         * @return a {@code Validation<T>} containing the transformed result, or an {@link Validation.Invalid} if any of the input validations fail
         */
        public <T> Validation<T> flatMap(Function5<T1, T2, T3, T4, T5, Validation<? extends T>> mapper) {
            return Validation.combine(vs._1, vs._2, vs._3, vs._4, vs._5).flatMap(mapper);
        }
    }

    public static class ValidatingBuilder6<T1, T2, T3, T4, T5, T6> {
        final Tuple6<Validation<T1>, Validation<T2>, Validation<T3>, Validation<T4>, Validation<T5>, Validation<T6>> vs;

        public ValidatingBuilder6(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4, Validation<T5> v5, Validation<T6> v6) {
            this.vs = Tuple.of(
                Objects.requireNonNull(v1),
                Objects.requireNonNull(v2),
                Objects.requireNonNull(v3),
                Objects.requireNonNull(v4),
                Objects.requireNonNull(v5),
                Objects.requireNonNull(v6)
            );
        }

        /**
         * Maps the values of the passed Validations if they are {@link Validation.Valid}
         *
         * @param mapper a function that takes the inputs of types T1, T2, T3, T4, T5, and T6 and produces a result of type T
         
         * @return a {@code Validation<T>} containing the transformed result, or an {@link Validation.Invalid} if either validation fails
         */
        public <T> Validation<T> map(Function6<T1, T2, T3, T4, T5, T6, T> mapper) {
            return Validation.combine(vs._1, vs._2, vs._3, vs._4, vs._5, vs._6).map(mapper);
        }

        /**
         * FlatMaps the values of the passed Validations if they are {@link Validation.Valid}
         *
         * @param mapper a function that takes six inputs of types T1, T2, T3, T4, T5, and T6 and produces a {@code Validation} of type T
         
         * @return a {@code Validation<T>} containing the transformed result, or an {@link Validation.Invalid} if any of the input validations fail
         */
        public <T> Validation<T> flatMap(Function6<T1, T2, T3, T4, T5, T6, Validation<? extends T>> mapper) {
            return Validation.combine(vs._1, vs._2, vs._3, vs._4, vs._5, vs._6).flatMap(mapper);
        }
    }

    public static class ValidatingBuilder7<T1, T2, T3, T4, T5, T6, T7> {
        final Tuple7<Validation<T1>, Validation<T2>, Validation<T3>, Validation<T4>, Validation<T5>, Validation<T6>, Validation<T7>> vs;

        public ValidatingBuilder7(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4, Validation<T5> v5, Validation<T6> v6, Validation<T7> v7) {
            this.vs = Tuple.of(
                Objects.requireNonNull(v1),
                Objects.requireNonNull(v2),
                Objects.requireNonNull(v3),
                Objects.requireNonNull(v4),
                Objects.requireNonNull(v5),
                Objects.requireNonNull(v6),
                Objects.requireNonNull(v7)
            );
        }

        /**
         * Maps the values of the passed Validations if they are {@link Validation.Valid}
         *
         * @param mapper a function that takes the inputs of types T1, T2, T3, T4, T5, T6, and T7 and produces a result of type T
         
         * @return a {@code Validation<T>} containing the transformed result, or an {@link Validation.Invalid} if either validation fails
         */
        public <T> Validation<T> map(Function7<T1, T2, T3, T4, T5, T6, T7, T> mapper) {
            return Validation.combine(vs._1, vs._2, vs._3, vs._4, vs._5, vs._6, vs._7).map(mapper);
        }

        /**
         * FlatMaps the values of the passed Validations if they are {@link Validation.Valid}
         *
         * @param mapper a function that takes seven inputs of types T1, T2, T3, T4, T5, T6, and T7 and produces a {@code Validation} of type T
         
         * @return a {@code Validation<T>} containing the transformed result, or an {@link Validation.Invalid} if any of the input validations fail
         */
        public <T> Validation<T> flatMap(Function7<T1, T2, T3, T4, T5, T6, T7, Validation<? extends T>> mapper) {
            return Validation.combine(vs._1, vs._2, vs._3, vs._4, vs._5, vs._6, vs._7).flatMap(mapper);
        }
    }

    public static class ValidatingBuilder8<T1, T2, T3, T4, T5, T6, T7, T8> {
        final Tuple8<Validation<T1>, Validation<T2>, Validation<T3>, Validation<T4>, Validation<T5>, Validation<T6>, Validation<T7>, Validation<T8>> vs;

       public ValidatingBuilder8(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Validation<T4> v4, Validation<T5> v5, Validation<T6> v6, Validation<T7> v7, Validation<T8> v8) {
            this.vs = Tuple.of(
                Objects.requireNonNull(v1),
                Objects.requireNonNull(v2),
                Objects.requireNonNull(v3),
                Objects.requireNonNull(v4),
                Objects.requireNonNull(v5),
                Objects.requireNonNull(v6),
                Objects.requireNonNull(v7),
                Objects.requireNonNull(v8)
            );
        }

        /**
         * Maps the values of the passed Validations if they are {@link Validation.Valid}
         *
         * @param mapper a function that takes the inputs of types T1, T2, T3, T4, T5, T6, T7, and T8 and produces a result of type T
         
         * @return a {@code Validation<T>} containing the transformed result, or an {@link Validation.Invalid} if either validation fails
         */
        public <T> Validation<T> map(Function8<T1, T2, T3, T4, T5, T6, T7, T8, T> mapper) {
            return Validation.combine(vs._1, vs._2, vs._3, vs._4, vs._5, vs._6, vs._7, vs._8).map(mapper);
        }

        /**
         * FlatMaps the values of the passed Validations if they are {@link Validation.Valid}
         *
         * @param mapper a function that takes eight inputs of types T1, T2, T3, T4, T5, T6, T7, and T8 and produces a {@code Validation} of type T
         
         * @return a {@code Validation<T>} containing the transformed result, or an {@link Validation.Invalid} if any of the input validations fail
         */
        public <T> Validation<T> flatMap(Function8<T1, T2, T3, T4, T5, T6, T7, T8, Validation<? extends T>> mapper) {
            return Validation.combine(vs._1, vs._2, vs._3, vs._4, vs._5, vs._6, vs._7, vs._8).flatMap(mapper);
        }
    }

}
