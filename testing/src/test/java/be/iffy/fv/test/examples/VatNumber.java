package be.iffy.fv.test.examples;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Validation;

import static be.iffy.fv.MappingRule.combine;
import static be.iffy.fv.dsl.DSL.*;

public record VatNumber(String value, CountryCode countrycode) {

    static final MappingRule<String, CountryCode> countryRule =
            strings.take(2).then(strings.asEnum(CountryCode.class));

    static final MappingRule<String, String> numberRule =
            strings.drop(2).then(strings.alphaNumeric()).then(strings.minLength(2));

    static final MappingRule<String, VatNumber> valid = after(stringOps.alphanumeric()).is(
            combine(numberRule, countryRule).map(VatNumber::new)
    );

    public VatNumber {
        value = assertAllValid(
                validateThat(value,VatNumber::value).is(strings.notBlank().and(strings.alphaNumeric())),
                notNull(countrycode, VatNumber::countrycode)
        )._1;
    }

    public static Validation<VatNumber> of(String vat) {
        return valid.test(vat);
    }
}
