package net.vanfleteren.fv.rules;

import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;

import java.time.Duration;

/**
 * Validation rules for {@link Duration} values.
 */
public class DurationRules implements ComparableRules<Duration>, IObjectRules<Duration> {

    DurationRules() {
    }

    /**
     * Singleton instance of {@link DurationRules}.
     */
    public static final DurationRules durations = new DurationRules();

    /**
     * Returns the singleton instance of {@link DurationRules}.
     *
     * @return the {@link DurationRules} instance.
     */
    public static DurationRules durations() {
        return durations;
    }

    /**
     * Fails if the duration is less than the specified minimum (inclusive).
     * <p>
     * Error key: {@code must.be.at.least}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code min}: the minimum allowed duration ({@link Duration})</li>
     * </ul>
     *
     * @param min the minimum duration (inclusive).
     * @return a {@link Rule} checking if the duration is at least the minimum.
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
     *
     * @param max the maximum duration (inclusive).
     * @return a {@link Rule} checking if the duration is at most the maximum.
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
     *
     * @param limit the limit.
     * @return a {@link Rule} checking if the duration is before the limit.
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
     *
     * @param limit the limit.
     * @return a {@link Rule} checking if the duration is after the limit.
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
     *
     * @return a {@link Rule} checking if the duration is positive.
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
     *
     * @return a {@link Rule} checking if the duration is negative.
     */
    public Rule<Duration> isNegative() {
        return Rule.of(
                d -> d.compareTo(Duration.ZERO) < 0,
                ErrorMessage.of("must.be.negative")
        );
    }
}
