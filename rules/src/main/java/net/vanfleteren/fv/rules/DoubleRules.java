package net.vanfleteren.fv.rules;

import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;

/**
 * Validation rules for {@link Double} values.
 */
public class DoubleRules implements ComparableRules<Double>, NumberRules<Double>, IObjectRules<Double> {

    /**
     * Singleton instance of {@link DoubleRules}.
     */
    public static final DoubleRules doubles = new DoubleRules();

    /**
     * Returns the singleton instance of {@link DoubleRules}.
     *
     * @return the {@link DoubleRules} instance.
     */
    public static DoubleRules doubles() {
        return doubles;
    }

    //region sign related
    /**
     * Fails if the {@link Double} is not positive (not greater than zero).
     * <p>
     * Error key: {@code must.be.positive}
     *
     * @return a {@link Rule} checking for positive values.
     */
    @Override
    public Rule<Double> positive() {
        return Rule.of(d -> d > 0.0, "must.be.positive");
    }

    /**
     * Fails if the {@link Double} is negative (less than zero).
     * <p>
     * Error key: {@code must.be.non.negative}
     *
     * @return a {@link Rule} checking for non-negative values.
     */
    @Override
    public Rule<Double> nonNegative() {
        return Rule.of(d -> d >= 0.0, "must.be.non.negative");
    }

    /**
     * Fails if the {@link Double} is not negative (not less than zero).
     * <p>
     * Error key: {@code must.be.negative}
     *
     * @return a {@link Rule} checking for negative values.
     */
    @Override
    public Rule<Double> negative() {
        return Rule.of(d -> d < 0.0, "must.be.negative");
    }

    /**
     * Fails if the {@link Double} is positive (greater than zero).
     * <p>
     * Error key: {@code must.be.non.positive}
     *
     * @return a {@link Rule} checking for non-positive values.
     */
    @Override
    public Rule<Double> nonPositive() {
        return Rule.of(d -> d <= 0.0, "must.be.non.positive");
    }

    /**
     * Fails if the {@link Double} is not zero.
     * <p>
     * Error key: {@code must.be.zero}
     *
     * @return a {@link Rule} checking for zero values.
     */
    @Override
    public Rule<Double> zero() {
        return Rule.of(d -> d == 0.0, "must.be.zero");
    }

    /**
     * Fails if the {@link Double} is zero.
     * <p>
     * Error key: {@code must.not.be.zero}
     *
     * @return a {@link Rule} checking for non-zero values.
     */
    @Override
    public Rule<Double> nonZero() {
        return Rule.of(d -> d != 0.0, "must.not.be.zero");
    }
    //endregion

    //region floating point specific
    /**
     * Fails if the {@link Double} is not a finite number.
     * <p>
     * Error key: {@code must.be.finite}
     *
     * @return a {@link Rule} checking for finite values.
     */
    public Rule<Double> finite() {
        return Rule.of(Double::isFinite, "must.be.finite");
    }

    /**
     * Fails if the {@link Double} is not NaN.
     * <p>
     * Error key: {@code must.be.nan}
     *
     * @return a {@link Rule} checking for NaN values.
     */
    public Rule<Double> nan() {
        return Rule.of(d -> Double.isNaN(d), "must.be.nan");
    }

    /**
     * Fails if the {@link Double} is NaN.
     * <p>
     * Error key: {@code must.not.be.nan}
     *
     * @return a {@link Rule} checking for non-NaN values.
     */
    public Rule<Double> nonNan() {
        return Rule.of(d -> !Double.isNaN(d), "must.not.be.nan");
    }
    //endregion

    //region comparisons
    /**
     * Fails if the value is less than the specified minimum.
     * <p>
     * Error key: {@code min.value}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code min}: the minimum allowed value ({@code double})</li>
     * </ul>
     *
     * @param minInclusive the minimum allowed value (inclusive).
     * @return a {@link Rule} checking the minimum value.
     */
    public Rule<Double> min(double minInclusive) {
        return Rule.of(
                d -> d >= minInclusive,
                ErrorMessage.of("min.value", "min", minInclusive)
        );
    }

    /**
     * Fails if the value is greater than the specified maximum.
     * <p>
     * Error key: {@code max.value}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code max}: the maximum allowed value ({@code double})</li>
     * </ul>
     *
     * @param maxInclusive the maximum allowed value (inclusive).
     * @return a {@link Rule} checking the maximum value.
     */
    public Rule<Double> max(double maxInclusive) {
        return Rule.of(
                d -> d <= maxInclusive,
                ErrorMessage.of("max.value", "max", maxInclusive)
        );
    }
    //endregion

}
