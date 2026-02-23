package net.vanfleteren.fv.rules;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;

class StringRulesTest {


    @Nested
    class NotEmpty {

        @Test
        void valid() {
            validTest("hello", StringRules.notEmpty);
            validTest(" ", StringRules.notEmpty);
            validTest("\n", StringRules.notEmpty);
        }

        @Test
        void invalid() {
            invalidTest("", StringRules.notEmpty, "not.empty");
        }
    }

    @Nested
    class NotBlank {

        @Test
        void valid() {
            validTest("hello", StringRules.notBlank);
            validTest(" x ", StringRules.notBlank);
        }

        @Test
        void invalid() {
            invalidTest("", StringRules.notBlank, "not.blank");
            invalidTest("   ", StringRules.notBlank, "not.blank");
            invalidTest("\n\t", StringRules.notBlank, "not.blank");
        }
    }

    @Nested
    class Trimmed {

        @Test
        void valid() {
            validTest("hello", StringRules.trimmed);
            validTest("he llo", StringRules.trimmed);
            validTest("", StringRules.trimmed);
        }

        @Test
        void invalid() {
            invalidTest(" hello", StringRules.trimmed, "must.be.trimmed");
            invalidTest("hello ", StringRules.trimmed, "must.be.trimmed");
            invalidTest(" hello ", StringRules.trimmed, "must.be.trimmed");
        }
    }

    @Nested
    class NoWhitespace {

        @Test
        void valid() {
            validTest("hello", StringRules.noWhitespace);
            validTest("", StringRules.noWhitespace);
            validTest("abc123", StringRules.noWhitespace);
        }

        @Test
        void invalid() {
            invalidTest(" ", StringRules.noWhitespace, "no.whitespace.allowed");
            invalidTest("a b", StringRules.noWhitespace, "no.whitespace.allowed");
            invalidTest("\n", StringRules.noWhitespace, "no.whitespace.allowed");
            invalidTest("a\tb", StringRules.noWhitespace, "no.whitespace.allowed");
        }
    }

    @Nested
    class MinLength {

        @Test
        void valid() {
            validTest("12", StringRules.minLength(2));
            validTest("  ", StringRules.minLength(2));
            validTest("123", StringRules.minLength(2));
        }

        @Test
        void invalid() {
            invalidTest("", StringRules.minLength(2), "min.length", HashMap.of("min", 2));
            invalidTest("1", StringRules.minLength(2), "min.length", HashMap.of("min", 2));
        }
    }

    @Nested
    class MaxLength {

        @Test
        void valid() {
            validTest("", StringRules.maxLength(0));
            validTest("1", StringRules.maxLength(1));
            validTest("12", StringRules.maxLength(2));
        }

        @Test
        void invalid() {
            invalidTest("1", StringRules.maxLength(0), "max.length", HashMap.of("max", 0));
            invalidTest("123", StringRules.maxLength(2), "max.length", HashMap.of("max", 2));
        }
    }

    @Nested
    class LengthBetween {

        @Test
        void valid() {
            validTest("", StringRules.lengthBetween(0, 0));
            validTest("1", StringRules.lengthBetween(1, 1));
            validTest("12", StringRules.lengthBetween(1, 2));
            validTest("12", StringRules.lengthBetween(2, 3));
        }

        @Test
        void invalid() {
            invalidTest("", StringRules.lengthBetween(1, 2), "length.between", HashMap.of("min", 1, "max", 2));
            invalidTest("123", StringRules.lengthBetween(1, 2), "length.between", HashMap.of("min", 1, "max", 2));
        }
    }

    @Nested
    class ExactLength {

        @Test
        void valid() {
            validTest("", StringRules.exactLength(0));
            validTest("1", StringRules.exactLength(1));
            validTest("12", StringRules.exactLength(2));
        }

        @Test
        void invalid() {
            invalidTest("", StringRules.exactLength(1), "length.exact", HashMap.of("len", 1));
            invalidTest("12", StringRules.exactLength(1), "length.exact", HashMap.of("len", 1));
        }
    }

    @Nested
    class Contains {

        @Test
        void valid() {
            validTest("hello", StringRules.contains("ell"));
            validTest("hello", StringRules.contains(""));
            validTest("", StringRules.contains(""));
        }

        @Test
        void invalid() {
            invalidTest("hello", StringRules.contains("xyz"), "must.contain", HashMap.of("fragment", "xyz"));
            invalidTest("", StringRules.contains("x"), "must.contain", HashMap.of("fragment", "x"));
        }
    }

    @Nested
    class ContainsIgnoreCase {

        @Test
        void valid() {
            validTest("hello", StringRules.containsIgnoreCase("ELL"));
            validTest("HELLO", StringRules.containsIgnoreCase("ell"));
            validTest("HeLlO", StringRules.containsIgnoreCase("eLl"));
            validTest("hello", StringRules.containsIgnoreCase(""));
            validTest("", StringRules.containsIgnoreCase(""));
        }

        @Test
        void invalid() {
            invalidTest("hello", StringRules.containsIgnoreCase("XYZ"), "must.contain.ignorecase", HashMap.of("fragment", "XYZ"));
            invalidTest("", StringRules.containsIgnoreCase("x"), "must.contain.ignorecase", HashMap.of("fragment", "x"));
        }
    }

    @Nested
    class StartsWith {

        @Test
        void valid() {
            validTest("hello", StringRules.startsWith("he"));
            validTest("hello", StringRules.startsWith(""));
            validTest("", StringRules.startsWith(""));
        }

        @Test
        void invalid() {
            invalidTest("hello", StringRules.startsWith("xy"), "must.start.with", HashMap.of("prefix", "xy"));
            invalidTest("", StringRules.startsWith("x"), "must.start.with", HashMap.of("prefix", "x"));
        }
    }

