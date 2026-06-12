package be.iffy.fv.rules.text;

import be.iffy.fv.*;
import be.iffy.fv.rules.ComparableRules;
import be.iffy.fv.rules.IObjectRules;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import io.vavr.collection.Set;
import io.vavr.control.Try;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Validation rules for {@link String} values.
 */
public class StringRules implements ComparableRules<String>, IObjectRules<String> {

    /**
     * Singleton instance of {@link StringRules}.
     */
    public static final StringRules strings = new StringRules();

    //region conversions

    /**
     * Fails if the string is not a valid integer.
     * <p>
     * Error key: {@code must.be.integer}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code value}: the input string ({@link String})</li>
     * </ul>
     *
     * @return a {@link MappingRule} that transforms a String into an {@link Integer}.
     */
    public MappingRule<String, Integer> asInteger() {
        return MappingRules.catching(Integer::parseInt, (input, e) -> ErrorMessage.of("must.be.integer", "value", input));
    }

    /**
     * Fails if the string is not a valid long.
     * <p>
     * Error key: {@code must.be.long}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code value}: the input string ({@link String})</li>
     * </ul>
     */
    public MappingRule<String, Long> asLong() {
        return MappingRules.catching(Long::parseLong, (input, e) -> ErrorMessage.of("must.be.long", "value", input));
    }

    /**
     * Fails if the string is not a valid double.
     * <p>
     * Error key: {@code must.be.double}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code value}: the input string ({@link String})</li>
     * </ul>
     */
    public MappingRule<String, Double> asDouble() {
        return MappingRules.catching(Double::parseDouble, (input, e) -> ErrorMessage.of("must.be.double", "value", input));
    }

    /**
     * Fails if the string is not a valid float.
     * <p>
     * Error key: {@code must.be.float}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code value}: the input string ({@link String})</li>
     * </ul>
     */
    public MappingRule<String, Float> asFloat() {
        return MappingRules.catching(Float::parseFloat, (input, e) -> ErrorMessage.of("must.be.float", "value", input));
    }

    /**
     * Fails if the string is not a valid BigInteger.
     * <p>
     * Error key: {@code must.be.biginteger}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code value}: the input string ({@link String})</li>
     * </ul>
     */
    public MappingRule<String, BigInteger> asBigInteger() {
        return MappingRules.catching(BigInteger::new, (input, e) -> ErrorMessage.of("must.be.biginteger", "value", input));
    }

    /**
     * Fails if the string is not a valid BigDecimal.
     * <p>
     * Error key: {@code must.be.bigdecimal}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code value}: the input string ({@link String})</li>
     * </ul>
     */
    public MappingRule<String, BigDecimal> asBigDecimal() {
        return MappingRules.catching(BigDecimal::new, (input, e) -> ErrorMessage.of("must.be.bigdecimal", "value", input));
    }

    static final Set<String> TRUES = HashSet.of("TRUE", "1", "YES", "Y");

    /**
     * Converts a String into a boolean. Doesn't have any fail conditions.
     * Will consider "true","1","YES","Y" to be true values, anything else is considered false.
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code value}: the input string ({@link String})</li>
     * </ul>
     */
    public MappingRule<String, Boolean> asBoolean() {
        return MappingRules.catching(input -> TRUES.contains(input.toUpperCase()), "must.be.boolean");
    }

    /**
     * Fails if the string is not a valid UUID.
     * <p>
     * Error key: {@code must.be.uuid}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code value}: the input string ({@link String})</li>
     * </ul>
     */
    public MappingRule<String, UUID> asUUID() {
        return MappingRules.<String>notNull().then(input -> {
            try {
                return Validation.valid(UUID.fromString(input));
            } catch (IllegalArgumentException e) {
                return Validation.invalid(ErrorMessage.of("must.be.uuid", "value", input));
            }
        });
    }

    /**
     * Fails if the strings is not a valid URL.
     * <p>
     * Error key: {@code must.be.url}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code value}: the input string ({@link String})</li>
     * </ul>
     */
    public MappingRule<String, URL> asURL() {
        return MappingRules.<String>notNull().then(input -> {
            try {
                return Validation.valid(URI.create(input).toURL());
            } catch (Exception e) {
                return Validation.invalid(ErrorMessage.of("must.be.url", "value", input));
            }
        });
    }

