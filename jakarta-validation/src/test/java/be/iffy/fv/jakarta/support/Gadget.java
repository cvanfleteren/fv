package be.iffy.fv.jakarta.support;

import be.iffy.fv.Rule;
import be.iffy.fv.jakarta.FvStaticRule;

import static be.iffy.fv.dsl.DSL.*;

/**
 * Test model using @FvStaticRule: the rule is a plain static constant on the type itself.
 */
@FvStaticRule(on = Gadget.class, field = "RULE")
public record Gadget(String code, int quantity) {

    public static final Rule<Gadget> RULE = Rule.all(
        strings.minLength(3).on(Gadget::code),
        ints.atLeast(1).on(Gadget::quantity)
    );
}
