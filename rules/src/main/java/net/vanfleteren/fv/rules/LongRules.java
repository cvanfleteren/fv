package net.vanfleteren.fv.rules;

import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;

public class LongRules implements ComparableRules<Long>, NumberRules<Long>, IObjectRules<Long> {

    public static final LongRules longs = new LongRules();

    public static LongRules longs() {
        return longs;
    }

    //region sign related
    @Override
    public Rule<Long> positive() {
        return Rule.of(l -> l > 0, "must.be.positive");
    }

    @Override
    public Rule<Long> nonNegative() {
        return Rule.of(l -> l >= 0, "must.be.non.negative");
    }

    @Override
    public Rule<Long> negative() {
        return Rule.of(l -> l < 0, "must.be.negative");
    }

    @Override
    public Rule<Long> nonPositive() {
        return Rule.of(l -> l <= 0, "must.be.non.positive");
    }

    @Override
    public Rule<Long> zero() {
        return Rule.of(l -> l == 0, "must.be.zero");
    }

    @Override
    public Rule<Long> nonZero() {
        return Rule.of(l -> l != 0, "must.not.be.zero");
    }
    //endregion

    //region parity related
    public Rule<Long> odd() {
        return Rule.of(l -> (l & 1) != 0, "must.be.odd");
    }

    public Rule<Long> even() {
        return Rule.of(l -> (l & 1) == 0, "must.be.even");
    }
    //endregion

    //region comparisons
    public Rule<Long> min(long minInclusive) {
        return Rule.of(
                l -> l >= minInclusive,
                ErrorMessage.of("min.value", "min", minInclusive)
        );
    }

    public Rule<Long> max(long maxInclusive) {
        return Rule.of(
                l -> l <= maxInclusive,
                ErrorMessage.of("max.value", "max", maxInclusive)
        );
    }
    //endregion

}

