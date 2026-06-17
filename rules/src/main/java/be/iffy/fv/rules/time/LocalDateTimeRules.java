package be.iffy.fv.rules.time;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.rules.ComparableRules;
import be.iffy.fv.rules.IObjectRules;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Validation rules for {@link LocalDateTime} values.
 */
public final class LocalDateTimeRules implements ComparableRules<LocalDateTime>, IObjectRules<LocalDateTime> {

    private final Clock clock;

    LocalDateTimeRules(Clock clock) {
        this.clock = clock;
    }

    /**
     * Singleton instance of {@link LocalDateTimeRules}.
     */
    public static final LocalDateTimeRules localDateTimes = new LocalDateTimeRules(Clock.systemDefaultZone());

    /**
     * Returns an instance of {@link LocalDateTimeRules} that uses the passed {@link java.time.Clock} for determining the current date-time.
     */
    public static LocalDateTimeRules localDateTimes(Clock clock) {
        return new LocalDateTimeRules(clock);
    }

    /**
     * Fails if the date-time is not before the specified limit.
     * <p>
     * Error key: {@code must.be.before}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code limit}: the limit ({@link LocalDateTime})</li>
     * </ul>
     */
    public Rule<LocalDateTime> isBefore(LocalDateTime limit) {
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
     *     <li>{@code limit}: the limit ({@link LocalDateTime})</li>
     * </ul>
     */
    public Rule<LocalDateTime> isAfter(LocalDateTime limit) {
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
    public Rule<LocalDateTime> isPast() {
        return Rule.of(
                d -> d.isBefore(LocalDateTime.now(clock)),
                "must.be.past"
        );
    }

    /**
     * Fails if the date-time is not in the future according to the provided {@link Clock}.
     * <p>
     * Error key: {@code must.be.future}
     */
    public Rule<LocalDateTime> isFuture() {
        return Rule.of(
                d -> d.isAfter(LocalDateTime.now(clock)),
                "must.be.future"
        );
    }

}
