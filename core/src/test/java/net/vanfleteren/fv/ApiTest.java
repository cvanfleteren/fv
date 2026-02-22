package net.vanfleteren.fv;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.API.*;
import static net.vanfleteren.fv.assertj.ValidationAssert.assertThatValidation;
import static org.assertj.core.api.Assertions.*;

import io.vavr.collection.List;

import java.math.BigDecimal;

public class ApiTest {

    record Person(String name, int age) {
    }

    Rule<Number> positive = Rule.of(n -> n.doubleValue() > 0, "must.be.positive");

    @Nested
    class ValidateAll {

        @Test
        void areAll_whenAllValid_returnsValidValidation() {
            // Arrange
            List<BigDecimal> numbers = List.of(BigDecimal.ONE, BigDecimal.TEN);

            // Act
            Validation<List<BigDecimal>> result = validateAll(numbers).areAll(positive);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(numbers);
        }

        @Test
        void areAll_whenSomeInvalid_returnsInvalidWithAccumulatedErrors() {
            // Arrange
            List<BigDecimal> numbers = List.of(BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.TEN);

            // Act
            Validation<List<BigDecimal>> result = validateAll(numbers).areAll(positive);

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("[0].must.be.positive");
        }
    }

    @Nested
    class ValidateThat {
        @Test
        public void test() {
            Rule<String> startsWithH = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "must.not.be.empty");

            Rule<String> compliant = notEmpty.and(startsWithH);


            Person p = new Person("hugh", 30);

            Validation<String> v = validateThat(p.name()).is(compliant);

            assertThatValidation(v).isValid();
        }

        @Test
        public void validationDsl_invalid() {
            Person p = new Person("john", 0);

            Validation<Integer> v = validateThat(p.age(),"age").is(positive);

            assertThatValidation(v).isInvalid().hasErrorMessage("age.must.be.positive");
        }
    }

    @Nested
    class AssertAllValid {

        @Test
        void assertAllValid_whenAllValidationsAreValid_doesNotThrow() {
            // Arrange
            Validation<String> v1 = Validation.valid("ok");
            Validation<Integer> v2 = Validation.valid(123);

            // Act & Assert
            assertThatCode(() -> API.assertAllValid(v1, v2))
                    .doesNotThrowAnyException();
        }

        @Test
        void assertAllValid_whenSomeValidationsInvalid_throwsValidationExceptionWithAllErrors() {
            // Arrange
            Validation<String> v1 = Validation.invalid("error1");
            Validation<Integer> v2 = Validation.valid(123);
            Validation<String> v3 = Validation.invalid("error2");

            // Act & Assert
            assertThatThrownBy(() -> API.assertAllValid(v1, v2, v3))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException ve = (ValidationException) ex;
                        assertThat(ve.errors())
                                .isEqualTo(List.of(ErrorMessage.of("error1"), ErrorMessage.of("error2")));
                    });
        }

        @Test
        void assertAllValid_whenNoValidationsProvided_doesNotThrow() {
            // Act & Assert
            assertThatCode(API::assertAllValid).doesNotThrowAnyException();
        }
    }
}
