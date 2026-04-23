package be.iffy.fv.rules.time;

import io.vavr.collection.HashMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static be.iffy.fv.rules.time.LocalTimeRules.localTimes;
import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;

class LocalTimeRulesTest {

    @Nested
    class IsBefore {
        @Test
        void valid() {
            LocalTime limit = LocalTime.NOON;
            validTest(limit.minusHours(1), localTimes.isBefore(limit));
        }

        @Test
        void invalid() {
            LocalTime limit = LocalTime.NOON;
            invalidTest(limit, localTimes.isBefore(limit), "must.be.before", HashMap.of("limit", limit));
            invalidTest(limit.plusHours(1), localTimes.isBefore(limit), "must.be.before", HashMap.of("limit", limit));
            invalidTest(null, localTimes.isBefore(limit), "must.not.be.null");
        }
    }

    @Nested
    class IsAfter {
        @Test
        void valid() {
            LocalTime limit = LocalTime.NOON;
            validTest(limit.plusHours(1), localTimes.isAfter(limit));
        }

        @Test
        void invalid() {
            LocalTime limit = LocalTime.NOON;
            invalidTest(limit, localTimes.isAfter(limit), "must.be.after", HashMap.of("limit", limit));
            invalidTest(limit.minusHours(1), localTimes.isAfter(limit), "must.be.after", HashMap.of("limit", limit));
            invalidTest(null, localTimes.isAfter(limit), "must.not.be.null");
        }
    }

    @Nested
    class IsAm {
        @Test
        void valid() {
            validTest(LocalTime.of(0, 0), localTimes.isAm());
            validTest(LocalTime.of(11, 59, 59), localTimes.isAm());
        }

        @Test
        void invalid() {
            invalidTest(LocalTime.NOON, localTimes.isAm(), "must.be.am");
            invalidTest(LocalTime.of(13, 0), localTimes.isAm(), "must.be.am");
            invalidTest(null, localTimes.isAm(), "must.not.be.null");
        }
    }

    @Nested
    class IsPm {
        @Test
        void valid() {
            validTest(LocalTime.NOON, localTimes.isPm());
            validTest(LocalTime.of(23, 59, 59), localTimes.isPm());
        }

        @Test
        void invalid() {
            invalidTest(LocalTime.of(11, 59, 59), localTimes.isPm(), "must.be.pm");
            invalidTest(LocalTime.of(0, 0), localTimes.isPm(), "must.be.pm");
            invalidTest(null, localTimes.isPm(), "must.not.be.null");
        }
    }

    @Nested
    class Between {
        @Test
        void valid() {
            LocalTime min = LocalTime.of(9, 0);
            LocalTime max = LocalTime.of(17, 0);
            validTest(LocalTime.of(9, 0), localTimes.between(min, max));
            validTest(LocalTime.of(12, 0), localTimes.between(min, max));
            validTest(LocalTime.of(17, 0), localTimes.between(min, max));
        }

        @Test
        void invalid() {
            LocalTime min = LocalTime.of(9, 0);
            LocalTime max = LocalTime.of(17, 0);
            invalidTest(LocalTime.of(8, 59), localTimes.between(min, max), "must.be.between", HashMap.of("min", min, "max", max));
            invalidTest(LocalTime.of(17, 1), localTimes.between(min, max), "must.be.between", HashMap.of("min", min, "max", max));
            invalidTest(null, localTimes.between(min, max), "must.not.be.null");
        }
    }
}
