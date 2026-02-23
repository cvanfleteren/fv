package net.vanfleteren.fv.rules;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;

class IntegerRulesTest {

    @Nested
    class Positive {

        @Test
        void valid() {
            validTest(1, IntegerRules.positive);
            validTest(42, IntegerRules.positive);
        }

        @Test
        void invalid() {
            invalidTest(0, IntegerRules.positive, "must.be.positive");
            invalidTest(-1, IntegerRules.positive, "must.be.positive");
        }
    }

    @Nested
    class NonNegative {

        @Test
        void valid() {
            validTest(0, IntegerRules.nonNegative);
            validTest(1, IntegerRules.nonNegative);
            validTest(42, IntegerRules.nonNegative);
        }

        @Test
        void invalid() {
            invalidTest(-1, IntegerRules.nonNegative, "must.be.non.negative");
            invalidTest(-42, IntegerRules.nonNegative, "must.be.non.negative");
        }
    }

    @Nested
    class Negative {

        @Test
        void valid() {
            validTest(-1, IntegerRules.negative);
            validTest(-42, IntegerRules.negative);
        }

        @Test
        void invalid() {
            invalidTest(0, IntegerRules.negative, "must.be.negative");
            invalidTest(1, IntegerRules.negative, "must.be.negative");
        }
    }

    @Nested
    class NonPositive {

        @Test
        void valid() {
            validTest(-1, IntegerRules.nonPositive);
            validTest(0, IntegerRules.nonPositive);
        }

        @Test
        void invalid() {
            invalidTest(1, IntegerRules.nonPositive, "must.be.non.positive");
            invalidTest(42, IntegerRules.nonPositive, "must.be.non.positive");
        }
    }

    @Nested
    class Zero {

        @Test
        void valid() {
            validTest(0, IntegerRules.zero);
        }

        @Test
        void invalid() {
            invalidTest(1, IntegerRules.zero, "must.be.zero");
            invalidTest(-1, IntegerRules.zero, "must.be.zero");
        }
    }

    @Nested
    class NonZero {

        @Test
        void valid() {
            validTest(1, IntegerRules.nonZero);
            validTest(-1, IntegerRules.nonZero);
        }

        @Test
        void invalid() {
            invalidTest(0, IntegerRules.nonZero, "must.not.be.zero");
        }
    }

    @Nested
    class Even {

        @Test
        void valid() {
            validTest(0, IntegerRules.even);
            validTest(2, IntegerRules.even);
            validTest(-2, IntegerRules.even);
        }

        @Test
        void invalid() {
            invalidTest(1, IntegerRules.even, "must.be.even");
            invalidTest(-1, IntegerRules.even, "must.be.even");
        }
    }

    @Nested
    class Odd {

        @Test
        void valid() {
            validTest(1, IntegerRules.odd);
            validTest(-1, IntegerRules.odd);
        }

        @Test
        void invalid() {
            invalidTest(0, IntegerRules.odd, "must.be.odd");
            invalidTest(2, IntegerRules.odd, "must.be.odd");
            invalidTest(-2, IntegerRules.odd, "must.be.odd");
        }
    }

    @Nested
    class Min {

        @Test
        void valid() {
            validTest(2, IntegerRules.min(2));
            validTest(3, IntegerRules.min(2));
        }

        @Test
        void invalid() {
            invalidTest(1, IntegerRules.min(2), "min.value", HashMap.of("min", 2));
            invalidTest(-100, IntegerRules.min(2), "min.value", HashMap.of("min", 2));
        }
    }

    @Nested
    class Max {

        @Test
        void valid() {
            validTest(2, IntegerRules.max(2));
            validTest(1, IntegerRules.max(2));
            validTest(-100, IntegerRules.max(2));
        }

        @Test
        void invalid() {
            invalidTest(3, IntegerRules.max(2), "max.value", HashMap.of("max", 2));
            invalidTest(100, IntegerRules.max(2), "max.value", HashMap.of("max", 2));
        }
    }

    @Nested
    class Between {

