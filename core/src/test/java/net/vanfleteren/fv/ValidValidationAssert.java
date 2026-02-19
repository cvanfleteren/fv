package net.vanfleteren.fv;

import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidValidationAssert<SELF extends ValidValidationAssert<SELF, VALID, T>, VALID extends Validation.Valid<T>, T>
        extends AbstractAssert<SELF, VALID> {

    protected ValidValidationAssert(VALID actual) {
        super(actual, ValidValidationAssert.class);
    }

    public SELF hasValue(T expectedValue) {
        assertThat(actual.value()).isEqualTo(expectedValue);
        return (SELF) this;
    }
}
