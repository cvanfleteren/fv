package be.iffy.fv.rules.time;

import io.vavr.collection.HashMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static be.iffy.fv.rules.time.LocalDateTimeRules.localDateTimes;
import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;

class LocalDateTimeRulesTest {

    @Nested
    class ClockDependent {
        @Test
        void isPast_usesClock() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 1, 12, 0);
            Clock fixedClock = Clock.fixed(now.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
            LocalDateTimeRules rules = localDateTimes(fixedClock);

            validTest(now.minusSeconds(1), rules.isPast());
            invalidTest(now, rules.isPast(), "must.be.past");
            invalidTest(now.plusSeconds(1), rules.isPast(), "must.be.past");
        }

        @Test
        void isFuture_usesClock() {
            LocalDateTime now = LocalDateTime.of(2024, 1, 1, 12, 0);
            Clock fixedClock = Clock.fixed(now.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
            LocalDateTimeRules rules = localDateTimes(fixedClock);

            validTest(now.plusSeconds(1), rules.isFuture());
            invalidTest(now, rules.isFuture(), "must.be.future");
            invalidTest(now.minusSeconds(1), rules.isFuture(), "must.be.future");
        }
    }

    @Nested
    class IsBefore {
        @Test
        void valid() {
            LocalDateTime limit = LocalDateTime.now();
            validTest(limit.minusHours(1), localDateTimes.isBefore(limit));
        }

        @Test
        void invalid() {
            LocalDateTime limit = LocalDateTime.now();
            invalidTest(limit, localDateTimes.isBefore(limit), "must.be.before", HashMap.of("limit", limit));
            invalidTest(limit.plusHours(1), localDateTimes.isBefore(limit), "must.be.before", HashMap.of("limit", limit));
            invalidTest(null, localDateTimes.isBefore(limit), "must.not.be.null");
        }
    }

    @Nested
    class IsAfter {
        @Test
        void valid() {
            LocalDateTime limit = LocalDateTime.now();
            validTest(limit.plusHours(1), localDateTimes.isAfter(limit));
        }

        @Test
        void invalid() {
            LocalDateTime limit = LocalDateTime.now();
            invalidTest(limit, localDateTimes.isAfter(limit), "must.be.after", HashMap.of("limit", limit));
            invalidTest(limit.minusHours(1), localDateTimes.isAfter(limit), "must.be.after", HashMap.of("limit", limit));
            invalidTest(null, localDateTimes.isAfter(limit), "must.not.be.null");
        }
    }

    @Nested
    class IsPast {
        @Test
        void valid() {
            validTest(LocalDateTime.now().minusHours(1), localDateTimes.isPast());
        }

        @Test
        void invalid() {
            invalidTest(LocalDateTime.now().plusHours(1), localDateTimes.isPast(), "must.be.past");
            invalidTest(null, localDateTimes.isPast(), "must.not.be.null");
        }
    }

    @Nested
    class IsFuture {
        @Test
        void valid() {
            validTest(LocalDateTime.now().plusHours(1), localDateTimes.isFuture());
        }

        @Test
        void invalid() {
            invalidTest(LocalDateTime.now().minusHours(1), localDateTimes.isFuture(), "must.be.future");
            invalidTest(null, localDateTimes.isFuture(), "must.not.be.null");
        }
    }

    @Nested
    class Between {
        @Test
        void valid() {
            LocalDateTime min = LocalDateTime.of(2023, 1, 1, 0, 0);
            LocalDateTime max = LocalDateTime.of(2023, 12, 31, 23, 59);
            validTest(LocalDateTime.of(2023, 1, 1, 0, 0), localDateTimes.between(min, max));
            validTest(LocalDateTime.of(2023, 6, 15, 12, 0), localDateTimes.between(min, max));
            validTest(LocalDateTime.of(2023, 12, 31, 23, 59), localDateTimes.between(min, max));
        }

        @Test
        void invalid() {
            LocalDateTime min = LocalDateTime.of(2023, 1, 1, 0, 0);
            LocalDateTime max = LocalDateTime.of(2023, 12, 31, 23, 59);
            invalidTest(LocalDateTime.of(2022, 12, 31, 23, 59), localDateTimes.between(min, max), "must.be.between", HashMap.of("min", min, "max", max));
            invalidTest(LocalDateTime.of(2024, 1, 1, 0, 0), localDateTimes.between(min, max), "must.be.between", HashMap.of("min", min, "max", max));
            invalidTest(null, localDateTimes.between(min, max), "must.not.be.null");
        }
    }
}
