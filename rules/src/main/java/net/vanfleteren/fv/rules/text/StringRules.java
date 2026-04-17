package net.vanfleteren.fv.rules.text;

import io.vavr.collection.HashMap;
import io.vavr.collection.Set;
import io.vavr.control.Try;
import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.MappingRule;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.rules.ComparableRules;
import net.vanfleteren.fv.rules.IObjectRules;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    /**
     * Returns the singleton instance of {@link StringRules}.
     */
    public static StringRules strings() {
        return strings;
    }

    //region conversions

    /**
     * Fails if the string is not a valid integer.
     * <p>
     * Error key: {@code must.be.integer}
     *
     * @return a {@link MappingRule} that transforms a String into an Integer.
     */
    public MappingRule<String, Integer> asInteger() {
        return Rule.<String>notNull().andThen(MappingRule.of(Integer::parseInt, "must.be.integer"));
    }

    /**
     * Fails if the string is not a valid long.
     * <p>
     * Error key: {@code must.be.long}
     *
     * @return a {@link MappingRule} that transforms a String into a Long.
     */
    public MappingRule<String, Long> asLong() {
        return Rule.<String>notNull().andThen(MappingRule.of(Long::parseLong, "must.be.long"));
    }

    /**
     * Fails if the string is not a valid double.
     * <p>
     * Error key: {@code must.be.double}
     *
     * @return a {@link MappingRule} that transforms a String into a Double.
     */
    public MappingRule<String, Double> asDouble() {
        return Rule.<String>notNull().andThen(MappingRule.of(Double::parseDouble, "must.be.double"));
    }

    /**
     * Fails if the string is not a valid float.
     * <p>
     * Error key: {@code must.be.float}
     *
     * @return a {@link MappingRule} that transforms a String into a Float.
     */
    public MappingRule<String, Float> asFloat() {
        return Rule.<String>notNull().andThen(MappingRule.of(Float::parseFloat, "must.be.float"));
    }

    /**
     * Fails if the string is not a valid BigInteger.
     * <p>
     * Error key: {@code must.be.biginteger}
     *
     * @return a {@link MappingRule} that transforms a String into a BigInteger.
     */
    public MappingRule<String, BigInteger> asBigInteger() {
        return Rule.<String>notNull().andThen(MappingRule.of(BigInteger::new, "must.be.biginteger"));
    }

    /**
     * Fails if the string is not a valid BigDecimal.
     * <p>
     * Error key: {@code must.be.bigdecimal}
     *
     * @return a {@link MappingRule} that transforms a String into a BigDecimal.
     */
    public MappingRule<String, BigDecimal> asBigDecimal() {
        return Rule.<String>notNull().andThen(MappingRule.of(BigDecimal::new, "must.be.bigdecimal"));
    }


    /**
     * Fails if the string is not a valid UUID.
     * <p>
     * Error key: {@code must.be.uuid}
     *
     * @return a {@link MappingRule} that transforms a String into a UUID.
     */
    public MappingRule<String, UUID> asUUID() {
        return Rule.<String>notNull().andThen(MappingRule.of(UUID::fromString, "must.be.uuid"));
    }

    /**
     * Fails if the strings is not a valid URL.
     * <p>
     * Error key: {@code must.be.url}
     *
     * @return a {@link MappingRule} that transforms a String into a URL.
     */
    public MappingRule<String, URL> asURL() {
        return Rule.<String>notNull().andThen(MappingRule.ofTry(s -> Try.of(() -> URI.create(s).toURL()), "must.be.url"));
    }

    /**
     * Fails if the string is not a valid LocalDateTime in ISO format ( eg. 2011-12-03T10:15:30)
     *
     * <p>
     * Error key: {@code must.be.localdatetime}
     *
     * @return a {@link MappingRule} that transforms a String into a {@link LocalDateTime}.
     * @see java.time.format.DateTimeFormatter#ISO_LOCAL_DATE_TIME
     * @see LocalDateTime#parse(CharSequence)
     */
    public MappingRule<String, LocalDateTime> asLocalDateTime() {
        return Rule.<String>notNull().andThen(MappingRule.of(LocalDateTime::parse, "must.be.localdatetime"));
    }

    /**
     * Fails if the string is not a valid LocalDate in ISO format ( eg. 2011-12-03)
     *
     * <p>
     * Error key: {@code must.be.localdate}
     *
     * @return a {@link MappingRule} that transforms a String into a {@link LocalDate}.
     * @see java.time.format.DateTimeFormatter#ISO_LOCAL_DATE
     * @see LocalDateTime#parse(CharSequence)
     */
    public MappingRule<String, LocalDate> asLocalDate() {
        return Rule.<String>notNull().andThen(MappingRule.of(LocalDate::parse, "must.be.localdate"));
    }

    /**
     * Fails if the string is not a valid Instant in ISO format ( eg. 2011-12-03T10:15:30Z)
     *
     * <p>
     * Error key: {@code must.be.instant}
     *
     * @return a {@link MappingRule} that transforms a String into an {@link java.time.Instant}.
     * @see java.time.format.DateTimeFormatter#ISO_INSTANT
     * @see LocalDateTime#parse(CharSequence)
     */
    public MappingRule<String, Instant> asInstant() {
        return Rule.<String>notNull().andThen(MappingRule.of(Instant::parse, "must.be.instant"));
    }

    /**
     * Fails if the string is not a valid URI.
     * <p>
     * Error key: {@code must.be.uri}
     *
     * @return a {@link MappingRule} that transforms a String into a {@link URI}.
     */
    public MappingRule<String, URI> asURI() {
        return Rule.<String>notNull().andThen(MappingRule.of(URI::create, "must.be.uri"));
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
    public Rule<String> singleLine = Rule.notNull().and(Rule.of(s -> !LINE_BREAK.matcher(s).find(), "must.be.single.line"));

    /**
     * Fails if the string is empty.
     * <p>
     * Error key: {@code must.not.be.empty}
     */
    public Rule<String> notEmpty = Rule.notNull().and(Rule.of(s -> !s.isEmpty(), "must.not.be.empty"));

    /**
     * Fails if the string is empty or contains only whitespace.
     * <p>
     * Error key: {@code must.not.be.blank}
     */
    public Rule<String> notBlank = Rule.notNull().and(Rule.of(s -> !s.isBlank(), "must.not.be.blank"));

    /**
     * Fails if the string has leading or trailing whitespace.
     * <p>
     * Error key: {@code must.be.trimmed}
     */
    public Rule<String> trimmed = Rule.notNull().and(Rule.of(s -> s.equals(s.trim()), "must.be.trimmed"));

    /**
     * Fails if the string contains any whitespace anywhere.
     * <p>
     * Error key: {@code must.not.contain.whitespace}
     */
    public Rule<String> noWhitespace = Rule.notNull().and(Rule.of(
            s -> s.chars().noneMatch(Character::isWhitespace),
            "must.not.contain.whitespace"
    ));
    //endregion

    //region case

    /**
     * Fails if the string contains any lowercase letter.
     * Uses {@link String#toUpperCase()} for comparison, so locale-sensitive characters are supported.
     * Empty strings pass.
     * <p>
     * Error key: {@code must.be.uppercase}
     */
    public Rule<String> uppercase = Rule.notNull().and(Rule.of(
            s -> s.equals(s.toUpperCase()),
            "must.be.uppercase"
    ));

    /**
     * Fails if the string contains any uppercase letter.
     * Uses {@link String#toLowerCase()} for comparison, so locale-sensitive characters are supported.
     * Empty strings pass.
     * <p>
     * Error key: {@code must.be.lowercase}
     */
    public Rule<String> lowercase = Rule.notNull().and(Rule.of(
            s -> s.equals(s.toLowerCase()),
            "must.be.lowercase"
    ));

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
        return Rule.notNull().and(Rule.of(s -> s.length() >= minLength, ErrorMessage.of("must.have.min.length", "min", minLength)));
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
        return Rule.notNull().and(Rule.of(s -> s.length() <= maxLength, ErrorMessage.of("must.have.max.length", "max", maxLength)));
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
     * Error key: {@code must.have.exact.length}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code len}: the required length ({@code int})</li>
     * </ul>
     *
     * @param length the required length.
     * @return a {@link Rule} checking the exact length.
     */
    public Rule<String> exactLength(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length must be >= 0");
        }
        return Rule.notNull().and(Rule.of(s -> s.length() == length, ErrorMessage.of("must.have.exact.length", "len", length)));
    }
    //endregion

    //region contains / starts / ends / matches

    /**
     * Fails if the string does not start with the specified prefix.
     * <p>
     * Error key: {@code must.start.with}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code prefix}: the required prefix ({@link String})</li>
     * </ul>
     *
     * @param prefix the required prefix.
     * @return a {@link Rule} checking the prefix.
     */
    public Rule<String> startsWith(String prefix) {
        Objects.requireNonNull(prefix, "prefix cannot be null");
        return Rule.notNull().and(Rule.of(
                s -> s.startsWith(prefix),
                ErrorMessage.of("must.start.with", "prefix", prefix)
        ));
    }

    /**
     * Fails if the string does not start with the specified prefix (ignoring case).
     * <p>
     * Error key: {@code must.start.with.ignorecase}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code prefix}: the required prefix ({@link String})</li>
     * </ul>
     *
     * @param prefix the required prefix.
     * @return a {@link Rule} checking the prefix (ignoring case).
     */
    public Rule<String> startsWithIgnoreCase(String prefix) {
        Objects.requireNonNull(prefix, "prefix cannot be null");
        return Rule.notNull().and(Rule.of(
                s -> s.regionMatches(true, 0, prefix, 0, prefix.length()),
                ErrorMessage.of("must.start.with.ignorecase", "prefix", prefix)
        ));
    }

    /**
     * Fails if the string does not end with the specified suffix.
     * <p>
     * Error key: {@code must.end.with}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code suffix}: the required suffix ({@link String})</li>
     * </ul>
     *
     * @param suffix the required suffix.
     * @return a {@link Rule} checking the suffix.
     */
    public Rule<String> endsWith(String suffix) {
        Objects.requireNonNull(suffix, "suffix cannot be null");
        return Rule.notNull().and(Rule.of(
                s -> s.endsWith(suffix),
                ErrorMessage.of("must.end.with", "suffix", suffix)
        ));
    }

    /**
     * Fails if the string does not end with the specified suffix (ignoring case).
     * <p>
     * Error key: {@code must.end.with.ignorecase}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code suffix}: the required suffix ({@link String})</li>
     * </ul>
     *
     * @param suffix the required suffix.
     * @return a {@link Rule} checking the suffix (ignoring case).
     */
    public Rule<String> endsWithIgnoreCase(String suffix) {
        Objects.requireNonNull(suffix, "suffix cannot be null");
        return Rule.notNull().and(Rule.of(
                s -> s.length() >= suffix.length()
                        && s.regionMatches(true, s.length() - suffix.length(), suffix, 0, suffix.length()),
                ErrorMessage.of("must.end.with.ignorecase", "suffix", suffix)
        ));
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
        return Rule.notNull().and(Rule.of(
                s -> s.contains(fragment),
                ErrorMessage.of("must.contain", "fragment", fragment)
        ));
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

        return Rule.notNull().and(Rule.of(
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
        ));
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
        return Rule.notNull().and(Rule.of(
                s -> !s.contains(fragment),
                ErrorMessage.of("must.not.contain", "fragment", fragment)
        ));
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
     * @return a {@link Rule} checking if the value is forbidden.
     */
    public Rule<String> notIn(Set<String> forbidden) {
        Objects.requireNonNull(forbidden, "forbidden cannot be null");

        return Rule.notNull().and(Rule.of(
                s -> !forbidden.contains(s),
                ErrorMessage.of("must.not.be.in", "forbidden", forbidden)
        ));
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
     * @return a {@link Rule} checking if the value is allowed.
     */
    public Rule<String> isIn(Set<String> allowed) {
        Objects.requireNonNull(allowed, "allowed cannot be null");
        return Rule.notNull().and(Rule.of(
                allowed::contains,
                ErrorMessage.of("must.be.in", "allowed", allowed)
        ));
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
     * @return a {@link Rule} checking if the string matches the regex.
     */
    public Rule<String> matches(String regex) {
        Objects.requireNonNull(regex, "regex cannot be null");
        Pattern pattern = Pattern.compile(regex);
        return Rule.notNull().and(Rule.of(
                s -> pattern.matcher(s).matches(),
                ErrorMessage.of("must.match.regex", "regex", regex)
        ));
    }

    /**
     * Fails if the string contains anything other than letters.
     * Uses {@link Character#isLetter(int)} so it supports Unicode letters (not just A-Z).
     * <p>
     * Error key: {@code must.be.alpha}
     */
    public Rule<String> alpha = Rule.notNull().and(Rule.of(
            s -> s.codePoints().allMatch(Character::isLetter),
            "must.be.alpha"
    ));

    /**
     * Fails if the string contains anything other than letters or digits (ASCII).
     * <p>
     * Error key: {@code must.be.alphanumeric}
     */
    public Rule<String> alphaNumeric = Rule.notNull().and(Rule.of(
            s -> s.codePoints().allMatch(c ->
                    // ‘0–9’
                    (c >= 48 && c <= 57) ||
                            // ‘A–Z’
                            (c >= 65 && c <= 90) ||
                            // ‘a–z’
                            (c >= 97 && c <= 122)
            ),
            "must.be.alphanumeric"
    ));

    /**
     * Fails if the string contains anything other than letters or digits (Unicode).
     * Uses {@link Character#isLetterOrDigit(int)} so it supports unicode letters/digits.
     * <p>
     * Error key: {@code must.be.unicode.alphanumeric}
     */
    public Rule<String> alphaNumericUnicode = Rule.notNull().and(Rule.of(
            s -> s.codePoints().allMatch(Character::isLetterOrDigit),
            "must.be.unicode.alphanumeric"
    ));

    /**
     * Fails if the string contains anything other than digits (Unicode).
     * Note: this accepts Unicode digits too (e.g. Arabic-Indic digits).
     * <p>
     * Error key: {@code must.be.unicode.digits.only}
     */
    public Rule<String> onlyUnicodeDigits = Rule.notNull().and(Rule.of(
            s -> s.codePoints().allMatch(Character::isDigit),
            "must.be.unicode.digits.only"
    ));

    /**
     * Fails if the string contains anything other than digits (0-9).
     * <p>
     * Error key: {@code must.be.digits.only}
     *
     * @return a {@link Rule} checking if the string contains only digits.
     */
    public Rule<String> onlyDigits() {
        return Rule.notNull().and(Rule.of(s -> s.codePoints().allMatch(c ->
                        // ‘0–9’
                        (c >= 48 && c <= 57)
                ),
                "must.be.digits.only"
        ));
    }

    /**
     * Fails if the string contains anything other than hexadecimal characters.
     * <p>
     * Error key: {@code must.be.hexadecimal}
     *
     * @return a {@link Rule} checking if the string is hexadecimal.
     */
    public Rule<String> hexadecimal() {
        Pattern p = Pattern.compile("[0-9a-f]*", Pattern.CASE_INSENSITIVE);
        return Rule.notNull().and(Rule.of(s -> p.matcher(s).matches(), ErrorMessage.of("must.be.hexadecimal")));
    }


    private static final Pattern isEmailPattern = Pattern.compile(
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
        return Rule.notNull().and(Rule.of(
                s -> isEmailPattern.matcher(s).matches(),
                "must.be.email"
        ));
    }

    //endregion


}
