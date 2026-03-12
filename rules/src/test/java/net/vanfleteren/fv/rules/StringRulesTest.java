package net.vanfleteren.fv.rules;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;
import static net.vanfleteren.fv.rules.StringRules.strings;

class StringRulesTest {

    @Nested
    class NotEmpty {

        @Test
        void valid() {
            validTest("hello", strings.notEmpty);
            validTest(" ", strings.notEmpty);
            validTest("\n", strings.notEmpty);
        }

        @Test
        void invalid() {
            invalidTest("", strings.notEmpty, "cannot.be.empty");
        }
    }

    @Nested
    class NotBlank {

        @Test
        void valid() {
            validTest("hello", strings.notBlank);
            validTest(" x ", strings.notBlank);
        }

        @Test
        void invalid() {
            invalidTest("", strings.notBlank, "not.blank");
            invalidTest("   ", strings.notBlank, "not.blank");
            invalidTest("\n\t", strings.notBlank, "not.blank");
        }
    }

    @Nested
    class Trimmed {

        @Test
        void valid() {
            validTest("hello", strings.trimmed);
            validTest("he llo", strings.trimmed);
            validTest("", strings.trimmed);
        }

        @Test
        void invalid() {
            invalidTest(" hello", strings.trimmed, "must.be.trimmed");
            invalidTest("hello ", strings.trimmed, "must.be.trimmed");
            invalidTest(" hello ", strings.trimmed, "must.be.trimmed");
        }
    }

    @Nested
    class NoWhitespace {

        @Test
        void valid() {
            validTest("hello", strings.noWhitespace);
            validTest("", strings.noWhitespace);
            validTest("abc123", strings.noWhitespace);
        }

        @Test
        void invalid() {
            invalidTest(" ", strings.noWhitespace, "no.whitespace.allowed");
            invalidTest("a b", strings.noWhitespace, "no.whitespace.allowed");
            invalidTest("\n", strings.noWhitespace, "no.whitespace.allowed");
            invalidTest("a\tb", strings.noWhitespace, "no.whitespace.allowed");
        }
    }

    @Nested
    class MinLength {

        @Test
        void valid() {
            validTest("12", strings.minLength(2));
            validTest("  ", strings.minLength(2));
            validTest("123", strings.minLength(2));
        }

        @Test
        void invalid() {
            invalidTest("", strings.minLength(2), "min.length", HashMap.of("min", 2));
            invalidTest("1", strings.minLength(2), "min.length", HashMap.of("min", 2));
        }
    }

    @Nested
    class MaxLength {

        @Test
        void valid() {
            validTest("", strings.maxLength(0));
            validTest("1", strings.maxLength(1));
            validTest("12", strings.maxLength(2));
        }

        @Test
        void invalid() {
            invalidTest("1", strings.maxLength(0), "max.length", HashMap.of("max", 0));
            invalidTest("123", strings.maxLength(2), "max.length", HashMap.of("max", 2));
        }
    }

    @Nested
    class LengthBetween {

        @Test
        void valid() {
            validTest("", strings.lengthBetween(0, 0));
            validTest("1", strings.lengthBetween(1, 1));
            validTest("12", strings.lengthBetween(1, 2));
            validTest("12", strings.lengthBetween(2, 3));
        }

        @Test
        void invalid() {
            invalidTest("", strings.lengthBetween(1, 2), "length.between", HashMap.of("min", 1, "max", 2));
            invalidTest("123", strings.lengthBetween(1, 2), "length.between", HashMap.of("min", 1, "max", 2));
        }
    }

    @Nested
    class ExactLength {

        @Test
        void valid() {
            validTest("", strings.exactLength(0));
            validTest("1", strings.exactLength(1));
            validTest("12", strings.exactLength(2));
        }

        @Test
        void invalid() {
            invalidTest("", strings.exactLength(1), "length.exact", HashMap.of("len", 1));
            invalidTest("12", strings.exactLength(1), "length.exact", HashMap.of("len", 1));
        }
    }

    @Nested
    class Contains {

        @Test
        void valid() {
            validTest("hello", strings.contains("ell"));
            validTest("hello", strings.contains(""));
            validTest("", strings.contains(""));
        }

