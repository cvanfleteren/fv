
package net.vanfleteren.fv.rules.numbers;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;
import static net.vanfleteren.fv.rules.numbers.BigDecimalRules.bigDecimals;

class BigDecimalRulesTest {

    @Nested
    class Positive {

        @Test
        void valid() {
            validTest(BigDecimal.ONE, bigDecimals.positive());
            validTest(new BigDecimal("42"), bigDecimals.positive());
        }

        @Test
        void invalid() {
            invalidTest(BigDecimal.ZERO, bigDecimals.positive(), "must.be.positive");
            invalidTest(BigDecimal.ONE.negate(), bigDecimals.positive(), "must.be.positive");
        }
    }

    @Nested
    class NonNegative {

        @Test
        void valid() {
            validTest(BigDecimal.ZERO, bigDecimals.nonNegative());
            validTest(BigDecimal.ONE, bigDecimals.nonNegative());
            validTest(new BigDecimal("42"), bigDecimals.nonNegative());
        }

        @Test
        void invalid() {
            invalidTest(BigDecimal.ONE.negate(), bigDecimals.nonNegative(), "must.be.non.negative");
            invalidTest(new BigDecimal("-42"), bigDecimals.nonNegative(), "must.be.non.negative");
        }
    }

    @Nested
    class Negative {

        @Test
        void valid() {
            validTest(BigDecimal.ONE.negate(),bigDecimals.negative());
            validTest(new BigDecimal("-42"),bigDecimals.negative());
        }

        @Test
        void invalid() {
            invalidTest(BigDecimal.ZERO,bigDecimals.negative(), "must.be.negative");
            invalidTest(BigDecimal.ONE,bigDecimals.negative(), "must.be.negative");
        }
    }

    @Nested
    class NonPositive {

        @Test
        void valid() {
            validTest(BigDecimal.ONE.negate(),bigDecimals.nonPositive());
            validTest(BigDecimal.ZERO,bigDecimals.nonPositive());
        }

        @Test
        void invalid() {
            invalidTest(BigDecimal.ONE,bigDecimals.nonPositive(), "must.be.non.positive");
            invalidTest(new BigDecimal("42"),bigDecimals.nonPositive(), "must.be.non.positive");
        }
    }

    @Nested
    class Zero {

        @Test
        void valid() {
            validTest(BigDecimal.ZERO,bigDecimals.zero());
        }

        @Test
        void invalid() {
            invalidTest(BigDecimal.ONE,bigDecimals.zero(), "must.be.zero");
            invalidTest(BigDecimal.ONE.negate(),bigDecimals.zero(), "must.be.zero");
        }
    }

    @Nested
    class NonZero {

        @Test
        void valid() {
            validTest(BigDecimal.ONE,bigDecimals.nonZero());
            validTest(BigDecimal.ONE.negate(),bigDecimals.nonZero());
        }

        @Test
        void invalid() {
            invalidTest(BigDecimal.ZERO,bigDecimals.nonZero(), "must.not.be.zero");
        }
    }

    @Nested
    class Min {

        @Test
        void valid() {
            validTest(new BigDecimal("2"), bigDecimals.min(new BigDecimal("2")));
            validTest(new BigDecimal("3"), bigDecimals.min(new BigDecimal("2")));
        }

        @Test
        void invalid() {
            invalidTest(new BigDecimal("1"), bigDecimals.min(new BigDecimal("2")), "must.be.at.least", HashMap.of("min", new BigDecimal("2")));
            invalidTest(new BigDecimal("-100"), bigDecimals.min(new BigDecimal("2")), "must.be.at.least", HashMap.of("min", new BigDecimal("2")));
        }
    }

    @Nested
    class Max {

        @Test
        void valid() {
            validTest(new BigDecimal("2"), bigDecimals.max(new BigDecimal("2")));
            validTest(new BigDecimal("1"), bigDecimals.max(new BigDecimal("2")));
            validTest(new BigDecimal("-100"), bigDecimals.max(new BigDecimal("2")));
        }

