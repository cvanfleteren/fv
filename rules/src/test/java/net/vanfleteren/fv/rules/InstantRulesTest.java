package net.vanfleteren.fv.rules;

import io.vavr.collection.HashMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static net.vanfleteren.fv.rules.InstantRules.instants;
import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;

class InstantRulesTest {

    @Nested
    class ClockDependent {
        @Test
        void isPast_usesClock() {
            Instant now = Instant.parse("2024-01-01T12:00:00Z");
            Clock fixedClock = Clock.fixed(now, ZoneId.of("UTC"));
            InstantRules rules = instants(fixedClock);

            validTest(now.minusSeconds(1), rules.isPast());
            invalidTest(now, rules.isPast(), "must.be.past");
            invalidTest(now.plusSeconds(1), rules.isPast(), "must.be.past");
        }

        @Test
        void isFuture_usesClock() {
            Instant now = Instant.parse("2024-01-01T12:00:00Z");
            Clock fixedClock = Clock.fixed(now, ZoneId.of("UTC"));
            InstantRules rules = instants(fixedClock);

            validTest(now.plusSeconds(1), rules.isFuture());
            invalidTest(now, rules.isFuture(), "must.be.future");
            invalidTest(now.minusSeconds(1), rules.isFuture(), "must.be.future");
        }
    }

    @Nested
    class IsBefore {
        @Test
        void valid() {
            Instant limit = Instant.now();
            validTest(limit.minusSeconds(1), instants.isBefore(limit));
        }

        @Test
        void invalid() {
            Instant limit = Instant.now();
            invalidTest(limit, instants.isBefore(limit), "must.be.before", HashMap.of("limit", limit));
            invalidTest(limit.plusSeconds(1), instants.isBefore(limit), "must.be.before", HashMap.of("limit", limit));
        }
    }

    @Nested
    class IsAfter {
        @Test
        void valid() {
            Instant limit = Instant.now();
            validTest(limit.plusSeconds(1), instants.isAfter(limit));
        }

        @Test
        void invalid() {
            Instant limit = Instant.now();
            invalidTest(limit, instants.isAfter(limit), "must.be.after", HashMap.of("limit", limit));
            invalidTest(limit.minusSeconds(1), instants.isAfter(limit), "must.be.after", HashMap.of("limit", limit));
        }
    }

    @Nested
    class IsPast {
        @Test
        void valid() {
            validTest(Instant.now().minusSeconds(1), instants.isPast());
        }

        @Test
        void invalid() {
            invalidTest(Instant.now().plusSeconds(1), instants.isPast(), "must.be.past");
        }
    }

    @Nested
    class IsFuture {
        @Test
        void valid() {
            validTest(Instant.now().plusSeconds(1), instants.isFuture());
        }

        @Test
        void invalid() {
            invalidTest(Instant.now().minusSeconds(1), instants.isFuture(), "must.be.future");
        }
    }

    @Nested
    class Between {
        @Test
        void valid() {
            Instant min = Instant.parse("2023-01-01T00:00:00Z");
            Instant max = Instant.parse("2023-12-31T23:59:59Z");
            validTest(Instant.parse("2023-01-01T00:00:00Z"), instants.between(min, max));
            validTest(Instant.parse("2023-06-15T12:00:00Z"), instants.between(min, max));
            validTest(Instant.parse("2023-12-31T23:59:59Z"), instants.between(min, max));
        }

        @Test
        void invalid() {
            Instant min = Instant.parse("2023-01-01T00:00:00Z");
            Instant max = Instant.parse("2023-12-31T23:59:59Z");
            invalidTest(Instant.parse("2022-12-31T23:59:59Z"), instants.between(min, max), "must.be.between", HashMap.of("min", min, "max", max));
            invalidTest(Instant.parse("2024-01-01T00:00:00Z"), instants.between(min, max), "must.be.between", HashMap.of("min", min, "max", max));
        }
    }
}
