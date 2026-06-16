package be.iffy.fv;

import io.vavr.*;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Option;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class Validations {

    /**
     * Transforms a {@link Seq} of {@link Validation}s into a single {@code Validation} of a {@link List}.
     * If any validation is invalid, the result will contain all accumulated errors.
     *
     * @param validations the sequence of validations to sequence.
     */
    public static <T> Validation<List<T>> sequence(Seq<? extends Validation<? extends T>> validations) {
        return sequence(validations, "");
    }

    /**
     * Transforms a {@link Seq} of {@link Validation}s into a single {@code Validation} of a {@link List}.
     * If any validation is invalid, the result will contain all accumulated errors.
     *
     * @param validations the sequence of validations to sequence.
     * @param name        the path entry under which the errors will be mapped. e.g., name "foo" will result in errormessages like "foo[1].some.message"
     *                    if the second entry in the list is invalid.
     */
    public static <T> Validation<List<T>> sequence(Seq<? extends Validation<? extends T>> validations, String name) {
        Objects.requireNonNull(validations, "validations cannot be null");
        Objects.requireNonNull(name, "name cannot be null");

        return validations
                .zipWithIndex()
                .foldLeft(
                        Validation.valid(List.empty()),
                        (acc, t) -> {
                            Validation<T> v = Validation.narrow(t._1.at(name).atIndex(t._2));
                            return Validations.combine(acc, v).map(List::append);
                        }
                );
    }

    /**
     * Transforms a {@code Option<Validation<T>>} into a {@code Validation<Option<T>>}.
     * If the Option is empty, the resulting Validation is considered to be {@link Validation.Valid}
     */
    public static <T> Validation<Option<T>> sequence(Option<? extends Validation<? extends T>> option) {
        Objects.requireNonNull(option, "option cannot be null");
        return option.fold(
                () -> Validation.valid(Option.none()),
                validation -> validation.map(Option::of)
        );
    }

    /**
     * Transforms a {@code Optional<Validation<T>>} into a {@code Validation<Optional<T>>}.
     * If the Optional is empty, the resulting Validation is considered to be {@link Validation.Valid}
     */
    public static <T> Validation<Optional<T>> sequence(Optional<? extends Validation<? extends T>> optional) {
        Objects.requireNonNull(optional, "optional cannot be null");
        return optional.<Validation<Optional<T>>>map(validation -> validation.map(Optional::ofNullable))
                .orElseGet(() -> Validation.valid(Optional.empty()));
    }

    /**
     * Transforms a {@link java.util.Collection} of {@link Validation}s into a single {@code Validation} of a {@link java.util.List}.
     * If any validation is invalid, the result will contain all accumulated errors.
     *
     * @param validations the collection of validations to transpose.
     * @return a {@code Validation} containing a list of values if all are valid, or all errors if any are invalid.
     */
    public static <T> Validation<java.util.List<T>> sequence(java.util.Collection<? extends Validation<? extends T>> validations) {
        Objects.requireNonNull(validations, "validations cannot be null");
        return sequence(List.ofAll(validations))
                .map(List::asJava);
    }

    /**
     * Transforms a {@link java.util.Collection} of {@link Validation}s into a single {@code Validation} of a {@link java.util.List}.
     * If any validation is invalid, the result will contain all accumulated errors.
     *
     * @param validations the collection of validations to sequence.
     * @return a {@code Validation} containing a list of values if all are valid, or all errors if any are invalid.
     */
    public static <T> Validation<java.util.List<T>> sequence(java.util.Collection<? extends Validation<? extends T>> validations, String at) {
        Objects.requireNonNull(validations, "validations cannot be null");
        Objects.requireNonNull(at, "at cannot be null");
        return sequence(List.ofAll(validations), at)
                .map(List::asJava);
    }

    /**
     * Combines two validations into a builder that can map all valid values or accumulate all errors.
     */
    public static <T1, T2> CombineBuilder2<T1, T2> combine(Validation<? extends T1> v1, Validation<? extends T2> v2) {
        return new CombineBuilder2<>(v1, v2);
    }

    /**
     * Combines three validations into a builder that can map all valid values or accumulate all errors.
     */
    public static <T1, T2, T3> CombineBuilder3<T1, T2, T3> combine(Validation<? extends T1> v1, Validation<? extends T2> v2, Validation<? extends T3> v3) {
        return new CombineBuilder3<>(v1, v2, v3);
    }

    /**
     * Combines four validations into a builder that can map all valid values or accumulate all errors.
     */
    public static <T1, T2, T3, T4> CombineBuilder4<T1, T2, T3, T4> combine(Validation<? extends T1> v1, Validation<? extends T2> v2, Validation<? extends T3> v3, Validation<? extends T4> v4) {
        return new CombineBuilder4<>(v1, v2, v3, v4);
    }

    /**
     * Combines five validations into a builder that can map all valid values or accumulate all errors.
     */
    public static <T1, T2, T3, T4, T5> CombineBuilder5<T1, T2, T3, T4, T5> combine(Validation<? extends T1> v1, Validation<? extends T2> v2, Validation<? extends T3> v3, Validation<? extends T4> v4, Validation<? extends T5> v5) {
        return new CombineBuilder5<>(v1, v2, v3, v4, v5);
    }

    /**
     * Combines six validations into a builder that can map all valid values or accumulate all errors.
     */
    public static <T1, T2, T3, T4, T5, T6> CombineBuilder6<T1, T2, T3, T4, T5, T6> combine(Validation<? extends T1> v1, Validation<? extends T2> v2, Validation<? extends T3> v3, Validation<? extends T4> v4, Validation<? extends T5> v5, Validation<? extends T6> v6) {
        return new CombineBuilder6<>(v1, v2, v3, v4, v5, v6);
    }

    /**
     * Combines seven validations into a builder that can map all valid values or accumulate all errors.
     */
    public static <T1, T2, T3, T4, T5, T6, T7> CombineBuilder7<T1, T2, T3, T4, T5, T6, T7> combine(Validation<? extends T1> v1, Validation<? extends T2> v2, Validation<? extends T3> v3, Validation<? extends T4> v4, Validation<? extends T5> v5, Validation<? extends T6> v6, Validation<? extends T7> v7) {
        return new CombineBuilder7<>(v1, v2, v3, v4, v5, v6, v7);
    }

    /**
     * Combines eight validations into a builder that can map all valid values or accumulate all errors.
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8> CombineBuilder8<T1, T2, T3, T4, T5, T6, T7, T8> combine(Validation<? extends T1> v1, Validation<? extends T2> v2, Validation<? extends T3> v3, Validation<? extends T4> v4, Validation<? extends T5> v5, Validation<? extends T6> v6, Validation<? extends T7> v7, Validation<? extends T8> v8) {
        return new CombineBuilder8<>(v1, v2, v3, v4, v5, v6, v7, v8);
    }

    /**
     * Builder for combining two validations applicatively.
     */
    public record CombineBuilder2<T1, T2>(Validation<? extends T1> v1, Validation<? extends T2> v2) {
        public CombineBuilder2 {
            Objects.requireNonNull(v1, "v1 validation cannot be null");
            Objects.requireNonNull(v2, "v2 validation cannot be null");
        }

        /**
         * Applies the mapper only when all validations are valid; otherwise accumulates all errors in input order.
         */
        public <R> Validation<R> map(Function2<? super T1, ? super T2, ? extends R> mapper) {
            Objects.requireNonNull(mapper, "mapper cannot be null");
            if (v1 instanceof Validation.Valid(var t1) && v2 instanceof Validation.Valid(var t2)) {
                return Validation.valid(mapper.apply(t1, t2));
            }
            return Validation.invalid(List.of(v1.errors(), v2.errors()).flatMap(Function.identity()));
        }

        /**
         * Applies the flatMapper only when all validations are valid; otherwise accumulates all errors in input order.
         */
        public <R> Validation<R> flatMap(Function2<? super T1, ? super T2, Validation<? extends R>> flatMapper) {
            Objects.requireNonNull(flatMapper, "flatMapper cannot be null");
            if (v1 instanceof Validation.Valid(var t1) && v2 instanceof Validation.Valid(var t2)) {
                return Validation.narrow(Objects.requireNonNull(flatMapper.apply(t1, t2), "flatMapper result cannot be null"));
            }
            return Validation.invalid(List.of(v1.errors(), v2.errors()).flatMap(Function.identity()));
        }
    }

    /**
     * Builder for combining three validations applicatively.
     */
    public record CombineBuilder3<T1, T2, T3>(Validation<? extends T1> v1, Validation<? extends T2> v2, Validation<? extends T3> v3) {
        public CombineBuilder3 {
            Objects.requireNonNull(v1, "v1 validation cannot be null");
            Objects.requireNonNull(v2, "v2 validation cannot be null");
            Objects.requireNonNull(v3, "v3 validation cannot be null");
        }

        /**
         * Applies the mapper only when all validations are valid; otherwise accumulates all errors in input order.
         */
        public <R> Validation<R> map(Function3<? super T1, ? super T2, ? super T3, ? extends R> mapper) {
            Objects.requireNonNull(mapper, "mapper cannot be null");
            if (v1 instanceof Validation.Valid(var t1) && v2 instanceof Validation.Valid(var t2) && v3 instanceof Validation.Valid(var t3)) {
                return Validation.valid(mapper.apply(t1, t2, t3));
            }
            return Validation.invalid(List.of(v1.errors(), v2.errors(), v3.errors()).flatMap(Function.identity()));
        }

        /**
         * Applies the flatMapper only when all validations are valid; otherwise accumulates all errors in input order.
         */
        public <R> Validation<R> flatMap(Function3<? super T1, ? super T2, ? super T3, Validation<? extends R>> flatMapper) {
            Objects.requireNonNull(flatMapper, "flatMapper cannot be null");
            if (v1 instanceof Validation.Valid(var t1) && v2 instanceof Validation.Valid(var t2) && v3 instanceof Validation.Valid(var t3)) {
                return Validation.narrow(Objects.requireNonNull(flatMapper.apply(t1, t2, t3), "flatMapper result cannot be null"));
            }
            return Validation.invalid(List.of(v1.errors(), v2.errors(), v3.errors()).flatMap(Function.identity()));
        }
    }

    /**
     * Builder for combining four validations applicatively.
     */
    public record CombineBuilder4<T1, T2, T3, T4>(Validation<? extends T1> v1, Validation<? extends T2> v2, Validation<? extends T3> v3, Validation<? extends T4> v4) {
        public CombineBuilder4 {
            Objects.requireNonNull(v1, "v1 validation cannot be null");
            Objects.requireNonNull(v2, "v2 validation cannot be null");
            Objects.requireNonNull(v3, "v3 validation cannot be null");
            Objects.requireNonNull(v4, "v4 validation cannot be null");
        }

        /**
         * Applies the mapper only when all validations are valid; otherwise accumulates all errors in input order.
         */
        public <R> Validation<R> map(Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> mapper) {
            Objects.requireNonNull(mapper, "mapper cannot be null");
            if (v1 instanceof Validation.Valid(var t1) && v2 instanceof Validation.Valid(var t2) && v3 instanceof Validation.Valid(var t3) && v4 instanceof Validation.Valid(var t4)) {
                return Validation.valid(mapper.apply(t1, t2, t3, t4));
            }
            return Validation.invalid(List.of(v1.errors(), v2.errors(), v3.errors(), v4.errors()).flatMap(Function.identity()));
        }

        /**
         * Applies the flatMapper only when all validations are valid; otherwise accumulates all errors in input order.
         */
        public <R> Validation<R> flatMap(Function4<? super T1, ? super T2, ? super T3, ? super T4, Validation<? extends R>> flatMapper) {
            Objects.requireNonNull(flatMapper, "flatMapper cannot be null");
            if (v1 instanceof Validation.Valid(var t1) && v2 instanceof Validation.Valid(var t2) && v3 instanceof Validation.Valid(var t3) && v4 instanceof Validation.Valid(var t4)) {
                return Validation.narrow(Objects.requireNonNull(flatMapper.apply(t1, t2, t3, t4), "flatMapper result cannot be null"));
            }
            return Validation.invalid(List.of(v1.errors(), v2.errors(), v3.errors(), v4.errors()).flatMap(Function.identity()));
        }
    }

    /**
     * Builder for combining five validations applicatively.
     */
    public record CombineBuilder5<T1, T2, T3, T4, T5>(Validation<? extends T1> v1, Validation<? extends T2> v2, Validation<? extends T3> v3, Validation<? extends T4> v4, Validation<? extends T5> v5) {
        public CombineBuilder5 {
            Objects.requireNonNull(v1, "v1 validation cannot be null");
            Objects.requireNonNull(v2, "v2 validation cannot be null");
            Objects.requireNonNull(v3, "v3 validation cannot be null");
            Objects.requireNonNull(v4, "v4 validation cannot be null");
            Objects.requireNonNull(v5, "v5 validation cannot be null");
        }

        /**
         * Applies the mapper only when all validations are valid; otherwise accumulates all errors in input order.
         */
        public <R> Validation<R> map(Function5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> mapper) {
            Objects.requireNonNull(mapper, "mapper cannot be null");
            if (v1 instanceof Validation.Valid(var t1) && v2 instanceof Validation.Valid(var t2) && v3 instanceof Validation.Valid(var t3) && v4 instanceof Validation.Valid(var t4) && v5 instanceof Validation.Valid(var t5)) {
                return Validation.valid(mapper.apply(t1, t2, t3, t4, t5));
            }
            return Validation.invalid(List.of(v1.errors(), v2.errors(), v3.errors(), v4.errors(), v5.errors()).flatMap(Function.identity()));
        }

        /**
         * Applies the flatMapper only when all validations are valid; otherwise accumulates all errors in input order.
         */
        public <R> Validation<R> flatMap(Function5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, Validation<? extends R>> flatMapper) {
            Objects.requireNonNull(flatMapper, "flatMapper cannot be null");
            if (v1 instanceof Validation.Valid(var t1) && v2 instanceof Validation.Valid(var t2) && v3 instanceof Validation.Valid(var t3) && v4 instanceof Validation.Valid(var t4) && v5 instanceof Validation.Valid(var t5)) {
                return Validation.narrow(Objects.requireNonNull(flatMapper.apply(t1, t2, t3, t4, t5), "flatMapper result cannot be null"));
            }
            return Validation.invalid(List.of(v1.errors(), v2.errors(), v3.errors(), v4.errors(), v5.errors()).flatMap(Function.identity()));
        }
    }

    /**
     * Builder for combining six validations applicatively.
     */
    public record CombineBuilder6<T1, T2, T3, T4, T5, T6>(Validation<? extends T1> v1, Validation<? extends T2> v2, Validation<? extends T3> v3, Validation<? extends T4> v4, Validation<? extends T5> v5, Validation<? extends T6> v6) {
        public CombineBuilder6 {
            Objects.requireNonNull(v1, "v1 validation cannot be null");
            Objects.requireNonNull(v2, "v2 validation cannot be null");
            Objects.requireNonNull(v3, "v3 validation cannot be null");
            Objects.requireNonNull(v4, "v4 validation cannot be null");
            Objects.requireNonNull(v5, "v5 validation cannot be null");
            Objects.requireNonNull(v6, "v6 validation cannot be null");
        }

        /**
         * Applies the mapper only when all validations are valid; otherwise accumulates all errors in input order.
         */
        public <R> Validation<R> map(Function6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> mapper) {
            Objects.requireNonNull(mapper, "mapper cannot be null");
            if (v1 instanceof Validation.Valid(var t1) && v2 instanceof Validation.Valid(var t2) && v3 instanceof Validation.Valid(var t3) && v4 instanceof Validation.Valid(var t4) && v5 instanceof Validation.Valid(var t5) && v6 instanceof Validation.Valid(var t6)) {
                return Validation.valid(mapper.apply(t1, t2, t3, t4, t5, t6));
            }
            return Validation.invalid(List.of(v1.errors(), v2.errors(), v3.errors(), v4.errors(), v5.errors(), v6.errors()).flatMap(Function.identity()));
        }

        /**
         * Applies the flatMapper only when all validations are valid; otherwise accumulates all errors in input order.
         */
        public <R> Validation<R> flatMap(Function6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, Validation<? extends R>> flatMapper) {
            Objects.requireNonNull(flatMapper, "flatMapper cannot be null");
            if (v1 instanceof Validation.Valid(var t1) && v2 instanceof Validation.Valid(var t2) && v3 instanceof Validation.Valid(var t3) && v4 instanceof Validation.Valid(var t4) && v5 instanceof Validation.Valid(var t5) && v6 instanceof Validation.Valid(var t6)) {
                return Validation.narrow(Objects.requireNonNull(flatMapper.apply(t1, t2, t3, t4, t5, t6), "flatMapper result cannot be null"));
            }
            return Validation.invalid(List.of(v1.errors(), v2.errors(), v3.errors(), v4.errors(), v5.errors(), v6.errors()).flatMap(Function.identity()));
        }
    }

    /**
     * Builder for combining seven validations applicatively.
     */
    public record CombineBuilder7<T1, T2, T3, T4, T5, T6, T7>(Validation<? extends T1> v1, Validation<? extends T2> v2, Validation<? extends T3> v3, Validation<? extends T4> v4, Validation<? extends T5> v5, Validation<? extends T6> v6, Validation<? extends T7> v7) {
        public CombineBuilder7 {
            Objects.requireNonNull(v1, "v1 validation cannot be null");
            Objects.requireNonNull(v2, "v2 validation cannot be null");
            Objects.requireNonNull(v3, "v3 validation cannot be null");
            Objects.requireNonNull(v4, "v4 validation cannot be null");
            Objects.requireNonNull(v5, "v5 validation cannot be null");
            Objects.requireNonNull(v6, "v6 validation cannot be null");
            Objects.requireNonNull(v7, "v7 validation cannot be null");
        }

        /**
         * Applies the mapper only when all validations are valid; otherwise accumulates all errors in input order.
         */
        public <R> Validation<R> map(Function7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? extends R> mapper) {
            Objects.requireNonNull(mapper, "mapper cannot be null");
            if (v1 instanceof Validation.Valid(var t1) && v2 instanceof Validation.Valid(var t2) && v3 instanceof Validation.Valid(var t3) && v4 instanceof Validation.Valid(var t4) && v5 instanceof Validation.Valid(var t5) && v6 instanceof Validation.Valid(var t6) && v7 instanceof Validation.Valid(var t7)) {
                return Validation.valid(mapper.apply(t1, t2, t3, t4, t5, t6, t7));
            }
            return Validation.invalid(List.of(v1.errors(), v2.errors(), v3.errors(), v4.errors(), v5.errors(), v6.errors(), v7.errors()).flatMap(Function.identity()));
        }

        /**
         * Applies the flatMapper only when all validations are valid; otherwise accumulates all errors in input order.
         */
        public <R> Validation<R> flatMap(Function7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, Validation<? extends R>> flatMapper) {
            Objects.requireNonNull(flatMapper, "flatMapper cannot be null");
            if (v1 instanceof Validation.Valid(var t1) && v2 instanceof Validation.Valid(var t2) && v3 instanceof Validation.Valid(var t3) && v4 instanceof Validation.Valid(var t4) && v5 instanceof Validation.Valid(var t5) && v6 instanceof Validation.Valid(var t6) && v7 instanceof Validation.Valid(var t7)) {
                return Validation.narrow(Objects.requireNonNull(flatMapper.apply(t1, t2, t3, t4, t5, t6, t7), "flatMapper result cannot be null"));
            }
            return Validation.invalid(List.of(v1.errors(), v2.errors(), v3.errors(), v4.errors(), v5.errors(), v6.errors(), v7.errors()).flatMap(Function.identity()));
        }
    }

    /**
     * Builder for combining eight validations applicatively.
     */
    public record CombineBuilder8<T1, T2, T3, T4, T5, T6, T7, T8>(Validation<? extends T1> v1, Validation<? extends T2> v2, Validation<? extends T3> v3, Validation<? extends T4> v4, Validation<? extends T5> v5, Validation<? extends T6> v6, Validation<? extends T7> v7, Validation<? extends T8> v8) {
        public CombineBuilder8 {
            Objects.requireNonNull(v1, "v1 validation cannot be null");
            Objects.requireNonNull(v2, "v2 validation cannot be null");
            Objects.requireNonNull(v3, "v3 validation cannot be null");
            Objects.requireNonNull(v4, "v4 validation cannot be null");
            Objects.requireNonNull(v5, "v5 validation cannot be null");
            Objects.requireNonNull(v6, "v6 validation cannot be null");
            Objects.requireNonNull(v7, "v7 validation cannot be null");
            Objects.requireNonNull(v8, "v8 validation cannot be null");
        }

        /**
         * Applies the mapper only when all validations are valid; otherwise accumulates all errors in input order.
         */
        public <R> Validation<R> map(Function8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R> mapper) {
            Objects.requireNonNull(mapper, "mapper cannot be null");
            if (v1 instanceof Validation.Valid(var t1) && v2 instanceof Validation.Valid(var t2) && v3 instanceof Validation.Valid(var t3) && v4 instanceof Validation.Valid(var t4) && v5 instanceof Validation.Valid(var t5) && v6 instanceof Validation.Valid(var t6) && v7 instanceof Validation.Valid(var t7) && v8 instanceof Validation.Valid(var t8)) {
                return Validation.valid(mapper.apply(t1, t2, t3, t4, t5, t6, t7, t8));
            }
            return Validation.invalid(List.of(v1.errors(), v2.errors(), v3.errors(), v4.errors(), v5.errors(), v6.errors(), v7.errors(), v8.errors()).flatMap(Function.identity()));
        }

        /**
         * Applies the flatMapper only when all validations are valid; otherwise accumulates all errors in input order.
         */
        public <R> Validation<R> flatMap(Function8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, Validation<? extends R>> flatMapper) {
            Objects.requireNonNull(flatMapper, "flatMapper cannot be null");
            if (v1 instanceof Validation.Valid(var t1) && v2 instanceof Validation.Valid(var t2) && v3 instanceof Validation.Valid(var t3) && v4 instanceof Validation.Valid(var t4) && v5 instanceof Validation.Valid(var t5) && v6 instanceof Validation.Valid(var t6) && v7 instanceof Validation.Valid(var t7) && v8 instanceof Validation.Valid(var t8)) {
                return Validation.narrow(Objects.requireNonNull(flatMapper.apply(t1, t2, t3, t4, t5, t6, t7, t8), "flatMapper result cannot be null"));
            }
            return Validation.invalid(List.of(v1.errors(), v2.errors(), v3.errors(), v4.errors(), v5.errors(), v6.errors(), v7.errors(), v8.errors()).flatMap(Function.identity()));
        }
    }


}
