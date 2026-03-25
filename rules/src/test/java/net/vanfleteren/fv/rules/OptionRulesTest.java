package net.vanfleteren.fv.rules;

import io.vavr.control.Option;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.rules.OptionRules.options;
import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;

class OptionRulesTest {

    @Nested
    class Required {

        @Test
        void required_whenOptionIsSome_returnsValidResult() {
            validTest(Option.of("value"), "value", options().required());
        }

        @Test
        void required_whenOptionIsNone_returnsInvalidWithErrorMessage() {
            invalidTest(Option.none(), options().required(), "must.not.be.empty");
        }
    }

    @Nested
    class Empty {

        @Test
        void empty_whenOptionIsNone_returnsValidResult() {
            Option<String> none = Option.none();
            validTest(none, none, options().empty());
        }

        @Test
        void empty_whenOptionIsSome_returnsInvalidWithErrorMessage() {
            invalidTest(Option.of("value"), options().empty(), "must.be.empty");
        }
    }
}
