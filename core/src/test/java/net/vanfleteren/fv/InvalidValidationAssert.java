package net.vanfleteren.fv;

import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class InvalidValidationAssert<SELF extends InvalidValidationAssert<SELF, VALID, T>, VALID extends Validation.Invalid<T>, T>
        extends AbstractAssert<SELF, VALID> {

    protected InvalidValidationAssert(VALID actual) {
        super(actual, InvalidValidationAssert.class);
    }

    public SELF hasErrorMessage(String errorMessage) {
        assertThat(actual.errors()).map(ErrorMessage::message).contains(errorMessage);
        return (SELF) this;
    }
}
