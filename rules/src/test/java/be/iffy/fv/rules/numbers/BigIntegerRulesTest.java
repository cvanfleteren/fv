package be.iffy.fv.rules.numbers;

import io.vavr.collection.HashMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;
import static be.iffy.fv.rules.numbers.BigIntegerRules.bigIntegers;

class BigIntegerRulesTest {

    @Nested
    class Positive {
        @Test
        void valid() {
            validTest(BigInteger.ONE, bigIntegers.positive());
            validTest(new BigInteger("42"), bigIntegers.positive());
        }

        @Test
        void invalid() {
            invalidTest(null, bigIntegers.positive(), "must.not.be.null");
            invalidTest(BigInteger.ZERO, bigIntegers.positive(), "must.be.positive");
            invalidTest(BigInteger.ONE.negate(), bigIntegers.positive(), "must.be.positive");
        }
    }

    @Nested
    class NonNegative {
        @Test
        void valid() {
            validTest(BigInteger.ZERO, bigIntegers.nonNegative());
            validTest(BigInteger.ONE, bigIntegers.nonNegative());
        }

        @Test
        void invalid() {
            invalidTest(null, bigIntegers.nonNegative(), "must.not.be.null");
            invalidTest(BigInteger.ONE.negate(), bigIntegers.nonNegative(), "must.be.non.negative");
        }
    }

    @Nested
    class Negative {
        @Test
        void valid() {
            validTest(BigInteger.ONE.negate(), bigIntegers.negative());
        }

        @Test
        void invalid() {
            invalidTest(null, bigIntegers.negative(), "must.not.be.null");
            invalidTest(BigInteger.ZERO, bigIntegers.negative(), "must.be.negative");
            invalidTest(BigInteger.ONE, bigIntegers.negative(), "must.be.negative");
        }
    }

    @Nested
    class NonPositive {
        @Test
        void valid() {
            validTest(BigInteger.ONE.negate(), bigIntegers.nonPositive());
            validTest(BigInteger.ZERO, bigIntegers.nonPositive());
        }

        @Test
        void invalid() {
            invalidTest(null, bigIntegers.nonPositive(), "must.not.be.null");
            invalidTest(BigInteger.ONE, bigIntegers.nonPositive(), "must.be.non.positive");
        }
    }

    @Nested
    class Zero {
        @Test
        void valid() {
            validTest(BigInteger.ZERO, bigIntegers.zero());
        }

        @Test
        void invalid() {
            invalidTest(null, bigIntegers.zero(), "must.not.be.null");
            invalidTest(BigInteger.ONE, bigIntegers.zero(), "must.be.zero");
        }
    }

    @Nested
    class NonZero {
        @Test
        void valid() {
            validTest(BigInteger.ONE, bigIntegers.nonZero());
        }

        @Test
        void invalid() {
            invalidTest(null, bigIntegers.nonZero(), "must.not.be.null");
            invalidTest(BigInteger.ZERO, bigIntegers.nonZero(), "must.not.be.zero");
        }
    }

    @Nested
    class Odd {
        @Test
        void valid() {
            validTest(BigInteger.ONE, bigIntegers.odd());
            validTest(new BigInteger("3"), bigIntegers.odd());
            validTest(BigInteger.ONE.negate(), bigIntegers.odd());
        }

        @Test
        void invalid() {
            invalidTest(null, bigIntegers.odd(), "must.not.be.null");
            invalidTest(BigInteger.ZERO, bigIntegers.odd(), "must.be.odd");
            invalidTest(new BigInteger("2"), bigIntegers.odd(), "must.be.odd");
        }
    }

    @Nested
    class Even {
        @Test
        void valid() {
            validTest(BigInteger.ZERO, bigIntegers.even());
            validTest(new BigInteger("2"), bigIntegers.even());
            validTest(new BigInteger("-2"), bigIntegers.even());
        }

        @Test
        void invalid() {
            invalidTest(null, bigIntegers.even(), "must.not.be.null");
            invalidTest(BigInteger.ONE, bigIntegers.even(), "must.be.even");
            invalidTest(BigInteger.ONE.negate(), bigIntegers.even(), "must.be.even");
        }
    }

    @Nested
    class Min {
        @Test
        void valid() {
            validTest(new BigInteger("2"), bigIntegers.min(new BigInteger("2")));
            validTest(new BigInteger("3"), bigIntegers.min(new BigInteger("2")));
        }

        @Test
        void invalid() {
            invalidTest(null, bigIntegers.min(new BigInteger("2")), "must.not.be.null");
            invalidTest(BigInteger.ONE, bigIntegers.min(new BigInteger("2")), "must.be.at.least", HashMap.of("min", new BigInteger("2")));
        }
    }

    @Nested
    class Max {
        @Test
        void valid() {
            validTest(new BigInteger("2"), bigIntegers.max(new BigInteger("2")));
            validTest(BigInteger.ONE, bigIntegers.max(new BigInteger("2")));
        }

        @Test
        void invalid() {
            invalidTest(null, bigIntegers.max(new BigInteger("2")), "must.not.be.null");
            invalidTest(new BigInteger("3"), bigIntegers.max(new BigInteger("2")), "must.be.at.most", HashMap.of("max", new BigInteger("2")));
        }
    }

    @Nested
    class Between {
        @Test
        void valid() {
            validTest(new BigInteger("2"), bigIntegers.between(BigInteger.ONE, new BigInteger("3")));
        }

        @Test
        void invalid() {
            invalidTest(BigInteger.ZERO, bigIntegers.between(BigInteger.ONE, new BigInteger("3")), "must.be.between", HashMap.of("min", BigInteger.ONE, "max", new BigInteger("3")));
        }
    }
}
