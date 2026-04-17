package net.vanfleteren.fv.rules.time;

import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.rules.ComparableRules;
import net.vanfleteren.fv.rules.IObjectRules;

import java.time.Clock;
import java.time.YearMonth;

public class YearMonthRules implements ComparableRules<YearMonth>, IObjectRules<YearMonth> {

    private final Clock clock;

    YearMonthRules(Clock clock) {
        this.clock = clock;
    }

    /**
     * Singleton instance of {@link YearMonthRules}.
     */
    public static final YearMonthRules yearMonths = new YearMonthRules(Clock.systemDefaultZone());

    /**
     * Returns the singleton instance of {@link YearMonthRules}.
     */
    public static YearMonthRules yearMonths() {
        return yearMonths;
    }

    /**
     * Returns an instance of {@link YearMonthRules} that uses the passed {@link Clock} for determining the current YearMonth.
     */
    public static YearMonthRules yearMonths(Clock clock) {
        return new YearMonthRules(clock);
    }

    /**
     * Fails if the YearMonth is not before the specified limit.
     * <p>
     * Error key: {@code must.be.before}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code limit}: the limit ({@link YearMonth})</li>
     * </ul>
     *
     * @param limit the limit.
     * @return a {@link Rule} checking if the YearMonth is before the limit.
     */
    public Rule<YearMonth> isBefore(YearMonth limit) {
        return Rule.notNull().and(Rule.of(
                d -> d.isBefore(limit),
                ErrorMessage.of("must.be.before", "limit", limit)
        ));
    }

    /**
     * Fails if the YearMonth is not after the specified limit.
     * <p>
     * Error key: {@code must.be.after}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code limit}: the limit ({@link YearMonth})</li>
     * </ul>
     *
     * @param limit the limit.
     * @return a {@link Rule} checking if the YearMonth is after the limit.
     */
    public Rule<YearMonth> isAfter(YearMonth limit) {
        return Rule.notNull().and(Rule.of(
                d -> d.isAfter(limit),
                ErrorMessage.of("must.be.after", "limit", limit)
        ));
    }

    /**
     * Fails if the YearMonth is not in the past.
     * <p>
     * Error key: {@code must.be.past}
     *
     * @return a {@link Rule} checking if the YearMonth is in the past.
     */
    public Rule<YearMonth> isPast() {
        return Rule.notNull().and(Rule.of(d -> d.isBefore(YearMonth.now(clock)), "must.be.past"));
    }

    /**
     * Fails if the YearMonth is not in the future.
     * <p>
     * Error key: {@code must.be.future}
     *
     * @return a {@link Rule} checking if the YearMonth is in the future.
     */
    public Rule<YearMonth> isFuture() {
        return Rule.notNull().and(Rule.of(d ->
                d.isAfter(YearMonth.now(clock)
                ), "must.be.future"));
    }

}
