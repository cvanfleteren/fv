package net.vanfleteren.fv.rules.collections;

import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.Validation;

import static net.vanfleteren.fv.dsl.DSL.validateThat;

public class MapRulesSnippets {

    void validateValuesWithExample() {
        // @start region="validate-values-with-example"
        Rule<String> notEmpty = Rule.of(s -> s != null && !s.isEmpty(), "must.not.be.empty");
        Rule<Map<String, String>> mapRule = MapRules.validateValuesWith(notEmpty);

        Map<String, String> input = HashMap.of(
            "key1", "value1",
            "key2", ""
        );

        Validation<Map<String, String>> result = validateThat(input, "myMap").is(mapRule);
        // result will be invalid with error "myMap[key2].must.not.be.empty"
        // @end
    }
}
