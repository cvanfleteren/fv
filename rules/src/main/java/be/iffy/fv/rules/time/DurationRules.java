package be.iffy.fv.rules.time;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.rules.ComparableRules;
import be.iffy.fv.rules.IObjectRules;

import java.time.Duration;

/**
 * Validation rules for {@link Duration} values.
 */
public final class DurationRules implements ComparableRules<Duration>, IObjectRules<Duration> {

    /**
     * Singleton instance of {@link DurationRules}.
     */
    public static final DurationRules durations = new DurationRules();

    /**
     * Fails if the duration is less than the specified minimum (inclusive).
     * <p>
     * Error key: {@code must.be.at.least}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code min}: the minimum allowed duration ({@link Duration})</li>
     * </ul>
     */
    public Rule<Duration> isAtLeast(Duration min) {
        return Rule.of(
                d -> d.compareTo(min) >= 0,
                ErrorMessage.of("must.be.at.least", "min", min)
        );
    }

    /**
     * Fails if the duration is greater than the specified maximum (inclusive).
     * <p>
     * Error key: {@code must.be.at.most}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code max}: the maximum allowed duration ({@link Duration})</li>
     * </ul>
     */
    public Rule<Duration> isAtMost(Duration max) {
        return Rule.of(
                d -> d.compareTo(max) <= 0,
                ErrorMessage.of("must.be.at.most", "max", max)
        );
    }

    /**
     * Fails if the duration is not shorter than the specified limit.
     * <p>
     * Error key: {@code must.be.shorter}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code limit}: the limit ({@link Duration})</li>
     * </ul>
     */
    public Rule<Duration> isShorterThan(Duration limit) {
        return Rule.of(
                d -> d.compareTo(limit) < 0,
                ErrorMessage.of("must.be.shorter", "limit", limit)
        );
    }

    /**
     * Fails if the duration is not longer than the specified limit.
     * <p>
     * Error key: {@code must.be.longer}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code limit}: the limit ({@link Duration})</li>
     * </ul>
     */
    public Rule<Duration> isLongerThan(Duration limit) {
        return Rule.of(
                d -> d.compareTo(limit) > 0,
                ErrorMessage.of("must.be.longer", "limit", limit)
        );
    }

    /**
     * Fails if the duration is not positive.
     * <p>
     * Error key: {@code must.be.positive}
     */
    public Rule<Duration> isPositive() {
        return Rule.of(
                d -> d.compareTo(Duration.ZERO) > 0,
                ErrorMessage.of("must.be.positive")
        );
    }

    /**
     * Fails if the duration is not negative.
     * <p>
     * Error key: {@code must.be.negative}
     */
    public Rule<Duration> isNegative() {
        return Rule.of(
                d -> d.compareTo(Duration.ZERO) < 0,
                ErrorMessage.of("must.be.negative")
        );
    }
}
