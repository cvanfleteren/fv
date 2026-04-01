package net.vanfleteren.fv.rules.numbers;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import net.vanfleteren.fv.rules.RulesTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.rules.numbers.IntegerRules.ints;
import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;

class IntegerRulesTest {

    @Nested
    class Positive {

        @Test
        void valid() {
            validTest(1, ints().positive());
            validTest(42, ints().positive());
        }

        @Test
        void invalid() {
            invalidTest(0, ints().positive(), "must.be.positive");
            invalidTest(-1, ints().positive(), "must.be.positive");
        }
    }

    @Nested
    class NonNegative {

        @Test
        void valid() {
            validTest(0, ints().nonNegative());
            validTest(1, ints().nonNegative());
            validTest(42, ints().nonNegative());
        }

        @Test
        void invalid() {
            invalidTest(-1, ints().nonNegative(), "must.be.non.negative");
            invalidTest(-42, ints().nonNegative(), "must.be.non.negative");
        }
    }

    @Nested
    class Negative {

        @Test
        void valid() {
            validTest(-1,ints().negative());
            validTest(-42,ints().negative());
        }

        @Test
        void invalid() {
            invalidTest(0,ints().negative(), "must.be.negative");
            invalidTest(1,ints().negative(), "must.be.negative");
        }
    }

    @Nested
    class NonPositive {

        @Test
        void valid() {
            validTest(-1,ints().nonPositive());
            validTest(0,ints().nonPositive());
        }

        @Test
        void invalid() {
            invalidTest(1,ints().nonPositive(), "must.be.non.positive");
            invalidTest(42,ints().nonPositive(), "must.be.non.positive");
        }
    }

    @Nested
    class Zero {

        @Test
        void valid() {
            validTest(0,ints().zero());
        }

        @Test
        void invalid() {
            invalidTest(1,ints().zero(), "must.be.zero");
            invalidTest(-1,ints().zero(), "must.be.zero");
        }
    }

    @Nested
    class NonZero {

        @Test
        void valid() {
            validTest(1,ints().nonZero());
            validTest(-1,ints().nonZero());
        }

        @Test
        void invalid() {
            invalidTest(0,ints().nonZero(), "must.not.be.zero");
        }
    }

    @Nested
    class Even {

        @Test
        void valid() {
            validTest(0,ints().even());
            validTest(2,ints().even());
            validTest(-2,ints().even());
        }

        @Test
        void invalid() {
            invalidTest(1,ints().even(), "must.be.even");
            invalidTest(-1,ints().even(), "must.be.even");
        }
    }

    @Nested
    class Odd {

        @Test
        void valid() {
            validTest(1,ints().odd());
            validTest(-1,ints().odd());
        }

        @Test
        void invalid() {
            invalidTest(0,ints().odd(), "must.be.odd");
            invalidTest(2,ints().odd(), "must.be.odd");
            invalidTest(-2,ints().odd(), "must.be.odd");
        }
    }

    @Nested
    class Min {

        @Test
        void valid() {
            validTest(2, ints().min(2));
            validTest(3, ints().min(2));
        }

        @Test
        void invalid() {
            invalidTest(1, ints().min(2), "must.be.at.least", HashMap.of("min", 2));
            invalidTest(-100, ints().min(2), "must.be.at.least", HashMap.of("min", 2));
        }
    }

    @Nested
    class Max {

        @Test
        void valid() {
            validTest(2, ints().max(2));
            validTest(1, ints().max(2));
            validTest(-100, ints().max(2));
        }

        @Test
        void invalid() {
            invalidTest(3, ints().max(2), "must.be.at.most", HashMap.of("max", 2));
            invalidTest(100, ints().max(2), "must.be.at.most", HashMap.of("max", 2));
        }
    }

    @Nested
    class Between {

        @Test
        void valid() {
            validTest(1, ints().between(1, 1));
            validTest(1, ints().between(1, 2));
            validTest(2, ints().between(1, 2));
            validTest(0, ints().between(-1, 1));
        }

        @Test
        void invalid() {
            invalidTest(0, ints().between(1, 2), "must.be.between", HashMap.of("min", 1, "max", 2));
            invalidTest(3, ints().between(1, 2), "must.be.between", HashMap.of("min", 1, "max", 2));
        }
    }

    @Nested
    class BetweenExclusive {

        @Test
        void valid() {
            validTest(0, ints().betweenExclusive(-1, 1));
            validTest(2, ints().betweenExclusive(1, 3));
        }

        @Test
        void invalid() {
            invalidTest(1, ints().betweenExclusive(1, 3), "must.be.between.exclusive", HashMap.of("min", 1, "max", 3));
            invalidTest(3, ints().betweenExclusive(1, 3), "must.be.between.exclusive", HashMap.of("min", 1, "max", 3));
            invalidTest(0, ints().betweenExclusive(0, 1), "must.be.between.exclusive", HashMap.of("min", 0, "max", 1));
        }
    }

    @Nested
    class GreaterThan {

        @Test
        void valid() {
            validTest(2, ints().greaterThan(1));
            validTest(42, ints().greaterThan(1));
        }

        @Test
        void invalid() {
            invalidTest(1, ints().greaterThan(1), "must.be.greater.than", HashMap.of("min", 1));
            invalidTest(0, ints().greaterThan(1), "must.be.greater.than", HashMap.of("min", 1));
        }
    }

    @Nested
    class AtLeast {

        @Test
        void valid() {
            validTest(1, ints().atLeast(1));
            validTest(2, ints().atLeast(1));
        }

        @Test
        void invalid() {
            invalidTest(0, ints().atLeast(1), "must.be.at.least", HashMap.of("min", 1));
            invalidTest(-1, ints().atLeast(1), "must.be.at.least", HashMap.of("min", 1));
        }
    }

    @Nested
    class LessThan {

        @Test
        void valid() {
            validTest(0, ints().lessThan(1));
            validTest(-1, ints().lessThan(1));
        }

        @Test
        void invalid() {
            invalidTest(1, ints().lessThan(1), "must.be.less.than", HashMap.of("max", 1));
            invalidTest(2, ints().lessThan(1), "must.be.less.than", HashMap.of("max", 1));
        }
    }

    @Nested
    class AtMost {

        @Test
        void valid() {
            validTest(1, ints().atMost(1));
            validTest(0, ints().atMost(1));
            validTest(-1, ints().atMost(1));
        }

        @Test
        void invalid() {
            invalidTest(2, ints().atMost(1), "must.be.at.most", HashMap.of("max", 1));
            invalidTest(42, ints().atMost(1), "must.be.at.most", HashMap.of("max", 1));
        }
    }

    @Nested
    class OneOf {

        @Test
        void valid() {
            validTest(1, ints().oneOf(1, 2, 3));
            validTest(0, ints().oneOf(0));
        }

        @Test
        void invalid() {
            invalidTest(
                    4,
                    ints().oneOf(1, 2, 3),
                    "must.be.one.of",
                    HashMap.of("values", HashSet.of(1, 2, 3))
            );
        }
    }

    @Nested
    class NotOneOf {

        @Test
        void valid() {
            validTest(1, ints().notOneOf(2, 3));
            validTest(0, ints().notOneOf(1));
        }

        @Test
        void invalid() {
            invalidTest(
                    2,
                    ints().notOneOf(1, 2, 3),
                    "must.not.be.one.of",
                    HashMap.of("values", HashSet.of(1, 2, 3))
            );
        }
    }
}