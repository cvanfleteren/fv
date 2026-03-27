package net.vanfleteren.fv.rules;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;
import static net.vanfleteren.fv.rules.StringTransformations.stringTransforms;

class StringTransformationsTest {

    @Nested
    class Trim {

        @Test
        void trim_removesLeadingAndTrailingWhitespace() {
            validTest("  hello  ", "hello", stringTransforms().trim());
        }

        @Test
        void trim_leavesAlreadyTrimmedStringUnchanged() {
            validTest("hello", "hello", stringTransforms().trim());
        }

        @Test
        void trim_emptyStringRemainsEmpty() {
            validTest("", "", stringTransforms().trim());
        }

        @Test
        void trim_whitespaceOnlyStringBecomesEmpty() {
            validTest("   ", "", stringTransforms().trim());
        }

        @Test
        void trim_nullInputReturnsInvalid() {
            invalidTest(null, stringTransforms().trim(), "cannot.be.null");
        }
    }

    @Nested
    class RemoveNewlines {

        @Test
        void removeNewlines_replacesNewlinesWithSpace_andTrims() {
            validTest("hello\nworld", "hello world", stringTransforms().removeNewlines());
        }

        @Test
        void removeNewlines_nullInputReturnsInvalid() {
            invalidTest(null, stringTransforms().removeNewlines(), "cannot.be.null");
        }
    }

    @Nested
    class CollapseWhitespace {

        @Test
        void collapseWhitespace_collapsesAllWhitespaceToSingleSpace() {
            validTest("a \n\t b", "a b", stringTransforms().collapseWhitespace());
        }

        @Test
        void collapseWhitespace_nullInputReturnsInvalid() {
            invalidTest(null, stringTransforms().collapseWhitespace(), "cannot.be.null");
        }
    }

    @Nested
    class RemoveWhitespace {

        @Test
        void removeWhitespace_removesAllWhitespace() {
            validTest(" a b c ", "abc", stringTransforms().removeWhitespace());
        }

        @Test
        void removeWhitespace_nullInputReturnsInvalid() {
            invalidTest(null, stringTransforms().removeWhitespace(), "cannot.be.null");
        }
    }

    @Nested
    class Digits {

        @Test
        void digits_keepsOnlyDigits() {
            validTest("abc123def456", "123456", stringTransforms().digits());
        }

        @Test
        void digits_keepsOnlyDigits_endsUpEmpty() {
            validTest("abcdef", "", stringTransforms().digits());
        }

        @Test
        void digits_nullInputReturnsInvalid() {
            invalidTest(null, stringTransforms().digits(), "cannot.be.null");
        }
    }

    @Nested
    class NonDigits {

        @Test
        void nonDigits_keepsOnlyNonDigits() {
            validTest("abc123def456", "abcdef", stringTransforms().nonDigits());
        }

        @Test
        void nonDigits_keepsOnlyNonDigits_endsUpEmpty() {
            validTest("abcdef\uD83D\uDE19", "abcdef\uD83D\uDE19", stringTransforms().nonDigits());
        }

        @Test
        void nonDigits_nullInputReturnsInvalid() {
            invalidTest(null, stringTransforms().nonDigits(), "cannot.be.null");
        }
    }

    @Nested
    class Alphanumeric {

        @Test
        void alphanumeric_keepsLettersAndDigitsOnly() {
            validTest("abc@#123", "abc123", stringTransforms().alphanumeric());
        }

        @Test
        void alphanumeric_nullInputReturnsInvalid() {
            invalidTest(null, stringTransforms().alphanumeric(), "cannot.be.null");
        }
    }

    @Nested
    class Lowercase {

        @Test
        void lowercase_convertsToLowercase() {
            validTest("HeLLo", "hello", stringTransforms().lowercase());
        }

        @Test
        void lowercase_nullInputReturnsInvalid() {
            invalidTest(null, stringTransforms().lowercase(), "cannot.be.null");
        }
    }

    @Nested
    class Uppercase {

        @Test
        void uppercase_convertsToUppercase() {
            validTest("HeLLo", "HELLO", stringTransforms().uppercase());
        }

        @Test
        void uppercase_nullInputReturnsInvalid() {
            invalidTest(null, stringTransforms().uppercase(), "cannot.be.null");
        }
    }

    @Nested
    class RemoveCharacters {

        @Test
        void removeCharacters_removesSpecifiedCharacters() {
            validTest("a-b-c", "abc", stringTransforms().removeCharacters("-"));
        }

        @Test
        void removeCharacters_nullCharacterListLeavesInputUnchanged() {
            validTest("abc", "abc", stringTransforms().removeCharacters(null));
        }

        @Test
        void removeCharacters_nullInputReturnsInvalid() {
            invalidTest(null, stringTransforms().removeCharacters("-"), "cannot.be.null");
        }
    }

    @Nested
    class ReplaceAll {

        @Test
        void replaceAll_replacesWithProvidedRegexAndReplacement() {
            validTest("phone: 123-456-7890", "phone: 1234567890", stringTransforms().replaceAll("-", ""));
        }

        @Test
        void replaceAll_nullInputReturnsInvalid() {
            invalidTest(null, stringTransforms().replaceAll("-", ""), "cannot.be.null");
        }
    }
}
