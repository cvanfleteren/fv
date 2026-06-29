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
    class MinSize {

        @Test
        void valid() {
            validTest(Map.of("a", 1, "b", 2), maps.minSize(2));
            validTest(Map.of("a", 1, "b", 2, "c", 3), maps.minSize(2));
        }

        @Test
        void invalid() {
            invalidTest(null, maps.minSize(2), "must.not.be.null");
            invalidTest(
                    Map.of("a", 1),
                    maps.minSize(2),
                    "must.have.min.size",
                    HashMap.of("min", 2)
            );
        }
    }

    @Nested
    class MaxSize {

        @Test
        void valid() {
            validTest(Map.of("a", 1, "b", 2), maps.maxSize(2));
            validTest(Map.of("a", 1), maps.maxSize(2));
        }

        @Test
        void invalid() {
            invalidTest(null, maps.maxSize(2), "must.not.be.null");
            invalidTest(
                    Map.of("a", 1, "b", 2, "c", 3),
                    maps.maxSize(2),
                    "must.have.max.size",
                    HashMap.of("max", 2)
            );
        }
    }

    @Nested
    class SizeEquals {

        @Test
        void valid() {
            validTest(Map.of("a", 1, "b", 2), maps.sizeEquals(2));
        }

        @Test
        void invalid() {
            invalidTest(null, maps.sizeEquals(2), "must.not.be.null");
            invalidTest(
                    Map.of("a", 1),
                    maps.sizeEquals(2),
                    "must.have.exact.size",
                    HashMap.of("equal", 2)
            );
            invalidTest(
                    Map.of("a", 1, "b", 2, "c", 3),
                    maps.sizeEquals(2),
                    "must.have.exact.size",
                    HashMap.of("equal", 2)
            );
        }
    }

    @Nested
    class SizeBetween {

        @Test
        void valid() {
            validTest(Map.of("a", 1), maps.sizeBetween(1, 3));
            validTest(Map.of("a", 1, "b", 2), maps.sizeBetween(1, 3));
            validTest(Map.of("a", 1, "b", 2, "c", 3), maps.sizeBetween(1, 3));
        }

        @Test
        void invalid() {
            invalidTest(null, maps.sizeBetween(1, 3), "must.not.be.null");
            invalidTest(
                    Map.of(),
                    maps.sizeBetween(1, 3),
                    "must.have.size.between",
                    HashMap.of("min", 1, "max", 3)
            );
            invalidTest(
                    Map.of("a", 1, "b", 2, "c", 3, "d", 4),
                    maps.sizeBetween(1, 3),
                    "must.have.size.between",
                    HashMap.of("min", 1, "max", 3)
            );
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

    @Nested
    class DoesNotContainKey {

        @Test
        void valid() {
            validTest(Map.of("a", 1, "b", 2), maps.doesNotContainKey("c"));
        }

        @Test
        void invalid() {
            invalidTest(null, maps.doesNotContainKey("a"), "must.not.be.null");
            invalidTest(
                Map.of("a", 1),
                maps.doesNotContainKey("a"),
                "must.not.contain.key",
                HashMap.of("key", "a")
            );
        }
    }

    @Nested
    class DoesNotContainKeys {

        @Test
        void valid() {
            validTest(
                Map.of("a", 1, "b", 2),
                maps.doesNotContainKeys("c", "d")
            );
            validTest(
                Map.of("a", 1, "b", 2),
                maps.doesNotContainKeys("c")
            );
        }

        @Test
        void invalid() {
            invalidTest(
                null,
                maps.doesNotContainKeys("a", "b"),
                "must.not.be.null"
            );
            invalidTest(
                Map.of("a", 1, "b", 2),
                maps.doesNotContainKeys("a", "b"),
                "must.not.contain.keys",
                HashMap.of("keys", HashSet.of("a", "b"))
            );
            invalidTest(
                Map.of("a", 1),
                maps.doesNotContainKeys("a", "c"),
                "must.not.contain.keys",
                HashMap.of("keys", HashSet.of("a", "c"))
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

            Validation<Map<String, BigDecimal>> result = mapRule.apply(input).at("value");

            assertThatValidation(result)
                    .isInvalid();

            assertThatValidation(result).isInvalid().hasErrorMessage("value[a].must.be.positive");
            assertThatValidation(result).isInvalid().hasErrorMessage("value[c].must.be.positive");
        }
    }

}
