package be.iffy.fv;

import io.vavr.*;
import io.vavr.control.Try;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import static be.iffy.fv.Validation.invalid;

public class MappingRules {

    /**
     * Combines two mapping rules into a builder that can map all valid values or accumulate all errors.
     */
   public static <T, R1, R2> CombineBuilder2<T, R1, R2> combine(MappingRule<T, R1> r1, MappingRule<T, R2> r2) {
        return new CombineBuilder2<>(r1, r2);
    }

    /**
     * Combines three mapping rules into a builder that can map all valid values or accumulate all errors.
     */
   public static <T, R1, R2, R3> CombineBuilder3<T, R1, R2, R3> combine(MappingRule<T, R1> r1, MappingRule<T, R2> r2, MappingRule<T, R3> r3) {
        return new CombineBuilder3<>(r1, r2, r3);
    }

    /**
     * Combines four mapping rules into a builder that can map all valid values or accumulate all errors.
     */
   public static <T, R1, R2, R3, R4> CombineBuilder4<T, R1, R2, R3, R4> combine(MappingRule<T, R1> r1, MappingRule<T, R2> r2, MappingRule<T, R3> r3, MappingRule<T, R4> r4) {
        return new CombineBuilder4<>(r1, r2, r3, r4);
    }

    /**
     * Combines five mapping rules into a builder that can map all valid values or accumulate all errors.
     */
   public static <T, R1, R2, R3, R4, R5> CombineBuilder5<T, R1, R2, R3, R4, R5> combine(MappingRule<T, R1> r1, MappingRule<T, R2> r2, MappingRule<T, R3> r3, MappingRule<T, R4> r4, MappingRule<T, R5> r5) {
        return new CombineBuilder5<>(r1, r2, r3, r4, r5);
    }

    /**
     * Combines six mapping rules into a builder that can map all valid values or accumulate all errors.
     */
   public static <T, R1, R2, R3, R4, R5, R6> CombineBuilder6<T, R1, R2, R3, R4, R5, R6> combine(MappingRule<T, R1> r1, MappingRule<T, R2> r2, MappingRule<T, R3> r3, MappingRule<T, R4> r4, MappingRule<T, R5> r5, MappingRule<T, R6> r6) {
        return new CombineBuilder6<>(r1, r2, r3, r4, r5, r6);
    }

    /**
     * Combines seven mapping rules into a builder that can map all valid values or accumulate all errors.
     */
   public static <T, R1, R2, R3, R4, R5, R6, R7> CombineBuilder7<T, R1, R2, R3, R4, R5, R6, R7> combine(MappingRule<T, R1> r1, MappingRule<T, R2> r2, MappingRule<T, R3> r3, MappingRule<T, R4> r4, MappingRule<T, R5> r5, MappingRule<T, R6> r6, MappingRule<T, R7> r7) {
        return new CombineBuilder7<>(r1, r2, r3, r4, r5, r6, r7);
    }

    /**
     * Combines eight mapping rules into a builder that can map all valid values or accumulate all errors.
     */
   public static <T, R1, R2, R3, R4, R5, R6, R7, R8> CombineBuilder8<T, R1, R2, R3, R4, R5, R6, R7, R8> combine(MappingRule<T, R1> r1, MappingRule<T, R2> r2, MappingRule<T, R3> r3, MappingRule<T, R4> r4, MappingRule<T, R5> r5, MappingRule<T, R6> r6, MappingRule<T, R7> r7, MappingRule<T, R8> r8) {
        return new CombineBuilder8<>(r1, r2, r3, r4, r5, r6, r7, r8);
    }

    /**
     * Creates a new MappingRule that applies the given mapper function to the input.
     * If the throwingMapper throws an exception, the rule will fail with the specified error message.
     * If the throwingMapper throws {@link ValidationException}, the rule will fail with its errors.
     */
    public static <T, R> MappingRule<T, R> catching(Function<? super T, ? extends R> throwingMapper, String errorKey) {
        return catching(throwingMapper, ErrorMessage.of(errorKey));
    }

    /**
     * Creates a new MappingRule that applies the given mapper function to the input.
     * If the throwingMapper throws an exception, the rule will fail with the specified error message.
     * If the throwingMapper throws {@link ValidationException}, the rule will fail with its errors.
     */
    public static <T, R> MappingRule<T, R> catching(Function<? super T, ? extends R> throwingMapper, ErrorMessage errorMessage) {
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        return catching(throwingMapper, (input, exception) -> errorMessage);
    }

