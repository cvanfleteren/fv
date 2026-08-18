package be.iffy.fv.jakarta.bad;

import be.iffy.fv.jakarta.FvRule;

/** Used to verify that method-level @FvRule misconfiguration (return value) is detected at startup. */
public class BadReturnAnnotation {

    @FvRule(Object.class)
    public String badMethod() { return ""; }
}
