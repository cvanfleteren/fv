package be.iffy.fv.rules.numbers;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static be.iffy.fv.rules.numbers.DoubleRules.doubles;
import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;

class DoubleRulesTest {

    @Nested
    class Positive {
        @Test
        void valid() {
            validTest(1.0, doubles.positive());
        }

        @Test
        void invalid() {
            invalidTest(null, doubles.positive(), "must.not.be.null");
            invalidTest(0.0, doubles.positive(), "must.be.positive");
            invalidTest(-1.0, doubles.positive(), "must.be.positive");
        }
    }

    @Nested
    class NonNegative {
        @Test
        void valid() {
            validTest(0.0, doubles.nonNegative());
            validTest(1.0, doubles.nonNegative());
        }

        @Test
        void invalid() {
            invalidTest(null, doubles.nonNegative(), "must.not.be.null");
            invalidTest(-1.0, doubles.nonNegative(), "must.be.non.negative");
        }
    }

    @Nested
    class Negative {
        @Test
        void valid() {
            validTest(-1.0, doubles.negative());
        }

        @Test
        void invalid() {
            invalidTest(null, doubles.negative(), "must.not.be.null");
            invalidTest(0.0, doubles.negative(), "must.be.negative");
            invalidTest(1.0, doubles.negative(), "must.be.negative");
        }
    }

    @Nested
    class NonPositive {
        @Test
        void valid() {
            validTest(0.0, doubles.nonPositive());
            validTest(-1.0, doubles.nonPositive());
        }

        @Test
        void invalid() {
            invalidTest(null, doubles.nonPositive(), "must.not.be.null");
            invalidTest(1.0, doubles.nonPositive(), "must.be.non.positive");
        }
    }

    @Nested
    class Zero {
        @Test
        void valid() {
            validTest(0.0, doubles.zero());
        }

        @Test
        void invalid() {
            invalidTest(null, doubles.zero(), "must.not.be.null");
            invalidTest(1.0, doubles.zero(), "must.be.zero");
            invalidTest(-1.0, doubles.zero(), "must.be.zero");
        }
    }

    @Nested
    class NonZero {
        @Test
        void valid() {
            validTest(1.0, doubles.nonZero());
            validTest(-1.0, doubles.nonZero());
        }

        @Test
        void invalid() {
            invalidTest(null, doubles.nonZero(), "must.not.be.null");
            invalidTest(0.0, doubles.nonZero(), "must.not.be.zero");
        }
    }

    @Nested
    class Finite {
        @Test
        void valid() {
            validTest(1.0, doubles.finite());
        }

        @Test
        void invalid() {
            invalidTest(null, doubles.finite(), "must.not.be.null");
            invalidTest(Double.POSITIVE_INFINITY, doubles.finite(), "must.be.finite");
            invalidTest(Double.NaN, doubles.finite(), "must.be.finite");
        }
    }

    @Nested
    class Nan {
        @Test
        void valid() {
            validTest(Double.NaN, doubles.nan());
        }

        @Test
        void invalid() {
            invalidTest(null, doubles.nan(), "must.not.be.null");
            invalidTest(1.0, doubles.nan(), "must.be.nan");
        }
    }

    @Nested
    class NonNan {
        @Test
        void valid() {
            validTest(1.0, doubles.nonNan());
        }

        @Test
        void invalid() {
            invalidTest(null, doubles.nonNan(), "must.not.be.null");
            invalidTest(Double.NaN, doubles.nonNan(), "must.not.be.nan");
        }
    }

    @Nested
    class Min {
        @Test
        void valid() {
            validTest(2.0, doubles.min(2.0));
            validTest(3.0, doubles.min(2.0));
        }

        @Test
        void invalid() {
            invalidTest(null, doubles.min(2.0), "must.not.be.null");
            invalidTest(1.0, doubles.min(2.0), "must.be.at.least", HashMap.of("min", 2.0));
        }
    }

    @Nested
    class Max {
        @Test
        void valid() {
            validTest(2.0, doubles.max(2.0));
            validTest(1.0, doubles.max(2.0));
        }

        @Test
        void invalid() {
            invalidTest(null, doubles.max(2.0), "must.not.be.null");
            invalidTest(3.0, doubles.max(2.0), "must.be.at.most", HashMap.of("max", 2.0));
        }
    }

