package net.vanfleteren.fv.rules;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.rules.LongRules.longs;
import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;

class LongRulesTest {

    @Nested
    class Positive {

        @Test
        void valid() {
            validTest(1L, longs.positive());
            validTest(42L, longs.positive());
        }

        @Test
        void invalid() {
            invalidTest(0L, longs.positive(), "must.be.positive");
            invalidTest(-1L, longs.positive(), "must.be.positive");
        }
    }

    @Nested
    class NonNegative {

        @Test
        void valid() {
            validTest(0L, longs.nonNegative());
            validTest(1L, longs.nonNegative());
            validTest(42L, longs.nonNegative());
        }

        @Test
        void invalid() {
            invalidTest(-1L, longs.nonNegative(), "must.be.non.negative");
            invalidTest(-42L, longs.nonNegative(), "must.be.non.negative");
        }
    }

    @Nested
    class Negative {

        @Test
        void valid() {
            validTest(-1L,longs.negative());
            validTest(-42L,longs.negative());
        }

        @Test
        void invalid() {
            invalidTest(0L,longs.negative(), "must.be.negative");
            invalidTest(1L,longs.negative(), "must.be.negative");
        }
    }

    @Nested
    class NonPositive {

        @Test
        void valid() {
            validTest(-1L,longs.nonPositive());
            validTest(0L,longs.nonPositive());
        }

        @Test
        void invalid() {
            invalidTest(1L,longs.nonPositive(), "must.be.non.positive");
            invalidTest(42L,longs.nonPositive(), "must.be.non.positive");
        }
    }

    @Nested
    class Zero {

        @Test
        void valid() {
            validTest(0L,longs.zero());
        }

        @Test
        void invalid() {
            invalidTest(1L,longs.zero(), "must.be.zero");
            invalidTest(-1L,longs.zero(), "must.be.zero");
        }
    }

    @Nested
    class NonZero {

        @Test
        void valid() {
            validTest(1L,longs.nonZero());
            validTest(-1L,longs.nonZero());
        }

        @Test
        void invalid() {
            invalidTest(0L,longs.nonZero(), "must.not.be.zero");
        }
    }

    @Nested
    class Even {

        @Test
        void valid() {
            validTest(0L,longs.even());
            validTest(2L,longs.even());
            validTest(-2L,longs.even());
        }

        @Test
        void invalid() {
            invalidTest(1L,longs.even(), "must.be.even");
            invalidTest(-1L,longs.even(), "must.be.even");
        }
    }

    @Nested
    class Odd {

        @Test
        void valid() {
            validTest(1L,longs.odd());
            validTest(-1L,longs.odd());
        }

        @Test
        void invalid() {
            invalidTest(0L,longs.odd(), "must.be.odd");
            invalidTest(2L,longs.odd(), "must.be.odd");
            invalidTest(-2L,longs.odd(), "must.be.odd");
        }
    }

    @Nested
    class Min {

        @Test
        void valid() {
            validTest(2L, longs.min(2L));
            validTest(3L, longs.min(2L));
        }

        @Test
        void invalid() {
            invalidTest(1L, longs.min(2L), "must.be.at.least", HashMap.of("min", 2L));
            invalidTest(-100L, longs.min(2L), "must.be.at.least", HashMap.of("min", 2L));
        }
    }

    @Nested
    class Max {

        @Test
        void valid() {
            validTest(2L, longs.max(2L));
            validTest(1L, longs.max(2L));
            validTest(-100L, longs.max(2L));
        }

        @Test
        void invalid() {
            invalidTest(3L, longs.max(2L), "must.be.at.most", HashMap.of("max", 2L));
            invalidTest(100L, longs.max(2L), "must.be.at.most", HashMap.of("max", 2L));
        }
    }

    @Nested
    class Between {

