package be.iffy.fv.rules.numbers;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static be.iffy.fv.rules.numbers.FloatRules.floats;
import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;

class FloatRulesTest {

    @Nested
    class Positive {
        @Test
        void valid() {
            validTest(1.0f, floats.positive());
        }

        @Test
        void invalid() {
            invalidTest(null, floats.positive(), "must.not.be.null");
            invalidTest(0.0f, floats.positive(), "must.be.positive");
            invalidTest(-1.0f, floats.positive(), "must.be.positive");
        }
    }

    @Nested
    class NonNegative {
        @Test
        void valid() {
            validTest(0.0f, floats.nonNegative());
            validTest(1.0f, floats.nonNegative());
        }

        @Test
        void invalid() {
            invalidTest(null, floats.nonNegative(), "must.not.be.null");
            invalidTest(-1.0f, floats.nonNegative(), "must.be.non.negative");
        }
    }

    @Nested
    class Negative {
        @Test
        void valid() {
            validTest(-1.0f, floats.negative());
        }

        @Test
        void invalid() {
            invalidTest(null, floats.negative(), "must.not.be.null");
            invalidTest(0.0f, floats.negative(), "must.be.negative");
            invalidTest(1.0f, floats.negative(), "must.be.negative");
        }
    }

    @Nested
    class NonPositive {
        @Test
        void valid() {
            validTest(0.0f, floats.nonPositive());
            validTest(-1.0f, floats.nonPositive());
        }

        @Test
        void invalid() {
            invalidTest(null, floats.nonPositive(), "must.not.be.null");
            invalidTest(1.0f, floats.nonPositive(), "must.be.non.positive");
        }
    }

    @Nested
    class Zero {
        @Test
        void valid() {
            validTest(0.0f, floats.zero());
        }

        @Test
        void invalid() {
            invalidTest(null, floats.zero(), "must.not.be.null");
            invalidTest(1.0f, floats.zero(), "must.be.zero");
            invalidTest(-1.0f, floats.zero(), "must.be.zero");
        }
    }

    @Nested
    class NonZero {
        @Test
        void valid() {
            validTest(1.0f, floats.nonZero());
            validTest(-1.0f, floats.nonZero());
        }

        @Test
        void invalid() {
            invalidTest(null, floats.nonZero(), "must.not.be.null");
            invalidTest(0.0f, floats.nonZero(), "must.not.be.zero");
        }
    }

    @Nested
    class Finite {
        @Test
        void valid() {
            validTest(1.0f, floats.finite());
        }

        @Test
        void invalid() {
            invalidTest(null, floats.finite(), "must.not.be.null");
            invalidTest(Float.POSITIVE_INFINITY, floats.finite(), "must.be.finite");
            invalidTest(Float.NaN, floats.finite(), "must.be.finite");
        }
    }

    @Nested
    class Nan {
        @Test
        void valid() {
            validTest(Float.NaN, floats.nan());
        }

        @Test
        void invalid() {
            invalidTest(null, floats.nan(), "must.not.be.null");
            invalidTest(1.0f, floats.nan(), "must.be.nan");
        }
    }

    @Nested
    class NonNan {
        @Test
        void valid() {
            validTest(1.0f, floats.nonNan());
        }

        @Test
        void invalid() {
            invalidTest(null, floats.nonNan(), "must.not.be.null");
            invalidTest(Float.NaN, floats.nonNan(), "must.not.be.nan");
        }
    }

    @Nested
    class Min {
        @Test
        void valid() {
            validTest(2.0f, floats.min(2.0f));
            validTest(3.0f, floats.min(2.0f));
        }

        @Test
        void invalid() {
            invalidTest(null, floats.min(2.0f), "must.not.be.null");
            invalidTest(1.0f, floats.min(2.0f), "must.be.at.least", HashMap.of("min", 2.0f));
        }
    }

    @Nested
    class Max {
        @Test
        void valid() {
            validTest(2.0f, floats.max(2.0f));
            validTest(1.0f, floats.max(2.0f));
        }

        @Test
        void invalid() {
            invalidTest(null, floats.max(2.0f), "must.not.be.null");
            invalidTest(3.0f, floats.max(2.0f), "must.be.at.most", HashMap.of("max", 2.0f));
        }
    }

    @Nested
    class Between {
        @Test
        void valid() {
            validTest(1.0f, floats.between(1.0f, 3.0f));
            validTest(2.0f, floats.between(1.0f, 3.0f));
            validTest(3.0f, floats.between(1.0f, 3.0f));
        }

