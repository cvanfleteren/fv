package net.vanfleteren.fv.rules;

import io.vavr.collection.HashMap;
import io.vavr.collection.Set;
import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;

import java.util.Objects;

public class LongRules {

    //region sign related
    public static final Rule<Long> positive = Rule.of(i -> i > 0, "must.be.positive");
    public static final Rule<Long> nonNegative = Rule.of(i -> i >= 0, "must.be.non.negative");
    public static final Rule<Long> negative = Rule.of(i -> i < 0, "must.be.negative");
    public static final Rule<Long> nonPositive = Rule.of(i -> i <= 0, "must.be.non.positive");

    public static final Rule<Long> zero = Rule.of(i -> i == 0, "must.be.zero");
    public static final Rule<Long> nonZero = Rule.of(i -> i != 0, "must.not.be.zero");
    //endregion

    //region parity related
    public static final Rule<Long> even = Rule.of(i -> (i & 1) == 0, "must.be.even");
    public static final Rule<Long> odd = Rule.of(i -> (i & 1) != 0, "must.be.odd");
    //endregion

    //region comparisons
    public static Rule<Long> min(long minInclusive) {
        return Rule.of(
                i -> i >= minInclusive,
                ErrorMessage.of("min.value", "min", minInclusive)
        );
    }

    public static Rule<Long> max(long maxInclusive) {
        return Rule.of(
                i -> i <= maxInclusive,
                ErrorMessage.of("max.value", "max", maxInclusive)
        );
    }

    /**
     * Inclusive bounds.
     */
    public static Rule<Long> between(long minInclusive, long maxInclusive) {
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
    public static Rule<Long> betweenExclusive(long minExclusive, long maxExclusive) {
        if (maxExclusive <= minExclusive) {
            throw new IllegalArgumentException("maxExclusive must be > minExclusive");
        }
        return Rule.of(
                i -> i > minExclusive && i < maxExclusive,
                ErrorMessage.of("value.between.exclusive", HashMap.of("min", minExclusive, "max", maxExclusive))
        );
    }

    public static Rule<Long> greaterThan(long minExclusive) {
        return Rule.of(
                i -> i > minExclusive,
                ErrorMessage.of("must.be.greater.than", "min", minExclusive)
        );
    }

    public static Rule<Long> atLeast(long minInclusive) {
        return Rule.of(
                i -> i >= minInclusive,
                ErrorMessage.of("must.be.at.least", "min", minInclusive)
        );
    }

    public static Rule<Long> lessThan(long maxExclusive) {
        return Rule.of(
                i -> i < maxExclusive,
                ErrorMessage.of("must.be.less.than", "max", maxExclusive)
        );
    }

    public static Rule<Long> atMost(long maxInclusive) {
        return Rule.of(
                i -> i <= maxInclusive,
                ErrorMessage.of("must.be.at.most", "max", maxInclusive)
        );
    }
    //endregion

    //region membership related
    public static Rule<Long> in(Set<Long> allowed) {
        Objects.requireNonNull(allowed, "allowed cannot be null");
        return Rule.of(
                allowed::contains,
                ErrorMessage.of("must.be.in", "allowed", allowed)
        );
    }

    public static Rule<Long> notIn(Set<Long> forbidden) {
        Objects.requireNonNull(forbidden, "forbidden cannot be null");
        return Rule.of(
                i -> !forbidden.contains(i),
                ErrorMessage.of("must.not.be.in", "forbidden", forbidden)
        );
    }
    //endregion
}

