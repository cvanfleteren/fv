package net.vanfleteren.fv.rules;

import io.vavr.collection.HashMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.rules.DoubleRules.doubles;
import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;

class DoubleRulesTest {

    @Nested
    class Positive {
        @Test
        void valid() {
            validTest(1.0, doubles().positive());
        }

        @Test
        void invalid() {
            invalidTest(0.0, doubles().positive(), "must.be.positive");
            invalidTest(-1.0, doubles().positive(), "must.be.positive");
        }
    }

    @Nested
    class Finite {
        @Test
        void valid() {
            validTest(1.0, doubles().finite());
        }

        @Test
        void invalid() {
            invalidTest(Double.POSITIVE_INFINITY, doubles().finite(), "must.be.finite");
            invalidTest(Double.NaN, doubles().finite(), "must.be.finite");
        }
    }

    @Nested
    class Nan {
        @Test
        void valid() {
            validTest(Double.NaN, doubles().nan());
        }

        @Test
        void invalid() {
            invalidTest(1.0, doubles().nan(), "must.be.nan");
        }
    }

    @Nested
    class Min {
        @Test
        void valid() {
            validTest(2.0, doubles().min(2.0));
            validTest(3.0, doubles().min(2.0));
        }

        @Test
        void invalid() {
            invalidTest(1.0, doubles().min(2.0), "must.be.at.least", HashMap.of("min", 2.0));
        }
    }
}
