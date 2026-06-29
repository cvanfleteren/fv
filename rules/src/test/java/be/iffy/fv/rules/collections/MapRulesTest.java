package be.iffy.fv.rules.collections;

import be.iffy.fv.ErrorMessage;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Predicate;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static be.iffy.fv.rules.collections.MapRules.*;
import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;

class MapRulesTest {

    @Nested
    class Empty {

        @Test
        void valid() {
            validTest(Map.of(), maps.empty());
        }

        @Test
        void invalid() {
            invalidTest(null, maps.empty(), "must.not.be.null");
            invalidTest(Map.of("a", 1), maps.empty(), "must.be.empty");
        }
    }

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


    @Nested
    class AllMatch {

        @Test
        void valid() {
            Predicate<Integer> isPositive = v -> v > 0;
            validTest(Map.of("a", 1, "b", 2), maps.allMatch(isPositive));
            validTest(Map.of(), maps.allMatch(isPositive));
        }

        @Test
        void invalid() {
            Predicate<Integer> isPositive = v -> v > 0;
            invalidTest(null, maps.allMatch(isPositive), "must.not.be.null");
            invalidTest(
                Map.of("a", 1, "b", -1),
                maps.allMatch(isPositive),
                "must.all.match"
            );
        }

        @Test
        void customErrorMessage() {
            Predicate<Integer> isPositive = v -> v > 0;
            ErrorMessage errorMsg = ErrorMessage.of("custom.all.match");
            Rule<Map<String, Integer>> rule = maps.allMatch(isPositive, errorMsg);
            invalidTest(
                Map.of("a", 1, "b", -1),
                rule,
                "custom.all.match"
            );
        }

        @Test
        void allMatch_withNullValues() {
            Predicate<Integer> isPositive = v -> v > 0;
            Rule<Map<String, Integer>> rule = maps.allMatch(isPositive);
            // null values should be considered invalid
            java.util.Map<String, Integer> input = new java.util.HashMap<>();
            input.put("a", 1);
            input.put("b", null);
            invalidTest(
                input,
                rule,
                "must.all.match"
            );
        }
    }

    @Nested
    class AllMatchRule {

        @Test
        void valid() {
            Rule<Integer> rule = Rule.of(v -> v > 0, "must.be.positive");
            validTest(Map.of("a", 1, "b", 2), maps.allMatchRule(rule));
            validTest(Map.of(), maps.allMatchRule(rule));
        }

        @Test
        void invalid() {
            Rule<Integer> rule = Rule.of(v -> v > 0, "must.be.positive");
            invalidTest(null, maps.allMatchRule(rule), "must.not.be.null");
            invalidTest(
                Map.of("a", 1, "b", -1),
                maps.allMatchRule(rule),
                "must.all.match"
            );
        }
    }

    @Nested
    class NoneMatch {

        @Test
        void valid() {
            Predicate<Integer> isNegative = v -> v < 0;
            validTest(Map.of("a", 1, "b", 2), maps.noneMatch(isNegative));
            validTest(Map.of(), maps.noneMatch(isNegative));
        }

        @Test
        void invalid() {
            Predicate<Integer> isNegative = v -> v < 0;
            invalidTest(null, maps.noneMatch(isNegative), "must.not.be.null");
            invalidTest(
                Map.of("a", 1, "b", -1),
                maps.noneMatch(isNegative),
                "must.none.match"
            );
        }

        @Test
        void customErrorMessage() {
            Predicate<Integer> isNegative = v -> v < 0;
            ErrorMessage errorMsg = ErrorMessage.of("custom.none.match");
            Rule<Map<String, Integer>> rule = maps.noneMatch(isNegative, errorMsg);
            invalidTest(
                Map.of("a", 1, "b", -1),
                rule,
                "custom.none.match"
            );
        }
    }

    @Nested
    class NoneMatchRule {

        @Test
        void valid() {
            Rule<Integer> rule = Rule.of(v -> v < 0, "must.be.positive");
            validTest(Map.of("a", 1, "b", 2), maps.noneMatchRule(rule));
            validTest(Map.of(), maps.noneMatchRule(rule));
        }

        @Test
        void invalid() {
            Rule<Integer> rule = Rule.of(v -> v < 0, "must.be.positive");
            invalidTest(null, maps.noneMatchRule(rule), "must.not.be.null");
            invalidTest(
                Map.of("a", 1, "b", -1),
                maps.noneMatchRule(rule),
                "must.none.match"
            );
        }

        @Test
        void noneMatch_withNullValues() {
            Predicate<Integer> isPositive = v -> v > 0;
            // null values are not matching the predicate, and 1 is > 0 so noneMatch fails
            java.util.Map<String, Integer> input = new java.util.HashMap<>();
            input.put("a", 1);
            input.put("b", null);
            // 1 matches the predicate, so noneMatch should fail
            invalidTest(
                input,
                maps.noneMatch(isPositive),
                "must.none.match"
            );
        }
    }

    @Nested
    class AnyMatch {

        @Test
        void valid() {
            Predicate<Integer> isNegative = v -> v < 0;
            validTest(Map.of("a", -1), maps.anyMatch(isNegative));
            validTest(Map.of("a", 1, "b", -1), maps.anyMatch(isNegative));
        }

        @Test
        void invalid() {
            Predicate<Integer> isNegative = v -> v < 0;
            invalidTest(null, maps.anyMatch(isNegative), "must.not.be.null");
            invalidTest(
                Map.of("a", 1, "b", 2),
                maps.anyMatch(isNegative),
                "must.at.least.one.match"
            );
        }

        @Test
        void customErrorMessage() {
            Predicate<Integer> isNegative = v -> v < 0;
            ErrorMessage errorMsg = ErrorMessage.of("custom.any.match");
            Rule<Map<String, Integer>> rule = maps.anyMatch(isNegative, errorMsg);
            invalidTest(
                Map.of("a", 1, "b", 2),
                rule,
                "custom.any.match"
            );
        }

        @Test
        void anyMatch_withNullValues() {
            Predicate<Integer> isPositive = v -> v > 0;
            Rule<Map<String, Integer>> rule = maps.anyMatch(isPositive);
            // All values are null which are not > 0, so anyMatch fails
            java.util.Map<String, Integer> input = new java.util.HashMap<>();
            input.put("a", null);
            input.put("b", null);
            invalidTest(
                input,
                rule,
                "must.at.least.one.match"
            );
        }
    }


}
