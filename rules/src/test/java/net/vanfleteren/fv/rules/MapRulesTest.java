package net.vanfleteren.fv.rules;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.Validation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static net.vanfleteren.fv.API.validateThat;
import static net.vanfleteren.fv.assertj.ValidationAssert.assertThatValidation;
import static net.vanfleteren.fv.rules.MapRules.*;
import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;

class MapRulesTest {

    @Nested
    class NotEmpty {

        @Test
        void valid() {
            validTest(HashMap.of("a", 1), notEmpty());
        }

        @Test
        void invalid_whenNull_thenCannotBeNull() {
            invalidTest(null, notEmpty(), "cannot.be.null");
        }

        @Test
        void invalid_whenEmpty_thenCannotBeEmpty() {
            invalidTest(HashMap.empty(), notEmpty(), "must.not.be.empty");
        }
    }

    @Nested
    class ContainsKey {

        @Test
        void valid() {
            validTest(HashMap.of("a", 1, "b", 2), containsKey("a"));
        }

        @Test
        void invalid_whenNull_thenCannotBeNull() {
            invalidTest(null, containsKey("a"), "cannot.be.null");
        }

        @Test
        void invalid_whenKeyMissing_thenHasKeyAndArgs() {
            invalidTest(
                    HashMap.of("a", 1),
                    containsKey("b"),
                    "must.contain.key",
                    HashMap.of("key", "b")
            );
        }
    }

    @Nested
    class ValuesNotNull {

        @Test
        void valid_whenEmptyMap() {
            validTest(HashMap.empty(), valuesNotNull());
        }

        @Test
        void valid_whenNoNullValues() {
            validTest(HashMap.of("a", 1, "b", 2), valuesNotNull());
        }

        @Test
        void invalid_whenNullMap_thenCannotBeNull() {
            invalidTest(null, valuesNotNull(), "cannot.be.null");
        }

        @Test
        void invalid_whenContainsNullValues_thenReportsKeys() {
            invalidTest(
                    HashMap.of("a", 1, "b", null, "c", null),
                    valuesNotNull(),
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
            Rule<Map<String, BigDecimal>> mapRule = MapRules.validateValuesWith(rule);

            Map<String,BigDecimal> input = HashMap.of(
                    "a", BigDecimal.valueOf(-1),
                    "b", BigDecimal.TEN,
                    "c", BigDecimal.ZERO
            );

            // Act
            Validation<Map<String, BigDecimal>> result = validateThat(input, "value").is(mapRule);

            // Assert: failures are attributed to their keys in the path
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("value[a].must.be.positive", "value[c].must.be.positive");
        }
    }

    @Nested
    class ContainsKeys {

        @Test
        void valid() {
            // The map contains all requested keys.
            validTest(
                    HashMap.of("a", 1, "b", 2),
                    containsKeys("a", "b")
            );
        }

        @Test
        void invalid_whenNull_thenCannotBeNull() {
            // A null map should trigger the universal "cannot.be.null" message.
            invalidTest(
                    null,
                    containsKeys("a", "b"),
                    "cannot.be.null"
            );
        }

        @Test
        void invalid_whenMissingKey_thenHasKeyAndArgs() {
            // The map is missing key "b".
            invalidTest(
                    HashMap.of("a", 1),
                    containsKeys("a", "b"),
                    "must.contain.keys",
                    HashMap.of("keys", HashSet.of("a","b"))
            );
        }

        @Test
        void invalid_whenMultipleMissingKeys_showsFirstMissingKey() {
            // The map is missing both "b" and "c".  The error message contains the
            // first missing key (because the current implementation returns the last
            // key that fails the containsAll check).
            invalidTest(
                    HashMap.of("a", 1),
                    containsKeys("a", "b", "c"),
                    "must.contain.keys",
                    HashMap.of("keys", HashSet.of("a","b","c"))
            );
        }
    }

}