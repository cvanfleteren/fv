package net.vanfleteren.fv.rules;

import io.vavr.collection.HashMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static net.vanfleteren.fv.rules.LocalDateRules.localDates;
import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;

class LocalDateRulesTest {

    @Nested
    class IsBefore {
        @Test
        void valid() {
            LocalDate limit = LocalDate.now();
            validTest(limit.minusDays(1), localDates.isBefore(limit));
        }

        @Test
        void invalid() {
            LocalDate limit = LocalDate.now();
            invalidTest(limit, localDates.isBefore(limit), "must.be.before", HashMap.of("limit", limit));
            invalidTest(limit.plusDays(1), localDates.isBefore(limit), "must.be.before", HashMap.of("limit", limit));
        }
    }

    @Nested
    class IsAfter {
        @Test
        void valid() {
            LocalDate limit = LocalDate.now();
            validTest(limit.plusDays(1), localDates.isAfter(limit));
        }

        @Test
        void invalid() {
            LocalDate limit = LocalDate.now();
            invalidTest(limit, localDates.isAfter(limit), "must.be.after", HashMap.of("limit", limit));
            invalidTest(limit.minusDays(1), localDates.isAfter(limit), "must.be.after", HashMap.of("limit", limit));
        }
    }

    @Nested
    class IsPast {
        @Test
        void valid() {
            validTest(LocalDate.now().minusDays(1), localDates.isPast());
        }

        @Test
        void invalid() {
            invalidTest(LocalDate.now(), localDates.isPast(), "must.be.past");
            invalidTest(LocalDate.now().plusDays(1), localDates.isPast(), "must.be.past");
        }
    }

    @Nested
    class IsFuture {
        @Test
        void valid() {
            validTest(LocalDate.now().plusDays(1), localDates.isFuture());
        }

        @Test
        void invalid() {
            invalidTest(LocalDate.now(), localDates.isFuture(), "must.be.future");
            invalidTest(LocalDate.now().minusDays(1), localDates.isFuture(), "must.be.future");
        }
    }

    @Nested
    class IsToday {
        @Test
        void valid() {
            validTest(LocalDate.now(), localDates.isToday());
        }

        @Test
        void invalid() {
            invalidTest(LocalDate.now().minusDays(1), localDates.isToday(), "must.be.today");
            invalidTest(LocalDate.now().plusDays(1), localDates.isToday(), "must.be.today");
        }
    }

    @Nested
    class IsLeapYear {
        @Test
        void valid() {
            validTest(LocalDate.of(2024, 1, 1), localDates.isLeapYear());
            validTest(LocalDate.of(2000, 1, 1), localDates.isLeapYear());
        }

        @Test
        void invalid() {
            invalidTest(LocalDate.of(2023, 1, 1), localDates.isLeapYear(), "must.be.leap.year");
            invalidTest(LocalDate.of(2100, 1, 1), localDates.isLeapYear(), "must.be.leap.year");
        }
    }

    @Nested
    class Between {
        @Test
        void valid() {
            LocalDate min = LocalDate.of(2023, 1, 1);
            LocalDate max = LocalDate.of(2023, 12, 31);
            validTest(LocalDate.of(2023, 1, 1), localDates.between(min, max));
            validTest(LocalDate.of(2023, 6, 15), localDates.between(min, max));
            validTest(LocalDate.of(2023, 12, 31), localDates.between(min, max));
        }

        @Test
        void invalid() {
            LocalDate min = LocalDate.of(2023, 1, 1);
            LocalDate max = LocalDate.of(2023, 12, 31);
            invalidTest(LocalDate.of(2022, 12, 31), localDates.between(min, max), "must.be.between", HashMap.of("min", min, "max", max));
            invalidTest(LocalDate.of(2024, 1, 1), localDates.between(min, max), "must.be.between", HashMap.of("min", min, "max", max));
        }
    }
}
