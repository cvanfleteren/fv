package be.iffy.fv.test.examples;

import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import be.iffy.fv.rules.text.StringOps;

import java.util.regex.Pattern;

import static be.iffy.fv.dsl.DSL.*;
import static be.iffy.fv.rules.Rules.strings;

public record Bic(String value) {

    static Rule<String> followsBicPattern = Rule.of(
            input -> {
                Pattern pattern = Pattern.compile("^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$");
                return pattern.matcher(input).matches();
            },
            "invalid.format"
    );

    static Rule<String> validBic = after(StringOps.removeWhitespace()).is(
            Rule.any(
                    strings.exactLength(8),
                    strings.exactLength(11)
            ).describe("length.must.be.8.or.11").and(followsBicPattern)
    );

    public Bic {
        value = assertThat(value, Bic::value).is(validBic);
    }

    public static Validation<Bic> validate(String value) {
        return Validation.from(() -> new Bic(value));
    }

    public static Bic of(String value) {
        return new Bic(value);
    }
}