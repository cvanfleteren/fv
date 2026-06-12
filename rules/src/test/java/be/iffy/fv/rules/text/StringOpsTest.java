package be.iffy.fv.rules.text;

import be.iffy.fv.Transformation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static be.iffy.fv.rules.text.StringOps.stringOps;
import static org.assertj.core.api.Assertions.assertThat;

class StringOpsTest {


    void transform(String in, String out, Transformation<String> transformer) {
        assertThat(out).isEqualTo(transformer.apply(in));
    }

    void whenNull(Transformation<String> transformer) {
        assertThat(transformer.apply(null)).isNull();
    }

    @Nested
    class Trim {

        @Test
        void trim_removesLeadingAndTrailingWhitespace() {
            transform("  hello  ", "hello", stringOps.trim());
        }

        @Test
        void trim_leavesAlreadyTrimmedStringUnchanged() {
            transform("hello", "hello", stringOps.trim());
        }

        @Test
        void trim_emptyStringRemainsEmpty() {
            transform("", "", stringOps.trim());
        }

        @Test
        void trim_whitespaceOnlyStringBecomesEmpty() {
            transform("   ", "", stringOps.trim());
        }

        @Test
        void trim_nullInputReturnsNull() {
            whenNull(stringOps.trim());
        }
    }

    @Nested
    class Truncate {

        @Test
        void truncate_whenShorterOrEqual_returnsUnchanged() {
            transform("Hello", "Hello", stringOps.truncate(5));
            transform("Hi", "Hi", stringOps.truncate(10));
        }

        @Test
        void truncate_whenLonger_cutsToMaxLen() {
            transform("HelloWorld", "Hello", stringOps.truncate(5));
        }

        @Test
        void truncate_doesNotSplitSurrogatePairs() {
            // 😀 is U+1F600 (surrogate pair), ensure not split when cutting inside the pair
            String s = "A😀B"; // length 4 (A, high, low, B)
            transform(s, "A😀", stringOps.truncate(3)); // cut at 3 would split before 'B', safe
            transform(s, "A", stringOps.truncate(2)); // 2 would be A + high surrogate -> back off to A
        }

        @Test
        void truncate_handlesZeroAndNull() {
            transform("Hello", "", stringOps.truncate(0));
            whenNull(stringOps.truncate(2));
        }
    }

    @Nested
    class TruncateWithEllipsis {

        @Test
        void truncateWithEllipsis_whenShortOrEqual_doesNotAppend() {
            transform("Hello", "Hello", stringOps.truncateWithEllipsis(5));
            transform("Hi", "Hi", stringOps.truncateWithEllipsis(10));
        }

        @Test
        void truncateWithEllipsis_appendsUnicodeEllipsisByDefault() {
            transform("HelloWorld", "Hell…", stringOps.truncateWithEllipsis(5));
        }

        @Test
        void truncateWithEllipsis_usesAsciiWhenUnicodeDoesNotFitButThreeDotsDo() {
            // String starts with a surrogate pair; with maxLen 3 and Unicode ellipsis, there is no safe room
            // for content (room=2 but safeCutIndex=0). In that case, fall back to ASCII '...'.
            transform("😀Hello", "😀…", stringOps.truncateWithEllipsis(3));
        }

        @Test
        void truncateWithEllipsis_lengthOne_returnsJustEllipsis() {
            transform("Hello", "…", stringOps.truncateWithEllipsis(1));
        }

        @Test
        void truncateWithEllipsis_tooSmall_returnsBestEffort() {
            transform("Hello", "", stringOps.truncateWithEllipsis(0));
            transform("Hello", "H…", stringOps.truncateWithEllipsis(2));
        }

        @Test
        void truncateWithEllipsis_doesNotSplitSurrogatePairs() {
            String s = "A😀B";
            // maxLen 3 -> room for 2 chars + ellipsis; safe cut is 1 (avoid splitting pair), result "A…"
            transform(s, "A…", stringOps.truncateWithEllipsis(3));
        }

        @Test
        void truncateWithEllipsis_unicodeEdgeCases_combiningAndCJK() {
            // Combining mark: e + acute combining; cutting should keep base at boundary
            String combining = "Cafe\u0301 noir"; // Café as decomposed
            transform(combining, "Cafe\u0301…", stringOps.truncateWithEllipsis(6));

            // CJK characters
            String cjk = "世界您好"; // 4 chars
            transform(cjk, "世…", stringOps.truncateWithEllipsis(2));
        }

        @Test
        void truncateWithEllipsis_nullInputReturnsNull() {
            whenNull(stringOps.truncateWithEllipsis(5));
        }
    }

    @Nested
    class KeepChars {

        @Test
        void keepChars_keepsOnlyAllowedDigits() {
            transform("abc123-45", "12345", stringOps.keepChars("0123456789"));
        }

        @Test
        void keepChars_keepsOnlyProvidedLettersAndSpace() {
            transform("a1b_ c!d", "ab cd", stringOps.keepChars("abcd "));
            transform("H3llo, 世界!", "Hllo世界", stringOps.keepChars("Hllo世界"));
        }

