package be.iffy.fv.test.examples;

import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import be.iffy.fv.rules.Rules;
import be.iffy.fv.rules.text.StringOps;

import static be.iffy.fv.dsl.DSL.after;
import static be.iffy.fv.dsl.DSL.assertThat;

public record EnterpriseNumber(String value) {

    static final Rule<String> valid = after(StringOps.alphanumeric()).is(Rules.strings.notBlank());

    public EnterpriseNumber {
        value = assertThat(value,"value").is(valid);
    }

    public static Validation<EnterpriseNumber> from(String value) {
        return Validation.from(() -> new EnterpriseNumber(value));
    }
}
