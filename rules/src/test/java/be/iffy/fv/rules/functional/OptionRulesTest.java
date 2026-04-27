package be.iffy.fv.rules.functional;

import io.vavr.control.Option;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static be.iffy.fv.rules.functional.OptionRules.options;
import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;

class OptionRulesTest {

    @Nested
    class Required {

        @Test
        void valid() {
            validTest(Option.of("value"), "value", options().required());
        }

        @Test
        void invalid() {
            invalidTest(Option.none(), options().required(), "must.not.be.empty");
            invalidTest(null, options().required(), "must.not.be.null");
        }
    }

    @Nested
    class RequiredOption {

        @Test
        void valid() {
            Option<String> some = Option.of("value");
            validTest(some, options().notEmpty());
        }

        @Test
        void invalid() {
            invalidTest(Option.none(), options().notEmpty(), "must.not.be.empty");
            invalidTest(null, options().notEmpty(), "must.not.be.null");
        }
    }

    @Nested
    class Empty {

        @Test
        void valid() {
            Option<String> none = Option.none();
            validTest(none, options().empty());
        }

        @Test
        void invalid() {
            invalidTest(Option.of("value"), options().empty(), "must.be.empty");
            invalidTest(null, options().empty(), "must.not.be.null");
        }
    }
}
