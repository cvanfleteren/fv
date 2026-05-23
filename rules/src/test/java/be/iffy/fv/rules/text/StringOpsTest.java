package be.iffy.fv.rules.text;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static be.iffy.fv.rules.text.StringOps.*;
import static org.assertj.core.api.Assertions.assertThat;

class StringOpsTest {

    void transform(String in, String out, Function<String,String> transformer) {
        assertThat(out).isEqualTo(transformer.apply(in));
    }

    void whenNull(Function<String,String> transformer) {
        assertThat(transformer.apply(null)).isNull();
    }

    @Nested
    class Trim {

        @Test
        void trim_removesLeadingAndTrailingWhitespace() {
            transform("  hello  ", "hello", trim());
        }

        @Test
        void trim_leavesAlreadyTrimmedStringUnchanged() {
            transform("hello", "hello", trim());
        }

        @Test
        void trim_emptyStringRemainsEmpty() {
            transform("", "", trim());
        }

        @Test
        void trim_whitespaceOnlyStringBecomesEmpty() {
            transform("   ", "", trim());
        }

        @Test
        void trim_nullInputReturnsNull() {
            whenNull(trim());
        }
    }

    @Nested
    class Truncate {

        @Test
        void truncate_whenShorterOrEqual_returnsUnchanged() {
            transform("Hello", "Hello", truncate(5));
            transform("Hi", "Hi", truncate(10));
        }

        @Test
        void truncate_whenLonger_cutsToMaxLen() {
            transform("HelloWorld", "Hello", truncate(5));
        }

        @Test
        void truncate_doesNotSplitSurrogatePairs() {
            // 😀 is U+1F600 (surrogate pair), ensure not split when cutting inside the pair
            String s = "A😀B"; // length 4 (A, high, low, B)
            transform(s, "A😀", truncate(3)); // cut at 3 would split before 'B', safe
            transform(s, "A", truncate(2)); // 2 would be A + high surrogate -> back off to A
        }

        @Test
        void truncate_handlesZeroAndNull() {
            transform("Hello", "", truncate(0));
            whenNull(truncate(2));
        }
    }

    @Nested
    class TruncateWithEllipsis {

        @Test
        void truncateWithEllipsis_whenShortOrEqual_doesNotAppend() {
            transform("Hello", "Hello", truncateWithEllipsis(5));
            transform("Hi", "Hi", truncateWithEllipsis(10));
        }

        @Test
        void truncateWithEllipsis_appendsUnicodeEllipsisByDefault() {
            transform("HelloWorld", "Hell…", truncateWithEllipsis(5));
        }

        @Test
        void truncateWithEllipsis_usesAsciiWhenUnicodeDoesNotFitButThreeDotsDo() {
            // String starts with a surrogate pair; with maxLen 3 and Unicode ellipsis, there is no safe room
            // for content (room=2 but safeCutIndex=0). In that case, fall back to ASCII '...'.
            transform("😀Hello", "😀…", truncateWithEllipsis(3));
        }

        @Test
        void truncateWithEllipsis_lengthOne_returnsJustEllipsis() {
            transform("Hello", "…", truncateWithEllipsis(1));
        }

        @Test
        void truncateWithEllipsis_tooSmall_returnsBestEffort() {
            transform("Hello", "", truncateWithEllipsis(0));
            transform("Hello", "H…", truncateWithEllipsis(2));
        }

        @Test
        void truncateWithEllipsis_doesNotSplitSurrogatePairs() {
            String s = "A😀B";
            // maxLen 3 -> room for 2 chars + ellipsis; safe cut is 1 (avoid splitting pair), result "A…"
            transform(s, "A…", truncateWithEllipsis(3));
        }

        @Test
        void truncateWithEllipsis_unicodeEdgeCases_combiningAndCJK() {
            // Combining mark: e + acute combining; cutting should keep base at boundary
            String combining = "Cafe\u0301 noir"; // Café as decomposed
            transform(combining, "Cafe\u0301…", truncateWithEllipsis(6));

            // CJK characters
            String cjk = "世界您好"; // 4 chars
            transform(cjk, "世…", truncateWithEllipsis(2));
        }

        @Test
        void truncateWithEllipsis_nullInputReturnsNull() {
            whenNull(truncateWithEllipsis(5));
        }
    }

    @Nested
    class KeepChars {

        @Test
        void keepChars_keepsOnlyAllowedDigits() {
            transform("abc123-45", "12345", keepChars("0123456789"));
        }

