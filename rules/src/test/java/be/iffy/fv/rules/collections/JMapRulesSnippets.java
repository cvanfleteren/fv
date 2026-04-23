package be.iffy.fv.rules.collections;

import be.iffy.fv.Rule;
import be.iffy.fv.Validation;

import java.util.Map;

import static be.iffy.fv.dsl.DSL.validateThat;


public class JMapRulesSnippets {

    void validateValuesWithExample() {
        // @start region="validate-values-with-example"
        Rule<String> notEmpty = Rule.of(s -> s != null && !s.isEmpty(), "must.not.be.empty");
        Rule<Map<String, String>> mapRule = JMapRules.validateValuesWith(notEmpty);

        Map<String, String> input = Map.of(
            "key1", "value1",
            "key2", ""
        );

        Validation<Map<String, String>> result = validateThat(input, "myMap").is(mapRule);
        // result will be invalid with error "myMap[key2].must.not.be.empty"
        // @end
    }
}
