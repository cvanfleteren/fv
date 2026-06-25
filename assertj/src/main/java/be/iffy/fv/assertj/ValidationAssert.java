package be.iffy.fv.assertj;

import be.iffy.fv.Validation;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ObjectAssert;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidationAssert<SELF extends ValidationAssert<SELF, VALIDATION, T>, VALIDATION extends Validation<T>, T>
        extends AbstractAssert<SELF, VALIDATION> {

    protected ValidationAssert(VALIDATION actual, Class<?> selfType) {
        super(actual, selfType);
    }

    /**
     * Asserts that the validation is valid.
     *
     * @return a {@link ObjectAssert} for further assertions.
     */
    public ObjectAssert<T> isValid() {
        assertThat(actual.isValid()).as("Expected validation to be valid but was invalid").isTrue();
        return new ObjectAssert<>(actual.getOrElseThrow());
    }

    /**
     * Asserts that the validation is invalid.
     *
     * @return an {@link InvalidValidationAssert} for further assertions.
     */
    public InvalidValidationAssert<?, Validation.Invalid<T>, T> isInvalid() {
        assertThat(actual.isValid()).as("Expected validation to be invalid but was valid").isFalse();
        return new InvalidValidationAssert<>((Validation.Invalid) actual);
    }

    /**
     * Creates a new instance of {@link ValidationAssert}.
     *
     * @param actual the actual value.

     * @return the created assertion object.
     */
    public static <T> ValidationAssert<?, Validation<T>, T> assertThatValidation(Validation<T> actual) {
        return new ValidationAssert<>(actual, ValidationAssert.class);
    }

    /**
     * Asserts that the validation is valid.
     *
     * @param actual the actual value.

     * @return a {@link ObjectAssert} with the value inside the Validation for further assertions.
     */
    public static <T> ObjectAssert<T> assertValid(Validation<T> actual) {
        assertThat(actual.isValid()).as("Expected validation to be valid but was invalid").isTrue();
        return new ObjectAssert<>(actual.getOrElseThrow());
    }

    /**
     * Asserts that the validation is invalid.
     *
     * @param actual the actual value.

     * @return an {@link InvalidValidationAssert} for further assertions.
     */
    public static <T> InvalidValidationAssert<?, Validation.Invalid<T>, T> assertInvalid(Validation<T> actual) {
        assertThat(actual.isValid()).as("Expected validation to be invalid but was valid").isFalse();
        return new InvalidValidationAssert<>((Validation.Invalid<T>) actual);
    }

    /**
     * Asserts that code passed will throw a ValidationException.
     */
    public static <T> InvalidValidationAssert<?, Validation.Invalid<T>, T> assertInvalid(Supplier<T> codeThrowingValidationException) {
        Validation<T> validation = Validation.from().catching(codeThrowingValidationException);
        assertThat(validation.isInvalid()).as("Expected validation to be invalid but was valid").isTrue();
        return new InvalidValidationAssert<>((Validation.Invalid<T>) validation);
    }

    /**
     * Asserts that code passed will throw a ValidationException.
     */
    public static InvalidValidationAssert<?, Validation.Invalid<Object>, Object> assertInvalid(Runnable codeThrowingValidationException) {
        Validation<?> validation = Validation.from().catching(() -> {
            codeThrowingValidationException.run();
            Assertions.fail("Expected codeThrowingValidationException to throw ValidationException");
            return null;
        });
        return new InvalidValidationAssert<>((Validation.Invalid<Object>) validation);
    }

}
