package be.iffy.fv.dsl;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import be.iffy.fv.ValidationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static be.iffy.fv.dsl.DSL.*;
import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static org.assertj.core.api.Assertions.*;

import io.vavr.collection.List;

import java.math.BigDecimal;

public class DSLTest {

    record Person(String name, int age) {
    }

    Rule<Number> positive = Rule.of(n -> n.doubleValue() > 0, "must.be.positive");
    Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "must.not.be.empty");

    @Nested
    class ValidateAll {

        @Test
        void areAll_whenAllValid_returnsValidValidation() {
            // Arrange
            List<BigDecimal> numbers = List.of(BigDecimal.ONE, BigDecimal.TEN);

            // Act
            var result = validateAll(numbers).areAll(positive);

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
            var result = validateAll(numbers).areAll(positive);

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
            Rule<String> compliant = notEmpty.and(startsWithH);


            Person p = new Person("hugh", 30);

            Validation<String> v = validateThat(p.name()).is(compliant);

            assertThatValidation(v).isValid();
        }

        @Test
        public void map_whenUsed_passedMappedValueToValidation() {
            Rule<String> startsWithH = Rule.of(s -> s.startsWith("h"), "must.start.with.h");


            Rule<String> compliant = notEmpty.and(startsWithH);


            Person p = new Person("  hugh", 30);

            Validation<String> v = validateThat(p.name()).map(String::trim).is(compliant);

            assertThatValidation(v).isValid().hasValue("hugh");
        }

        @Test
        public void map_whenMultipleMapsAreChained_appliesAllMappers() {
            // Arrange
            var value = "  123  ";

            // Act
            var result = validateThat(value)
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .map(i -> i * 2)
                    .is(Rule.of(i -> i == 246, "must.be.246"));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(246);
        }

        @Test
        public void map_whenMapFails_becomesInvalidWithErrorMessage() {
            // Arrange
            var value = "  abc  ";

            // Act
            var result = validateThat(value)
                    .map(Integer::parseInt)
                    .map(i -> i * 2)
                    .is(Rule.of(i -> i == 246, "must.be.246"));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("could.not.be.mapped");
        }

        @Test
        public void map_whenMappingToDifferentType_worksCorrectly() {
            // Arrange
            var value = "123";

            // Act
            var result = validateThat(value)
                    .map(Integer::parseInt)
                    .is(Rule.of(i -> i > 100, "must.be.greater.than.100"));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(123);
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
            assertThatCode(() -> DSL.assertAllValid(v1, v2))
                    .doesNotThrowAnyException();
        }

        @Test
        void assertAllValid_whenTwoValidationsAreValid_returnsTuple2() {
            // Arrange
            Validation<String> v1 = Validation.valid("ok");
            Validation<Integer> v2 = Validation.valid(123);

            // Act
            var result = DSL.assertAllValid(v1, v2);

            // Assert
            assertThat(result).isEqualTo(io.vavr.Tuple.of("ok", 123));
        }

        @Test
        void assertAllValid_whenThreeValidationsAreValid_returnsTuple3() {
            // Arrange
            Validation<String> v1 = Validation.valid("ok");
            Validation<Integer> v2 = Validation.valid(123);
            Validation<Double> v3 = Validation.valid(1.0);

            // Act
            var result = DSL.assertAllValid(v1, v2, v3);

            // Assert
            assertThat(result).isEqualTo(io.vavr.Tuple.of("ok", 123, 1.0));
        }

        @Test
        void assertAllValid_whenFourValidationsAreValid_returnsTuple4() {
            // Arrange
            Validation<String> v1 = Validation.valid("v1");
            Validation<String> v2 = Validation.valid("v2");
            Validation<String> v3 = Validation.valid("v3");
            Validation<String> v4 = Validation.valid("v4");

            // Act
            var result = DSL.assertAllValid(v1, v2, v3, v4);

            // Assert
            assertThat(result).isEqualTo(io.vavr.Tuple.of("v1", "v2", "v3", "v4"));
        }

        @Test
        void assertAllValid_whenFiveValidationsAreValid_returnsTuple5() {
            // Arrange
            Validation<String> v1 = Validation.valid("v1");
            Validation<String> v2 = Validation.valid("v2");
            Validation<String> v3 = Validation.valid("v3");
            Validation<String> v4 = Validation.valid("v4");
            Validation<String> v5 = Validation.valid("v5");

            // Act
            var result = DSL.assertAllValid(v1, v2, v3, v4, v5);

            // Assert
            assertThat(result).isEqualTo(io.vavr.Tuple.of("v1", "v2", "v3", "v4", "v5"));
        }

        @Test
        void assertAllValid_whenSixValidationsAreValid_returnsTuple6() {
            // Arrange
            Validation<String> v1 = Validation.valid("v1");
            Validation<String> v2 = Validation.valid("v2");
            Validation<String> v3 = Validation.valid("v3");
            Validation<String> v4 = Validation.valid("v4");
            Validation<String> v5 = Validation.valid("v5");
            Validation<String> v6 = Validation.valid("v6");

            // Act
            var result = DSL.assertAllValid(v1, v2, v3, v4, v5, v6);

            // Assert
            assertThat(result).isEqualTo(io.vavr.Tuple.of("v1", "v2", "v3", "v4", "v5", "v6"));
        }

        @Test
        void assertAllValid_whenSevenValidationsAreValid_returnsTuple7() {
            // Arrange
            Validation<String> v1 = Validation.valid("v1");
            Validation<String> v2 = Validation.valid("v2");
            Validation<String> v3 = Validation.valid("v3");
            Validation<String> v4 = Validation.valid("v4");
            Validation<String> v5 = Validation.valid("v5");
            Validation<String> v6 = Validation.valid("v6");
            Validation<String> v7 = Validation.valid("v7");

            // Act
            var result = DSL.assertAllValid(v1, v2, v3, v4, v5, v6, v7);

            // Assert
            assertThat(result).isEqualTo(io.vavr.Tuple.of("v1", "v2", "v3", "v4", "v5", "v6", "v7"));
        }

        @Test
        void assertAllValid_whenEightValidationsAreValid_returnsTuple8() {
            // Arrange
            Validation<String> v1 = Validation.valid("v1");
            Validation<String> v2 = Validation.valid("v2");
            Validation<String> v3 = Validation.valid("v3");
            Validation<String> v4 = Validation.valid("v4");
            Validation<String> v5 = Validation.valid("v5");
            Validation<String> v6 = Validation.valid("v6");
            Validation<String> v7 = Validation.valid("v7");
            Validation<String> v8 = Validation.valid("v8");

            // Act
            var result = DSL.assertAllValid(v1, v2, v3, v4, v5, v6, v7, v8);

            // Assert
            assertThat(result).isEqualTo(io.vavr.Tuple.of("v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8"));
        }

        @Test
        void assertAllValid_whenSomeValidationsInvalid_throwsValidationExceptionWithAllErrors() {
            // Arrange
            Validation<String> v1 = Validation.invalid("error1");
            Validation<Integer> v2 = Validation.valid(123);
            Validation<String> v3 = Validation.invalid("error2");

            // Act & Assert
            assertThatThrownBy(() -> DSL.assertAllValid(v1, v2, v3))
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
            assertThatCode(DSL::assertAllValid).doesNotThrowAnyException();
        }
    }
}
