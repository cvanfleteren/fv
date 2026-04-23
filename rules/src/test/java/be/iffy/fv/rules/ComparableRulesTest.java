package be.iffy.fv.rules;

import io.vavr.collection.HashMap;
import be.iffy.fv.Rule;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static be.iffy.fv.rules.numbers.IntegerRules.ints;
import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;
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
        void valid() {
            Rule<Integer> rule = ints().between(10, 20);
            validTest(15, rule);
            validTest(10, rule);
            validTest(20, rule);
        }

        @Test
        void invalid() {
            Rule<Integer> rule = ints().between(10, 20);
            invalidTest(5, rule, "must.be.between", HashMap.of("min", 10, "max", 20));
            invalidTest(25, rule, "must.be.between", HashMap.of("min", 10, "max", 20));
            invalidTest(null, rule, "must.not.be.null");
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
        void valid() {
            Rule<Integer> rule = ints().betweenExclusive(10, 20);
            validTest(15, rule);
        }

        @Test
        void invalid() {
            Rule<Integer> rule = ints().betweenExclusive(10, 20);
            invalidTest(10, rule, "must.be.between.exclusive", HashMap.of("min", 10, "max", 20));
            invalidTest(20, rule, "must.be.between.exclusive", HashMap.of("min", 10, "max", 20));
            invalidTest(null, rule, "must.not.be.null");
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
        void valid() {
            Rule<Integer> rule = ints().greaterThan(10);
            validTest(11, rule);
        }

        @Test
        void invalid() {
            Rule<Integer> rule = ints().greaterThan(10);
            invalidTest(10, rule, "must.be.greater.than", HashMap.of("min", 10));
            invalidTest(5, rule, "must.be.greater.than", HashMap.of("min", 10));
            invalidTest(null, rule, "must.not.be.null");
        }
    }

    @Nested
    class AtLeast {

        @Test
        void valid() {
            Rule<Integer> rule = ints().atLeast(10);
            validTest(10, rule);
            validTest(20, rule);
        }

        @Test
        void invalid() {
            Rule<Integer> rule = ints().atLeast(10);
            invalidTest(5, rule, "must.be.at.least", HashMap.of("min", 10));
            invalidTest(null, rule, "must.not.be.null");
        }
    }

    @Nested
    class LessThan {

        @Test
        void valid() {
            Rule<Integer> rule = ints().lessThan(20);
            validTest(15, rule);
        }

        @Test
        void invalid() {
            Rule<Integer> rule = ints().lessThan(20);
            invalidTest(20, rule, "must.be.less.than", HashMap.of("max", 20));
            invalidTest(25, rule, "must.be.less.than", HashMap.of("max", 20));
            invalidTest(null, rule, "must.not.be.null");
        }
    }

    @Nested
    class AtMost {

        @Test
        void valid() {
            Rule<Integer> rule = ints().atMost(20);
            validTest(20, rule);
            validTest(15, rule);
        }

        @Test
        void invalid() {
            Rule<Integer> rule = ints().atMost(20);
            invalidTest(25, rule, "must.be.at.most", HashMap.of("max", 20));
            invalidTest(null, rule, "must.not.be.null");
        }
    }
}