        @Test
        void invalid() {
            invalidTest(new BigDecimal("3"), bigDecimals.max(new BigDecimal("2")), "must.be.at.most", HashMap.of("max", new BigDecimal("2")));
            invalidTest(new BigDecimal("100"), bigDecimals.max(new BigDecimal("2")), "must.be.at.most", HashMap.of("max", new BigDecimal("2")));
        }
    }

    @Nested
    class Between {

        @Test
        void valid() {
            validTest(BigDecimal.ONE, bigDecimals.between(BigDecimal.ONE, BigDecimal.ONE));
            validTest(BigDecimal.ONE, bigDecimals.between(BigDecimal.ONE, new BigDecimal("2")));
            validTest(new BigDecimal("2"), bigDecimals.between(BigDecimal.ONE, new BigDecimal("2")));
            validTest(BigDecimal.ZERO, bigDecimals.between(BigDecimal.ONE.negate(), BigDecimal.ONE));
        }

        @Test
        void invalid() {
            invalidTest(BigDecimal.ZERO, bigDecimals.between(BigDecimal.ONE, new BigDecimal("2")), "must.be.between", HashMap.of("min", BigDecimal.ONE, "max", new BigDecimal("2")));
            invalidTest(new BigDecimal("3"), bigDecimals.between(BigDecimal.ONE, new BigDecimal("2")), "must.be.between", HashMap.of("min", BigDecimal.ONE, "max", new BigDecimal("2")));
        }
    }

    @Nested
    class BetweenExclusive {

        @Test
        void valid() {
            validTest(BigDecimal.ZERO, bigDecimals.betweenExclusive(BigDecimal.ONE.negate(), BigDecimal.ONE));
            validTest(new BigDecimal("2"), bigDecimals.betweenExclusive(BigDecimal.ONE, new BigDecimal("3")));
        }

        @Test
        void invalid() {
            invalidTest(BigDecimal.ONE, bigDecimals.betweenExclusive(BigDecimal.ONE, new BigDecimal("3")), "must.be.between.exclusive", HashMap.of("min", BigDecimal.ONE, "max", new BigDecimal("3")));
            invalidTest(new BigDecimal("3"), bigDecimals.betweenExclusive(BigDecimal.ONE, new BigDecimal("3")), "must.be.between.exclusive", HashMap.of("min", BigDecimal.ONE, "max", new BigDecimal("3")));
            invalidTest(BigDecimal.ZERO, bigDecimals.betweenExclusive(BigDecimal.ZERO, BigDecimal.ONE), "must.be.between.exclusive", HashMap.of("min", BigDecimal.ZERO, "max", BigDecimal.ONE));
        }
    }

    @Nested
    class GreaterThan {

        @Test
        void valid() {
            validTest(new BigDecimal("2"), bigDecimals.greaterThan(BigDecimal.ONE));
            validTest(new BigDecimal("42"), bigDecimals.greaterThan(BigDecimal.ONE));
        }

        @Test
        void invalid() {
            invalidTest(BigDecimal.ONE, bigDecimals.greaterThan(BigDecimal.ONE), "must.be.greater.than", HashMap.of("min", BigDecimal.ONE));
            invalidTest(BigDecimal.ZERO, bigDecimals.greaterThan(BigDecimal.ONE), "must.be.greater.than", HashMap.of("min", BigDecimal.ONE));
        }
    }

    @Nested
    class AtLeast {

        @Test
        void valid() {
            validTest(BigDecimal.ONE, bigDecimals.atLeast(BigDecimal.ONE));
            validTest(new BigDecimal("2"), bigDecimals.atLeast(BigDecimal.ONE));
        }

