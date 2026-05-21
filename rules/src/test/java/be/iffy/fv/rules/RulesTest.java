package be.iffy.fv.rules;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import be.iffy.fv.assertj.InvalidValidationAssert;
import io.vavr.collection.Map;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;

public class RulesTest {
    public static <T> void validTest(T value, Rule<? super T> rule) {
        assertThatValidation(rule.test(value).at("value"))
                .isValid()
                .hasValue(value);
    }

    public static <T, R> void validTest(T value, R expected, MappingRule<? super T, R> rule) {
        assertThatValidation(rule.test(value).at("value"))
                .isValid()
                .hasValue(expected);
    }

    public static <T, R> InvalidValidationAssert<?, Validation.Invalid, R> invalidTest(T value, MappingRule<? super T, R> rule, String... errorKeys) {
        return assertThatValidation(rule.test(value).at("value"))
                .isInvalid()
                .hasErrorKeys(errorKeys);
    }

    public static <T, R> void invalidTest(T value, MappingRule<? super T, R> rule, String errorKey, Map<String, Object> args) {
        assertThatValidation(rule.test(value).at("value"))
                .isInvalid()
                .hasErrorMessage(errorKey, args);
    }

}
