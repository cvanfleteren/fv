package net.vanfleteren.fv;

import io.vavr.collection.List;

public interface Validation<T> {

    /**
     * Indicates whether the validation is successful.
     * @return true if validation is successful, false otherwise.
     */
    boolean valid();

    /**
     * Creates a successful validation.
     */
    static <T> Validation<T> valid(T value) {
        return new Valid<>(value);
    }

    /**
     * Creates an invalid validation.
     */
    static <T> Validation<T> invalid(ErrorMessage... errors) {
        return new Invalid<>(List.of(errors));
    }

    /**
     * Represents a successful validation.
     */
    record Valid<T> (T value) implements Validation<T> {
        @Override
        public boolean valid() {
            return true;
        }
    }

    /**
     * Represents an invalid validation.
     * @param errors The list of error messages that describe the validation failure.
     */
    record Invalid<T>(List<ErrorMessage> errors) implements Validation<T> {
        @Override
        public boolean valid() {
            return false;
        }
    }
}
