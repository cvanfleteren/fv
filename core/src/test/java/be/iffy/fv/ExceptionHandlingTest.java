package be.iffy.fv;

import io.vavr.collection.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExceptionHandlingTest {

    @Nested
    class ValidationExceptionHandling {

        @Test
        void map_whenMapperThrowsValidationException_propagatesException() {
            Validation<String> valid = Validation.valid("test");
            assertThatThrownBy(() -> valid.map(s -> {
                throw new IllegalArgumentException("boom");
            })).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void map_whenMapperThrowsValidationException_capturesErrors() {
            Validation<String> valid = Validation.valid("test");

            assertThatThrownBy(() -> valid.map(s -> {
                throw new ValidationException(List.of(ErrorMessage.of("inner.error")));
            })).isInstanceOf(ValidationException.class);
        }

        @Test
        void mapCatching_whenMapperThrowsValidationException_capturesErrors() {
            Validation<String> valid = Validation.valid("test");
            Validation<Integer> result = valid.mapCatching(s -> {
                throw new ValidationException(List.of(ErrorMessage.of("inner.error")));
            });
            assertThatValidation(result).isInvalid().hasErrorKeys("inner.error");
        }

        @Test
        void flatMap_whenMapperThrowsValidationException_propagatesException() {
            Validation<String> valid = Validation.valid("test");
            assertThatThrownBy(() -> valid.flatMap(s -> {
                throw new ValidationException(List.of(ErrorMessage.of("inner.error")));
            })).isInstanceOf(ValidationException.class);
        }

        @Test
        void flatMapCatching_whenMapperThrowsValidationException_capturesErrors() {
            Validation<String> valid = Validation.valid("test");
            Validation<Integer> result = valid.flatMapCatching(s -> {
                throw new ValidationException(List.of(ErrorMessage.of("inner.error")));
            });
            assertThatValidation(result).isInvalid().hasErrorKeys("inner.error");
        }

        @Test
        void mapN_whenMapperThrowsValidationException_propagatesException() {
            Validation<String> v1 = Validation.valid("a");
            Validation<String> v2 = Validation.valid("b");
            assertThatThrownBy(() -> Validation.mapN(v1, v2, (a, b) -> {
                throw new ValidationException(List.of(ErrorMessage.of("inner.error")));
            })).isInstanceOf(ValidationException.class);
        }

        @Test
        void flatMapN_whenMapperThrows_propagatesException() {
            Validation<String> v1 = Validation.valid("a");
            Validation<String> v2 = Validation.valid("b");
            assertThatThrownBy(() -> Validation.flatMapN(v1, v2, (a, b) -> {
                throw new RuntimeException("boom");
            })).isInstanceOf(RuntimeException.class).hasMessage("boom");
        }

        @Test
        void flatMapN_whenMapperThrowsValidationException_propagatesException() {
            Validation<String> v1 = Validation.valid("a");
            Validation<String> v2 = Validation.valid("b");
            assertThatThrownBy(() -> Validation.flatMapN(v1, v2, (a, b) -> {
                throw new ValidationException(List.of(ErrorMessage.of("inner.error")));
            })).isInstanceOf(ValidationException.class);
        }
    }

    @Nested
    class MappingRuleExceptionHandling {

        @Test
        void of_whenThrowingMapperThrowsValidationException_capturesErrors() {
            MappingRule<String, Integer> rule = MappingRule.of(s -> {
                throw new ValidationException(List.of(ErrorMessage.of("rule.error")));
            }, "fallback.error");

            Validation<Integer> result = rule.test("input");
            assertThatValidation(result).isInvalid().hasErrorKeys("rule.error");
        }

        @Test
        void ofTry_whenMapperThrowsValidationException_propagatesException() {
            // MappingRule.ofTry(mapper, error) itself calls mapper.apply(input) directly
            
            MappingRule<String, Integer> rule = MappingRule.ofTry(s -> {
                throw new ValidationException(List.of(ErrorMessage.of("rule.error")));
            }, "fallback.error");

            assertThatThrownBy(() -> rule.test("input")).isInstanceOf(ValidationException.class);
        }
    }

    @Nested
    class RuleCompositionExceptionHandling {
        @Test
        void or_whenFirstRuleThrows_propagatesException() {
            Rule<String> rule1 = s -> { throw new RuntimeException("boom"); };
            Rule<String> rule2 = Rule.ok();
            Rule<String> combined = rule1.or(rule2);

            assertThatThrownBy(() -> combined.test("test")).isInstanceOf(RuntimeException.class).hasMessage("boom");
        }

        @Test
        void andAlso_whenBothThrow_propagatesFirstException() {
            Rule<String> rule1 = s -> { throw new RuntimeException("boom1"); };
            Rule<String> rule2 = s -> { throw new RuntimeException("boom2"); };
            Rule<String> combined = rule1.andAlso(rule2);

            assertThatThrownBy(() -> combined.test("test")).isInstanceOf(RuntimeException.class).hasMessage("boom1");
        }
    }
}
