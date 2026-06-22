package be.iffy.fv.test.examples;

import be.iffy.fv.MappingRule;

import static be.iffy.fv.dsl.DSL.*;

public record KboNumber(String value) {

    static final MappingRule<String, String> validKbo = after(stringOps.keepDigits()).is(
            strings.length(10).and(strings.startsWith("0","1"))
    );

    public KboNumber {
        value = assertThat(value,"value").is(validKbo);
    }
}
