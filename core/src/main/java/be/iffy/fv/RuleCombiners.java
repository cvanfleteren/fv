package be.iffy.fv;

import io.vavr.*;

import java.util.function.Function;

public class RuleCombiners {

    /**
     * Combines two mapping rules into a builder that can map all valid values or accumulate all errors.
     */
   public static <T, R1, R2> CombineBuilder2<T, R1, R2> combine(Function<? super T, Validation<R1>> r1, Function<? super T, Validation<R2>> r2) {
        return new CombineBuilder2<>(r1, r2);
    }

    /**
     * Combines three mapping rules into a builder that can map all valid values or accumulate all errors.
     */
   public static <T, R1, R2, R3> CombineBuilder3<T, R1, R2, R3> combine(Function<? super T, Validation<R1>> r1, Function<? super T, Validation<R2>> r2, Function<? super T, Validation<R3>> r3) {
        return new CombineBuilder3<>(r1, r2, r3);
    }

    /**
     * Combines four mapping rules into a builder that can map all valid values or accumulate all errors.
     */
   public static <T, R1, R2, R3, R4> CombineBuilder4<T, R1, R2, R3, R4> combine(Function<? super T, Validation<R1>> r1, Function<? super T, Validation<R2>> r2, Function<? super T, Validation<R3>> r3, Function<? super T, Validation<R4>> r4) {
        return new CombineBuilder4<>(r1, r2, r3, r4);
    }

    /**
     * Combines five mapping rules into a builder that can map all valid values or accumulate all errors.
     */
   public static <T, R1, R2, R3, R4, R5> CombineBuilder5<T, R1, R2, R3, R4, R5> combine(Function<? super T, Validation<R1>> r1, Function<? super T, Validation<R2>> r2, Function<? super T, Validation<R3>> r3, Function<? super T, Validation<R4>> r4, Function<? super T, Validation<R5>> r5) {
        return new CombineBuilder5<>(r1, r2, r3, r4, r5);
    }

    /**
     * Combines six mapping rules into a builder that can map all valid values or accumulate all errors.
     */
   public static <T, R1, R2, R3, R4, R5, R6> CombineBuilder6<T, R1, R2, R3, R4, R5, R6> combine(Function<? super T, Validation<R1>> r1, Function<? super T, Validation<R2>> r2, Function<? super T, Validation<R3>> r3, Function<? super T, Validation<R4>> r4, Function<? super T, Validation<R5>> r5, Function<? super T, Validation<R6>> r6) {
        return new CombineBuilder6<>(r1, r2, r3, r4, r5, r6);
    }

    /**
     * Combines seven mapping rules into a builder that can map all valid values or accumulate all errors.
     */
   public static <T, R1, R2, R3, R4, R5, R6, R7> CombineBuilder7<T, R1, R2, R3, R4, R5, R6, R7> combine(Function<? super T, Validation<R1>> r1, Function<? super T, Validation<R2>> r2, Function<? super T, Validation<R3>> r3, Function<? super T, Validation<R4>> r4, Function<? super T, Validation<R5>> r5, Function<? super T, Validation<R6>> r6, Function<? super T, Validation<R7>> r7) {
        return new CombineBuilder7<>(r1, r2, r3, r4, r5, r6, r7);
    }

    /**
     * Combines eight mapping rules into a builder that can map all valid values or accumulate all errors.
     */
   public static <T, R1, R2, R3, R4, R5, R6, R7, R8> CombineBuilder8<T, R1, R2, R3, R4, R5, R6, R7, R8> combine(Function<? super T, Validation<R1>> r1, Function<? super T, Validation<R2>> r2, Function<? super T, Validation<R3>> r3, Function<? super T, Validation<R4>> r4, Function<? super T, Validation<R5>> r5, Function<? super T, Validation<R6>> r6, Function<? super T, Validation<R7>> r7, Function<? super T, Validation<R8>> r8) {
        return new CombineBuilder8<>(r1, r2, r3, r4, r5, r6, r7, r8);
    }

    public record CombineBuilder2<T, R1, R2>(Function<? super T, Validation<R1>> r1, Function<? super T, Validation<R2>> r2) {
        public <R> MappingRule<T, R> map(Function2<? super R1, ? super R2, ? extends R> mapper) {
            return input -> Validations.combine(r1.apply(input), r2.apply(input)).map(mapper);
        }

        public <R> MappingRule<T, R> into(Function2<? super R1, ? super R2, ? extends R> mapper) {
            return map(mapper);
        }
    }

