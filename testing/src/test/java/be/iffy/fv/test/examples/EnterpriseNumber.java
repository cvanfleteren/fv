package be.iffy.fv.test.examples;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Validation;

import static be.iffy.fv.dsl.DSL.*;
import static be.iffy.fv.rules.text.CharCategory.*;

public record EnterpriseNumber(String value) {

    static final MappingRule<String, String> valid = after(stringOps.keep(ASCII_LETTERS, ASCII_DIGITS)).is(strings.notBlank());

    public EnterpriseNumber {
        value = assertThat(value,"value").is(valid);
    }

    public static Validation<EnterpriseNumber> from(String value) {
        return Validation.from().catching(() -> new EnterpriseNumber(value));
    }
}
