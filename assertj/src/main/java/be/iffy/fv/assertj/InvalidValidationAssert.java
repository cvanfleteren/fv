package be.iffy.fv.assertj;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Validation;
import io.vavr.collection.Map;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import static org.assertj.core.api.Assertions.assertThat;

public class InvalidValidationAssert<SELF extends InvalidValidationAssert<SELF, VALID, T>, VALID extends Validation.Invalid, T>
        extends AbstractAssert<SELF, VALID> {

    protected InvalidValidationAssert(VALID actual) {
        super(actual, InvalidValidationAssert.class);
    }

    /**
     * Asserts that the validation contains an error with the specified message.
     *
     * @param errorMessage the error message.
     * @return {@code this} assertion object.
     */
    public SELF hasErrorMessage(String errorMessage) {
        assertThat(actual.errors()).map(ErrorMessage::message).contains(errorMessage);
        return (SELF) this;
    }

    /**
     * Asserts that the validation contains an error witch partially matches the specified message.
     *
     * @param errorMessage the error message.
     * @return {@code this} assertion object.
     */
    public SELF hasErrorMessageContaining(String errorMessage) {
        assertThat(actual.errors()).map(ErrorMessage::message).anyMatch(e -> e.contains(errorMessage));
        return (SELF) this;
    }

    /**
     * Asserts that the validation contains an error with the specified key and arguments.
     *
     * @param errorKey the error key.
     * @param args     the error arguments.
     * @return {@code this} assertion object.
     */
    public SELF hasErrorMessage(String errorKey, Map<String, Object> args) {
        assertThat(actual.errors()).map(ErrorMessage::key).contains(errorKey);
        assertThat(actual.errors().filter(e -> e.key().equals(errorKey)).head().parameters()).isEqualTo(args);
        return (SELF) this;
    }

    /**
     * Asserts that the validation contains exactly the specified error messages.
     *
     * @param errorMessages the error messages.
     * @return {@code this} assertion object.
     */
    public SELF hasErrorMessages(String... errorMessages) {
        assertThat(actual.errors()).map(ErrorMessage::message).containsExactly(errorMessages);
        return (SELF) this;
    }

    /**
     * Asserts that the validation contains exactly the specified error keys.
     *
     * @param errorKeys the error keys.
     * @return {@code this} assertion object.
     */
    public SELF hasErrorKeys(String... errorKeys) {
        assertThat(actual.errors()).map(ErrorMessage::key).containsExactly(errorKeys);
        return (SELF) this;
    }

    /**
     * Asserts that the validation contains exactly the specified number of errors.
     *
     * @param count the expected number of errors.
     * @return this assertion object.
     */
    public SELF hasErrorCount(int count) {
        Assertions.assertThat(count).isEqualTo(actual.errors().size());
        return (SELF) this;
    }
}
