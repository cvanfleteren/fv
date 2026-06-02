package be.iffy.fv.dsl;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import be.iffy.fv.ValidationException;
import be.iffy.fv.rules.text.StringOps;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static be.iffy.fv.dsl.DSL.assertThat;
import static be.iffy.fv.rules.Rules.strings;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssertDSLTest {

    @Nested
    class NullInput {
        @Test
        void nullInput_whenNullOk_isNull() {
            String result = assertThat((String)null, "field").map(StringOps.trim()).is(Rule.nullOk(strings.minLength(4)));
            assertThat(result).isNull();
        }

        @Test
        void nullInput_noNullOk_throws() {
            assertThatThrownBy(()  -> assertThat((String)null, "field").map(StringOps.trim()).is(strings.minLength(4)))
                    .isInstanceOf(ValidationException.class).hasMessage("field.must.not.be.null");
        }

        @Test
        void nullInput_throwsValidationException() {
            String result = assertThat(" 1234", "field").map(StringOps.trim()).is(Rule.nullOk(strings.minLength(4)));
            assertThat(result).isEqualTo("1234");
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
            Function<String, Validation<Integer>> functionRule = (String s) -> Validation.valid(s.length());
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
            String result = assertThat(" ok ", "field").map(String::trim).isNotNull();
            assertThat(result).isEqualTo("ok");
        }

        @Test
        void map_whenTransformationThrows_throwsValidationException() {
            assertThatThrownBy(() -> assertThat("boom", "field")
                    .map(s -> {
                        throw new RuntimeException(s);
                    })
                    .isNotNull())
                    .isInstanceOf(ValidationException.class);
        }
    }
}
