package be.iffy.fv.rules.time;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.rules.ComparableRules;
import be.iffy.fv.rules.IObjectRules;

import java.time.LocalTime;

/**
 * Validation rules for {@link LocalTime} values.
 */
public class LocalTimeRules implements ComparableRules<LocalTime>, IObjectRules<LocalTime> {

    /**
     * Singleton instance of {@link LocalTimeRules}.
     */
    public static final LocalTimeRules localTimes = new LocalTimeRules();

    /**
     * Returns the singleton instance of {@link LocalTimeRules}.
     */
    public static LocalTimeRules localTimes() {
        return localTimes;
    }

    /**
     * Fails if the time is not before the specified limit.
     * <p>
     * Error key: {@code must.be.before}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code limit}: the limit ({@link LocalTime})</li>
     * </ul>
     *
     * @param limit the limit.
     * @return a {@link Rule} checking if the time is before the limit.
     */
    public Rule<LocalTime> isBefore(LocalTime limit) {
        return Rule.notNull().and(Rule.of(
                t -> t.isBefore(limit),
                ErrorMessage.of("must.be.before", "limit", limit)
        ));
    }

    /**
     * Fails if the time is not after the specified limit.
     * <p>
     * Error key: {@code must.be.after}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code limit}: the limit ({@link LocalTime})</li>
     * </ul>
     *
     * @param limit the limit.
     * @return a {@link Rule} checking if the time is after the limit.
     */
    public Rule<LocalTime> isAfter(LocalTime limit) {
        return Rule.notNull().and(Rule.of(
                t -> t.isAfter(limit),
                ErrorMessage.of("must.be.after", "limit", limit)
        ));
    }

    /**
     * Fails if the time is not in the AM (before noon).
     * <p>
     * Error key: {@code must.be.am}
     *
     * @return a {@link Rule} checking if the time is in the AM.
     */
    public Rule<LocalTime> isAm() {
        return Rule.notNull().and(Rule.of(t -> t.isBefore(LocalTime.NOON), "must.be.am"));
    }

    /**
     * Fails if the time is not in the PM (noon or later).
     * <p>
     * Error key: {@code must.be.pm}
     *
     * @return a {@link Rule} checking if the time is in the PM.
     */
    public Rule<LocalTime> isPm() {
        return Rule.notNull().and(Rule.of(t -> !t.isBefore(LocalTime.NOON), "must.be.pm"));
    }

}
