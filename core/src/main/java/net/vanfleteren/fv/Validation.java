package net.vanfleteren.fv;

import io.vavr.Function1;
import io.vavr.collection.List;

import java.util.Objects;

public sealed interface Validation<T> {

    /**
     * Indicates whether the validation is successful.
     * @return true if validation is successful, false otherwise.
     */
    boolean valid();


    default <R> Validation<R> map(Function1<T, R> mapper) {
        Objects.requireNonNull(mapper, "Mapper cannot be null");
        return switch(this) {
            case Valid(var value) -> new Valid<>(mapper.apply(value));
            default -> this.upcast();
        };
    }

    @SuppressWarnings("unchecked")
    default <R> Validation<R> upcast() {
        return (Validation<R>) this;
    }

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

    /**
     * Represents a successful validation.
     */
    record Valid<T> (T value) implements Validation<T> {
        public Valid {
            Objects.requireNonNull(value, "Value cannot be null");
        }

        @Override
        public boolean valid() {
            return true;
        }
    }

    /**
     * Represents an invalid validation.
     * @param errors The list of error messages that describe the validation failure.
     */
    @SuppressWarnings("unchecked")
    record Invalid(List<ErrorMessage> errors) implements Validation<Object> {

        public Invalid {
            Objects.requireNonNull(errors, "Errors cannot be null");
        }

        @Override
        public boolean valid() {
            return false;
        }
    }
}
