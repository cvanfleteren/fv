package net.vanfleteren.fv;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.API.validateThat;
import static net.vanfleteren.fv.assertj.ValidationAssert.assertThatValidation;

public class ApiTest {

    record Person(String name, int age) {
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
