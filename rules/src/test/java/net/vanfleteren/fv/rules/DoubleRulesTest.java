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
    class NonNegative {
        @Test
        void valid() {
            validTest(0.0, doubles().nonNegative());
            validTest(1.0, doubles().nonNegative());
        }

        @Test
        void invalid() {
            invalidTest(-1.0, doubles().nonNegative(), "must.be.non.negative");
        }
    }

    @Nested
    class Negative {
        @Test
        void valid() {
            validTest(-1.0, doubles().negative());
        }

        @Test
        void invalid() {
            invalidTest(0.0, doubles().negative(), "must.be.negative");
            invalidTest(1.0, doubles().negative(), "must.be.negative");
        }
    }

    @Nested
    class NonPositive {
        @Test
        void valid() {
            validTest(0.0, doubles().nonPositive());
            validTest(-1.0, doubles().nonPositive());
        }

        @Test
        void invalid() {
            invalidTest(1.0, doubles().nonPositive(), "must.be.non.positive");
        }
    }

    @Nested
    class Zero {
        @Test
        void valid() {
            validTest(0.0, doubles().zero());
        }

        @Test
        void invalid() {
            invalidTest(1.0, doubles().zero(), "must.be.zero");
            invalidTest(-1.0, doubles().zero(), "must.be.zero");
        }
    }

    @Nested
    class NonZero {
        @Test
        void valid() {
            validTest(1.0, doubles().nonZero());
            validTest(-1.0, doubles().nonZero());
        }

        @Test
        void invalid() {
            invalidTest(0.0, doubles().nonZero(), "must.not.be.zero");
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
    class NonNan {
        @Test
        void valid() {
            validTest(1.0, doubles().nonNan());
        }

        @Test
        void invalid() {
            invalidTest(Double.NaN, doubles().nonNan(), "must.not.be.nan");
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

    @Nested
    class Max {
        @Test
        void valid() {
            validTest(2.0, doubles().max(2.0));
            validTest(1.0, doubles().max(2.0));
        }

        @Test
        void invalid() {
            invalidTest(3.0, doubles().max(2.0), "must.be.at.most", HashMap.of("max", 2.0));
        }
    }
}
