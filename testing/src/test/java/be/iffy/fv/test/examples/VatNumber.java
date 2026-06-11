package be.iffy.fv.test.examples;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Validation;

import static be.iffy.fv.MappingRules.combine;
import static be.iffy.fv.dsl.DSL.*;

public record VatNumber(String value, CountryCode countrycode) {

    public VatNumber {
        value = assertValid(
                validateThat(value,VatNumber::value).is(strings.notBlank().and(strings.alphaNumeric())),
                notNull(countrycode, VatNumber::countrycode)
        )._1;
    }

    /**
     * Parses a String in the form of BE123A456 to a VatNumber of countryCode BE and value 123A456
     */
    public static Validation<VatNumber> of(String vat) {
        MappingRule<String, CountryCode> countryRule = strings.take(2).then(strings.asEnum(CountryCode.class));
        MappingRule<String, String> numberRule = strings.drop(2).then(strings.minLength(2));

        MappingRule<String, VatNumber> valid = after(stringOps.keepAlphanumeric()).is(
                strings.minLength(4).then(
                        combine(numberRule, countryRule).into(VatNumber::new)
                )
        );

        return valid.test(vat);
    }
}
