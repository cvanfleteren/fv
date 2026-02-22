package net.vanfleteren.fv.rules;

import io.vavr.collection.HashMap;
import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;

import java.util.Objects;

public class StringRules {

    //region whitespace related
    public static final Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "not.empty");

    /**
     * Fails if the string is empty or contains only whitespace.
     */
    public static final Rule<String> notBlank = Rule.of(s -> !s.isBlank(), "not.blank");

    /**
     * Fails if the string has leading or trailing whitespace.
     */
    public static final Rule<String> trimmed = Rule.of(s -> s.equals(s.trim()), "must.be.trimmed");

    /**
     * Fails if the string contains any whitespace anywhere.
     */
    public static final Rule<String> noWhitespace = Rule.of(
            s -> s.chars().noneMatch(Character::isWhitespace),
            "no.whitespace.allowed"
    );
    //endregion

    //region length related
    public static Rule<String> minLength(int minLength) {
        if (minLength < 0) {
            throw new IllegalArgumentException("minLength must be >= 0");
        }
        return Rule.of(s -> s.length() >= minLength, ErrorMessage.of("min.length", "min", minLength));
    }

    public static Rule<String> maxLength(int maxLength) {
        if (maxLength < 0) {
            throw new IllegalArgumentException("maxLength must be >= 0");
        }
        return Rule.of(s -> s.length() <= maxLength, ErrorMessage.of("max.length", "max", maxLength));
    }

    /**
     * Inclusive bounds.
     */
    public static Rule<String> lengthBetween(int minLength, int maxLength) {
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

    public static Rule<String> exactLength(int length) {
        if (length < 0) {
            throw new IllegalArgumentException("length must be >= 0");
        }
        return Rule.of(s -> s.length() == length, ErrorMessage.of("length.exact", "len", length));
    }
    //endregion

    //region contains / starts / ends
    public static Rule<String> contains(String fragment) {
        Objects.requireNonNull(fragment, "fragment cannot be null");
        return Rule.of(
                s -> s.contains(fragment),
                ErrorMessage.of("must.contain", "fragment", fragment)
        );
    }

    public static Rule<String> startsWith(String prefix) {
        Objects.requireNonNull(prefix, "prefix cannot be null");
        return Rule.of(
                s -> s.startsWith(prefix),
                ErrorMessage.of("must.start.with", "prefix", prefix)
        );
    }

    public static Rule<String> endsWith(String suffix) {
        Objects.requireNonNull(suffix, "suffix cannot be null");
        return Rule.of(
                s -> s.endsWith(suffix),
                ErrorMessage.of("must.end.with", "suffix", suffix)
        );
    }
    //endregion



}
