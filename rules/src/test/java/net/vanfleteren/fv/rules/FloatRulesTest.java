package net.vanfleteren.fv.rules;

import io.vavr.collection.HashMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.rules.FloatRules.floats;
import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;

class FloatRulesTest {

    @Nested
    class Positive {
        @Test
        void valid() {
            validTest(1.0f, floats().positive());
        }

        @Test
        void invalid() {
            invalidTest(0.0f, floats().positive(), "must.be.positive");
            invalidTest(-1.0f, floats().positive(), "must.be.positive");
        }
    }

    @Nested
    class Finite {
        @Test
        void valid() {
            validTest(1.0f, floats().finite());
        }

        @Test
        void invalid() {
            invalidTest(Float.POSITIVE_INFINITY, floats().finite(), "must.be.finite");
            invalidTest(Float.NaN, floats().finite(), "must.be.finite");
        }
    }

    @Nested
    class Nan {
        @Test
        void valid() {
            validTest(Float.NaN, floats().nan());
        }

        @Test
        void invalid() {
            invalidTest(1.0f, floats().nan(), "must.be.nan");
        }
    }

    @Nested
    class Min {
        @Test
        void valid() {
            validTest(2.0f, floats().min(2.0f));
            validTest(3.0f, floats().min(2.0f));
        }

        @Test
        void invalid() {
            invalidTest(1.0f, floats().min(2.0f), "min.value", HashMap.of("min", 2.0f));
        }
    }
}
