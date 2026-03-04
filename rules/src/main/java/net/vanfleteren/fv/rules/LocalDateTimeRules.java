package net.vanfleteren.fv.rules;

import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Validation rules for {@link LocalDateTime} values.
 */
public class LocalDateTimeRules implements ComparableRules<LocalDateTime>, IObjectRules<LocalDateTime> {

    private final Clock clock;

    LocalDateTimeRules(Clock clock) {
        this.clock = clock;
    }

    /**
     * Singleton instance of {@link LocalDateTimeRules}.
     */
    public static final LocalDateTimeRules localDateTimes = new LocalDateTimeRules(Clock.systemDefaultZone());

    /**
     * Returns the singleton instance of {@link LocalDateTimeRules}.
     *
     * @return the {@link LocalDateTimeRules} instance.
     */
    public static LocalDateTimeRules localDateTimes() {
        return localDateTimes;
    }

    /**
     * Returns an instance of {@link LocalDateTimeRules} that uses the passed {@link java.time.Clock} for determining the current date-time.
     *
     * @return the {@link LocalDateTimeRules} instance.
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
     *
     * @param limit the limit.
     * @return a {@link Rule} checking if the date-time is before the limit.
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
     *
     * @param limit the limit.
     * @return a {@link Rule} checking if the date-time is after the limit.
     */
    public Rule<LocalDateTime> isAfter(LocalDateTime limit) {
        return Rule.of(
                d -> d.isAfter(limit),
                ErrorMessage.of("must.be.after", "limit", limit)
        );
    }

    /**
     * Fails if the date-time is not in the past.
     * <p>
     * Error key: {@code must.be.past}
     *
     * @return a {@link Rule} checking if the date-time is in the past.
     */
    public Rule<LocalDateTime> isPast() {
        return Rule.of(d -> d.isBefore(LocalDateTime.now(clock)), "must.be.past");
    }

    /**
     * Fails if the date-time is not in the future.
     * <p>
     * Error key: {@code must.be.future}
     *
     * @return a {@link Rule} checking if the date-time is in the future.
     */
    public Rule<LocalDateTime> isFuture() {
        return Rule.of(d -> d.isAfter(LocalDateTime.now(clock)), "must.be.future");
    }

}
