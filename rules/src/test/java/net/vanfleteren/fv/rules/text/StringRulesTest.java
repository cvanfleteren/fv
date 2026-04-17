package net.vanfleteren.fv.rules.text;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;
import static net.vanfleteren.fv.rules.text.StringRules.strings;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
            invalidTest("", strings().notEmpty, "must.not.be.empty");
            invalidTest(null, strings().notEmpty, "must.not.be.null");
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
            invalidTest("", strings.notBlank, "must.not.be.blank");
            invalidTest("   ", strings.notBlank, "must.not.be.blank");
            invalidTest("\n\t", strings.notBlank, "must.not.be.blank");
            invalidTest(null, strings.notBlank, "must.not.be.null");
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
            invalidTest(null, strings.trimmed, "must.not.be.null");
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
            invalidTest(" ", strings.noWhitespace, "must.not.contain.whitespace");
            invalidTest("a b", strings.noWhitespace, "must.not.contain.whitespace");
            invalidTest("\n", strings.noWhitespace, "must.not.contain.whitespace");
            invalidTest("a\tb", strings.noWhitespace, "must.not.contain.whitespace");
            invalidTest(null, strings.noWhitespace, "must.not.be.null");
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
            invalidTest("", strings.minLength(2), "must.have.min.length", HashMap.of("min", 2));
            invalidTest("1", strings.minLength(2), "must.have.min.length", HashMap.of("min", 2));
            invalidTest(null, strings.minLength(2), "must.not.be.null");
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
            invalidTest("1", strings.maxLength(0), "must.have.max.length", HashMap.of("max", 0));
            invalidTest("123", strings.maxLength(2), "must.have.max.length", HashMap.of("max", 2));
            invalidTest(null, strings.maxLength(2), "must.not.be.null");
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
            invalidTest("", strings.lengthBetween(1, 2), "must.have.length.between", HashMap.of("min", 1, "max", 2));
            invalidTest("123", strings.lengthBetween(1, 2), "must.have.length.between", HashMap.of("min", 1, "max", 2));
            invalidTest(null, strings.lengthBetween(1, 2), "must.not.be.null");
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
            invalidTest("", strings.exactLength(1), "must.have.exact.length", HashMap.of("len", 1));
            invalidTest("12", strings.exactLength(1), "must.have.exact.length", HashMap.of("len", 1));
            invalidTest(null, strings.exactLength(1), "must.not.be.null");
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
            invalidTest(null, strings.contains("x"), "must.not.be.null");
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
            invalidTest(null, strings.containsIgnoreCase("x"), "must.not.be.null");
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
            invalidTest(null, strings.startsWith("x"), "must.not.be.null");
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
            invalidTest(null, strings.startsWithIgnoreCase("x"), "must.not.be.null");
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
            invalidTest(null, strings.endsWith("x"), "must.not.be.null");
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
            invalidTest(null, strings.endsWithIgnoreCase("x"), "must.not.be.null");
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
            invalidTest(null, strings.notIn(HashSet.of("admin")), "must.not.be.null");
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
            invalidTest(null, strings.matches("\\d+"), "must.not.be.null");
        }

        @Test
        void valid_pattern() {
            validTest("12345", strings.matches(Pattern.compile("\\d+")));
            validTest("AB12", strings.matches(Pattern.compile("[a-z]{2}\\d{2}", Pattern.CASE_INSENSITIVE)));
        }

        @Test
        void invalid_pattern() {
            Pattern pattern = Pattern.compile("\\d+");
            invalidTest("12a", strings.matches(pattern), "must.match.regex", HashMap.of("regex", pattern));
            invalidTest(null, strings.matches(pattern), "must.not.be.null");
        }

        @Test
        void nullPattern_throwsException() {
            assertThrows(NullPointerException.class, () -> strings.matches((Pattern) null));
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
            invalidTest(null, strings.alpha, "must.not.be.null");
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
            invalidTest(null, strings.alphaNumeric, "must.not.be.null");
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
            invalidTest(null, strings.alphaNumericUnicode, "must.not.be.null");
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
            invalidTest(null, strings.onlyUnicodeDigits, "must.not.be.null");
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
            invalidTest(null, strings.onlyDigits(), "must.not.be.null");
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
            invalidTest(null, strings.hexadecimal(), "must.not.be.null");
        }
    }

    @Nested
    class Base64 {

        @Test
        void valid() {
            validTest("", strings.base64());
            validTest("SGVsbG8=", strings.base64());
            validTest("SGVsbG8gd29ybGQ=", strings.base64());
            validTest("YW55IGNhcm5hbCBwbGVhc3VyZS4=", strings.base64());
            validTest("YW55IGNhcm5hbCBwbGVhc3VyZQ==", strings.base64());
            validTest("YW55IGNhcm5hbCBwbGVhc3Vy", strings.base64());
            validTest("Zm9vYmFy", strings.base64());
        }

        @Test
        void invalid() {
            invalidTest("SGVsbG8", strings.base64(), "must.be.base64"); // length not multiple of 4 and no padding
            invalidTest("SGVsbG8!", strings.base64(), "must.be.base64"); // invalid character
            invalidTest("SGVsbG8==", strings.base64(), "must.be.base64"); // invalid padding (only 1 or 2 '=' allowed at end)
            invalidTest("=SGVsbG8", strings.base64(), "must.be.base64"); // padding at start
            invalidTest("SGVz-G8", strings.base64(), "must.be.base64"); // URL-safe char in standard Base64
            invalidTest(null, strings.base64(), "must.not.be.null");
        }
    }

    @Nested
    class Base64UrlSafe {

        @Test
        void valid() {
            validTest("", strings.base64UrlSafe());
            validTest("SGVsbG8=", strings.base64UrlSafe());
            validTest("SGVsbG8_dz9ybGQ=", strings.base64UrlSafe()); // includes '_'
            validTest("YW55LWNhcm5hbC1wbGVhc3VyZQ==", strings.base64UrlSafe()); // includes '-'
        }

        @Test
        void invalid() {
            invalidTest("SGVsbG8+", strings.base64UrlSafe(), "must.be.base64.urlsafe"); // '+' not allowed in URL-safe
            invalidTest("SGVsbG8/", strings.base64UrlSafe(), "must.be.base64.urlsafe"); // '/' not allowed in URL-safe
            invalidTest(null, strings.base64UrlSafe(), "must.not.be.null");
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
            invalidTest(null, strings.looksLikeEmailAddress(), "must.not.be.null");
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

        @Test
        void asInteger_whenNull_returnsInvalid() {
            invalidTest(null, strings.asInteger(), "must.not.be.null");
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

        @Test
        void asLong_whenNull_returnsInvalid() {
            invalidTest(null, strings.asLong(), "must.not.be.null");
        }
    }

    @Nested
    class AsDouble {

        @Test
        void asDouble_whenValidDoubleString_returnsValidDouble() {
            validTest("123.45", 123.45, strings.asDouble());
        }

        @Test
        void asDouble_whenInvalidDoubleString_returnsInvalid() {
            invalidTest("abc", strings.asDouble(), "must.be.double");
        }

        @Test
        void asDouble_whenEmptyString_returnsInvalid() {
            invalidTest("", strings.asDouble(), "must.be.double");
        }

        @Test
        void asDouble_whenNull_returnsInvalid() {
            invalidTest(null, strings.asDouble(), "must.not.be.null");
        }
    }

    @Nested
    class AsFloat {

        @Test
        void asFloat_whenValidFloatString_returnsValidFloat() {
            validTest("12.3", 12.3f, strings.asFloat());
        }

        @Test
        void asFloat_whenInvalidFloatString_returnsInvalid() {
            invalidTest("abc", strings.asFloat(), "must.be.float");
        }

        @Test
        void asFloat_whenEmptyString_returnsInvalid() {
            invalidTest("", strings.asFloat(), "must.be.float");
        }

        @Test
        void asFloat_whenNull_returnsInvalid() {
            invalidTest(null, strings.asFloat(), "must.not.be.null");
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

        @Test
        void asBigInteger_whenNull_returnsInvalid() {
            invalidTest(null, strings.asBigInteger(), "must.not.be.null");
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

        @Test
        void asBigDecimal_whenNull_returnsInvalid() {
            invalidTest(null, strings.asBigDecimal(), "must.not.be.null");
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

        @Test
        void asUUID_whenNull_returnsInvalid() {
            invalidTest(null, strings.asUUID(), "must.not.be.null");
        }
    }

    @Nested
    class AsURL {

        @Test
        void asURL_whenValidURLString_returnsValidURL() throws MalformedURLException {
            String urlStr = "https://www.example.com";
            validTest(urlStr, URI.create(urlStr).toURL(), strings.asURL());
        }

        @Test
        void asURL_whenInvalidURLString_returnsInvalid() {
            invalidTest("not-a-url", strings.asURL(), "must.be.url");
        }

        @Test
        void asURL_whenEmptyString_returnsInvalid() {
            invalidTest("", strings.asURL(), "must.be.url");
        }

        @Test
        void asURL_whenNull_returnsInvalid() {
            invalidTest(null, strings.asURL(), "must.not.be.null");
        }
    }

    @Nested
    class AsLocalDateTime {

        @Test
        void asLocalDateTime_whenValidLocalDateTimeString_returnsValidLocalDateTime() {
            String ldtStr = "2023-10-27T10:15:30";
            validTest(ldtStr, LocalDateTime.parse(ldtStr), strings.asLocalDateTime());
        }

        @Test
        void asLocalDateTime_whenInvalidLocalDateTimeString_returnsInvalid() {
            invalidTest("not-a-date-time", strings.asLocalDateTime(), "must.be.localdatetime");
        }

        @Test
        void asLocalDateTime_whenEmptyString_returnsInvalid() {
            invalidTest("", strings.asLocalDateTime(), "must.be.localdatetime");
        }

        @Test
        void asLocalDateTime_whenNull_returnsInvalid() {
            invalidTest(null, strings.asLocalDateTime(), "must.not.be.null");
        }
    }

    @Nested
    class AsLocalDate {

        @Test
        void asLocalDate_whenValidLocalDateString_returnsValidLocalDate() {
            String ldStr = "2023-10-27";
            validTest(ldStr, LocalDate.parse(ldStr), strings.asLocalDate());
        }

        @Test
        void asLocalDate_whenInvalidLocalDateString_returnsInvalid() {
            invalidTest("not-a-date", strings.asLocalDate(), "must.be.localdate");
        }

        @Test
        void asLocalDate_whenEmptyString_returnsInvalid() {
            invalidTest("", strings.asLocalDate(), "must.be.localdate");
        }

        @Test
        void asLocalDate_whenNull_returnsInvalid() {
            invalidTest(null, strings.asLocalDate(), "must.not.be.null");
        }
    }

    @Nested
    class AsInstant {

        @Test
        void asInstant_whenValidInstantString_returnsValidInstant() {
            String instantStr = "2023-10-27T10:15:30Z";
            validTest(instantStr, Instant.parse(instantStr), strings.asInstant());
        }

        @Test
        void asInstant_whenInvalidInstantString_returnsInvalid() {
            invalidTest("not-an-instant", strings.asInstant(), "must.be.instant");
        }

        @Test
        void asInstant_whenEmptyString_returnsInvalid() {
            invalidTest("", strings.asInstant(), "must.be.instant");
        }

        @Test
        void asInstant_whenNull_returnsInvalid() {
            invalidTest(null, strings.asInstant(), "must.not.be.null");
        }
    }

    @Nested
    class AsURI {

        @Test
        void asURI_whenValidURIString_returnsValidURI() {
            String uriStr = "https://www.example.com/path?q=1#anchor";
            validTest(uriStr, URI.create(uriStr), strings.asURI());
        }

        @Test
        void asURI_whenRelativeURIString_returnsValidURI() {
            String uriStr = "/relative/path";
            validTest(uriStr, URI.create(uriStr), strings.asURI());
        }

        @Test
        void asURI_whenInvalidURIString_returnsInvalid() {
            invalidTest("not a valid uri with spaces", strings.asURI(), "must.be.uri");
        }

        @Test
        void asURI_whenNull_returnsInvalid() {
            invalidTest(null, strings.asURI(), "must.not.be.null");
        }
    }

    @Nested
    class SingleLine {

        @Test
        void valid() {
            validTest("hello world", strings.singleLine);
            validTest("", strings.singleLine);
            validTest("no line breaks here", strings.singleLine);
        }

        @Test
        void invalid() {
            invalidTest("line1\nline2", strings.singleLine, "must.be.single.line");
            invalidTest("line1\rline2", strings.singleLine, "must.be.single.line");
            invalidTest("line1\r\nline2", strings.singleLine, "must.be.single.line");
            invalidTest("\n", strings.singleLine, "must.be.single.line");
            invalidTest(null, strings.singleLine, "must.not.be.null");
        }
    }

    @Nested
    class Uppercase {

        @Test
        void valid() {
            validTest("HELLO", strings.uppercase);
            validTest("ABC123", strings.uppercase);
            validTest("", strings.uppercase);
            validTest("123", strings.uppercase);  // digits have no case
        }

        @Test
        void invalid() {
            invalidTest("Hello", strings.uppercase, "must.be.uppercase");
            invalidTest("hello", strings.uppercase, "must.be.uppercase");
            invalidTest("ABCd", strings.uppercase, "must.be.uppercase");
            invalidTest(null, strings.uppercase, "must.not.be.null");
        }
    }

    @Nested
    class Lowercase {

        @Test
        void valid() {
            validTest("hello", strings.lowercase);
            validTest("abc123", strings.lowercase);
            validTest("", strings.lowercase);
            validTest("123", strings.lowercase);  // digits have no case
        }

        @Test
        void invalid() {
            invalidTest("Hello", strings.lowercase, "must.be.lowercase");
            invalidTest("HELLO", strings.lowercase, "must.be.lowercase");
            invalidTest("abcD", strings.lowercase, "must.be.lowercase");
            invalidTest(null, strings.lowercase, "must.not.be.null");
        }
    }

    @Nested
    class DoesNotContain {

        @Test
        void valid() {
            validTest("hello", strings.doesNotContain("xyz"));
            validTest("", strings.doesNotContain("x"));
            validTest("hello", strings.doesNotContain("HELLO")); // case-sensitive
        }

        @Test
        void invalid() {
            invalidTest("hello world", strings.doesNotContain("world"), "must.not.contain", HashMap.of("fragment", "world"));
            invalidTest("hello", strings.doesNotContain("ell"), "must.not.contain", HashMap.of("fragment", "ell"));
            invalidTest("abc", strings.doesNotContain(""), "must.not.contain", HashMap.of("fragment", ""));
            invalidTest(null, strings.doesNotContain("world"), "must.not.be.null");
        }

        @Test
        void doesNotContain_whenNullFragment_throwsException() {
            assertThrows(NullPointerException.class, () -> strings.doesNotContain(null));
        }
    }

    @Nested
    class IsIn {

        @Test
        void valid() {
            validTest("admin", strings.isIn(HashSet.of("admin", "user", "guest")));
            validTest("user", strings.isIn(HashSet.of("admin", "user")));
        }

        @Test
        void invalid() {
            invalidTest(
                    "root",
                    strings.isIn(HashSet.of("admin", "user")),
                    "must.be.in",
                    HashMap.of("allowed", HashSet.of("admin", "user"))
            );
            invalidTest(
                    "",
                    strings.isIn(HashSet.of("admin")),
                    "must.be.in",
                    HashMap.of("allowed", HashSet.of("admin"))
            );
            invalidTest(null, strings.isIn(HashSet.of("admin")), "must.not.be.null");
        }

        @Test
        void isIn_whenNullAllowed_throwsException() {
            assertThrows(NullPointerException.class, () -> strings.isIn(null));
        }
    }
}