        @Test
        void valid() {
            validTest(1, IntegerRules.between(1, 1));
            validTest(1, IntegerRules.between(1, 2));
            validTest(2, IntegerRules.between(1, 2));
            validTest(0, IntegerRules.between(-1, 1));
        }

        @Test
        void invalid() {
            invalidTest(0, IntegerRules.between(1, 2), "value.between", HashMap.of("min", 1, "max", 2));
            invalidTest(3, IntegerRules.between(1, 2), "value.between", HashMap.of("min", 1, "max", 2));
        }
    }

    @Nested
    class BetweenExclusive {

        @Test
        void valid() {
            validTest(0, IntegerRules.betweenExclusive(-1, 1));
            validTest(2, IntegerRules.betweenExclusive(1, 3));
        }

        @Test
        void invalid() {
            invalidTest(1, IntegerRules.betweenExclusive(1, 3), "value.between.exclusive", HashMap.of("min", 1, "max", 3));
            invalidTest(3, IntegerRules.betweenExclusive(1, 3), "value.between.exclusive", HashMap.of("min", 1, "max", 3));
            invalidTest(0, IntegerRules.betweenExclusive(0, 1), "value.between.exclusive", HashMap.of("min", 0, "max", 1));
        }
    }

    @Nested
    class GreaterThan {

        @Test
        void valid() {
            validTest(2, IntegerRules.greaterThan(1));
            validTest(42, IntegerRules.greaterThan(1));
        }

        @Test
        void invalid() {
            invalidTest(1, IntegerRules.greaterThan(1), "must.be.greater.than", HashMap.of("min", 1));
            invalidTest(0, IntegerRules.greaterThan(1), "must.be.greater.than", HashMap.of("min", 1));
        }
    }

    @Nested
    class AtLeast {

        @Test
        void valid() {
            validTest(1, IntegerRules.atLeast(1));
            validTest(2, IntegerRules.atLeast(1));
        }

        @Test
        void invalid() {
            invalidTest(0, IntegerRules.atLeast(1), "must.be.at.least", HashMap.of("min", 1));
            invalidTest(-1, IntegerRules.atLeast(1), "must.be.at.least", HashMap.of("min", 1));
        }
    }

    @Nested
    class LessThan {

        @Test
        void valid() {
            validTest(0, IntegerRules.lessThan(1));
            validTest(-1, IntegerRules.lessThan(1));
        }

        @Test
        void invalid() {
            invalidTest(1, IntegerRules.lessThan(1), "must.be.less.than", HashMap.of("max", 1));
            invalidTest(2, IntegerRules.lessThan(1), "must.be.less.than", HashMap.of("max", 1));
        }
    }

    @Nested
    class AtMost {

        @Test
        void valid() {
            validTest(1, IntegerRules.atMost(1));
            validTest(0, IntegerRules.atMost(1));
            validTest(-1, IntegerRules.atMost(1));
        }

        @Test
        void invalid() {
            invalidTest(2, IntegerRules.atMost(1), "must.be.at.most", HashMap.of("max", 1));
            invalidTest(42, IntegerRules.atMost(1), "must.be.at.most", HashMap.of("max", 1));
        }
    }

    @Nested
    class In {

        @Test
        void valid() {
            validTest(1, IntegerRules.in(HashSet.of(1, 2, 3)));
            validTest(0, IntegerRules.in(HashSet.of(0)));
        }

        @Test
        void invalid() {
            invalidTest(
                    4,
                    IntegerRules.in(HashSet.of(1, 2, 3)),
                    "must.be.in",
                    HashMap.of("allowed", HashSet.of(1, 2, 3))
            );
        }
    }

    @Nested
    class NotIn {

        @Test
        void valid() {
            validTest(1, IntegerRules.notIn(HashSet.of(2, 3)));
            validTest(0, IntegerRules.notIn(HashSet.of(1)));
        }

        @Test
        void invalid() {
            invalidTest(
                    2,
                    IntegerRules.notIn(HashSet.of(1, 2, 3)),
                    "must.not.be.in",
                    HashMap.of("forbidden", HashSet.of(1, 2, 3))
            );
        }
    }
}