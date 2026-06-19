package be.iffy.fv.rules.time;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.rules.ComparableRules;
import be.iffy.fv.rules.IObjectRules;

import java.time.Clock;
import java.time.OffsetDateTime;

/**
 * Validation rules for {@link OffsetDateTime} values.
 */
public final class OffsetDateTimeRules implements ComparableRules<OffsetDateTime>, IObjectRules<OffsetDateTime> {

    private final Clock clock;

    OffsetDateTimeRules(Clock clock) {
        this.clock = clock;
    }

    /**
     * Singleton instance of {@link OffsetDateTimeRules}.
     */
    public static final OffsetDateTimeRules offsetDateTimes = new OffsetDateTimeRules(Clock.systemDefaultZone());

    /**
     * Returns an instance of {@link OffsetDateTimeRules} that uses the passed {@link java.time.Clock} for determining the current date-time.
     */
    public static OffsetDateTimeRules offsetDateTimes(Clock clock) {
        return new OffsetDateTimeRules(clock);
    }

    /**
     * Fails if the date-time is not before the specified limit.
     * <p>
     * Error key: {@code must.be.before}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code limit}: the limit ({@link OffsetDateTime})</li>
     * </ul>
     */
    public Rule<OffsetDateTime> isBefore(OffsetDateTime limit) {
        return Rule.of(
                d -> d.isBefore(limit),
                ErrorMessage.of("must.be.before", "limit", limit)
        );
    }

    /**
     * Fails if the date-time is not after the specified limit.
     * <p>
     * Error key: {@code must.be.after}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code limit}: the limit ({@link OffsetDateTime})</li>
     * </ul>
     */
    public Rule<OffsetDateTime> isAfter(OffsetDateTime limit) {
        return Rule.of(
                d -> d.isAfter(limit),
                ErrorMessage.of("must.be.after", "limit", limit)
        );
    }

    /**
     * Fails if the date-time is not in the past according to the provided {@link Clock}.
     * <p>
     * Error key: {@code must.be.past}
     */
    public Rule<OffsetDateTime> isPast() {
        return Rule.of(d -> d.isBefore(OffsetDateTime.now(clock)), "must.be.past");
    }

    /**
     * Fails if the date-time is not in the future according to the provided {@link Clock}.
     * <p>
     * Error key: {@code must.be.future}
     */
    public Rule<OffsetDateTime> isFuture() {
        return Rule.of(d -> d.isAfter(OffsetDateTime.now(clock)), "must.be.future");
    }

}
