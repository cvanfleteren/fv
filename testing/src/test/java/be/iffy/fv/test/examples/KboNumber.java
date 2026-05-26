package be.iffy.fv.test.examples;

import be.iffy.fv.rules.text.StringRules;

import static be.iffy.fv.dsl.DSL.assertThat;
import static be.iffy.fv.rules.Rules.strings;

public record KboNumber(String value) {

    public KboNumber {
        value = assertThat(value,"value").is(strings.notBlank());
    }
}
