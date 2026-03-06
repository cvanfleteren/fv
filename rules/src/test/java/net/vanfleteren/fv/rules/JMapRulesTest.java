package net.vanfleteren.fv.rules;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.Validation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import static net.vanfleteren.fv.API.validateThat;
import static net.vanfleteren.fv.assertj.ValidationAssert.assertThatValidation;
import static net.vanfleteren.fv.rules.JMapRules.*;
import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;

class JMapRulesTest {

    @Nested
    class NotEmpty {

        @Test
        void valid() {
            validTest(Map.of("a", 1), notEmpty());
        }

        @Test
        void invalid_whenNull_thenCannotBeNull() {
            invalidTest(null, notEmpty(), "cannot.be.null");
        }

        @Test
        void invalid_whenEmpty_thenCannotBeEmpty() {
            invalidTest(Map.of(), notEmpty(), "cannot.be.empty");
        }
    }

    @Nested
    class ContainsKey {

        @Test
        void valid() {
            validTest(Map.of("a", 1, "b", 2), containsKey("a"));
        }

        @Test
        void invalid_whenNull_thenCannotBeNull() {
            invalidTest(null, containsKey("a"), "cannot.be.null");
        }

        @Test
        void invalid_whenKeyMissing_thenHasKeyAndArgs() {
            invalidTest(
                    Map.of("a", 1),
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
            validTest(Map.of(), valuesNotNull());
        }

        @Test
        void valid_whenNoNullValues() {
            validTest(Map.of("a", 1, "b", 2), valuesNotNull());
        }

        @Test
        void invalid_whenNullMap_thenCannotBeNull() {
            invalidTest(null, valuesNotNull(), "cannot.be.null");
        }

        @Test
        void invalid_whenContainsNullValues_thenReportsKeys() {
            java.util.Map<String, Integer> input = new java.util.HashMap<>();
            input.put("a", 1);
            input.put("b", null);
            input.put("c", null);

            invalidTest(
                    input,
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
            Rule<Number> rule = Rule.of(b -> b.doubleValue() > 0, "must.be.positive");
            Rule<Map<String, BigDecimal>> mapRule = JMapRules.validateValuesWith(rule);

            Map<String, BigDecimal> input = Map.of(
                    "a", BigDecimal.valueOf(-1),
                    "b", BigDecimal.TEN,
                    "c", BigDecimal.ZERO
            );

            Validation<Map<String, BigDecimal>> result = validateThat(input, "value").is(mapRule);

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
                    containsKeys("a", "b")
            );
        }

        @Test
        void invalid_whenNull_thenCannotBeNull() {
            invalidTest(
                    null,
                    containsKeys("a", "b"),
                    "cannot.be.null"
            );
        }

        @Test
        void invalid_whenMissingKey_thenHasKeyAndArgs() {
            invalidTest(
                    Map.of("a", 1),
                    containsKeys("a", "b"),
                    "must.contain.keys",
                    HashMap.of("keys", HashSet.of("a", "b"))
            );
        }

        @Test
        void invalid_whenMultipleMissingKeys_showsFirstMissingKey() {
            invalidTest(
                    Map.of("a", 1),
                    containsKeys("a", "b", "c"),
                    "must.contain.keys",
                    HashMap.of("keys", HashSet.of("a", "b", "c"))
            );
        }
    }

}
