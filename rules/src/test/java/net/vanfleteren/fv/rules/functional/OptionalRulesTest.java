package net.vanfleteren.fv.rules.functional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;
import static net.vanfleteren.fv.rules.functional.OptionalRules.optionals;

class OptionalRulesTest {

    @Nested
    class Required {

        @Test
        void required_whenOptionIsSome_returnsValidResult() {
            validTest(Optional.of("value"), "value", optionals().required());
        }

        @Test
        void required_whenOptionIsNone_returnsInvalidWithErrorMessage() {
            invalidTest(Optional.empty(), optionals().required(), "must.not.be.empty");
        }
    }

    @Nested
    class RequiredOption {

        @Test
        void requiredOption_whenOptionIsSome_returnsValidResult() {
            Optional<String> some = Optional.of("value");
            validTest(some, optionals().requiredOptional());
        }

        @Test
        void requiredOption_whenOptionIsNone_returnsInvalidWithErrorMessage() {
            invalidTest(Optional.empty(), optionals().requiredOptional(), "must.not.be.empty");
        }
    }

    @Nested
    class Empty {

        @Test
        void empty_whenOptionIsNone_returnsValidResult() {
            Optional<String> none = Optional.empty();
            validTest(none, optionals().empty());
        }

        @Test
        void empty_whenOptionIsSome_returnsInvalidWithErrorMessage() {
            invalidTest(Optional.of("value"), optionals().empty(), "must.be.empty");
        }
    }
}
