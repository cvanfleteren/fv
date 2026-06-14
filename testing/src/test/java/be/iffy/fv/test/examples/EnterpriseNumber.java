package be.iffy.fv.test.examples;

import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import be.iffy.fv.ValidationFactory;

import static be.iffy.fv.dsl.DSL.*;

public record EnterpriseNumber(String value) {

    static final Rule<String> valid = after(stringOps.keepAlphanumeric()).is(strings.notBlank());

    public EnterpriseNumber {
        value = assertThat(value,"value").is(valid);
    }

    public static Validation<EnterpriseNumber> from(String value) {
        return Validation.from().catching(() -> new EnterpriseNumber(value));
    }
}
