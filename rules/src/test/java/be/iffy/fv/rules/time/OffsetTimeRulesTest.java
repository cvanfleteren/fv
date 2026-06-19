package be.iffy.fv.rules.time;

import io.vavr.collection.HashMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetTime;
import java.time.ZoneOffset;

import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;
import static be.iffy.fv.rules.time.OffsetTimeRules.offsetTimes;

class OffsetTimeRulesTest {

    @Nested
    class IsBefore {
        @Test
        void valid() {
            OffsetTime limit = OffsetTime.now();
            validTest(limit.minusSeconds(1), offsetTimes.isBefore(limit));
        }

        @Test
        void invalid() {
            OffsetTime limit = OffsetTime.now();
            invalidTest(limit, offsetTimes.isBefore(limit), "must.be.before", HashMap.of("limit", limit));
            invalidTest(limit.plusSeconds(1), offsetTimes.isBefore(limit), "must.be.before", HashMap.of("limit", limit));
            invalidTest(null, offsetTimes.isBefore(limit), "must.not.be.null");
        }
    }

    @Nested
    class IsAfter {
        @Test
        void valid() {
            OffsetTime limit = OffsetTime.now();
            validTest(limit.plusSeconds(1), offsetTimes.isAfter(limit));
        }

        @Test
        void invalid() {
            OffsetTime limit = OffsetTime.now();
            invalidTest(limit, offsetTimes.isAfter(limit), "must.be.after", HashMap.of("limit", limit));
            invalidTest(limit.minusSeconds(1), offsetTimes.isAfter(limit), "must.be.after", HashMap.of("limit", limit));
            invalidTest(null, offsetTimes.isAfter(limit), "must.not.be.null");
        }
    }

    @Nested
    class IsPast {
        @Test
        void valid() {
            validTest(OffsetTime.now().minusSeconds(1), offsetTimes.isPast());
        }

        @Test
        void invalid() {
            invalidTest(OffsetTime.now().plusSeconds(10), offsetTimes.isPast(), "must.be.past");
            invalidTest(null, offsetTimes.isPast(), "must.not.be.null");
        }
    }

    @Nested
    class IsFuture {
        @Test
        void valid() {
            validTest(OffsetTime.now().plusSeconds(10), offsetTimes.isFuture());
        }

        @Test
        void invalid() {
            invalidTest(OffsetTime.now().minusSeconds(1), offsetTimes.isFuture(), "must.be.future");
            invalidTest(null, offsetTimes.isFuture(), "must.not.be.null");
        }
    }

    @Nested
    class Between {
        @Test
        void valid() {
            OffsetTime min = OffsetTime.of(8, 0, 0, 0, ZoneOffset.UTC);
            OffsetTime max = OffsetTime.of(17, 0, 0, 0, ZoneOffset.UTC);
            validTest(min, offsetTimes.between(min, max));
            validTest(OffsetTime.of(12, 0, 0, 0, ZoneOffset.UTC), offsetTimes.between(min, max));
            validTest(max, offsetTimes.between(min, max));
        }

        @Test
        void invalid() {
            OffsetTime min = OffsetTime.of(8, 0, 0, 0, ZoneOffset.UTC);
            OffsetTime max = OffsetTime.of(17, 0, 0, 0, ZoneOffset.UTC);
            invalidTest(min.minusSeconds(1), offsetTimes.between(min, max), "must.be.between", HashMap.of("min", min, "max", max));
            invalidTest(max.plusSeconds(1), offsetTimes.between(min, max), "must.be.between", HashMap.of("min", min, "max", max));
            invalidTest(null, offsetTimes.between(min, max), "must.not.be.null");
        }
    }
}
