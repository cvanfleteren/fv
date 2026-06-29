package be.iffy.fv.rules.collections;

import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;
import static be.iffy.fv.rules.collections.VavrMapRules.vavrMaps;

class VavrMapRulesTest {

    @Nested
    class NotEmpty {

        @Test
        void valid() {
            validTest(HashMap.of("a", 1), vavrMaps.notEmpty());
        }

        @Test
        void invalid() {
            invalidTest(null, vavrMaps.notEmpty(), "must.not.be.null");
            invalidTest(HashMap.empty(), vavrMaps.notEmpty(), "must.not.be.empty");
        }
    }

    @Nested
    class MinSize {

        @Test
        void valid() {
            validTest(HashMap.of("a", 1, "b", 2), vavrMaps.minSize(2));
            validTest(HashMap.of("a", 1, "b", 2, "c", 3), vavrMaps.minSize(2));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrMaps.minSize(2), "must.not.be.null");
            invalidTest(
                    HashMap.of("a", 1),
                    vavrMaps.minSize(2),
                    "must.have.min.size",
                    HashMap.of("min", 2)
            );
        }
    }

    @Nested
    class MaxSize {

        @Test
        void valid() {
            validTest(HashMap.of("a", 1, "b", 2), vavrMaps.maxSize(2));
            validTest(HashMap.of("a", 1), vavrMaps.maxSize(2));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrMaps.maxSize(2), "must.not.be.null");
            invalidTest(
                    HashMap.of("a", 1, "b", 2, "c", 3),
                    vavrMaps.maxSize(2),
                    "must.have.max.size",
                    HashMap.of("max", 2)
            );
        }
    }

    @Nested
    class SizeEquals {

        @Test
        void valid() {
            validTest(HashMap.of("a", 1, "b", 2), vavrMaps.sizeEquals(2));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrMaps.sizeEquals(2), "must.not.be.null");
            invalidTest(
                    HashMap.of("a", 1),
                    vavrMaps.sizeEquals(2),
                    "must.have.exact.size",
                    HashMap.of("equal", 2)
            );
            invalidTest(
                    HashMap.of("a", 1, "b", 2, "c", 3),
                    vavrMaps.sizeEquals(2),
                    "must.have.exact.size",
                    HashMap.of("equal", 2)
            );
        }
    }

    @Nested
    class SizeBetween {

        @Test
        void valid() {
            validTest(HashMap.of("a", 1), vavrMaps.sizeBetween(1, 3));
            validTest(HashMap.of("a", 1, "b", 2), vavrMaps.sizeBetween(1, 3));
            validTest(HashMap.of("a", 1, "b", 2, "c", 3), vavrMaps.sizeBetween(1, 3));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrMaps.sizeBetween(1, 3), "must.not.be.null");
            invalidTest(
                    HashMap.empty(),
                    vavrMaps.sizeBetween(1, 3),
                    "must.have.size.between",
                    HashMap.of("min", 1, "max", 3)
            );
            invalidTest(
                    HashMap.of("a", 1, "b", 2, "c", 3, "d", 4),
                    vavrMaps.sizeBetween(1, 3),
                    "must.have.size.between",
                    HashMap.of("min", 1, "max", 3)
            );
        }
    }

    @Nested
    class ContainsKey {

        @Test
        void valid() {
            validTest(HashMap.of("a", 1, "b", 2), vavrMaps.containsKey("a"));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrMaps.containsKey("a"), "must.not.be.null");
            invalidTest(
                    HashMap.of("a", 1),
                    vavrMaps.containsKey("b"),
                    "must.contain.key",
                    HashMap.of("key", "b")
            );
        }
    }

    @Nested
    class ValuesNotNull {

        @Test
        void valid() {
            validTest(HashMap.empty(), vavrMaps.valuesNotNull());
            validTest(HashMap.of("a", 1, "b", 2), vavrMaps.valuesNotNull());
        }

        @Test
        void invalid() {
            invalidTest(null, vavrMaps.valuesNotNull(), "must.not.be.null");
            invalidTest(
                    HashMap.of("a", 1, "b", null, "c", null),
                    vavrMaps.valuesNotNull(),
                    "must.not.contain.null.values",
                    HashMap.of("keys", HashSet.of("b", "c"))
            );
        }
    }

    @Nested
    class ValidateValuesWith {

        @Test
        void validateValuesWith_whenSomeValuesFail_accumulatesErrorsAndAddsKeyToPath() {
            // Arrange: validate string length >= 3 for each map value
            Rule<Number> rule = Rule.of(b -> b.doubleValue() > 0, "must.be.positive");
            Rule<Map<String, BigDecimal>> mapRule = vavrMaps.validateValuesWith(rule);

            Map<String,BigDecimal> input = HashMap.of(
                    "a", BigDecimal.valueOf(-1),
                    "b", BigDecimal.TEN,
                    "c", BigDecimal.ZERO
            );

            // Act
            Validation<Map<String, BigDecimal>> result = mapRule.apply(input).at("value");

            // Assert: failures are attributed to their keys in the path
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("value[a].must.be.positive", "value[c].must.be.positive");
        }
    }

    @Nested
    class DoesNotContainKey {

        @Test
        void valid() {
            validTest(HashMap.of("a", 1, "b", 2), vavrMaps.doesNotContainKey("c"));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrMaps.doesNotContainKey("a"), "must.not.be.null");
            invalidTest(
                HashMap.of("a", 1),
                vavrMaps.doesNotContainKey("a"),
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
                HashMap.of("a", 1, "b", 2),
                vavrMaps.doesNotContainKeys("c", "d")
            );
            validTest(
                HashMap.of("a", 1, "b", 2),
                vavrMaps.doesNotContainKeys("c")
            );
        }

        @Test
        void invalid() {
            invalidTest(
                null,
                vavrMaps.doesNotContainKeys("a", "b"),
                "must.not.be.null"
            );
            invalidTest(
                HashMap.of("a", 1, "b", 2),
                vavrMaps.doesNotContainKeys("a", "b"),
                "must.not.contain.keys",
                HashMap.of("keys", HashSet.of("a", "b"))
            );
            invalidTest(
                HashMap.of("a", 1),
                vavrMaps.doesNotContainKeys("a", "c"),
                "must.not.contain.keys",
                HashMap.of("keys", HashSet.of("a", "c"))
            );
        }
    }

    @Nested
    class ContainsKeys {

        @Test
        void valid() {
            // The map contains all requested keys.
            validTest(
                    HashMap.of("a", 1, "b", 2),
                    vavrMaps.containsKeys("a", "b")
            );
        }

        @Test
        void invalid() {
            // A null map should trigger the universal "must.not.be.null" message.
            invalidTest(
                    null,
                    vavrMaps.containsKeys("a", "b"),
                    "must.not.be.null"
            );
            // The map is missing key "b".
            invalidTest(
                    HashMap.of("a", 1),
                    vavrMaps.containsKeys("a", "b"),
                    "must.contain.keys",
                    HashMap.of("keys", HashSet.of("a","b"))
            );
            // The map is missing both "b" and "c".  The error message contains the
            // first missing key (because the current implementation returns the last
            // key that fails the containsAll check).
            invalidTest(
                    HashMap.of("a", 1),
                    vavrMaps.containsKeys("a", "b", "c"),
                    "must.contain.keys",
                    HashMap.of("keys", HashSet.of("a","b","c"))
            );
        }
    }

}