    @Nested
    class Between {
        @Test
        void valid() {
            validTest(1.0, doubles.between(1.0, 3.0));
            validTest(2.0, doubles.between(1.0, 3.0));
            validTest(3.0, doubles.between(1.0, 3.0));
        }

        @Test
        void invalid() {
            invalidTest(null, doubles.between(1.0, 3.0), "must.not.be.null");
            invalidTest(0.9, doubles.between(1.0, 3.0), "must.be.between", HashMap.of("min", 1.0, "max", 3.0));
            invalidTest(3.1, doubles.between(1.0, 3.0), "must.be.between", HashMap.of("min", 1.0, "max", 3.0));
        }
    }

    @Nested
    class BetweenExclusive {
        @Test
        void valid() {
            validTest(1.5, doubles.betweenExclusive(1.0, 3.0));
            validTest(2.0, doubles.betweenExclusive(1.0, 3.0));
        }

        @Test
        void invalid() {
            invalidTest(null, doubles.betweenExclusive(1.0, 3.0), "must.not.be.null");
            invalidTest(1.0, doubles.betweenExclusive(1.0, 3.0), "must.be.between.exclusive", HashMap.of("min", 1.0, "max", 3.0));
            invalidTest(3.0, doubles.betweenExclusive(1.0, 3.0), "must.be.between.exclusive", HashMap.of("min", 1.0, "max", 3.0));
        }
    }

    @Nested
    class GreaterThan {
        @Test
        void valid() {
            validTest(1.5, doubles.greaterThan(1.0));
            validTest(2.0, doubles.greaterThan(1.0));
        }

        @Test
        void invalid() {
            invalidTest(null, doubles.greaterThan(1.0), "must.not.be.null");
            invalidTest(1.0, doubles.greaterThan(1.0), "must.be.greater.than", HashMap.of("min", 1.0));
            invalidTest(0.9, doubles.greaterThan(1.0), "must.be.greater.than", HashMap.of("min", 1.0));
        }
    }

    @Nested
    class AtLeast {
        @Test
        void valid() {
            validTest(1.0, doubles.atLeast(1.0));
            validTest(2.0, doubles.atLeast(1.0));
        }

        @Test
        void invalid() {
            invalidTest(null, doubles.atLeast(1.0), "must.not.be.null");
            invalidTest(0.9, doubles.atLeast(1.0), "must.be.at.least", HashMap.of("min", 1.0));
        }
    }

    @Nested
    class LessThan {
        @Test
        void valid() {
            validTest(0.9, doubles.lessThan(1.0));
            validTest(0.0, doubles.lessThan(1.0));
        }

        @Test
        void invalid() {
            invalidTest(null, doubles.lessThan(1.0), "must.not.be.null");
            invalidTest(1.0, doubles.lessThan(1.0), "must.be.less.than", HashMap.of("max", 1.0));
            invalidTest(2.0, doubles.lessThan(1.0), "must.be.less.than", HashMap.of("max", 1.0));
        }
    }

    @Nested
    class AtMost {
        @Test
        void valid() {
            validTest(1.0, doubles.atMost(1.0));
            validTest(0.9, doubles.atMost(1.0));
        }

        @Test
        void invalid() {
            invalidTest(null, doubles.atMost(1.0), "must.not.be.null");
            invalidTest(1.1, doubles.atMost(1.0), "must.be.at.most", HashMap.of("max", 1.0));
        }
    }

    @Nested
    class OneOf {
        @Test
        void valid() {
            validTest(1.0, doubles.oneOf(1.0, 2.0, 3.0));
        }

        @Test
        void invalid() {
            invalidTest(null, doubles.oneOf(1.0, 2.0, 3.0), "must.not.be.null");
            invalidTest(4.0, doubles.oneOf(1.0, 2.0, 3.0), "must.be.one.of", HashMap.of("values", HashSet.of(1.0, 2.0, 3.0)));
        }
    }

    @Nested
    class NotOneOf {
        @Test
        void valid() {
            validTest(4.0, doubles.notOneOf(1.0, 2.0, 3.0));
        }

        @Test
        void invalid() {
            invalidTest(null, doubles.notOneOf(1.0, 2.0, 3.0), "must.not.be.null");
            invalidTest(1.0, doubles.notOneOf(1.0, 2.0, 3.0), "must.not.be.one.of", HashMap.of("values", HashSet.of(1.0, 2.0, 3.0)));
        }
    }
}
