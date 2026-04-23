package be.iffy.fv.rules.time;

import io.vavr.collection.HashMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;

import static be.iffy.fv.rules.time.YearMonthRules.yearMonths;
import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;

class YearMonthRulesTest {

    @Nested
    class ClockDependent {
        @Test
        void isPast_usesClock() {
            Instant now = Instant.parse("2024-01-01T12:00:00Z");
            Clock fixedClock = Clock.fixed(now, ZoneId.of("UTC"));
            YearMonth current = YearMonth.now(fixedClock);
            YearMonthRules rules = yearMonths(fixedClock);

            validTest(current.minusMonths(1), rules.isPast());
            invalidTest(current, rules.isPast(), "must.be.past");
            invalidTest(current.plusMonths(1), rules.isPast(), "must.be.past");
        }

        @Test
        void isFuture_usesClock() {
            Instant now = Instant.parse("2024-01-01T12:00:00Z");
            Clock fixedClock = Clock.fixed(now, ZoneId.of("UTC"));
            YearMonth current = YearMonth.now(fixedClock);
            YearMonthRules rules = yearMonths(fixedClock);

            validTest(current.plusMonths(1), rules.isFuture());
            invalidTest(current, rules.isFuture(), "must.be.future");
            invalidTest(current.minusMonths(1), rules.isFuture(), "must.be.future");
        }
    }

    @Nested
    class IsBefore {
        @Test
        void valid() {
            YearMonth limit = YearMonth.now();
            validTest(limit.minusMonths(1), yearMonths.isBefore(limit));
        }

        @Test
        void invalid() {
            YearMonth limit = YearMonth.now();
            invalidTest(limit, yearMonths.isBefore(limit), "must.be.before", HashMap.of("limit", limit));
            invalidTest(limit.plusMonths(1), yearMonths.isBefore(limit), "must.be.before", HashMap.of("limit", limit));
            invalidTest(null, yearMonths.isBefore(limit), "must.not.be.null");
        }
    }

    @Nested
    class IsAfter {
        @Test
        void valid() {
            YearMonth limit = YearMonth.now();
            validTest(limit.plusMonths(1), yearMonths.isAfter(limit));
        }

        @Test
        void invalid() {
            YearMonth limit = YearMonth.now();
            invalidTest(limit, yearMonths.isAfter(limit), "must.be.after", HashMap.of("limit", limit));
            invalidTest(limit.minusMonths(1), yearMonths.isAfter(limit), "must.be.after", HashMap.of("limit", limit));
            invalidTest(null, yearMonths.isAfter(limit), "must.not.be.null");
        }
    }

    @Nested
    class IsPast {
        @Test
        void valid() {
            validTest(YearMonth.now().minusMonths(1), yearMonths.isPast());
        }

        @Test
        void invalid() {
            invalidTest(YearMonth.now().plusMonths(1), yearMonths.isPast(), "must.be.past");
            invalidTest(null, yearMonths.isPast(), "must.not.be.null");
        }
    }

    @Nested
    class IsFuture {
        @Test
        void valid() {
            validTest(YearMonth.now().plusMonths(1), yearMonths.isFuture());
        }

        @Test
        void invalid() {
            invalidTest(YearMonth.now().minusMonths(1), yearMonths.isFuture(), "must.be.future");
            invalidTest(null, yearMonths.isFuture(), "must.not.be.null");
        }
    }

    @Nested
    class Between {
        @Test
        void valid() {
            YearMonth min = YearMonth.parse("2023-01");
            YearMonth max = YearMonth.parse("2023-12");
            validTest(YearMonth.parse("2023-01"), yearMonths.between(min, max));
            validTest(YearMonth.parse("2023-06"), yearMonths.between(min, max));
            validTest(YearMonth.parse("2023-12"), yearMonths.between(min, max));
        }

        @Test
        void invalid() {
            YearMonth min = YearMonth.parse("2023-01");
            YearMonth max = YearMonth.parse("2023-12");
            invalidTest(YearMonth.parse("2022-12"), yearMonths.between(min, max), "must.be.between", HashMap.of("min", min, "max", max));
            invalidTest(YearMonth.parse("2024-01"), yearMonths.between(min, max), "must.be.between", HashMap.of("min", min, "max", max));
            invalidTest(null, yearMonths.between(min, max), "must.not.be.null");
        }
    }
}
