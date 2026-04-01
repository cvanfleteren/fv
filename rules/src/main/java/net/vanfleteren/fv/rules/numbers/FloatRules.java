package net.vanfleteren.fv.rules.numbers;

import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.rules.ComparableRules;
import net.vanfleteren.fv.rules.IObjectRules;

/**
 * Validation rules for {@link Float} values.
 */
public class FloatRules implements ComparableRules<Float>, NumberRules<Float>, IObjectRules<Float> {

    /**
     * Singleton instance of {@link FloatRules}.
     */
    public static final FloatRules floats = new FloatRules();

    /**
     * Returns the singleton instance of {@link FloatRules}.
     *
     * @return the {@link FloatRules} instance.
     */
    public static FloatRules floats() {
        return floats;
    }

    //region sign related
    /**
     * Fails if the {@link Float} is not positive (not greater than zero).
     * <p>
     * Error key: {@code must.be.positive}
     *
     * @return a {@link Rule} checking for positive values.
     */
    @Override
    public Rule<Float> positive() {
        return Rule.of(f -> f > 0.0f, "must.be.positive");
    }

    /**
     * Fails if the {@link Float} is negative (less than zero).
     * <p>
     * Error key: {@code must.be.non.negative}
     *
     * @return a {@link Rule} checking for non-negative values.
     */
    @Override
    public Rule<Float> nonNegative() {
        return Rule.of(f -> f >= 0.0f, "must.be.non.negative");
    }

    /**
     * Fails if the {@link Float} is not negative (not less than zero).
     * <p>
     * Error key: {@code must.be.negative}
     *
     * @return a {@link Rule} checking for negative values.
     */
    @Override
    public Rule<Float> negative() {
        return Rule.of(f -> f < 0.0f, "must.be.negative");
    }

    /**
     * Fails if the {@link Float} is positive (greater than zero).
     * <p>
     * Error key: {@code must.be.non.positive}
     *
     * @return a {@link Rule} checking for non-positive values.
     */
    @Override
    public Rule<Float> nonPositive() {
        return Rule.of(f -> f <= 0.0f, "must.be.non.positive");
    }

    /**
     * Fails if the {@link Float} is not zero.
     * <p>
     * Error key: {@code must.be.zero}
     *
     * @return a {@link Rule} checking for zero values.
     */
    @Override
    public Rule<Float> zero() {
        return Rule.of(f -> f == 0.0f, "must.be.zero");
    }

    /**
     * Fails if the {@link Float} is zero.
     * <p>
     * Error key: {@code must.not.be.zero}
     *
     * @return a {@link Rule} checking for non-zero values.
     */
    @Override
    public Rule<Float> nonZero() {
        return Rule.of(f -> f != 0.0f, "must.not.be.zero");
    }
    //endregion

    //region floating point specific
    /**
     * Fails if the {@link Float} is not a finite number.
     * <p>
     * Error key: {@code must.be.finite}
     *
     * @return a {@link Rule} checking for finite values.
     */
    public Rule<Float> finite() {
        return Rule.of(Float::isFinite, "must.be.finite");
    }

    /**
     * Fails if the {@link Float} is not NaN.
     * <p>
     * Error key: {@code must.be.nan}
     *
     * @return a {@link Rule} checking for NaN values.
     */
    public Rule<Float> nan() {
        return Rule.of(f -> Float.isNaN(f), "must.be.nan");
    }

    /**
     * Fails if the {@link Float} is NaN.
     * <p>
     * Error key: {@code must.not.be.nan}
     *
     * @return a {@link Rule} checking for non-NaN values.
     */
    public Rule<Float> nonNan() {
        return Rule.of(f -> !Float.isNaN(f), "must.not.be.nan");
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
     *     <li>{@code min}: the minimum allowed value ({@code float})</li>
     * </ul>
     *
     * @param minInclusive the minimum allowed value (inclusive).
     * @return a {@link Rule} checking the minimum value.
     */
    public Rule<Float> min(float minInclusive) {
        return Rule.of(
                f -> f >= minInclusive,
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
     *     <li>{@code max}: the maximum allowed value ({@code float})</li>
     * </ul>
     *
     * @param maxInclusive the maximum allowed value (inclusive).
     * @return a {@link Rule} checking the maximum value.
     */
    public Rule<Float> max(float maxInclusive) {
        return Rule.of(
                f -> f <= maxInclusive,
                ErrorMessage.of("must.be.at.most", "max", maxInclusive)
        );
    }
    //endregion

}
