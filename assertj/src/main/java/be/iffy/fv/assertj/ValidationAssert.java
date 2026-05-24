package be.iffy.fv.assertj;

import be.iffy.fv.Validation;
import org.assertj.core.api.AbstractAssert;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidationAssert<SELF extends ValidationAssert<SELF, VALIDATION, T>, VALIDATION extends Validation<T>, T>
        extends AbstractAssert<SELF, VALIDATION> {

    protected ValidationAssert(VALIDATION actual, Class<?> selfType) {
        super(actual, selfType);
    }

    /**
     * Asserts that the validation is valid.
     *
     * @return a {@link ValidValidationAssert} for further assertions.
     */
    public ValidValidationAssert<?, Validation.Valid<T>, T> isValid() {
        assertThat(actual.isValid()).as("Expected validation to be valid but was invalid").isTrue();
        return new ValidValidationAssert<>((Validation.Valid<T>) actual);
    }

    /**
     * Asserts that the validation is invalid.
     *
     * @return an {@link InvalidValidationAssert} for further assertions.
     */
    @SuppressWarnings("unchecked")
    public InvalidValidationAssert<?, Validation.Invalid, T> isInvalid() {
        assertThat(actual.isValid()).as("Expected validation to be invalid but was valid").isFalse();
        return new InvalidValidationAssert<>((Validation.Invalid) actual);
    }

    /**
     * Creates a new instance of {@link ValidationAssert}.
     *
     * @param actual the actual value.
     * @param <T> the type of the value.
     * @return the created assertion object.
     */
    public static <T> ValidationAssert<?, Validation<T>, T> assertThatValidation(Validation<T> actual) {
        return new ValidationAssert<>(actual, ValidationAssert.class);
    }

    /**
     * Asserts that the validation is valid.
     *
     * @param actual the actual value.
     * @param <T> the type of the value.
     * @return a {@link ValidValidationAssert} for further assertions.
     */
    public static <T> ValidValidationAssert<?, Validation.Valid<T>, T> assertValid(Validation<T> actual) {
        assertThat(actual.isValid()).as("Expected validation to be valid but was invalid").isTrue();
        return new ValidValidationAssert<>((Validation.Valid<T>) actual);
    }

    /**
     * Asserts that the validation is invalid.
     *
     * @param actual the actual value.
     * @param <T> the type of the value.
     * @return an {@link InvalidValidationAssert} for further assertions.
     */
    public static <T> InvalidValidationAssert<?, Validation.Invalid, T> assertInvalid(Validation<T> actual) {
        assertThat(actual.isValid()).as("Expected validation to be invalid but was valid").isFalse();
        return new InvalidValidationAssert<>((Validation.Invalid) actual);
    }

}