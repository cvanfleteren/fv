package be.iffy.fv.dsl.impl;

import be.iffy.fv.*;
import be.iffy.fv.dsl.DSL;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static be.iffy.fv.dsl.DSL.assertThat;
import static be.iffy.fv.dsl.DSL.stringOps;
import static be.iffy.fv.dsl.DSL.strings;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssertDSLTest {

    @Nested
    class NullInput {

        @Test
        void nullInput_noNullOk_throws() {
            assertThatThrownBy(()  ->
                DSL.assertThat((String)null, "field")
                    .after(stringOps.trim())
                    .is(strings.minLength(4))
            ).isInstanceOf(ValidationException.class).hasMessage("field.must.not.be.null");
        }
    }

    @Nested
    class Is {

        @Test
        void is_whenRuleMatches_returnsValue() {
            String result = assertThat("ok", "field").is(Rule.notNull());
            assertThat(result).isEqualTo("ok");
        }

        @Test
        void is_whenRuleFails_throwsValidationException() {
            assertThatThrownBy(() -> assertThat((String) null, "field").is(Rule.notNull()))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException ve = (ValidationException) ex;
                        assertThat(ve.errors().head().message()).isEqualTo("field.must.not.be.null");
                    });
        }

        @Test
        void is_whenMappingRuleMatches_returnsMappedValue() {
            MappingRule<String, Integer> lengthRule = MappingRule.of((String s) -> Validation.valid(s.length()));
            Integer result = assertThat("abc", "field").is(lengthRule);
            assertThat(result).isEqualTo(3);
        }

        @Test
        void is_whenFunctionMatches_returnsMappedValue() {
            RuleLike<String, Validation<Integer>> functionRule = (String s) -> Validation.valid(s.length());
            Integer result = assertThat("abc", "field").is(functionRule);
            assertThat(result).isEqualTo(3);
        }
    }

    @Nested
    class IsNotNull {

        @Test
        void isNotNull_whenValueIsPresent_returnsValue() {
            String result = assertThat("ok", "field").isNotNull();
            assertThat(result).isEqualTo("ok");
        }

        @Test
        void isNotNull_whenValueIsNull_throwsValidationException() {
            assertThatThrownBy(() -> assertThat((String) null, "field").isNotNull())
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException ve = (ValidationException) ex;
                        assertThat(ve.errors().head().message()).isEqualTo("field.must.not.be.null");
                    });
        }
    }

    @Nested
    class Map {
        @Test
        void map_whenTransformationSucceeds_returnsMappedValue() {
            String result = assertThat(" ok ", "field").after(String::trim).isNotNull();
            assertThat(result).isEqualTo("ok");
        }

        @Test
        void map_whenTransformationThrows_throwsValidationException() {

            Transformation<String> t = s -> {throw new IllegalArgumentException(s);};

            assertThatThrownBy(() -> assertThat("boom", "field")
                    .after(t)
                    .isNotNull())
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class After {

        @Test
        void after_varargs_appliesAllTransformationsInOrder() {
            String result = assertThat("  hello  ", "field")
                    .after(String::trim, String::toUpperCase, s -> s + "!")
                    .isNotNull();
            assertThat(result).isEqualTo("HELLO!");
        }
    }
}
