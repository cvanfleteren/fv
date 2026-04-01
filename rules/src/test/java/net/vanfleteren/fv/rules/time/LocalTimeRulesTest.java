package net.vanfleteren.fv.rules.time;

import io.vavr.collection.HashMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

import static net.vanfleteren.fv.rules.time.LocalTimeRules.localTimes;
import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;

class LocalTimeRulesTest {

    @Nested
    class ClockDependent {
        @Test
        void factoryMethod_usesProvidedClock() {
            Clock fixedClock = Clock.fixed(Instant.parse("2024-01-01T12:00:00Z"), ZoneId.of("UTC"));
            LocalTimeRules rules = localTimes(fixedClock);
            // This is just a sanity check that the factory works
            validTest(LocalTime.of(12, 0), rules.equalTo(LocalTime.of(12, 0)));
        }
    }

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
        }
    }
}
