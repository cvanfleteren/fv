package net.vanfleteren.fv.rules.text;

import net.vanfleteren.fv.MappingRule;
import net.vanfleteren.fv.Validation;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

public class StringTransformations {


    /**
     * Singleton instance of {@link StringTransformations}.
     */
    public static final StringTransformations strings = new StringTransformations();

    /**
     * Returns the singleton instance of {@link StringTransformations}.
     *
     * @return the {@link StringTransformations} instance.
     */
    public static StringTransformations stringTransforms() {
        return strings;
    }


    /**
     * Trims leading and trailing whitespace from the input string.
     * <p>
     * Null handling: returns {@code Validation.invalid("cannot.be.null")} when the input is {@code null}.
     *
     * @return a {@link MappingRule} that maps {@code "  hello  "} to {@code "hello"}.
     */
    public MappingRule<String,String> trim() {
        return nullSafe(String::trim);
    }

    /**
     * Replaces all newline sequences with a single space and trims the result.
     * <p>
     * Example: {@code "hello\nworld" -> "hello world"}.
     * <p>
     * Null handling: returns {@code Validation.invalid("cannot.be.null")} when the input is {@code null}.
     *
     * @return a {@link MappingRule} that normalizes line breaks to spaces and trims.
     */
    public MappingRule<String, String> removeNewlines() {
        return nullSafe(s -> s.replaceAll("\\R", " ").trim());
    }

    /**
     * Collapses any consecutive whitespace (spaces, tabs, newlines, etc.) to a single space.
     * This method does not trim leading or trailing spaces.
     * <p>
     * Example: {@code "a \n\t b" -> "a b"}.
     * <p>
     * Null handling: returns {@code Validation.invalid("cannot.be.null")} when the input is {@code null}.
     *
     * @return a {@link MappingRule} that collapses all runs of whitespace to a single space.
     */
    public MappingRule<String, String> collapseWhitespace() {
        // Collapse any consecutive whitespace (including newlines and tabs) to a single space
        return nullSafe(s -> s.replaceAll("\\s+", " "));
    }

    /**
     * Removes all whitespace characters from the input (spaces, tabs, newlines, etc.).
     * <p>
     * Example: {@code " a b c " -> "abc"}.
     * <p>
     * Null handling: returns {@code Validation.invalid("cannot.be.null")} when the input is {@code null}.
     *
     * @return a {@link MappingRule} that strips all whitespace.
     */
    public MappingRule<String, String> removeWhitespace() {
        return nullSafe(s -> s.replaceAll("\\s+", ""));
    }

    /**
     * Keeps only digit characters (0-9) and removes all others.
     * <p>
     * Example: {@code "abc123def456" -> "123456"}.
     * <p>
     * Null handling: returns {@code Validation.invalid("cannot.be.null")} when the input is {@code null}.
     *
     * @return a {@link MappingRule} that filters the input to digits only.
     */
    public MappingRule<String, String> digits() {
        return nullSafe(s -> s.replaceAll("\\D+", ""));
    }

    /**
     * Removes all digit characters (0-9), keeping only non-digits.
     * <p>
     * Example: {@code "abc123def456" -> "abcdef"}.
     * <p>
     * Null handling: returns {@code Validation.invalid("cannot.be.null")} when the input is {@code null}.
     *
     * @return a {@link MappingRule} that removes digits from the input.
     */
    public MappingRule<String, String> nonDigits() {
        return nullSafe(s -> s.replaceAll("\\d+", ""));
    }

    /**
     * Keeps only alphanumeric ASCII characters (letters A-Z/a-z and digits 0-9).
     * <p>
     * Example: {@code "abc@#123" -> "abc123"}.
     * <p>
     * Null handling: returns {@code Validation.invalid("cannot.be.null")} when the input is {@code null}.
     *
     * @return a {@link MappingRule} that filters the input to letters and digits.
     */
    public MappingRule<String, String> alphanumeric() {
        return nullSafe(s -> s.replaceAll("[^A-Za-z0-9]+", ""));
    }

    /**
     * Keeps only Unicode letters and removes everything else (digits, punctuation, symbols, whitespace, etc.).
     * <p>
     * Uses the Unicode category {@code \p{L}} to detect letters across all scripts.
     * <p>
     * Example: {@code "H3llo, 世界!" -> "Hllo世界"}.
     * <p>
     * Null handling: returns {@code Validation.invalid("cannot.be.null")} when the input is {@code null}.
     *
     * @return a {@link MappingRule} that filters the input to letters only.
     */
    public MappingRule<String, String> lettersOnly() {
        return nullSafe(s -> s.replaceAll("[^\\p{L}]+", ""));
    }

