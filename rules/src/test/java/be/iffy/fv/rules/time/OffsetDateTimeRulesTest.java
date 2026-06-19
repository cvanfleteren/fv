package be.iffy.fv.rules.time;

import io.vavr.collection.HashMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;
import static be.iffy.fv.rules.time.OffsetDateTimeRules.offsetDateTimes;

class OffsetDateTimeRulesTest {

    @Nested
    class IsBefore {
        @Test
        void valid() {
            OffsetDateTime limit = OffsetDateTime.now();
            validTest(limit.minusSeconds(1), offsetDateTimes.isBefore(limit));
        }

        @Test
        void invalid() {
            OffsetDateTime limit = OffsetDateTime.now();
            invalidTest(limit, offsetDateTimes.isBefore(limit), "must.be.before", HashMap.of("limit", limit));
            invalidTest(limit.plusSeconds(1), offsetDateTimes.isBefore(limit), "must.be.before", HashMap.of("limit", limit));
            invalidTest(null, offsetDateTimes.isBefore(limit), "must.not.be.null");
        }
    }

    @Nested
    class IsAfter {
        @Test
        void valid() {
            OffsetDateTime limit = OffsetDateTime.now();
            validTest(limit.plusSeconds(1), offsetDateTimes.isAfter(limit));
        }

        @Test
        void invalid() {
            OffsetDateTime limit = OffsetDateTime.now();
            invalidTest(limit, offsetDateTimes.isAfter(limit), "must.be.after", HashMap.of("limit", limit));
            invalidTest(limit.minusSeconds(1), offsetDateTimes.isAfter(limit), "must.be.after", HashMap.of("limit", limit));
            invalidTest(null, offsetDateTimes.isAfter(limit), "must.not.be.null");
        }
    }

    @Nested
    class IsPast {
        @Test
        void valid() {
            validTest(OffsetDateTime.now().minusSeconds(1), offsetDateTimes.isPast());
        }

        @Test
        void invalid() {
            invalidTest(OffsetDateTime.now().plusSeconds(10), offsetDateTimes.isPast(), "must.be.past");
            invalidTest(null, offsetDateTimes.isPast(), "must.not.be.null");
        }
    }

    @Nested
    class IsFuture {
        @Test
        void valid() {
            validTest(OffsetDateTime.now().plusSeconds(10), offsetDateTimes.isFuture());
        }

        @Test
        void invalid() {
            invalidTest(OffsetDateTime.now().minusSeconds(1), offsetDateTimes.isFuture(), "must.be.future");
            invalidTest(null, offsetDateTimes.isFuture(), "must.not.be.null");
        }
    }

    @Nested
    class Between {
        @Test
        void valid() {
            OffsetDateTime min = OffsetDateTime.of(2023, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
            OffsetDateTime max = OffsetDateTime.of(2023, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC);
            validTest(min, offsetDateTimes.between(min, max));
            validTest(OffsetDateTime.of(2023, 6, 15, 12, 0, 0, 0, ZoneOffset.UTC), offsetDateTimes.between(min, max));
            validTest(max, offsetDateTimes.between(min, max));
        }

        @Test
        void invalid() {
            OffsetDateTime min = OffsetDateTime.of(2023, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
            OffsetDateTime max = OffsetDateTime.of(2023, 12, 31, 23, 59, 59, 0, ZoneOffset.UTC);
            invalidTest(min.minusSeconds(1), offsetDateTimes.between(min, max), "must.be.between", HashMap.of("min", min, "max", max));
            invalidTest(max.plusSeconds(1), offsetDateTimes.between(min, max), "must.be.between", HashMap.of("min", min, "max", max));
            invalidTest(null, offsetDateTimes.between(min, max), "must.not.be.null");
        }
    }
}
