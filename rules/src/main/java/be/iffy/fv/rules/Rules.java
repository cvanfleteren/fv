package be.iffy.fv.rules;

import be.iffy.fv.rules.collections.*;
import be.iffy.fv.rules.functional.EitherRules;
import be.iffy.fv.rules.functional.OptionRules;
import be.iffy.fv.rules.functional.OptionalRules;
import be.iffy.fv.rules.numbers.*;
import be.iffy.fv.rules.text.StringRules;
import be.iffy.fv.rules.time.*;

public class Rules {

    public static final ObjectRules objects = ObjectRules.objects;

    public static final BooleanRules booleans = BooleanRules.booleans;

    public static final StringRules strings = StringRules.strings;

    public static final BigDecimalRules bigDecimals = BigDecimalRules.bigDecimals;

    public static final BigIntegerRules bigInts= BigIntegerRules.bigInts;

    public static final DoubleRules doubles = DoubleRules.doubles;

    public static final FloatRules floats = FloatRules.floats;

    public static final IntegerRules ints = IntegerRules.ints;

    public static final LongRules longs = LongRules.longs;

    public static final DurationRules durations = DurationRules.durations;

    public static final InstantRules instants = InstantRules.instants;

    public static final LocalDateRules localDates = LocalDateRules.localDates;

    public static final LocalDateTimeRules localDateTimes = LocalDateTimeRules.localDateTimes;

    public static final LocalTimeRules localTimes = LocalTimeRules.localTimes;

    public static final YearMonthRules yearMonths = YearMonthRules.yearMonths;

    public static final ZonedDateTimeRules zonedDateTimes = ZonedDateTimeRules.zonedDateTimes;

    public static final VavrListRules vavrLists = VavrListRules.vavrLists;

    public static final CollectionRules collections = CollectionRules.collections;

    public static final VavrMapRules vavrMaps = VavrMapRules.vavrMaps;

    public static final MapRules maps = MapRules.maps;

    public static final OptionRules options = OptionRules.options;

    public static final OptionalRules optionals = OptionalRules.optionals;

    public static <L, R> EitherRules<L, R> eithers() {
        return EitherRules.eithers();
    }
}
