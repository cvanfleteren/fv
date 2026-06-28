package be.iffy.fv.jakarta.bad;

import be.iffy.fv.jakarta.FvRule;

/** Used to verify that field/parameter-level @FvRule misconfiguration is detected at startup. */
public record BadParamAnnotation(@FvRule(Object.class) String x) {}
