package be.iffy.fv.rules.time;

import io.vavr.collection.HashMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.Year;
import java.time.ZoneId;

import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;
import static be.iffy.fv.rules.time.YearRules.years;

class YearRulesTest {

    private final Clock fixedClock = Clock.fixed(Instant.parse("2024-06-19T12:00:00Z"), ZoneId.of("UTC"));
    private final YearRules fixedYears = years(fixedClock);

    @Nested
    class IsBefore {
        @Test
        void valid() {
            Year limit = Year.of(2024);
            validTest(Year.of(2023), years.isBefore(limit));
        }

        @Test
        void invalid() {
            Year limit = Year.of(2024);
            invalidTest(Year.of(2024), years.isBefore(limit), "must.be.before", HashMap.of("limit", limit));
            invalidTest(Year.of(2025), years.isBefore(limit), "must.be.before", HashMap.of("limit", limit));
            invalidTest(null, years.isBefore(limit), "must.not.be.null");
        }
    }

    @Nested
    class IsAfter {
        @Test
        void valid() {
            Year limit = Year.of(2024);
            validTest(Year.of(2025), years.isAfter(limit));
        }

        @Test
        void invalid() {
            Year limit = Year.of(2024);
            invalidTest(Year.of(2024), years.isAfter(limit), "must.be.after", HashMap.of("limit", limit));
            invalidTest(Year.of(2023), years.isAfter(limit), "must.be.after", HashMap.of("limit", limit));
            invalidTest(null, years.isAfter(limit), "must.not.be.null");
        }
    }

    @Nested
    class IsPast {
        @Test
        void valid() {
            validTest(Year.of(2023), fixedYears.isPast());
        }

        @Test
        void invalid() {
            invalidTest(Year.of(2024), fixedYears.isPast(), "must.be.past");
            invalidTest(Year.of(2025), fixedYears.isPast(), "must.be.past");
            invalidTest(null, fixedYears.isPast(), "must.not.be.null");
        }
    }

    @Nested
    class IsFuture {
        @Test
        void valid() {
            validTest(Year.of(2025), fixedYears.isFuture());
        }

        @Test
        void invalid() {
            invalidTest(Year.of(2024), fixedYears.isFuture(), "must.be.future");
            invalidTest(Year.of(2023), fixedYears.isFuture(), "must.be.future");
            invalidTest(null, fixedYears.isFuture(), "must.not.be.null");
        }
    }

    @Nested
    class IsCurrent {
        @Test
        void valid() {
            validTest(Year.of(2024), fixedYears.isCurrent());
        }

        @Test
        void invalid() {
            invalidTest(Year.of(2023), fixedYears.isCurrent(), "must.be.current");
            invalidTest(Year.of(2025), fixedYears.isCurrent(), "must.be.current");
            invalidTest(null, fixedYears.isCurrent(), "must.not.be.null");
        }
    }

    @Nested
    class IsLeapYear {
        @Test
        void valid() {
            validTest(Year.of(2024), years.isLeapYear());
            validTest(Year.of(2000), years.isLeapYear());
        }

        @Test
        void invalid() {
            invalidTest(Year.of(2023), years.isLeapYear(), "must.be.leap.year");
            invalidTest(Year.of(2100), years.isLeapYear(), "must.be.leap.year");
            invalidTest(null, years.isLeapYear(), "must.not.be.null");
        }
    }
}