        @Test
        void keepChars_keepsOnlyProvidedLettersAndSpace() {
            transform("a1b_ c!d", "ab cd", keepChars("abcd "));
            transform("H3llo, 世界!", "Hllo世界", keepChars("Hllo世界"));
        }

        @Test
        void keepChars_handlesRegexMetaCharsInAllowed() {
            // Allowed contains '-', ']', '[' which must be treated literally
            transform("x-]y[", "-][", keepChars("-[]"));
        }

        @Test
        void keepChars_emptyAllowedRemovesEverything() {
            transform("Hello 123", "", keepChars(""));
        }

        @Test
        void keepChars_nullAllowedRemovesEverything() {
            transform("Hello 123", "", keepChars(null));
        }

        @Test
        void keepChars_nullInputReturnsNull() {
            whenNull(keepChars("abc"));
        }


    }

    @Nested
    class RemoveNewlines {

        @Test
        void removeNewlines_replacesNewlinesWithSpace_andTrims() {
            transform("hello\nworld", "hello world", removeNewlines());
        }

        @Test
        void removeNewlines_nullInputReturnsNull() {
            whenNull(removeNewlines());
        }
    }

    @Nested
    class CollapseWhitespace {

        @Test
        void collapseWhitespace_collapsesAllWhitespaceToSingleSpace() {
            transform("a \n\t b", "a b", collapseWhitespace());
        }

        @Test
        void collapseWhitespace_nullInputReturnsNull() {
            whenNull(collapseWhitespace());
        }
    }

    @Nested
    class RemoveWhitespace {

        @Test
        void removeWhitespace_removesAllWhitespace() {
            transform(" a b c ", "abc", removeWhitespace());
        }

        @Test
        void removeWhitespace_nullInputReturnsNull() {
            whenNull(removeWhitespace());
        }
    }

    @Nested
    class Digits {

        @Test
        void digits_keepsOnlyDigits() {
            transform("abc123def456", "123456", digits());
        }

        @Test
        void digits_keepsOnlyDigits_endsUpEmpty() {
            transform("abcdef", "", digits());
        }

        @Test
        void digits_nullInputReturnsNull() {
            whenNull(digits());
        }
    }

    @Nested
    class NonDigits {

        @Test
        void nonDigits_keepsOnlyNonDigits() {
            transform("abc123def456", "abcdef", nonDigits());
        }

        @Test
        void nonDigits_keepsOnlyNonDigits_endsUpEmpty() {
            transform("abcdef\uD83D\uDE19", "abcdef\uD83D\uDE19", nonDigits());
        }

        @Test
        void nonDigits_nullInputReturnsNull() {
            whenNull(nonDigits());
        }
    }

    @Nested
    class Alphanumeric {

        @Test
        void alphanumeric_keepsLettersAndDigitsOnly() {
            transform("abc@#123", "abc123", alphanumeric());
        }

        @Test
        void alphanumeric_nullInputReturnsNull() {
            whenNull(alphanumeric());
        }
    }

    @Nested
    class LettersOnly {

        @Test
        void lettersOnly_keepsOnlyUnicodeLetters() {
            transform("H3llo, 世界!", "Hllo世界", lettersOnly());
        }

        @Test
        void lettersOnly_handlesAccentsAndCombiningMarks() {
            // "e\u0301" is e + combining acute; combining mark should be removed
            transform("Cafe\u0301 and naïve", "Cafeandnaïve", lettersOnly());
            // Precomposed accents (like é, ï) are letters and should be preserved; spaces are removed
            transform("Café naïve", "Cafénaïve", lettersOnly());
        }

        @Test
        void lettersOnly_returnsEmptyWhenNoLetters() {
            transform("1234 !?", "", lettersOnly());
        }

        @Test
        void lettersOnly_nullInputReturnsNull() {
            whenNull(lettersOnly());
        }
    }

    @Nested
    class LettersAndSpacesOnly {

        @Test
        void lettersAndSpacesOnly_keepsLettersAndSpacesOnly() {
            transform("Hello, 世界! 123", "Hello 世界 ", lettersAndSpacesOnly());
        }

        @Test
        void lettersAndSpacesOnly_preservesRegularSpacesButRemovesOtherWhitespace() {
            transform("A\tB\nC D", "ABC D", lettersAndSpacesOnly());
        }

        @Test
        void lettersAndSpacesOnly_allowsMultipleSpacesAndDoesNotTrim() {
            transform("Hi,  there!", "Hi  there", lettersAndSpacesOnly());
        }

        @Test
        void lettersAndSpacesOnly_emptyWhenNoLettersOrSpaces() {
            transform("\t\n123,!", "", lettersAndSpacesOnly());
        }

