package net.vanfleteren.fv.rules;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.rules.BooleanRules.booleans;
import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;

class BooleanRulesTest {

    @Nested
    class IsTrue {

        @Test
        void valid() {
            validTest(true, booleans.isTrue);
        }

        @Test
        void invalid() {
            invalidTest(false, booleans.isTrue, "must.be.true");
            invalidTest(null, booleans.isTrue, "must.not.be.null");
        }
    }

    @Nested
    class IsFalse {

        @Test
        void valid() {
            validTest(false, booleans.isFalse);
        }

        @Test
        void invalid() {
            invalidTest(true, booleans.isFalse, "must.be.false");
            invalidTest(null, booleans.isFalse, "must.not.be.null");
        }
    }

    @Nested
    class NotNull {

        @Test
        void valid() {
            validTest(true, booleans.notNull);
            validTest(false, booleans.notNull);
        }

        @Test
        void invalid() {
            invalidTest(null, booleans.notNull, "must.not.be.null");
        }
    }
}
