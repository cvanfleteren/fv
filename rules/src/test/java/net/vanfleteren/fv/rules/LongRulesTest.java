package net.vanfleteren.fv.rules;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;

class LongRulesTest {

    @Nested
    class Positive {

        @Test
        void valid() {
            validTest(1L, LongRules.positive);
            validTest(42L, LongRules.positive);
        }

        @Test
        void invalid() {
            invalidTest(0L, LongRules.positive, "must.be.positive");
            invalidTest(-1L, LongRules.positive, "must.be.positive");
        }
    }

    @Nested
    class NonNegative {

        @Test
        void valid() {
            validTest(0L, LongRules.nonNegative);
            validTest(1L, LongRules.nonNegative);
            validTest(42L, LongRules.nonNegative);
        }

        @Test
        void invalid() {
            invalidTest(-1L, LongRules.nonNegative, "must.be.non.negative");
            invalidTest(-42L, LongRules.nonNegative, "must.be.non.negative");
        }
    }

    @Nested
    class Negative {

        @Test
        void valid() {
            validTest(-1L, LongRules.negative);
            validTest(-42L, LongRules.negative);
        }

        @Test
        void invalid() {
            invalidTest(0L, LongRules.negative, "must.be.negative");
            invalidTest(1L, LongRules.negative, "must.be.negative");
        }
    }

    @Nested
    class NonPositive {

        @Test
        void valid() {
            validTest(-1L, LongRules.nonPositive);
            validTest(0L, LongRules.nonPositive);
        }

        @Test
        void invalid() {
            invalidTest(1L, LongRules.nonPositive, "must.be.non.positive");
            invalidTest(42L, LongRules.nonPositive, "must.be.non.positive");
        }
    }

    @Nested
    class Zero {

        @Test
        void valid() {
            validTest(0L, LongRules.zero);
        }

        @Test
        void invalid() {
            invalidTest(1L, LongRules.zero, "must.be.zero");
            invalidTest(-1L, LongRules.zero, "must.be.zero");
        }
    }

    @Nested
    class NonZero {

        @Test
        void valid() {
            validTest(1L, LongRules.nonZero);
            validTest(-1L, LongRules.nonZero);
        }

        @Test
        void invalid() {
            invalidTest(0L, LongRules.nonZero, "must.not.be.zero");
        }
    }

    @Nested
    class Even {

        @Test
        void valid() {
            validTest(0L, LongRules.even);
            validTest(2L, LongRules.even);
            validTest(-2L, LongRules.even);
        }

        @Test
        void invalid() {
            invalidTest(1L, LongRules.even, "must.be.even");
            invalidTest(-1L, LongRules.even, "must.be.even");
        }
    }

    @Nested
    class Odd {

        @Test
        void valid() {
            validTest(1L, LongRules.odd);
            validTest(-1L, LongRules.odd);
        }

        @Test
        void invalid() {
            invalidTest(0L, LongRules.odd, "must.be.odd");
            invalidTest(2L, LongRules.odd, "must.be.odd");
            invalidTest(-2L, LongRules.odd, "must.be.odd");
        }
    }

    @Nested
    class Min {

        @Test
        void valid() {
            validTest(2L, LongRules.min(2));
            validTest(3L, LongRules.min(2));
        }

        @Test
        void invalid() {
            invalidTest(1L, LongRules.min(2), "min.value", HashMap.of("min", 2L));
            invalidTest(-100L, LongRules.min(2), "min.value", HashMap.of("min", 2L));
        }
    }

    @Nested
    class Max {

        @Test
        void valid() {
            validTest(2L, LongRules.max(2));
            validTest(1L, LongRules.max(2));
            validTest(-100L, LongRules.max(2));
        }

        @Test
        void invalid() {
            invalidTest(3L, LongRules.max(2), "max.value", HashMap.of("max", 2L));
            invalidTest(100L, LongRules.max(2), "max.value", HashMap.of("max", 2L));
        }
    }

    @Nested
    class Between {

