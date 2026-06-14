package be.iffy.fv.rules;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import be.iffy.fv.assertj.InvalidValidationAssert;
import io.vavr.collection.Map;

import java.util.function.Function;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;

public class RulesTest {
    public static <T> void validTest(T value, Rule<? super T> rule) {
        assertThatValidation(rule.apply(value))
                .isValid()
                .isEqualTo(value);
    }

    public static <T, R> void validTest(T value, R expected, MappingRule<? super T, R> rule) {
        assertThatValidation(rule.apply(value))
                .isValid()
                .isEqualTo(expected);
    }

    public static <T, R> InvalidValidationAssert<?, Validation.Invalid<R>, R> invalidTest(T value, Function<? super T, Validation<R>> rule, String... errorKeys) {
        return assertThatValidation(rule.apply(value))
                .isInvalid()
                .hasErrorKeys(errorKeys);
    }

    public static <T, R> void invalidTest(T value, Function<? super T, Validation<R>> rule, String errorMessage, Map<String, Object> args) {
        assertThatValidation(rule.apply(value))
                .isInvalid()
                .hasErrorMessage(errorMessage, args);
    }

}
