package be.iffy.fv.test.examples;

import be.iffy.fv.Rule;

import static be.iffy.fv.dsl.DSL.*;

public record KboNumber(String value) {

    static final Rule<String> validKbo = after(stringOps.digits()).is(Rule.all(
            strings.length(10),
            strings.startsWith("0","1")
    ));

    public KboNumber {
        value = assertThat(value,"value").is(validKbo);
    }
}