package be.iffy.fv.rules.text;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Transformation;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class StringOps {

    /**
     * Singleton instance of {@link StringRules}.
     */
    public static final StringOps stringOps = new StringOps();

    /**
     * Trims leading and trailing whitespace from the input string.
     * Example: {@code "  hello  " ->  "hello"}.
     */
    public Transformation<String> trim() {
        return nullSafe(String::trim);
    }

    /**
     * Replaces all newline sequences with a single space and trims the result.
     * <p>
     * Example: {@code "hello\nworld" -> "hello world"}.
     */
    public Transformation<String> stripNewlines() {
        return nullSafe(s -> s.replaceAll("\\R", " ").trim());
    }

    /**
     * Collapses any consecutive whitespace (spaces, tabs, newlines, etc.) to a single space.
     * This method does not trim leading or trailing spaces.
     * <p>
     * Example: {@code " a \n\t b" -> " a b"}.
     */
    public Transformation<String> collapseWhitespace() {
        // Collapse any consecutive whitespace (including newlines and tabs) to a single space
        return nullSafe(s -> s.replaceAll("\\s+", " "));
    }

    /**
     * Normalizes whitespace by collapsing consecutive whitespace characters to a single space
     * and trimming leading and trailing whitespace.
     * <p>
     * Example: {@code "  a \n\t b  " -> "a b"}.
     */
    public Transformation<String> normalizeSpace() {
        return collapseWhitespace().andThen(trim());
    }

    /**
     * Keeps only characters belonging to the given categories, removing everything else.
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code keep(ASCII_DIGITS)} → keeps only 0–9</li>
     *   <li>{@code keep(LETTERS, SPACE)} → keeps Unicode letters and U+0020</li>
     *   <li>{@code keep(ASCII_LETTERS, ASCII_DIGITS)} → keeps ASCII alphanumeric only</li>
     * </ul>
     *
     * @see CharCategory
     */
    public Transformation<String> keep(CharCategory... categories) {
        String joined = Arrays.stream(categories).map(c -> c.fragment).collect(Collectors.joining());
        Pattern pattern = Pattern.compile("[^" + joined + "]+");
        return nullSafe(s -> pattern.matcher(s).replaceAll(""));
    }

    /**
     * Removes all characters belonging to the given categories, keeping everything else.
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code strip(ASCII_DIGITS)} → removes 0–9, keeps Unicode digits</li>
     *   <li>{@code strip(WHITESPACE)} → removes all Unicode whitespace</li>
     *   <li>{@code strip(DIGITS, LETTERS)} → removes all Unicode digits and letters</li>
     * </ul>
     *
     * @see CharCategory
     */
    public Transformation<String> strip(CharCategory... categories) {
        String joined = Arrays.stream(categories).map(c -> c.fragment).collect(Collectors.joining());
        Pattern pattern = Pattern.compile("[" + joined + "]+");
        return nullSafe(s -> pattern.matcher(s).replaceAll(""));
    }

    /**
     * Converts the input to lower case using {@link Locale#ROOT}.
     * <p>
     * Example: {@code "HeLLo" -> "hello"}.
     */
    public Transformation<String> toLowercase() {
        return nullSafe(s -> s.toLowerCase(Locale.ROOT));
    }

    /**
     * Converts the input to lower case using the provided {@link Locale}.
     * <p>
     * Example: {@code "HeLLo" -> "hello"} (actual output may depend on the locale).
     */
    public Transformation<String> toLowercase(Locale locale) {
        return nullSafe(s -> s.toLowerCase(locale));
    }

    /**
     * Converts the input to upper case using {@link Locale#ROOT}.
     * <p>
     * Example: {@code "HeLLo" -> "HELLO"}.
     */
    public Transformation<String> toUppercase() {
        return nullSafe(s -> s.toUpperCase(Locale.ROOT));
    }

    /**
     * Converts the input to upper case using the provided {@link Locale}.
     * <p>
     * Example: {@code "HeLLo" -> "HELLO"} (actual output may depend on the locale).
     */
    public Transformation<String> toUppercase(Locale locale) {
        return nullSafe(s -> s.toUpperCase(locale));
    }

    /**
     * Removes all occurrences of the given characters from the input.
     * <p>
     * The set of characters to remove is taken as-is (no regex), but internally escaped
     * to form a character class. If {@code chars} is {@code null} or empty, the input is returned unchanged.
     * <p>
     * Example: {@code removeCharacters("-").apply("a-b-c") -> "abc"}.
     *
     * @param chars characters to remove; if {@code null}, nothing is removed.
     */
    public Transformation<String> removeCharacters(String chars) {
        final String toRemove = Objects.requireNonNullElse(chars, "");
        if (toRemove.isEmpty()) {
            return input -> input;
        }
        // Build a character class from the provided characters, escaping regex metacharacters
        final String characterClass = toRemove
                .chars()
                .mapToObj(c -> {
                    char ch = (char) c;
                    if (ch == '-' || ch == ']' || ch == '^' || ch == '\\') {
                        return "\\" + ch;
                    }
                    return String.valueOf(ch);
                })
                .reduce(new StringBuilder("["), StringBuilder::append, StringBuilder::append)
                .append("]+")
                .toString();

        Pattern pattern = Pattern.compile(characterClass);

        return input -> input != null ? pattern.matcher(input).replaceAll("") : null;
    }

    /**
     * Replaces all substrings of the input that match the given regular expression with the given replacement.
     * <p>
     * Example: {@code replaceAll("-", "").apply("phone: 123-456-7890") -> "phone: 1234567890"}.
     * <p>
     * If {@code regex} or {@code replacement} is {@code null}, they are treated as empty strings.
     * Invalid patterns will throw {@link java.util.regex.PatternSyntaxException} at creation time,
     * The regex gets compiled into a {@link Pattern} for efficient reuse.
     *
     * @param regex        the regular expression to which the input is to be matched.
     * @param replacement  the string to be substituted for each match.
     */
    public Transformation<String> replaceAll(String regex, String replacement) {
        final String rx = Objects.requireNonNullElse(regex, "");
        final String repl = Objects.requireNonNullElse(replacement, "");
        final Pattern pattern = Pattern.compile(rx);
        return input -> {
            if (input != null) {
                return pattern.matcher(input).replaceAll(repl);
            } else {
                return null;
            }
        };
    }

    /**
     * Keeps only the characters that are present in the supplied {@code allowed} set and removes all others.
     * <p>
     * The {@code allowed} string is treated as a literal set of characters (not a regex). Internally, it is escaped
     * to form a character class. If {@code allowed} is {@code null} or empty, the result will always be the empty
     * string for non-null inputs.
     * <p>
     * Examples:
     * <ul>
     *     <li>{@code keepChars("0123456789").apply("abc123-45")} → {@code "12345"}</li>
     *     <li>{@code keepChars("-[]").apply("a-]b[")} → {@code "-]["}</li>
     * </ul>
     *
     * @param allowed the characters to keep; if {@code null} or empty, nothing is kept (result becomes empty string)
     * @return a {@link MappingRule} that filters the input to the provided character set.
     */
    public Transformation<String> keepChars(String allowed) {
        final String toKeep = Objects.requireNonNullElse(allowed, "");

        if (toKeep.isEmpty()) {
            // For any non-null input, return empty string
            return input -> input != null ? "" : null;
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
                return notAllowed.matcher(input).replaceAll("");
            } else {
                return null;
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
     */
    public Transformation<String> stripDiacritics() {
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
     */
    public Transformation<String> stripControlChars() {
        // \p{Cc}: control chars. Additionally strip zero-width/formatting characters.
        return nullSafe(s -> s.replaceAll("[\\p{Cc}\\u200B-\\u200D\\u2060\\uFEFF]+", ""));
    }

    /**
     * Truncates the string to at most {@code maxLen} UTF-16 code units without splitting a surrogate pair.
     * <p>
     * If the input length is less than or equal to {@code maxLen}, the input is returned unchanged.
     * If cutting at {@code maxLen} would split a surrogate pair (i.e. the character boundary falls between
     * a high and low surrogate), the cut index is moved one position to the left to preserve the pair.
     *
     * @param maxLen maximum length of the resulting string, must be {@code >= 0}
     * @throws IllegalArgumentException when {@code maxLen} is negative
     */
    public Transformation<String> truncate(int maxLen) {
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
     *
     * @param maxLen maximum total length including the ellipsis, must be {@code >= 0}
     * @throws IllegalArgumentException when {@code maxLen} is negative
     */
    public Transformation<String> truncateWithEllipsis(int maxLen) {
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
                    // Prefer one char + Unicode ellipsis if safe; else best two code units without ellipsis
                    int cut1 = safeCutIndex(s, 1);
                    if (cut1 > 0) {
                        return s.substring(0, cut1) + ellipsis;
                    } else {
                        int cut2 = safeCutIndex(s, 2);
                        return s.substring(0, cut2);
                    }
                }
                default -> {
                    // maxLen >= 3: prefer single-character ellipsis with as much content as fits
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

    private int safeCutIndex(String s, int maxUnits) {
        if (maxUnits <= 0) return 0;
        int len = s.length();
        int cut = Math.min(maxUnits, len);
        // If we are exactly at a boundary that would split a surrogate pair, step back one
        if (cut < len) {
            char prev = s.charAt(cut - 1);
            char next = s.charAt(cut);
            if (Character.isHighSurrogate(prev) && Character.isLowSurrogate(next)) {
                cut -= 1;
            }
        }
        return cut;
    }

    private <T> Transformation<T> nullSafe(Function<T,T> op) {
        return input -> {
            if (input != null) {
                return op.apply(input);
            } else {
                return null;
            }
        };
    }
}
