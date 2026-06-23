package be.iffy.fv.rules.text;

import be.iffy.fv.Transformation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.text.Normalizer;

import static be.iffy.fv.rules.text.CharCategory.*;
import static be.iffy.fv.rules.text.StringOps.stringOps;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

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
            String combining = "Café noir"; // Café as decomposed
            transform(combining, "Café…", stringOps.truncateWithEllipsis(6));

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
    class StripNewlines {

        @Test
        void removeNewlines_replacesNewlinesWithSpace_andTrims() {
            transform("hello\nworld", "hello world", stringOps.stripNewlines());
        }

        @Test
        void removeNewlines_nullInputReturnsNull() {
            whenNull(stringOps.stripNewlines());
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
    class Keep {

        @Test
        void keep_asciiDigits_onlyKeeps0to9() {
            transform("abc١٢٣123def456", "123456", stringOps.keep(ASCII_DIGITS));
        }

        @Test
        void keep_digits_keepsUnicodeDigitsToo() {
            transform("abc١٢٣123def", "١٢٣123", stringOps.keep(DIGITS));
        }

        @Test
        void keep_asciiLetters_excludesAccented() {
            transform("abc@#123ë", "abc", stringOps.keep(ASCII_LETTERS));
        }

        @Test
        void keep_letters_keepsUnicodeLetters() {
            transform("H3llo, 世界!", "Hllo世界", stringOps.keep(LETTERS));
        }

        @Test
        void keep_letters_handlesAccentsAndCombiningMarks() {
            // e + combining acute; combining mark should be removed
            transform("Café and naïve", "Cafeandnaïve", stringOps.keep(LETTERS));
            // Precomposed accents are letters and should be preserved; spaces are removed
            transform("Café naïve", "Cafénaïve", stringOps.keep(LETTERS));
        }

        @Test
        void keep_lettersAndSpace_keepsOnlyLettersAndU0020() {
            transform("Hello, 世界! 123", "Hello 世界 ", stringOps.keep(LETTERS, SPACE));
            transform("A\tB\nC D", "ABC D", stringOps.keep(LETTERS, SPACE));
            transform("Hi,  there!", "Hi  there", stringOps.keep(LETTERS, SPACE));
        }

        @Test
        void keep_asciiLettersAndDigits_keepsAsciiAlphanumeric() {
            transform("abc@#123ë", "abc123", stringOps.keep(ASCII_LETTERS, ASCII_DIGITS));
        }

        @Test
        void keep_multipleCategories_combinesCorrectly() {
            transform("abc١٢٣123 def!", "abc١٢٣123def", stringOps.keep(LETTERS, DIGITS));
        }

        @Test
        void keep_asciiPunctuation_keepsAsciiPunctOnly() {
            transform("Hello, world! 123", ",!", stringOps.keep(ASCII_PUNCTUATION));
        }

        @Test
        void keep_punctuation_keepsUnicodePunctuation() {
            // guillemets «» are \p{P} but not \p{Punct}
            transform("Hello, «world»! 123", ",«»!", stringOps.keep(PUNCTUATION));
        }

        @Test
        void keep_marks_keepsOnlyCombiningMarks() {
            // NFD-decomposed "Café" has a combining acute after 'e'
            String nfd = Normalizer.normalize("Café", Normalizer.Form.NFD);
            transform(nfd, "́", stringOps.keep(MARKS));
        }

        @Test
        void keep_nullInputReturnsNull() {
            whenNull(stringOps.keep(ASCII_DIGITS));
            whenNull(stringOps.keep(LETTERS, SPACE));
        }
    }

    @Nested
    class Strip {

        @Test
        void strip_asciiDigits_leavesUnicodeDigitsIntact() {
            transform("abc١٢٣123def456", "abc١٢٣def", stringOps.strip(ASCII_DIGITS));
        }

        @Test
        void strip_digits_removesAllUnicodeDigits() {
            transform("abc١٢٣123def456", "abcdef", stringOps.strip(DIGITS));
        }

        @Test
        void strip_asciiWhitespace_removesCommonWhitespace() {
            transform(" a b c ", "abc", stringOps.strip(ASCII_WHITESPACE));
        }

        @Test
        void strip_whitespace_removesUnicodeWhitespace() {
            // no-break space (U+00A0) and em space (U+2003)
            transform("a b c", "abc", stringOps.strip(WHITESPACE));
        }


        @Test
        void strip_asciiPunctuation_removesAsciiPunct() {
            transform("Hello, world! 123", "Hello world 123", stringOps.strip(ASCII_PUNCTUATION));
        }

        @Test
        void strip_punctuation_removesUnicodePunctuation() {
            transform("Hello, «world»! 123", "Hello world 123", stringOps.strip(PUNCTUATION));
        }

        @Test
        void strip_marks_removesCombiningMarks() {
            // strip(MARKS) on NFD input removes diacritics — equivalent to the NFD step inside stripDiacritics()
            String nfd = Normalizer.normalize("Café naïve", Normalizer.Form.NFD);
            transform(nfd, "Cafe naive", stringOps.strip(MARKS));
        }

        @Test
        void strip_multipleCategories_combinesCorrectly() {
            transform("abc 123", "abc", stringOps.strip(ASCII_DIGITS, ASCII_WHITESPACE));
        }

        @Test
        void strip_nullInputReturnsNull() {
            whenNull(stringOps.strip(DIGITS));
            whenNull(stringOps.strip(WHITESPACE));
        }
    }

    @Nested
    class CharCategoryRegexSafety {

        @Test
        void eachCategory_compilesInKeepAndStrip() {
            for (CharCategory category : CharCategory.values()) {
                assertThatCode(() -> stringOps.keep(category))
                    .as("keep(%s) must compile without PatternSyntaxException", category)
                    .doesNotThrowAnyException();
                assertThatCode(() -> stringOps.strip(category))
                    .as("strip(%s) must compile without PatternSyntaxException", category)
                    .doesNotThrowAnyException();
            }
        }

        @Test
        void representativeCombinations_compile() {
            assertThatCode(() -> stringOps.keep(LETTERS, SPACE)).doesNotThrowAnyException();
            assertThatCode(() -> stringOps.keep(ASCII_LETTERS, ASCII_DIGITS)).doesNotThrowAnyException();
            assertThatCode(() -> stringOps.keep(DIGITS, LETTERS)).doesNotThrowAnyException();
            assertThatCode(() -> stringOps.keep(ASCII_PUNCTUATION, SPACE)).doesNotThrowAnyException();
            assertThatCode(() -> stringOps.keep(PUNCTUATION, LETTERS)).doesNotThrowAnyException();
            assertThatCode(() -> stringOps.keep(WHITESPACE, LETTERS)).doesNotThrowAnyException();
            assertThatCode(() -> stringOps.strip(WHITESPACE, ASCII_PUNCTUATION)).doesNotThrowAnyException();
        }

        @Test
        void whitespace_unicodePropertyDistinguishedFromAscii() {
            // \p{IsWhite_Space} must match no-break space (U+00A0) and em space (U+2003) — Unicode White_Space property
            transform("a b c", "abc", stringOps.strip(WHITESPACE));
            // \s must NOT match no-break space
            transform("a b", "a b", stringOps.strip(ASCII_WHITESPACE));
        }
    }

    @Nested
    class ToLowercase {

        @Test
        void lowercase_convertsToLowercase() {
            transform("HeLLo", "hello", stringOps.toLowercase());
        }

        @Test
        void lowercase_nullInputReturnsNull() {
            whenNull(stringOps.toLowercase());
        }
    }

    @Nested
    class ToUppercase {

        @Test
        void uppercase_convertsToUppercase() {
            transform("HeLLo", "HELLO", stringOps.toUppercase());
        }

        @Test
        void uppercase_nullInputReturnsNull() {
            whenNull(stringOps.toUppercase());
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
            // "é" is e + combining acute
            transform("Café", "Cafe", stringOps.stripDiacritics());
        }

        @Test
        void stripDiacritics_multipleCombiningMarks() {
            // a + combining ring + combining acute
            transform("ǻ", "a", stringOps.stripDiacritics());
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
            transform("A B​C", "ABC", stringOps.stripControlChars());
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
            // ZWJ ‍, ZWNJ ‌, WORD JOINER ⁠, BOM
            transform("ab‍cd‌ef⁠gh﻿ij", "abcdefghij", stringOps.stripControlChars());
        }

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
