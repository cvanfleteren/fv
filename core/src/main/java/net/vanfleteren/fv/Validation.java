package net.vanfleteren.fv;

import io.vavr.Function1;
import io.vavr.collection.List;

import java.util.Objects;

public sealed interface Validation<T> {

    /**
     * Indicates whether the validation is successful.
     * @return true if validation is successful, false otherwise.
     */
    boolean isValid();

    List<ErrorMessage> errors();

    //region common functional operations on single validations
    default <R> Validation<R> map(Function1<T, R> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return switch(this) {
            case Valid(var value) -> new Valid<>(mapper.apply(value));
            default -> (Validation<R>)this;
        };
    }

    default <R> Validation<R> flatMap(Function1<T, Validation<R>> flatMapper) {
        Objects.requireNonNull(flatMapper, "flatMapper cannot be null");
        return switch(this) {
            case Valid(var value) -> flatMapper.apply(value);
            default -> (Validation<R>)this;
        };
    }

    default <R> R fold(Function1<List<ErrorMessage>, R> whenInvalid, Function1<T, R> whenValid) {
        Objects.requireNonNull(whenInvalid, "validMapper cannot be null");
        Objects.requireNonNull(whenValid, "invalidMapper cannot be null");
        return switch(this) {
            case Valid(var value) -> whenValid.apply(value);
            case Invalid(var errors) -> whenInvalid.apply(errors);
        };
    }
    //endregion

    //region common functional operations on multiple validations

    /**
     * Turns a List of Validation<T> into a single Validation<List<T>>.
     * Collects all errors if any validations are invalid.
     */
    static <T> Validation<List<T>> sequence(List<Validation<T>> validations) {
        return validations.foldLeft(
                Validation.valid(List.empty()),
                (acc, validation) -> {
                    if (acc instanceof Valid<List<T>>(var list)) {
                        if (validation instanceof Valid<T>(var value)) {
                            return Validation.valid(list.append(value));
                        } else {
                            return Validation.invalid(validation.errors());
                        }
                    } else {
                        if (validation instanceof Valid<T>) {
                            return acc;
                        } else {
                            return Validation.invalid(acc.errors().appendAll(validation.errors()));
                        }
                    }
                }
        );
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
     * @param validation The validation to narrow.
     * @param <T> The target type.
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
    record Valid<T> (T value) implements Validation<T> {
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
