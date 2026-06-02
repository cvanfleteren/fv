package be.iffy.fv.rules.numbers;

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
    Rule<T> positive();

    /**
     * Fails if the number is negative (less than zero).
     * <p>
     * Error key: {@code must.be.non.negative}
     */
    Rule<T> nonNegative();

    /**
     * Fails if the number is not negative (not less than zero).
     * <p>
     * Error key: {@code must.be.negative}
     */
    Rule<T> negative();

    /**
     * Fails if the number is positive (greater than zero).
     * <p>
     * Error key: {@code must.be.non.positive}
     */
    Rule<T> nonPositive();

    /**
     * Fails if the number is not zero.
     * <p>
     * Error key: {@code must.be.zero}
     */
    Rule<T> zero();

    /**
     * Fails if the number is zero.
     * <p>
     * Error key: {@code must.not.be.zero}
     */
    Rule<T> nonZero();

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
    Rule<T> min(T minInclusive);

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
    Rule<T> max(T maxInclusive);
}
