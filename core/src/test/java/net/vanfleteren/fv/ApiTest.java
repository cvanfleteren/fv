package net.vanfleteren.fv;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.API.validateAll;
import static net.vanfleteren.fv.API.validateThat;
import static net.vanfleteren.fv.assertj.ValidationAssert.assertThatValidation;
import io.vavr.collection.List;

public class ApiTest {

    record Person(String name, int age) {
    }

    @Nested
    class ValidateAll {

        @Test
        void areAll_whenAllValid_returnsValidValidation() {
            // Arrange
            List<String> names = List.of("hello", "hi", "hey");
            Rule<String> startsWithH = Rule.of(s -> s.startsWith("h"), "must.start.with.h");

            // Act
            Validation<List<String>> result = validateAll(names).areAll(startsWithH);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(names);
        }

        @Test
        void areAll_whenSomeInvalid_returnsInvalidWithAccumulatedErrors() {
            // Arrange
            List<String> names = List.of("hello", "apple", "hey", "banana");
            Rule<String> startsWithH = Rule.of(s -> s.startsWith("h"), "must.start.with.h");

            // Act
            Validation<List<String>> result = validateAll(names).areAll(startsWithH);

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("[1].must.start.with.h", "[3].must.start.with.h");
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
