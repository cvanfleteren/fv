package net.vanfleteren.fv;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.API.validateAll;
import static net.vanfleteren.fv.API.validateThat;
import static net.vanfleteren.fv.assertj.ValidationAssert.assertThatValidation;
import io.vavr.collection.List;

import java.math.BigDecimal;

public class ApiTest {

    record Person(String name, int age) {
    }

    @Nested
    class ValidateAll {

        @Test
        void areAll_whenAllValid_returnsValidValidation() {
            // Arrange
            List<BigDecimal> numbers = List.of(BigDecimal.ONE, BigDecimal.TEN);
            Rule<Number> positive = Rule.of(n -> n.doubleValue() > 0, "must.be.positive");

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
            Rule<Number> positive = Rule.of(n -> n.doubleValue() > 0, "must.be.positive");

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
            Rule<String> startsWithH = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "must.not.be.empty");

            Rule<String> compliant = notEmpty.and(startsWithH);


            Person p = new Person("john", 30);

            Validation<String> v = validateThat(p.name(),"name").is(compliant);

            assertThatValidation(v).isInvalid().hasErrorMessage("name.must.start.with.h");
        }
    }
}
