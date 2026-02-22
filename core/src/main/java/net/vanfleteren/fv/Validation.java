package net.vanfleteren.fv;

import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Function3;
import io.vavr.collection.Iterator;
import io.vavr.collection.List;

import java.util.Objects;
import java.util.function.Function;

public sealed interface Validation<T> {

    /**
     * Indicates whether the validation is successful.
     *
     * @return true if validation is successful, false otherwise.
     */
    boolean isValid();

    List<ErrorMessage> errors();

    //region common functional operations on single validations
    default <R> Validation<R> map(Function1<T, R> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return switch (this) {
            case Valid(var value) -> new Valid<>(mapper.apply(value));
            default -> (Validation<R>) this;
        };
    }

    default <R> Validation<R> flatMap(Function1<T, Validation<R>> flatMapper) {
        Objects.requireNonNull(flatMapper, "flatMapper cannot be null");
        return switch (this) {
            case Valid(var value) -> flatMapper.apply(value);
            default -> (Validation<R>) this;
        };
    }

    default <R> R fold(Function1<List<ErrorMessage>, R> whenInvalid, Function1<T, R> whenValid) {
        Objects.requireNonNull(whenInvalid, "validMapper cannot be null");
        Objects.requireNonNull(whenValid, "invalidMapper cannot be null");
        return switch (this) {
            case Valid(var value) -> whenValid.apply(value);
            case Invalid(var errors) -> whenInvalid.apply(errors);
        };
    }
    //endregion

    //regionError handling
    default Validation<T> mapErrors(Function1<List<ErrorMessage>, List<ErrorMessage>> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return switch (this) {
            case Valid<T> v -> v;
            case Invalid(var errors) -> invalid(mapper.apply(errors));
        };
    }

    /**
     * Maps error messages prepending the given name to the segments of the error message.
     *
     * @param name a logical name for the value being validated, eg the name of the field in a form/record/class.
     */
    default Validation<T> at(String name) {
        return mapErrors(errors -> errors.map(error -> error.prepend(ErrorMessage.Path.of(name))));
    }
    //endregion

    //region common functional operations on multiple validations

    /**
     * Turns a List of Validation<T> into a single Validation<List<T>>.
     * Collects all errors if any validations are invalid.
     */
    static <T> Validation<List<T>> sequence(List<Validation<T>> validations) {
        return validations
                .zipWithIndex()
                .foldLeft(
                Validation.valid(List.empty()),
                (acc, validationWithIndex) -> {
                    if (acc instanceof Valid<List<T>>(var list)) {
                        if (validationWithIndex._1 instanceof Valid<T>(var value)) {
                            return Validation.valid(list.append(value));
                        } else {
                            return Validation.invalid(validationWithIndex._1.errors().map(error -> error.atIndex(validationWithIndex._2)));
                        }
                    } else {
                        if (validationWithIndex._1 instanceof Valid<T>) {
                            return acc;
                        } else {
                            return Validation.invalid(acc.errors().appendAll(validationWithIndex._1.errors().map(error -> error.atIndex(validationWithIndex._2))));
                        }
                    }
                }
        );
    }
    //endregion

    //region mapN / flatMapN
    static <R, T1, T2> Validation<R> mapN(Validation<T1> v1, Validation<T2> v2, Function2<T1, T2, R> mapper) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(mapper, "mapper cannot be null");

        if (v1 instanceof Valid<T1>(var t1) && v2 instanceof Valid<T2>(var t2)) {
            return valid(mapper.apply(t1, t2));
        } else {
            return invalid(Iterator.of(v1.errors(), v2.errors()).flatMap(Function.identity()).toList());
        }
    }

    static <R, T1, T2> Validation<R> flatMapN(Validation<T1> v1, Validation<T2> v2, Function2<T1, T2, Validation<R>> mapper) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(mapper, "mapper cannot be null");

        if (v1 instanceof Valid<T1>(var t1) && v2 instanceof Valid<T2>(var t2)) {
            return mapper.apply(t1, t2);
        } else {
            return invalid(Iterator.of(v1.errors(), v2.errors()).flatMap(Function.identity()).toList());
        }
    }

    static <R, T1, T2, T3> Validation<R> mapN(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Function3<T1, T2, T3, R> mapper) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        Objects.requireNonNull(mapper, "mapper cannot be null");

        if (v1 instanceof Valid<T1>(var t1) && v2 instanceof Valid<T2>(var t2) && v3 instanceof Valid<T3>(var t3)) {
            return valid(mapper.apply(t1, t2, t3));
        } else {
            return invalid(List.of(v1.errors(), v2.errors(), v3.errors()).flatMap(Function.identity()));
        }
    }

    static <R, T1, T2, T3> Validation<R> flatMapN(Validation<T1> v1, Validation<T2> v2, Validation<T3> v3, Function3<T1, T2, T3, Validation<R>> mapper) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        Objects.requireNonNull(mapper, "mapper cannot be null");

        if (v1 instanceof Valid<T1>(var t1) && v2 instanceof Valid<T2>(var t2) && v3 instanceof Valid<T3>(var t3)) {
            return mapper.apply(t1, t2, t3);
        } else {
            return invalid(List.of(v1.errors(), v2.errors(), v3.errors()).flatMap(Function.identity()));
        }
    }

    //endregion

    //region factory methods

    /**
     * Creates a successful validation.
     */
    static <T> Validation<T> valid(T value) {
        return new Valid<>(value);
    }

    /**
     * Creates an invalid validation.
     */
    @SuppressWarnings("unchecked")
    static <T> Validation<T> invalid(ErrorMessage... errors) {
        return (Validation<T>) new Invalid(List.of(errors));
    }

    static <T> Validation<T> invalid(String errorMessage) {
        return (Validation<T>) new Invalid(List.of(ErrorMessage.of(errorMessage)));
    }

    /**
     * Creates an invalid validation.
     */
    @SuppressWarnings("unchecked")
    static <T> Validation<T> invalid(List<ErrorMessage> errors) {
        return (Validation<T>) new Invalid(errors);
    }
    //endregion

    //region casting

    /**
     * Narrows a {@code Validation<? extends T>} to a {@code Validation<T>}.
     *
     * @param validation The validation to narrow.
     * @param <T>        The target type.
     * @return The narrowed validation.
     */
    @SuppressWarnings("unchecked")
    static <T> Validation<T> narrow(Validation<? extends T> validation) {
        return (Validation<T>) validation;
    }

    @SuppressWarnings("unchecked")
    static <T> Validation<T> narrowSuper(Validation<? super T> validation) {
        return (Validation<T>) validation;
    }
    //endregion


    /**
     * Represents a successful validation.
     */
    record Valid<T>(T value) implements Validation<T> {
        public Valid {
            Objects.requireNonNull(value, "Value cannot be null");
        }

        @Override
        public List<ErrorMessage> errors() {
            return List.of();
        }

        @Override
        public boolean isValid() {
            return true;
        }
    }

    /**
     * Represents an invalid validation.
     *
     * @param errors The list of error messages that describe the validation failure.
     */
    record Invalid(List<ErrorMessage> errors) implements Validation<Object> {

        public Invalid {
            Objects.requireNonNull(errors, "Errors cannot be null");
        }

        @Override
        public boolean isValid() {
            return false;
        }
    }
}
