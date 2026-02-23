package net.vanfleteren.fv.rules;

import io.vavr.collection.Map;
import net.vanfleteren.fv.Rule;

import static net.vanfleteren.fv.API.validateThat;
import static net.vanfleteren.fv.assertj.ValidationAssert.assertThatValidation;

public class RulesTest {
    static <T> void validTest(T value, Rule<? super T> rule) {
        assertThatValidation(validateThat(value, "value").is(rule))
                .isValid()
                .hasValue(value);
    }

    static <T> void invalidTest(T value, Rule<? super T> rule, String... errorKeys) {
        assertThatValidation(validateThat(value, "value").is(rule))
                .isInvalid()
                .hasErrorKeys(errorKeys);
    }

    static <T> void invalidTest(T value, Rule<? super T> rule, String errorKey, Map<String, Object> args) {
        assertThatValidation(validateThat(value, "value").is(rule))
                .isInvalid()
                .hasErrorMessage(errorKey, args);
    }
}
