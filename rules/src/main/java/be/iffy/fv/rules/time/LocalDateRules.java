package be.iffy.fv.rules.time;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.rules.ComparableRules;
import be.iffy.fv.rules.IObjectRules;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Validation rules for {@link LocalDate} values.
 */
public final class LocalDateRules implements ComparableRules<LocalDate>, IObjectRules<LocalDate> {

    private final Clock clock;

    LocalDateRules(Clock clock) {
        this.clock = clock;
    }

    /**
     * Singleton instance of {@link LocalDateRules}.
     */
    public static final LocalDateRules localDates = new LocalDateRules(Clock.systemDefaultZone());

    /**
     * Returns an instance of {@link LocalDateRules} that uses the passed {@link java.time.Clock} for determining the current date.
     */
    public static LocalDateRules localDates(Clock clock) {
        return new LocalDateRules(clock);
    }

    /**
     * Fails if the date is not before the specified limit.
     * <p>
     * Error key: {@code must.be.before}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code limit}: the limit ({@link LocalDate})</li>
     * </ul>
     */
    public Rule<LocalDate> isBefore(LocalDate limit) {
        return Rule.of(
                d -> d.isBefore(limit),
                ErrorMessage.of("must.be.before", "limit", limit)
        );
    }

    /**
     * Fails if the date is not after the specified limit.
     * <p>
     * Error key: {@code must.be.after}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code limit}: the limit ({@link LocalDate})</li>
     * </ul>
     */
    public Rule<LocalDate> isAfter(LocalDate limit) {
        return Rule.of(
                d -> d.isAfter(limit),
                ErrorMessage.of("must.be.after", "limit", limit)
        );
    }

    /**
     * Fails if the date is not in the past according to the provided {@link Clock}..
     * <p>
     * Error key: {@code must.be.past}
     */
    public Rule<LocalDate> isPast() {
        return Rule.of(d -> d.isBefore(LocalDate.now(clock)), "must.be.past");
    }

    /**
     * Fails if the date is not in the future according to the provided {@link Clock}..
     * <p>
     * Error key: {@code must.be.future}
     */
    public Rule<LocalDate> isFuture() {
        return Rule.of(d -> d.isAfter(LocalDate.now(clock)), "must.be.future");
    }

    /**
     * Fails if the date is not today according to the provided {@link Clock}..
     * <p>
     * Error key: {@code must.be.today}
     */
    public Rule<LocalDate> isToday() {
        return Rule.of(d -> d.isEqual(LocalDate.now(clock)), "must.be.today");
    }

    /**
     * Fails if the year of the date is not a leap year.
     * <p>
     * Error key: {@code must.be.leap.year}
     */
    public Rule<LocalDate> isLeapYear() {
        return Rule.of(LocalDate::isLeapYear, "must.be.leap.year");
    }

}
