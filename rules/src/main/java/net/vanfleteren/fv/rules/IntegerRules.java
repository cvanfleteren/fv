package net.vanfleteren.fv.rules;

import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;

public class IntegerRules implements ComparableRules<Integer>, NumberRules<Integer>, IObjectRules<Integer> {

    public static final IntegerRules ints = new IntegerRules();

    public static IntegerRules ints() {
        return ints;
    }

    //region sign related
    @Override
    public Rule<Integer> positive() {
        return Rule.of(i -> i > 0, "must.be.positive");
    }

    @Override
    public Rule<Integer> nonNegative() {
        return Rule.of(i -> i >= 0, "must.be.non.negative");
    }

    @Override
    public Rule<Integer> negative() {
        return Rule.of(i -> i < 0, "must.be.negative");
    }

    @Override
    public Rule<Integer> nonPositive() {
        return Rule.of(i -> i <= 0, "must.be.non.positive");
    }

    @Override
    public Rule<Integer> zero() {
        return Rule.of(i -> i == 0, "must.be.zero");
    }

    @Override
    public Rule<Integer> nonZero() {
        return Rule.of(i -> i != 0, "must.not.be.zero");
    }
    //endregion

    //region parity related
    public Rule<Integer> odd() {
        return Rule.of(i -> (i & 1) != 0, "must.be.odd");
    }

    public Rule<Integer> even() {
        return Rule.of(i -> (i & 1) == 0, "must.be.even");
    }
    //endregion

    //region comparisons
    public Rule<Integer> min(int minInclusive) {
        return Rule.of(
                i -> i >= minInclusive,
                ErrorMessage.of("min.value", "min", minInclusive)
        );
    }

    public Rule<Integer> max(int maxInclusive) {
        return Rule.of(
                i -> i <= maxInclusive,
                ErrorMessage.of("max.value", "max", maxInclusive)
        );
    }
    //endregion

}

