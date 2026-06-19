package be.iffy.fv.rules.time;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.rules.ComparableRules;
import be.iffy.fv.rules.IObjectRules;

import java.time.Clock;
import java.time.Year;

/**
 * Validation rules for {@link Year} values.
 */
public final class YearRules implements ComparableRules<Year>, IObjectRules<Year> {

    private final Clock clock;

    YearRules(Clock clock) {
        this.clock = clock;
    }

    /**
     * Singleton instance of {@link YearRules}.
     */
    public static final YearRules years = new YearRules(Clock.systemDefaultZone());

    /**
     * Returns an instance of {@link YearRules} that uses the passed {@link Clock} for determining the current Year.
     */
    public static YearRules years(Clock clock) {
        return new YearRules(clock);
    }

    /**
     * Fails if the Year is not before the specified limit.
     * <p>
     * Error key: {@code must.be.before}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code limit}: the limit ({@link Year})</li>
     * </ul>
     */
    public Rule<Year> isBefore(Year limit) {
        return Rule.of(
            d -> d.isBefore(limit),
            ErrorMessage.of("must.be.before", "limit", limit)
        );
    }

    /**
     * Fails if the Year is not after the specified limit.
     * <p>
     * Error key: {@code must.be.after}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code limit}: the limit ({@link Year})</li>
     * </ul>
     */
    public Rule<Year> isAfter(Year limit) {
        return Rule.of(
            d -> d.isAfter(limit),
            ErrorMessage.of("must.be.after", "limit", limit)
        );
    }

    /**
     * Fails if the Year is not in the past.
     * <p>
     * Error key: {@code must.be.past}
     */
    public Rule<Year> isPast() {
        return Rule.of(
            d -> d.isBefore(Year.now(clock)),
            "must.be.past"
        );
    }

    /**
     * Fails if the Year is not in the future.
     * <p>
     * Error key: {@code must.be.future}
     */
    public Rule<Year> isFuture() {
        return Rule.of(
            d -> d.isAfter(Year.now(clock)),
            "must.be.future"
        );
    }

    /**
     * Fails if the Year is not the current Year.
     * <p>
     * Error key: {@code must.be.current}
     */
    public Rule<Year> isCurrent() {
        return Rule.of(
            d -> d.equals(Year.now(clock)),
            "must.be.current"
        );
    }

    /**
     * Fails if the Year is not a leap year.
     * <p>
     * Error key: {@code must.be.leap.year}
     */
    public Rule<Year> isLeapYear() {
        return Rule.of(Year::isLeap, "must.be.leap.year");
    }

}
