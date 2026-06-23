package be.iffy.fv.jakarta.bad;

import be.iffy.fv.jakarta.FvRule;

/** Used by {@code FvRuleStartupValidatorTest} to verify field-not-found error is reported. */
@FvRule(on = WrongFieldName.class, field = "NONEXISTENT")
public record WrongFieldName(String x) {}
