package net.vanfleteren.fv.rules;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import net.vanfleteren.fv.Rule;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.API.validateThat;
import static net.vanfleteren.fv.assertj.ValidationAssert.assertThatValidation;

class StringRulesTest {

    static void defaultValidTest(String value, Rule<String> rule) {
        assertThatValidation(validateThat(value, "value").is(rule)).isValid().hasValue(value);
    }

    static void defaultInvalidTest(String value, Rule<String> rule, String... errorKeys) {
        assertThatValidation(validateThat(value, "value").is(rule))
                .isInvalid()
                .hasErrorKeys(errorKeys);
    }

    static void defaultInvalidTest(String value, Rule<String> rule, String errorKey, Map<String, Object> args) {
        assertThatValidation(validateThat(value, "value").is(rule))
                .isInvalid()
                .hasErrorMessage(errorKey, args);
    }

    @Nested
    class NotEmpty {

        @Test
        void valid() {
            defaultValidTest("hello", StringRules.notEmpty);
            defaultValidTest(" ", StringRules.notEmpty);
            defaultValidTest("\n", StringRules.notEmpty);
        }

        @Test
        void invalid() {
            defaultInvalidTest("", StringRules.notEmpty, "not.empty");
        }
    }

    @Nested
    class NotBlank {

        @Test
        void valid() {
            defaultValidTest("hello", StringRules.notBlank);
            defaultValidTest(" x ", StringRules.notBlank);
        }

        @Test
        void invalid() {
            defaultInvalidTest("", StringRules.notBlank, "not.blank");
            defaultInvalidTest("   ", StringRules.notBlank, "not.blank");
            defaultInvalidTest("\n\t", StringRules.notBlank, "not.blank");
        }
    }

    @Nested
    class Trimmed {

        @Test
        void valid() {
            defaultValidTest("hello", StringRules.trimmed);
            defaultValidTest("he llo", StringRules.trimmed);
            defaultValidTest("", StringRules.trimmed);
        }

        @Test
        void invalid() {
            defaultInvalidTest(" hello", StringRules.trimmed, "must.be.trimmed");
            defaultInvalidTest("hello ", StringRules.trimmed, "must.be.trimmed");
            defaultInvalidTest(" hello ", StringRules.trimmed, "must.be.trimmed");
        }
    }

    @Nested
    class NoWhitespace {

        @Test
        void valid() {
            defaultValidTest("hello", StringRules.noWhitespace);
            defaultValidTest("", StringRules.noWhitespace);
            defaultValidTest("abc123", StringRules.noWhitespace);
        }

        @Test
        void invalid() {
            defaultInvalidTest(" ", StringRules.noWhitespace, "no.whitespace.allowed");
            defaultInvalidTest("a b", StringRules.noWhitespace, "no.whitespace.allowed");
            defaultInvalidTest("\n", StringRules.noWhitespace, "no.whitespace.allowed");
            defaultInvalidTest("a\tb", StringRules.noWhitespace, "no.whitespace.allowed");
        }
    }

    @Nested
    class MinLength {

        @Test
        void valid() {
            defaultValidTest("12", StringRules.minLength(2));
            defaultValidTest("  ", StringRules.minLength(2));
            defaultValidTest("123", StringRules.minLength(2));
        }

        @Test
        void invalid() {
            defaultInvalidTest("", StringRules.minLength(2), "min.length", HashMap.of("min", 2));
            defaultInvalidTest("1", StringRules.minLength(2), "min.length", HashMap.of("min", 2));
        }
    }

    @Nested
    class MaxLength {

        @Test
        void valid() {
            defaultValidTest("", StringRules.maxLength(0));
            defaultValidTest("1", StringRules.maxLength(1));
            defaultValidTest("12", StringRules.maxLength(2));
        }

        @Test
        void invalid() {
            defaultInvalidTest("1", StringRules.maxLength(0), "max.length", HashMap.of("max", 0));
            defaultInvalidTest("123", StringRules.maxLength(2), "max.length", HashMap.of("max", 2));
        }
    }

    @Nested
    class LengthBetween {

        @Test
        void valid() {
            defaultValidTest("", StringRules.lengthBetween(0, 0));
            defaultValidTest("1", StringRules.lengthBetween(1, 1));
            defaultValidTest("12", StringRules.lengthBetween(1, 2));
            defaultValidTest("12", StringRules.lengthBetween(2, 3));
        }

        @Test
        void invalid() {
            defaultInvalidTest("", StringRules.lengthBetween(1, 2), "length.between", HashMap.of("min", 1, "max", 2));
            defaultInvalidTest("123", StringRules.lengthBetween(1, 2), "length.between", HashMap.of("min", 1, "max", 2));
        }
    }

    @Nested
    class ExactLength {

        @Test
        void valid() {
            defaultValidTest("", StringRules.exactLength(0));
            defaultValidTest("1", StringRules.exactLength(1));
            defaultValidTest("12", StringRules.exactLength(2));
        }

