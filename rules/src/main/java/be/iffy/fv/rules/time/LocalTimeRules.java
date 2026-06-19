package be.iffy.fv.rules.time;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.rules.ComparableRules;
import be.iffy.fv.rules.IObjectRules;

import java.time.LocalTime;

/**
 * Validation rules for {@link LocalTime} values.
 */
public final class LocalTimeRules implements ComparableRules<LocalTime>, IObjectRules<LocalTime> {

    /**
     * Singleton instance of {@link LocalTimeRules}.
     */
    public static final LocalTimeRules localTimes = new LocalTimeRules();

    /**
     * Fails if the time is not before the specified limit.
     * <p>
     * Error key: {@code must.be.before}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code limit}: the limit ({@link LocalTime})</li>
     * </ul>
     */
    public Rule<LocalTime> isBefore(LocalTime limit) {
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
     *     <li>{@code limit}: the limit ({@link LocalTime})</li>
     * </ul>
     */
    public Rule<LocalTime> isAfter(LocalTime limit) {
        return Rule.of(
                t -> t.isAfter(limit),
                ErrorMessage.of("must.be.after", "limit", limit)
        );
    }

    /**
     * Fails if the time is not in the AM (before noon).
     * <p>
     * Error key: {@code must.be.am}
     */
    public Rule<LocalTime> isAm() {
        return Rule.of(
                t -> t.isBefore(LocalTime.NOON),
                "must.be.am"
        );
    }

    /**
     * Fails if the time is not in the PM (noon or later).
     * <p>
     * Error key: {@code must.be.pm}
     */
    public Rule<LocalTime> isPm() {
        return Rule.of(
                t -> !t.isBefore(LocalTime.NOON),
                "must.be.pm"
        );
    }

}
