package net.vanfleteren.fv.rules.text;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;
import static net.vanfleteren.fv.rules.text.StringTransformations.stringTransforms;

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
    class Truncate {

        @Test
        void truncate_whenShorterOrEqual_returnsUnchanged() {
            validTest("Hello", "Hello", stringTransforms().truncate(5));
            validTest("Hi", "Hi", stringTransforms().truncate(10));
        }

        @Test
        void truncate_whenLonger_cutsToMaxLen() {
            validTest("HelloWorld", "Hello", stringTransforms().truncate(5));
        }

        @Test
        void truncate_doesNotSplitSurrogatePairs() {
            // 😀 is U+1F600 (surrogate pair), ensure not split when cutting inside the pair
            String s = "A😀B"; // length 4 (A, high, low, B)
            validTest(s, "A😀", stringTransforms().truncate(3)); // cut at 3 would split before 'B', safe
            validTest(s, "A", stringTransforms().truncate(2)); // 2 would be A + high surrogate -> back off to A
        }

        @Test
        void truncate_handlesZeroAndNull() {
            validTest("Hello", "", stringTransforms().truncate(0));
            invalidTest(null, stringTransforms().truncate(2), "cannot.be.null");
        }
    }

    @Nested
    class TruncateWithEllipsis {

        @Test
        void truncateWithEllipsis_whenShortOrEqual_doesNotAppend() {
            validTest("Hello", "Hello", stringTransforms().truncateWithEllipsis(5));
            validTest("Hi", "Hi", stringTransforms().truncateWithEllipsis(10));
        }

        @Test
        void truncateWithEllipsis_appendsUnicodeEllipsisByDefault() {
            validTest("HelloWorld", "Hell…", stringTransforms().truncateWithEllipsis(5));
        }

        @Test
        void truncateWithEllipsis_usesAsciiWhenUnicodeDoesNotFitButThreeDotsDo() {
            // String starts with a surrogate pair; with maxLen 3 and unicode ellipsis, there is no safe room
            // for content (room=2 but safeCutIndex=0). In that case, fall back to ASCII '...'.
            validTest("😀Hello", "😀…", stringTransforms().truncateWithEllipsis(3));
        }

        @Test
        void truncateWithEllipsis_lengthOne_returnsJustEllipsis() {
            validTest("Hello", "…", stringTransforms().truncateWithEllipsis(1));
        }

        @Test
        void truncateWithEllipsis_tooSmall_returnsBestEffort() {
            validTest("Hello", "", stringTransforms().truncateWithEllipsis(0));
            validTest("Hello", "H…", stringTransforms().truncateWithEllipsis(2));
        }

        @Test
        void truncateWithEllipsis_doesNotSplitSurrogatePairs() {
            String s = "A😀B";
            // maxLen 3 -> room for 2 chars + ellipsis; safe cut is 1 (avoid splitting pair), result "A…"
            validTest(s, "A…", stringTransforms().truncateWithEllipsis(3));
        }

        @Test
        void truncateWithEllipsis_unicodeEdgeCases_combiningAndCJK() {
            // Combining mark: e + acute combining; cutting should keep base at boundary
            String combining = "Cafe\u0301 noir"; // Café as decomposed
            validTest(combining, "Cafe\u0301…", stringTransforms().truncateWithEllipsis(6));

            // CJK characters
            String cjk = "世界您好"; // 4 chars
            validTest(cjk, "世…", stringTransforms().truncateWithEllipsis(2));
        }

        @Test
        void truncateWithEllipsis_nullInputReturnsInvalid() {
            invalidTest(null, stringTransforms().truncateWithEllipsis(5), "cannot.be.null");
        }
    }

    @Nested
    class KeepChars {

        @Test
        void keepChars_keepsOnlyAllowedDigits() {
            validTest("abc123-45", "12345", stringTransforms().keepChars("0123456789"));
        }

        @Test
        void keepChars_keepsOnlyProvidedLettersAndSpace() {
            validTest("a1b_ c!d", "ab cd", stringTransforms().keepChars("abcd "));
            validTest("H3llo, 世界!", "Hllo世界", stringTransforms().keepChars("Hllo世界"));
        }

        @Test
        void keepChars_handlesRegexMetaCharsInAllowed() {
            // Allowed contains '-', ']', '[' which must be treated literally
            validTest("x-]y[", "-][", stringTransforms().keepChars("-[]"));
        }

        @Test
        void keepChars_emptyAllowedRemovesEverything() {
            validTest("Hello 123", "", stringTransforms().keepChars(""));
        }

        @Test
        void keepChars_nullAllowedRemovesEverything() {
            validTest("Hello 123", "", stringTransforms().keepChars(null));
        }

        @Test
        void keepChars_nullInputReturnsInvalid() {
            invalidTest(null, stringTransforms().keepChars("abc"), "cannot.be.null");
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
    class LettersOnly {

        @Test
        void lettersOnly_keepsOnlyUnicodeLetters() {
            validTest("H3llo, 世界!", "Hllo世界", stringTransforms().lettersOnly());
        }

        @Test
        void lettersOnly_handlesAccentsAndCombiningMarks() {
            // "e\u0301" is e + combining acute; combining mark should be removed
            validTest("Cafe\u0301 and naïve", "Cafeandnaïve", stringTransforms().lettersOnly());
            // Precomposed accents (like é, ï) are letters and should be preserved; spaces are removed
            validTest("Café naïve", "Cafénaïve", stringTransforms().lettersOnly());
        }

        @Test
        void lettersOnly_returnsEmptyWhenNoLetters() {
            validTest("1234 !?", "", stringTransforms().lettersOnly());
        }

        @Test
        void lettersOnly_nullInputReturnsInvalid() {
            invalidTest(null, stringTransforms().lettersOnly(), "cannot.be.null");
        }
    }

    @Nested
    class LettersAndSpacesOnly {

        @Test
        void lettersAndSpacesOnly_keepsLettersAndSpacesOnly() {
            validTest("Hello, 世界! 123", "Hello 世界 ", stringTransforms().lettersAndSpacesOnly());
        }

        @Test
        void lettersAndSpacesOnly_preservesRegularSpacesButRemovesOtherWhitespace() {
            validTest("A\tB\nC D", "ABC D", stringTransforms().lettersAndSpacesOnly());
        }

        @Test
        void lettersAndSpacesOnly_allowsMultipleSpacesAndDoesNotTrim() {
            validTest("Hi,  there!", "Hi  there", stringTransforms().lettersAndSpacesOnly());
        }

        @Test
        void lettersAndSpacesOnly_emptyWhenNoLettersOrSpaces() {
            validTest("\t\n123,!", "", stringTransforms().lettersAndSpacesOnly());
        }

        @Test
        void lettersAndSpacesOnly_nullInputReturnsInvalid() {
            invalidTest(null, stringTransforms().lettersAndSpacesOnly(), "cannot.be.null");
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

    @Nested
    class StripDiacritics {

        @Test
        void stripDiacritics_removesCombiningMarks() {
            validTest("Café naïve", "Cafe naive", stringTransforms().stripDiacritics());
        }

        @Test
        void stripDiacritics_handlesPrecomposedAndCombiningForms() {
            // "e\u0301" is e + combining acute
            validTest("Cafe\u0301", "Cafe", stringTransforms().stripDiacritics());
        }

        @Test
        void stripDiacritics_multipleCombiningMarks() {
            // a + combining ring + combining acute
            validTest("a\u030A\u0301", "a", stringTransforms().stripDiacritics());
        }

        @Test
        void stripDiacritics_leavesNonAccentedScriptsUntouched() {
            validTest("Привет 世界", "Привет 世界", stringTransforms().stripDiacritics());
        }

        @Test
        void stripDiacritics_doesNotAffectEmojiOrSymbols() {
            validTest("Café 😊", "Cafe 😊", stringTransforms().stripDiacritics());
        }

        @Test
        void stripDiacritics_stringWithoutDiacriticsUnchanged() {
            validTest("Simple ASCII", "Simple ASCII", stringTransforms().stripDiacritics());
        }

        @Test
        void stripDiacritics_emptyStringUnchanged() {
            validTest("", "", stringTransforms().stripDiacritics());
        }

        @Test
        void stripDiacritics_nullInputReturnsInvalid() {
            invalidTest(null, stringTransforms().stripDiacritics(), "cannot.be.null");
        }
    }

    @Nested
    class StripControlChars {

        @Test
        void stripControlChars_removesCcAndZeroWidth() {
            // contains NUL and ZERO WIDTH SPACE between letters
            validTest("A\u0000B\u200BC", "ABC", stringTransforms().stripControlChars());
        }

        @Test
        void stripControlChars_removesLineBreaksAsControls() {
            // LF is a control character; it will be removed
            validTest("hello\nworld", "helloworld", stringTransforms().stripControlChars());
        }

        @Test
        void stripControlChars_removesCRLFAndCR() {
            validTest("a\r\nb\rc\nd", "abcd", stringTransforms().stripControlChars());
        }

        @Test
        void stripControlChars_removesTabsFormFeedAndMore() {
            // includes TAB (\t) and FORM FEED (\f)
            validTest("X\tY\fZ", "XYZ", stringTransforms().stripControlChars());
        }

        @Test
        void stripControlChars_removesZeroWidthJoinersAndBom() {
            // ZWJ \u200D, ZWNJ \u200C, WORD JOINER \u2060, BOM \uFEFF
            validTest("ab\u200Dcd\u200Cef\u2060gh\uFEFFij", "abcdefghij", stringTransforms().stripControlChars());
        }

        @Test
        void stripControlChars_idempotentOnSecondApplication() {
            var rule = stringTransforms().stripControlChars();
            validTest("a\u0000b\u200Bc", "abc", rule.andThen(rule));
        }

        @Test
        void stripControlChars_noControlsUnchanged() {
            validTest("Already clean", "Already clean", stringTransforms().stripControlChars());
        }

        @Test
        void stripControlChars_emptyStringUnchanged() {
            validTest("", "", stringTransforms().stripControlChars());
        }

        @Test
        void stripControlChars_nullInputReturnsInvalid() {
            invalidTest(null, stringTransforms().stripControlChars(), "cannot.be.null");
        }
    }
}
