package net.vanfleteren.fv.rules;

import io.vavr.collection.Map;
import net.vanfleteren.fv.MappingRule;
import net.vanfleteren.fv.Rule;

import static net.vanfleteren.fv.API.validateThat;
import static net.vanfleteren.fv.assertj.ValidationAssert.assertThatValidation;

public class RulesTest {
    public static <T> void validTest(T value, Rule<? super T> rule) {
        assertThatValidation(validateThat(value, "value").is(rule))
                .isValid()
                .hasValue(value);
    }

    public static <T, R> void validTest(T value, R expected, MappingRule<? super T, R> rule) {
        assertThatValidation(validateThat(value, "value").is(rule))
                .isValid()
                .hasValue(expected);
    }

    public static <T, R> void invalidTest(T value, MappingRule<? super T, R> rule, String... errorKeys) {
        assertThatValidation(validateThat(value, "value").is(rule))
                .isInvalid()
                .hasErrorKeys(errorKeys);
    }

    public static <T, R> void invalidTest(T value, MappingRule<? super T, R> rule, String errorKey, Map<String, Object> args) {
        assertThatValidation(validateThat(value, "value").is(rule))
                .isInvalid()
                .hasErrorMessage(errorKey, args);
    }

}
