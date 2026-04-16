package net.vanfleteren.fv.rules.numbers;

import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.rules.ComparableRules;
import net.vanfleteren.fv.rules.IObjectRules;

import java.math.BigDecimal;

/**
 * Validation rules for {@link BigDecimal} values.
 */
public class BigDecimalRules implements ComparableRules<BigDecimal>, NumberRules<BigDecimal>, IObjectRules<BigDecimal> {

    /**
     * Singleton instance of {@link BigDecimalRules}.
     */
    public static final BigDecimalRules bigDecimals = new BigDecimalRules();

    /**
     * Returns the singleton instance of {@link BigDecimalRules}.
     */
    public static BigDecimalRules bigDecimals() {
        return bigDecimals;
    }

    //region sign related
    /**
     * Fails if the {@link BigDecimal} is not positive (not greater than zero).
     * <p>
     * Error key: {@code must.be.positive}
     *
     * @return a {@link Rule} checking for positive values.
     */
    @Override
    public Rule<BigDecimal> positive() {
        return Rule.notNull().and(Rule.of(b -> b.signum() == 1, "must.be.positive"));
    }

    /**
     * Fails if the {@link BigDecimal} is negative (less than zero).
     * <p>
     * Error key: {@code must.be.non.negative}
     *
     * @return a {@link Rule} checking for non-negative values.
     */
    @Override
    public Rule<BigDecimal> nonNegative() {
        return Rule.notNull().and(Rule.of(b -> b.signum() > -1, "must.be.non.negative"));
    }

    /**
     * Fails if the {@link BigDecimal} is not negative (not less than zero).
     * <p>
     * Error key: {@code must.be.negative}
     *
     * @return a {@link Rule} checking for negative values.
     */
    @Override
    public Rule<BigDecimal> negative() {
        return Rule.notNull().and(Rule.of(b -> b.signum() == -1, "must.be.negative"));
    }

    /**
     * Fails if the {@link BigDecimal} is positive (greater than zero).
     * <p>
     * Error key: {@code must.be.non.positive}
     *
     * @return a {@link Rule} checking for non-positive values.
     */
    @Override
    public Rule<BigDecimal> nonPositive() {
        return Rule.notNull().and(Rule.of(b -> b.signum() < 1, "must.be.non.positive"));
    }

    /**
     * Fails if the {@link BigDecimal} is not zero.
     * <p>
     * Error key: {@code must.be.zero}
     *
     * @return a {@link Rule} checking for zero values.
     */
    @Override
    public Rule<BigDecimal> zero() {
        return Rule.notNull().and(Rule.of(b -> b.signum() == 0, "must.be.zero"));
    }

    /**
     * Fails if the {@link BigDecimal} is zero.
     * <p>
     * Error key: {@code must.not.be.zero}
     *
     * @return a {@link Rule} checking for non-zero values.
     */
    @Override
    public Rule<BigDecimal> nonZero() {
        return Rule.notNull().and(Rule.of(b -> b.signum() != 0, "must.not.be.zero"));
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
     *     <li>{@code min}: the minimum allowed value ({@link BigDecimal})</li>
     * </ul>
     *
     * @param minInclusive the minimum allowed value (inclusive).
     * @return a {@link Rule} checking the minimum value.
     */
    public Rule<BigDecimal> min(BigDecimal minInclusive) {
        return Rule.notNull().and(Rule.of(
                b -> b.compareTo(minInclusive) >= 0,
                ErrorMessage.of("must.be.at.least", "min", minInclusive)
        ));
    }

    /**
     * Fails if the value is greater than the specified maximum.
     * <p>
     * Error key: {@code must.be.at.most}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code max}: the maximum allowed value ({@link BigDecimal})</li>
     * </ul>
     *
     * @param maxInclusive the maximum allowed value (inclusive).
     * @return a {@link Rule} checking the maximum value.
     */
    public Rule<BigDecimal> max(BigDecimal maxInclusive) {
        return Rule.notNull().and(Rule.of(
                b -> b.compareTo(maxInclusive) <= 0,
                ErrorMessage.of("must.be.at.most", "max", maxInclusive)
        ));
    }
    //endregion

}

