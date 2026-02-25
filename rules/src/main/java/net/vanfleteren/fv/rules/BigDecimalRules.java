package net.vanfleteren.fv.rules;

import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;

import java.math.BigDecimal;

public class BigDecimalRules implements ComparableRules<BigDecimal>, NumberRules<BigDecimal>, IObjectRules<BigDecimal> {

    public static final BigDecimalRules bigDecimals = new BigDecimalRules();

    public static BigDecimalRules bigDecimals() {
        return bigDecimals;
    }

    //region sign related
    @Override
    public Rule<BigDecimal> positive() {
        return Rule.of(b -> b.signum() == 1, "must.be.positive");
    }

    @Override
    public Rule<BigDecimal> nonNegative() {
        return Rule.of(b -> b.signum() > -1, "must.be.non.negative");
    }

    @Override
    public Rule<BigDecimal> negative() {
        return Rule.of(b -> b.signum() == -1, "must.be.negative");
    }

    @Override
    public Rule<BigDecimal> nonPositive() {
        return Rule.of(b -> b.signum() < 1, "must.be.non.positive");
    }

    @Override
    public Rule<BigDecimal> zero() {
        return Rule.of(b -> b.signum() == 0, "must.be.zero");
    }

    @Override
    public Rule<BigDecimal> nonZero() {
        return Rule.of(b -> b.signum() != 0, "must.not.be.zero");
    }
    //endregion

    //region comparisons
    public Rule<BigDecimal> min(BigDecimal minInclusive) {
        return Rule.of(
                b -> b.compareTo(minInclusive) >= 0,
                ErrorMessage.of("min.value", "min", minInclusive)
        );
    }

    public Rule<BigDecimal> max(BigDecimal maxInclusive) {
        return Rule.of(
                b -> b.compareTo(maxInclusive) <= 0,
                ErrorMessage.of("max.value", "max", maxInclusive)
        );
    }
    //endregion

}

