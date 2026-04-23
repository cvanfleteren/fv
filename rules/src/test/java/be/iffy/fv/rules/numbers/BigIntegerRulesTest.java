package be.iffy.fv.rules.numbers;

import io.vavr.collection.HashMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static be.iffy.fv.rules.numbers.BigIntegerRules.bigInts;
import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;

class BigIntegerRulesTest {

    @Nested
    class Positive {
        @Test
        void valid() {
            validTest(BigInteger.ONE, bigInts().positive());
            validTest(new BigInteger("42"), bigInts().positive());
        }

        @Test
        void invalid() {
            invalidTest(null, bigInts().positive(), "must.not.be.null");
            invalidTest(BigInteger.ZERO, bigInts().positive(), "must.be.positive");
            invalidTest(BigInteger.ONE.negate(), bigInts().positive(), "must.be.positive");
        }
    }

    @Nested
    class NonNegative {
        @Test
        void valid() {
            validTest(BigInteger.ZERO, bigInts().nonNegative());
            validTest(BigInteger.ONE, bigInts().nonNegative());
        }

        @Test
        void invalid() {
            invalidTest(null, bigInts().nonNegative(), "must.not.be.null");
            invalidTest(BigInteger.ONE.negate(), bigInts().nonNegative(), "must.be.non.negative");
        }
    }

    @Nested
    class Negative {
        @Test
        void valid() {
            validTest(BigInteger.ONE.negate(), bigInts().negative());
        }

        @Test
        void invalid() {
            invalidTest(null, bigInts().negative(), "must.not.be.null");
            invalidTest(BigInteger.ZERO, bigInts().negative(), "must.be.negative");
            invalidTest(BigInteger.ONE, bigInts().negative(), "must.be.negative");
        }
    }

    @Nested
    class NonPositive {
        @Test
        void valid() {
            validTest(BigInteger.ONE.negate(), bigInts().nonPositive());
            validTest(BigInteger.ZERO, bigInts().nonPositive());
        }

        @Test
        void invalid() {
            invalidTest(null, bigInts().nonPositive(), "must.not.be.null");
            invalidTest(BigInteger.ONE, bigInts().nonPositive(), "must.be.non.positive");
        }
    }

    @Nested
    class Zero {
        @Test
        void valid() {
            validTest(BigInteger.ZERO, bigInts().zero());
        }

        @Test
        void invalid() {
            invalidTest(null, bigInts().zero(), "must.not.be.null");
            invalidTest(BigInteger.ONE, bigInts().zero(), "must.be.zero");
        }
    }

    @Nested
    class NonZero {
        @Test
        void valid() {
            validTest(BigInteger.ONE, bigInts().nonZero());
        }

        @Test
        void invalid() {
            invalidTest(null, bigInts().nonZero(), "must.not.be.null");
            invalidTest(BigInteger.ZERO, bigInts().nonZero(), "must.not.be.zero");
        }
    }

    @Nested
    class Odd {
        @Test
        void valid() {
            validTest(BigInteger.ONE, bigInts().odd());
            validTest(new BigInteger("3"), bigInts().odd());
            validTest(BigInteger.ONE.negate(), bigInts().odd());
        }

        @Test
        void invalid() {
            invalidTest(null, bigInts().odd(), "must.not.be.null");
            invalidTest(BigInteger.ZERO, bigInts().odd(), "must.be.odd");
            invalidTest(new BigInteger("2"), bigInts().odd(), "must.be.odd");
        }
    }

    @Nested
    class Even {
        @Test
        void valid() {
            validTest(BigInteger.ZERO, bigInts().even());
            validTest(new BigInteger("2"), bigInts().even());
            validTest(new BigInteger("-2"), bigInts().even());
        }

        @Test
        void invalid() {
            invalidTest(null, bigInts().even(), "must.not.be.null");
            invalidTest(BigInteger.ONE, bigInts().even(), "must.be.even");
            invalidTest(BigInteger.ONE.negate(), bigInts().even(), "must.be.even");
        }
    }

    @Nested
    class Min {
        @Test
        void valid() {
            validTest(new BigInteger("2"), bigInts().min(new BigInteger("2")));
            validTest(new BigInteger("3"), bigInts().min(new BigInteger("2")));
        }

        @Test
        void invalid() {
            invalidTest(null, bigInts().min(new BigInteger("2")), "must.not.be.null");
            invalidTest(BigInteger.ONE, bigInts().min(new BigInteger("2")), "must.be.at.least", HashMap.of("min", new BigInteger("2")));
        }
    }

    @Nested
    class Max {
        @Test
        void valid() {
            validTest(new BigInteger("2"), bigInts().max(new BigInteger("2")));
            validTest(BigInteger.ONE, bigInts().max(new BigInteger("2")));
        }

        @Test
        void invalid() {
            invalidTest(null, bigInts().max(new BigInteger("2")), "must.not.be.null");
            invalidTest(new BigInteger("3"), bigInts().max(new BigInteger("2")), "must.be.at.most", HashMap.of("max", new BigInteger("2")));
        }
    }

    @Nested
    class Between {
        @Test
        void valid() {
            validTest(new BigInteger("2"), bigInts().between(BigInteger.ONE, new BigInteger("3")));
        }

        @Test
        void invalid() {
            invalidTest(BigInteger.ZERO, bigInts().between(BigInteger.ONE, new BigInteger("3")), "must.be.between", HashMap.of("min", BigInteger.ONE, "max", new BigInteger("3")));
        }
    }
}
