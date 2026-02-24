package net.vanfleteren.fv.rules;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
            invalidTest(HashMap.empty(), notEmpty(), "cannot.be.empty");
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

}