package be.iffy.fv.rules.collections;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static be.iffy.fv.rules.collections.MapRules.*;
import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;
import static be.iffy.fv.rules.collections.VavrMapRules.vavrMaps;

class MapRulesTest {

    @Nested
    class NotEmpty {

        @Test
        void valid() {
            validTest(Map.of("a", 1), maps.notEmpty());
        }

        @Test
        void invalid() {
            invalidTest(null, maps.notEmpty(), "must.not.be.null");
            invalidTest(Map.of(), maps.notEmpty(), "must.not.be.empty");
        }
    }

    @Nested
    class ContainsKey {

        @Test
        void valid() {
            validTest(Map.of("a", 1, "b", 2), maps.containsKey("a"));
        }

        @Test
        void invalid() {
            invalidTest(null, maps.containsKey("a"), "must.not.be.null");
            invalidTest(
                    Map.of("a", 1),
                    maps.containsKey("b"),
                    "must.contain.key",
                    HashMap.of("key", "b")
            );
        }
    }

    @Nested
    class ValuesNotNull {

        @Test
        void valid() {
            validTest(Map.of(), maps.valuesNotNull());
            validTest(Map.of("a", 1, "b", 2), maps.valuesNotNull());
        }

        @Test
        void invalid() {
            invalidTest(null, maps.valuesNotNull(), "must.not.be.null");

            java.util.Map<String, Integer> input = new java.util.HashMap<>();
            input.put("a", 1);
            input.put("b", null);
            input.put("c", null);

            invalidTest(
                    input,
                    maps.valuesNotNull(),
                    "must.not.contain.null.values",
                    HashMap.of("keys", HashSet.of("b", "c"))
            );
        }
    }

    @Nested
    class ValidateValuesWith {

        @Test
        void validateValuesWith_whenSomeValuesFail_accumulatesErrorsAndAddsKeyToPath() {
            Rule<Number> rule = Rule.of(b -> b.doubleValue() > 0, "must.be.positive");
            Rule<Map<String, BigDecimal>> mapRule = maps.validateValuesWith(rule);

            Map<String, BigDecimal> input = Map.of(
                    "a", BigDecimal.valueOf(-1),
                    "b", BigDecimal.TEN,
                    "c", BigDecimal.ZERO
            );

            Validation<Map<String, BigDecimal>> result = mapRule.test(input).at("value");

            assertThatValidation(result)
                    .isInvalid();

            assertThatValidation(result).isInvalid().hasErrorMessage("value[a].must.be.positive");
            assertThatValidation(result).isInvalid().hasErrorMessage("value[c].must.be.positive");
        }
    }

    @Nested
    class ContainsKeys {

        @Test
        void valid() {
            validTest(
                    Map.of("a", 1, "b", 2),
                    maps.containsKeys("a", "b")
            );
        }

        @Test
        void invalid() {
            invalidTest(
                    null,
                    maps.containsKeys("a", "b"),
                    "must.not.be.null"
            );
            invalidTest(
                    Map.of("a", 1),
                    maps.containsKeys("a", "b"),
                    "must.contain.keys",
                    HashMap.of("keys", HashSet.of("a", "b"))
            );
            invalidTest(
                    Map.of("a", 1),
                    maps.containsKeys("a", "b", "c"),
                    "must.contain.keys",
                    HashMap.of("keys", HashSet.of("a", "b", "c"))
            );
        }
    }

}
