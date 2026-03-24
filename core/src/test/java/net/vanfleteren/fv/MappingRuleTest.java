package net.vanfleteren.fv;

import io.vavr.collection.HashMap;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.function.Predicate;

import static net.vanfleteren.fv.assertj.ValidationAssert.assertThatValidation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class MappingRuleTest {

    public static final MappingRule<String, Integer> mustBeInt = MappingRule.of(Integer::parseInt, "must.be.int");

    @Nested
    class AndThen {

        @Test
        void andThen_whenBothRulesAreValid_returnsValidResult() {
            MappingRule<String, Integer> lengthRule = s -> Validation.valid(s.length());
            MappingRule<Number, Object> toStringRule = n -> Validation.valid(n.toString());

            MappingRule<String, Object> rule = lengthRule.andThen(toStringRule);

            Validation<Object> result = rule.test("hello");

            assertThat(result.isValid()).isTrue();
            assertThat(result.getOrElse("")).isEqualTo("5");
        }


        @Test
        void andThen_whenFirstRuleIsInvalid_returnsFirstRuleErrors() {
            MappingRule<String, Integer> lengthRule = s -> Validation.invalid("length.invalid");
            MappingRule<Integer, Boolean> evenRule = i -> Validation.valid(i % 2 == 0);

            MappingRule<String, Boolean> lengthIsEvenRule = lengthRule.andThen(evenRule);

            Validation<Boolean> result = lengthIsEvenRule.test("hello");

            assertThat(result.isValid()).isFalse();
            assertThat(result.errors()).extracting(ErrorMessage::message).containsExactly("length.invalid");
        }

        @Test
        void andThenWithRule_whenFirstRuleIsInvalid_returnsFirstRuleErrors() {
            MappingRule<String, Integer> lengthRule = s -> Validation.invalid("length.invalid");
            Rule<Number> evenRule = Rule.of(i -> i.intValue() % 2 == 0, "not.event");

            MappingRule<String, Number> lengthIsEvenRule = lengthRule.andThen(evenRule);

            Validation<Number> result = lengthIsEvenRule.test("hello");

            assertThat(result.isValid()).isFalse();
            assertThat(result.errors()).extracting(ErrorMessage::message).containsExactly("length.invalid");
        }

        @Test
        void andThen_whenSecondRuleIsInvalid_returnsSecondRuleErrors() {
            MappingRule<String, Integer> lengthRule = s -> Validation.valid(s.length());
            MappingRule<Integer, Boolean> evenRule = i -> Validation.invalid("even.invalid");

            MappingRule<String, Boolean> lengthIsEvenRule = lengthRule.andThen(evenRule);

            Validation<Boolean> result = lengthIsEvenRule.test("hello");

            assertThat(result.isValid()).isFalse();
            assertThat(result.errors()).extracting(ErrorMessage::message).containsExactly("even.invalid");
        }

        @Test
        void andThen_whenBothRulesAreInvalid_returnsFirstRuleErrors() {
            MappingRule<String, Integer> lengthRule = s -> Validation.invalid("length.invalid");
            MappingRule<Integer, Boolean> evenRule = i -> Validation.invalid("even.invalid");

            MappingRule<String, Boolean> lengthIsEvenRule = lengthRule.andThen(evenRule);

            Validation<Boolean> result = lengthIsEvenRule.test("hello");

            assertThat(result.isValid()).isFalse();
            assertThat(result.errors()).extracting(ErrorMessage::message).containsExactly("length.invalid");
        }
    }

    @Nested
    class MapTo {

        @Test
        void mapTo_whenRulePasses_returnsConstantValue() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.of(Integer::parseInt, "not.a.number");
            MappingRule<String, String> mapToRule = rule.mapTo("Success");

            // Act
            Validation<String> result = mapToRule.test("123");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("Success");
        }

        @Test
        void mapTo_whenRuleFails_returnsInvalidWithOriginalErrors() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.of(Integer::parseInt, "not.a.number");
            MappingRule<String, String> mapToRule = rule.mapTo("Success");

            // Act
            Validation<String> result = mapToRule.test("abc");

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
            MappingRule<String, Integer> rule = MappingRule.of(Integer::parseInt, "must.be.int");
            MappingRule<List<String>, List<Integer>> listRule = rule.liftToList();

            // Act
            Validation<List<Integer>> result = listRule.test(List.of("123", "456"));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(List.of(123, 456));
        }

        @Test
        void liftToList_whenSomeElementsAreInvalid_accumulatesErrorsWithCorrectIndices() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.of(Integer::parseInt, "must.be.int");
            MappingRule<List<String>, List<Integer>> listRule = rule.liftToList();

            // Act
            Validation<List<Integer>> result = listRule.test(List.of("123", "hi", "yo"));

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
            MappingRule<List<String>, List<Integer>> listRule = rule.liftToList();

            // Act
            Validation<List<Integer>> result = listRule.test(List.of("hi", "hello"));

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
            MappingRule<String, Integer> rule = MappingRule.of(Integer::parseInt, "must.be.int");
            MappingRule<Option<String>, Option<Integer>> optionRule = rule.liftToOption();

            // Act
            Validation<Option<Integer>> result = optionRule.test(Option.none());

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(Option.none());
        }

        @Test
        void liftToOption_whenSomeAndValid_returnsValidResult() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.of(Integer::parseInt, "must.be.int");
            MappingRule<Option<String>, Option<Integer>> optionRule = rule.liftToOption();

            // Act
            Validation<Option<Integer>> result = optionRule.test(Option.of("123"));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(Option.of(123));
        }

        @Test
        void liftToOption_whenSomeAndInvalid_returnsInvalidWithSameErrors() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.of(Integer::parseInt, "must.be.int");
            MappingRule<Option<String>, Option<Integer>> optionRule = rule.liftToOption();

            // Act
            Validation<Option<Integer>> result = optionRule.test(Option.of("a"));

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
            MappingRule<String, Integer> rule = MappingRule.of(Integer::parseInt, "must.be.int");
            MappingRule<java.util.Optional<String>, java.util.Optional<Integer>> optionalRule = rule.liftToOptional();

            // Act
            Validation<java.util.Optional<Integer>> result = optionalRule.test(java.util.Optional.empty());

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(java.util.Optional.empty());
        }

        @Test
        void liftToOptional_whenNotEmptyAndValid_returnsValidResult() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.of(Integer::parseInt, "must.be.int");
            MappingRule<java.util.Optional<String>, java.util.Optional<Integer>> optionalRule = rule.liftToOptional();

            // Act
            Validation<java.util.Optional<Integer>> result = optionalRule.test(java.util.Optional.of("123"));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(java.util.Optional.of(123));
        }

        @Test
        void liftToOptional_whenNotEmptyAndInvalid_returnsInvalidWithSameErrors() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.of(Integer::parseInt, "must.be.int");
            MappingRule<java.util.Optional<String>, java.util.Optional<Integer>> optionalRule = rule.liftToOptional();

            // Act
            Validation<java.util.Optional<Integer>> result = optionalRule.test(java.util.Optional.of("a"));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("must.be.int");
        }
    }

    @Nested
    class LiftToMap {

        @Test
        void liftToMap_whenAllValuesAreValid_returnsValidResult() {
            // Arrange
            MappingRule<Map<String, String>, Map<String, Integer>> mapRule = mustBeInt.liftToMap();

            Map<String, String> input = LinkedHashMap.of(
                    "a", "123",
                    "b", "456"
            );

            // Act
            Validation<Map<String, Integer>> result = mapRule.test(input);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(LinkedHashMap.of("a", 123, "b", 456));
        }

        @Test
        void liftToMap_whenSomeValuesAreInvalid_addsKeyToPathAndAccumulatesErrors() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.of(Integer::parseInt, "must.be.int");
            MappingRule<Map<String, String>, Map<String, Integer>> mapRule = rule.liftToMap();

            Map<String, String> input = HashMap.of(
                    "a", "hi",
                    "b", "yo"
            );

            // Act
            Validation<Map<String, Integer>> result = mapRule.test(input).at("aMap");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("aMap[a].must.be.int", "aMap[b].must.be.int");
        }

        @Test
        void liftToMap_withKeyExtractor_whenSomeValuesAreInvalid_usesExtractedKeyInPath() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.of(Integer::parseInt, "must.be.int");
            MappingRule<Map<Integer, String>, Map<Integer, Integer>> mapRule = rule.liftToMap(k -> "k" + k);

            Map<Integer, String> input = HashMap.of(
                    10, "a",
                    20, "b"
            );

            // Act
            Validation<Map<Integer, Integer>> result = mapRule.test(input).at("aMap");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("aMap[k10].must.be.int", "aMap[k20].must.be.int");
        }

        @Test
        void liftToMap_withKeyExtractor_whenAllValuesAreValid_returnsValidResult() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.of(Integer::parseInt, "must.be.int");
            MappingRule<Map<Integer, String>, Map<Integer, Integer>> mapRule = rule.liftToMap(k -> "id-" + k);

            Map<Integer, String> input = LinkedHashMap.of(
                    1, "123",
                    2, "456"
            );

            // Act
            Validation<Map<Integer, Integer>> result = mapRule.test(input);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(HashMap.of(1, 123, 2, 456));
        }
    }

    @Nested
    class NotNull {
        @Test
        void notNull_whenValueIsPresent_returnsValidResult() {
            MappingRule<String, String> rule = MappingRule.notNull();
            assertThatValidation(rule.test("hello"))
                    .isValid()
                    .hasValue("hello");
        }

        @Test
        void notNull_whenValueIsNull_returnsInvalidWithDefaultError() {
            MappingRule<String, String> rule = MappingRule.notNull();
            assertThatValidation(rule.test(null))
                    .isInvalid()
                    .hasErrorMessages("must.not.be.null");
        }
    }

    @Nested
    class FactoryMethods {
        @Test
        void of_whenMapperIsNull_throwsNullPointerException() {
            assertThatCode(() -> MappingRule.of(null, "error"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("mapper cannot be null");
        }

        @Test
        void of_whenErrorMessageIsNull_throwsNullPointerException() {
            assertThatCode(() -> MappingRule.of(input -> input, (ErrorMessage) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("errorMessage cannot be null");
        }

        @Test
        void ofTry_whenTryIsSuccess_returnsValidResult() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.ofTry(s -> Try.of(() -> Integer.parseInt(s)), "not.a.number");

            // Act
            Validation<Integer> result = rule.test("123");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(123);
        }

        @Test
        void ofTry_whenTryIsFailure_returnsInvalidWithErrorMessage() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.ofTry(s -> Try.of(() -> Integer.parseInt(s)), "not.a.number");

            // Act
            Validation<Integer> result = rule.test("abc");

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
            Rule<Number> rule = Rule.of(s -> s.doubleValue() > 0, "must.be.positive");
            Predicate<BigDecimal> p = rule.toPredicate();

            // Act + Assert
            assertThat(p.test(BigDecimal.ONE)).isTrue();
        }

        @Test
        void toPredicate_whenRuleInvalid_returnsFalse() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Predicate<? super String> p = rule.toPredicate();

            // Act + Assert
            assertThat(p.test("hi")).isFalse();
        }

        @Test
        void toPredicate_delegatesToRuleTest_everyTime() {
            // Arrange
            final int[] calls = {0};
            Rule<Integer> countingRule = value -> {
                calls[0]++;
                return value > 0
                        ? Validation.valid(value)
                        : Validation.invalid("must.be.positive");
            };

            Predicate<? super Integer> p = countingRule.toPredicate();

            // Act
            p.test(1);
            p.test(-1);
            p.test(2);

            // Assert
            assertThat(calls[0]).isEqualTo(3);
        }
    }

    @Nested
    class RequiredOption {

        @Test
        void requiredOption_whenOptionIsNone_returnsInvalid() {
            // Arrange
            MappingRule<String, String> rule = MappingRule.of(s -> s, "error");
            MappingRule<Option<String>, String> requiredRule = MappingRule.requiredOption(rule);

            // Act + Assert
            assertThatValidation(requiredRule.test(Option.none()))
                    .isInvalid()
                    .hasErrorMessage("must.not.be.empty");
        }

        @Test
        void requiredOption_whenOptionIsSomeAndValid_returnsValidResult() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.of(Integer::parseInt, "not.a.number");
            MappingRule<Option<String>, Integer> requiredRule = MappingRule.requiredOption(rule);

            // Act + Assert
            assertThatValidation(requiredRule.test(Option.of("123")))
                    .isValid()
                    .hasValue(123);
        }

        @Test
        void requiredOption_whenOptionIsSomeAndInvalid_returnsInvalidWithRuleErrors() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.of(Integer::parseInt, "not.a.number");
            MappingRule<Option<String>, Integer> requiredRule = MappingRule.requiredOption(rule);

            // Act + Assert
            assertThatValidation(requiredRule.test(Option.of("abc")))
                    .isInvalid()
                    .hasErrorMessage("not.a.number");
        }
    }

    @Nested
    class RequiredOptional {

        @Test
        void requiredOptional_whenOptionalIsEmpty_returnsInvalid() {
            // Arrange
            MappingRule<String, String> rule = MappingRule.of(s -> s, "error");
            MappingRule<java.util.Optional<String>, String> requiredRule = MappingRule.requiredOptional(rule);

            // Act + Assert
            assertThatValidation(requiredRule.test(java.util.Optional.empty()))
                    .isInvalid()
                    .hasErrorMessage("must.not.be.empty");
        }

        @Test
        void requiredOptional_whenOptionalIsPresentAndValid_returnsValidResult() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.of(Integer::parseInt, "not.a.number");
            MappingRule<java.util.Optional<String>, Integer> requiredRule = MappingRule.requiredOptional(rule);

            // Act + Assert
            assertThatValidation(requiredRule.test(java.util.Optional.of("123")))
                    .isValid()
                    .hasValue(123);
        }

        @Test
        void requiredOptional_whenOptionalIsPresentAndInvalid_returnsInvalidWithRuleErrors() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.of(Integer::parseInt, "not.a.number");
            MappingRule<java.util.Optional<String>, Integer> requiredRule = MappingRule.requiredOptional(rule);

            // Act + Assert
            assertThatValidation(requiredRule.test(java.util.Optional.of("abc")))
                    .isInvalid()
                    .hasErrorMessage("not.a.number");
        }
    }

    @Nested
    class OrElse {


        MappingRule<String, Integer> rule1 = MappingRule.of(Integer::parseInt, "not.a.number");
        MappingRule<String, Integer> rule2 = s -> Validation.valid(s.length());
        MappingRule<String, Integer> orRule = rule1.orElse(rule2);

        @Test
        void orElse_whenFirstRuleIsSuccessful_returnsFirstRuleResult() {
            // Act
            Validation<Integer> result = orRule.test("123");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(123);
        }

        @Test
        void orElse_whenFirstRuleFailsAndSecondRuleIsSuccessful_returnsSecondRuleResult() {
            // Act
            Validation<Integer> result = orRule.test("abc");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(3);
        }

        @Test
        void orElse_whenBothRulesFail_returnsCombinedErrors() {
            // Arrange
            MappingRule<String, Integer> rule1 = MappingRule.of(Integer::parseInt, "not.a.number");
            MappingRule<String, Integer> rule2 = MappingRule.of(s -> { throw new RuntimeException(); }, "generic.error");
            MappingRule<String, Integer> orRule = rule1.orElse(rule2);

            // Act
            Validation<Integer> result = orRule.test("abc");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("not.a.number", "generic.error");
        }

        @Test
        void orElse_whenOtherIsNull_throwsNullPointerException() {
            MappingRule<String, Integer> rule = MappingRule.of(Integer::parseInt, "not.a.number");
            assertThatCode(() -> rule.orElse(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("other rule cannot be null");
        }
    }

    @Nested
    class Recover {

        @Test
        void recover_whenFirstRuleIsSuccessful_returnsFirstRuleResult() {
            // Arrange
            MappingRule<String, Integer> rule1 = MappingRule.of(Integer::parseInt, "not.a.number");
            MappingRule<String, Integer> rule2 = s -> Validation.valid(s.length());
            MappingRule<String, Integer> recoverRule = rule1.recover(rule2);

            // Act
            Validation<Integer> result = recoverRule.test("123");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(123);
        }

        @Test
        void recover_whenFirstRuleFailsAndSecondRuleIsSuccessful_returnsSecondRuleResult() {
            // Arrange
            MappingRule<String, Integer> rule1 = MappingRule.of(Integer::parseInt, "not.a.number");
            MappingRule<String, Integer> rule2 = s -> Validation.valid(s.length());
            MappingRule<String, Integer> recoverRule = rule1.recover(rule2);

            // Act
            Validation<Integer> result = recoverRule.test("abc");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(3);
        }

        @Test
        void recover_whenBothRulesFail_returnsSecondRuleErrors() {
            // Arrange
            MappingRule<String, Integer> rule1 = MappingRule.of(Integer::parseInt, "not.a.number");
            MappingRule<String, Integer> rule2 = MappingRule.of(s -> {
                throw new RuntimeException();
            }, "generic.error");
            MappingRule<String, Integer> recoverRule = rule1.recover(rule2);

            // Act
            Validation<Integer> result = recoverRule.test("abc");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("generic.error");
        }

        @Test
        void recover_whenOtherIsNull_throwsNullPointerException() {
            MappingRule<String, Integer> rule = MappingRule.of(Integer::parseInt, "not.a.number");
            assertThatCode(() -> rule.recover(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("other rule cannot be null");
        }
    }

    @Nested
    class With {

        record StringHolder(String value) {
        }

        @Test
        void with_whenRulePasses_returnsMappedValidResult() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.of(Integer::parseInt, "not.a.number");
            MappingRule<StringHolder, Number> withRule = MappingRule.with(StringHolder::value, rule);

            // Act
            Validation<Number> result = withRule.test(new StringHolder("1234"));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(1234);
        }

        @Test
        void with_whenRuleFails_returnsInvalidWithRuleErrors() {
            // Arrange
            MappingRule<String, Integer> rule = MappingRule.of(Integer::parseInt, "not.a.number");
            MappingRule<StringHolder, Integer> withRule = MappingRule.with(StringHolder::value, rule);

            // Act
            Validation<Integer> result = withRule.test(new StringHolder("abc"));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("not.a.number");
        }
    }
}