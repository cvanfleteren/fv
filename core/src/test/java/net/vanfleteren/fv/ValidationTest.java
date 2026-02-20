package net.vanfleteren.fv;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.assertj.ValidationAssert.assertThatValidation;
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
            assertThat(((Validation.Invalid) (Object) result).errors()).containsExactly(error1, error2);
        }

        @Test
        void invalid_whenGivenNoErrors_returnsInvalidValidationWithEmptyList() {
            // Act
            Validation<String> result = Validation.invalid();

            // Assert
            assertThat(result).isInstanceOf(Validation.Invalid.class);
            assertThat(result.valid()).isFalse();
            assertThat(((Validation.Invalid) (Object) result).errors()).isEmpty();
        }

        @Test
        void valid_whenGivenNull_throwsNullPointerException() {
            // Act & Assert
            assertThatCode(() -> Validation.valid(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Value cannot be null");
        }

        @Test
        void invalid_whenGivenNull_throwsNullPointerException() {
            // Act & Assert
            assertThatCode(() -> new Validation.Invalid(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("Errors cannot be null");
        }

    }

    @Nested
    class Assertions {
        //test against the Assertion helpers, getting meta :)

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

        @Test
        void invalid_canBeAssignedToDifferentTypes() {
            // Arrange
            ErrorMessage error = new ErrorMessage("Error");

            // Act
            Validation<String> stringValidation = Validation.invalid(error);
            Validation<Integer> integerValidation = Validation.invalid(error);

            // Assert
            assertThat(stringValidation).isInstanceOf(Validation.Invalid.class);
            assertThat(integerValidation).isInstanceOf(Validation.Invalid.class);
        }
    }

    @Nested
    class Upcast {
        @Test
        void upcast_whenCalled_allowsAssignmentToSuperType() {
            // Arrange
            Validation<String> stringValidation = Validation.valid("Success");

            // Act
            Validation<Object> objectValidation = stringValidation.upcast();

            // Assert
            assertThat(objectValidation).isSameAs(stringValidation);
            assertThat(objectValidation.valid()).isTrue();
        }
    }

    @Nested
    class Map {

        @Test
        void map_whenValid_returnsMappedValue() {
            // Arrange
            Validation<String> valid = Validation.valid("123");

            // Act
            Validation<Integer> result = valid.map(Integer::parseInt);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(123);
        }

        @Test
        void map_whenInvalid_returnsSameInvalidInstance() {
            // Arrange
            ErrorMessage error = new ErrorMessage("Error");
            Validation<String> invalid = Validation.invalid(error);

            // Act
            Validation<Integer> result = invalid.map(Integer::parseInt);

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("Error");
        }

        @Test
        void map_whenMapperIsNull_throwsNullPointerException() {
            // Arrange
            Validation<String> valid = Validation.valid("Success");

            // Act & Assert
            assertThatCode(() -> valid.map(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("mapper cannot be null");
        }
    }

    @Nested
    class FlatMap {

        @Test
        void flatMap_whenValidAndMapperReturnsValid_returnsValidValidation() {
            // Arrange
            Validation<String> valid = Validation.valid("123");

            // Act
            Validation<Integer> result = valid.flatMap(s -> Validation.valid(Integer.parseInt(s)));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(123);
        }

        @Test
        void flatMap_whenValidAndMapperReturnsInvalid_returnsInvalidValidation() {
            // Arrange
            Validation<String> valid = Validation.valid("abc");
            ErrorMessage error = new ErrorMessage("Invalid number");

            // Act
            Validation<Integer> result = valid.flatMap(s -> Validation.invalid(error));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("Invalid number");
        }

        @Test
        void flatMap_whenInvalid_returnsSameInvalidInstance() {
            // Arrange
            ErrorMessage error = new ErrorMessage("Error");
            Validation<String> invalid = Validation.invalid(error);

            // Act
            Validation<Integer> result = invalid.flatMap(s -> Validation.valid(Integer.parseInt(s)));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("Error");
        }

        @Test
        void flatMap_whenFlatMapperIsNull_throwsNullPointerException() {
            // Arrange
            Validation<String> valid = Validation.valid("Success");

            // Act & Assert
            assertThatCode(() -> valid.flatMap(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("flatMapper cannot be null");
        }
    }
}
