package be.iffy.fv.rules.numbers;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.rules.ComparableRules;
import be.iffy.fv.rules.IObjectRules;

import java.util.Objects;

/**
 * Validation rules for {@link Float} values.
 */
public final class FloatRules implements ComparableRules<Float>, NumberRules<Float>, IObjectRules<Float> {

    /**
     * Singleton instance of {@link FloatRules}.
     */
    public static final FloatRules floats = new FloatRules();

    //region sign related

    /**
     * Fails if the {@link Float} is not positive (not greater than zero).
     * <p>
     * Error key: {@code must.be.positive}
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
     */
    @Override
    public Rule<Float> nonNegative() {
        return Rule.of(
                f -> f >= 0.0f,
                "must.be.non.negative"
        );
    }

    /**
     * Fails if the {@link Float} is not negative (not less than zero).
     * <p>
     * Error key: {@code must.be.negative}
     *
     */
    @Override
    public Rule<Float> negative() {
        return Rule.of(
                f -> f < 0.0f,
                "must.be.negative"
        );
    }

    /**
     * Fails if the {@link Float} is positive (greater than zero).
     * <p>
     * Error key: {@code must.be.non.positive}
     *
     */
    @Override
    public Rule<Float> nonPositive() {
        return Rule.of(
                f -> f <= 0.0f,
                "must.be.non.positive"
        );
    }

    /**
     * Fails if the {@link Float} is not zero.
     * <p>
     * Error key: {@code must.be.zero}
     *
     */
    @Override
    public Rule<Float> zero() {
        return Rule.of(
                f -> f == 0.0f,
                "must.be.zero"
        );
    }

    /**
     * Fails if the {@link Float} is zero.
     * <p>
     * Error key: {@code must.not.be.zero}
     *
     */
    @Override
    public Rule<Float> nonZero() {
        return Rule.of(
                f -> f != 0.0f,
                "must.not.be.zero"
        );
    }
    //endregion

    //region floating point specific

    /**
     * Fails if the {@link Float} is not a finite number.
     * <p>
     * Error key: {@code must.be.finite}
     *
     */
    public Rule<Float> finite() {
        return Rule.of(
                Float::isFinite,
                "must.be.finite"
        );
    }

    /**
     * Fails if the {@link Float} is not NaN.
     * <p>
     * Error key: {@code must.be.nan}
     *
     */
    public Rule<Float> nan() {
        return Rule.of(
                f -> Float.isNaN(f),
                "must.be.nan"
        );
    }

    /**
     * Fails if the {@link Float} is NaN.
     * <p>
     * Error key: {@code must.not.be.nan}
     *
     */
    public Rule<Float> nonNan() {
        return Rule.of(
                f -> !Float.isNaN(f),
                "must.not.be.nan"
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
     *     <li>{@code min}: the minimum allowed value ({@code float})</li>
     * </ul>
     *
     * @param minInclusive the minimum allowed value (inclusive).
     */
    public Rule<Float> min(float minInclusive) {
        return Rule.of(
                f -> f >= minInclusive,
                ErrorMessage.of("must.be.at.least", "min", minInclusive)
        );
    }

    @Override
    public Rule<Float> min(Float minInclusive) {
        Objects.requireNonNull(minInclusive, "minInclusive cannot be null");
        return min(minInclusive.floatValue());
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
     */
    public Rule<Float> max(float maxInclusive) {
        return Rule.of(
                f -> f <= maxInclusive,
                ErrorMessage.of("must.be.at.most", "max", maxInclusive)
        );
    }

    @Override
    public Rule<Float> max(Float maxInclusive) {
        Objects.requireNonNull(maxInclusive, "maxInclusive cannot be null");
        return max(maxInclusive.floatValue());
    }
    //endregion

}
