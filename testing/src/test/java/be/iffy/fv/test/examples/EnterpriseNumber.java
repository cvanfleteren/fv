package be.iffy.fv.test.examples;

import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import be.iffy.fv.rules.text.StringOps;

import static be.iffy.fv.dsl.DSL.*;

public record EnterpriseNumber(String value) {

    static final Rule<String> valid = after(StringOps.alphanumeric()).is(strings.notBlank());

    public EnterpriseNumber {
        value = assertThat(value,"value").is(valid);
    }

    public static Validation<EnterpriseNumber> from(String value) {
        return Validation.fromCatching(() -> new EnterpriseNumber(value));
    }
}