    /**
     * Fails if the string is not a valid LocalDateTime in the specified format.
     * <p>
     * Error key: {@code must.be.localdatetime}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code value}: the input string ({@link String})</li>
     *     <li>{@code format}: the expected format ({@link String})</li>
     * </ul>
     *
     * @param format the expected date time format.
     * @see DateTimeFormatter#ofPattern(String)
     */
    public MappingRule<String, LocalDateTime> asLocalDateTime(String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return MappingRules.<String>notNull().then(input -> {
            try {
                return Validation.valid(LocalDateTime.parse(input, formatter));
            } catch (DateTimeParseException e) {
                return Validation.invalid(ErrorMessage.of("must.be.localdatetime",
                        HashMap.of("value", input, "format", format)));
            }
        });
    }

    /**
     * Fails if the string is not a valid LocalDateTime in ISO format (e.g. 2011-12-03T10:15:30)
     * <p>
     * Error key: {@code must.be.localdatetime}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code value}: the input string ({@link String})</li>
     * </ul>
     *
     * @see java.time.format.DateTimeFormatter#ISO_LOCAL_DATE_TIME
     * @see LocalDateTime#parse(CharSequence)
     */
    public MappingRule<String, LocalDateTime> asLocalDateTime() {
        return MappingRules.<String>notNull().then(input -> {
            try {
                return Validation.valid(LocalDateTime.parse(input));
            } catch (DateTimeParseException e) {
                return Validation.invalid(ErrorMessage.of("must.be.localdatetime", "value", input));
            }
        });
    }

    /**
     * Fails if the string is not a valid LocalDate in the specified format.
     * <p>
     * Error key: {@code must.be.localdate}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code value}: the input string ({@link String})</li>
     *     <li>{@code format}: the expected format ({@link String})</li>
     * </ul>
     *
     * @param format the expected date format.
     * @see DateTimeFormatter#ofPattern(String)
     */
    public MappingRule<String, LocalDate> asLocalDate(String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return MappingRules.<String>notNull().then(input -> {
            try {
                return Validation.valid(LocalDate.parse(input, formatter));
            } catch (DateTimeParseException e) {
                return Validation.invalid(ErrorMessage.of("must.be.localdate",
                        HashMap.of("value", input, "format", format)));
            }
        });
    }

    /**
     * Fails if the string is not a valid LocalDate in ISO format (e.g. 2011-12-03)
     * <p>
     * Error key: {@code must.be.localdate}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code value}: the input string ({@link String})</li>
     * </ul>
     *
     * @see java.time.format.DateTimeFormatter#ISO_LOCAL_DATE
     * @see LocalDateTime#parse(CharSequence)
     */
    public MappingRule<String, LocalDate> asLocalDate() {
        return MappingRules.<String>notNull().then(input -> {
            try {
                return Validation.valid(LocalDate.parse(input));
            } catch (DateTimeParseException e) {
                return Validation.invalid(ErrorMessage.of("must.be.localdate", "value", input));
            }
        });
    }

