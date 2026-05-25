package be.iffy.fv.dsl;

import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import io.vavr.control.Option;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static be.iffy.fv.dsl.DSL.option;
import static be.iffy.fv.dsl.DSL.optional;

public class OptionalOptionDSLTest {

    private final Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "must.not.be.empty");

    @Nested
    class OptionalTests {

        @Test
        void optional_whenEmpty_isValid() {
            Rule<Optional<String>> rule = optional(notEmpty);
            Validation<Optional<String>> result = rule.test(Optional.empty());
            assertThatValidation(result).isValid().isEqualTo(Optional.empty());
        }

        @Test
        void optional_whenValueIsValid_isValid() {
            Rule<Optional<String>> rule = optional(notEmpty);
            Validation<Optional<String>> result = rule.test(Optional.of("valid"));
            assertThatValidation(result).isValid().isEqualTo(Optional.of("valid"));
        }

        @Test
        void optional_whenValueIsInvalid_isInvalid() {
            Rule<Optional<String>> rule = optional(notEmpty);
            Validation<Optional<String>> result = rule.test(Optional.of(""));
            assertThatValidation(result).isInvalid().hasErrorMessage("must.not.be.empty");
        }
    }

    @Nested
    class OptionTests {

        @Test
        void option_whenNone_isValid() {
            Rule<Option<String>> rule = option(notEmpty);
            Validation<Option<String>> result = rule.test(Option.none());
            assertThatValidation(result).isValid().isEqualTo(Option.none());
        }

        @Test
        void option_whenSomeIsValid_isValid() {
            Rule<Option<String>> rule = option(notEmpty);
            Validation<Option<String>> result = rule.test(Option.of("valid"));
            assertThatValidation(result).isValid().isEqualTo(Option.of("valid"));
        }

        @Test
        void option_whenSomeIsInvalid_isInvalid() {
            Rule<Option<String>> rule = option(notEmpty);
            Validation<Option<String>> result = rule.test(Option.of(""));
            assertThatValidation(result).isInvalid().hasErrorMessage("must.not.be.empty");
        }
    }
}
