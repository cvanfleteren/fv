package be.iffy.fv.assertj;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Validation;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class InvalidValidationAssert<SELF extends InvalidValidationAssert<SELF, VALID, T>, VALID extends Validation.Invalid<T>, T>
        extends AbstractAssert<SELF, VALID> {

    protected InvalidValidationAssert(VALID actual) {
        super(actual, InvalidValidationAssert.class);
    }

    public ListAssert<String> errorMessages() {
        return Assertions.assertThat(actual.errors().map(ErrorMessage::message).toJavaList());
    }

    public ListAssert<String> errorKeys() {
        return Assertions.assertThat(actual.errors().map(ErrorMessage::key).toJavaList());
    }

    public ListAssert<String> formattedMessages() {
        return Assertions.assertThat(actual.errors().map(ErrorMessage::formatted).toJavaList());
    }

    public ListAssert<ErrorMessage> errors() {
        return Assertions.assertThat(actual.errors().toJavaList());
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
     * @param errorMessage the error message.
     * @param args     the error arguments.
     * @return {@code this} assertion object.
     */
    public SELF hasErrorMessage(String errorMessage, Map<String, Object> args) {
        assertThat(actual.errors()).map(ErrorMessage::message).contains(errorMessage);
        assertThat(actual.errors().filter(e -> e.message().equals(errorMessage)).head().parameters()).isEqualTo(args);
        return (SELF) this;
    }

    /**
     * Asserts that the validation contains an error with the specified key and arguments.
     *
     * @param errorMessage the error message.
     * @param args     the error arguments.
     * @return {@code this} assertion object.
     */
    public SELF hasErrorMessage(String errorMessage, java.util.Map<String, Object> args) {
        Map<String,Object> map = HashMap.ofAll(args);
        assertThat(actual.errors()).map(ErrorMessage::message).contains(errorMessage);
        assertThat(actual.errors().filter(e -> e.message().equals(errorMessage)).head().parameters()).isEqualTo(map);
        return (SELF) this;
    }

    /**
     * Asserts that the validation contains exactly the specified error messages.
     *
     * @param errorMessages the error messages.
     * @return {@code this} assertion object.
     */
    public SELF hasErrorMessages(String... errorMessages) {
        assertThat(actual.errors()).map(ErrorMessage::message).contains(errorMessages);
        return (SELF) this;
    }

    /**
     * Asserts that the validation contains exactly the specified error keys.
     *
     * @param errorKeys the error keys.
     * @return {@code this} assertion object.
     */
    public SELF hasErrorKeys(String... errorKeys) {
        assertThat(actual.errors()).map(ErrorMessage::key).contains(errorKeys);
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

    public SELF hasFormattedMessage(String errorMessage) {
        assertThat(actual.errors()).map(ErrorMessage::formatted).contains(errorMessage);
        return (SELF) this;
    }

    public SELF doesNotContainErrorMessages(String... errorMessages) {
        assertThat(actual.errors()).map(ErrorMessage::message).doesNotContain(errorMessages);
        return (SELF) this;
    }
}