    /**
     * Fails if the string is not a valid Instant in the specified format.
     * <p>
     * Error key: {@code must.be.instant}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code value}: the input string ({@link String})</li>
     *     <li>{@code format}: the expected format ({@link String})</li>
     * </ul>
     *
     * @param format the expected instant format.
     * @see DateTimeFormatter#ofPattern(String)
     */
    public MappingRule<String, Instant> asInstant(String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format).withZone(java.time.ZoneOffset.UTC);
        return MappingRules.<String>notNull().then(input -> {
            try {
                return Validation.valid(Instant.from(formatter.parse(input)));
            } catch (java.time.DateTimeException e) {
                return Validation.invalid(ErrorMessage.of("must.be.instant",
                        HashMap.of("value", input, "format", format)));
            }
        });
    }

    /**
     * Fails if the string is not a valid Instant in ISO format (e.g. 2011-12-03T10:15:30Z)
     * <p>
     * Error key: {@code must.be.instant}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code value}: the input string ({@link String})</li>
     * </ul>
     *
     * @see java.time.format.DateTimeFormatter#ISO_INSTANT
     * @see LocalDateTime#parse(CharSequence)
     */
    public MappingRule<String, Instant> asInstant() {
        return MappingRules.<String>notNull().then(input -> {
            try {
                return Validation.valid(Instant.parse(input));
            } catch (DateTimeParseException e) {
                return Validation.invalid(ErrorMessage.of("must.be.instant", "value", input));
            }
        });
    }

    /**
     * Fails if the string is not a valid URI.
     * <p>
     * Error key: {@code must.be.uri}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code value}: the input string ({@link String})</li>
     * </ul>
     */
    public MappingRule<String, URI> asURI() {
        return MappingRules.<String>notNull().then(input -> {
            try {
                return Validation.valid(URI.create(input));
            } catch (IllegalArgumentException e) {
                return Validation.invalid(ErrorMessage.of("must.be.uri", "value", input));
            }
        });
    }

    /**
     * Fails if the input string is not a valid enum value for the given enum class while mapping to the enum. Is NOT case-sensitive.
     * <p>
     * Error key: {@code must.be.valid.enum.value}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code value}: the input string ({@link String})</li>
     * </ul>
     */
    public <E extends Enum<E>> MappingRule<String, E> asEnum(Class<E> enumClass) {
        return MappingRules.fromValidation(s -> {
                    for (E constant : enumClass.getEnumConstants()) {
                        if (constant.name().equalsIgnoreCase(s)) {
                            return Validation.valid(constant);
                        }
                    }
                    return Validation.invalid(ErrorMessage.of("must.be.valid.enum.value", "value", s));
                }
        );
    }

    /**
     * Fails if the input string is not a valid enum value for the given enum class.
     * <p>
     * Error key: {@code must.be.valid.enum.value}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code value}: the input string ({@link String})</li>
     * </ul>
     */
    public <E extends Enum<E>> Rule<String> canBeEnum(Class<E> clazz) {
        Objects.requireNonNull(clazz, "clazz cannot be null");
        return input -> Try.of(() -> Enum.valueOf(clazz, input))
                .fold(
                        f -> Validation.invalid(ErrorMessage.of("must.be.valid.enum.value", "value", input)),
                        v -> Validation.valid(input)
                );
    }

    //endregion

    // region parts of string

    /**
     * Takes a substrring of the string.
     * Fails if the indices are not valid for the input string.
     * <p>
     * Error key: {@code must.be.valid.substring}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code beginIndex}: the beginning index, inclusive ({@link Integer})</li>
     *     <li>{@code endIndex}: the ending index, exclusive ({@link Integer})</li>
     * </ul>
     *
     * @param beginIndex the beginning index, inclusive.
     * @param endIndex the ending index, exclusive.
     */
    public MappingRule<String, String> substring(int beginIndex, int endIndex) {
        return MappingRules.<String>notNull().then(input -> {
            if (beginIndex < 0 || endIndex > input.length() || beginIndex > endIndex) {
                return Validation.invalid(
                        ErrorMessage.of("must.be.valid.substring", HashMap.of("beginIndex", beginIndex, "endIndex", endIndex))
                );
            }
            return Validation.valid(input.substring(beginIndex, endIndex));
        });
    }

    /**
     * Takes the first N charachters of the string.
     * So "12345" take(2) is "12".
     * Fails if the string length is less than the requested length.
     * <p>
     * Error key: {@code must.be.valid.substring}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code length}: the number of characters to take ({@link Integer})</li>
     * </ul>
     *
     * @param length the number of characters to take from the start.
     */
    public MappingRule<String, String> take(int length) {
        return MappingRules.<String>notNull().then(input -> {
            if (length < 0 || length > input.length()) {
                return Validation.invalid(
                        ErrorMessage.of("must.be.valid.substring", HashMap.of("beginIndex", 0, "endIndex", length))
                );
            }
            return Validation.valid(input.substring(0, length));
        });
    }

    /**
     * Drops the first N characters from the string.
     * So "12345" drop(2) is "345".
     * Fails if the string length is less than the requested length.
     * <p>
     * Error key: {@code must.be.valid.substring}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code length}: the number of characters to drop ({@link Integer})</li>
     * </ul>
     *
     * @param length the number of characters to drop from the start.
     * @return a {@link MappingRule} that drops the first {@code length} characters.
     */
    public MappingRule<String, String> drop(int length) {
        return MappingRules.<String>notNull().then(input -> {
            if (length < 0 || length > input.length()) {
                return Validation.invalid(
                        ErrorMessage.of("must.be.valid.substring", HashMap.of("beginIndex", length))
                );
            }
            return Validation.valid(input.substring(length));
        });
    }

    /**
     * Takes the last N charachters of the string.
     * So "12345" takeRight(2) is "45".
     * Fails if the string length is less than the requested length.
     * <p>
     * Error key: {@code must.be.valid.substring}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code length}: the number of characters to take ({@link Integer})</li>
     * </ul>
     *
     * @param length the number of characters to take from the right.
     * @return a {@link MappingRule} that takes the last {@code length} characters.
     */
    public MappingRule<String, String> takeRight(int length) {
        return MappingRules.<String>notNull().then(input -> {
            if (length < 0 || length > input.length()) {
                return Validation.invalid(
                        ErrorMessage.of("must.be.valid.substring", HashMap.of("length", length))
                );
            }
            return Validation.valid(input.substring(input.length() - length));
        });
    }

    /**
     * Drops the last N charachters of the string.
     * So "12345" dropRight(2) is "123".
     * Fails if the string length is less than the requested length.
     * <p>
     * Error key: {@code must.be.valid.substring}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code length}: the number of characters to drop ({@link Integer})</li>
     * </ul>
     *
     * @param length the number of characters to drop from the right.
     * @return a {@link MappingRule} that drops the last {@code length} characters.
     */
    public MappingRule<String, String> dropRight(int length) {
        return MappingRules.<String>notNull().then(input -> {
            if (length < 0 || length > input.length()) {
                return Validation.invalid(
                        ErrorMessage.of("must.be.valid.substring", HashMap.of("length", length))
                );
            }
            return Validation.valid(input.substring(0, input.length() - length));
        });
    }


    //endregion

    //region whitespace related
    private static final Pattern LINE_BREAK = Pattern.compile("\\R");

    /**
     * Fails if the string contains any line break characters such as({@code \n}, {@code \r}, {{@code \u000B}). Basically any character that matches the \R regex.
     *
     * <p>
     * Error key: {@code must.be.single.line}
     */
    public Rule<String> singleLine() {
        return Rule.of(
                s -> !LINE_BREAK.matcher(s).find(),
                "must.be.single.line"
        );
    }

    /**
     * Fails if the string is empty.
     * <p>
     * Error key: {@code must.not.be.empty}
     */
    public Rule<String> notEmpty() {
        return Rule.of(
                s -> !s.isEmpty(),
                "must.not.be.empty"
        );
    }

    /**
     * Fails if the string is empty or contains only whitespace.
     * <p>
     * Error key: {@code must.not.be.blank}
     */
    public Rule<String> notBlank() {
        return Rule.of(
                s -> !s.isBlank(),
                "must.not.be.blank"
        );
    }

    /**
     * Fails if the string has leading or trailing whitespace.
     * <p>
     * Error key: {@code must.be.trimmed}
     */
    public Rule<String> trimmed() {
        return Rule.of(
                s -> s.equals(s.trim()),
                "must.be.trimmed"
        );
    }

    /**
     * Fails if the string contains any whitespace anywhere.
     * <p>
     * Error key: {@code must.not.contain.whitespace}
     */
    public Rule<String> noWhitespace() {
        return Rule.of(
                s -> s.chars().noneMatch(Character::isWhitespace),
                "must.not.contain.whitespace"
        );
    }
    //endregion

    //region case

    /**
     * Fails if the string contains any lowercase letter.
     * Uses {@link String#toUpperCase()} for comparison.
     * Empty strings pass.
     * <p>
     * Error key: {@code must.be.uppercase}
     */
    public Rule<String> uppercase() {
        return Rule.of(
                s -> s.equals(s.toUpperCase()),
                "must.be.uppercase"
        );
    }

    /**
     * Fails if the string contains any uppercase letter.
     * Uses {@link String#toLowerCase()} for comparison.
     * Empty strings pass.
     * <p>
     * Error key: {@code must.be.lowercase}
     */
    public Rule<String> lowercase() {
        return Rule.of(
                s -> s.equals(s.toLowerCase()),
                "must.be.lowercase"
        );
    }

    //endregion

    //region length related

    /**
     * Fails if the string length is less than the specified minimum.
     * <p>
     * Error key: {@code must.have.min.length}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code min}: the minimum allowed length ({@code int})</li>
     * </ul>
     *
     * @param minLength the minimum allowed length.
     * @return a {@link Rule} checking the minimum length.
     */
    public Rule<String> minLength(int minLength) {
        if (minLength < 0) {
            throw new IllegalArgumentException("minLength must be >= 0");
        }
        return Rule.of(
                s -> s.length() >= minLength,
                ErrorMessage.of("must.have.min.length", "min", minLength)
        );
    }

    /**
     * Fails if the string length is greater than the specified maximum.
     * <p>
     * Error key: {@code must.have.max.length}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code max}: the maximum allowed length ({@code int})</li>
     * </ul>
     *
     * @param maxLength the maximum allowed length.
     * @return a {@link Rule} checking the maximum length.
     */
    public Rule<String> maxLength(int maxLength) {
        if (maxLength < 0) {
            throw new IllegalArgumentException("maxLength must be >= 0");
        }
        return Rule.of(
                s -> s.length() <= maxLength,
                ErrorMessage.of("must.have.max.length", "max", maxLength)
        );
    }

    /**
     * Fails if the string length is not between the specified bounds (inclusive).
     * <p>
     * Error key: {@code must.have.length.between}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code min}: the minimum allowed length ({@code int})</li>
     *     <li>{@code max}: the maximum allowed length ({@code int})</li>
     * </ul>
     *
     * @param minLength the minimum allowed length (inclusive).
     * @param maxLength the maximum allowed length (inclusive).
     * @return a {@link Rule} checking the length range.
     */
    public Rule<String> lengthBetween(int minLength, int maxLength) {
        if (minLength < 0) {
            throw new IllegalArgumentException("minLength must be >= 0");
        }
        if (maxLength < 0) {
            throw new IllegalArgumentException("maxLength must be >= 0");
        }
        if (maxLength < minLength) {
            throw new IllegalArgumentException("maxLength must be >= minLength");
        }
        return Rule.notNull().and(Rule.of(
                s -> s.length() >= minLength && s.length() <= maxLength,
                ErrorMessage.of("must.have.length.between", HashMap.of("min", minLength, "max", maxLength))
        ));
    }

    /**
     * Fails if the string length is not equal to the specified length.
     * <p>
     * Error key: {@code must.have.length}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code length}: the required length ({@code int})</li>
     * </ul>
     *
     * @param length the required length.
     * @return a {@link Rule} checking the exact length.
     */
    public Rule<String> length(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length must be >= 0");
        }
        return Rule.of(
                s -> s.length() == length,
                ErrorMessage.of("must.have.length", "length", length)
        );
    }
    //endregion

    //region contains / starts / ends / matches

    /**
     * Fails if the string does not start with any of the specified prefixes.
     * <p>
     * Error key: {@code must.start.with}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code prefixes}: the allowed prefixes ({@link List<String>})</li>
     * </ul>
     *
     */
    public Rule<String> startsWith(String... prefixes) {
        Objects.requireNonNull(prefixes, "prefixes cannot be null");

        return Rule.of(
                s -> Arrays.stream(prefixes).anyMatch(s::startsWith),
                ErrorMessage.of("must.start.with", "prefixes", List.of(prefixes))
        );
    }

    /**
     * Fails if the string does not start with any of the specified prefixes (ignoring case).
     * <p>
     * Error key: {@code must.start.with.ignorecase}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code prefixes}: the allowed prefixes ({@link List<String>})</li>
     * </ul>
     *
     * @param prefixes the allowed prefixes.
     * @return a {@link Rule} checking the prefix (ignoring case).
     */
    public Rule<String> startsWithIgnoreCase(String... prefixes) {
        Objects.requireNonNull(prefixes, "prefixes cannot be null");
        return Rule.of(
                s -> Arrays.stream(prefixes).anyMatch(prefix -> s.regionMatches(true, 0, prefix, 0, prefix.length())),
                ErrorMessage.of("must.start.with.ignorecase", "prefixes", List.of(prefixes))
        );
    }

    /**
     * Fails if the string does not end with any of the specified suffixes.
     * <p>
     * Error key: {@code must.end.with}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code suffixes}: the allowed suffixes ({@link List<String>})</li>
     * </ul>
     *
     * @param suffixes allowed suffixes.
     * @return a {@link Rule} checking the suffix.
     */
    public Rule<String> endsWith(String... suffixes) {
        Objects.requireNonNull(suffixes, "suffixes cannot be null");

        return Rule.of(
                s -> Arrays.stream(suffixes).anyMatch(s::endsWith),
                ErrorMessage.of("must.end.with", "suffixes", List.of(suffixes))
        );
    }

    /**
     * Fails if the string does not end with the specified suffix (ignoring case).
     * <p>
     * Error key: {@code must.end.with.ignorecase}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code suffixes}: the allowed suffixes ({@link List<String>})</li>
     * </ul>
     */
    public Rule<String> endsWithIgnoreCase(String... suffixes) {
        Objects.requireNonNull(suffixes, "suffixes cannot be null");
        return Rule.of(
                s -> Arrays.stream(suffixes).anyMatch(suffix -> s.length() >= suffix.length()
                        && s.regionMatches(true, s.length() - suffix.length(), suffix, 0, suffix.length())),
                ErrorMessage.of("must.end.with.ignorecase", "suffixes", List.of(suffixes))
        );
    }

    /**
     * Fails if the string does not contain the specified fragment.
     * <p>
     * Error key: {@code must.contain}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code fragment}: the required fragment ({@link String})</li>
     * </ul>
     *
     * @param fragment the required fragment.
     * @return a {@link Rule} checking if the fragment is present.
     */
    public Rule<String> contains(String fragment) {
        Objects.requireNonNull(fragment, "fragment cannot be null");
        return Rule.of(
                s -> s.contains(fragment),
                ErrorMessage.of("must.contain", "fragment", fragment)
        );
    }

    /**
     * Fails if the string does not contain the specified fragment (ignoring case).
     * <p>
     * Error key: {@code must.contain.ignorecase}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code fragment}: the required fragment ({@link String})</li>
     * </ul>
     *
     * @param fragment the required fragment.
     * @return a {@link Rule} checking if the fragment is present (ignoring case).
     */
    public Rule<String> containsIgnoreCase(String fragment) {
        Objects.requireNonNull(fragment, "fragment cannot be null");

        return Rule.of(
                s -> {
                    int needleLen = fragment.length();
                    if (needleLen == 0) {
                        return true; // matches String.contains("")
                    }

                    int maxStart = s.length() - needleLen;
                    if (maxStart < 0) {
                        return false; // fragment longer than input can never match
                    }

                    for (int i = 0; i <= maxStart; i++) {
                        if (s.regionMatches(true, i, fragment, 0, needleLen)) {
                            return true;
                        }
                    }
                    return false;
                },
                ErrorMessage.of("must.contain.ignorecase", "fragment", fragment)
        );
    }

    /**
     * Fails if the string does not contain a match for the specified pattern.
     * <p>
     * Error key: {@code must.contain.regex}
     *
     * @param pattern the pattern to find.
     * @return a {@link Rule} checking if the pattern is found.
     */
    public Rule<String> containsPattern(Pattern pattern) {
        Objects.requireNonNull(pattern, "pattern cannot be null");
        return Rule.of(
                s -> pattern.matcher(s).find(),
                ErrorMessage.of("must.contain.regex", "regex", pattern.pattern())
        );
    }

    /**
     * Fails if the string contains the specified fragment.
     * <p>
     * Error key: {@code must.not.contain}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code fragment}: the forbidden fragment ({@link String})</li>
     * </ul>
     *
     * @param fragment the forbidden fragment.
     * @return a {@link Rule} checking if the fragment is absent.
     */
    public Rule<String> doesNotContain(String fragment) {
        Objects.requireNonNull(fragment, "fragment cannot be null");
        return Rule.of(
                s -> !s.contains(fragment),
                ErrorMessage.of("must.not.contain", "fragment", fragment)
        );
    }

    /**
     * Fails if the string is in the specified set of forbidden values.
     * <p>
     * Error key: {@code must.not.be.in}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code forbidden}: the set of forbidden values ({@link Set})</li>
     * </ul>
     *
     * @param forbidden the set of forbidden values.
     */
    public Rule<String> notIn(Set<String> forbidden) {
        Objects.requireNonNull(forbidden, "forbidden cannot be null");

        return Rule.of(
                s -> !forbidden.contains(s),
                ErrorMessage.of("must.not.be.in", "forbidden", forbidden)
        );
    }

    /**
     * Fails if the string is not in the specified set of allowed values.
     * <p>
     * Error key: {@code must.be.in}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code allowed}: the set of allowed values ({@link Set})</li>
     * </ul>
     *
     * @param allowed the set of allowed values.
     */
    public Rule<String> isIn(Set<String> allowed) {
        Objects.requireNonNull(allowed, "allowed cannot be null");
        return Rule.of(
                allowed::contains,
                ErrorMessage.of("must.be.in", "allowed", allowed)
        );
    }

    /**
     * Fails if the string does not match the specified regular expression.
     * <p>
     * Error key: {@code must.match.regex}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code regex}: the regular expression ({@link String})</li>
     * </ul>
     *
     * @param regex the regular expression.
     */
    public Rule<String> matches(String regex) {
        Objects.requireNonNull(regex, "regex cannot be null");
        Pattern pattern = Pattern.compile(regex);
        return Rule.of(
                s -> pattern.matcher(s).matches(),
                ErrorMessage.of("must.match.regex", "regex", regex)
        );
    }

    /**
     * Fails if the string does not match the specified regular expression.
     * <p>
     * Error key: {@code must.match.regex}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code regex}: the regular expression ({@link String})</li>
     * </ul>
     *
     * @param regex the regular expression.
     */
    public Rule<String> matches(Pattern regex) {
        Objects.requireNonNull(regex, "regex cannot be null");
        return Rule.of(
                s -> regex.matcher(s).matches(),
                ErrorMessage.of("must.match.regex", "regex", regex)
        );
    }

    /**
     * Fails if the string is not equal ignoring case the specified value.
     * <p>
     * Error key: {@code must.equal.ignoreCase}
     * Parameters:
     * <ul>
     *     <li>{@code value}: the value it should match</li>
     * </ul>
     */
    public Rule<String> equalsIgnoreCase(String expected) {
        return Rule.of(
                s -> s.equalsIgnoreCase(expected),
                ErrorMessage.of("must.equal.ignoreCase", "value", expected)
        );
    }

    /**
     * Fails if the string is equal ignoring case the specified value.
     * <p>
     * Error key: {@code must.not.equal.ignoreCase}
     * Parameters:
     * <ul>
     *     <li>{@code value}: the values must not match (({@link List<String>})</li>
     * </ul>
     */
    public Rule<String> notEqualsIgnoreCase(String... forbidden) {
        Objects.requireNonNull(forbidden, "forbidden can not be null");
        return Rule.of(
                s -> Arrays.stream(forbidden).noneMatch(s::equalsIgnoreCase),
                ErrorMessage.of("must.not.equal.ignoreCase", "value", List.of(forbidden))
        );
    }

    /**
     * Fails if the string contains anything other than letters.
     * Uses {@link Character#isLetter(int)} so it supports Unicode letters (not just A-Z).
     * <p>
     * Error key: {@code must.be.alpha}
     */
    public Rule<String> alpha() {
        return Rule.of(
                s -> s.codePoints().allMatch(Character::isLetter),
                "must.be.alpha"
        );
    }

    /**
     * Fails if the string contains anything other than letters or digits (ASCII).
     * <p>
     * Error key: {@code must.be.alphanumeric}
     */
    public Rule<String> alphaNumeric() {
        return Rule.of(
                s -> s.codePoints().allMatch(c ->
                        // ‘0–9’
                        (c >= 48 && c <= 57) ||
                                // ‘A–Z’
                                (c >= 65 && c <= 90) ||
                                // ‘a–z’
                                (c >= 97 && c <= 122)
                ),
                "must.be.alphanumeric"
        );
    }

    /**
     * Fails if the string contains anything other than letters or digits (Unicode).
     * Uses {@link Character#isLetterOrDigit(int)} so it supports Unicode letters/digits.
     * <p>
     * Error key: {@code must.be.unicode.alphanumeric}
     */
    public Rule<String> alphaNumericUnicode() {
        return Rule.of(
                s -> s.codePoints().allMatch(Character::isLetterOrDigit),
                "must.be.unicode.alphanumeric"
        );
    }

    /**
     * Fails if the string contains anything other than digits (Unicode).
     * Note: this accepts Unicode digits too (e.g. Arabic-Indic digits).
     * <p>
     * Error key: {@code must.be.unicode.digits.only}
     */
    public Rule<String> onlyUnicodeDigits() {
        return Rule.of(
                s -> s.codePoints().allMatch(Character::isDigit),
                "must.be.unicode.digits.only"
        );
    }

    /**
     * Fails if the string contains anything other than digits (0-9).
     * <p>
     * Error key: {@code must.be.digits.only}
     *
     * @return a {@link Rule} checking if the string contains only digits.
     */
    public Rule<String> onlyDigits() {
        return Rule.of(
                s -> s.codePoints().allMatch(c ->
                        // ‘0–9’
                        (c >= 48 && c <= 57)
                ),
                "must.be.digits.only"
        );
    }

    private static final Pattern HEXADECIMAL_PATTERN = Pattern.compile("[0-9a-f]*", Pattern.CASE_INSENSITIVE);

    /**
     * Fails if the string contains anything other than hexadecimal characters.
     * <p>
     * Error key: {@code must.be.hexadecimal}
     *
     * @return a {@link Rule} checking if the string is hexadecimal.
     */
    public Rule<String> hexadecimal() {
        return Rule.of(
                s -> HEXADECIMAL_PATTERN.matcher(s).matches(),
                ErrorMessage.of("must.be.hexadecimal")
        );
    }

    // Regex for standard Base64
    private static final Pattern STANDARD_BASE64_PATTERN =
            Pattern.compile("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$");

    // Regex for URL-safe Base64 (uses - and _ instead of + and /)
    private static final Pattern URL_SAFE_BASE64_PATTERN =
            Pattern.compile("^(?:[A-Za-z0-9-_]{4})*(?:[A-Za-z0-9-_]{2}==|[A-Za-z0-9-_]{3}=)?$");

    /**
     * Fails if the string is not valid Base64.
     * <p>
     * Error key: {@code must.be.base64}
     *
     * @return a {@link Rule} checking if the string is Base64.
     */
    public Rule<String> base64() {
        return Rule.of(
                s -> STANDARD_BASE64_PATTERN.matcher(s).matches(),
                ErrorMessage.of("must.be.base64")
        );
    }

    /**
     * Fails if the string is not valid URL-safe Base64.
     * <p>
     * Error key: {@code must.be.base64.urlsafe}
     *
     * @return a {@link Rule} checking if the string is URL-safe Base64.
     */
    public Rule<String> base64UrlSafe() {
        return Rule.of(
                s -> URL_SAFE_BASE64_PATTERN.matcher(s).matches(),
                ErrorMessage.of("must.be.base64.urlsafe")
        );
    }

    private static final Pattern IS_EMAIL_PATTERN = Pattern.compile(
            // local part
            "^[A-Za-z0-9+_.-]+@" +
                    // domain part = either:
                    //   (1) a standard domain with at least one dot
                    //   or
                    //   (2) a single label that starts with a letter/digit
                    "([A-Za-z0-9-]+\\.[A-Za-z0-9.-]*|[A-Za-z0-9][A-Za-z0-9-]*)$"
    );

    /**
     * Simplified rule to check if a string looks like an email address. Don't use this for full email validation.
     * The only valid email address is one you've received a confirmation from.
     * <p>
     * Error key: {@code must.be.email}
     */
    public Rule<String> looksLikeEmailAddress() {
        return Rule.of(
                s -> IS_EMAIL_PATTERN.matcher(s).matches(),
                "must.be.email"
        );
    }

    //endregion

}
