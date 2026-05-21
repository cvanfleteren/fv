package be.iffy.fv.rules.collections;

import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;

import static be.iffy.fv.rules.Rules.strings;


public class MapRulesSnippets {

    void validateValuesWithExample() {
        // @start region="validate-values-with-example"
        Rule<Map<String, String>> mapRule = VavrMapRules.validateValuesWith(strings().notEmpty());

        Map<String, String> input = HashMap.of(
                "key1", "value1",
                "key2", ""
        );

        Validation<Map<String, String>> result = mapRule.test(input).at("myMap");
        // result will be invalid with error "myMap[key2].must.not.be.empty"
        // @end
    }
}
