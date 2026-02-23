package net.vanfleteren.fv.rules;

import io.vavr.collection.HashMap;
import io.vavr.collection.Set;
import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;

import java.util.Objects;

public class IntegerRules {

    //region sign related
    public static final Rule<Integer> positive = Rule.of(i -> i > 0, "must.be.positive");
    public static final Rule<Integer> nonNegative = Rule.of(i -> i >= 0, "must.be.non.negative");
    public static final Rule<Integer> negative = Rule.of(i -> i < 0, "must.be.negative");
    public static final Rule<Integer> nonPositive = Rule.of(i -> i <= 0, "must.be.non.positive");

    public static final Rule<Integer> zero = Rule.of(i -> i == 0, "must.be.zero");
    public static final Rule<Integer> nonZero = Rule.of(i -> i != 0, "must.not.be.zero");
    //endregion

    //region parity related
    public static final Rule<Integer> even = Rule.of(i -> (i & 1) == 0, "must.be.even");
    public static final Rule<Integer> odd = Rule.of(i -> (i & 1) != 0, "must.be.odd");
    //endregion

    //region comparisons
    public static Rule<Integer> min(int minInclusive) {
        return Rule.of(
                i -> i >= minInclusive,
                ErrorMessage.of("min.value", "min", minInclusive)
        );
    }

    public static Rule<Integer> max(int maxInclusive) {
        return Rule.of(
                i -> i <= maxInclusive,
                ErrorMessage.of("max.value", "max", maxInclusive)
        );
    }

    /**
     * Inclusive bounds.
     */
    public static Rule<Integer> between(int minInclusive, int maxInclusive) {
        if (maxInclusive < minInclusive) {
            throw new IllegalArgumentException("maxInclusive must be >= minInclusive");
        }
        return Rule.of(
                i -> i >= minInclusive && i <= maxInclusive,
                ErrorMessage.of("value.between", HashMap.of("min", minInclusive, "max", maxInclusive))
        );
    }

    /**
     * Exclusive bounds.
     */
    public static Rule<Integer> betweenExclusive(int minExclusive, int maxExclusive) {
        if (maxExclusive <= minExclusive) {
            throw new IllegalArgumentException("maxExclusive must be > minExclusive");
        }
        return Rule.of(
                i -> i > minExclusive && i < maxExclusive,
                ErrorMessage.of("value.between.exclusive", HashMap.of("min", minExclusive, "max", maxExclusive))
        );
    }

    public static Rule<Integer> greaterThan(int minExclusive) {
        return Rule.of(
                i -> i > minExclusive,
                ErrorMessage.of("must.be.greater.than", "min", minExclusive)
        );
    }

    public static Rule<Integer> atLeast(int minInclusive) {
        return Rule.of(
                i -> i >= minInclusive,
                ErrorMessage.of("must.be.at.least", "min", minInclusive)
        );
    }

    public static Rule<Integer> lessThan(int maxExclusive) {
        return Rule.of(
                i -> i < maxExclusive,
                ErrorMessage.of("must.be.less.than", "max", maxExclusive)
        );
    }

    public static Rule<Integer> atMost(int maxInclusive) {
        return Rule.of(
                i -> i <= maxInclusive,
                ErrorMessage.of("must.be.at.most", "max", maxInclusive)
        );
    }
    //endregion

    //region membership related
    public static Rule<Integer> in(Set<Integer> allowed) {
        Objects.requireNonNull(allowed, "allowed cannot be null");
        return Rule.of(
                allowed::contains,
                ErrorMessage.of("must.be.in", "allowed", allowed)
        );
    }

    public static Rule<Integer> notIn(Set<Integer> forbidden) {
        Objects.requireNonNull(forbidden, "forbidden cannot be null");
        return Rule.of(
                i -> !forbidden.contains(i),
                ErrorMessage.of("must.not.be.in", "forbidden", forbidden)
        );
    }
    //endregion
}

