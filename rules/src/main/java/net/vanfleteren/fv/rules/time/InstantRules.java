package net.vanfleteren.fv.rules.time;

import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.rules.ComparableRules;
import net.vanfleteren.fv.rules.IObjectRules;

import java.time.Clock;
import java.time.Instant;

public class InstantRules implements ComparableRules<Instant>, IObjectRules<Instant> {

    private final Clock clock;

    InstantRules(Clock clock) {
        this.clock = clock;
    }

    /**
     * Singleton instance of {@link InstantRules}.
     */
    public static final InstantRules instants = new InstantRules(Clock.systemDefaultZone());

    /**
     * Returns the singleton instance of {@link InstantRules}.
     *
     * @return the {@link InstantRules} instance.
     */
    public static InstantRules instants() {
        return instants;
    }

    /**
     * Returns an instance of {@link InstantRules} that uses the passed {@link java.time.Clock} for determining the current Instant.
     *
     * @return the {@link InstantRules} instance.
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
     *
     * @param limit the limit.
     * @return a {@link Rule} checking if the Instant is before the limit.
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
     *
     * @param limit the limit.
     * @return a {@link Rule} checking if the Instant is after the limit.
     */
    public Rule<Instant> isAfter(Instant limit) {
        return Rule.of(
                d -> d.isAfter(limit),
                ErrorMessage.of("must.be.after", "limit", limit)
        );
    }

    /**
     * Fails if the Instant is not in the past.
     * <p>
     * Error key: {@code must.be.past}
     *
     * @return a {@link Rule} checking if the Instant is in the past.
     */
    public Rule<Instant> isPast() {
        return Rule.of(d -> d.isBefore(Instant.now(clock)), "must.be.past");
    }

    /**
     * Fails if the Instant is not in the future.
     * <p>
     * Error key: {@code must.be.future}
     *
     * @return a {@link Rule} checking if the Instant is in the future.
     */
    public Rule<Instant> isFuture() {
        return Rule.of(d -> d.isAfter(Instant.now(clock)), "must.be.future");
    }

}
