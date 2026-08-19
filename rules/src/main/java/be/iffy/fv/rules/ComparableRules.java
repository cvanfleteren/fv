package be.iffy.fv.rules;

import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;

/**
 * Common validation rules for {@link Comparable} values.
 *
 */
public interface ComparableRules<T extends Comparable<? super T>> {

    /**
     * Fails if the value is not between the specified bounds (inclusive).
     * <p>
     * Error key: {@code must.be.between}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code min}: the minimum allowed value ({@code T})</li>
     *     <li>{@code max}: the maximum allowed value ({@code T})</li>
     * </ul>
     *
     * @param minInclusive the minimum allowed value (inclusive).
     * @param maxInclusive the maximum allowed value (inclusive).
     */
    default Rule<T> between(T minInclusive, T maxInclusive) {
        if (maxInclusive.compareTo(minInclusive) < 0) {
            throw new IllegalArgumentException("maxInclusive must be >= minInclusive");
        }
        return Rule.of(
                i -> i.compareTo(minInclusive) >= 0 && i.compareTo(maxInclusive) <= 0,
                ErrorMessage.of("must.be.between", HashMap.of("min", minInclusive, "max", maxInclusive))
        );
    }

    /**
     * Fails if the value is not between the specified bounds (exclusive).
     * <p>
     * Error key: {@code must.be.between.exclusive}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code min}: the minimum allowed value ({@code T})</li>
     *     <li>{@code max}: the maximum allowed value ({@code T})</li>
     * </ul>
     *
     * @param minExclusive the minimum allowed value (exclusive).
     * @param maxExclusive the maximum allowed value (exclusive).
     */
    default Rule<T> betweenExclusive(T minExclusive, T maxExclusive) {
        if (maxExclusive.compareTo(minExclusive) <= 0) {
            throw new IllegalArgumentException("maxExclusive must be > minExclusive");
        }
        return Rule.of(
                i -> i.compareTo(minExclusive) > 0 && i.compareTo(maxExclusive) < 0,
                ErrorMessage.of("must.be.between.exclusive", HashMap.of("min", minExclusive, "max", maxExclusive))
        );
    }

    /**
     * Fails if the value is not greater than the specified minimum (exclusive).
     * <p>
     * Error key: {@code must.be.greater.than}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code min}: the minimum value ({@code T})</li>
     * </ul>
     *
     * @param minExclusive the minimum value (exclusive).
     */
    default Rule<T> greaterThan(T minExclusive) {
        return Rule.of(
                i -> i.compareTo(minExclusive) > 0,
                ErrorMessage.of("must.be.greater.than", "min", minExclusive)
        );
    }

    /**
     * Fails if the value is not at least the specified minimum (inclusive).
     * <p>
     * Error key: {@code must.be.at.least}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code min}: the minimum value ({@code T})</li>
     * </ul>
     *
     * @param minInclusive the minimum value (inclusive).
     */
    default Rule<T> atLeast(T minInclusive) {
        return Rule.of(
                i -> i.compareTo(minInclusive) >= 0,
                ErrorMessage.of("must.be.at.least", "min", minInclusive)
        );
    }

    /**
     * Fails if the value is not less than the specified maximum (exclusive).
     * <p>
     * Error key: {@code must.be.less.than}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code max}: the maximum value ({@code T})</li>
     * </ul>
     *
     * @param maxExclusive the maximum value (exclusive).
     */
    default Rule<T> lessThan(T maxExclusive) {
        return Rule.of(
                i -> i.compareTo(maxExclusive) < 0,
                ErrorMessage.of("must.be.less.than", "max", maxExclusive)
        );
    }

    /**
     * Fails if the value is not at most the specified maximum (inclusive).
     * <p>
     * Error key: {@code must.be.at.most}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code max}: the maximum value ({@code T})</li>
     * </ul>
     *
     * @param maxInclusive the maximum value (inclusive).
     */
    default Rule<T> atMost(T maxInclusive) {
        return Rule.of(
                i -> i.compareTo(maxInclusive) <= 0,
                ErrorMessage.of("must.be.at.most", "max", maxInclusive)
        );
    }

    /**
     * Fails if the first element of the pair is not strictly before the second element, according to their
     * natural ordering.
     * <p>
     * Error key: {@code first.must.be.before.second}
     */
    static <T extends Comparable<? super T>> Rule<Tuple2<T, T>> strictlyOrdered() {
        return Rule.of(
                t -> t._1.compareTo(t._2) < 0,
                ErrorMessage.of("first.must.be.before.second")
        );
    }

    /**
     * Fails if the first element of the pair is not before or equal to the second element, according to their
     * natural ordering.
     * <p>
     * Error key: {@code first.must.be.at.most.second}
     */
    static <T extends Comparable<? super T>> Rule<Tuple2<T, T>> ordered() {
        return Rule.of(
                t -> t._1.compareTo(t._2) <= 0,
                ErrorMessage.of("first.must.be.at.most.second")
        );
    }

    /**
     * Fails if the first element of the pair is not strictly after the second element, according to their
     * natural ordering.
     * <p>
     * Error key: {@code first.must.be.after.second}
     */
    static <T extends Comparable<? super T>> Rule<Tuple2<T, T>> strictlyDescending() {
        return Rule.of(
                t -> t._1.compareTo(t._2) > 0,
                ErrorMessage.of("first.must.be.after.second")
        );
    }

    /**
     * Fails if the first element of the pair is not after or equal to the second element, according to their
     * natural ordering.
     * <p>
     * Error key: {@code first.must.be.at.least.second}
     */
    static <T extends Comparable<? super T>> Rule<Tuple2<T, T>> descending() {
        return Rule.of(
                t -> t._1.compareTo(t._2) >= 0,
                ErrorMessage.of("first.must.be.at.least.second")
        );
    }

}
