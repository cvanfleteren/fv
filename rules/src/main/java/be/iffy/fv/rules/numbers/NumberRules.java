package be.iffy.fv.rules.numbers;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;

/**
 * Common validation rules for {@link Number} values.
 *
 
 */
public interface NumberRules<T extends Number> {

    /**
     * Fails if the number is not positive (not greater than zero).
     * <p>
     * Error key: {@code must.be.positive}
     */
    default Rule<T> positive() {
        return Rule.of(
                i -> i.doubleValue() > 0,
                "must.be.positive"
        );
    }

    /**
     * Fails if the number is negative (less than zero).
     * <p>
     * Error key: {@code must.be.non.negative}
     */
    default Rule<T> nonNegative() {
        return Rule.of(
                i -> i.doubleValue() >= 0,
                "must.be.non.negative"
        );
    }

    /**
     * Fails if the number is not negative (not less than zero).
     * <p>
     * Error key: {@code must.be.negative}
     */
    default Rule<T> negative() {
        return Rule.of(
                i -> i.doubleValue() < 0,
                "must.be.negative"
        );
    }

    /**
     * Fails if the number is positive (greater than zero).
     * <p>
     * Error key: {@code must.be.non.positive}
     */
    default Rule<T> nonPositive() {
        return Rule.of(
                i -> i.doubleValue() <= 0,
                "must.be.non.positive"
        );
    }

    /**
     * Fails if the number is not zero.
     * <p>
     * Error key: {@code must.be.zero}
     */
    default Rule<T> zero() {
        return Rule.of(
                i -> i.doubleValue() == 0,
                "must.be.zero"
        );
    }

    /**
     * Fails if the number is zero.
     * <p>
     * Error key: {@code must.not.be.zero}
     */
    default Rule<T> nonZero() {
        return Rule.of(
                i -> i.doubleValue() != 0,
                "must.not.be.zero"
        );
    }

    /**
     * Fails if the number is less than the specified minimum.
     * <p>
     * Error key: {@code must.be.at.least}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code min}: the minimum allowed value ({@code T})</li>
     * </ul>
     *
     * @param minInclusive the minimum allowed value (inclusive).
     */
    default Rule<T> min(T minInclusive) {
        return Rule.of(
                i -> i.doubleValue() >= minInclusive.doubleValue(),
                ErrorMessage.of("must.be.at.least", "min", minInclusive)
        );
    }

    /**
     * Fails if the number is greater than the specified maximum.
     * <p>
     * Error key: {@code must.be.at.most}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code max}: the maximum allowed value ({@code T})</li>
     * </ul>
     *
     * @param maxInclusive the maximum allowed value (inclusive).
     */
    default Rule<T> max(T maxInclusive) {
        return Rule.of(
                i -> i.doubleValue() <= maxInclusive.doubleValue(),
                ErrorMessage.of("must.be.at.most", "max", maxInclusive)
        );
    }
}
