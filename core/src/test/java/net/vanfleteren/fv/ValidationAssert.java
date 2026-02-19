package net.vanfleteren.fv;

import io.vavr.control.Try;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.ObjectAssert;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidationAssert<SELF extends ValidationAssert<SELF, VALIDATION, T>, VALIDATION extends Validation<T>, T>
        extends AbstractAssert<SELF, VALIDATION> {

    protected ValidationAssert(VALIDATION actual, Class<?> selfType) {
        super(actual, selfType);
    }

    public ValidValidationAssert<?, Validation.Valid<T>, T> isValid() {
        assertThat(actual.valid()).as("Expected validation to be valid but was invalid").isTrue();
        return new ValidValidationAssert<>((Validation.Valid<T>) actual);
    }

    public InvalidValidationAssert<?, Validation.Invalid<T>, T> isInvalid() {
        assertThat(actual.valid()).as("Expected validation to be invalid but was valid").isFalse();
        return new InvalidValidationAssert<>((Validation.Invalid<T>) actual);
    }

    public static <T> ValidationAssert<?, Validation<T>, T> assertThatValidation(Validation<T> actual) {
        return new ValidationAssert<>(actual, ValidationAssert.class);
    }
}