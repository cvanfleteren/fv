package net.vanfleteren.fv.rules.time;

import io.vavr.collection.HashMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static net.vanfleteren.fv.rules.time.DurationRules.durations;
import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;

class DurationRulesTest {

    @Nested
    class IsShorterThan {
        @Test
        void valid() {
            Duration limit = Duration.ofHours(1);
            validTest(Duration.ofMinutes(59), durations.isShorterThan(limit));
        }

        @Test
        void invalid() {
            Duration limit = Duration.ofHours(1);
            invalidTest(limit, durations.isShorterThan(limit), "must.be.shorter", HashMap.of("limit", limit));
            invalidTest(Duration.ofHours(2), durations.isShorterThan(limit), "must.be.shorter", HashMap.of("limit", limit));
            invalidTest(null, durations.isShorterThan(limit), "must.not.be.null");
        }
    }

    @Nested
    class IsLongerThan {
        @Test
        void valid() {
            Duration limit = Duration.ofHours(1);
            validTest(Duration.ofHours(2), durations.isLongerThan(limit));
        }

        @Test
        void invalid() {
            Duration limit = Duration.ofHours(1);
            invalidTest(limit, durations.isLongerThan(limit), "must.be.longer", HashMap.of("limit", limit));
            invalidTest(Duration.ofMinutes(30), durations.isLongerThan(limit), "must.be.longer", HashMap.of("limit", limit));
            invalidTest(null, durations.isLongerThan(limit), "must.not.be.null");
        }
    }

    @Nested
    class Between {
        @Test
        void valid() {
            Duration min = Duration.ofMinutes(10);
            Duration max = Duration.ofMinutes(20);
            validTest(Duration.ofMinutes(10), durations.between(min, max));
            validTest(Duration.ofMinutes(15), durations.between(min, max));
            validTest(Duration.ofMinutes(20), durations.between(min, max));
        }

        @Test
        void invalid() {
            Duration min = Duration.ofMinutes(10);
            Duration max = Duration.ofMinutes(20);
            invalidTest(Duration.ofMinutes(9), durations.between(min, max), "must.be.between", HashMap.of("min", min, "max", max));
            invalidTest(Duration.ofMinutes(21), durations.between(min, max), "must.be.between", HashMap.of("min", min, "max", max));
            invalidTest(null, durations.between(min, max), "must.not.be.null");
        }
    }

    @Nested
    class IsAtLeast {
        @Test
        void valid() {
            Duration min = Duration.ofMinutes(10);
            validTest(Duration.ofMinutes(10), durations.isAtLeast(min));
            validTest(Duration.ofMinutes(15), durations.isAtLeast(min));
        }

        @Test
        void invalid() {
            Duration min = Duration.ofMinutes(10);
            invalidTest(Duration.ofMinutes(9), durations.isAtLeast(min), "must.be.at.least", HashMap.of("min", min));
            invalidTest(null, durations.isAtLeast(min), "must.not.be.null");
        }
    }

    @Nested
    class IsAtMost {
        @Test
        void valid() {
            Duration max = Duration.ofMinutes(20);
            validTest(Duration.ofMinutes(20), durations.isAtMost(max));
            validTest(Duration.ofMinutes(15), durations.isAtMost(max));
        }

        @Test
        void invalid() {
            Duration max = Duration.ofMinutes(20);
            invalidTest(Duration.ofMinutes(21), durations.isAtMost(max), "must.be.at.most", HashMap.of("max", max));
            invalidTest(null, durations.isAtMost(max), "must.not.be.null");
        }
    }

    @Nested
    class IsPositive {
        @Test
        void valid() {
            validTest(Duration.ofSeconds(1), durations.isPositive());
        }

        @Test
        void invalid() {
            invalidTest(Duration.ZERO, durations.isPositive(), "must.be.positive", HashMap.empty());
            invalidTest(Duration.ofSeconds(-1), durations.isPositive(), "must.be.positive", HashMap.empty());
            invalidTest(null, durations.isPositive(), "must.not.be.null");
        }
    }

    @Nested
    class IsNegative {
        @Test
        void valid() {
            validTest(Duration.ofSeconds(-1), durations.isNegative());
        }

        @Test
        void invalid() {
            invalidTest(Duration.ZERO, durations.isNegative(), "must.be.negative", HashMap.empty());
            invalidTest(Duration.ofSeconds(1), durations.isNegative(), "must.be.negative", HashMap.empty());
            invalidTest(null, durations.isNegative(), "must.not.be.null");
        }
    }
}
