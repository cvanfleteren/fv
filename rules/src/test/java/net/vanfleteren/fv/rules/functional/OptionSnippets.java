package net.vanfleteren.fv.rules.functional;

import io.vavr.control.Option;
import net.vanfleteren.fv.MappingRule;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.Validation;

import static net.vanfleteren.fv.rules.functional.OptionRules.options;

public class OptionSnippets {

    void requiredExample() {
        // @start region="required-example"
        MappingRule<Option<String>, String> rule = options().required();

        Validation<String> result1 = rule.test(Option.of("hello")); // Valid("hello")
        Validation<String> result2 = rule.test(Option.none());      // Invalid("must.not.be.empty")
        // @end
    }

    void requiredOptionExample() {
        // @start region="required-option-example"
        Rule<Option<String>> rule = options().requiredOption();

        Validation<Option<String>> result1 = rule.test(Option.of("hello")); // Valid(Some("hello"))
        Validation<Option<String>> result2 = rule.test(Option.none());      // Invalid("must.not.be.empty")
        // @end
    }
}
