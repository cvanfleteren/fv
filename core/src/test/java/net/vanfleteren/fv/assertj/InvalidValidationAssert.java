package net.vanfleteren.fv.assertj;

import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Validation;
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

    public SELF hasErrorMessages(String... errorMessages) {
        assertThat(actual.errors()).map(ErrorMessage::message).containsExactly(errorMessages);
        return (SELF) this;
    }
}
