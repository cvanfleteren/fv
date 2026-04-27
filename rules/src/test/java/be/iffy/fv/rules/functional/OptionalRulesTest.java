package be.iffy.fv.rules.functional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;
import static be.iffy.fv.rules.functional.OptionalRules.optionals;

class OptionalRulesTest {

    @Nested
    class Required {

        @Test
        void valid() {
            validTest(Optional.of("value"), "value", optionals().required());
        }

        @Test
        void invalid() {
            invalidTest(Optional.empty(), optionals().required(), "must.not.be.empty");
            invalidTest(null, optionals().required(), "must.not.be.null");
        }
    }

    @Nested
    class RequiredOption {

        @Test
        void valid() {
            Optional<String> some = Optional.of("value");
            validTest(some, optionals().notEmpty());
        }

        @Test
        void invalid() {
            invalidTest(Optional.empty(), optionals().notEmpty(), "must.not.be.empty");
            invalidTest(null, optionals().notEmpty(), "must.not.be.null");
        }
    }

    @Nested
    class Empty {

        @Test
        void valid() {
            Optional<String> none = Optional.empty();
            validTest(none, optionals().empty());
        }

        @Test
        void invalid() {
            invalidTest(Optional.of("value"), optionals().empty(), "must.be.empty");
            invalidTest(null, optionals().empty(), "must.not.be.null");
        }
    }
}
