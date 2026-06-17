package be.iffy.fv;

import io.vavr.collection.List;

/**
 * Exception thrown when validation fails during mandatory validation checks.
 * It contains the list of {@link ErrorMessage} objects that caused the failure.
 */
public final class ValidationException extends RuntimeException {

    private final List<ErrorMessage> errors;

    /**
     * Creates a new {@link ValidationException} with the given list of errors.
     *
     * @param errors the list of validation errors.
     */
    public ValidationException(List<ErrorMessage> errors) {
        // we don't use the formattedMessage because that could potentially become very big
        // requireNonEmpty is evaluated before super() so the object is never partially initialized
        super(requireNonEmpty(errors).map(ErrorMessage::message).mkString(", "));
        this.errors = errors;
    }

    private static List<ErrorMessage> requireNonEmpty(List<ErrorMessage> errors) {
        if (errors.isEmpty()) {
            throw new IllegalArgumentException("Errors must be non-empty");
        }
        return errors;
    }

    public ValidationException(String errorKey) {
        super(errorKey);
        this.errors = List.of(ErrorMessage.of(errorKey));
    }

    /**
     * Returns the list of validation errors.
     *
     * @return a {@link List} of {@link ErrorMessage} objects.
     */
    public List<ErrorMessage> errors() {
        return errors;
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
