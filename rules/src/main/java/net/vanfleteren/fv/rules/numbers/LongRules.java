package net.vanfleteren.fv.rules.numbers;

import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.rules.ComparableRules;
import net.vanfleteren.fv.rules.IObjectRules;

/**
 * Validation rules for {@link Long} values.
 */
public class LongRules implements ComparableRules<Long>, NumberRules<Long>, IObjectRules<Long> {

    /**
     * Singleton instance of {@link LongRules}.
     */
    public static final LongRules longs = new LongRules();

    /**
     * Returns the singleton instance of {@link LongRules}.
     */
    public static LongRules longs() {
        return longs;
    }

    //region sign related
    /**
     * Fails if the {@link Long} is not positive (not greater than zero).
     * <p>
     * Error key: {@code must.be.positive}
     *
     * @return a {@link Rule} checking for positive values.
     */
    @Override
    public Rule<Long> positive() {
        return Rule.of(l -> l > 0, "must.be.positive");
    }

    /**
     * Fails if the {@link Long} is negative (less than zero).
     * <p>
     * Error key: {@code must.be.non.negative}
     *
     * @return a {@link Rule} checking for non-negative values.
     */
    @Override
    public Rule<Long> nonNegative() {
        return Rule.of(l -> l >= 0, "must.be.non.negative");
    }

    /**
     * Fails if the {@link Long} is not negative (not less than zero).
     * <p>
     * Error key: {@code must.be.negative}
     *
     * @return a {@link Rule} checking for negative values.
     */
    @Override
    public Rule<Long> negative() {
        return Rule.of(l -> l < 0, "must.be.negative");
    }

    /**
     * Fails if the {@link Long} is positive (greater than zero).
     * <p>
     * Error key: {@code must.be.non.positive}
     *
     * @return a {@link Rule} checking for non-positive values.
     */
    @Override
    public Rule<Long> nonPositive() {
        return Rule.of(l -> l <= 0, "must.be.non.positive");
    }

    /**
     * Fails if the {@link Long} is not zero.
     * <p>
     * Error key: {@code must.be.zero}
     *
     * @return a {@link Rule} checking for zero values.
     */
    @Override
    public Rule<Long> zero() {
        return Rule.of(l -> l == 0, "must.be.zero");
    }

    /**
     * Fails if the {@link Long} is zero.
     * <p>
     * Error key: {@code must.not.be.zero}
     *
     * @return a {@link Rule} checking for non-zero values.
     */
    @Override
    public Rule<Long> nonZero() {
        return Rule.of(l -> l != 0, "must.not.be.zero");
    }
    //endregion

    //region parity related
    /**
     * Fails if the {@link Long} is not odd.
     * <p>
     * Error key: {@code must.be.odd}
     *
     * @return a {@link Rule} checking for odd values.
     */
    public Rule<Long> odd() {
        return Rule.of(l -> (l & 1) != 0, "must.be.odd");
    }

    /**
     * Fails if the {@link Long} is not even.
     * <p>
     * Error key: {@code must.be.even}
     *
     * @return a {@link Rule} checking for even values.
     */
    public Rule<Long> even() {
        return Rule.of(l -> (l & 1) == 0, "must.be.even");
    }
    //endregion

    //region comparisons
    /**
     * Fails if the value is less than the specified minimum.
     * <p>
     * Error key: {@code must.be.at.least}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code min}: the minimum allowed value ({@code long})</li>
     * </ul>
     *
     * @param minInclusive the minimum allowed value (inclusive).
     * @return a {@link Rule} checking the minimum value.
     */
    public Rule<Long> min(long minInclusive) {
        return Rule.of(
                l -> l >= minInclusive,
                ErrorMessage.of("must.be.at.least", "min", minInclusive)
        );
    }

    /**
     * Fails if the value is greater than the specified maximum.
     * <p>
     * Error key: {@code must.be.at.most}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code max}: the maximum allowed value ({@code long})</li>
     * </ul>
     *
     * @param maxInclusive the maximum allowed value (inclusive).
     * @return a {@link Rule} checking the maximum value.
     */
    public Rule<Long> max(long maxInclusive) {
        return Rule.of(
                l -> l <= maxInclusive,
                ErrorMessage.of("must.be.at.most", "max", maxInclusive)
        );
    }
    //endregion

}

