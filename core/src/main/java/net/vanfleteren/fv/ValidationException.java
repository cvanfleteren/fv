package net.vanfleteren.fv;

import io.vavr.collection.List;

public class ValidationException extends RuntimeException {

    private final List<ErrorMessage> errors;

    public ValidationException(List<ErrorMessage> errors) {
        this.errors = errors;
    }

    public List<ErrorMessage> errors() {
        return errors;
    }
}
