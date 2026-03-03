package net.vanfleteren.fv;

import io.vavr.Function2;
import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static net.vanfleteren.fv.assertj.ValidationAssert.assertThatValidation;
import static org.assertj.core.api.Assertions.*;

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
    class MapCatching {

        @Test
        void mapCatching_whenValidAndMapperSucceeds_returnsMappedValue() {
            // Arrange
            Validation<String> valid = Validation.valid("123");

            // Act
            Validation<Integer> result = valid.mapCatching(Integer::parseInt);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(123);
        }

        @Test
        void mapCatching_whenValidAndMapperThrows_returnsInvalidWithDefaultErrorMessage() {
            // Arrange
            Validation<String> valid = Validation.valid("abc");

            // Act
            Validation<Integer> result = valid.mapCatching(Integer::parseInt);

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("could.not.be.mapped");
        }

        @Test
        void mapCatching_whenInvalid_returnsSameInvalidInstance() {
            // Arrange
            ErrorMessage error = ErrorMessage.of("Error");
            Validation<String> invalid = Validation.invalid(error);

            // Act
            Validation<Integer> result = invalid.mapCatching(Integer::parseInt);

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("Error");
        }

        @Test
        void mapCatchingWithErrorMessage_whenValidAndMapperSucceeds_returnsMappedValue() {
            // Arrange
            Validation<String> valid = Validation.valid("123");

            // Act
            Validation<Integer> result = valid.mapCatching(Integer::parseInt, "custom.error");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(123);
        }

        @Test
        void mapCatchingWithErrorMessage_whenValidAndMapperThrows_returnsInvalidWithCustomErrorMessage() {
            // Arrange
            Validation<String> valid = Validation.valid("abc");

            // Act
            Validation<Integer> result = valid.mapCatching(Integer::parseInt, "custom.error");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("custom.error");
        }

        @Test
        void mapCatching_whenMapperIsNull_throwsNullPointerException() {
            // Arrange
            Validation<String> valid = Validation.valid("Success");

            // Act & Assert
            assertThatCode(() -> valid.mapCatching(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("mapper cannot be null");
        }

        @Test
        void mapCatching_whenErrorMessageIsNull_throwsNullPointerException() {
            // Arrange
            Validation<String> valid = Validation.valid("Success");

            // Act & Assert
            assertThatCode(() -> valid.mapCatching(Integer::parseInt, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("errorMessage cannot be null");
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
    class FlatMapCatching {

        @Test
        void flatMapCatching_whenValidAndFlatMapperReturnsValid_returnsThatValidation() {
            // Arrange
            Validation<String> valid = Validation.valid("123");

            // Act
            Validation<Integer> result = valid.flatMapCatching(s -> Validation.valid(Integer.parseInt(s)));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(123);
        }

        @Test
        void flatMapCatching_whenValidAndFlatMapperReturnsInvalid_returnsThatInvalid() {
            // Arrange
            Validation<String> valid = Validation.valid("whatever");
            ErrorMessage error = ErrorMessage.of("some.error");

            // Act
            Validation<Integer> result = valid.flatMapCatching(s -> Validation.invalid(error));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("some.error");
        }

        @Test
        void flatMapCatching_whenInvalid_returnsSameInvalidInstance() {
            // Arrange
            ErrorMessage error = ErrorMessage.of("Error");
            Validation<String> invalid = Validation.invalid(error);

            // Act
            Validation<Integer> result = invalid.flatMapCatching(s -> Validation.valid(Integer.parseInt(s)));

            // Assert
            assertThat(result).isSameAs(invalid);
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("Error");
        }

        @Test
        void flatMapCatching_whenValidAndFlatMapperThrowsRuntimeException_becomesInvalidWithDefaultErrorMessage() {
            // Arrange
            Validation<String> valid = Validation.valid("abc");

            // Act
            Validation<Integer> result = valid.flatMapCatching(s -> {
                throw new RuntimeException("boom");
            });

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("could.not.be.mapped");
        }

        @Test
        void flatMapCatching_whenValidAndFlatMapperThrowsRuntimeException_becomesInvalidWithCustomErrorMessage() {
            // Arrange
            Validation<String> valid = Validation.valid("abc");

            // Act
            Validation<Integer> result = valid.flatMapCatching(s -> {
                throw new RuntimeException("boom");
            }, "custom.error");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("custom.error");
        }

        @Test
        void flatMapCatching_whenValidAndFlatMapperThrowsValidationException_becomesInvalidWithThoseErrors() {
            // Arrange
            Validation<String> valid = Validation.valid("abc");
            ErrorMessage e1 = ErrorMessage.of("error1");
            ErrorMessage e2 = ErrorMessage.of("error2");

            // Act
            Validation<Integer> result = valid.flatMapCatching(s -> {
                throw new ValidationException(List.of(e1, e2));
            });

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("error1", "error2");
        }

        @Test
        void flatMapCatching_whenFlatMapperIsNull_throwsNullPointerException() {
            // Arrange
            Validation<String> valid = Validation.valid("Success");

            // Act & Assert
            assertThatCode(() -> valid.flatMapCatching(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("flatMapper cannot be null");
        }

        @Test
        void flatMapCatching_whenErrorMessageIsNull_throwsNullPointerException() {
            // Arrange
            Validation<String> valid = Validation.valid("Success");

            // Act & Assert
            assertThatCode(() -> valid.flatMapCatching(s -> Validation.valid(1), null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("errorMessage cannot be null");
        }
    }

    @Nested
    class Fold {

        @Test
        void fold_whenValid_callsValidMapper() {
            // Arrange
            Validation<String> valid = Validation.valid("Success");

            // Act
            CharSequence result = valid.fold(
                    errors -> "Invalid: " + errors.size(),
                    (CharSequence value) -> (CharSequence)("Valid: " + value)
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
        void mapN_whenBothValid_returnsMappedValueWithVariance() {
            // Arrange
            Validation<String> v1 = Validation.valid("hello");
            Validation<Integer> v2 = Validation.valid(5);

            Function2<Object, Number, String> mapper = (o, n) -> o.toString() + n.intValue();

            // Act
            Validation<CharSequence> result = Validation.mapN(v1, v2, mapper);

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

    @Nested
    class GetOrElse {

        @Test
        void getOrElse_whenValid_returnsValueIgnoringFallback() {
            // Arrange
            Validation<String> valid = Validation.valid("actual");

            // Act
            String result = valid.getOrElse("fallback");

            // Assert
            assertThat(result).isEqualTo("actual");
        }

        @Test
        void getOrElse_whenInvalid_returnsFallback() {
            // Arrange
            Validation<String> invalid = Validation.invalid("some.error");

            // Act
            String result = invalid.getOrElse("fallback");

            // Assert
            assertThat(result).isEqualTo("fallback");
        }

        @Test
        void getOrElse_whenFallbackIsNull_throwsNullPointerException() {
            // Arrange
            Validation<String> invalid = Validation.invalid("some.error");

            // Act & Assert
            assertThatCode(() -> invalid.getOrElse(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("fallback cannot be null");
        }
    }

    @Nested
    class GetOrElseThrow {

        @Test
        void getOrElseThrow_whenValid_returnsValue() {
            // Arrange
            Validation<String> valid = Validation.valid("actual");

            // Act
            String result = valid.getOrElseThrow();

            // Assert
            assertThat(result).isEqualTo("actual");
        }

        @Test
        void getOrElseThrow_whenInvalid_throwsValidationExceptionContainingErrors() {
            // Arrange
            ErrorMessage e1 = ErrorMessage.of("error1");
            ErrorMessage e2 = ErrorMessage.of("error2");
            Validation<String> invalid = Validation.invalid(e1, e2);

            // Act & Assert
            assertThatThrownBy(invalid::getOrElseThrow)
                    .isInstanceOf(ValidationException.class)
                    .extracting(ex -> ((ValidationException) ex).errors())
                    .isEqualTo(List.of(e1, e2));
        }
    }

    @Nested
    class FromSupplier {

        @Test
        void from_whenSupplierReturnsValue_returnsValidWithThatValue() {
            // Act
            Validation<String> result = Validation.from(() -> "expected");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("expected");
        }

        @Test
        void from_whenSupplierThrowsValidationException_returnsInvalidWithSameErrors() {
            // Arrange
            ErrorMessage e1 = ErrorMessage.of("name.too.short");
            ErrorMessage e2 = ErrorMessage.of("age.too.young");

            // Act
            Validation<Object> result = Validation.from(() -> {
                throw new ValidationException(List.of(e1, e2));
            });

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("name.too.short", "age.too.young");
        }

        @Test
        void from_whenSupplierThrowsOtherException_returnsInvalidWithGenericErrorMessage() {
            // Arrange
            RuntimeException boom = new RuntimeException("boom");

            // Act
            assertThatThrownBy(() -> Validation.from(() -> {
                throw boom;
            })).isSameAs(boom);
        }
    }

    @Nested
    class FromTry {

        private <T> Try<T> success(T value) { return Try.success(value); }
        private <T> Try<T> failure(Throwable t) { return Try.failure(t); }

        @Test
        void from_whenTrySucceeds_returnsValidValidation() {
            Try<String> tryVal = success("hello");
            Validation<String> v = Validation.from(tryVal, ErrorMessage.of("oops"));

            assertThatValidation(v)
                    .isValid()
                    .hasValue("hello");
        }

        @Test
        void from_whenTryFails_returnsInvalidValidationWithProvidedMessages() {
            Try<String> tryVal = failure(new IllegalStateException());
            ErrorMessage e1 = ErrorMessage.of("first.fault");

            Validation<Object> v = Validation.from(tryVal, e1);

            assertThatValidation(v)
                    .isInvalid()
                    .hasErrorMessages("first.fault");
        }

        @Test
        void from_whenTryFails_andNoMessages_presentedErrorListIsEmpty() {
            Try<String> tryVal = failure(new IllegalStateException("foo"));

            Validation<Object> v = Validation.from(tryVal);

            assertThatValidation(v)
                    .isInvalid()
                    .hasErrorMessages("foo");
        }
    }

    @Nested
    class FromOption {

        @Test
        void from_whenOptionIsSome_returnsValidValidation() {
            // Arrange
            Option<String> option = Option.of("hello");

            // Act
            Validation<String> result = Validation.from(option);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("hello");
        }

        @Test
        void from_whenOptionIsNone_returnsInvalidWithDefaultMessage() {
            // Arrange
            Option<String> option = Option.none();

            // Act
            Validation<String> result = Validation.from(option);

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("value.is.none");
        }

        @Test
        void fromWithErrorMessage_whenOptionIsSome_returnsValidValidation() {
            // Arrange
            Option<String> option = Option.of("hello");
            ErrorMessage error = ErrorMessage.of("custom.error");

            // Act
            Validation<String> result = Validation.from(option, error);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("hello");
        }

        @Test
        void fromWithErrorMessage_whenOptionIsNone_returnsInvalidWithCustomError() {
            // Arrange
            Option<String> option = Option.none();
            ErrorMessage error = ErrorMessage.of("custom.error");

            // Act
            Validation<String> result = Validation.from(option, error);

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("custom.error");
        }

        @Test
        void fromWithString_whenOptionIsNone_returnsInvalidWithCustomError() {
            // Arrange
            Option<String> option = Option.none();

            // Act
            Validation<String> result = Validation.from(option, "string.error");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("string.error");
        }
    }

    @Nested
    class FromEither {

        @Test
        void from_whenEitherIsRight_returnsValidValidation() {
            // Arrange
            Either<String, Integer> either = Either.right(42);

            // Act
            Validation<Integer> result = Validation.from(either, ErrorMessage::of);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(42);
        }

        @Test
        void from_whenEitherIsLeft_returnsInvalidValidationUsingMapper() {
            // Arrange
            Either<String, Integer> either = Either.left("fail");

            // Act
            Validation<Integer> result = Validation.from(either, ErrorMessage::of);

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("fail");
        }
    }

    @Nested
    class ToOptional {

        @Test
        void toOptional_whenValid_returnsOptionalWithValue() {
            // Arrange
            Validation<String> valid = Validation.valid("hello");

            // Act
            Optional<String> result = valid.toOptional();

            // Assert
            assertThat(result).contains("hello");
        }

        @Test
        void toOptional_whenInvalid_returnsEmptyOptional() {
            // Arrange
            Validation<String> invalid = Validation.invalid("error");

            // Act
            Optional<String> result = invalid.toOptional();

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class JavaErrors {

        @Test
        void javaErrors_whenValid_returnsEmptyList() {
            // Arrange
            Validation<String> valid = Validation.valid("hello");

            // Act
            java.util.List<ErrorMessage> result = valid.javaErrors();

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        void javaErrors_whenInvalid_returnsListOfErrors() {
            // Arrange
            ErrorMessage e1 = ErrorMessage.of("error1");
            ErrorMessage e2 = ErrorMessage.of("error2");
            Validation<String> invalid = Validation.invalid(e1, e2);

            // Act
            java.util.List<ErrorMessage> result = invalid.javaErrors();

            // Assert
            assertThat(result).containsExactly(e1, e2);
        }
    }

    @Nested
    class FromOptional {

        @Test
        void from_whenOptionalIsPresent_returnsValidValidation() {
            // Arrange
            Optional<String> optional = Optional.of("hello");

            // Act
            Validation<String> result = Validation.from(optional);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("hello");
        }

        @Test
        void from_whenOptionalIsEmpty_returnsInvalidWithDefaultMessage() {
            // Arrange
            Optional<String> optional = Optional.empty();

            // Act
            Validation<String> result = Validation.from(optional);

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("value.is.none");
        }

        @Test
        void fromWithErrorMessage_whenOptionalIsPresent_returnsValidValidation() {
            // Arrange
            Optional<String> optional = Optional.of("hello");
            ErrorMessage error = ErrorMessage.of("custom.error");

            // Act
            Validation<String> result = Validation.from(optional, error);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("hello");
        }

        @Test
        void fromWithErrorMessage_whenOptionalIsEmpty_returnsInvalidWithCustomError() {
            // Arrange
            Optional<String> optional = Optional.empty();
            ErrorMessage error = ErrorMessage.of("custom.error");

            // Act
            Validation<String> result = Validation.from(optional, error);

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("custom.error");
        }

        @Test
        void fromWithString_whenOptionalIsEmpty_returnsInvalidWithCustomError() {
            // Arrange
            Optional<String> optional = Optional.empty();

            // Act
            Validation<String> result = Validation.from(optional, "string.error");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("string.error");
        }
    }

    @Nested
    class SequenceJavaCollection {

        @Test
        void sequence_whenAllAreValid_returnsValidWithListOfValues() {
            // Arrange
            java.util.List<Validation<String>> validations = java.util.List.of(
                    Validation.valid("a"),
                    Validation.valid("b")
            );

            // Act
            Validation<java.util.List<String>> result = Validation.sequence(validations);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(java.util.List.of("a", "b"));
        }

        @Test
        void sequence_whenSomeAreInvalid_returnsInvalidWithAllErrors() {
            // Arrange
            java.util.List<Validation<String>> validations = java.util.List.of(
                    Validation.valid("a"),
                    Validation.invalid("error1"),
                    Validation.invalid("error2")
            );

            // Act
            Validation<java.util.List<String>> result = Validation.sequence(validations);

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("[1].error1", "[2].error2");
        }
    }
}
