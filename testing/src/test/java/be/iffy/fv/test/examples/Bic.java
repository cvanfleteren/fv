package be.iffy.fv.test.examples;

import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import be.iffy.fv.Validations;

import java.util.regex.Pattern;

import static be.iffy.fv.dsl.DSL.*;

public record Bic(String value) {

    static Rule<String> followsBicPattern = Rule.of(
            input -> {
                Pattern pattern = Pattern.compile("^[A-Z]{6}[A-Z0-9]{2}([A-Z0-9]{3})?$");
                return pattern.matcher(input).matches();
            },
            "invalid.format"
    );

    static Rule<String> validBic = Rule.any(
                    strings.length(8),
                    strings.length(11)
            )
            .withErrorKey("length.must.be.8.or.11")
            .and(followsBicPattern);

    public Bic {
        value = assertThat(value, Bic::value).map(stringOps.removeWhitespace()).is(validBic);
    }

    public static Validation<Bic> from(String value) {
        return Validations.fromCatching(() -> new Bic(value));
    }
}