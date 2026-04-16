package net.vanfleteren.fv.rules.collections;

import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.Validation;

import java.util.Map;

import static net.vanfleteren.fv.API.validateThat;

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
