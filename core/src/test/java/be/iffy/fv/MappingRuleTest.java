package be.iffy.fv;

import io.vavr.collection.HashMap;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class MappingRuleTest {

    public static final MappingRule<String, Integer> mustBeInt = MappingRule.catching(Integer::parseInt, "must.be.int");

    @Nested
    class Then {

        @Test
        void then_whenBothRulesAreValid_returnsValidResult() {
            MappingRule<String, Integer> lengthRule = s -> Validation.valid(s.length());
            MappingRule<Number, Object> toStringRule = n -> Validation.valid(n.toString());

            MappingRule<String, Object> rule = lengthRule.then(toStringRule);

            Validation<Object> result = rule.apply("hello");

            assertThat(result.isValid()).isTrue();
            assertThat(result.getOrElse("")).isEqualTo("5");
        }


        @Test
        void then_whenFirstRuleIsInvalid_returnsFirstRuleErrors() {
            MappingRule<String, Integer> lengthRule = s -> Validation.invalid("length.invalid");
            MappingRule<Integer, Boolean> evenRule = i -> Validation.valid(i % 2 == 0);

            MappingRule<String, Boolean> lengthIsEvenRule = lengthRule.then(evenRule);

            Validation<Boolean> result = lengthIsEvenRule.apply("hello");

            assertThat(result.isValid()).isFalse();
            assertThat(result.errors()).extracting(ErrorMessage::message).containsExactly("length.invalid");
        }

        @Test
        void andThenWithRule_whenFirstRuleIsInvalid_returnsFirstRuleErrors() {
            MappingRule<String, Integer> lengthRule = s -> Validation.invalid("length.invalid");
            Rule<Number> evenRule = Rule.of(i -> i.intValue() % 2 == 0, "not.event");

            MappingRule<String, Number> lengthIsEvenRule = lengthRule.then(evenRule);

            Validation<Number> result = lengthIsEvenRule.apply("hello");

            assertThat(result.isValid()).isFalse();
            assertThat(result.errors()).extracting(ErrorMessage::message).containsExactly("length.invalid");
        }

        @Test
        void then_whenSecondRuleIsInvalid_returnsSecondRuleErrors() {
            MappingRule<String, Integer> lengthRule = s -> Validation.valid(s.length());
            MappingRule<Integer, Boolean> evenRule = i -> Validation.invalid("even.invalid");

            MappingRule<String, Boolean> lengthIsEvenRule = lengthRule.then(evenRule);

            Validation<Boolean> result = lengthIsEvenRule.apply("hello");

            assertThat(result.isValid()).isFalse();
            assertThat(result.errors()).extracting(ErrorMessage::message).containsExactly("even.invalid");
        }

        @Test
        void then_whenOtherIsNull_throwsNullPointerException() {
            MappingRule<String, Integer> rule = s -> Validation.valid(s.length());
            assertThatCode(() -> rule.then(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("rule cannot be null");
        }

        @Test
        void then_whenBothRulesAreInvalid_returnsFirstRuleErrors() {
            MappingRule<String, Integer> lengthRule = s -> Validation.invalid("length.invalid");
            MappingRule<Integer, Boolean> evenRule = i -> Validation.invalid("even.invalid");

            MappingRule<String, Boolean> lengthIsEvenRule = lengthRule.then(evenRule);

            Validation<Boolean> result = lengthIsEvenRule.apply("hello");

            assertThat(result.isValid()).isFalse();
            assertThat(result.errors()).extracting(ErrorMessage::message).containsExactly("length.invalid");
        }
    }

    @Nested
    class MapTo {

        @Test
        void mapTo_whenRulePasses_returnsConstantValue() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "not.a.number");
            MappingRule<String, String> mapToRule = rule.mapTo("Success");

            // Act
            Validation<String> result = mapToRule.apply("123");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo("Success");
        }

        @Test
        void mapTo_whenRuleFails_returnsInvalidWithOriginalErrors() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "not.a.number");
            MappingRule<String, String> mapToRule = rule.mapTo("Success");

            // Act
            Validation<String> result = mapToRule.apply("abc");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("not.a.number");
        }
    }

    @Nested
    class LiftToList {

        @Test
        void liftToList_whenAllElementsAreValid_returnsValidResult() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "must.be.int");
            MappingRule<List<String>, List<Integer>> listRule = rule.lift().toVavrList();

            // Act
            Validation<List<Integer>> result = listRule.apply(List.of("123", "456"));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(List.of(123, 456));
        }

        @Test
        void liftToList_whenSomeElementsAreInvalid_accumulatesErrorsWithCorrectIndices() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "must.be.int");
            MappingRule<List<String>, List<Integer>> listRule = rule.lift().toVavrList();

            // Act
            Validation<List<Integer>> result = listRule.apply(List.of("123", "hi", "yo"));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("[1].must.be.int", "[2].must.be.int");
        }

        @Test
        void liftToList_whenElementHasMultipleErrors_preservesAllErrorsWithSameIndex() {
            // Arrange
            MappingRule<String, Integer> rule = s -> s.length() > 3
                    ? Validation.valid(s.length())
                    : Validation.invalid(ErrorMessage.of("too.short"), ErrorMessage.of("must.be.longer"));
            MappingRule<List<String>, List<Integer>> listRule = rule.lift().toVavrList();

            // Act
            Validation<List<Integer>> result = listRule.apply(List.of("hi", "hello"));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("[0].too.short", "[0].must.be.longer");
        }
    }

    @Nested
    class LiftToJList {

        @Test
        void liftToList_whenAllElementsAreValid_returnsValidResult() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "must.be.int");
            MappingRule<java.util.List<String>, java.util.List<Integer>> listRule = rule.lift().toList();

            // Act
            Validation<java.util.List<Integer>> result = listRule.apply(java.util.List.of("123", "456"));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(java.util.List.of(123, 456));
        }

        @Test
        void liftToList_whenSomeElementsAreInvalid_accumulatesErrorsWithCorrectIndices() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "must.be.int");
            MappingRule<java.util.List<String>, java.util.List<Integer>> listRule = rule.lift().toList();

            // Act
            Validation<java.util.List<Integer>> result = listRule.apply(java.util.List.of("123", "hi", "yo"));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("[1].must.be.int", "[2].must.be.int");
        }

        @Test
        void liftToList_whenElementHasMultipleErrors_preservesAllErrorsWithSameIndex() {
            // Arrange
            MappingRule<String, Integer> rule = s -> s.length() > 3
                    ? Validation.valid(s.length())
                    : Validation.invalid(ErrorMessage.of("too.short"), ErrorMessage.of("must.be.longer"));
            MappingRule<java.util.List<String>, java.util.List<Integer>> listRule = rule.lift().toList();

            // Act
            Validation<java.util.List<Integer>> result = listRule.apply(java.util.List.of("hi", "hello"));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("[0].too.short", "[0].must.be.longer");
        }
    }

    @Nested
    class LiftToOption {

        @Test
        void liftToOption_whenNone_returnsValidResult() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "must.be.int");
            MappingRule<Option<String>, Option<Integer>> optionRule = rule.lift().toOption();

            // Act
            Validation<Option<Integer>> result = optionRule.apply(Option.none());

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(Option.none());
        }

        @Test
        void liftToOption_whenSomeAndValid_returnsValidResult() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "must.be.int");
            MappingRule<Option<String>, Option<Integer>> optionRule = rule.lift().toOption();

            // Act
            Validation<Option<Integer>> result = optionRule.apply(Option.of("123"));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(Option.of(123));
        }

        @Test
        void liftToOption_whenSomeAndInvalid_returnsInvalidWithSameErrors() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "must.be.int");
            MappingRule<Option<String>, Option<Integer>> optionRule = rule.lift().toOption();

            // Act
            Validation<Option<Integer>> result = optionRule.apply(Option.of("a"));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("must.be.int");
        }
    }

    @Nested
    class LiftToOptional {

        @Test
        void liftToOptional_whenEmpty_returnsValidResult() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "must.be.int");
            MappingRule<java.util.Optional<String>, java.util.Optional<Integer>> optionalRule = rule.lift().toOptional();

            // Act
            Validation<java.util.Optional<Integer>> result = optionalRule.apply(java.util.Optional.empty());

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(java.util.Optional.empty());
        }

        @Test
        void liftToOptional_whenNotEmptyAndValid_returnsValidResult() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "must.be.int");
            MappingRule<java.util.Optional<String>, java.util.Optional<Integer>> optionalRule = rule.lift().toOptional();

            // Act
            Validation<java.util.Optional<Integer>> result = optionalRule.apply(java.util.Optional.of("123"));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(java.util.Optional.of(123));
        }

        @Test
        void liftToOptional_whenNotEmptyAndInvalid_returnsInvalidWithSameErrors() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "must.be.int");
            MappingRule<java.util.Optional<String>, java.util.Optional<Integer>> optionalRule = rule.lift().toOptional();

            // Act
            Validation<java.util.Optional<Integer>> result = optionalRule.apply(java.util.Optional.of("a"));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("must.be.int");
        }
    }

    @Nested
    class LiftToVavrMap {

        @Test
        void liftToVavrMap_whenAllValuesAreValid_returnsValidResult() {
            // Arrange
            MappingRule<Map<String, String>, Map<String, Integer>> mapRule = mustBeInt.lift().toVavrMap();

            Map<String, String> input = LinkedHashMap.of(
                    "a", "123",
                    "b", "456"
            );

            // Act
            Validation<Map<String, Integer>> result = mapRule.apply(input);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(LinkedHashMap.of("a", 123, "b", 456));
        }

        @Test
        void liftToVavrMap_whenSomeValuesAreInvalid_addsKeyToPathAndAccumulatesErrors() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "must.be.int");
            MappingRule<Map<String, String>, Map<String, Integer>> mapRule = rule.lift().toVavrMap();

            Map<String, String> input = HashMap.of(
                    "a", "hi",
                    "b", "yo"
            );

            // Act
            Validation<Map<String, Integer>> result = mapRule.apply(input).at("aMap");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("aMap[a].must.be.int", "aMap[b].must.be.int");
        }

        @Test
        void liftToVavrMap_withKeyExtractor_whenSomeValuesAreInvalid_usesExtractedKeyInPath() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "must.be.int");
            MappingRule<Map<Integer, String>, Map<Integer, Integer>> mapRule = rule.lift().toVavrMap(k -> "k" + k);

            Map<Integer, String> input = HashMap.of(
                    10, "a",
                    20, "b"
            );

            // Act
            Validation<Map<Integer, Integer>> result = mapRule.apply(input).at("aMap");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("aMap[k10].must.be.int", "aMap[k20].must.be.int");
        }

        @Test
        void liftToVavrMap_withKeyExtractor_whenAllValuesAreValid_returnsValidResult() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "must.be.int");
            MappingRule<Map<Integer, String>, Map<Integer, Integer>> mapRule = rule.lift().toVavrMap(k -> "id-" + k);

            Map<Integer, String> input = LinkedHashMap.of(
                    1, "123",
                    2, "456"
            );

            // Act
            Validation<Map<Integer, Integer>> result = mapRule.apply(input);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(HashMap.of(1, 123, 2, 456));
        }
    }

    @Nested
    class LiftToJMap {

        @Test
        void liftToJMap_whenAllValuesAreValid_returnsValidResult() {
            // Arrange
            MappingRule<java.util.Map<String, String>, java.util.Map<String, Integer>> mapRule = mustBeInt.lift().toMap();

            java.util.Map<String, String> input = java.util.Map.of(
                    "a", "123",
                    "b", "456"
            );

            // Act
            Validation<java.util.Map<String, Integer>> result = mapRule.apply(input);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(java.util.Map.of("a", 123, "b", 456));
        }

        @Test
        void liftToJMap_whenSomeValuesAreInvalid_addsKeyToPathAndAccumulatesErrors() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "must.be.int");
            MappingRule<java.util.Map<String, String>, java.util.Map<String, Integer>> mapRule = rule.lift().toMap();

            java.util.Map<String, String> input = java.util.Map.of(
                    "a", "hi",
                    "b", "yo"
            );

            // Act
            Validation<java.util.Map<String, Integer>> result = mapRule.apply(input).at("aMap");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("aMap[a].must.be.int", "aMap[b].must.be.int");
        }

        @Test
        void liftToJMap_withKeyExtractor_whenSomeValuesAreInvalid_usesExtractedKeyInPath() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "must.be.int");
            MappingRule<java.util.Map<Integer, String>, java.util.Map<Integer, Integer>> mapRule = rule.lift().toMap(k -> "k" + k);

            java.util.Map<Integer, String> input = java.util.Map.of(
                    10, "a",
                    20, "b"
            );

            // Act
            Validation<java.util.Map<Integer, Integer>> result = mapRule.apply(input).at("aMap");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("aMap[k10].must.be.int", "aMap[k20].must.be.int");
        }

        @Test
        void liftToJMap_withKeyExtractor_whenAllValuesAreValid_returnsValidResult() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "must.be.int");
            MappingRule<java.util.Map<Integer, String>, java.util.Map<Integer, Integer>> mapRule = rule.lift().toMap(k -> "id-" + k);

            java.util.Map<Integer, String> input = java.util.Map.of(
                    1, "123",
                    2, "456"
            );

            // Act
            Validation<java.util.Map<Integer, Integer>> result = mapRule.apply(input);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(java.util.Map.of(1, 123, 2, 456));
        }
    }

    @Nested
    class NotNull {
        @Test
        void notNull_whenValueIsPresent_returnsValidResult() {
            MappingRule<String, String> rule = MappingRule.notNull();
            assertThatValidation(rule.apply("hello"))
                    .isValid()
                    .isEqualTo("hello");
        }

        @Test
        void notNull_whenValueIsNull_returnsInvalidWithDefaultError() {
            MappingRule<String, String> rule = MappingRule.notNull();
            assertThatValidation(rule.apply(null))
                    .isInvalid()
                    .hasErrorMessages("must.not.be.null");
        }
    }

    @Nested
    class FactoryMethods {
        @Test
        void ofTry_withErrorMessage_whenTryIsSuccess_returnsValidResult() {
            MappingRule<String, Integer> rule = MappingRule.fromTry(s -> Try.success(Integer.parseInt(s)), ErrorMessage.of("error"));
            assertThat(rule.apply("123")).isEqualTo(Validation.valid(123));
        }
        @Test
        void ofTry_withErrorMessage_whenTryIsFailure_returnsInvalidWithErrorMessage() {
            MappingRule<String, Integer> rule = MappingRule.fromTry(s -> Try.failure(new NumberFormatException()), ErrorMessage.of("invalid.number"));
            assertThat(rule.apply("abc")).isEqualTo(Validation.invalid(ErrorMessage.of("invalid.number")));
        }
        @Test
        void ofTry_whenTryIsFailureWithValidationException_returnsInvalidWithValidationExceptionErrors() {
            List<ErrorMessage> errors = List.of(ErrorMessage.of("error.1"), ErrorMessage.of("error.2"));
            MappingRule<String, Integer> rule = MappingRule.fromTry(s -> Try.failure(new ValidationException(errors)), "fallback.error");
            assertThatValidation(rule.apply("abc"))
                    .isInvalid()
                    .hasErrorMessages("error.1", "error.2");
        }
        @Test
        void ofTry_withErrorMessage_whenTryProviderIsNull_throwsNullPointerException() {
            assertThatCode(() -> MappingRule.fromTry(null, ErrorMessage.of("error")))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("tryProvider cannot be null");
        }
        @Test
        void ofTry_withErrorMessage_whenErrorMessageIsNull_throwsNullPointerException() {
            assertThatCode(() -> MappingRule.fromTry(s -> Try.success(1), (ErrorMessage) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("errorMessage cannot be null");
        }

        @Test
        void fromTry_withErrorMessageMaker_whenTryIsFailure_usesMakerToCreateErrorMessage() {
            MappingRule<String, Integer> rule = MappingRule.fromTry(
                    s -> Try.failure(new RuntimeException("boom")),
                    (input, throwable) -> ErrorMessage.of("error.with.input", "input", input).prepend(ErrorMessage.Path.of(throwable.getMessage()))
            );

            Validation<Integer> result = rule.apply("test-input");

            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("boom.error.with.input", HashMap.of("input", "test-input"));
        }

        @Test
        @SuppressWarnings("unchecked")
        void fromTry_withErrorMessageMaker_whenErrorMessageMakerIsNull_throwsNullPointerException() {
            assertThatCode(() -> MappingRule.fromTry(s -> Try.success(1), (BiFunction) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("errorMessageMaker cannot be null");
        }
        @Test
        void of_whenMapperIsNull_throwsNullPointerException() {
            assertThatCode(() -> MappingRule.catching(null, "error"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("mapper cannot be null");
        }

        @Test
        void of_whenErrorMessageIsNull_throwsNullPointerException() {
            assertThatCode(() -> MappingRule.catching(input -> input, (ErrorMessage) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("errorMessage cannot be null");
        }

        @Test
        void fromTry_whenTryIsSuccess_returnsValidResult() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.fromTry(s -> Try.of(() -> Integer.parseInt(s)), "not.a.number");

            // Act
            Validation<Integer> result = rule.apply("123");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(123);
        }

        @Test
        void fromTry_whenTryIsFailure_returnsInvalidWithErrorMessage() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.fromTry(s -> Try.of(() -> Integer.parseInt(s)), "not.a.number");

            // Act
            Validation<Integer> result = rule.apply("abc");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("not.a.number");
        }
    }

    @Nested
    class ToPredicate {

        @Test
        void toPredicate_whenRuleValid_returnsTrue() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "must.be.int");
            Predicate<String> p = rule.toPredicate();

            // Act + Assert
            assertThat(p.test("1")).isTrue();
        }

        @Test
        void toPredicate_whenRuleInvalid_returnsFalse() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "must.be.int");
            Predicate<String> p = rule.toPredicate();

            // Act + Assert
            assertThat(p.test("hi")).isFalse();
        }

        @Test
        void toPredicate_delegatesToRuleTest_everyTime() {
            // Arrange
            final int[] calls = {0};
            MappingRule<String, Integer> countingRule = value -> {
                calls[0]++;
                return Integer.parseInt(value) > 0
                        ? Validation.valid(Integer.parseInt(value))
                        : Validation.invalid("must.be.positive");
            };

            Predicate<String> p = countingRule.toPredicate();

            // Act
            p.test("1");
            p.test("-1");
            p.test("2");

            // Assert
            assertThat(calls[0]).isEqualTo(3);
        }
    }

    @Nested
    class Or {

        MappingRule<String, Integer> rule1 = MappingRule.catching(Integer::parseInt, "not.a.number");
        MappingRule<String, Integer> rule2 = s -> Validation.valid(s.length());
        MappingRule<String, Integer> orRule = rule1.or(rule2);

        @Test
        void or_whenFirstRuleIsSuccessful_returnsFirstRuleResult() {
            // Act
            Validation<Integer> result = orRule.apply("123");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(123);
        }

        @Test
        void or_whenFirstRuleFailsAndSecondRuleIsSuccessful_returnsSecondRuleResult() {
            // Act
            Validation<Integer> result = orRule.apply("abc");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(3);
        }

        @Test
        void or_whenBothRulesFail_returnsCombinedErrors() {
            // Arrange
            MappingRule<String, Integer> rule1 = MappingRule.catching(Integer::parseInt, "not.a.number");
            MappingRule<String, Integer> rule2 = MappingRule.catching(s -> { throw new RuntimeException(); }, "generic.error");
            MappingRule<String, Integer> orRule = rule1.or(rule2);

            // Act
            Validation<Integer> result = orRule.apply("abc");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("not.a.number", "generic.error");
        }

        @Test
        void or_whenFirstRuleIsSuccessful_doesNotEvaluateSecondRule() {
            // Arrange
            AtomicBoolean secondRuleCalled = new AtomicBoolean(false);
            MappingRule<String, Integer> firstRule = MappingRule.catching(Integer::parseInt, "not.a.number");
            MappingRule<String, Integer> secondRule = s -> {
                secondRuleCalled.set(true);
                return Validation.valid(s.length());
            };

            // Act
            firstRule.or(secondRule).apply("123");

            // Assert
            assertThat(secondRuleCalled.get()).isFalse();
        }

        @Test
        void or_whenOtherIsNull_throwsNullPointerException() {
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "not.a.number");
            assertThatCode(() -> rule.or(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("other rule cannot be null");
        }
    }

    @Nested
    class Fallback {

        @Test
        void fallback_whenFirstRuleIsSuccessful_returnsFirstRuleResult() {
            // Arrange
            MappingRule<String, Integer> rule1 = MappingRule.catching(Integer::parseInt, "not.a.number");
            MappingRule<String, Integer> rule2 = s -> Validation.valid(s.length());
            MappingRule<String, Integer> recoverRule = rule1.fallback(rule2);

            // Act
            Validation<Integer> result = recoverRule.apply("123");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(123);
        }

        @Test
        void fallback_whenFirstRuleFailsAndSecondRuleIsSuccessful_returnsSecondRuleResult() {
            // Arrange
            MappingRule<String, Integer> rule1 = MappingRule.catching(Integer::parseInt, "not.a.number");
            MappingRule<String, Integer> rule2 = s -> Validation.valid(s.length());
            MappingRule<String, Integer> recoverRule = rule1.fallback(rule2);

            // Act
            Validation<Integer> result = recoverRule.apply("abc");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(3);
        }

        @Test
        void fallback_whenBothRulesFail_returnsSecondRuleErrors() {
            // Arrange
            MappingRule<String, Integer> rule1 = MappingRule.catching(Integer::parseInt, "not.a.number");
            MappingRule<String, Integer> rule2 = MappingRule.catching(s -> {
                throw new RuntimeException();
            }, "generic.error");
            MappingRule<String, Integer> recoverRule = rule1.fallback(rule2);

            // Act
            Validation<Integer> result = recoverRule.apply("abc");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("generic.error");
        }

        @Test
        void fallback_whenOtherIsNull_throwsNullPointerException() {
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "not.a.number");
            assertThatCode(() -> rule.fallback(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("fallback rule cannot be null");
        }
    }

    @Nested
    class On {

        record StringHolder(String value) {
        }

        record NumberHolder(String value) {
        }

        @Test
        void on_whenRulePasses_variance() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "not.a.number");
            MappingRule<NumberHolder, Number> withRule = MappingRule.on(NumberHolder::value, rule);

            // Act
            Validation<Number> result = withRule.apply(new NumberHolder("1234"));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(1234);
        }

        @Test
        void on_whenRulePasses_returnsMappedValidResult() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "not.a.number");
            MappingRule<StringHolder, Number> withRule = MappingRule.on(StringHolder::value, rule);

            // Act
            Validation<Number> result = withRule.apply(new StringHolder("1234"));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(1234);
        }

        @Test
        void on_instanceMethod_whenRulePasses_returnsMappedValidResult() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "not.a.number");
            MappingRule<StringHolder, Integer> withRule = rule.on(StringHolder::value);

            // Act
            Validation<Integer> result = withRule.apply(new StringHolder("1234"));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(1234);
        }

        @Test
        void on_instanceMethod_whenRuleFails_returnsInvalidWithRuleErrors() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "not.a.number");
            MappingRule<StringHolder, Integer> withRule = rule.on(StringHolder::value);

            // Act
            Validation<Integer> result = withRule.apply(new StringHolder("abc"));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("value.not.a.number");
        }

        @Test
        void on_whenRuleFails_returnsInvalidWithRuleErrors() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "not.a.number");
            MappingRule<StringHolder, Integer> withRule = MappingRule.on(StringHolder::value, rule);

            // Act
            Validation<Integer> result = withRule.apply(new StringHolder("abc"));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("value.not.a.number");
        }
    }

    @Nested
    class WithErrorKey {

        @Test
        void withErrorKey_whenRuleIsInvalid_replacesErrorsWithNewErrorKey() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "not.a.number");
            MappingRule<String, Integer> describedRule = rule.withErrorKey("invalid.input");

            // Act
            Validation<Integer> result = describedRule.apply("abc");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorKeys("invalid.input");

            assertThat(result.errors().map(ErrorMessage::key))
                    .doesNotContain("not.a.number");
        }

        @Test
        void withErrorKey_whenRuleIsValid_preservesValidResult() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.catching(Integer::parseInt, "not.a.number");
            MappingRule<String, Integer> describedRule = rule.withErrorKey("invalid.input");

            // Act
            Validation<Integer> result = describedRule.apply("123");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(123);
        }
    }

    @Nested
    class AsMappingRule {

        static Validation<Integer> validator(String input) {
            return  Validation.from()._try(Try.of(() -> Integer.parseInt(input)));
        }

        @Test
        void of_whenFunctionReturnsValid_returnsValidResult() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.of(AsMappingRule::validator);

            // Act
            Validation<Integer> result = rule.apply("123");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(123);
        }

        @Test
        void of_whenFunctionReturnsInvalid_returnsInvalidResult() {
            // Arrange
            ErrorMessage error = ErrorMessage.of("invalid.input");
            Function<String, Validation<Integer>> func = s -> Validation.invalid(error);
            MappingRule<String, Integer> rule = MappingRule.of(func);

            // Act
            Validation<Integer> result = rule.apply("abc");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorKeys("invalid.input");
        }
    }
}