        @Test
        void invalid() {
            invalidTest("hello", strings.contains("xyz"), "must.contain", HashMap.of("fragment", "xyz"));
            invalidTest("", strings.contains("x"), "must.contain", HashMap.of("fragment", "x"));
        }
    }

    @Nested
    class ContainsIgnoreCase {

        @Test
        void valid() {
            validTest("hello", strings.containsIgnoreCase("ELL"));
            validTest("HELLO", strings.containsIgnoreCase("ell"));
            validTest("HeLlO", strings.containsIgnoreCase("eLl"));
            validTest("hello", strings.containsIgnoreCase(""));
            validTest("", strings.containsIgnoreCase(""));
        }

        @Test
        void invalid() {
            invalidTest("hello", strings.containsIgnoreCase("XYZ"), "must.contain.ignorecase", HashMap.of("fragment", "XYZ"));
            invalidTest("", strings.containsIgnoreCase("x"), "must.contain.ignorecase", HashMap.of("fragment", "x"));
        }
    }

    @Nested
    class StartsWith {

        @Test
        void valid() {
            validTest("hello", strings.startsWith("he"));
            validTest("hello", strings.startsWith(""));
            validTest("", strings.startsWith(""));
        }

        @Test
        void invalid() {
            invalidTest("hello", strings.startsWith("xy"), "must.start.with", HashMap.of("prefix", "xy"));
            invalidTest("", strings.startsWith("x"), "must.start.with", HashMap.of("prefix", "x"));
        }
    }

    @Nested
    class StartsWithIgnoreCase {

        @Test
        void valid() {
            validTest("hello", strings.startsWithIgnoreCase("HE"));
            validTest("HELLO", strings.startsWithIgnoreCase("he"));
            validTest("HeLlO", strings.startsWithIgnoreCase("hEl"));
            validTest("hello", strings.startsWithIgnoreCase(""));
            validTest("", strings.startsWithIgnoreCase(""));
        }

        @Test
        void invalid() {
            invalidTest("hello", strings.startsWithIgnoreCase("XY"), "must.start.with.ignorecase", HashMap.of("prefix", "XY"));
            invalidTest("", strings.startsWithIgnoreCase("x"), "must.start.with.ignorecase", HashMap.of("prefix", "x"));
        }
    }

    @Nested
    class EndsWith {

        @Test
        void valid() {
            validTest("hello", strings.endsWith("lo"));
            validTest("hello", strings.endsWith(""));
            validTest("", strings.endsWith(""));
        }

        @Test
        void invalid() {
            invalidTest("hello", strings.endsWith("xy"), "must.end.with", HashMap.of("suffix", "xy"));
            invalidTest("", strings.endsWith("x"), "must.end.with", HashMap.of("suffix", "x"));
        }
    }

    @Nested
    class EndsWithIgnoreCase {

        @Test
        void valid() {
            validTest("hello", strings.endsWithIgnoreCase("LO"));
            validTest("HELLO", strings.endsWithIgnoreCase("lo"));
            validTest("HeLlO", strings.endsWithIgnoreCase("LlO"));
            validTest("hello", strings.endsWithIgnoreCase(""));
            validTest("", strings.endsWithIgnoreCase(""));
        }

        @Test
        void invalid() {
            invalidTest("hello", strings.endsWithIgnoreCase("XY"), "must.end.with.ignorecase", HashMap.of("suffix", "XY"));
            invalidTest("", strings.endsWithIgnoreCase("x"), "must.end.with.ignorecase", HashMap.of("suffix", "x"));
            invalidTest("hi", strings.endsWithIgnoreCase("LONGER"), "must.end.with.ignorecase", HashMap.of("suffix", "LONGER"));
        }
    }

    @Nested
    class NotIn {

        @Test
        void valid() {
            validTest("hello", strings.notIn(HashSet.of("nope", "forbidden")));
            validTest("", strings.notIn(HashSet.of("x")));
        }

        @Test
        void invalid() {
            invalidTest(
                    "admin",
                    strings.notIn(HashSet.of("admin", "root")),
                    "must.not.be.in",
                    HashMap.of("forbidden", HashSet.of("admin", "root"))
            );
        }
    }

    @Nested
    class Matches {

