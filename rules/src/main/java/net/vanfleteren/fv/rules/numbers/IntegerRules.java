package net.vanfleteren.fv.rules.numbers;

import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.rules.ComparableRules;
import net.vanfleteren.fv.rules.IObjectRules;

/**
 * Validation rules for {@link Integer} values.
 */
public class IntegerRules implements ComparableRules<Integer>, NumberRules<Integer>, IObjectRules<Integer> {

    /**
     * Singleton instance of {@link IntegerRules}.
     */
    public static final IntegerRules ints = new IntegerRules();

    /**
     * Returns the singleton instance of {@link IntegerRules}.
     */
    public static IntegerRules ints() {
        return ints;
    }

    //region sign related
    /**
     * Fails if the {@link Integer} is not positive (not greater than zero).
     * <p>
     * Error key: {@code must.be.positive}
     *
     * @return a {@link Rule} checking for positive values.
     */
    @Override
    public Rule<Integer> positive() {
        return Rule.of(i -> i > 0, "must.be.positive");
    }

    /**
     * Fails if the {@link Integer} is negative (less than zero).
     * <p>
     * Error key: {@code must.be.non.negative}
     *
     * @return a {@link Rule} checking for non-negative values.
     */
    @Override
    public Rule<Integer> nonNegative() {
        return Rule.of(i -> i >= 0, "must.be.non.negative");
    }

    /**
     * Fails if the {@link Integer} is not negative (not less than zero).
     * <p>
     * Error key: {@code must.be.negative}
     *
     * @return a {@link Rule} checking for negative values.
     */
    @Override
    public Rule<Integer> negative() {
        return Rule.of(i -> i < 0, "must.be.negative");
    }

    /**
     * Fails if the {@link Integer} is positive (greater than zero).
     * <p>
     * Error key: {@code must.be.non.positive}
     *
     * @return a {@link Rule} checking for non-positive values.
     */
    @Override
    public Rule<Integer> nonPositive() {
        return Rule.of(i -> i <= 0, "must.be.non.positive");
    }

    /**
     * Fails if the {@link Integer} is not zero.
     * <p>
     * Error key: {@code must.be.zero}
     *
     * @return a {@link Rule} checking for zero values.
     */
    @Override
    public Rule<Integer> zero() {
        return Rule.of(i -> i == 0, "must.be.zero");
    }

    /**
     * Fails if the {@link Integer} is zero.
     * <p>
     * Error key: {@code must.not.be.zero}
     *
     * @return a {@link Rule} checking for non-zero values.
     */
    @Override
    public Rule<Integer> nonZero() {
        return Rule.of(i -> i != 0, "must.not.be.zero");
    }
    //endregion

    //region parity related
    /**
     * Fails if the {@link Integer} is not odd.
     * <p>
     * Error key: {@code must.be.odd}
     *
     * @return a {@link Rule} checking for odd values.
     */
    public Rule<Integer> odd() {
        return Rule.of(i -> (i & 1) != 0, "must.be.odd");
    }

    /**
     * Fails if the {@link Integer} is not even.
     * <p>
     * Error key: {@code must.be.even}
     *
     * @return a {@link Rule} checking for even values.
     */
    public Rule<Integer> even() {
        return Rule.of(i -> (i & 1) == 0, "must.be.even");
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
     * @return a {@link Rule} checking the minimum value.
     */
    public Rule<Integer> min(int minInclusive) {
        return Rule.of(
                i -> i >= minInclusive,
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
     *     <li>{@code max}: the maximum allowed value ({@code int})</li>
     * </ul>
     *
     * @param maxInclusive the maximum allowed value (inclusive).
     * @return a {@link Rule} checking the maximum value.
     */
    public Rule<Integer> max(int maxInclusive) {
        return Rule.of(
                i -> i <= maxInclusive,
                ErrorMessage.of("must.be.at.most", "max", maxInclusive)
        );
    }
    //endregion

}

