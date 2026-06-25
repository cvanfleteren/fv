package be.iffy.fv.assertj;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Validation;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static org.assertj.core.api.Assertions.assertThatCode;

class ValidationAssertTest {

    @Nested
    class ValidTests {

        @Test
        void isValid_whenValidationIsValid_shouldPass() {
            Validation<String> valid = Validation.valid("test");
            assertThatValidation(valid).isValid();
        }

        @Test
        void isValid_whenValidationIsInvalid_shouldFail() {
            Validation<String> invalid = Validation.invalid("error");
            assertThatCode(() -> assertThatValidation(invalid).isValid())
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("Expected validation to be valid but was invalid");
        }

        @Test
        void hasValue_whenValidationHasExpectedValue_shouldPass() {
            Validation<String> valid = Validation.valid("test");
            assertThatValidation(valid).isValid().isEqualTo("test");
        }

        @Test
        void hasValue_whenValidationHasDifferentValue_shouldFail() {
            Validation<String> valid = Validation.valid("test");
            assertThatCode(() -> assertThatValidation(valid).isValid().isEqualTo("wrong"))
                    .isInstanceOf(AssertionError.class);
        }
    }

    @Nested
    class InvalidTests {

        @Test
        void isInvalid_whenValidationIsInvalid_shouldPass() {
            Validation<String> invalid = Validation.invalid("error");
            assertThatValidation(invalid).isInvalid();
        }

        @Test
        void isInvalid_whenValidationIsValid_shouldFail() {
            Validation<String> valid = Validation.valid("test");
            assertThatCode(() -> assertThatValidation(valid).isInvalid())
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("Expected validation to be invalid but was valid");
        }

        @Test
        void hasErrorMessage_withString_whenContainsMessage_shouldPass() {
            Validation<String> invalid = Validation.invalid("error.key");
            assertThatValidation(invalid).isInvalid().hasErrorMessage("error.key");
        }

        @Test
        void hasErrorMessage_withKeyAndArgs_whenMatches_shouldPass() {
            Map<String, Object> args = HashMap.of("min", 5);
            Validation<String> invalid = Validation.invalid(ErrorMessage.of("too.short", args));
            
            assertThatValidation(invalid).isInvalid().hasErrorMessage("too.short", args);
        }

        @Test
        void hasErrorMessages_whenMatchesExactly_shouldPass() {
            Validation<String> invalid = Validation.invalid(
                    ErrorMessage.of("error1"),
                    ErrorMessage.of("error2")
            );
            
            assertThatValidation(invalid).isInvalid().hasErrorMessages("error1", "error2");
        }

        @Test
        void hasErrorKeys_whenMatchesExactly_shouldPass() {
            Validation<String> invalid = Validation.invalid(
                    ErrorMessage.of("key1"),
                    ErrorMessage.of("key2")
            );
            
            assertThatValidation(invalid).isInvalid().hasErrorKeys("key1", "key2");
        }
    }

    @Nested
    class StaticMethods {

        @Test
        void assertValid_whenValid_shouldPass() {
            Validation<String> valid = Validation.valid("test");
            ValidationAssert.assertValid(valid).isEqualTo("test");
        }

        @Test
        void assertInvalid_whenInvalid_shouldPass() {
            Validation<String> invalid = Validation.invalid("error");
            ValidationAssert.assertInvalid(invalid).hasErrorMessage("error");
        }

        @Test
        void assertInvalid_withSupplier_whenThrowsValidationException_shouldPass() {
            ValidationAssert.assertInvalid(() -> {
                throw new be.iffy.fv.ValidationException(List.of(ErrorMessage.of("error")));
            }).hasErrorMessage("error");
        }

        @Test
        void assertInvalid_withSupplier_whenReturnsValue_shouldFail() {
            assertThatCode(() -> ValidationAssert.assertInvalid(() -> "test"))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("Expected validation to be invalid but was valid");
        }

        @Test
        void assertInvalid_withRunnable_whenThrowsValidationException_shouldPass() {
            ValidationAssert.assertInvalid((Runnable) () -> {
                throw new be.iffy.fv.ValidationException(List.of(ErrorMessage.of("error")));
            }).hasErrorMessage("error");
        }

        @Test
        void assertInvalid_withRunnable_whenDoesNotThrow_shouldFail() {
            assertThatCode(() -> ValidationAssert.assertInvalid((Runnable) () -> {}))
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("Expected codeThrowingValidationException to throw ValidationException");
        }
    }
}
