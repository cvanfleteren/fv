package be.iffy.fv.rules.functional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static be.iffy.fv.rules.Rules.strings;
import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;
import static be.iffy.fv.rules.functional.OptionalRules.optionals;

class OptionalRulesTest {

    @Nested
    class Required {

        @Test
        void valid() {
            validTest(Optional.of("value"), "value", optionals.required());
        }

        @Test
        void invalid() {
            invalidTest(Optional.empty(), optionals.required(), "must.not.be.empty");
            invalidTest(null, optionals.required(), "must.not.be.null");
        }
    }

    @Nested
    class RequiredRule {

        @Test
        void valid() {
            validTest(Optional.of("value"), "value", optionals.required(strings.notBlank()));
        }

        @Test
        void invalid() {
            invalidTest(Optional.empty(), optionals.required(strings.notBlank()), "must.not.be.empty");
            invalidTest(Optional.empty(), optionals.required(strings.notBlank()), "must.not.be.empty");
            invalidTest(null, optionals.required(), "must.not.be.null");
        }
    }




    @Nested
    class NotEmpty {

        @Test
        void valid() {
            Optional<String> some = Optional.of("value");
            validTest(some, optionals.notEmpty());
        }

        @Test
        void invalid() {
            invalidTest(Optional.empty(), optionals.notEmpty(), "must.not.be.empty");
            invalidTest(null, optionals.notEmpty(), "must.not.be.null");
        }
    }

    @Nested
    class Empty {

        @Test
        void valid() {
            Optional<String> none = Optional.empty();
            validTest(none, optionals.empty());
        }

        @Test
        void invalid() {
            invalidTest(Optional.of("value"), optionals.empty(), "must.be.empty");
            invalidTest(null, optionals.empty(), "must.not.be.null");
        }
    }

    @Nested
    class Contains {

        @Test
        void valid() {
            validTest(Optional.of("value"), optionals.contains(strings.notBlank()));
        }

        @Test
        void invalid() {
            invalidTest(Optional.of(""), optionals.contains(strings.notBlank()),"must.not.be.blank");
            invalidTest(Optional.empty(), optionals.contains(strings.notBlank()),"must.not.be.empty");
        }
    }
}
