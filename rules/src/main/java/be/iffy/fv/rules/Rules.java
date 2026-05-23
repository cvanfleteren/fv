package be.iffy.fv.rules;

import be.iffy.fv.rules.collections.*;
import be.iffy.fv.rules.functional.EitherRules;
import be.iffy.fv.rules.functional.OptionRules;
import be.iffy.fv.rules.functional.OptionalRules;
import be.iffy.fv.rules.numbers.*;
import be.iffy.fv.rules.text.StringRules;
import be.iffy.fv.rules.time.*;

public class Rules {

    public static ObjectRules objects() {
        return ObjectRules.objects;
    }

    public static BooleanRules booleans() {
        return BooleanRules.booleans;
    }

    public static StringRules strings() {
        return StringRules.strings;
    }

    public static final StringRules strings = StringRules.strings;

    public static BigDecimalRules bigDecimals() {
        return BigDecimalRules.bigDecimals;
    }

    public static BigIntegerRules bigInts() {
        return BigIntegerRules.bigInts;
    }

    public static DoubleRules doubles() {
        return DoubleRules.doubles;
    }

    public static FloatRules floats() {
        return FloatRules.floats;
    }

    public static IntegerRules ints() {
        return IntegerRules.ints;
    }

    public static LongRules longs() {
        return LongRules.longs;
    }

    public static DurationRules durations() {
        return DurationRules.durations;
    }

    public static InstantRules instants() {
        return InstantRules.instants;
    }

    public static LocalDateRules localDates() {
        return LocalDateRules.localDates;
    }

    public static LocalDateTimeRules localDateTimes() {
        return LocalDateTimeRules.localDateTimes;
    }

    public static LocalTimeRules localTimes() {
        return LocalTimeRules.localTimes;
    }

    public static YearMonthRules yearMonths() {
        return YearMonthRules.yearMonths;
    }

    public static ZonedDateTimeRules zonedDateTimes() {
        return ZonedDateTimeRules.zonedDateTimes;
    }

    public static VavrCollectionRules vavrCollections() {
        return VavrCollectionRules.vavrCollections;
    }

    public static CollectionRules collections() {
        return CollectionRules.collections;
    }

    public static VavrMapRules vavrMaps() {
        return VavrMapRules.vavrMaps;
    }

    public static MapRules maps() {
        return MapRules.maps;
    }

    public static OptionRules options() {
        return OptionRules.options;
    }

    public static OptionalRules optionals() {
        return OptionalRules.optionals;
    }

    public static <L, R> EitherRules<L, R> eithers() {
        return EitherRules.eithers();
    }
}
