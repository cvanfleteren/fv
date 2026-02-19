package net.vanfleteren.fv;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.ValidationAssert.assertThatValidation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

public class ValidationTest {

    @Nested
    class FactoryMethods {

        @Test
        void valid_whenGivenValue_returnsValidValidation() {
            // Arrange
            String value = "Success";

            // Act
            Validation<String> result = Validation.valid(value);

            // Assert
            assertThat(result).isInstanceOf(Validation.Valid.class);
            assertThat(result.valid()).isTrue();
            assertThat(((Validation.Valid<String>) result).value()).isEqualTo(value);
        }

        @Test
        void invalid_whenGivenErrors_returnsInvalidValidation() {
            // Arrange
            ErrorMessage error1 = new ErrorMessage("Error 1");
            ErrorMessage error2 = new ErrorMessage("Error 2");

            // Act
            Validation<String> result = Validation.invalid(error1, error2);

            // Assert
            assertThat(result).isInstanceOf(Validation.Invalid.class);
            assertThat(result.valid()).isFalse();
            assertThat(((Validation.Invalid<String>) result).errors()).containsExactly(error1, error2);
        }

        @Test
        void invalid_whenGivenNoErrors_returnsInvalidValidationWithEmptyList() {
            // Act
            Validation<String> result = Validation.invalid();

            // Assert
            assertThat(result).isInstanceOf(Validation.Invalid.class);
            assertThat(result.valid()).isFalse();
            assertThat(((Validation.Invalid<String>) result).errors()).isEmpty();
        }

    }

    @Nested
    class Assertions {

        @Test
        void isValid_whenValid_returnsValidValidationAssert() {
            // Arrange
            Validation<String> valid = Validation.valid("Success");

            // Act & Assert
            assertThatValidation(valid)
                    .isValid()
                    .hasValue("Success");
        }

        @Test
        void isValid_whenInvalid_fails() {
            // Arrange
            Validation<String> invalid = Validation.invalid(new ErrorMessage("Error"));

            // Act & Assert
            assertThatCode(() -> assertThatValidation(invalid).isValid())
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("Expected validation to be valid but was invalid");
        }

        @Test
        void isInvalid_whenInvalid_returnsInvalidValidationAssert() {
            // Arrange
            Validation<String> invalid = Validation.invalid(new ErrorMessage("Error Message"));

            // Act & Assert
            assertThatValidation(invalid)
                    .isInvalid()
                    .hasErrorMessage("Error Message");
        }

        @Test
        void isInvalid_whenValid_fails() {
            // Arrange
            Validation<String> valid = Validation.valid("Success");

            // Act & Assert
            assertThatCode(() -> assertThatValidation(valid).isInvalid())
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("Expected validation to be invalid but was valid");
        }
    }

}