        @Test
        void valid() {
            validTest("12345", strings.matches("\\d+"));
            validTest("ab12", strings.matches("[a-z]{2}\\d{2}"));
        }

        @Test
        void invalid() {
            invalidTest("12a", strings.matches("\\d+"), "must.match.regex", HashMap.of("regex", "\\d+"));
            invalidTest("ab123", strings.matches("[a-z]{2}\\d{2}"), "must.match.regex", HashMap.of("regex", "[a-z]{2}\\d{2}"));
        }
    }

    @Nested
    class Alpha {

        @Test
        void valid() {
            validTest("", strings.alpha);          // empty is ok; combine with notEmpty if you need non-empty
            validTest("abc", strings.alpha);
            validTest("Åß", strings.alpha);        // unicode letters are allowed
        }

        @Test
        void invalid() {
            invalidTest("abc1", strings.alpha, "must.be.alpha");
            invalidTest("a b", strings.alpha, "must.be.alpha");
            invalidTest("-", strings.alpha, "must.be.alpha");
        }
    }

    @Nested
    class AlphaNumeric {

        @Test
        void valid() {
            validTest("", strings.alphaNumeric);
            validTest("abcz", strings.alphaNumeric);
            validTest("abc12390", strings.alphaNumeric);
        }

        @Test
        void invalid() {
            invalidTest("a_b", strings.alphaNumeric, "must.be.alphanumeric");
            invalidTest("a b", strings.alphaNumeric, "must.be.alphanumeric");
            invalidTest("ë", strings.alphaNumeric, "must.be.alphanumeric");
            invalidTest("!", strings.alphaNumeric, "must.be.alphanumeric");
            invalidTest("Åß١٢3", strings.alphaNumeric, "must.be.alphanumeric");
        }
    }

    @Nested
    class AlphaNumericUnicode {

        @Test
        void valid() {
            validTest("", strings.alphaNumericUnicode);
            validTest("abc", strings.alphaNumericUnicode);
            validTest("abc123", strings.alphaNumericUnicode);
            validTest("Åß١٢3", strings.alphaNumericUnicode); // letters + digits (unicode digits too)
        }

        @Test
        void invalid() {
            invalidTest("a_b", strings.alphaNumericUnicode, "must.be.unicode.alphanumeric");
            invalidTest("a b", strings.alphaNumericUnicode, "must.be.unicode.alphanumeric");
            invalidTest("!", strings.alphaNumericUnicode, "must.be.unicode.alphanumeric");
        }
    }

    @Nested
    class OnlyUnicodeDigits {

        @Test
        void valid() {
            validTest("", strings.onlyUnicodeDigits);
            validTest("0123", strings.onlyUnicodeDigits);
            validTest("١٢٣", strings.onlyUnicodeDigits); // unicode digits are allowed by Character.isDigit
        }

        @Test
        void invalid() {
            invalidTest("12a", strings.onlyUnicodeDigits, "must.be.unicode.digits.only");
            invalidTest("12 3", strings.onlyUnicodeDigits, "must.be.unicode.digits.only");
            invalidTest("-", strings.onlyUnicodeDigits, "must.be.unicode.digits.only");
        }
    }

    @Nested
    class OnlyDigits {

        @Test
        void valid() {
            validTest("", strings.onlyDigits());
            validTest("0123", strings.onlyDigits());
        }

        @Test
        void invalid() {
            invalidTest("١٢٣", strings.onlyDigits(), "must.be.digits.only");
            invalidTest("12a", strings.onlyDigits(), "must.be.digits.only");
        }
    }

    @Nested
    class Hexadecimal {

        @Test
        void valid() {
            validTest("", strings.hexadecimal());              // empty is ok
            validTest("0", strings.hexadecimal());
            validTest("F", strings.hexadecimal());             // uppercase
            validTest("a", strings.hexadecimal());             // lowercase
            validTest("123456789ABCDEF", strings.hexadecimal()); // uppercase
            validTest("abcdef", strings.hexadecimal());        // lowercase
            validTest("deadBEEF", strings.hexadecimal());      // mixed case
        }

