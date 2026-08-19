package be.iffy.fv.rules;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static be.iffy.fv.rules.PairRules.pairs;
import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;

class PairRulesTest {

    @Nested
    class StrictlyOrdered {

        @Test
        void valid() {
            Rule<Tuple2<Integer, Integer>> rule = pairs.strictlyOrdered();
            validTest(Tuple.of(10, 20), rule);
        }

        @Test
        void invalid() {
            Rule<Tuple2<Integer, Integer>> rule = pairs.strictlyOrdered();
            invalidTest(Tuple.of(20, 10), rule, "first.must.be.before.second");
            invalidTest(Tuple.of(10, 10), rule, "first.must.be.before.second");
            invalidTest(null, rule, "must.not.be.null");
        }
    }

    @Nested
    class Ordered {

        @Test
        void valid() {
            Rule<Tuple2<Integer, Integer>> rule = pairs.ordered();
            validTest(Tuple.of(10, 20), rule);
            validTest(Tuple.of(10, 10), rule);
        }

        @Test
        void invalid() {
            Rule<Tuple2<Integer, Integer>> rule = pairs.ordered();
            invalidTest(Tuple.of(20, 10), rule, "first.must.be.at.most.second");
            invalidTest(null, rule, "must.not.be.null");
        }
    }

    @Nested
    class StrictlyDescending {

        @Test
        void valid() {
            Rule<Tuple2<Integer, Integer>> rule = pairs.strictlyDescending();
            validTest(Tuple.of(20, 10), rule);
        }

        @Test
        void invalid() {
            Rule<Tuple2<Integer, Integer>> rule = pairs.strictlyDescending();
            invalidTest(Tuple.of(10, 20), rule, "first.must.be.after.second");
            invalidTest(Tuple.of(10, 10), rule, "first.must.be.after.second");
            invalidTest(null, rule, "must.not.be.null");
        }
    }

    @Nested
    class Descending {

        @Test
        void valid() {
            Rule<Tuple2<Integer, Integer>> rule = pairs.descending();
            validTest(Tuple.of(20, 10), rule);
            validTest(Tuple.of(10, 10), rule);
        }

        @Test
        void invalid() {
            Rule<Tuple2<Integer, Integer>> rule = pairs.descending();
            invalidTest(Tuple.of(10, 20), rule, "first.must.be.at.least.second");
            invalidTest(null, rule, "must.not.be.null");
        }
    }

    @Nested
    class Equal {

        @Test
        void valid() {
            Rule<Tuple2<Integer, Integer>> rule = pairs.equal();
            validTest(Tuple.of(10, 10), rule);
        }

        @Test
        void invalid() {
            Rule<Tuple2<Integer, Integer>> rule = pairs.equal();
            invalidTest(Tuple.of(10, 20), rule, "pair.must.be.equal");
            invalidTest(null, rule, "must.not.be.null");
        }
    }

    @Nested
    class NotEqual {

        @Test
        void valid() {
            Rule<Tuple2<Integer, Integer>> rule = pairs.notEqual();
            validTest(Tuple.of(10, 20), rule);
        }

        @Test
        void invalid() {
            Rule<Tuple2<Integer, Integer>> rule = pairs.notEqual();
            invalidTest(Tuple.of(10, 10), rule, "pair.must.not.be.equal");
            invalidTest(null, rule, "must.not.be.null");
        }
    }

    @Nested
    class SatisfiesWithErrorKey {

        @Test
        void valid() {
            Rule<Tuple2<Integer, Integer>> rule = pairs.satisfies((a, b) -> a + b == 30, "sum.must.be.thirty");
            validTest(Tuple.of(10, 20), rule);
        }

        @Test
        void invalid() {
            Rule<Tuple2<Integer, Integer>> rule = pairs.satisfies((a, b) -> a + b == 30, "sum.must.be.thirty");
            invalidTest(Tuple.of(10, 10), rule, "sum.must.be.thirty");
            invalidTest(null, rule, "must.not.be.null");
        }
    }

    @Nested
    class SatisfiesWithErrorMessage {

        @Test
        void valid() {
            Rule<Tuple2<Integer, Integer>> rule =
                    pairs.satisfies((a, b) -> a + b == 30, ErrorMessage.of("sum.must.be.thirty", "sum", 30));
            validTest(Tuple.of(10, 20), rule);
        }

        @Test
        void invalid() {
            Rule<Tuple2<Integer, Integer>> rule =
                    pairs.satisfies((a, b) -> a + b == 30, ErrorMessage.of("sum.must.be.thirty", "sum", 30));
            invalidTest(Tuple.of(10, 10), rule, "sum.must.be.thirty");
            invalidTest(null, rule, "must.not.be.null");
        }
    }
}
