package net.vanfleteren.fv.rules.time;

import io.vavr.collection.HashMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;

import static net.vanfleteren.fv.rules.time.ZonedDateTimeRules.zonedDateTimes;
import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;

class ZonedDateTimeRulesTest {

    @Nested
    class ClockDependent {
        @Test
        void isPast_usesClock() {
            ZonedDateTime now = ZonedDateTime.of(LocalDateTime.of(2024, 1, 1, 12, 0), ZoneId.systemDefault());
            Clock fixedClock = Clock.fixed(now.toInstant(), ZoneId.systemDefault());
            ZonedDateTimeRules rules = zonedDateTimes(fixedClock);

            validTest(now.minusSeconds(1), rules.isPast());
            invalidTest(now, rules.isPast(), "must.be.past");
            invalidTest(now.plusSeconds(1), rules.isPast(), "must.be.past");
        }

        @Test
        void isFuture_usesClock() {
            ZonedDateTime now = ZonedDateTime.of(LocalDateTime.of(2024, 1, 1, 12, 0), ZoneId.systemDefault());
            Clock fixedClock = Clock.fixed(now.toInstant(), ZoneId.systemDefault());
            ZonedDateTimeRules rules = zonedDateTimes(fixedClock);

            validTest(now.plusSeconds(1), rules.isFuture());
            invalidTest(now, rules.isFuture(), "must.be.future");
            invalidTest(now.minusSeconds(1), rules.isFuture(), "must.be.future");
        }
    }

    @Nested
    class IsBefore {
        @Test
        void valid() {
            ZonedDateTime limit = ZonedDateTime.now();
            validTest(limit.minusHours(1), zonedDateTimes.isBefore(limit));
        }

        @Test
        void invalid() {
            ZonedDateTime limit = ZonedDateTime.now();
            invalidTest(limit, zonedDateTimes.isBefore(limit), "must.be.before", HashMap.of("limit", limit));
            invalidTest(limit.plusHours(1), zonedDateTimes.isBefore(limit), "must.be.before", HashMap.of("limit", limit));
            invalidTest(null, zonedDateTimes.isBefore(limit), "must.not.be.null");
        }
    }

    @Nested
    class IsAfter {
        @Test
        void valid() {
            ZonedDateTime limit = ZonedDateTime.now();
            validTest(limit.plusHours(1), zonedDateTimes.isAfter(limit));
        }

        @Test
        void invalid() {
            ZonedDateTime limit = ZonedDateTime.now();
            invalidTest(limit, zonedDateTimes.isAfter(limit), "must.be.after", HashMap.of("limit", limit));
            invalidTest(limit.minusHours(1), zonedDateTimes.isAfter(limit), "must.be.after", HashMap.of("limit", limit));
            invalidTest(null, zonedDateTimes.isAfter(limit), "must.not.be.null");
        }
    }

    @Nested
    class IsPast {
        @Test
        void valid() {
            validTest(ZonedDateTime.now().minusHours(1), zonedDateTimes.isPast());
        }

        @Test
        void invalid() {
            invalidTest(ZonedDateTime.now().plusHours(1), zonedDateTimes.isPast(), "must.be.past");
            invalidTest(null, zonedDateTimes.isPast(), "must.not.be.null");
        }
    }

    @Nested
    class IsFuture {
        @Test
        void valid() {
            validTest(ZonedDateTime.now().plusHours(1), zonedDateTimes.isFuture());
        }

        @Test
        void invalid() {
            invalidTest(ZonedDateTime.now().minusHours(1), zonedDateTimes.isFuture(), "must.be.future");
            invalidTest(null, zonedDateTimes.isFuture(), "must.not.be.null");
        }
    }

    @Nested
    class Between {
        @Test
        void valid() {
            ZonedDateTime min = ZonedDateTime.of(2023, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
            ZonedDateTime max = ZonedDateTime.of(2023, 12, 31, 23, 59, 59, 999999999, ZoneId.systemDefault());
            validTest(ZonedDateTime.of(2023, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()), zonedDateTimes.between(min, max));
            validTest(ZonedDateTime.of(2023, 6, 15, 12, 0, 0, 0, ZoneId.systemDefault()), zonedDateTimes.between(min, max));
            validTest(ZonedDateTime.of(2023, 12, 31, 23, 59, 59, 999999999, ZoneId.systemDefault()), zonedDateTimes.between(min, max));
        }

        @Test
        void invalid() {
            ZonedDateTime min = ZonedDateTime.of(2023, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
            ZonedDateTime max = ZonedDateTime.of(2023, 12, 31, 23, 59, 59, 999999999, ZoneId.systemDefault());
            invalidTest(ZonedDateTime.of(2022, 12, 31, 23, 59, 59, 999999999, ZoneId.systemDefault()), zonedDateTimes.between(min, max), "must.be.between", HashMap.of("min", min, "max", max));
            invalidTest(ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()), zonedDateTimes.between(min, max), "must.be.between", HashMap.of("min", min, "max", max));
            invalidTest(null, zonedDateTimes.between(min, max), "must.not.be.null");
        }
    }
}
