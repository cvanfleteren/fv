package be.iffy.fv.rules.collections;

import be.iffy.fv.Rule;
import be.iffy.fv.Validation;

import java.util.Map;

import static be.iffy.fv.rules.Rules.maps;
import static be.iffy.fv.rules.Rules.strings;


public class MapRulesSnippets {

    void validateValuesWithExample() {
        // @start region="validate-values-with-example"
        Rule<Map<String, String>> mapRule = maps.validateValuesWith(strings.notEmpty());

        Map<String, String> input = Map.of(
            "key1", "value1",
            "key2", ""
        );

        Validation<Map<String, String>> result = mapRule.test(input).at("myMap");
        // result will be invalid with error "myMap[key2].must.not.be.empty"
        // @end
    }
}