    @Nested
    class StartsWithIgnoreCase {

        @Test
        void valid() {
            validTest("hello", StringRules.startsWithIgnoreCase("HE"));
            validTest("HELLO", StringRules.startsWithIgnoreCase("he"));
            validTest("HeLlO", StringRules.startsWithIgnoreCase("hEl"));
            validTest("hello", StringRules.startsWithIgnoreCase(""));
            validTest("", StringRules.startsWithIgnoreCase(""));
        }

        @Test
        void invalid() {
            invalidTest("hello", StringRules.startsWithIgnoreCase("XY"), "must.start.with.ignorecase", HashMap.of("prefix", "XY"));
            invalidTest("", StringRules.startsWithIgnoreCase("x"), "must.start.with.ignorecase", HashMap.of("prefix", "x"));
        }
    }

    @Nested
    class EndsWith {

        @Test
        void valid() {
            validTest("hello", StringRules.endsWith("lo"));
            validTest("hello", StringRules.endsWith(""));
            validTest("", StringRules.endsWith(""));
        }

        @Test
        void invalid() {
            invalidTest("hello", StringRules.endsWith("xy"), "must.end.with", HashMap.of("suffix", "xy"));
            invalidTest("", StringRules.endsWith("x"), "must.end.with", HashMap.of("suffix", "x"));
        }
    }

    @Nested
    class EndsWithIgnoreCase {

        @Test
        void valid() {
            validTest("hello", StringRules.endsWithIgnoreCase("LO"));
            validTest("HELLO", StringRules.endsWithIgnoreCase("lo"));
            validTest("HeLlO", StringRules.endsWithIgnoreCase("LlO"));
            validTest("hello", StringRules.endsWithIgnoreCase(""));
            validTest("", StringRules.endsWithIgnoreCase(""));
        }

        @Test
        void invalid() {
            invalidTest("hello", StringRules.endsWithIgnoreCase("XY"), "must.end.with.ignorecase", HashMap.of("suffix", "XY"));
            invalidTest("", StringRules.endsWithIgnoreCase("x"), "must.end.with.ignorecase", HashMap.of("suffix", "x"));
            invalidTest("hi", StringRules.endsWithIgnoreCase("LONGER"), "must.end.with.ignorecase", HashMap.of("suffix", "LONGER"));
        }
    }

    @Nested
    class NotIn {

        @Test
        void valid() {
            validTest("hello", StringRules.notIn(HashSet.of("nope", "forbidden")));
            validTest("", StringRules.notIn(HashSet.of("x")));
        }

        @Test
        void invalid() {
            invalidTest(
                    "admin",
                    StringRules.notIn(HashSet.of("admin", "root")),
                    "must.not.be.in",
                    HashMap.of("forbidden", HashSet.of("admin", "root"))
            );
        }
    }

    @Nested
    class Matches {

        @Test
        void valid() {
            validTest("12345", StringRules.matches("\\d+"));
            validTest("ab12", StringRules.matches("[a-z]{2}\\d{2}"));
        }

        @Test
        void invalid() {
            invalidTest("12a", StringRules.matches("\\d+"), "must.match.regex", HashMap.of("regex", "\\d+"));
            invalidTest("ab123", StringRules.matches("[a-z]{2}\\d{2}"), "must.match.regex", HashMap.of("regex", "[a-z]{2}\\d{2}"));
        }
    }

    @Nested
    class Alpha {

        @Test
        void valid() {
            validTest("", StringRules.alpha);          // empty is ok; combine with notEmpty if you need non-empty
            validTest("abc", StringRules.alpha);
            validTest("Åß", StringRules.alpha);        // unicode letters are allowed
        }

        @Test
        void invalid() {
            invalidTest("abc1", StringRules.alpha, "must.be.alpha");
            invalidTest("a b", StringRules.alpha, "must.be.alpha");
            invalidTest("-", StringRules.alpha, "must.be.alpha");
        }
    }

    @Nested
    class AlphaNumeric {

        @Test
        void valid() {
            validTest("", StringRules.alphaNumeric);
            validTest("abc", StringRules.alphaNumeric);
            validTest("abc123", StringRules.alphaNumeric);
            validTest("Åß١٢3", StringRules.alphaNumeric); // letters + digits (unicode digits too)
        }

        @Test
        void invalid() {
            invalidTest("a_b", StringRules.alphaNumeric, "must.be.alphanumeric");
            invalidTest("a b", StringRules.alphaNumeric, "must.be.alphanumeric");
            invalidTest("!", StringRules.alphaNumeric, "must.be.alphanumeric");
        }
    }

    @Nested
    class OnlyDigits {

        @Test
        void valid() {
            validTest("", StringRules.onlyDigits);
            validTest("0123", StringRules.onlyDigits);
            validTest("١٢٣", StringRules.onlyDigits); // unicode digits are allowed by Character.isDigit
        }

        @Test
        void invalid() {
            invalidTest("12a", StringRules.onlyDigits, "must.be.digits.only");
            invalidTest("12 3", StringRules.onlyDigits, "must.be.digits.only");
            invalidTest("-", StringRules.onlyDigits, "must.be.digits.only");
        }
    }

    @Nested
    class OnlyAsciiDigits {

        @Test
        void valid() {
            validTest("", StringRules.onlyAsciiDigits());
            validTest("0123", StringRules.onlyAsciiDigits());
        }

        @Test
        void invalid() {
            invalidTest("١٢٣", StringRules.onlyAsciiDigits(), "must.be.ascii.digits.only");
            invalidTest("12a", StringRules.onlyAsciiDigits(), "must.be.ascii.digits.only");
        }
    }
}