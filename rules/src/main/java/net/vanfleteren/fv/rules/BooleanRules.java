package net.vanfleteren.fv.rules;

import net.vanfleteren.fv.Rule;

public class BooleanRules {

    /**
     * Singleton instance of {@link BooleanRules}.
     */
    public static final BooleanRules booleans = new BooleanRules();

    /**
     * Returns the singleton instance of {@link BooleanRules}.
     */
    public static BooleanRules booleans() {
        return booleans;
    }

    /**
     * Fails if the boolean is {@code null} or {@code false}.
     * <p>
     * Error key: {@code must.be.true}
     */
    public Rule<Boolean> isTrue() {
        return Rule.notNull().and(Rule.of(b -> b, "must.be.true"));
    }

    /**
     * Fails if the boolean is {@code null} or {@code true}.
     * <p>
     * Error key: {@code must.be.false}
     */
    public Rule<Boolean> isFalse() {
        return Rule.notNull().and(Rule.of(b -> !b, "must.be.false"));
    }

    /**
     * Fails if the boolean is {@code null}.
     * <p>
     * Error key: {@code must.not.be.null}
     */
    public Rule<Boolean> notNull() {
        return Rule.notNull();
    }

}
