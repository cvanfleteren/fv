package be.iffy.fv.rules.functional;

import be.iffy.fv.Validation;
import io.vavr.control.Option;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static be.iffy.fv.rules.Rules.strings;
import static be.iffy.fv.rules.functional.OptionRules.options;
import static be.iffy.fv.rules.RulesTest.*;

class OptionRulesTest {

    @Nested
    class Required {

        @Test
        void valid() {
            validTest(Option.of("value"), "value", options.required());
        }

        @Test
        void invalid() {
            invalidTest(Option.none(), options.required(), "must.not.be.empty");
            invalidTest(null, options.required(), "must.not.be.null");
        }
    }

    @Nested
    class RequiredRule {

        @Test
        void valid() {
            validTest(Option.of("value"), "value", options.required(strings.notBlank()));
        }

        @Test
        void invalid() {
            invalidTest(Option.none(), options.required(strings.notBlank()), "must.not.be.empty");
            invalidTest(Option.none(), options.required(strings.notBlank()), "must.not.be.empty");
            invalidTest(null, options.required(), "must.not.be.null");
        }
    }

    @Nested
    class Matches {

        @Test
        void valid() {
            validTest(Option.of("123"), Option.of(123), options.matches(strings.asInteger()));
        }

        @Test
        void validEmpty() {
            validTest(Option.none(), Option.none(), options.matches(strings.asInteger()));
        }

        @Test
        void invalid() {
            invalidTest(Option.of("abc"), options.matches(strings.asInteger()), "must.be.integer");
        }

        @Test
        void validFunction() {
            java.util.function.Function<String, Validation<Integer>> ruleLike = s -> Validation.valid(Integer.parseInt(s));
            validTest(Option.of("123"), Option.of(123), options.matches(ruleLike));
        }
    }

    @Nested
    class NotEmpty {

        @Test
        void valid() {
            Option<String> some = Option.of("value");
            validTest(some, options.notEmpty());
        }

        @Test
        void invalid() {
            invalidTest(Option.none(), options.notEmpty(), "must.not.be.empty");
            invalidTest(null, options.notEmpty(), "must.not.be.null");
        }
    }

    @Nested
    class Empty {

        @Test
        void valid() {
            Option<String> none = Option.none();
            validTest(none, options.empty());
        }

        @Test
        void invalid() {
            invalidTest(Option.of("value"), options.empty(), "must.be.empty");
            invalidTest(null, options.empty(), "must.not.be.null");
        }
    }

    @Nested
    class Contains {

        @Test
        void valid() {
            validTest(Option.of("value"), options.contains(strings.notBlank()));
        }

        @Test
        void invalid() {
            invalidTest(Option.of(""), options.contains(strings.notBlank()),"must.not.be.blank");
            invalidTest(Option.none(), options.contains(strings.notBlank()),"must.not.be.empty");
        }
    }
}
