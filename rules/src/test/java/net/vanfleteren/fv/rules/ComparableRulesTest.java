package net.vanfleteren.fv.rules;

import io.vavr.collection.HashMap;
import net.vanfleteren.fv.Rule;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.rules.numbers.IntegerRules.ints;
import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ComparableRulesTest {

    @Test
    void initial_expectations() {
        assertThat(ints()).isInstanceOf(ComparableRules.class);
    }

    @Nested
    class Between {

        @Test
        void valid_whenValueIsWithinInclusiveBounds() {
            Rule<Integer> rule = ints().between(10, 20);
            validTest(15, rule);
        }

        @Test
        void invalid_whenValueIsBelowLowerBound() {
            Rule<Integer> rule = ints().between(10, 20);
            invalidTest(5, rule, "must.be.between", HashMap.of("min", 10, "max", 20));
        }

        @Test
        void invalid_whenValueIsAboveUpperBound() {
            Rule<Integer> rule = ints().between(10, 20);
            invalidTest(25, rule, "must.be.between", HashMap.of("min", 10, "max", 20));
        }

        @Test
        void invalid_whenLowerBoundIsGreaterThanUpperBound_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> ints().between(20, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("maxInclusive must be >= minInclusive");
        }
    }

    @Nested
    class BetweenExclusive {

        @Test
        void valid_whenValueIsStrictlyBetweenBounds() {
            Rule<Integer> rule = ints().betweenExclusive(10, 20);
            validTest(15, rule);
        }

        @Test
        void invalid_whenValueEqualsLowerBound() {
            Rule<Integer> rule = ints().betweenExclusive(10, 20);
            invalidTest(10, rule, "must.be.between.exclusive", HashMap.of("min", 10, "max", 20));
        }

        @Test
        void invalid_whenValueEqualsUpperBound() {
            Rule<Integer> rule = ints().betweenExclusive(10, 20);
            invalidTest(20, rule, "must.be.between.exclusive", HashMap.of("min", 10, "max", 20));
        }

        @Test
        void invalid_whenLowerBoundIsGreaterThanOrEqualToUpperBound_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> ints().betweenExclusive(20, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("maxExclusive must be > minExclusive");

            assertThatThrownBy(() -> ints().betweenExclusive(20, 20))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("maxExclusive must be > minExclusive");
        }
    }

    @Nested
    class GreaterThan {

        @Test
        void valid_whenValueIsStrictlyGreaterThanMinimum() {
            Rule<Integer> rule = ints().greaterThan(10);
            validTest(11, rule);
        }

        @Test
        void invalid_whenValueEqualsMinimum() {
            Rule<Integer> rule = ints().greaterThan(10);
            invalidTest(10, rule, "must.be.greater.than", HashMap.of("min", 10));
        }

        @Test
        void invalid_whenValueIsLessThanMinimum() {
            Rule<Integer> rule = ints().greaterThan(10);
            invalidTest(5, rule, "must.be.greater.than", HashMap.of("min", 10));
        }
    }

    @Nested
    class AtLeast {

        @Test
        void valid_whenValueIsGreaterThanOrEqualToMinimum() {
            Rule<Integer> rule = ints().atLeast(10);
            validTest(10, rule);
            validTest(20, rule);
        }

        @Test
        void invalid_whenValueIsLessThanMinimum() {
            Rule<Integer> rule = ints().atLeast(10);
            invalidTest(5, rule, "must.be.at.least", HashMap.of("min", 10));
        }
    }

    @Nested
    class LessThan {

        @Test
        void valid_whenValueIsStrictlyLessThanMaximum() {
            Rule<Integer> rule = ints().lessThan(20);
            validTest(15, rule);
        }

        @Test
        void invalid_whenValueEqualsMaximum() {
            Rule<Integer> rule = ints().lessThan(20);
            invalidTest(20, rule, "must.be.less.than", HashMap.of("max", 20));
        }

        @Test
        void invalid_whenValueIsGreaterThanMaximum() {
            Rule<Integer> rule = ints().lessThan(20);
            invalidTest(25, rule, "must.be.less.than", HashMap.of("max", 20));
        }
    }

    @Nested
    class AtMost {

        @Test
        void valid_whenValueIsLessThanOrEqualToMaximum() {
            Rule<Integer> rule = ints().atMost(20);
            validTest(20, rule);
            validTest(15, rule);
        }

        @Test
        void invalid_whenValueIsGreaterThanMaximum() {
            Rule<Integer> rule = ints().atMost(20);
            invalidTest(25, rule, "must.be.at.most", HashMap.of("max", 20));
        }
    }
}