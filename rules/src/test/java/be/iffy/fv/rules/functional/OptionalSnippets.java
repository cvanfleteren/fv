package be.iffy.fv.rules.functional;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;

import java.util.Optional;

import static be.iffy.fv.rules.functional.OptionalRules.optionals;

public class OptionalSnippets {

    void requiredExample() {
        // @start region="required-example"
        MappingRule<Optional<String>, String> rule = optionals().required();

        Validation<String> result1 = rule.test(Optional.of("hello")); // Valid("hello")
        Validation<String> result2 = rule.test(Optional.empty());      // Invalid("must.not.be.empty")
        // @end
    }

    void requiredOptionExample() {
        // @start region="not-empty-example"
        Rule<Optional<String>> rule = optionals().notEmpty();

        Validation<Optional<String>> result1 = rule.test(Optional.of("hello")); // Valid(Some("hello"))
        Validation<Optional<String>> result2 = rule.test(Optional.empty());      // Invalid("must.not.be.empty")
        // @end
    }
}
