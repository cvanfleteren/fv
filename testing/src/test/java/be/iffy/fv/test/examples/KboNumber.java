package be.iffy.fv.test.examples;

import be.iffy.fv.Rule;
import be.iffy.fv.rules.text.StringOps;

import static be.iffy.fv.dsl.DSL.after;
import static be.iffy.fv.dsl.DSL.assertThat;
import static be.iffy.fv.rules.Rules.strings;

public record KboNumber(String value) {

    static final Rule<String> validKbo = after(StringOps.digits()).is(Rule.all(
            strings.length(10),
            strings.startsWith("0","1")
    ));

    public KboNumber {
        value = assertThat(value,"value").is(validKbo);
    }
}