    /**
     * Creates a new MappingRule that applies the given mapper function to the input.
     * If the throwingMapper throws an exception, the rule will fail with an {@link ErrorMessage} created by the provided maker.
     * If the throwingMapper throws {@link ValidationException}, the rule will fail with its errors.
     */
    public static <T, R> MappingRule<T, R> catching(Function<? super T, ? extends R> throwingMapper, BiFunction<? super T, Exception, ErrorMessage> errorMessageMaker) {
        Objects.requireNonNull(throwingMapper, "mapper cannot be null");
        Objects.requireNonNull(errorMessageMaker, "errorMessageMaker cannot be null");
        return input -> {
            if (input == null) {
                return Validation.Invalid.notNull();
            }
            try {
                return Validation.valid(throwingMapper.apply(input));
            } catch (ValidationException ve) {
                return invalid(ve.errors());
            } catch (Exception e) {
                return invalid(
                        Objects.requireNonNull(
                                errorMessageMaker.apply(input, e),
                                "errorMessageMaker result cannot be null"
                        )
                );
            }
        };
    }

    /**
     * Creates an explicit {@link MappingRule} from a function that has the same signature.
     * Use this to easily treat existing functions as MappingRules.
     */
    public static <T, R> MappingRule<T, R> fromValidation(Function<? super T, ? extends Validation<? extends R>> validationFunction) {
        Objects.requireNonNull(validationFunction, "validationFunction cannot be null");
        return input -> {
            if(input == null) {
                return Validation.Invalid.notNull();
            }
            return Validation.narrow(
                    Objects.requireNonNull(
                            validationFunction.apply(input),
                            "validationFunction cannot return null Validation"
                    )
            );
        };
    }

    /**
     * Creates a MappingRule from a function that returns a Try.
     * <p>
     * The tryProvider itself is invoked directly. If the tryProvider throws before returning a Try,
     * that exception is propagated. Only failures represented as Try.Failure are converted into Invalid.
     * <p>
     * If the Try fails with {@link ValidationException}, its errors are preserved, otherwise the provided error message is used.
     */
    public static <T, R> MappingRule<T, R> fromTry(Function<? super T, ? extends Try<? extends R>> tryProvider, String errorKey) {
        return fromTry(tryProvider, ErrorMessage.of(errorKey));
    }

    /**
     * Creates a MappingRule from a function that returns a Try.
     * <p>
     * The tryProvider itself is invoked directly. If the tryProvider throws before returning a Try,
     * that exception is propagated. Only failures represented as Try.Failure are converted into Invalid.
     * <p>
     * If the Try fails with {@link ValidationException}, its errors are preserved, otherwise the provided error message is used.
     */
    public static <T, R> MappingRule<T, R> fromTry(Function<? super T, ? extends Try<? extends R>> tryProvider, ErrorMessage errorMessage) {
        Objects.requireNonNull(tryProvider, "tryProvider cannot be null");
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        return input -> {
            if (input == null) {
                return Validation.Invalid.notNull();
            }
            Try<? extends R> _try = Objects.requireNonNull(tryProvider.apply(input), "tryProvider cannot return null Try");
            return _try.fold(
                    t -> {
                        if (t instanceof ValidationException ve) {
                            return invalid(ve.errors());
                        } else {
                            return invalid(errorMessage);
                        }
                    },
                    Validation::valid
            );
        };
    }

    /**
     * Returns a MappingRule that validates the input is not null.
     * <p>
     * Error key: "must.not.be.null"
     *
     * @return a MappingRule that returns valid input only if it's not null
     */
    public static <T> MappingRule<T, T> notNull() {
        return input ->
                input == null ? Validation.Invalid.notNull() : Validation.valid(input);
    }

    /**
     * Applies the specified {@link MappingRule} to the result of applying the selector function to the input. Aka <code>contramap</code>.
     *
     * @param selector a function that extracts a value of type V from an input of type T
     * @param rule     the rule to be applied to the extracted value
     * @return a new {@link MappingRule} that tests the applied selector and rule combination
     */
    public static <T, V, R> MappingRule<T, R> with(Function<? super T, ? extends V> selector, Function<? super V, ? extends Validation<? extends R>> rule) {
        Objects.requireNonNull(selector, "selector cannot be null");
        Objects.requireNonNull(rule, "rule cannot be null");
        return input -> Validation.narrow(
                Objects.requireNonNull(
                        rule.apply(selector.apply(input)),
                        "rule cannot return null Validation"
                )
        );
    }

