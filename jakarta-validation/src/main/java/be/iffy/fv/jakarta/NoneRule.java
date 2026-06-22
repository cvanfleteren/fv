package be.iffy.fv.jakarta;

import be.iffy.fv.Rule;
import be.iffy.fv.Validation;

/** Sentinel used as the default value of {@link FvRule#value()} — never instantiated. */
abstract class NoneRule implements Rule<Object> {
    private NoneRule() {}

    @Override
    public Validation<Object> apply(Object o) {
        throw new UnsupportedOperationException();
    }
}
