package net.vanfleteren.fv.rules.numbers;

import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.rules.ComparableRules;
import net.vanfleteren.fv.rules.IObjectRules;

import java.math.BigInteger;

/**
 * Validation rules for {@link BigInteger} values.
 */
public class BigIntegerRules implements ComparableRules<BigInteger>, NumberRules<BigInteger>, IObjectRules<BigInteger> {

    /**
     * Singleton instance of {@link BigIntegerRules}.
     */
    public static final BigIntegerRules bigInts = new BigIntegerRules();

    /**
     * Returns the singleton instance of {@link BigIntegerRules}.
     *
     * @return the {@link BigIntegerRules} instance.
     */
    public static BigIntegerRules bigInts() {
        return bigInts;
    }

    //region sign related
    /**
     * Fails if the {@link BigInteger} is not positive (not greater than zero).
     * <p>
     * Error key: {@code must.be.positive}
     *
     * @return a {@link Rule} checking for positive values.
     */
    @Override
    public Rule<BigInteger> positive() {
        return Rule.of(b -> b.signum() == 1, "must.be.positive");
    }

    /**
     * Fails if the {@link BigInteger} is negative (less than zero).
     * <p>
     * Error key: {@code must.be.non.negative}
     *
     * @return a {@link Rule} checking for non-negative values.
     */
    @Override
    public Rule<BigInteger> nonNegative() {
        return Rule.of(b -> b.signum() > -1, "must.be.non.negative");
    }

    /**
     * Fails if the {@link BigInteger} is not negative (not less than zero).
     * <p>
     * Error key: {@code must.be.negative}
     *
     * @return a {@link Rule} checking for negative values.
     */
    @Override
    public Rule<BigInteger> negative() {
        return Rule.of(b -> b.signum() == -1, "must.be.negative");
    }

    /**
     * Fails if the {@link BigInteger} is positive (greater than zero).
     * <p>
     * Error key: {@code must.be.non.positive}
     *
     * @return a {@link Rule} checking for non-positive values.
     */
    @Override
    public Rule<BigInteger> nonPositive() {
        return Rule.of(b -> b.signum() < 1, "must.be.non.positive");
    }

    /**
     * Fails if the {@link BigInteger} is not zero.
     * <p>
     * Error key: {@code must.be.zero}
     *
     * @return a {@link Rule} checking for zero values.
     */
    @Override
    public Rule<BigInteger> zero() {
        return Rule.of(b -> b.signum() == 0, "must.be.zero");
    }

    /**
     * Fails if the {@link BigInteger} is zero.
     * <p>
     * Error key: {@code must.not.be.zero}
     *
     * @return a {@link Rule} checking for non-zero values.
     */
    @Override
    public Rule<BigInteger> nonZero() {
        return Rule.of(b -> b.signum() != 0, "must.not.be.zero");
    }
    //endregion

    //region parity related
    /**
     * Fails if the {@link BigInteger} is not odd.
     * <p>
     * Error key: {@code must.be.odd}
     *
     * @return a {@link Rule} checking for odd values.
     */
    public Rule<BigInteger> odd() {
        return Rule.of(b -> b.testBit(0), "must.be.odd");
    }

    /**
     * Fails if the {@link BigInteger} is not even.
     * <p>
     * Error key: {@code must.be.even}
     *
     * @return a {@link Rule} checking for even values.
     */
    public Rule<BigInteger> even() {
        return Rule.of(b -> !b.testBit(0), "must.be.even");
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
     *     <li>{@code min}: the minimum allowed value ({@link BigInteger})</li>
     * </ul>
     *
     * @param minInclusive the minimum allowed value (inclusive).
     * @return a {@link Rule} checking the minimum value.
     */
    public Rule<BigInteger> min(BigInteger minInclusive) {
        return Rule.of(
                b -> b.compareTo(minInclusive) >= 0,
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
     *     <li>{@code max}: the maximum allowed value ({@link BigInteger})</li>
     * </ul>
     *
     * @param maxInclusive the maximum allowed value (inclusive).
     * @return a {@link Rule} checking the maximum value.
     */
    public Rule<BigInteger> max(BigInteger maxInclusive) {
        return Rule.of(
                b -> b.compareTo(maxInclusive) <= 0,
                ErrorMessage.of("must.be.at.most", "max", maxInclusive)
        );
    }
    //endregion

}
