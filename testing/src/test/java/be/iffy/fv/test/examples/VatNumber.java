package be.iffy.fv.test.examples;

import be.iffy.fv.MappingRule;
import be.iffy.fv.rules.text.StringOps;

import static be.iffy.fv.dsl.DSL.*;

public record VatNumber(String value, Countrycode countrycode) {strings

    static final MappingRule<String, CountryCode> ccRule =
            strings.firstN(2).then(strings.alpha()).then(strings.asEnum(CountryCode.class));

    static final MappingRule<String, String> numberRule =
            strings.skipN(2).then(strings.alphaNumeric()).then(strings.minLength(2));

    static final MappingRule<String, VatNumber> valid =

    public VatNumber {

    }


}