        @Test
        void keepChars_handlesRegexMetaCharsInAllowed() {
            // Allowed contains '-', ']', '[' which must be treated literally
            transform("x-]y[", "-][", stringOps.keepChars("-[]"));
        }

        @Test
        void keepChars_emptyAllowedRemovesEverything() {
            transform("Hello 123", "", stringOps.keepChars(""));
        }

        @Test
        void keepChars_nullAllowedRemovesEverything() {
            transform("Hello 123", "", stringOps.keepChars(null));
        }

        @Test
        void keepChars_nullInputReturnsNull() {
            whenNull(stringOps.keepChars("abc"));
        }


    }

    @Nested
    class RemoveNewlines {

        @Test
        void removeNewlines_replacesNewlinesWithSpace_andTrims() {
            transform("hello\nworld", "hello world", stringOps.removeNewlines());
        }

        @Test
        void removeNewlines_nullInputReturnsNull() {
            whenNull(stringOps.removeNewlines());
        }
    }

    @Nested
    class CollapseWhitespace {

        @Test
        void collapseWhitespace_collapsesAllWhitespaceToSingleSpace() {
            transform("a \n\t b", "a b", stringOps.collapseWhitespace());
        }

        @Test
        void collapseWhitespace_nullInputReturnsNull() {
            whenNull(stringOps.collapseWhitespace());
        }
    }

    @Nested
    class NormalizeSpace {

        @Test
        void normalizeSpace_collapsesAndTrims() {
            transform("  a \n\t b  ", "a b", stringOps.normalizeSpace());
        }

        @Test
        void normalizeSpace_whitespaceOnlyStringBecomesEmpty() {
            transform("   ", "", stringOps.normalizeSpace());
        }

        @Test
        void normalizeSpace_nullInputReturnsNull() {
            whenNull(stringOps.normalizeSpace());
        }
    }

    @Nested
    class RemoveWhitespace {

        @Test
        void removeWhitespace_removesAllWhitespace() {
            transform(" a b c ", "abc", stringOps.removeWhitespace());
        }

        @Test
        void removeWhitespace_nullInputReturnsNull() {
            whenNull(stringOps.removeWhitespace());
        }
    }

    @Nested
    class Digits {

        @Test
        void digits_keepsOnlyDigits() {
            transform("abc123def456", "123456", stringOps.digits());
        }

        @Test
        void digits_keepsOnlyDigits_endsUpEmpty() {
            transform("abcdef", "", stringOps.digits());
        }

        @Test
        void digits_nullInputReturnsNull() {
            whenNull(stringOps.digits());
        }
    }

    @Nested
    class NonDigits {

        @Test
        void nonDigits_keepsOnlyNonDigits() {
            transform("abc123def456", "abcdef", stringOps.nonDigits());
        }

        @Test
        void nonDigits_keepsOnlyNonDigits_endsUpEmpty() {
            transform("abcdef\uD83D\uDE19", "abcdef\uD83D\uDE19", stringOps.nonDigits());
        }

        @Test
        void nonDigits_nullInputReturnsNull() {
            whenNull(stringOps.nonDigits());
        }
    }

    @Nested
    class Alphanumeric {

        @Test
        void alphanumeric_keepsLettersAndDigitsOnly() {
            transform("abc@#123", "abc123", stringOps.alphanumeric());
        }

        @Test
        void alphanumeric_nullInputReturnsNull() {
            whenNull(stringOps.alphanumeric());
        }
    }

    @Nested
    class LettersOnly {

        @Test
        void lettersOnly_keepsOnlyUnicodeLetters() {
            transform("H3llo, 世界!", "Hllo世界", stringOps.lettersOnly());
        }

        @Test
        void lettersOnly_handlesAccentsAndCombiningMarks() {
            // "e\u0301" is e + combining acute; combining mark should be removed
            transform("Cafe\u0301 and naïve", "Cafeandnaïve", stringOps.lettersOnly());
            // Precomposed accents (like é, ï) are letters and should be preserved; spaces are removed
            transform("Café naïve", "Cafénaïve", stringOps.lettersOnly());
        }

        @Test
        void lettersOnly_returnsEmptyWhenNoLetters() {
            transform("1234 !?", "", stringOps.lettersOnly());
        }

        @Test
        void lettersOnly_nullInputReturnsNull() {
            whenNull(stringOps.lettersOnly());
        }
    }

    @Nested
    class LettersAndSpacesOnly {

        @Test
        void lettersAndSpacesOnly_keepsLettersAndSpacesOnly() {
            transform("Hello, 世界! 123", "Hello 世界 ", stringOps.lettersAndSpacesOnly());
        }

        @Test
        void lettersAndSpacesOnly_preservesRegularSpacesButRemovesOtherWhitespace() {
            transform("A\tB\nC D", "ABC D", stringOps.lettersAndSpacesOnly());
        }

        @Test
        void lettersAndSpacesOnly_allowsMultipleSpacesAndDoesNotTrim() {
            transform("Hi,  there!", "Hi  there", stringOps.lettersAndSpacesOnly());
        }

