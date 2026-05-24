package be.iffy.fv.assertj;

import io.vavr.collection.Map;
import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Validation;
import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class InvalidValidationAssert<SELF extends InvalidValidationAssert<SELF, VALID, T>, VALID extends Validation.Invalid, T>
        extends AbstractAssert<SELF, VALID> {

    protected InvalidValidationAssert(VALID actual) {
        super(actual, InvalidValidationAssert.class);
    }

    public SELF hasErrorMessage(String errorMessage) {
        assertThat(actual.errors()).map(ErrorMessage::message).contains(errorMessage);
        return (SELF) this;
    }

    public SELF hasFormattedMessage(String errorMessage) {
        assertThat(actual.errors()).map(ErrorMessage::formatted).contains(errorMessage);
        return (SELF) this;
    }

    public SELF hasErrorMessage(String errorKey, Map<String, Object> args) {
        assertThat(actual.errors()).map(ErrorMessage::key).contains(errorKey);
        assertThat(actual.errors().filter(e -> e.key().equals(errorKey)).head().parameters()).isEqualTo(args);
        return (SELF) this;
    }

    public SELF hasErrorMessages(String... errorMessages) {
        assertThat(actual.errors()).map(ErrorMessage::message).contains(errorMessages);
        return (SELF) this;
    }

    public SELF hasErrorKeys(String... errorKeys) {
        assertThat(actual.errors()).map(ErrorMessage::key).containsExactlyInAnyOrder(errorKeys);
        return (SELF) this;
    }

    public SELF doesNotContainErrorMessages(String... errorMessages) {
        assertThat(actual.errors()).map(ErrorMessage::message).doesNotContain(errorMessages);
        return (SELF) this;
    }
}