   public record CombineBuilder3<T, R1, R2, R3>(Function<? super T, Validation<R1>> r1, Function<? super T, Validation<R2>> r2, Function<? super T, Validation<R3>> r3) {
        public <R> MappingRule<T, R> map(Function3<? super R1, ? super R2, ? super R3, ? extends R> mapper) {
            return input -> Validations.combine(r1.apply(input), r2.apply(input), r3.apply(input)).map(mapper);
        }

        public <R> MappingRule<T, R> into(Function3<? super R1, ? super R2, ? super R3, ? extends R> mapper) {
            return map(mapper);
        }
    }

   public record CombineBuilder4<T, R1, R2, R3, R4>(Function<? super T, Validation<R1>> r1, Function<? super T, Validation<R2>> r2, Function<? super T, Validation<R3>> r3, Function<? super T, Validation<R4>> r4) {
        public <R> MappingRule<T, R> map(Function4<? super R1, ? super R2, ? super R3, ? super R4, ? extends R> mapper) {
            return input -> Validations.combine(r1.apply(input), r2.apply(input), r3.apply(input), r4.apply(input)).map(mapper);
        }

        public <R> MappingRule<T, R> into(Function4<? super R1, ? super R2, ? super R3, ? super R4, ? extends R> mapper) {
            return map(mapper);
        }
    }

   public record CombineBuilder5<T, R1, R2, R3, R4, R5>(Function<? super T, Validation<R1>> r1, Function<? super T, Validation<R2>> r2, Function<? super T, Validation<R3>> r3, Function<? super T, Validation<R4>> r4, Function<? super T, Validation<R5>> r5) {
        public <R> MappingRule<T, R> map(Function5<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? extends R> mapper) {
            return input -> Validations.combine(r1.apply(input), r2.apply(input), r3.apply(input), r4.apply(input), r5.apply(input)).map(mapper);
        }

        public <R> MappingRule<T, R> into(Function5<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? extends R> mapper) {
            return map(mapper);
        }
    }

   public record CombineBuilder6<T, R1, R2, R3, R4, R5, R6>(Function<? super T, Validation<R1>> r1, Function<? super T, Validation<R2>> r2, Function<? super T, Validation<R3>> r3, Function<? super T, Validation<R4>> r4, Function<? super T, Validation<R5>> r5, Function<? super T, Validation<R6>> r6) {
        public <R> MappingRule<T, R> map(Function6<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? extends R> mapper) {
            return input -> Validations.combine(r1.apply(input), r2.apply(input), r3.apply(input), r4.apply(input), r5.apply(input), r6.apply(input)).map(mapper);
        }

        public <R> MappingRule<T, R> into(Function6<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? extends R> mapper) {
            return map(mapper);
        }
    }

   public record CombineBuilder7<T, R1, R2, R3, R4, R5, R6, R7>(Function<? super T, Validation<R1>> r1, Function<? super T, Validation<R2>> r2, Function<? super T, Validation<R3>> r3, Function<? super T, Validation<R4>> r4, Function<? super T, Validation<R5>> r5, Function<? super T, Validation<R6>> r6, Function<? super T, Validation<R7>> r7) {
        public <R> MappingRule<T, R> map(Function7<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? extends R> mapper) {
            return input -> Validations.combine(r1.apply(input), r2.apply(input), r3.apply(input), r4.apply(input), r5.apply(input), r6.apply(input), r7.apply(input)).map(mapper);
        }

        public <R> MappingRule<T, R> into(Function7<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? extends R> mapper) {
            return map(mapper);
        }
    }

   public record CombineBuilder8<T, R1, R2, R3, R4, R5, R6, R7, R8>(Function<? super T, Validation<R1>> r1, Function<? super T, Validation<R2>> r2, Function<? super T, Validation<R3>> r3, Function<? super T, Validation<R4>> r4, Function<? super T, Validation<R5>> r5, Function<? super T, Validation<R6>> r6, Function<? super T, Validation<R7>> r7, Function<? super T, Validation<R8>> r8) {
        public <R> MappingRule<T, R> map(Function8<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? super R8, ? extends R> mapper) {
            return input -> Validations.combine(r1.apply(input), r2.apply(input), r3.apply(input), r4.apply(input), r5.apply(input), r6.apply(input), r7.apply(input), r8.apply(input)).map(mapper);
        }

        public <R> MappingRule<T, R> into(Function8<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? super R8, ? extends R> mapper) {
            return map(mapper);
        }
    }
}
