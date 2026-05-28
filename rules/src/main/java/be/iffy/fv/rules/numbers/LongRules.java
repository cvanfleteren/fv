package be.iffy.fv.rules.numbers;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.rules.ComparableRules;
import be.iffy.fv.rules.IObjectRules;

/**
 * Validation rules for {@link Long} values.
 */
public class LongRules implements ComparableRules<Long>, NumberRules<Long>, IObjectRules<Long> {

    /**
     * Singleton instance of {@link LongRules}.
     */
    public static final LongRules longs = new LongRules();

    //region sign related
    /**
     * Fails if the {@link Long} is not positive (not greater than zero).
     * <p>
     * Error key: {@code must.be.positive}
     */
    @Override
    public Rule<Long> positive() {
        return Rule.of(
                l -> l > 0,
                "must.be.positive"
        );
    }

    /**
     * Fails if the {@link Long} is negative (less than zero).
     * <p>
     * Error key: {@code must.be.non.negative}
     */
    @Override
    public Rule<Long> nonNegative() {
        return Rule.of(
                l -> l >= 0,
                "must.be.non.negative"
        );
    }

    /**
     * Fails if the {@link Long} is not negative (not less than zero).
     * <p>
     * Error key: {@code must.be.negative}
     */
    @Override
    public Rule<Long> negative() {
        return Rule.of(
                l -> l < 0,
                "must.be.negative"
        );
    }

    /**
     * Fails if the {@link Long} is positive (greater than zero).
     * <p>
     * Error key: {@code must.be.non.positive}
     */
    @Override
    public Rule<Long> nonPositive() {
        return Rule.of(
                l -> l <= 0,
                "must.be.non.positive"
        );
    }

    /**
     * Fails if the {@link Long} is not zero.
     * <p>
     * Error key: {@code must.be.zero}
     */
    @Override
    public Rule<Long> zero() {
        return Rule.of(
                l -> l == 0,
                "must.be.zero"
        );
    }

    /**
     * Fails if the {@link Long} is zero.
     * <p>
     * Error key: {@code must.not.be.zero}
     */
    @Override
    public Rule<Long> nonZero() {
        return Rule.of(
                l -> l != 0,
                "must.not.be.zero"
        );
    }
    //endregion

    //region parity related
    /**
     * Fails if the {@link Long} is not odd.
     * <p>
     * Error key: {@code must.be.odd}
     */
    public Rule<Long> odd() {
        return Rule.of(
                l -> (l & 1) != 0,
                "must.be.odd"
        );
    }

    /**
     * Fails if the {@link Long} is not even.
     * <p>
     * Error key: {@code must.be.even}
     */
    public Rule<Long> even() {
        return Rule.of(
                l -> (l & 1) == 0,
                "must.be.even"
        );
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
     */
    public Rule<Long> max(long maxInclusive) {
        return Rule.of(
                l -> l <= maxInclusive,
                ErrorMessage.of("must.be.at.most", "max", maxInclusive)
        );
    }
    //endregion

}