        @Test
        void valid() {
            validTest(1L, LongRules.between(1, 1));
            validTest(1L, LongRules.between(1, 2));
            validTest(2L, LongRules.between(1, 2));
            validTest(0L, LongRules.between(-1, 1));
        }

        @Test
        void invalid() {
            invalidTest(0L, LongRules.between(1, 2), "value.between", HashMap.of("min", 1L, "max", 2L));
            invalidTest(3L, LongRules.between(1, 2), "value.between", HashMap.of("min", 1L, "max", 2L));
        }
    }

    @Nested
    class BetweenExclusive {

        @Test
        void valid() {
            validTest(0L, LongRules.betweenExclusive(-1, 1));
            validTest(2L, LongRules.betweenExclusive(1, 3));
        }

        @Test
        void invalid() {
            invalidTest(1L, LongRules.betweenExclusive(1, 3), "value.between.exclusive", HashMap.of("min", 1L, "max", 3L));
            invalidTest(3L, LongRules.betweenExclusive(1, 3), "value.between.exclusive", HashMap.of("min", 1L, "max", 3L));
            invalidTest(0L, LongRules.betweenExclusive(0, 1), "value.between.exclusive", HashMap.of("min", 0L, "max", 1L));
        }
    }

    @Nested
    class GreaterThan {

        @Test
        void valid() {
            validTest(2L, LongRules.greaterThan(1));
            validTest(42L, LongRules.greaterThan(1));
        }

        @Test
        void invalid() {
            invalidTest(1L, LongRules.greaterThan(1), "must.be.greater.than", HashMap.of("min", 1L));
            invalidTest(0L, LongRules.greaterThan(1), "must.be.greater.than", HashMap.of("min", 1L));
        }
    }

    @Nested
    class AtLeast {

        @Test
        void valid() {
            validTest(1L, LongRules.atLeast(1));
            validTest(2L, LongRules.atLeast(1));
        }

        @Test
        void invalid() {
            invalidTest(0L, LongRules.atLeast(1), "must.be.at.least", HashMap.of("min", 1L));
            invalidTest(-1L, LongRules.atLeast(1), "must.be.at.least", HashMap.of("min", 1L));
        }
    }

    @Nested
    class LessThan {

        @Test
        void valid() {
            validTest(0L, LongRules.lessThan(1));
            validTest(-1L, LongRules.lessThan(1));
        }

        @Test
        void invalid() {
            invalidTest(1L, LongRules.lessThan(1), "must.be.less.than", HashMap.of("max", 1L));
            invalidTest(2L, LongRules.lessThan(1), "must.be.less.than", HashMap.of("max", 1L));
        }
    }

    @Nested
    class AtMost {

        @Test
        void valid() {
            validTest(1L, LongRules.atMost(1));
            validTest(0L, LongRules.atMost(1));
            validTest(-1L, LongRules.atMost(1));
        }

        @Test
        void invalid() {
            invalidTest(2L, LongRules.atMost(1), "must.be.at.most", HashMap.of("max", 1L));
            invalidTest(42L, LongRules.atMost(1), "must.be.at.most", HashMap.of("max", 1L));
        }
    }

    @Nested
    class In {

        @Test
        void valid() {
            validTest(1L, LongRules.in(HashSet.of(1L, 2L, 3L)));
            validTest(0L, LongRules.in(HashSet.of(0L)));
        }

        @Test
        void invalid() {
            invalidTest(
                    4L,
                    LongRules.in(HashSet.of(1L, 2L, 3L)),
                    "must.be.in",
                    HashMap.of("allowed", HashSet.of(1L, 2L, 3L))
            );
        }
    }

    @Nested
    class NotIn {

        @Test
        void valid() {
            validTest(1L, LongRules.notIn(HashSet.of(2L, 3L)));
            validTest(0L, LongRules.notIn(HashSet.of(1L)));
        }

        @Test
        void invalid() {
            invalidTest(
                    2L,
                    LongRules.notIn(HashSet.of(1L, 2L, 3L)),
                    "must.not.be.in",
                    HashMap.of("forbidden", HashSet.of(1L, 2L, 3L))
            );
        }
    }
}