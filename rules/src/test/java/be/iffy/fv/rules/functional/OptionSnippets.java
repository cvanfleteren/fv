package be.iffy.fv.rules.functional;

import io.vavr.control.Option;
import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;

import static be.iffy.fv.rules.Rules.strings;
import static be.iffy.fv.rules.functional.OptionRules.options;

public class OptionSnippets {

    void requiredExample() {
        // @start region="required-example"
        MappingRule<Option<String>, String> rule = options().required();

        Validation<String> result1 = rule.test(Option.of("hello")); // Valid("hello")
        Validation<String> result2 = rule.test(Option.none());      // Invalid("must.not.be.empty")
        // @end
    }

    void containsExample() {
        // @start region="contains-example"
        Rule<Option<String>> rule = options().contains(strings().notBlank());

        Validation<Option<String>> result1 = rule.test(Option.of("hello")); // Valid("hello")
        Validation<Option<String>> result2 = rule.test(Option.of(""));      // Invalid("must.not.be.blank")
        Validation<Option<String>> result3 = rule.test(Option.none());      // Invalid("must.not.be.empty")
        // @end
    }

    void notEmptyExample() {
        // @start region="not-empty-example"
        Rule<Option<String>> rule = options().notEmpty();

        Validation<Option<String>> result1 = rule.test(Option.of("hello")); // Valid(Some("hello"))
        Validation<Option<String>> result2 = rule.test(Option.none());      // Invalid("must.not.be.empty")
        // @end
    }
}