        @Test
        void invalid() {
            invalidTest(null, floats.between(1.0f, 3.0f), "must.not.be.null");
            invalidTest(0.9f, floats.between(1.0f, 3.0f), "must.be.between", HashMap.of("min", 1.0f, "max", 3.0f));
            invalidTest(3.1f, floats.between(1.0f, 3.0f), "must.be.between", HashMap.of("min", 1.0f, "max", 3.0f));
        }
    }

    @Nested
    class BetweenExclusive {
        @Test
        void valid() {
            validTest(1.5f, floats.betweenExclusive(1.0f, 3.0f));
            validTest(2.0f, floats.betweenExclusive(1.0f, 3.0f));
        }

        @Test
        void invalid() {
            invalidTest(null, floats.betweenExclusive(1.0f, 3.0f), "must.not.be.null");
            invalidTest(1.0f, floats.betweenExclusive(1.0f, 3.0f), "must.be.between.exclusive", HashMap.of("min", 1.0f, "max", 3.0f));
            invalidTest(3.0f, floats.betweenExclusive(1.0f, 3.0f), "must.be.between.exclusive", HashMap.of("min", 1.0f, "max", 3.0f));
        }
    }

    @Nested
    class GreaterThan {
        @Test
        void valid() {
            validTest(1.5f, floats.greaterThan(1.0f));
            validTest(2.0f, floats.greaterThan(1.0f));
        }

        @Test
        void invalid() {
            invalidTest(null, floats.greaterThan(1.0f), "must.not.be.null");
            invalidTest(1.0f, floats.greaterThan(1.0f), "must.be.greater.than", HashMap.of("min", 1.0f));
            invalidTest(0.9f, floats.greaterThan(1.0f), "must.be.greater.than", HashMap.of("min", 1.0f));
        }
    }

    @Nested
    class AtLeast {
        @Test
        void valid() {
            validTest(1.0f, floats.atLeast(1.0f));
            validTest(2.0f, floats.atLeast(1.0f));
        }

        @Test
        void invalid() {
            invalidTest(null, floats.atLeast(1.0f), "must.not.be.null");
            invalidTest(0.9f, floats.atLeast(1.0f), "must.be.at.least", HashMap.of("min", 1.0f));
        }
    }

    @Nested
    class LessThan {
        @Test
        void valid() {
            validTest(0.9f, floats.lessThan(1.0f));
            validTest(0.0f, floats.lessThan(1.0f));
        }

        @Test
        void invalid() {
            invalidTest(null, floats.lessThan(1.0f), "must.not.be.null");
            invalidTest(1.0f, floats.lessThan(1.0f), "must.be.less.than", HashMap.of("max", 1.0f));
            invalidTest(2.0f, floats.lessThan(1.0f), "must.be.less.than", HashMap.of("max", 1.0f));
        }
    }

    @Nested
    class AtMost {
        @Test
        void valid() {
            validTest(1.0f, floats.atMost(1.0f));
            validTest(0.9f, floats.atMost(1.0f));
        }

        @Test
        void invalid() {
            invalidTest(null, floats.atMost(1.0f), "must.not.be.null");
            invalidTest(1.1f, floats.atMost(1.0f), "must.be.at.most", HashMap.of("max", 1.0f));
        }
    }

    @Nested
    class OneOf {
        @Test
        void valid() {
            validTest(1.0f, floats.oneOf(1.0f, 2.0f, 3.0f));
        }

        @Test
        void invalid() {
            invalidTest(null, floats.oneOf(1.0f, 2.0f, 3.0f), "must.not.be.null");
            invalidTest(4.0f, floats.oneOf(1.0f, 2.0f, 3.0f), "must.be.one.of", HashMap.of("values", HashSet.of(1.0f, 2.0f, 3.0f)));
        }
    }

    @Nested
    class NotOneOf {
        @Test
        void valid() {
            validTest(4.0f, floats.notOneOf(1.0f, 2.0f, 3.0f));
        }

        @Test
        void invalid() {
            invalidTest(null, floats.notOneOf(1.0f, 2.0f, 3.0f), "must.not.be.null");
            invalidTest(1.0f, floats.notOneOf(1.0f, 2.0f, 3.0f), "must.not.be.one.of", HashMap.of("values", HashSet.of(1.0f, 2.0f, 3.0f)));
        }
    }
}
