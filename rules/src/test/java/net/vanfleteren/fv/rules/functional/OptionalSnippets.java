package net.vanfleteren.fv.rules.functional;

import net.vanfleteren.fv.MappingRule;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.Validation;

import java.util.Optional;

import static net.vanfleteren.fv.rules.functional.OptionalRules.optionals;

public class OptionalSnippets {

    void requiredExample() {
        // @start region="required-example"
        MappingRule<Optional<String>, String> rule = optionals().required();

        Validation<String> result1 = rule.test(Optional.of("hello")); // Valid("hello")
        Validation<String> result2 = rule.test(Optional.empty());      // Invalid("must.not.be.empty")
        // @end
    }

    void requiredOptionExample() {
        // @start region="required-option-example"
        Rule<Optional<String>> rule = optionals().requiredOptional();

        Validation<Optional<String>> result1 = rule.test(Optional.of("hello")); // Valid(Some("hello"))
        Validation<Optional<String>> result2 = rule.test(Optional.empty());      // Invalid("must.not.be.empty")
        // @end
    }
}
