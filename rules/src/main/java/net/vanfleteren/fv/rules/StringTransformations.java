package net.vanfleteren.fv.rules;

import net.vanfleteren.fv.MappingRule;
import net.vanfleteren.fv.Validation;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

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
     * Keeps only alphanumeric characters (letters A-Z/a-z and digits 0-9).
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
     * Invalid patterns will throw {@link java.util.regex.PatternSyntaxException} at runtime.
     * <p>
     * Null handling: returns {@code Validation.invalid("cannot.be.null")} when the input is {@code null}.
     *
     * @param regex        the regular expression to which the input is to be matched.
     * @param replacement  the string to be substituted for each match.
     * @return a {@link MappingRule} that performs {@link String#replaceAll(String, String)}.
     */
    public MappingRule<String, String> replaceAll(String regex, String replacement) {
        final String rx = Objects.requireNonNullElse(regex, "");
        final String repl = Objects.requireNonNullElse(replacement, "");
        return input -> {
            if (input != null) {
                return Validation.valid(input.replaceAll(rx, repl));
            } else {
                return Validation.invalid("cannot.be.null");
            }
        };
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