    public record CombineBuilder2<T, R1, R2>(MappingRule<T, R1> r1, MappingRule<T, R2> r2) {
        public <R> MappingRule<T, R> map(Function2<? super R1, ? super R2, ? extends R> mapper) {
            return input -> Validations.combine(r1.test(input), r2.test(input)).map(mapper);
        }

        public <R> MappingRule<T, R> into(Function2<? super R1, ? super R2, ? extends R> mapper) {
            return map(mapper);
        }
    }

   public record CombineBuilder3<T, R1, R2, R3>(MappingRule<T, R1> r1, MappingRule<T, R2> r2, MappingRule<T, R3> r3) {
        public <R> MappingRule<T, R> map(Function3<? super R1, ? super R2, ? super R3, ? extends R> mapper) {
            return input -> Validations.combine(r1.test(input), r2.test(input), r3.test(input)).map(mapper);
        }

        public <R> MappingRule<T, R> into(Function3<? super R1, ? super R2, ? super R3, ? extends R> mapper) {
            return map(mapper);
        }
    }

   public record CombineBuilder4<T, R1, R2, R3, R4>(MappingRule<T, R1> r1, MappingRule<T, R2> r2, MappingRule<T, R3> r3, MappingRule<T, R4> r4) {
        public <R> MappingRule<T, R> map(Function4<? super R1, ? super R2, ? super R3, ? super R4, ? extends R> mapper) {
            return input -> Validations.combine(r1.test(input), r2.test(input), r3.test(input), r4.test(input)).map(mapper);
        }

        public <R> MappingRule<T, R> into(Function4<? super R1, ? super R2, ? super R3, ? super R4, ? extends R> mapper) {
            return map(mapper);
        }
    }

   public record CombineBuilder5<T, R1, R2, R3, R4, R5>(MappingRule<T, R1> r1, MappingRule<T, R2> r2, MappingRule<T, R3> r3, MappingRule<T, R4> r4, MappingRule<T, R5> r5) {
        public <R> MappingRule<T, R> map(Function5<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? extends R> mapper) {
            return input -> Validations.combine(r1.test(input), r2.test(input), r3.test(input), r4.test(input), r5.test(input)).map(mapper);
        }

        public <R> MappingRule<T, R> into(Function5<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? extends R> mapper) {
            return map(mapper);
        }
    }

   public record CombineBuilder6<T, R1, R2, R3, R4, R5, R6>(MappingRule<T, R1> r1, MappingRule<T, R2> r2, MappingRule<T, R3> r3, MappingRule<T, R4> r4, MappingRule<T, R5> r5, MappingRule<T, R6> r6) {
        public <R> MappingRule<T, R> map(Function6<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? extends R> mapper) {
            return input -> Validations.combine(r1.test(input), r2.test(input), r3.test(input), r4.test(input), r5.test(input), r6.test(input)).map(mapper);
        }

        public <R> MappingRule<T, R> into(Function6<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? extends R> mapper) {
            return map(mapper);
        }
    }

   public record CombineBuilder7<T, R1, R2, R3, R4, R5, R6, R7>(MappingRule<T, R1> r1, MappingRule<T, R2> r2, MappingRule<T, R3> r3, MappingRule<T, R4> r4, MappingRule<T, R5> r5, MappingRule<T, R6> r6, MappingRule<T, R7> r7) {
        public <R> MappingRule<T, R> map(Function7<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? extends R> mapper) {
            return input -> Validations.combine(r1.test(input), r2.test(input), r3.test(input), r4.test(input), r5.test(input), r6.test(input), r7.test(input)).map(mapper);
        }

        public <R> MappingRule<T, R> into(Function7<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? extends R> mapper) {
            return map(mapper);
        }
    }

   public record CombineBuilder8<T, R1, R2, R3, R4, R5, R6, R7, R8>(MappingRule<T, R1> r1, MappingRule<T, R2> r2, MappingRule<T, R3> r3, MappingRule<T, R4> r4, MappingRule<T, R5> r5, MappingRule<T, R6> r6, MappingRule<T, R7> r7, MappingRule<T, R8> r8) {
        public <R> MappingRule<T, R> map(Function8<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? super R8, ? extends R> mapper) {
            return input -> Validations.combine(r1.test(input), r2.test(input), r3.test(input), r4.test(input), r5.test(input), r6.test(input), r7.test(input), r8.test(input)).map(mapper);
        }

        public <R> MappingRule<T, R> into(Function8<? super R1, ? super R2, ? super R3, ? super R4, ? super R5, ? super R6, ? super R7, ? super R8, ? extends R> mapper) {
            return map(mapper);
        }
    }
}
