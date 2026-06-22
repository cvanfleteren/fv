package be.iffy.fv.dsl.impl;

import be.iffy.fv.*;
import be.iffy.fv.Validation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static be.iffy.fv.dsl.DSL.validateThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationDSLTest {

    @Nested
    class NullInput {

        @Test
        void nullInput_whenNullInput_returnsInvalidWithNullError() {
            Validation<String> result = validateThat((String) null, "field")
                    .after(String::trim)
                    .isNotNull();

            assertThatValidation(result).isInvalid().hasErrorMessages("field.must.not.be.null");
        }
    }

    @Nested
    class Is {

        @Test
        void is_whenRuleMatches_returnsValidWithValue() {
            Validation<String> result = validateThat("ok", "field").is(Rule.notNull());

            assertThatValidation(result).isValid().isEqualTo("ok");
        }

        @Test
        void is_whenRuleFails_returnsInvalidWithError() {
            Validation<String> result = validateThat((String) null, "field").is(Rule.notNull());

            assertThatValidation(result).isInvalid().hasErrorMessages("field.must.not.be.null");
        }

        @Test
        void is_whenMappingRuleMatches_returnsValidWithMappedValue() {
            MappingRule<String, Integer> lengthRule = MappingRule.of((String s) -> Validation.valid(s.length()));

            Validation<Integer> result = validateThat("abc", "field").is(lengthRule);

            assertThatValidation(result).isValid().isEqualTo(3);
        }

        @Test
        void is_whenFunctionMatches_returnsValidWithMappedValue() {
            Function<String, Validation<Integer>> functionRule = s -> Validation.valid(s.length());

            Validation<Integer> result = validateThat("abc", "field").is(functionRule);

            assertThatValidation(result).isValid().isEqualTo(3);
        }

        @Test
        void is_whenFunctionReturnsInvalid_returnsInvalid() {
            ErrorMessage error = ErrorMessage.of("invalid.input");
            Function<String, Validation<Integer>> func = s -> Validation.invalid(error);

            Validation<Integer> result = validateThat("abc").is(func);

            assertThatValidation(result).isInvalid().hasErrorKeys("invalid.input");
        }

        @Test
        void is_whenNonNullValueFailsRule_returnsInvalidWithFieldPrefixedError() {
            Rule<String> minLength = Rule.of(s -> s.length() >= 3, "too.short");

            Validation<String> result = validateThat("ab", "field").is(minLength);

            assertThatValidation(result).isInvalid().hasErrorMessages("field.too.short");
        }
    }

    @Nested
    class IsNotNull {

        @Test
        void isNotNull_whenValueIsPresent_returnsValidWithValue() {
            Validation<String> result = validateThat("ok", "field").isNotNull();

            assertThatValidation(result).isValid().isEqualTo("ok");
        }

        @Test
        void isNotNull_whenValueIsNull_returnsInvalidWithNullError() {
            Validation<String> result = validateThat((String) null, "field").isNotNull();

            assertThatValidation(result).isInvalid().hasErrorMessages("field.must.not.be.null");
        }
    }

    @Nested
    class Map {

        @Test
        void after_whenTransformationSucceeds_returnsValidWithTransformedValue() {
            Validation<String> result = validateThat(" ok ", "field").after(String::trim).isNotNull();

            assertThatValidation(result).isValid().isEqualTo("ok");
        }

        @Test
        void after_whenTransformationThrows_propagatesException() {
            Transformation<String> t = s -> { throw new IllegalArgumentException(s); };

            assertThatThrownBy(() -> validateThat("boom", "field").after(t).isNotNull())
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class After {

        @Test
        void after_varargs_appliesAllTransformationsInOrder() {
            Validation<String> result = validateThat("  hello  ", "field")
                    .after(String::trim, String::toUpperCase, s -> s + "!")
                    .isNotNull();

            assertThatValidation(result).isValid().isEqualTo("HELLO!");
        }
    }
}
