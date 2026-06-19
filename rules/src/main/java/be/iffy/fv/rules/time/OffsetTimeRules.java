package be.iffy.fv.rules.time;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.rules.ComparableRules;
import be.iffy.fv.rules.IObjectRules;

import java.time.Clock;
import java.time.OffsetTime;

/**
 * Validation rules for {@link OffsetTime} values.
 */
public final class OffsetTimeRules implements ComparableRules<OffsetTime>, IObjectRules<OffsetTime> {

    private final Clock clock;

    OffsetTimeRules(Clock clock) {
        this.clock = clock;
    }

    /**
     * Singleton instance of {@link OffsetTimeRules}.
     */
    public static final OffsetTimeRules offsetTimes = new OffsetTimeRules(Clock.systemDefaultZone());

    /**
     * Returns an instance of {@link OffsetTimeRules} that uses the passed {@link java.time.Clock} for determining the current time.
     */
    public static OffsetTimeRules offsetTimes(Clock clock) {
        return new OffsetTimeRules(clock);
    }

    /**
     * Fails if the time is not before the specified limit.
     * <p>
     * Error key: {@code must.be.before}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code limit}: the limit ({@link OffsetTime})</li>
     * </ul>
     */
    public Rule<OffsetTime> isBefore(OffsetTime limit) {
        return Rule.of(
                t -> t.isBefore(limit),
                ErrorMessage.of("must.be.before", "limit", limit)
        );
    }

    /**
     * Fails if the time is not after the specified limit.
     * <p>
     * Error key: {@code must.be.after}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code limit}: the limit ({@link OffsetTime})</li>
     * </ul>
     */
    public Rule<OffsetTime> isAfter(OffsetTime limit) {
        return Rule.of(
                t -> t.isAfter(limit),
                ErrorMessage.of("must.be.after", "limit", limit)
        );
    }

    /**
     * Fails if the time is not in the past according to the provided {@link Clock}.
     * <p>
     * Error key: {@code must.be.past}
     */
    public Rule<OffsetTime> isPast() {
        return Rule.of(t -> t.isBefore(OffsetTime.now(clock)), "must.be.past");
    }

    /**
     * Fails if the time is not in the future according to the provided {@link Clock}.
     * <p>
     * Error key: {@code must.be.future}
     */
    public Rule<OffsetTime> isFuture() {
        return Rule.of(t -> t.isAfter(OffsetTime.now(clock)), "must.be.future");
    }

}
