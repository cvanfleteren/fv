package be.iffy.fv.jakarta.bad;

import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import be.iffy.fv.jakarta.FvRule;

/** Used by {@code FvRuleStartupValidatorTest} to verify missing-constructor error is reported. */
@FvRule(MissingConstructor.Validator.class)
public record MissingConstructor(String x) {

    public static class Validator implements Rule<MissingConstructor> {
        private Validator() {} // private — not instantiable by FvRuleValidator

        @Override
        public Validation<MissingConstructor> apply(MissingConstructor v) {
            return Validation.valid(v);
        }
    }
}
