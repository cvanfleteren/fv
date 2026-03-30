package net.vanfleteren.fv.rules;

import net.vanfleteren.fv.Rule;

import static net.vanfleteren.fv.rules.ObjectRules.objects;

public class BooleanRules {

    public static final BooleanRules booleans = new BooleanRules();

    public static BooleanRules booleans() {
        return booleans;
    }

    public Rule<Boolean> isTrue = Rule.of(b -> b != null && b, "must.be.true");

    public Rule<Boolean> isFalse = Rule.of(b -> b != null && !b, "must.be.false");

    public Rule<Boolean> notNull = Rule.notNull();

}
