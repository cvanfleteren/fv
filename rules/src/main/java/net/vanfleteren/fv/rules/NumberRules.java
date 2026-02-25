package net.vanfleteren.fv.rules;

import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;

public interface NumberRules<T extends Number> {

    default Rule<T> positive() {
        return Rule.of(i -> i.doubleValue() > 0, "must.be.positive");
    }

    default Rule<T> nonNegative() {
        return Rule.of(i -> i.doubleValue() >= 0, "must.be.non.negative");
    }

    default Rule<T> negative() {
        return Rule.of(i -> i.doubleValue() < 0, "must.be.negative");
    }

    default Rule<T> nonPositive() {
        return Rule.of(i -> i.doubleValue() <= 0, "must.be.non.positive");
    }

    default Rule<T> zero() {
        return Rule.of(i -> i.doubleValue() == 0, "must.be.zero");
    }

    default Rule<T> nonZero() {
        return Rule.of(i -> i.doubleValue() != 0, "must.not.be.zero");
    }

    default Rule<T> min(T minInclusive) {
        return Rule.of(
                i -> i.doubleValue() >= minInclusive.doubleValue(),
                ErrorMessage.of("min.value", "min", minInclusive)
        );
    }

    default Rule<T> max(T maxInclusive) {
        return Rule.of(
                i -> i.doubleValue() <= maxInclusive.doubleValue(),
                ErrorMessage.of("max.value", "max", maxInclusive)
        );
    }
}
