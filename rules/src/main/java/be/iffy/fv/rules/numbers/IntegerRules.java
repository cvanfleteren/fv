package be.iffy.fv.rules.numbers;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.rules.ComparableRules;
import be.iffy.fv.rules.IObjectRules;

import java.util.Objects;

/**
 * Validation rules for {@link Integer} values.
 */
public final class IntegerRules implements ComparableRules<Integer>, NumberRules<Integer>, IObjectRules<Integer> {

    /**
     * Singleton instance of {@link IntegerRules}.
     */
    public static final IntegerRules ints = new IntegerRules();

    //region sign related

    /**
     * Fails if the {@link Integer} is not positive (not greater than zero).
     * <p>
     * Error key: {@code must.be.positive}
     */
    @Override
    public Rule<Integer> positive() {
        return Rule.of(
                i -> i > 0,
                "must.be.positive"
        );
    }

    /**
     * Fails if the {@link Integer} is negative (less than zero).
     * <p>
     * Error key: {@code must.be.non.negative}
     */
    @Override
    public Rule<Integer> nonNegative() {
        return Rule.of(
                i -> i >= 0,
                "must.be.non.negative"
        );
    }

    /**
     * Fails if the {@link Integer} is not negative (not less than zero).
     * <p>
     * Error key: {@code must.be.negative}
     */
    @Override
    public Rule<Integer> negative() {
        return Rule.of(
                i -> i < 0,
                "must.be.negative"
        );
    }

    /**
     * Fails if the {@link Integer} is positive (greater than zero).
     * <p>
     * Error key: {@code must.be.non.positive}
     */
    @Override
    public Rule<Integer> nonPositive() {
        return Rule.of(
                i -> i <= 0,
                "must.be.non.positive"
        );
    }

    /**
     * Fails if the {@link Integer} is not zero.
     * <p>
     * Error key: {@code must.be.zero}
     */
    @Override
    public Rule<Integer> zero() {
        return Rule.of(
                i -> i == 0,
                "must.be.zero"
        );
    }

    /**
     * Fails if the {@link Integer} is zero.
     * <p>
     * Error key: {@code must.not.be.zero}
     */
    @Override
    public Rule<Integer> nonZero() {
        return Rule.of(
                i -> i != 0,
                "must.not.be.zero"
        );
    }
    //endregion

    //region parity related

    /**
     * Fails if the {@link Integer} is not odd.
     * <p>
     * Error key: {@code must.be.odd}
     */
    public Rule<Integer> odd() {
        return Rule.of(
                i -> (i & 1) != 0,
                "must.be.odd"
        );
    }

    /**
     * Fails if the {@link Integer} is not even.
     * <p>
     * Error key: {@code must.be.even}
     */
    public Rule<Integer> even() {
        return Rule.of(
                i -> (i & 1) == 0,
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
     *     <li>{@code min}: the minimum allowed value ({@code int})</li>
     * </ul>
     *
     * @param minInclusive the minimum allowed value (inclusive).
     */
    public Rule<Integer> min(int minInclusive) {
        return Rule.of(
                i -> i >= minInclusive,
                ErrorMessage.of("must.be.at.least", "min", minInclusive)
        );
    }

    @Override
    public Rule<Integer> min(Integer minInclusive) {
        Objects.requireNonNull(minInclusive, "minInclusive cannot be null");
        return min(minInclusive.intValue());
    }

    /**
     * Fails if the value is greater than the specified maximum.
     * <p>
     * Error key: {@code must.be.at.most}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code max}: the maximum allowed value ({@code int})</li>
     * </ul>
     *
     * @param maxInclusive the maximum allowed value (inclusive).
     */
    public Rule<Integer> max(int maxInclusive) {
        return Rule.of(
                i -> i <= maxInclusive,
                ErrorMessage.of("must.be.at.most", "max", maxInclusive)
        );
    }

    @Override
    public Rule<Integer> max(Integer maxInclusive) {
        Objects.requireNonNull(maxInclusive, "maxInclusive cannot be null");
        return max(maxInclusive.intValue());
    }
    //endregion

}