        @Test
        void valid() {
            validTest(1L, longs.between(1L, 1L));
            validTest(1L, longs.between(1L, 2L));
            validTest(2L, longs.between(1L, 2L));
            validTest(0L, longs.between(-1L, 1L));
        }

        @Test
        void invalid() {
            invalidTest(0L, longs.between(1L, 2L), "must.be.between", HashMap.of("min", 1L, "max", 2L));
            invalidTest(3L, longs.between(1L, 2L), "must.be.between", HashMap.of("min", 1L, "max", 2L));
        }
    }

    @Nested
    class BetweenExclusive {

        @Test
        void valid() {
            validTest(0L, longs.betweenExclusive(-1L, 1L));
            validTest(2L, longs.betweenExclusive(1L, 3L));
        }

        @Test
        void invalid() {
            invalidTest(1L, longs.betweenExclusive(1L, 3L), "must.be.between.exclusive", HashMap.of("min", 1L, "max", 3L));
            invalidTest(3L, longs.betweenExclusive(1L, 3L), "must.be.between.exclusive", HashMap.of("min", 1L, "max", 3L));
            invalidTest(0L, longs.betweenExclusive(0L, 1L), "must.be.between.exclusive", HashMap.of("min", 0L, "max", 1L));
        }
    }

    @Nested
    class GreaterThan {

        @Test
        void valid() {
            validTest(2L, longs.greaterThan(1L));
            validTest(42L, longs.greaterThan(1L));
        }

        @Test
        void invalid() {
            invalidTest(1L, longs.greaterThan(1L), "must.be.greater.than", HashMap.of("min", 1L));
            invalidTest(0L, longs.greaterThan(1L), "must.be.greater.than", HashMap.of("min", 1L));
        }
    }

    @Nested
    class AtLeast {

        @Test
        void valid() {
            validTest(1L, longs.atLeast(1L));
            validTest(2L, longs.atLeast(1L));
        }

        @Test
        void invalid() {
            invalidTest(0L, longs.atLeast(1L), "must.be.at.least", HashMap.of("min", 1L));
            invalidTest(-1L, longs.atLeast(1L), "must.be.at.least", HashMap.of("min", 1L));
        }
    }

    @Nested
    class LessThan {

        @Test
        void valid() {
            validTest(0L, longs.lessThan(1L));
            validTest(-1L, longs.lessThan(1L));
        }

        @Test
        void invalid() {
            invalidTest(1L, longs.lessThan(1L), "must.be.less.than", HashMap.of("max", 1L));
            invalidTest(2L, longs.lessThan(1L), "must.be.less.than", HashMap.of("max", 1L));
        }
    }

    @Nested
    class AtMost {

        @Test
        void valid() {
            validTest(1L, longs.atMost(1L));
            validTest(0L, longs.atMost(1L));
            validTest(-1L, longs.atMost(1L));
        }

        @Test
        void invalid() {
            invalidTest(2L, longs.atMost(1L), "must.be.at.most", HashMap.of("max", 1L));
            invalidTest(42L, longs.atMost(1L), "must.be.at.most", HashMap.of("max", 1L));
        }
    }

    @Nested
    class OneOf {

        @Test
        void valid() {
            validTest(1L, longs.oneOf(1L, 2L, 3L));
            validTest(0L, longs.oneOf(0L));
        }

        @Test
        void invalid() {
            invalidTest(
                    4L,
                    longs.oneOf(1L, 2L, 3L),
                    "must.be.one.of",
                    HashMap.of("values", HashSet.of(1L, 2L, 3L))
            );
        }
    }

    @Nested
    class NotOneOf {

        @Test
        void valid() {
            validTest(1L, longs.notOneOf(2L, 3L));
            validTest(0L, longs.notOneOf(1L));
        }

        @Test
        void invalid() {
            invalidTest(
                    2L,
                    longs.notOneOf(1L, 2L, 3L),
                    "must.not.be.one.of",
                    HashMap.of("values", HashSet.of(1L, 2L, 3L))
            );
        }
    }
}