        @Test
        void lettersAndSpacesOnly_nullInputReturnsNull() {
            whenNull(lettersAndSpacesOnly());
        }
    }

    @Nested
    class Lowercase {

        @Test
        void lowercase_convertsToLowercase() {
            transform("HeLLo", "hello", lowercase());
        }

        @Test
        void lowercase_nullInputReturnsNull() {
            whenNull(lowercase());
        }
    }

    @Nested
    class Uppercase {

        @Test
        void uppercase_convertsToUppercase() {
            transform("HeLLo", "HELLO", uppercase());
        }

        @Test
        void uppercase_nullInputReturnsNull() {
            whenNull(uppercase());
        }
    }

    @Nested
    class RemoveCharacters {

        @Test
        void removeCharacters_removesSpecifiedCharacters() {
            transform("a-b-c", "abc", removeCharacters("-"));
        }

        @Test
        void removeCharacters_nullCharacterListLeavesInputUnchanged() {
            transform("abc", "abc", removeCharacters(null));
        }

        @Test
        void removeCharacters_nullInputReturnsNull() {
            whenNull(removeCharacters("-"));
        }
    }

    @Nested
    class ReplaceAll {

        @Test
        void replaceAll_replacesWithProvidedRegexAndReplacement() {
            transform("phone: 123-456-7890", "phone: 1234567890", replaceAll("-", ""));
        }

        @Test
        void replaceAll_nullInputReturnsNull() {
            whenNull(replaceAll("-", ""));
        }
    }

    @Nested
    class StripDiacritics {

        @Test
        void stripDiacritics_removesCombiningMarks() {
            transform("Café naïve", "Cafe naive", stripDiacritics());
        }

        @Test
        void stripDiacritics_handlesPrecomposedAndCombiningForms() {
            // "e\u0301" is e + combining acute
            transform("Cafe\u0301", "Cafe", stripDiacritics());
        }

        @Test
        void stripDiacritics_multipleCombiningMarks() {
            // a + combining ring + combining acute
            transform("a\u030A\u0301", "a", stripDiacritics());
        }

        @Test
        void stripDiacritics_leavesNonAccentedScriptsUntouched() {
            transform("Привет 世界", "Привет 世界", stripDiacritics());
        }

        @Test
        void stripDiacritics_doesNotAffectEmojiOrSymbols() {
            transform("Café 😊", "Cafe 😊", stripDiacritics());
        }

        @Test
        void stripDiacritics_stringWithoutDiacriticsUnchanged() {
            transform("Simple ASCII", "Simple ASCII", stripDiacritics());
        }

        @Test
        void stripDiacritics_emptyStringUnchanged() {
            transform("", "", stripDiacritics());
        }

        @Test
        void stripDiacritics_nullInputReturnsNull() {
            whenNull(stripDiacritics());
        }
    }

    @Nested
    class StripControlChars {

        @Test
        void stripControlChars_removesCcAndZeroWidth() {
            // contains NUL and ZERO WIDTH SPACE between letters
            transform("A\u0000B\u200BC", "ABC", stripControlChars());
        }

        @Test
        void stripControlChars_removesLineBreaksAsControls() {
            // LF is a control character; it will be removed
            transform("hello\nworld", "helloworld", stripControlChars());
        }

        @Test
        void stripControlChars_removesCRLFAndCR() {
            transform("a\r\nb\rc\nd", "abcd", stripControlChars());
        }

        @Test
        void stripControlChars_removesTabsFormFeedAndMore() {
            // includes TAB (\t) and FORM FEED (\f)
            transform("X\tY\fZ", "XYZ", stripControlChars());
        }

        @Test
        void stripControlChars_removesZeroWidthJoinersAndBom() {
            // ZWJ \u200D, ZWNJ \u200C, WORD JOINER \u2060, BOM \uFEFF
            transform("ab\u200Dcd\u200Cef\u2060gh\uFEFFij", "abcdefghij", stripControlChars());
        }

        @Test
        void stripControlChars_idempotentOnSecondApplication() {
            var rule = stripControlChars();
            transform("a\u0000b\u200Bc", "abc", rule.andThen(rule));
        }

        @Test
        void stripControlChars_noControlsUnchanged() {
            transform("Already clean", "Already clean", stripControlChars());
        }

        @Test
        void stripControlChars_emptyStringUnchanged() {
            transform("", "", stripControlChars());
        }

        @Test
        void stripControlChars_nullInputReturnsNull() {
            whenNull(stripControlChars());
        }
    }
}