        @Test
        void invalid() {
            invalidTest("g", strings.hexadecimal(), "must.be.hexadecimal");
            invalidTest("xyz", strings.hexadecimal(), "must.be.hexadecimal");
            invalidTest("0x123", strings.hexadecimal(), "must.be.hexadecimal"); // no prefix allowed
            invalidTest("12 34", strings.hexadecimal(), "must.be.hexadecimal"); // no spaces
        }
    }


    @Nested
    class LooksLikeEmailAddress {

        @Test
        void valid() {
            // Basic RFC‑822‑style addresses – keep it simple for our use‑case
            validTest("user@example.com", strings.looksLikeEmailAddress());
            validTest("first.last+tag@sub.domain.org", strings.looksLikeEmailAddress());
            validTest("test@localhost", strings.looksLikeEmailAddress()); // host only
            validTest("name@123.456.789.012", strings.looksLikeEmailAddress()); // numeric host
        }

        @Test
        void invalid() {
            // Empty string
            invalidTest("", strings.looksLikeEmailAddress(), "must.be.email");
            // No @ symbol
            invalidTest("plainaddress", strings.looksLikeEmailAddress(), "must.be.email");
            // Missing domain part
            invalidTest("foo@", strings.looksLikeEmailAddress(), "must.be.email");
            // Domain starts with a dot
            invalidTest("foo@.com", strings.looksLikeEmailAddress(), "must.be.email");
            // No local part
            invalidTest("@example.com", strings.looksLikeEmailAddress(), "must.be.email");
        }
    }

    @Nested
    class AsInteger {

        @Test
        void asInteger_whenValidIntegerString_returnsValidInteger() {
            validTest("123", 123, strings.asInteger());
        }

        @Test
        void asInteger_whenInvalidIntegerString_returnsInvalid() {
            invalidTest("abc", strings.asInteger(), "must.be.integer");
        }

        @Test
        void asInteger_whenEmptyString_returnsInvalid() {
            invalidTest("", strings.asInteger(), "must.be.integer");
        }
    }

    @Nested
    class AsLong {

        @Test
        void asLong_whenValidLongString_returnsValidLong() {
            validTest("-1234567890123", -1234567890123L, strings.asLong());
        }

        @Test
        void asLong_whenInvalidLongString_returnsInvalid() {
            invalidTest("not-a-long", strings.asLong(), "must.be.long");
        }

        @Test
        void asLong_whenValueTooLargeForLong_returnsInvalid() {
            invalidTest("9223372036854775808", strings.asLong(), "must.be.long");
        }
    }

    @Nested
    class AsBigInteger {

        @Test
        void asBigInteger_whenValidBigIntegerString_returnsValidBigInteger() {
            validTest("12345678901234567890", new BigInteger("12345678901234567890"), strings.asBigInteger());
        }

        @Test
        void asBigInteger_whenInvalidBigIntegerString_returnsInvalid() {
            invalidTest("abc", strings.asBigInteger(), "must.be.biginteger");
        }

        @Test
        void asBigInteger_whenEmptyString_returnsInvalid() {
            invalidTest("", strings.asBigInteger(), "must.be.biginteger");
        }
    }

    @Nested
    class AsBigDecimal {

        @Test
        void asBigDecimal_whenValidBigDecimalString_returnsValidBigDecimal() {
            validTest("123.45", new BigDecimal("123.45"), strings.asBigDecimal());
        }

        @Test
        void asBigDecimal_whenInvalidBigDecimalString_returnsInvalid() {
            invalidTest("not-a-decimal", strings.asBigDecimal(), "must.be.bigdecimal");
        }

        @Test
        void asBigDecimal_whenEmptyString_returnsInvalid() {
            invalidTest("", strings.asBigDecimal(), "must.be.bigdecimal");
        }
    }

    @Nested
    class AsUUID {

        @Test
        void asUUID_whenValidUUIDString_returnsValidUUID() {
            String uuidStr = "550e8400-e29b-41d4-a716-446655440000";
            validTest(uuidStr, UUID.fromString(uuidStr), strings.asUUID());
        }

        @Test
        void asUUID_whenInvalidUUIDString_returnsInvalid() {
            invalidTest("not-a-uuid", strings.asUUID(), "must.be.uuid");
        }

        @Test
        void asUUID_whenEmptyString_returnsInvalid() {
            invalidTest("", strings.asUUID(), "must.be.uuid");
        }
    }
}