    /**
     * Keeps only Unicode letters and regular spaces (U+0020), removing all other characters including
     * digits, punctuation, symbols, and other kinds of whitespace (tabs, newlines, etc.).
     * <p>
     * Example: {@code "Hello, 世界! 123" -> "Hello 世界"}.
     * <p>
     * Note: This preserves existing spacing but does not trim. Use {@link #trim()} or {@link #collapseWhitespace()} if needed.
     * <p>
     * Null handling: returns {@code Validation.invalid("cannot.be.null")} when the input is {@code null}.
     *
     * @return a {@link MappingRule} that filters the input to letters and spaces only.
     */
    public MappingRule<String, String> lettersAndSpacesOnly() {
        // Preserve only \p{L} (letters) and literal space. Remove everything else, including other whitespace kinds.
        return nullSafe(s -> s.replaceAll("[^\\p{L} ]+", ""));
    }

    /**
     * Converts the input to lower case using {@link Locale#ROOT}.
     * <p>
     * Example: {@code "HeLLo" -> "hello"}.
     * <p>
     * Null handling: returns {@code Validation.invalid("cannot.be.null")} when the input is {@code null}.
     *
     * @return a {@link MappingRule} that lowercases the input with locale-independent semantics.
     */
    public MappingRule<String, String> lowercase() {
        return nullSafe(s -> s.toLowerCase(Locale.ROOT));
    }

    /**
     * Converts the input to lower case using the provided {@link Locale}.
     * <p>
     * Example: {@code "HeLLo" -> "hello"} (actual output may depend on the locale).
     * <p>
     * Null handling: returns {@code Validation.invalid("cannot.be.null")} when the input is {@code null}.
     *
     * @param locale the {@link Locale} to use for case conversion; must not be {@code null}.
     * @return a {@link MappingRule} that lowercases the input with the given locale.
     */
    public MappingRule<String, String> lowercase(Locale locale) {
        return nullSafe(s -> s.toLowerCase(locale));
    }

    /**
     * Converts the input to upper case using {@link Locale#ROOT}.
     * <p>
     * Example: {@code "HeLLo" -> "HELLO"}.
     * <p>
     * Null handling: returns {@code Validation.invalid("cannot.be.null")} when the input is {@code null}.
     *
     * @return a {@link MappingRule} that uppercases the input with locale-independent semantics.
     */
    public MappingRule<String, String> uppercase() {
        return nullSafe(s -> s.toUpperCase(Locale.ROOT));
    }

    /**
     * Converts the input to upper case using the provided {@link Locale}.
     * <p>
     * Example: {@code "HeLLo" -> "HELLO"} (actual output may depend on the locale).
     * <p>
     * Null handling: returns {@code Validation.invalid("cannot.be.null")} when the input is {@code null}.
     *
     * @param locale the {@link Locale} to use for case conversion; must not be {@code null}.
     * @return a {@link MappingRule} that uppercases the input with the given locale.
     */
    public MappingRule<String, String> uppercase(Locale locale) {
        return nullSafe(s -> s.toUpperCase(locale));
    }

    /**
     * Removes all occurrences of the given characters from the input.
     * <p>
     * The set of characters to remove is taken as-is (no regex), but internally escaped
     * to form a character class. If {@code chars} is {@code null} or empty, the input is returned unchanged.
     * <p>
     * Example: {@code removeCharacters("-").apply("a-b-c") -> "abc"}.
     * <p>
     * Null handling: returns {@code Validation.invalid("cannot.be.null")} when the input is {@code null}.
     *
     * @param chars characters to remove; if {@code null}, nothing is removed.
     * @return a {@link MappingRule} that strips the specified characters from the input.
     */
    public MappingRule<String, String> removeCharacters(String chars) {
        final String toRemove = Objects.requireNonNullElse(chars, "");
        // Build a character class from the provided characters
        final String characterClass = toRemove
                .chars()
                .mapToObj(c -> {
                    char ch = (char) c;
                    // Escape regex metacharacters inside character class: - ] ^ \
                    if (ch == '-' || ch == ']' || ch == '^' || ch == '\\') {
                        return "\\" + ch;
                    }
                    return String.valueOf(ch);
                })
                .reduce(new StringBuilder("["), StringBuilder::append, StringBuilder::append)
                .append("]+")
                .toString();

        return input -> {
            if (input != null) {
                if (toRemove.isEmpty()) {
                    return Validation.valid(input);
                }
                return Validation.valid(input.replaceAll(characterClass, ""));
            } else {
                return Validation.invalid("cannot.be.null");
            }
        };
    }

