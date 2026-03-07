package net.vanfleteren.fv;

import com.google.testing.compile.JavaFileObjects;
import io.vavr.Function1;
import io.vavr.collection.HashMap;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import java.math.BigDecimal;
import java.util.function.Predicate;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static net.vanfleteren.fv.assertj.ValidationAssert.assertThatValidation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class RuleTest {

    @Nested
    class CompilationTests {

        @Test
        void compilation_whenAssigningIncompatibleRuleType_failsToCompile() {

            JavaFileObject source = JavaFileObjects.forSourceLines(
                    "HelloWorld",
                    "package test;",
                    "import net.vanfleteren.fv.Rule;",
                    "public class HelloWorld {",
                    "  Rule<String> rule = Rule.of(n -> true, \"msg\");",
                    "  Rule<Integer> invalid = rule; // This should fail",
                    "}"
            );

            assert_().about(javaSource())
                    .that(source)
                    .failsToCompile()
                    .withErrorContaining("incompatible types");
        }

        @Test
        void compilationOfOr_whenAssigningIncompatibleRuleType_failsToCompile() {

            JavaFileObject source = JavaFileObjects.forSourceLines(
                    "HelloWorld",
                    "package test;",
                    "import net.vanfleteren.fv.Rule;",
                    "public class HelloWorld {",
                    "  Rule<String> rule = Rule.of(n -> true, \"msg\");",
                    "  Rule<Integer> intRule = Rule.of(n -> true, \"msg\");",
                    "  Rule<Integer> invalid = rule.or(intRule); // This should fail",
                    "}"
            );

            assert_().about(javaSource())
                    .that(source)
                    .failsToCompile()
                    .withErrorContaining("incompatible upper bounds");
        }

    }

    @Nested
    class Narrow {
        @Test
        void narrow_whenCalled_allowsAssignmentToSubtype() {
            // Arrange
            Rule<? super BigDecimal> superRule = Rule.of(n -> n.doubleValue() > 0, "must.be.positive");

            // Act
            Rule<BigDecimal> narrowedRule = Rule.narrow(superRule);

            // Assert
            assertThat(narrowedRule).isSameAs(superRule);
            assertThatValidation(narrowedRule.test(BigDecimal.valueOf(10)))
                    .isValid();
        }

        @Test
        void test_assignmentToSuperType_compiles() {
            Rule<BigDecimal> isPositive = Rule.of(b -> b.doubleValue() > 0, "must.be.positive");
            // Option 1: Use a wildcard
            Validation<? extends Number> v1 = isPositive.test(BigDecimal.valueOf(500));

            // Option 2: Use narrow
            Validation<Number> v2 = Validation.narrow(isPositive.test(BigDecimal.valueOf(500)));
        }

    }

    @Nested
    class FactoryMethods {

        @Test
        void of_whenPredicateMatches_returnsValidValidation() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");

            // Act
            Validation<String> result = rule.test("hello");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("hello");
        }

        @Test
        void of_whenPredicateFails_returnsInvalidWithErrorMessage() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");

            // Act
            Validation<String> result = rule.test("hi");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("too.short");
        }

        @Test
        void of_whenPredicateIsNull_throwsNullPointerException() {
            // Act & Assert
            assertThatCode(() -> Rule.of(null, "msg"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("predicate cannot be null");
        }

        @Test
        void of_whenErrorMessageIsNull_throwsNullPointerException() {
            // Act & Assert
            assertThatCode(() -> Rule.of(s -> true, (ErrorMessage) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("errorMessage cannot be null");
        }
    }

    @Nested
    class Both {
        @Test
        void both_static_whenBothRulesFail_returnsInvalidWithBothErrors() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> combined = Rule.both(rule1, rule2);

            // Act
            Validation<String> result = combined.test("a");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("too.short", "must.start.with.h");
        }

        @Test
        void both_static_whenBothRulesPass_returnsValid() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> combined = Rule.both(rule1, rule2);

            // Act
            Validation<String> result = combined.test("hello");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("hello");
        }
    }


    @Nested
    class All {

        @Test
        void all_whenMultipleRulesFail_returnsInvalidWithAllErrors() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> rule3 = Rule.of(s -> s.contains("!"), "must.contain.exclamation");
            Rule<String> combined = Rule.all(rule1, rule2, rule3);

            // Act
            Validation<String> result = combined.test("a");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("too.short", "must.start.with.h", "must.contain.exclamation");
        }

        @Test
        void all_whenAllRulesPass_returnsValid() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> rule3 = Rule.of(s -> s.contains("!"), "must.contain.exclamation");
            Rule<String> combined = Rule.all(rule1, rule2, rule3);

            // Act
            Validation<String> result = combined.test("hello!");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("hello!");
        }
    }

    @Nested
    class AtLeastOneOf {

        @Test
        void atLeastOneOf_whenOneRulePasses_returnsValid() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> rule3 = Rule.of(s -> s.contains("!"), "must.contain.exclamation");
            Rule<String> combined = Rule.atLeastOneOf(rule1, rule2, rule3);

            // Act
            Validation<String> result = combined.test("hi!");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("hi!");
        }

        @Test
        void atLeastOneOf_whenMultipleRulesPass_returnsValid() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> combined = Rule.atLeastOneOf(rule1, rule2);

            // Act
            Validation<String> result = combined.test("hello");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("hello");
        }

        @Test
        void atLeastOneOf_whenAllRulesFail_returnsInvalidWithAllErrors() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> combined = Rule.atLeastOneOf(rule1, rule2);

            // Act
            Validation<String> result = combined.test("a");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("too.short", "must.start.with.h");
        }
    }

    @Nested
    class AndAlso {
        @Test
        void andAlso_whenBothRulesFail_returnsInvalidWithBothErrors() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> combined = rule1.andAlso(rule2);

            // Act
            Validation<String> result = combined.test("a");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("too.short", "must.start.with.h");
        }

        @Test
        void andAlso_whenBothRulesPass_returnsValid() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> combined = rule1.andAlso(rule2);

            // Act
            Validation<String> result = combined.test("hello");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("hello");
        }
    }

    @Nested
    class And {

        @Test
        void and_whenCombinedWithSuperRule_orderDoesntMatter() {
            Rule<Number> numberRule = Rule.of(o -> true, "msg");
            Rule<BigDecimal> decimalRule = Rule.of(o -> true, "msg");


            Rule<BigDecimal> and = decimalRule.and(numberRule);
            Rule<BigDecimal> and2 = numberRule.and(decimalRule);
        }

        @Test
        void and_whenBothRulesMatch_returnsValidValidation() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> combined = rule1.and(rule2);

            // Act
            Validation<String> result = combined.test("hello");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("hello");
        }

        @Test
        void and_whenFirstRuleFails_returnsInvalidWithFirstErrorMessage() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> combined = rule1.and(rule2);

            // Act
            Validation<String> result = combined.test("hi");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("too.short");
        }

        @Test
        void and_whenSecondRuleFails_returnsInvalidWithSecondErrorMessage() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> combined = rule1.and(rule2);

            // Act
            Validation<String> result = combined.test("apple");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("must.start.with.h");
        }

        @Test
        void and_whenOtherRuleIsNull_throwsNullPointerException() {
            // Arrange
            Rule<String> rule = Rule.of(s -> true, "msg");

            // Act & Assert
            assertThatCode(() -> rule.and(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("other rule cannot be null");
        }

        @Test
        void and_whenCombiningWithRuleOfSuperType_compilesAndWorks() {
            // Arrange
            Rule<Number> isPositive = Rule.of(n -> n.doubleValue() > 0, "must.be.positive");
            Rule<BigDecimal> isLessThan1000 = Rule.of(b -> b.compareTo(new BigDecimal("1000")) < 0, "must.be.less.than.1000");

            // Act
            Rule<BigDecimal> combined = isLessThan1000.and(isPositive);

            // Assert
            assertThatValidation(combined.test(new BigDecimal("500")))
                    .isValid();
            assertThatValidation(combined.test(new BigDecimal("-1")))
                    .isInvalid()
                    .hasErrorMessage("must.be.positive");
        }
    }

    @Nested
    class Or {

        @Test
        void or_whenCombinedWithSuperRule_orderDoesntMatter() {
            Rule<Number> numberRule = Rule.of(o -> true, "msg");
            Rule<BigDecimal> decimalRule = Rule.of(o -> true, "msg");

            Rule<BigDecimal> or = decimalRule.or(numberRule);
            Rule<BigDecimal> or2 = numberRule.or(decimalRule);

            assertThatValidation(or.test(BigDecimal.valueOf(10))).isValid();
            assertThatValidation(or2.test(BigDecimal.valueOf(10))).isValid();
        }

        @Test
        void or_whenFirstRuleMatches_returnsValidValidation() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> combined = rule1.or(rule2);

            // Act
            Validation<String> result = combined.test("apple"); // Fails rule2, matches rule1 (length > 3)

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("apple");
        }

        @Test
        void or_whenSecondRuleMatches_returnsValidValidation() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 5, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> combined = rule1.or(rule2);

            // Act
            Validation<String> result = combined.test("hi"); // Fails rule1 (length <= 5), matches rule2

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("hi");
        }

        @Test
        void or_whenBothRulesFail_returnsInvalidWithAccumulatedErrors() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 5, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> combined = rule1.or(rule2);

            // Act
            Validation<String> result = combined.test("abc");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("too.short", "must.start.with.h");
        }

        @Test
        void or_whenOtherRuleIsNull_throwsNullPointerException() {
            // Arrange
            Rule<String> rule = Rule.of(s -> true, "msg");

            // Act & Assert
            assertThatCode(() -> rule.or(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("other rule cannot be null");
        }

        @Test
        void or_whenCombiningWithRuleOfSuperType_compilesAndWorks() {
            // Arrange
            Rule<Number> isPositive = Rule.of(n -> n.doubleValue() > 0, "must.be.positive");
            Rule<BigDecimal> isMinusFortyTwo = Rule.of(b -> b.compareTo(new BigDecimal("-42")) == 0, "must.be.minus.forty.two");

            // Act
            Rule<BigDecimal> combined = isMinusFortyTwo.or(isPositive);

            // Assert
            assertThatValidation(combined.test(new BigDecimal("10"))).isValid();
            assertThatValidation(combined.test(new BigDecimal("-42"))).isValid();

            assertThatValidation(combined.test(new BigDecimal("-1")))
                    .isInvalid()
                    .hasErrorMessages("must.be.minus.forty.two", "must.be.positive");
        }
    }

    @Nested
    class Not {

        @Test
        void not_withErrorKey_whenOriginalRuleMatches_returnsInvalidWithNegatedErrorKey() {
            // Arrange
            Rule<String> startsWithH = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> notStartsWithH = startsWithH.not("must.not.start.with.h");

            // Act
            Validation<String> result = notStartsWithH.test("hello");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("must.not.start.with.h");
        }

        @Test
        void not_withErrorKey_whenOriginalRuleFails_returnsValidWithSameValue() {
            // Arrange
            Rule<String> startsWithH = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> notStartsWithH = startsWithH.not("must.not.start.with.h");

            // Act
            Validation<String> result = notStartsWithH.test("apple");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue("apple");
        }

        @Test
        void not_withErrorMessage_whenOriginalRuleMatches_returnsInvalidWithNegatedErrorMessageKey() {
            // Arrange
            Rule<Integer> isEven = Rule.of(i -> i % 2 == 0, "must.be.even");
            Rule<Integer> isNotEven = isEven.not(ErrorMessage.of("must.not.be.even"));

            // Act
            Validation<Integer> result = isNotEven.test(2);

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("must.not.be.even");
        }

        @Test
        void not_whenNegatedErrorKeyIsNull_throwsNullPointerException() {
            // Arrange
            Rule<String> rule = Rule.of(s -> true, "ok");

            // Act & Assert
            assertThatCode(() -> rule.not((String) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("negatedErrorKey cannot be null");
        }

        @Test
        void not_whenNegatedErrorMessageIsNull_throwsNullPointerException() {
            // Arrange
            Rule<String> rule = Rule.of(s -> true, "ok");

            // Act & Assert
            assertThatCode(() -> rule.not((ErrorMessage) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("negatedError cannot be null");
        }

        @Test
        void not_withErrorMapper_whenOriginalRuleMatches_canReturnErrorWithArgsThatArePreserved() {
            // Arrange
            Rule<Integer> isEven = Rule.of(i -> i % 2 == 0, "must.be.even");

            Map<String, Object> expectedArgs = HashMap.of("reason", "because.i.said.so", "n", 2);

            Rule<Integer> isNotEven = isEven.not(err ->
                    ErrorMessage.of("must.not.be.even", expectedArgs)
            );

            // Act
            Validation<Integer> result = isNotEven.test(2);

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("must.not.be.even", expectedArgs);
        }

        @Test
        void not_withErrorMapper_whenOriginalRuleMatches_canKeepArgsFromInputErrorMessage() {
            // Arrange
            Rule<String> startsWithH = Rule.of(s -> s.startsWith("h"), "must.start.with.h");

            Map<String, Object> expectedArgs = HashMap.of("original.key", "must.not.satisfy.rule");

            Rule<String> notStartsWithH = startsWithH.not(err ->
                    ErrorMessage.of("must.not.start.with.h", err.parameters().put("original.key", err.key()))
            );

            // Act
            Validation<String> result = notStartsWithH.test("hello");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("must.not.start.with.h", expectedArgs);
        }

        @Test
        void not_withErrorMapper_whenErrorMapperIsNull_throwsNullPointerException() {
            // Arrange
            Rule<String> rule = Rule.of(s -> true, "ok.value");

            // Act & Assert
            assertThatCode(() -> rule.not((Function1<ErrorMessage,ErrorMessage>)null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("errorMapper cannot be null");
        }
    }

    @Nested
    class LiftToList {

        @Test
        void liftToList_whenAllElementsAreValid_returnsValidList() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<List<String>> listRule = rule.liftToList();

            // Act
            Validation<List<String>> result = listRule.test(List.of("hello", "world"));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(List.of("hello", "world"));
        }

        @Test
        void liftToList_whenSomeElementsAreInvalid_accumulatesErrorsWithCorrectIndices() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<List<String>> listRule = rule.liftToList();

            // Act
            Validation<List<String>> result = listRule.test(List.of("hello", "hi", "yo"));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("[1].too.short", "[2].too.short");
        }

        @Test
        void liftToList_whenElementHasMultipleErrors_preservesAllErrorsWithSameIndex() {
            // Arrange
            Rule<String> rule = s -> s.length() > 3
                    ? Validation.valid(s)
                    : Validation.invalid(ErrorMessage.of("too.short"), ErrorMessage.of("must.be.longer"));
            Rule<List<String>> listRule = rule.liftToList();

            // Act
            Validation<List<String>> result = listRule.test(List.of("hi", "hello"));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("[0].too.short", "[0].must.be.longer");
        }
    }

    @Nested
    class LiftToOption {

        @Test
        void liftToOption_whenNone_returnsValidNone() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<Option<String>> optionRule = rule.liftToOption();

            // Act
            Validation<Option<String>> result = optionRule.test(Option.none());

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(Option.none());
        }

        @Test
        void liftToOption_whenSomeAndValid_returnsValidSome() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<Option<String>> optionRule = rule.liftToOption();

            // Act
            Validation<Option<String>> result = optionRule.test(Option.of("hello"));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(Option.of("hello"));
        }

        @Test
        void liftToOption_whenSomeAndInvalid_returnsInvalidWithSameErrors() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<Option<String>> optionRule = rule.liftToOption();

            // Act
            Validation<Option<String>> result = optionRule.test(Option.of("hi"));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("too.short");
        }
    }

    @Nested
    class LiftToMap {

        @Test
        void liftToMap_whenAllValuesAreValid_returnsValidMap() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<Map<String, String>> mapRule = rule.liftToMap();

            Map<String, String> input = LinkedHashMap.of(
                    "a", "hello",
                    "b", "world"
            );

            // Act
            Validation<Map<String, String>> result = mapRule.test(input);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(input);
        }

        @Test
        void liftToMap_whenSomeValuesAreInvalid_addsKeyToPathAndAccumulatesErrors() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<Map<String, String>> mapRule = rule.liftToMap();

            Map<String, String> input = HashMap.of(
                    "a", "hi",
                    "b", "yo"
            );

            // Act
            Validation<Map<String, String>> result = mapRule.test(input).at("aMap");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("aMap[a].too.short", "aMap[b].too.short");
        }

        @Test
        void liftToMap_withKeyExtractor_whenSomeValuesAreInvalid_usesExtractedKeyInPath() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");

            Rule<Map<Integer, String>> mapRule = rule.liftToMap(k -> "k" + k);

            Map<Integer, String> input = HashMap.of(
                    10, "hi",
                    20, "yo"
            );

            // Act
            Validation<Map<Integer, String>> result = mapRule.test(input).at("aMap");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("aMap[k10].too.short", "aMap[k20].too.short");
        }

        @Test
        void liftToMap_withKeyExtractor_whenAllValuesAreValid_returnsValidMap() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");

            Rule<Map<Integer, String>> mapRule = rule.liftToMap(k -> "id-" + k);

            Map<Integer, String> input = LinkedHashMap.of(
                    1, "hello",
                    2, "world"
            );

            // Act
            Validation<Map<Integer, String>> result = mapRule.test(input);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .hasValue(input);
        }

        @Test
        void liftToMap_withKeyExtractor_whenKeyExtractorIsNull_throwsNullPointerExceptionOnTest() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<Map<Integer, String>> mapRule = rule.liftToMap((Function1<Integer, Object>) null);

            Map<Integer, String> input = HashMap.of(1, "hi");

            // Act & Assert
            assertThatCode(() -> mapRule.test(input))
                    .isInstanceOf(NullPointerException.class);
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
}