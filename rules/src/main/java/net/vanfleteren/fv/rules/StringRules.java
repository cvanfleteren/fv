package net.vanfleteren.fv.rules;

import io.vavr.collection.HashMap;
import io.vavr.collection.Set;
import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;

import java.util.Objects;
import java.util.regex.Pattern;

public class StringRules implements ComparableRules<String>, IObjectRules<String> {

    public static final StringRules strings = new StringRules();

    public static StringRules strings() {
        return strings;
    }

    //region whitespace related
    public Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "not.empty");

    /**
     * Fails if the string is empty or contains only whitespace.
     */
    public Rule<String> notBlank = Rule.of(s -> !s.isBlank(), "not.blank");

    /**
     * Fails if the string has leading or trailing whitespace.
     */
    public Rule<String> trimmed = Rule.of(s -> s.equals(s.trim()), "must.be.trimmed");

    /**
     * Fails if the string contains any whitespace anywhere.
     */
    public Rule<String> noWhitespace = Rule.of(
            s -> s.chars().noneMatch(Character::isWhitespace),
            "no.whitespace.allowed"
    );
    //endregion

    //region length related
    public Rule<String> minLength(int minLength) {
        if (minLength < 0) {
            throw new IllegalArgumentException("minLength must be >= 0");
        }
        return Rule.of(s -> s.length() >= minLength, ErrorMessage.of("min.length", "min", minLength));
    }

    public Rule<String> maxLength(int maxLength) {
        if (maxLength < 0) {
            throw new IllegalArgumentException("maxLength must be >= 0");
        }
        return Rule.of(s -> s.length() <= maxLength, ErrorMessage.of("max.length", "max", maxLength));
    }

    /**
     * Inclusive bounds.
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
        return Rule.of(
                s -> s.length() >= minLength && s.length() <= maxLength,
                ErrorMessage.of("length.between", HashMap.of("min", minLength, "max", maxLength))
        );
    }

    public Rule<String> exactLength(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length must be >= 0");
        }
        return Rule.of(s -> s.length() == length, ErrorMessage.of("length.exact", "len", length));
    }
    //endregion

    //region contains / starts / ends / matches
    public Rule<String> startsWith(String prefix) {
        Objects.requireNonNull(prefix, "prefix cannot be null");
        return Rule.of(
                s -> s.startsWith(prefix),
                ErrorMessage.of("must.start.with", "prefix", prefix)
        );
    }

    public Rule<String> startsWithIgnoreCase(String prefix) {
        Objects.requireNonNull(prefix, "prefix cannot be null");
        return Rule.of(
                s -> s.regionMatches(true, 0, prefix, 0, prefix.length()),
                ErrorMessage.of("must.start.with.ignorecase", "prefix", prefix)
        );
    }

    public Rule<String> endsWith(String suffix) {
        Objects.requireNonNull(suffix, "suffix cannot be null");
        return Rule.of(
                s -> s.endsWith(suffix),
                ErrorMessage.of("must.end.with", "suffix", suffix)
        );
    }

    public Rule<String> endsWithIgnoreCase(String suffix) {
        Objects.requireNonNull(suffix, "suffix cannot be null");
        return Rule.of(
                s -> s.length() >= suffix.length()
                        && s.regionMatches(true, s.length() - suffix.length(), suffix, 0, suffix.length()),
                ErrorMessage.of("must.end.with.ignorecase", "suffix", suffix)
        );
    }

    public Rule<String> contains(String fragment) {
        Objects.requireNonNull(fragment, "fragment cannot be null");
        return Rule.of(
                s -> s.contains(fragment),
                ErrorMessage.of("must.contain", "fragment", fragment)
        );
    }

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

    public Rule<String> notIn(Set<String> forbidden) {
        Objects.requireNonNull(forbidden, "forbidden cannot be null");

        return Rule.of(
                s -> !forbidden.contains(s),
                ErrorMessage.of("must.not.be.in", "forbidden", forbidden)
        );
    }

    public Rule<String> matches(String regex) {
        Objects.requireNonNull(regex, "regex cannot be null");
        Pattern pattern = Pattern.compile(regex);
        return Rule.of(
                s -> pattern.matcher(s).matches(),
                ErrorMessage.of("must.match.regex", "regex", regex)
        );
    }

    /**
     * Fails if the string contains anything other than letters.
     * Uses {@link Character#isLetter(int)} so it supports unicode letters (not just A-Z).
     */
    public Rule<String> alpha = Rule.of(
            s -> s.codePoints().allMatch(Character::isLetter),
            "must.be.alpha"
    );

    /**
     * Fails if the string contains anything other than letters or digits.
     * Uses {@link Character#isLetterOrDigit(int)} so it supports unicode letters/digits.
     */
    public Rule<String> alphaNumeric = Rule.of(
            s -> s.codePoints().allMatch(Character::isLetterOrDigit),
            "must.be.alphanumeric"
    );

    /**
     * Fails if the string contains anything other than digits.
     * Note: this accepts unicode digits too (e.g. Arabic-Indic digits).
     */
    public Rule<String> onlyUnicodeDigits = Rule.of(
            s -> s.codePoints().allMatch(Character::isDigit),
            "must.be.digits.only"
    );

    /**
     * Fails if the string contains anything other than digits.
     * Note: this doesn't accept unicode digits, only 0-9.
     */
    public Rule<String> onlyDigits() {
        Pattern p = Pattern.compile("[0-9]*");
        return Rule.of(s -> p.matcher(s).matches(), ErrorMessage.of("must.be.ascii.digits.only"));
    }

    public Rule<String> hexadecimal() {
        Pattern p = Pattern.compile("[0-9a-f]*", Pattern.CASE_INSENSITIVE);
        return Rule.of(s -> p.matcher(s).matches(), ErrorMessage.of("must.be.hexadecimal"));
    }



    //endregion



}
