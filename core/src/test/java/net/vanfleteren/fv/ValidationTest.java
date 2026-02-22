package net.vanfleteren.fv;

import io.vavr.collection.List;
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
            assertThat(result.isValid()).isTrue();
            assertThat(((Validation.Valid<String>) result).value()).isEqualTo(value);
        }

        @Test
        void invalid_whenGivenErrors_returnsInvalidValidation() {
            // Arrange
            ErrorMessage error1 = ErrorMessage.of("Error 1");
            ErrorMessage error2 = ErrorMessage.of("Error 2");

            // Act
            Validation<String> result = Validation.invalid(error1, error2);

            // Assert
            assertThat(result).isInstanceOf(Validation.Invalid.class);
            assertThat(result.isValid()).isFalse();
            assertThat(((Validation.Invalid) (Object) result).errors()).containsExactly(error1, error2);
        }

        @Test
        void invalid_whenGivenNoErrors_returnsInvalidValidationWithEmptyList() {
            // Act
            Validation<String> result = Validation.invalid();

            // Assert
            assertThat(result).isInstanceOf(Validation.Invalid.class);
            assertThat(result.isValid()).isFalse();
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
            Validation<String> invalid = Validation.invalid(ErrorMessage.of("Error"));

            // Act & Assert
            assertThatCode(() -> assertThatValidation(invalid).isValid())
                    .isInstanceOf(AssertionError.class)
                    .hasMessageContaining("Expected validation to be valid but was invalid");
        }

        @Test
        void isInvalid_whenInvalid_returnsInvalidValidationAssert() {
            // Arrange
            Validation<String> invalid = Validation.invalid(ErrorMessage.of("Error Message"));

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
            ErrorMessage error = ErrorMessage.of("Error");

            // Act
            Validation<String> stringValidation = Validation.invalid(error);
            Validation<Integer> integerValidation = Validation.invalid(error);

            // Assert
            assertThat(stringValidation).isInstanceOf(Validation.Invalid.class);
            assertThat(integerValidation).isInstanceOf(Validation.Invalid.class);
        }
    }

    @Nested
    class Narrow {
        @Test
        void narrow_whenCalled_allowsAssignmentToSubtype() {
            // Arrange
            Validation<? extends Number> numberValidation = Validation.valid(123);

            // Act
            Validation<Number> narrowedValidation = Validation.narrow(numberValidation);

            // Assert
            assertThat(narrowedValidation).isSameAs(numberValidation);
            assertThatValidation(narrowedValidation)
                    .isValid()
                    .hasValue(123);
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
            ErrorMessage error = ErrorMessage.of("Error");
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
            ErrorMessage error = ErrorMessage.of("Invalid number");

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
            ErrorMessage error = ErrorMessage.of("Error");
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

    @Nested
    class Fold {

        @Test
        void fold_whenValid_callsValidMapper() {
            // Arrange
            Validation<String> valid = Validation.valid("Success");

            // Act
            String result = valid.fold(
                    errors -> "Invalid: " + errors.size(),
                    value -> "Valid: " + value
            );

            // Assert
            assertThat(result).isEqualTo("Valid: Success");
        }

        @Test
        void fold_whenInvalid_callsInvalidMapper() {
            // Arrange
            Validation<String> invalid = Validation.invalid(ErrorMessage.of("Error1"), ErrorMessage.of("Error2"));

            // Act
            String result = invalid.fold(
                    errors -> "Invalid: " + errors.size(),
                    value -> "Valid: " + value
            );

            // Assert
            assertThat(result).isEqualTo("Invalid: 2");
        }

        @Test
        void fold_whenValidMapperIsNull_throwsNullPointerException() {
            // Arrange
            Validation<String> valid = Validation.valid("Success");

            // Act & Assert
            assertThatCode(() -> valid.fold(errors -> "invalid", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("invalidMapper cannot be null");
        }

        @Test
        void fold_whenInvalidMapperIsNull_throwsNullPointerException() {
            // Arrange
            Validation<String> valid = Validation.valid("Success");

            // Act & Assert
            assertThatCode(() -> valid.fold(null, value -> "valid"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("validMapper cannot be null");
        }
    }

    @Nested
    class Sequence {

        @Test
        void sequence_whenAllValid_returnsValidValidationWithList() {
            // Arrange
            List<Validation<Integer>> validations = List.of(
                    Validation.valid(1),
                    Validation.valid(2),
                    Validation.valid(3)
            );

            // Act
            Validation<List<Integer>> result = Validation.sequence(validations);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(List.of(1, 2, 3));
        }

        @Test
        void sequence_whenOneIsInvalid_returnsInvalidValidation() {
            // Arrange
            List<Validation<Integer>> validations = List.of(
                    Validation.valid(1),
                    Validation.invalid(ErrorMessage.of("error")),
                    Validation.valid(3)
            );

            // Act
            Validation<List<Integer>> result = Validation.sequence(validations);

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("[1].error");
        }

        @Test
        void sequence_whenMultipleAreInvalid_returnsInvalidValidationWithAllErrors() {
            // Arrange
            List<Validation<Integer>> validations = List.of(
                    Validation.valid(1),
                    Validation.invalid(ErrorMessage.of("error 1")),
                    Validation.invalid(ErrorMessage.of("error 2"))
            );

            // Act
            Validation<List<Integer>> result = Validation.sequence(validations);

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("[1].error 1", "[2].error 2");
        }

        @Test
        void sequence_whenEmptyList_returnsValidValidationWithEmptyList() {
            // Arrange
            List<Validation<Integer>> validations = List.empty();

            // Act
            Validation<List<Integer>> result = Validation.sequence(validations);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(List.empty());
        }
    }

    @Nested
    class At {

        @Test
        void at_whenValid_returnsSameValidValidation() {
            // Arrange
            Validation<String> valid = Validation.valid("Success");

            // Act
            Validation<String> result = valid.at("field");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("Success");
        }

        @Test
        void at_whenInvalid_prependsPathToAllErrorMessages() {
            // Arrange
            Validation<String> invalid = Validation.invalid("must.not.be.null");

            // Act
            Validation<String> result = invalid.at("field");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("field.must.not.be.null");
        }

        @Test
        void at_whenCalledNested_prependsPathsInCorrectOrder() {
            // Arrange
            Validation<String> invalid = Validation.invalid("must.not.be.null");

            // Act
            Validation<String> result = invalid.at("nested").at("root");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("root.nested.must.not.be.null");
        }

        @Test
        void at_whenMultipleErrors_prependsPathToAll() {
            // Arrange
            Validation<String> invalid = Validation.invalid(ErrorMessage.of("error1"), ErrorMessage.of("error2"));

            // Act
            Validation<String> result = invalid.at("field");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("field.error1", "field.error2");
        }

        @Test
        void at_whenSequencing_combinesWithIndex() {
            // Arrange
            Validation<String> invalid = Validation.invalid(ErrorMessage.of("error1"), ErrorMessage.of("error2"));
            Validation<String> invalid2 = Validation.invalid(ErrorMessage.of("error1"), ErrorMessage.of("error2"));

            // Act
            Validation<List<String>> result = Validation.sequence(List.of(invalid, invalid2)).at("field");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("field[0].error1", "field[0].error2", "field[1].error1", "field[1].error2");
        }
    }

    @Nested
    class MapN {
        @Test
        void mapN_whenBothValid_returnsMappedValue() {
            // Arrange
            Validation<String> v1 = Validation.valid("hello");
            Validation<Integer> v2 = Validation.valid(5);

            // Act
            Validation<String> result = Validation.mapN(v1, v2, (s, i) -> s + i);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("hello5");
        }

        @Test
        void mapN_whenBothInvalid_returnsAllErrors() {
            // Arrange
            Validation<String> v1 = Validation.invalid("error1");
            Validation<Integer> v2 = Validation.invalid("error2");

            // Act
            Validation<String> result = Validation.mapN(v1, v2, (s, i) -> s + i);

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("error1", "error2");
        }

        @Test
        void mapN3_whenAllValid_returnsMappedValue() {
            // Arrange
            Validation<String> v1 = Validation.valid("a");
            Validation<String> v2 = Validation.valid("b");
            Validation<String> v3 = Validation.valid("c");

            // Act
            Validation<String> result = Validation.mapN(v1, v2, v3, (s1, s2, s3) -> s1 + s2 + s3);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("abc");
        }

        @Test
        void mapN3_whenAllAreInvalid_returnsAccumulatedErrors() {
            // Arrange
            Validation<String> v1 = Validation.invalid("error1");
            Validation<String> v2 = Validation.invalid("error2");
            Validation<String> v3 = Validation.invalid("error3");

            // Act
            Validation<String> result = Validation.mapN(v1, v2, v3, (s1, s2, s3) -> s1 + s2 + s3);

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("error1", "error2", "error3");
        }

        @Test
        void mapN4_whenAllValid_returnsMappedValue() {
            // Arrange
            Validation<String> v1 = Validation.valid("a");
            Validation<Integer> v2 = Validation.valid(1);
            Validation<String> v3 = Validation.valid("b");
            Validation<Integer> v4 = Validation.valid(2);

            // Act
            Validation<String> result = Validation.mapN(v1, v2, v3, v4, (s1, i1, s2, i2) -> s1 + i1 + s2 + i2);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("a1b2");
        }

        @Test
        void mapN4_whenAllAreInvalid_returnsAccumulatedErrors() {
            // Arrange
            Validation<String> v1 = Validation.invalid("error1");
            Validation<String> v2 = Validation.invalid("error2");
            Validation<String> v3 = Validation.invalid("error3");
            Validation<String> v4 = Validation.invalid("error4");

            // Act
            Validation<String> result = Validation.mapN(v1, v2, v3, v4, (s1, s2, s3, s4) -> s1 + s2 + s3 + s4);

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("error1", "error2", "error3", "error4");
        }

        @Test
        void mapN5_whenAllValid_returnsMappedValue() {
            // Arrange
            Validation<String> v1 = Validation.valid("a");
            Validation<Integer> v2 = Validation.valid(1);
            Validation<String> v3 = Validation.valid("b");
            Validation<Integer> v4 = Validation.valid(2);
            Validation<String> v5 = Validation.valid("c");

            // Act
            Validation<String> result = Validation.mapN(v1, v2, v3, v4, v5, (s1, i1, s2, i2, s3) -> s1 + i1 + s2 + i2 + s3);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("a1b2c");
        }

        @Test
        void mapN5_whenAllAreInvalid_returnsAccumulatedErrors() {
            // Arrange
            Validation<String> v1 = Validation.invalid("error1");
            Validation<String> v2 = Validation.invalid("error2");
            Validation<String> v3 = Validation.invalid("error3");
            Validation<String> v4 = Validation.invalid("error4");
            Validation<String> v5 = Validation.invalid("error5");

            // Act
            Validation<String> result = Validation.mapN(v1, v2, v3, v4, v5, (s1, s2, s3, s4, s5) -> s1 + s2 + s3 + s4 + s5);

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("error1", "error2", "error3", "error4", "error5");
        }

        @Test
        void mapN6_whenAllValid_returnsMappedValue() {
            // Arrange
            Validation<String> v1 = Validation.valid("a");
            Validation<Integer> v2 = Validation.valid(1);
            Validation<String> v3 = Validation.valid("b");
            Validation<Integer> v4 = Validation.valid(2);
            Validation<String> v5 = Validation.valid("c");
            Validation<Integer> v6 = Validation.valid(3);

            // Act
            Validation<String> result = Validation.mapN(v1, v2, v3, v4, v5, v6, (s1, i1, s2, i2, s3, i3) -> s1 + i1 + s2 + i2 + s3 + i3);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("a1b2c3");
        }

        @Test
        void mapN6_whenAllAreInvalid_returnsAccumulatedErrors() {
            // Arrange
            Validation<String> v1 = Validation.invalid("error1");
            Validation<String> v2 = Validation.invalid("error2");
            Validation<String> v3 = Validation.invalid("error3");
            Validation<String> v4 = Validation.invalid("error4");
            Validation<String> v5 = Validation.invalid("error5");
            Validation<String> v6 = Validation.invalid("error6");

            // Act
            Validation<String> result = Validation.mapN(v1, v2, v3, v4, v5, v6, (s1, s2, s3, s4, s5, s6) -> s1 + s2 + s3 + s4 + s5 + s6);

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("error1", "error2", "error3", "error4", "error5", "error6");
        }

        @Test
        void mapN7_whenAllValid_returnsMappedValue() {
            // Arrange
            Validation<String> v1 = Validation.valid("a");
            Validation<Integer> v2 = Validation.valid(1);
            Validation<String> v3 = Validation.valid("b");
            Validation<Integer> v4 = Validation.valid(2);
            Validation<String> v5 = Validation.valid("c");
            Validation<Integer> v6 = Validation.valid(3);
            Validation<String> v7 = Validation.valid("d");

            // Act
            Validation<String> result = Validation.mapN(v1, v2, v3, v4, v5, v6, v7, (s1, i1, s2, i2, s3, i3, s4) -> s1 + i1 + s2 + i2 + s3 + i3 + s4);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("a1b2c3d");
        }

        @Test
        void mapN7_whenAllAreInvalid_returnsAccumulatedErrors() {
            // Arrange
            Validation<String> v1 = Validation.invalid("error1");
            Validation<String> v2 = Validation.invalid("error2");
            Validation<String> v3 = Validation.invalid("error3");
            Validation<String> v4 = Validation.invalid("error4");
            Validation<String> v5 = Validation.invalid("error5");
            Validation<String> v6 = Validation.invalid("error6");
            Validation<String> v7 = Validation.invalid("error7");

            // Act
            Validation<String> result = Validation.mapN(v1, v2, v3, v4, v5, v6, v7, (s1, s2, s3, s4, s5, s6, s7) -> s1 + s2 + s3 + s4 + s5 + s6 + s7);

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("error1", "error2", "error3", "error4", "error5", "error6", "error7");
        }

        @Test
        void mapN8_whenAllValid_returnsMappedValue() {
            // Arrange
            Validation<String> v1 = Validation.valid("a");
            Validation<Integer> v2 = Validation.valid(1);
            Validation<String> v3 = Validation.valid("b");
            Validation<Integer> v4 = Validation.valid(2);
            Validation<String> v5 = Validation.valid("c");
            Validation<Integer> v6 = Validation.valid(3);
            Validation<String> v7 = Validation.valid("d");
            Validation<Integer> v8 = Validation.valid(4);

            // Act
            Validation<String> result = Validation.mapN(v1, v2, v3, v4, v5, v6, v7, v8, (s1, i1, s2, i2, s3, i3, s4, i4) -> s1 + i1 + s2 + i2 + s3 + i3 + s4 + i4);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("a1b2c3d4");
        }

        @Test
        void mapN8_whenAllAreInvalid_returnsAccumulatedErrors() {
            // Arrange
            Validation<String> v1 = Validation.invalid("error1");
            Validation<String> v2 = Validation.invalid("error2");
            Validation<String> v3 = Validation.invalid("error3");
            Validation<String> v4 = Validation.invalid("error4");
            Validation<String> v5 = Validation.invalid("error5");
            Validation<String> v6 = Validation.invalid("error6");
            Validation<String> v7 = Validation.invalid("error7");
            Validation<String> v8 = Validation.invalid("error8");

            // Act
            Validation<String> result = Validation.mapN(v1, v2, v3, v4, v5, v6, v7, v8, (s1, s2, s3, s4, s5, s6, s7, s8) -> s1 + s2 + s3 + s4 + s5 + s6 + s7 + s8);

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("error1", "error2", "error3", "error4", "error5", "error6", "error7", "error8");
        }
    }

    @Nested
    class FlatMapN {
        @Test
        void flatMapN_whenBothValid_returnsMappedValidation() {
            // Arrange
            Validation<String> v1 = Validation.valid("hello");
            Validation<Integer> v2 = Validation.valid(5);

            // Act
            Validation<String> result = Validation.flatMapN(v1, v2, (s, i) -> Validation.valid(s + i));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("hello5");
        }

        @Test
        void flatMapN_whenBothInvalid_returnsAllErrors() {
            // Arrange
            Validation<String> v1 = Validation.invalid("error1");
            Validation<Integer> v2 = Validation.invalid("error2");

            // Act
            Validation<String> result = Validation.flatMapN(v1, v2, (s, i) -> Validation.valid(s + i));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("error1", "error2");
        }

        @Test
        void flatMapN3_whenAllValid_returnsMappedValidation() {
            // Arrange
            Validation<String> v1 = Validation.valid("a");
            Validation<String> v2 = Validation.valid("b");
            Validation<String> v3 = Validation.valid("c");

            // Act
            Validation<String> result = Validation.flatMapN(v1, v2, v3, (s1, s2, s3) -> Validation.valid(s1 + s2 + s3));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("abc");
        }

        @Test
        void flatMapN3_whenAllAreInvalid_returnsAccumulatedErrors() {
            // Arrange
            Validation<String> v1 = Validation.invalid("error1");
            Validation<String> v2 = Validation.invalid("error2");
            Validation<String> v3 = Validation.invalid("error3");

            // Act
            Validation<String> result = Validation.flatMapN(v1, v2, v3, (s1, s2, s3) -> Validation.valid(s1 + s2 + s3));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("error1", "error2", "error3");
        }

        @Test
        void flatMapN4_whenAllValid_returnsMappedValidation() {
            // Arrange
            Validation<String> v1 = Validation.valid("a");
            Validation<Integer> v2 = Validation.valid(1);
            Validation<String> v3 = Validation.valid("b");
            Validation<Integer> v4 = Validation.valid(2);

            // Act
            Validation<String> result = Validation.flatMapN(v1, v2, v3, v4, (s1, i1, s2, i2) -> Validation.valid(s1 + i1 + s2 + i2));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("a1b2");
        }

        @Test
        void flatMapN4_whenAllAreInvalid_returnsAccumulatedErrors() {
            // Arrange
            Validation<String> v1 = Validation.invalid("error1");
            Validation<String> v2 = Validation.invalid("error2");
            Validation<String> v3 = Validation.invalid("error3");
            Validation<String> v4 = Validation.invalid("error4");

            // Act
            Validation<String> result = Validation.flatMapN(v1, v2, v3, v4, (s1, s2, s3, s4) -> Validation.valid(s1 + s2 + s3 + s4));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("error1", "error2", "error3", "error4");
        }

        @Test
        void flatMapN5_whenAllValid_returnsMappedValidation() {
            // Arrange
            Validation<String> v1 = Validation.valid("a");
            Validation<Integer> v2 = Validation.valid(1);
            Validation<String> v3 = Validation.valid("b");
            Validation<Integer> v4 = Validation.valid(2);
            Validation<String> v5 = Validation.valid("c");

            // Act
            Validation<String> result = Validation.flatMapN(v1, v2, v3, v4, v5, (s1, i1, s2, i2, s3) -> Validation.valid(s1 + i1 + s2 + i2 + s3));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("a1b2c");
        }

        @Test
        void flatMapN5_whenAllAreInvalid_returnsAccumulatedErrors() {
            // Arrange
            Validation<String> v1 = Validation.invalid("error1");
            Validation<String> v2 = Validation.invalid("error2");
            Validation<String> v3 = Validation.invalid("error3");
            Validation<String> v4 = Validation.invalid("error4");
            Validation<String> v5 = Validation.invalid("error5");

            // Act
            Validation<String> result = Validation.flatMapN(v1, v2, v3, v4, v5, (s1, s2, s3, s4, s5) -> Validation.valid(s1 + s2 + s3 + s4 + s5));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("error1", "error2", "error3", "error4", "error5");
        }

        @Test
        void flatMapN6_whenAllValid_returnsMappedValidation() {
            // Arrange
            Validation<String> v1 = Validation.valid("a");
            Validation<Integer> v2 = Validation.valid(1);
            Validation<String> v3 = Validation.valid("b");
            Validation<Integer> v4 = Validation.valid(2);
            Validation<String> v5 = Validation.valid("c");
            Validation<Integer> v6 = Validation.valid(3);

            // Act
            Validation<String> result = Validation.flatMapN(v1, v2, v3, v4, v5, v6, (s1, i1, s2, i2, s3, i3) -> Validation.valid(s1 + i1 + s2 + i2 + s3 + i3));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("a1b2c3");
        }

        @Test
        void flatMapN6_whenAllAreInvalid_returnsAccumulatedErrors() {
            // Arrange
            Validation<String> v1 = Validation.invalid("error1");
            Validation<String> v2 = Validation.invalid("error2");
            Validation<String> v3 = Validation.invalid("error3");
            Validation<String> v4 = Validation.invalid("error4");
            Validation<String> v5 = Validation.invalid("error5");
            Validation<String> v6 = Validation.invalid("error6");

            // Act
            Validation<String> result = Validation.flatMapN(v1, v2, v3, v4, v5, v6, (s1, s2, s3, s4, s5, s6) -> Validation.valid(s1 + s2 + s3 + s4 + s5 + s6));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("error1", "error2", "error3", "error4", "error5", "error6");
        }

        @Test
        void flatMapN7_whenAllValid_returnsMappedValidation() {
            // Arrange
            Validation<String> v1 = Validation.valid("a");
            Validation<Integer> v2 = Validation.valid(1);
            Validation<String> v3 = Validation.valid("b");
            Validation<Integer> v4 = Validation.valid(2);
            Validation<String> v5 = Validation.valid("c");
            Validation<Integer> v6 = Validation.valid(3);
            Validation<String> v7 = Validation.valid("d");

            // Act
            Validation<String> result = Validation.flatMapN(v1, v2, v3, v4, v5, v6, v7, (s1, i1, s2, i2, s3, i3, s4) -> Validation.valid(s1 + i1 + s2 + i2 + s3 + i3 + s4));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("a1b2c3d");
        }

        @Test
        void flatMapN7_whenAllAreInvalid_returnsAccumulatedErrors() {
            // Arrange
            Validation<String> v1 = Validation.invalid("error1");
            Validation<String> v2 = Validation.invalid("error2");
            Validation<String> v3 = Validation.invalid("error3");
            Validation<String> v4 = Validation.invalid("error4");
            Validation<String> v5 = Validation.invalid("error5");
            Validation<String> v6 = Validation.invalid("error6");
            Validation<String> v7 = Validation.invalid("error7");

            // Act
            Validation<String> result = Validation.flatMapN(v1, v2, v3, v4, v5, v6, v7, (s1, s2, s3, s4, s5, s6, s7) -> Validation.valid(s1 + s2 + s3 + s4 + s5 + s6 + s7));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("error1", "error2", "error3", "error4", "error5", "error6", "error7");
        }

        @Test
        void flatMapN8_whenAllValid_returnsMappedValidation() {
            // Arrange
            Validation<String> v1 = Validation.valid("a");
            Validation<Integer> v2 = Validation.valid(1);
            Validation<String> v3 = Validation.valid("b");
            Validation<Integer> v4 = Validation.valid(2);
            Validation<String> v5 = Validation.valid("c");
            Validation<Integer> v6 = Validation.valid(3);
            Validation<String> v7 = Validation.valid("d");
            Validation<Integer> v8 = Validation.valid(4);

            // Act
            Validation<String> result = Validation.flatMapN(v1, v2, v3, v4, v5, v6, v7, v8, (s1, i1, s2, i2, s3, i3, s4, i4) -> Validation.valid(s1 + i1 + s2 + i2 + s3 + i3 + s4 + i4));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("a1b2c3d4");
        }

        @Test
        void flatMapN8_whenAllAreInvalid_returnsAccumulatedErrors() {
            // Arrange
            Validation<String> v1 = Validation.invalid("error1");
            Validation<String> v2 = Validation.invalid("error2");
            Validation<String> v3 = Validation.invalid("error3");
            Validation<String> v4 = Validation.invalid("error4");
            Validation<String> v5 = Validation.invalid("error5");
            Validation<String> v6 = Validation.invalid("error6");
            Validation<String> v7 = Validation.invalid("error7");
            Validation<String> v8 = Validation.invalid("error8");

            // Act
            Validation<String> result = Validation.flatMapN(v1, v2, v3, v4, v5, v6, v7, v8, (s1, s2, s3, s4, s5, s6, s7, s8) -> Validation.valid(s1 + s2 + s3 + s4 + s5 + s6 + s7 + s8));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("error1", "error2", "error3", "error4", "error5", "error6", "error7", "error8");
        }
    }
}
