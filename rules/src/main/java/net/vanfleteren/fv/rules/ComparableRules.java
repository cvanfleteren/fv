package net.vanfleteren.fv.rules;

import io.vavr.collection.HashMap;
import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;

public interface ComparableRules<T extends Comparable<T>> {

    /**
     * Inclusive bounds.
     */
    default Rule<T> between(T minInclusive, T maxInclusive) {
        if (maxInclusive.compareTo(minInclusive) < 0) {
            throw new IllegalArgumentException("maxInclusive must be >= minInclusive");
        }
        return Rule.of(
                i -> i.compareTo(minInclusive) >= 0 && i.compareTo(maxInclusive) <= 0,
                ErrorMessage.of("value.between", HashMap.of("min", minInclusive, "max", maxInclusive))
        );
    }

    /**
     * Exclusive bounds.
     */
    default Rule<T> betweenExclusive(T minExclusive, T maxExclusive) {
        if (maxExclusive.compareTo(minExclusive) <= 0) {
            throw new IllegalArgumentException("maxExclusive must be > minExclusive");
        }
        return Rule.of(
                i -> i.compareTo(minExclusive) > 0 && i.compareTo(maxExclusive) < 0,
                ErrorMessage.of("value.between.exclusive", HashMap.of("min", minExclusive, "max", maxExclusive))
        );
    }

    /**
     * Greater than (exclusive).
     */
    default Rule<T> greaterThan(T minExclusive) {
        return Rule.of(
                i -> i.compareTo(minExclusive) > 0,
                ErrorMessage.of("must.be.greater.than", "min", minExclusive)
        );
    }

    /**
     * Greater than or equal to (inclusive).
     */
    default Rule<T> atLeast(T minInclusive) {
        return Rule.of(
                i -> i.compareTo(minInclusive) >= 0,
                ErrorMessage.of("must.be.at.least", "min", minInclusive)
        );
    }

    /**
     * Less than (exclusive).
     */
    default Rule<T> lessThan(T maxExclusive) {
        return Rule.of(
                i -> i.compareTo(maxExclusive) < 0,
                ErrorMessage.of("must.be.less.than", "max", maxExclusive)
        );
    }

    /**
     * Less than or equal to (inclusive).
     */
    default Rule<T> atMost(T maxInclusive) {
        return Rule.of(
                i -> i.compareTo(maxInclusive) <= 0,
                ErrorMessage.of("must.be.at.most", "max", maxInclusive)
        );
    }

}