    /**
     * Replaces all substrings of the input that match the given regular expression with the given replacement.
     * <p>
     * Example: {@code replaceAll("-", "").apply("phone: 123-456-7890") -> "phone: 1234567890"}.
     * <p>
     * If {@code regex} or {@code replacement} is {@code null}, they are treated as empty strings.
     * Invalid patterns will throw {@link java.util.regex.PatternSyntaxException} at creation time,
     * The regex gets compiled into a {@link Pattern} for efficient reuse.
     * <p>
     * Null handling: returns {@code Validation.invalid("cannot.be.null")} when the input is {@code null}.
     *
     * @param regex        the regular expression to which the input is to be matched.
     * @param replacement  the string to be substituted for each match.
     * @return a {@link MappingRule} that performs {@link java.util.regex.Matcher#replaceAll(String)}.
     */
    public MappingRule<String, String> replaceAll(String regex, String replacement) {
        final String rx = Objects.requireNonNullElse(regex, "");
        final String repl = Objects.requireNonNullElse(replacement, "");
        final Pattern pattern = Pattern.compile(rx);
        return input -> {
            if (input != null) {
                return Validation.valid(pattern.matcher(input).replaceAll(repl));
            } else {
                return Validation.invalid("cannot.be.null");
            }
        };
    }

    /**
     * Keeps only the characters that are present in the supplied {@code allowed} set and removes all others.
     * <p>
     * The {@code allowed} string is treated as a literal set of characters (not a regex). Internally it is escaped
     * to form a character class. If {@code allowed} is {@code null} or empty, the result will always be the empty
     * string for non-null inputs.
     * <p>
     * Examples:
     * <ul>
     *     <li>{@code keepChars("0123456789").apply("abc123-45")} → {@code "12345"}</li>
     *     <li>{@code keepChars("-[]").apply("a-]b[")} → {@code "-]["}</li>
     * </ul>
     * <p>
     * Null handling: returns {@code Validation.invalid("cannot.be.null")} when the input is {@code null}.
     *
     * @param allowed the characters to keep; if {@code null} or empty, nothing is kept (result becomes empty string)
     * @return a {@link MappingRule} that filters the input to the provided character set.
     */
    public MappingRule<String, String> keepChars(String allowed) {
        final String toKeep = Objects.requireNonNullElse(allowed, "");

        if (toKeep.isEmpty()) {
            // For any non-null input, return empty string
            return input -> input != null ? Validation.valid("") : Validation.invalid("cannot.be.null");
        }

        // Build a safe character class content from the provided characters
        final StringBuilder allowedClassContent = new StringBuilder();
        toKeep.chars().forEach(c -> {
            char ch = (char) c;
            // Escape regex metacharacters inside a character class: - ] ^ \ [
            if (ch == '-' || ch == ']' || ch == '^' || ch == '\\' || ch == '[') {
                allowedClassContent.append('\\').append(ch);
            } else {
                allowedClassContent.append(ch);
            }
        });

        // We want to remove everything NOT in the allowed class
        final Pattern notAllowed = Pattern.compile("[^" + allowedClassContent + "]+");

        return input -> {
            if (input != null) {
                return Validation.valid(notAllowed.matcher(input).replaceAll(""));
            } else {
                return Validation.invalid("cannot.be.null");
            }
        };
    }

    /**
     * Removes diacritical marks (accents/combining marks) from the input while preserving base characters.
     * <p>
     * Implementation detail: normalizes to {@link java.text.Normalizer.Form#NFD}, removes all combining marks (\p{M}+), then
     * re-normalizes to {@link java.text.Normalizer.Form#NFC}.
     * <p>
     * Example: {@code "Café naïve" -> "Cafe naive"}.
     * <p>
     * Null handling: returns {@code Validation.invalid("cannot.be.null")} when the input is {@code null}.
     *
     * @return a {@link MappingRule} that strips diacritics while keeping base letters.
     */
    public MappingRule<String, String> stripDiacritics() {
        return nullSafe(s -> {
            String nfd = Normalizer.normalize(s, Normalizer.Form.NFD);
            String stripped = nfd.replaceAll("\\p{M}+", "");
            return Normalizer.normalize(stripped, Normalizer.Form.NFC);
        });
    }

    /**
     * Removes control characters and common zero-width/format characters from the input.
     * <p>
     * This removes Unicode category {@code Cc} (ISO control chars) and specific format characters like
     * ZERO WIDTH SPACE/ZWJ/ZWNJ, WORD JOINER, and BOM (\u200B-\u200D, \u2060, \uFEFF).
     * <p>
     * Example: {@code "A\u0000B\u200BC" -> "ABC"}.
     * <p>
     * Note: This will also remove line breaks since they are control characters.
     * <p>
     * Null handling: returns {@code Validation.invalid("cannot.be.null")} when the input is {@code null}.
     *
     * @return a {@link MappingRule} that strips control and zero-width formatting characters.
     */
    public MappingRule<String, String> stripControlChars() {
        // \p{Cc}: control chars. Additionally strip zero-width/formatting characters.
        return nullSafe(s -> s.replaceAll("[\\p{Cc}\\u200B-\\u200D\\u2060\\uFEFF]+", ""));
    }

