package net.vanfleteren.fv.rules;

import net.vanfleteren.fv.MappingRule;
import net.vanfleteren.fv.Validation;

import static net.vanfleteren.fv.rules.ObjectRules.objects;

public class ObjectRulesSnippets {

    void isEnumExample() {
        // @start region="is-enum-example"
        enum Status { OPEN, CLOSED }

        MappingRule<String, Status> rule = objects().isEnum(Status.class);

        Validation<Status> result = rule.test("OPEN"); // Valid(Status.OPEN)
        Validation<Status> invalidResult = rule.test("UNKNOWN"); // Invalid("must.be.valid.enum.value")
        // @end
    }

}
