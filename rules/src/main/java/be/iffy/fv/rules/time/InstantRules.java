package be.iffy.fv.rules.time;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.rules.ComparableRules;
import be.iffy.fv.rules.IObjectRules;

import java.time.Clock;
import java.time.Instant;

public final class InstantRules implements ComparableRules<Instant>, IObjectRules<Instant> {

    private final Clock clock;

    InstantRules(Clock clock) {
        this.clock = clock;
    }

    /**
     * Singleton instance of {@link InstantRules}.
     */
    public static final InstantRules instants = new InstantRules(Clock.systemDefaultZone());

    /**
     * Returns an instance of {@link InstantRules} that uses the passed {@link java.time.Clock} for determining the current Instant.
     */
    public static InstantRules instants(Clock clock) {
        return new InstantRules(clock);
    }

    /**
     * Fails if the instant is not before the specified limit.
     * <p>
     * Error key: {@code must.be.before}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code limit}: the limit ({@link Instant})</li>
     * </ul>
     */
    public Rule<Instant> isBefore(Instant limit) {
        return Rule.of(
                d -> d.isBefore(limit),
                ErrorMessage.of("must.be.before", "limit", limit)
        );
    }

    /**
     * Fails if the Instant is not after the specified limit.
     * <p>
     * Error key: {@code must.be.after}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code limit}: the limit ({@link Instant})</li>
     * </ul>
     */
    public Rule<Instant> isAfter(Instant limit) {
        return Rule.of(
                d -> d.isAfter(limit),
                ErrorMessage.of("must.be.after", "limit", limit)
        );
    }

    /**
     * Fails if the Instant is not in the past according to the provided {@link Clock}.
     * <p>
     * Error key: {@code must.be.past}
     */
    public Rule<Instant> isPast() {
        return Rule.of(
                d -> d.isBefore(Instant.now(clock)),
                "must.be.past"
        );
    }

    /**
     * Fails if the Instant is not in the future according to the provided {@link Clock}.
     * <p>
     * Error key: {@code must.be.future}
     */
    public Rule<Instant> isFuture() {
        return Rule.of(
                d -> d.isAfter(Instant.now(clock)),
                "must.be.future"
        );
    }

}
