package be.iffy.fv.rules;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Validation;

import static be.iffy.fv.rules.ObjectRules.objects;
import static be.iffy.fv.rules.Rules.strings;

public class ObjectRulesSnippets {

    void isEnumExample() {
        // @start region="is-enum-example"
        enum Status { OPEN, CLOSED }

        MappingRule<String, Status> rule = strings.asEnum(Status.class);

        Validation<Status> result = rule.test("OPEN"); // Valid(Status.OPEN)
        Validation<Status> invalidResult = rule.test("UNKNOWN"); // Invalid("must.be.valid.enum.value")
        // @end
    }

}
