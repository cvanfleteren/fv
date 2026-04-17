package net.vanfleteren.fv.rules.time;

import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.rules.ComparableRules;
import net.vanfleteren.fv.rules.IObjectRules;

import java.time.Clock;
import java.time.ZonedDateTime;

/**
 * Validation rules for {@link ZonedDateTime} values.
 */
public class ZonedDateTimeRules implements ComparableRules<ZonedDateTime>, IObjectRules<ZonedDateTime> {

    private final Clock clock;

    ZonedDateTimeRules(Clock clock) {
        this.clock = clock;
    }

    /**
     * Singleton instance of {@link ZonedDateTimeRules}.
     */
    public static final ZonedDateTimeRules zonedDateTimes = new ZonedDateTimeRules(Clock.systemDefaultZone());

    /**
     * Returns the singleton instance of {@link ZonedDateTimeRules}.
     */
    public static ZonedDateTimeRules zonedDateTimes() {
        return zonedDateTimes;
    }

    /**
     * Returns an instance of {@link ZonedDateTimeRules} that uses the passed {@link Clock} for determining the current date-time.
     */
    public static ZonedDateTimeRules zonedDateTimes(Clock clock) {
        return new ZonedDateTimeRules(clock);
    }

    /**
     * Fails if the date-time is not before the specified limit.
     * <p>
     * Error key: {@code must.be.before}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code limit}: the limit ({@link ZonedDateTime})</li>
     * </ul>
     *
     * @param limit the limit.
     * @return a {@link Rule} checking if the date-time is before the limit.
     */
    public Rule<ZonedDateTime> isBefore(ZonedDateTime limit) {
        return Rule.notNull().and(Rule.of(
                d -> d.isBefore(limit),
                ErrorMessage.of("must.be.before", "limit", limit)
        ));
    }

    /**
     * Fails if the date-time is not after the specified limit.
     * <p>
     * Error key: {@code must.be.after}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code limit}: the limit ({@link ZonedDateTime})</li>
     * </ul>
     *
     * @param limit the limit.
     * @return a {@link Rule} checking if the date-time is after the limit.
     */
    public Rule<ZonedDateTime> isAfter(ZonedDateTime limit) {
        return Rule.notNull().and(Rule.of(
                d -> d.isAfter(limit),
                ErrorMessage.of("must.be.after", "limit", limit)
        ));
    }

    /**
     * Fails if the date-time is not in the past according to the provided {@link Clock}..
     * <p>
     * Error key: {@code must.be.past}
     *
     * @return a {@link Rule} checking if the date-time is in the past.
     */
    public Rule<ZonedDateTime> isPast() {
        return Rule.notNull().and(Rule.of(d -> d.isBefore(ZonedDateTime.now(clock)), "must.be.past"));
    }

    /**
     * Fails if the date-time is not in the future according to the provided {@link Clock}..
     * <p>
     * Error key: {@code must.be.future}
     *
     * @return a {@link Rule} checking if the date-time is in the future.
     */
    public Rule<ZonedDateTime> isFuture() {
        return Rule.notNull().and(Rule.of(d -> d.isAfter(ZonedDateTime.now(clock)), "must.be.future"));
    }

}