    /**
     * Truncates the string to at most {@code maxLen} UTF-16 code units without splitting a surrogate pair.
     * <p>
     * If the input length is less than or equal to {@code maxLen}, the input is returned unchanged.
     * If cutting at {@code maxLen} would split a surrogate pair (i.e. the character boundary falls between
     * a high and low surrogate), the cut index is moved one position to the left to preserve the pair.
     * <p>
     * Null handling: returns {@code Validation.invalid("cannot.be.null")} when the input is {@code null}.
     *
     * @param maxLen maximum length of the resulting string, must be {@code >= 0}
     * @return a {@link MappingRule} that truncates strings safely.
     * @throws IllegalArgumentException when {@code maxLen} is negative
     */
    public MappingRule<String, String> truncate(int maxLen) {
        if (maxLen < 0) {
            throw new IllegalArgumentException("maxLen must be >= 0");
        }
        return nullSafe(s -> {
            if (s.length() <= maxLen) return s;
            int cut = safeCutIndex(s, maxLen);
            return s.substring(0, cut);
        });
    }

    /**
     * Truncates the string and appends an ellipsis when content is cut, avoiding surrogate pair splits.
     * <p>
     * Behavior:
     * - If the input length is {@code <= maxLen}, returns input unchanged (no ellipsis).
     * - If over length, appends an ellipsis while ensuring the total length is {@code <= maxLen}.
     * - Uses the single-character Unicode ellipsis {@code …} when it fits. If it doesn't fit but three characters
     *   do, falls back to ASCII {@code ...}. For very small {@code maxLen}, returns as much as possible without
     *   appending ellipsis when even the shortest ellipsis can't fit.
     * - Never splits surrogate pairs at the cut point.
     * <p>
     * Null handling: returns {@code Validation.invalid("cannot.be.null")} when the input is {@code null}.
     *
     * @param maxLen maximum total length including the ellipsis, must be {@code >= 0}
     * @return a {@link MappingRule} that truncates with ellipsis safely.
     * @throws IllegalArgumentException when {@code maxLen} is negative
     */
    public MappingRule<String, String> truncateWithEllipsis(int maxLen) {
        if (maxLen < 0) {
            throw new IllegalArgumentException("maxLen must be >= 0");
        }
        final String ellipsis = "…";
        final String asciiDots = "...";
        return nullSafe(s -> {
            if (s.length() <= maxLen) return s;

            switch(maxLen) {
                case 0 -> {
                    return "";
                }
                case 1 -> {
                    return ellipsis;
                }
                case 2 -> {
                    // Prefer one char + unicode ellipsis if safe; else best two code units without ellipsis
                    int cut1 = safeCutIndex(s, 1);
                    if (cut1 > 0) {
                        return s.substring(0, cut1) + ellipsis;
                    } else {
                        int cut2 = safeCutIndex(s, 2);
                        return s.substring(0, cut2);
                    }
                }
                default -> {
                    // maxLen >= 4: prefer single-character ellipsis with as much content as fits
                    int room = maxLen - 1;
                    int cut = safeCutIndex(s, room);
                    if (cut > 0) {
                        return s.substring(0, cut) + ellipsis;
                    }
                    // Fallback: if somehow no room for content with single ellipsis, try ASCII dots
                    room = maxLen - 3;
                    cut = safeCutIndex(s, room);
                    if (cut > 0) {
                        return s.substring(0, cut) + asciiDots;
                    }
                    // Ultimately, just return the dots (shouldn't usually happen for maxLen>=4)
                    return asciiDots;
                }
            }
        });
    }

    private static int safeCutIndex(String s, int maxUnits) {
        if (maxUnits <= 0) return 0;
        int len = s.length();
        int cut = Math.min(maxUnits, len);
        // If we are exactly at a boundary that would split a surrogate pair, step back one
        if (cut < len) {
            if (cut > 0) {
                char prev = s.charAt(cut - 1);
                char next = s.charAt(cut);
                if (Character.isHighSurrogate(prev) && Character.isLowSurrogate(next)) {
                    cut -= 1;
                }
            }
        }
        return cut;
    }

    private MappingRule<String,String> nullSafe(Function<String,String> op) {
        return input -> {
            if (input != null) {
                return Validation.valid(op.apply(input));
            } else {
                return Validation.invalid("cannot.be.null");
            }
        };
    }
}
