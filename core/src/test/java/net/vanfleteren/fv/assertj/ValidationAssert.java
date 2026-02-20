package net.vanfleteren.fv.assertj;

import net.vanfleteren.fv.Validation;
import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidationAssert<SELF extends ValidationAssert<SELF, VALIDATION, T>, VALIDATION extends Validation<T>, T>
        extends AbstractAssert<SELF, VALIDATION> {

    protected ValidationAssert(VALIDATION actual, Class<?> selfType) {
        super(actual, selfType);
    }

    public ValidValidationAssert<?, Validation.Valid<T>, T> isValid() {
        assertThat(actual.isValid()).as("Expected validation to be valid but was invalid").isTrue();
        return new ValidValidationAssert<>((Validation.Valid<T>) actual);
    }

    @SuppressWarnings("unchecked")
    public InvalidValidationAssert<?, Validation.Invalid, T> isInvalid() {
        assertThat(actual.isValid()).as("Expected validation to be invalid but was valid").isFalse();
        return new InvalidValidationAssert<>((Validation.Invalid) actual);
    }

    public static <T> ValidationAssert<?, Validation<T>, T> assertThatValidation(Validation<T> actual) {
        return new ValidationAssert<>(actual, ValidationAssert.class);
    }
}