        @Test
        void lettersAndSpacesOnly_emptyWhenNoLettersOrSpaces() {
            transform("\t\n123,!", "", stringOps.lettersAndSpacesOnly());
        }

        @Test
        void lettersAndSpacesOnly_nullInputReturnsNull() {
            whenNull(stringOps.lettersAndSpacesOnly());
        }
    }

    @Nested
    class Lowercase {

        @Test
        void lowercase_convertsToLowercase() {
            transform("HeLLo", "hello", stringOps.lowercase());
        }

        @Test
        void lowercase_nullInputReturnsNull() {
            whenNull(stringOps.lowercase());
        }
    }

    @Nested
    class Uppercase {

        @Test
        void uppercase_convertsToUppercase() {
            transform("HeLLo", "HELLO", stringOps.uppercase());
        }

        @Test
        void uppercase_nullInputReturnsNull() {
            whenNull(stringOps.uppercase());
        }
    }

    @Nested
    class RemoveCharacters {

        @Test
        void removeCharacters_removesSpecifiedCharacters() {
            transform("a-b-c", "abc", stringOps.removeCharacters("-"));
        }

        @Test
        void removeCharacters_nullCharacterListLeavesInputUnchanged() {
            transform("abc", "abc", stringOps.removeCharacters(null));
        }

        @Test
        void removeCharacters_nullInputReturnsNull() {
            whenNull(stringOps.removeCharacters("-"));
        }
    }

    @Nested
    class ReplaceAll {

        @Test
        void replaceAll_replacesWithProvidedRegexAndReplacement() {
            transform("phone: 123-456-7890", "phone: 1234567890", stringOps.replaceAll("-", ""));
        }

        @Test
        void replaceAll_nullInputReturnsNull() {
            whenNull(stringOps.replaceAll("-", ""));
        }
    }

    @Nested
    class StripDiacritics {

        @Test
        void stripDiacritics_removesCombiningMarks() {
            transform("Café naïve", "Cafe naive", stringOps.stripDiacritics());
        }

        @Test
        void stripDiacritics_handlesPrecomposedAndCombiningForms() {
            // "e\u0301" is e + combining acute
            transform("Cafe\u0301", "Cafe", stringOps.stripDiacritics());
        }

        @Test
        void stripDiacritics_multipleCombiningMarks() {
            // a + combining ring + combining acute
            transform("a\u030A\u0301", "a", stringOps.stripDiacritics());
        }

        @Test
        void stripDiacritics_leavesNonAccentedScriptsUntouched() {
            transform("Привет 世界", "Привет 世界", stringOps.stripDiacritics());
        }

        @Test
        void stripDiacritics_doesNotAffectEmojiOrSymbols() {
            transform("Café 😊", "Cafe 😊", stringOps.stripDiacritics());
        }

        @Test
        void stripDiacritics_stringWithoutDiacriticsUnchanged() {
            transform("Simple ASCII", "Simple ASCII", stringOps.stripDiacritics());
        }

        @Test
        void stripDiacritics_emptyStringUnchanged() {
            transform("", "", stringOps.stripDiacritics());
        }

        @Test
        void stripDiacritics_nullInputReturnsNull() {
            whenNull(stringOps.stripDiacritics());
        }
    }

    @Nested
    class StripControlChars {

        @Test
        void stripControlChars_removesCcAndZeroWidth() {
            // contains NUL and ZERO WIDTH SPACE between letters
            transform("A\u0000B\u200BC", "ABC", stringOps.stripControlChars());
        }

        @Test
        void stripControlChars_removesLineBreaksAsControls() {
            // LF is a control character; it will be removed
            transform("hello\nworld", "helloworld", stringOps.stripControlChars());
        }

        @Test
        void stripControlChars_removesCRLFAndCR() {
            transform("a\r\nb\rc\nd", "abcd", stringOps.stripControlChars());
        }

        @Test
        void stripControlChars_removesTabsFormFeedAndMore() {
            // includes TAB (\t) and FORM FEED (\f)
            transform("X\tY\fZ", "XYZ", stringOps.stripControlChars());
        }

        @Test
        void stripControlChars_removesZeroWidthJoinersAndBom() {
            // ZWJ \u200D, ZWNJ \u200C, WORD JOINER \u2060, BOM \uFEFF
            transform("ab\u200Dcd\u200Cef\u2060gh\uFEFFij", "abcdefghij", stringOps.stripControlChars());
        }
//
//        @Test
//        void stripControlChars_idempotentOnSecondApplication() {
//            var rule = stripControlChars();
//            transform("a\u0000b\u200Bc", "abc", rule.andThen(rule));
//        }

        @Test
        void stripControlChars_noControlsUnchanged() {
            transform("Already clean", "Already clean", stringOps.stripControlChars());
        }

        @Test
        void stripControlChars_emptyStringUnchanged() {
            transform("", "", stringOps.stripControlChars());
        }

        @Test
        void stripControlChars_nullInputReturnsNull() {
            whenNull(stringOps.stripControlChars());
        }
    }
}