        @Test
        void invalid() {
            defaultInvalidTest("", StringRules.exactLength(1), "length.exact", HashMap.of("len", 1));
            defaultInvalidTest("12", StringRules.exactLength(1), "length.exact", HashMap.of("len", 1));
        }
    }

    @Nested
    class Contains {

        @Test
        void valid() {
            defaultValidTest("hello", StringRules.contains("ell"));
            defaultValidTest("hello", StringRules.contains(""));
            defaultValidTest("", StringRules.contains(""));
        }

        @Test
        void invalid() {
            defaultInvalidTest("hello", StringRules.contains("xyz"), "must.contain", HashMap.of("fragment", "xyz"));
            defaultInvalidTest("", StringRules.contains("x"), "must.contain", HashMap.of("fragment", "x"));
        }
    }

    @Nested
    class StartsWith {

        @Test
        void valid() {
            defaultValidTest("hello", StringRules.startsWith("he"));
            defaultValidTest("hello", StringRules.startsWith(""));
            defaultValidTest("", StringRules.startsWith(""));
        }

        @Test
        void invalid() {
            defaultInvalidTest("hello", StringRules.startsWith("xy"), "must.start.with", HashMap.of("prefix", "xy"));
            defaultInvalidTest("", StringRules.startsWith("x"), "must.start.with", HashMap.of("prefix", "x"));
        }
    }

    @Nested
    class EndsWith {

        @Test
        void valid() {
            defaultValidTest("hello", StringRules.endsWith("lo"));
            defaultValidTest("hello", StringRules.endsWith(""));
            defaultValidTest("", StringRules.endsWith(""));
        }

        @Test
        void invalid() {
            defaultInvalidTest("hello", StringRules.endsWith("xy"), "must.end.with", HashMap.of("suffix", "xy"));
            defaultInvalidTest("", StringRules.endsWith("x"), "must.end.with", HashMap.of("suffix", "x"));
        }
    }

    @Nested
    class Matches {

        @Test
        void valid() {
            defaultValidTest("12345", StringRules.matches("\\d+"));
            defaultValidTest("ab12", StringRules.matches("[a-z]{2}\\d{2}"));
        }

        @Test
        void invalid() {
            defaultInvalidTest("12a", StringRules.matches("\\d+"), "must.match.regex", HashMap.of("regex", "\\d+"));
            defaultInvalidTest("ab123", StringRules.matches("[a-z]{2}\\d{2}"), "must.match.regex", HashMap.of("regex", "[a-z]{2}\\d{2}"));
        }
    }

    @Nested
    class Alpha {

        @Test
        void valid() {
            defaultValidTest("", StringRules.alpha);          // empty is ok; combine with notEmpty if you need non-empty
            defaultValidTest("abc", StringRules.alpha);
            defaultValidTest("Åß", StringRules.alpha);        // unicode letters are allowed
        }

        @Test
        void invalid() {
            defaultInvalidTest("abc1", StringRules.alpha, "must.be.alpha");
            defaultInvalidTest("a b", StringRules.alpha, "must.be.alpha");
            defaultInvalidTest("-", StringRules.alpha, "must.be.alpha");
        }
    }

    @Nested
    class AlphaNumeric {

        @Test
        void valid() {
            defaultValidTest("", StringRules.alphaNumeric);
            defaultValidTest("abc", StringRules.alphaNumeric);
            defaultValidTest("abc123", StringRules.alphaNumeric);
            defaultValidTest("Åß١٢3", StringRules.alphaNumeric); // letters + digits (unicode digits too)
        }

        @Test
        void invalid() {
            defaultInvalidTest("a_b", StringRules.alphaNumeric, "must.be.alphanumeric");
            defaultInvalidTest("a b", StringRules.alphaNumeric, "must.be.alphanumeric");
            defaultInvalidTest("!", StringRules.alphaNumeric, "must.be.alphanumeric");
        }
    }

    @Nested
    class OnlyDigits {

        @Test
        void valid() {
            defaultValidTest("", StringRules.onlyDigits);
            defaultValidTest("0123", StringRules.onlyDigits);
            defaultValidTest("١٢٣", StringRules.onlyDigits); // unicode digits are allowed by Character.isDigit
        }

        @Test
        void invalid() {
            defaultInvalidTest("12a", StringRules.onlyDigits, "must.be.digits.only");
            defaultInvalidTest("12 3", StringRules.onlyDigits, "must.be.digits.only");
            defaultInvalidTest("-", StringRules.onlyDigits, "must.be.digits.only");
        }
    }

    @Nested
    class OnlyAsciiDigits {

        @Test
        void valid() {
            defaultValidTest("", StringRules.onlyAsciiDigits());
            defaultValidTest("0123", StringRules.onlyAsciiDigits());
        }

        @Test
        void invalid() {
            defaultInvalidTest("١٢٣", StringRules.onlyAsciiDigits(), "must.be.ascii.digits.only");
            defaultInvalidTest("12a", StringRules.onlyAsciiDigits(), "must.be.ascii.digits.only");
        }
    }
}