        @Test
        void invalid() {
            invalidTest(BigDecimal.ZERO, bigDecimals.atLeast(BigDecimal.ONE), "must.be.at.least", HashMap.of("min", BigDecimal.ONE));
            invalidTest(BigDecimal.ONE.negate(), bigDecimals.atLeast(BigDecimal.ONE), "must.be.at.least", HashMap.of("min", BigDecimal.ONE));
        }
    }

    @Nested
    class LessThan {

        @Test
        void valid() {
            validTest(BigDecimal.ZERO, bigDecimals.lessThan(BigDecimal.ONE));
            validTest(BigDecimal.ONE.negate(), bigDecimals.lessThan(BigDecimal.ONE));
        }

        @Test
        void invalid() {
            invalidTest(BigDecimal.ONE, bigDecimals.lessThan(BigDecimal.ONE), "must.be.less.than", HashMap.of("max", BigDecimal.ONE));
            invalidTest(new BigDecimal("2"), bigDecimals.lessThan(BigDecimal.ONE), "must.be.less.than", HashMap.of("max", BigDecimal.ONE));
        }
    }

    @Nested
    class AtMost {

        @Test
        void valid() {
            validTest(BigDecimal.ONE, bigDecimals.atMost(BigDecimal.ONE));
            validTest(BigDecimal.ZERO, bigDecimals.atMost(BigDecimal.ONE));
            validTest(BigDecimal.ONE.negate(), bigDecimals.atMost(BigDecimal.ONE));
        }

        @Test
        void invalid() {
            invalidTest(new BigDecimal("2"), bigDecimals.atMost(BigDecimal.ONE), "must.be.at.most", HashMap.of("max", BigDecimal.ONE));
            invalidTest(new BigDecimal("42"), bigDecimals.atMost(BigDecimal.ONE), "must.be.at.most", HashMap.of("max", BigDecimal.ONE));
        }
    }

    @Nested
    class OneOf {

        @Test
        void valid() {
            validTest(BigDecimal.ONE, bigDecimals.oneOf(BigDecimal.ONE, new BigDecimal("2"), new BigDecimal("3")));
            validTest(BigDecimal.ZERO, bigDecimals.oneOf(BigDecimal.ZERO));
        }

        @Test
        void invalid() {
            invalidTest(
                    new BigDecimal("4"),
                    bigDecimals.oneOf(BigDecimal.ONE, new BigDecimal("2"), new BigDecimal("3")),
                    "must.be.one.of",
                    HashMap.of("values", HashSet.of(BigDecimal.ONE, new BigDecimal("2"), new BigDecimal("3")))
            );
        }
    }

    @Nested
    class NotOneOf {

        @Test
        void valid() {
            validTest(BigDecimal.ONE, bigDecimals.notOneOf(new BigDecimal("2"), new BigDecimal("3")));
            validTest(BigDecimal.ZERO, bigDecimals.notOneOf(BigDecimal.ONE));
        }

        @Test
        void invalid() {
            invalidTest(
                    new BigDecimal("2"),
                    bigDecimals.notOneOf(BigDecimal.ONE, new BigDecimal("2"), new BigDecimal("3")),
                    "must.not.be.one.of",
                    HashMap.of("values", HashSet.of(BigDecimal.ONE, new BigDecimal("2"), new BigDecimal("3")))
            );
        }
    }
    @Nested
    class NullHandling {
        @Test
        void returnsInvalidWhenNull() {
            invalidTest(null, bigDecimals.positive(), "cannot.be.null");
            invalidTest(null, bigDecimals.nonNegative(), "cannot.be.null");
            invalidTest(null, bigDecimals.negative(), "cannot.be.null");
            invalidTest(null, bigDecimals.nonPositive(), "cannot.be.null");
            invalidTest(null, bigDecimals.zero(), "cannot.be.null");
            invalidTest(null, bigDecimals.nonZero(), "cannot.be.null");
            invalidTest(null, bigDecimals.min(BigDecimal.ZERO), "cannot.be.null");
            invalidTest(null, bigDecimals.max(BigDecimal.ZERO), "cannot.be.null");
        }
    }
}