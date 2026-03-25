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
    class NonNegative {
        @Test
        void valid() {
            validTest(0.0f, floats().nonNegative());
            validTest(1.0f, floats().nonNegative());
        }

        @Test
        void invalid() {
            invalidTest(-1.0f, floats().nonNegative(), "must.be.non.negative");
        }
    }

    @Nested
    class Negative {
        @Test
        void valid() {
            validTest(-1.0f, floats().negative());
        }

        @Test
        void invalid() {
            invalidTest(0.0f, floats().negative(), "must.be.negative");
            invalidTest(1.0f, floats().negative(), "must.be.negative");
        }
    }

    @Nested
    class NonPositive {
        @Test
        void valid() {
            validTest(0.0f, floats().nonPositive());
            validTest(-1.0f, floats().nonPositive());
        }

        @Test
        void invalid() {
            invalidTest(1.0f, floats().nonPositive(), "must.be.non.positive");
        }
    }

    @Nested
    class Zero {
        @Test
        void valid() {
            validTest(0.0f, floats().zero());
        }

        @Test
        void invalid() {
            invalidTest(1.0f, floats().zero(), "must.be.zero");
            invalidTest(-1.0f, floats().zero(), "must.be.zero");
        }
    }

    @Nested
    class NonZero {
        @Test
        void valid() {
            validTest(1.0f, floats().nonZero());
            validTest(-1.0f, floats().nonZero());
        }

        @Test
        void invalid() {
            invalidTest(0.0f, floats().nonZero(), "must.not.be.zero");
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
    class NonNan {
        @Test
        void valid() {
            validTest(1.0f, floats().nonNan());
        }

        @Test
        void invalid() {
            invalidTest(Float.NaN, floats().nonNan(), "must.not.be.nan");
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
            invalidTest(1.0f, floats().min(2.0f), "must.be.at.least", HashMap.of("min", 2.0f));
        }
    }

    @Nested
    class Max {
        @Test
        void valid() {
            validTest(2.0f, floats().max(2.0f));
            validTest(1.0f, floats().max(2.0f));
        }

        @Test
        void invalid() {
            invalidTest(3.0f, floats().max(2.0f), "must.be.at.most", HashMap.of("max", 2.0f));
        }
    }
}
