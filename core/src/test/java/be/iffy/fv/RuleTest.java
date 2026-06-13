package be.iffy.fv;

import com.google.testing.compile.JavaFileObjects;
import io.vavr.collection.HashMap;
import io.vavr.collection.LinkedHashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
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
                    "import be.iffy.fv.Rule;",
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
                    "import be.iffy.fv.Rule;",
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
            Rule<BigDecimal> narrowedRule = superRule.narrow();

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
        void of_whenPredicateMatches_returnsValidResult() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");

            // Act
            Validation<String> result = rule.test("hello");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo("hello");
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

        @Test
        void of_whenTestedWithNull_isInvalidAndDoesNotCallPredicate() {
            // Arrange
            Rule<String> rule = Rule.of(s -> {
                throw new RuntimeException("Should not be called");
            }, "some.error");

            // Act
            Validation<String> result = rule.test(null);

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("must.not.be.null");
        }

        @Test
        void ok_whenCalled_returnsValidResult() {
            // Arrange
            Rule<String> rule = Rule.ok();

            // Act
            Validation<String> result = rule.test("any");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo("any");
        }

        @Test
        void ok_whenCalledWithNull_throwsNullPointerException() {
            // Arrange
            Rule<String> rule = Rule.ok();

            // Act & Assert
            assertThatValidation(rule.test(null)).isInvalid()
                    .hasErrorKeys("must.not.be.null");
        }
    }

    @Nested
    class Both {
        @Test
        void both_static_whenBothRulesFail_returnsInvalidWithBothErrors() {
            // Arrange
            Rule<CharSequence> rule1 = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> combined = Rule.both(rule1.narrow(), rule2);

            // Act
            Validation<String> result = combined.test("a");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("too.short", "must.start.with.h");
        }

        @Test
        void both_static_whenBothRulesPass_returnsValidResult() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> combined = Rule.both(rule1, rule2);

            // Act
            Validation<String> result = combined.test("hello");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo("hello");
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
            Rule<Object> rule4 = Rule.of(o -> false, "must.always.fail");
            Rule<String> combined = Rule.all(rule1, rule2, rule3, rule4.narrow());

            // Act
            Validation<String> result = combined.test("a");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("too.short", "must.start.with.h", "must.contain.exclamation");
        }

        @Test
        void all_whenAllRulesPass_returnsValidResult() {
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
                    .isEqualTo("hello!");
        }

        @Test
        void all_whenMultipleRulesFailWithSameError_returnsUniqueErrors() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> false, "error.message");
            Rule<String> rule2 = Rule.of(s -> false, "error.message");
            Rule<String> combined = Rule.all(rule1, rule2);

            // Act
            Validation<String> result = combined.test("any");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("error.message");
            assertThat(result.errors()).hasSize(1);
        }
    }

    @Nested
    class Any {

        @Test
        void any_whenOneRulePasses_returnsValidResult() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> rule3 = Rule.of(s -> s.contains("!"), "must.contain.exclamation");
            Rule<String> combined = Rule.any(rule1, rule2, rule3);

            // Act
            Validation<String> result = combined.test("hi!");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo("hi!");
        }

        @Test
        void any_whenMultipleRulesPass_returnsValidResult() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> combined = Rule.any(rule1, rule2);

            // Act
            Validation<String> result = combined.test("hello");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo("hello");
        }

        @Test
        void any_whenAllRulesFail_returnsInvalidWithAllErrors() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> combined = Rule.any(rule1, rule2);

            // Act
            Validation<String> result = combined.test("a");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("too.short", "must.start.with.h");
        }

        @Test
        void any_whenFirstRulePasses_doesNotEvaluateSecondRule() {
            // Arrange
            AtomicBoolean secondRuleCalled = new AtomicBoolean(false);
            Rule<String> firstRule = Rule.of(s -> true, "error.one");
            Rule<String> secondRule = Rule.of(s -> {
                secondRuleCalled.set(true);
                return false;
            }, "error.two");

            // Act
            Rule.any(firstRule, secondRule).test("test");

            // Assert
            assertThat(secondRuleCalled.get()).isFalse();
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
        void andAlso_whenBothRulesPass_returnsValidResult() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> combined = rule1.andAlso(rule2);

            // Act
            Validation<String> result = combined.test("hello");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo("hello");
        }
    }

    @Nested
    class And {

        @Test
        void and_withMisbehavingRule_returnsValueOfFirstRule() {
            // 1. A well-behaved Rule<String>
            Rule<String> stringRule = s -> Validation.valid(s);

            // 2. A "sneaky" Rule<Object> that is allowed by the 'and' signature
            // Because S is String, 'other' can be Rule<Object> (since Object is a supertype of String)
            Rule<Object> sneakyRule = obj -> Validation.valid(123);

            // 3. The Composition
            // The 'and' method takes the Validation<Object> from sneakyRule
            // and casts it to Validation<String> via (Validation<S>)
            Rule<String> combinedRule = stringRule.and(sneakyRule);

            // 4. The result: since and maps the result of the second Rule back to the value of the first Rule,
            // we're protected from the misbehaving second rule
            String result = combinedRule.test("some input").getOrElseThrow();
            assertThat(result).isEqualTo("some input");
        }

        @Test
        void and_whenCombinedWithSuperRule_orderDoesntMatter() {
            Rule<Number> numberRule = Rule.of(o -> true, "msg");
            Rule<BigDecimal> decimalRule = Rule.of(o -> true, "msg");


            Rule<BigDecimal> and = decimalRule.and(numberRule);
            Rule<BigDecimal> and2 = numberRule.and(decimalRule);
        }

        @Test
        void and_whenBothRulesMatch_returnsValidResult() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> combined = rule1.and(rule2);

            // Act
            Validation<String> result = combined.test("hello");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo("hello");
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
        void or_whenFirstRuleMatches_returnsValidResult() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> combined = rule1.or(rule2);

            // Act
            Validation<String> result = combined.test("apple"); // Fails rule2, matches rule1 (length > 3)

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo("apple");
        }

        @Test
        void or_whenSecondRuleMatches_returnsValidResult() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 5, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> combined = rule1.or(rule2);

            // Act
            Validation<String> result = combined.test("hi"); // Fails rule1 (length <= 5), matches rule2

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo("hi");
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
            assertThatCode(() -> rule.fallback(null))
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
    class Xor {


        Validation<String> isLongerThan5(String in) {
            return Rule.<String>of(s -> s.length() > 5, "too.short").test(in);
        }

        @Test
        void xor_whenFirstRulePassesAndSecondRuleFails_returnsValidResult() {
            // Arrange
            Rule<String> startsWithH = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> combined = startsWithH.xor(this::isLongerThan5, "exactly.one.must.match");

            // Act
            Validation<String> result = combined.test("hello");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo("hello");
        }

        @Test
        void xor_whenFirstRuleFailsAndSecondRulePasses_returnsValidResult() {
            // Arrange
            Rule<String> startsWithH = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> isLongerThan5 = Rule.of(s -> s.length() > 5, "too.short");
            Rule<String> combined = startsWithH.xor(isLongerThan5, "exactly.one.must.match");

            // Act
            Validation<String> result = combined.test("apple pie");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo("apple pie");
        }

        @Test
        void xor_whenBothRulesPass_returnsInvalidWithErrorKey() {
            // Arrange
            Rule<String> startsWithH = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> isLongerThan3 = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> combined = startsWithH.xor(isLongerThan3, "exactly.one.must.match");

            // Act
            Validation<String> result = combined.test("hello");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("exactly.one.must.match");
        }

        @Test
        void xor_whenBothRulesFail_returnsInvalidWithErrorKey() {
            // Arrange
            Rule<String> startsWithH = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> isLongerThan10 = Rule.of(s -> s.length() > 10, "too.short");
            Rule<String> combined = startsWithH.xor(isLongerThan10, "exactly.one.must.match");

            // Act
            Validation<String> result = combined.test("apple");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("exactly.one.must.match");
        }

        @Test
        void xor_whenCombiningWithRuleOfSuperType_compilesAndWorks() {
            // Arrange
            Rule<Number> isPositive = Rule.of(n -> n.doubleValue() > 0, "must.be.positive");
            Rule<BigDecimal> isMinusFortyTwo = Rule.of(b -> b.compareTo(new BigDecimal("-42")) == 0, "must.be.minus.forty.two");

            // Act
            Rule<BigDecimal> combined = isMinusFortyTwo.xor(isPositive, "exactly.one.must.match");

            // Assert
            assertThatValidation(combined.test(new BigDecimal("10"))).isValid();
            assertThatValidation(combined.test(new BigDecimal("-42"))).isValid();

            assertThatValidation(combined.test(new BigDecimal("-1")))
                    .isInvalid()
                    .hasErrorMessage("exactly.one.must.match");
        }

        @Test
        void xor_whenOtherRuleIsNull_throwsNullPointerException() {
            // Arrange
            Rule<String> rule = Rule.of(s -> true, "ok");

            // Act & Assert
            assertThatCode(() -> rule.xor(null, "error"))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("other rule cannot be null");
        }

        @Test
        void xor_whenErrorKeyIsNull_throwsNullPointerException() {
            // Arrange
            Rule<String> rule = Rule.of(s -> true, "ok");

            // Act & Assert
            assertThatCode(() -> rule.xor(Rule.of(s -> true, "ok"), null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("errorKey cannot be null");
        }
    }

    @Nested
    class Negate {

        @Test
        void negate_withErrorKey_whenOriginalRuleMatches_returnsInvalidWithNegatedErrorKey() {
            // Arrange
            Rule<String> startsWithH = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> notStartsWithH = startsWithH.negate("must.not.start.with.h");

            // Act
            Validation<String> result = notStartsWithH.test("hello");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("must.not.start.with.h");
        }

        @Test
        void negate_withErrorKey_whenOriginalRuleFails_returnsValidResult() {
            // Arrange
            Rule<String> startsWithH = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> notStartsWithH = startsWithH.negate("must.not.start.with.h");

            // Act
            Validation<String> result = notStartsWithH.test("apple");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo("apple");
        }

        @Test
        void negate_withErrorMessage_whenOriginalRuleMatches_returnsInvalidWithNegatedErrorMessageKey() {
            // Arrange
            Rule<Integer> isEven = Rule.of(i -> i % 2 == 0, "must.be.even");
            Rule<Integer> isNotEven = isEven.negate(ErrorMessage.of("must.not.be.even"));

            // Act
            Validation<Integer> result = isNotEven.test(2);

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("must.not.be.even");
        }

        @Test
        void negate_whenNegatedErrorKeyIsNull_throwsNullPointerException() {
            // Arrange
            Rule<String> rule = Rule.of(s -> true, "ok");

            // Act & Assert
            assertThatCode(() -> rule.negate((String) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("negatedErrorKey cannot be null");
        }

        @Test
        void negate_whenNegatedErrorMessageIsNull_throwsNullPointerException() {
            // Arrange
            Rule<String> rule = Rule.of(s -> true, "ok");

            // Act & Assert
            assertThatCode(() -> rule.negate((ErrorMessage) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("negatedError cannot be null");
        }
    }

    @Nested
    class OrElse {

        @Test
        void recoverWith_whenFirstRulePasses_returnsFirstResultAndDoesNotCallSecond() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> combined = rule1.fallback(rule2);

            // Act
            Validation<String> result = combined.test("apple");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo("apple");
        }

        @Test
        void recoverWith_whenFirstRuleFailsAndSecondRulePasses_returnsSecondResult() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 5, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> combined = rule1.fallback(rule2);

            // Act
            Validation<String> result = combined.test("hi");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo("hi");
        }

        @Test
        void recoverWith_whenBothRulesFail_returnsOnlySecondRuleErrors() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 5, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> combined = rule1.fallback(rule2);

            // Act
            Validation<String> result = combined.test("abc");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("must.start.with.h");

            // Verify that first rule error is NOT present
            assertThat(result.errors().map(ErrorMessage::message)).doesNotContain("too.short");
        }

        @Test
        void recoverWith_whenOtherRuleIsNull_throwsNullPointerException() {
            // Arrange
            Rule<String> rule = Rule.of(s -> true, "msg");

            // Act & Assert
            assertThatCode(() -> rule.fallback(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("other rule cannot be null");
        }

        @Test
        void recoverWith_whenCombiningWithRuleOfSuperType_compilesAndWorks() {
            // Arrange
            Rule<Number> isPositive = Rule.of(n -> n.doubleValue() > 0, "must.be.positive");
            Rule<BigDecimal> isMinusFortyTwo = Rule.of(b -> b.compareTo(new BigDecimal("-42")) == 0, "must.be.minus.forty.two");

            // Act
            Rule<BigDecimal> combined = isMinusFortyTwo.fallback(isPositive.narrow());

            // Assert
            // 1. First rule matches
            assertThatValidation(combined.test(new BigDecimal("-42"))).isValid();

            // 2. Second rule matches (recovery)
            assertThatValidation(combined.test(new BigDecimal("10"))).isValid();

            // 3. Both fail
            assertThatValidation(combined.test(new BigDecimal("-1")))
                    .isInvalid()
                    .hasErrorMessage("must.be.positive");
        }
    }

    @Nested
    class LiftToList {

        @Test
        void liftToList_whenAllElementsAreValid_returnsValidResult() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<List<String>> listRule = rule.liftToVavrList();

            // Act
            Validation<List<String>> result = listRule.test(List.of("hello", "world"));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(List.of("hello", "world"));
        }

        @Test
        void liftToList_whenSomeElementsAreInvalid_accumulatesErrorsWithCorrectIndices() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<List<String>> listRule = rule.liftToVavrList();

            // Act
            Validation<List<String>> result = listRule.test(List.of("hello", "hi", "yo","a"));

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
            Rule<List<String>> listRule = rule.liftToVavrList();

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
        void liftToOption_whenNone_returnsValidResult() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<Option<String>> optionRule = rule.liftToOption();

            // Act
            Validation<Option<String>> result = optionRule.test(Option.none());

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(Option.none());
        }

        @Test
        void liftToOption_whenSomeAndValid_returnsValidResult() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<Option<String>> optionRule = rule.liftToOption();

            // Act
            Validation<Option<String>> result = optionRule.test(Option.of("hello"));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(Option.of("hello"));
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
    class LiftToOptional {

        @Test
        void liftToOptional_whenEmpty_returnsValidResult() {
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<Optional<String>> lifted = rule.liftToOptional();

            assertThat(lifted.test(Optional.empty())).isEqualTo(Validation.valid(Optional.empty()));
        }

        @Test
        void liftToOptional_whenNotEmptyAndValid_returnsValidResult() {
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<Optional<String>> lifted = rule.liftToOptional();

            assertThat(lifted.test(Optional.of("Alice"))).isEqualTo(Validation.valid(Optional.of("Alice")));
        }

        @Test
        void liftToOptional_whenNotEmptyAndInvalid_returnsInvalidWithSameErrors() {
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<Optional<String>> lifted = rule.liftToOptional();

            assertThatValidation(lifted.test(Optional.of("Bob")))
                    .isInvalid()
                    .hasErrorMessage("too.short");
        }
    }

    @Nested
    class LiftToVavrMap {

        @Test
        void liftToVavrMap_whenAllValuesAreValid_returnsValidResult() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<Map<String, String>> mapRule = rule.liftToVavrMap();

            Map<String, String> input = LinkedHashMap.of(
                    "a", "hello",
                    "b", "world"
            );

            // Act
            Validation<Map<String, String>> result = mapRule.test(input);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(input);
        }

        @Test
        void liftToVavrMap_whenSomeValuesAreInvalid_addsKeyToPathAndAccumulatesErrors() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<Map<String, String>> mapRule = rule.liftToVavrMap();

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
        void liftToVavrMap_withKeyExtractor_whenSomeValuesAreInvalid_usesExtractedKeyInPath() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");

            Rule<Map<Integer, String>> mapRule = rule.liftToVavrMap(k -> "k" + k);

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
        void liftToVavrMap_whenMapIsEmpty_returnsValidResult() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<Map<String, String>> mapRule = rule.liftToVavrMap();

            Map<String, String> input = HashMap.empty();

            // Act
            Validation<Map<String, String>> result = mapRule.test(input);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(input);
        }

        @Test
        void liftToVavrMap_withKeyExtractor_whenAllValuesAreValid_returnsValidResult() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");

            Rule<Map<Integer, String>> mapRule = rule.liftToVavrMap(k -> "id-" + k);

            Map<Integer, String> input = LinkedHashMap.of(
                    1, "hello",
                    2, "world"
            );

            // Act
            Validation<Map<Integer, String>> result = mapRule.test(input);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(input);
        }

        @Test
        void liftToVavrMap_withKeyExtractor_whenKeyExtractorIsNull_throwsNullPointerExceptionOnCreation() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");

            // Act & Assert
            assertThatCode(() -> rule.liftToVavrMap((io.vavr.Function1<Integer, Object>) null))
                    .isInstanceOf(NullPointerException.class);
        }

    }

    @Nested
    class LiftToMap {

        @Test
        void liftToMap_whenAllValuesAreValid_returnsValidResult() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<java.util.Map<String, String>> mapRule = rule.liftToMap();

            java.util.Map<String, String> input = new java.util.LinkedHashMap<>();
            input.put("a", "hello");
            input.put("b", "world");

            // Act
            Validation<java.util.Map<String, String>> result = mapRule.test(input);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(input);
        }

        @Test
        void liftToMap_whenSomeValuesAreInvalid_addsKeyToPathAndAccumulatesErrors() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<java.util.Map<String, String>> mapRule = rule.liftToMap();

            java.util.Map<String, String> input = new java.util.TreeMap<>();
            input.put("a", "hi");
            input.put("b", "yo");

            // Act
            Validation<java.util.Map<String, String>> result = mapRule.test(input).at("aMap");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("aMap[a].too.short", "aMap[b].too.short");
        }

        @Test
        void liftToMap_withKeyExtractor_whenSomeValuesAreInvalid_usesExtractedKeyInPath() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");

            Rule<java.util.Map<Integer, String>> mapRule = rule.liftToMap(k -> "k" + k);

            java.util.Map<Integer, String> input = new java.util.TreeMap<>();
            input.put(10, "hi");
            input.put(20, "yo");

            // Act
            Validation<java.util.Map<Integer, String>> result = mapRule.test(input).at("aMap");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("aMap[k10].too.short", "aMap[k20].too.short");
        }

        @Test
        void liftToMap_whenMapIsEmpty_returnsValidResult() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<java.util.Map<String, String>> mapRule = rule.liftToMap();

            java.util.Map<String, String> input = java.util.Collections.emptyMap();

            // Act
            Validation<java.util.Map<String, String>> result = mapRule.test(input);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(input);
        }

        @Test
        void liftToMap_whenMapIsNull_isInvalid() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<java.util.Map<String, String>> mapRule = rule.liftToMap();

            // Act
            Validation<java.util.Map<String, String>> result = mapRule.test(null);

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("must.not.be.null");
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
    class OnlyIf {

        @Test
        void onlyIf_predicate_whenConditionIsMetAndRulePasses_returnsValidResult() {
            Rule<String> rule = Rule.of(s -> s.length() > 5, "too.short");
            Rule<String> conditionalRule = rule.onlyIf(s -> s.startsWith("a"));

            assertThatValidation(conditionalRule.test("apple-pie"))
                    .isValid()
                    .isEqualTo("apple-pie");
        }

        @Test
        void onlyIf_predicate_whenConditionIsMetAndRuleFails_returnsInvalid() {
            Rule<String> rule = Rule.of(s -> s.length() > 10, "too.short");
            Rule<String> conditionalRule = rule.onlyIf(s -> s.startsWith("a"));

            assertThatValidation(conditionalRule.test("apple"))
                    .isInvalid()
                    .hasErrorMessage("too.short");
        }

        @Test
        void onlyIf_predicate_whenConditionIsNotMet_returnsValidResult() {
            Rule<String> rule = Rule.of(s -> s.length() > 10, "too.short");
            Rule<String> conditionalRule = rule.onlyIf(s -> s.startsWith("b"));

            // "apple" does not start with "b", so rule shouldn't run
            assertThatValidation(conditionalRule.test("apple"))
                    .isValid()
                    .isEqualTo("apple");
        }

        @Test
        void onlyIf_supplier_whenConditionIsMetAndRuleFails_returnsInvalid() {
            Rule<String> rule = Rule.of(s -> s.length() > 5, "too.short");
            Rule<String> conditionalRule = rule.onlyIf(() -> true);

            assertThatValidation(conditionalRule.test("abc"))
                    .isInvalid()
                    .hasErrorMessage("too.short");
        }

        @Test
        void onlyIf_supplier_whenConditionIsNotMet_returnsValidResult() {
            Rule<String> rule = Rule.of(s -> s.length() > 5, "too.short");
            Rule<String> conditionalRule = rule.onlyIf(() -> false);

            assertThatValidation(conditionalRule.test("abc"))
                    .isValid()
                    .isEqualTo("abc");
        }

        @Test
        void onlyIf_predicate_whenConditionIsNull_throwsNullPointerException() {
            Rule<String> rule = Rule.of(s -> true, "ok");
            assertThatCode(() -> rule.onlyIf((Predicate<String>) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("condition cannot be null");
        }

        @Test
        void onlyIf_supplier_whenConditionIsNull_throwsNullPointerException() {
            Rule<String> rule = Rule.of(s -> true, "ok");
            assertThatCode(() -> rule.onlyIf((java.util.function.Supplier<Boolean>) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("condition cannot be null");
        }
    }

    @Nested
    class With {

        record StringHolder(String value) {
        }

        @Test
        void with_whenRulePasses_returnsValidResult() {
            // Arrange
            Rule<CharSequence> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<StringHolder> withRule = Rule.with(StringHolder::value, rule);

            // Act
            Validation<StringHolder> result = withRule.test(new StringHolder("1234"));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(new StringHolder("1234"));
        }

        @Test
        void with_whenRuleFails_returnsInvalidWithRuleErrors() {
            // Arrange
            Rule<CharSequence> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<StringHolder> withRule = Rule.with(StringHolder::value, rule);

            // Act
            Validation<StringHolder> result = withRule.test(new StringHolder("12"));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("too.short");
        }
    }

    @Nested
    class Given {

        record StringHolder(String value) {
        }

        @Test
        void given_whenRulePasses_returnsValidResult() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<StringHolder> givenRule = rule.given(StringHolder::value);

            // Act
            Validation<StringHolder> result = givenRule.test(new StringHolder("1234"));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(new StringHolder("1234"));
        }

        @Test
        void given_whenRuleFails_returnsInvalidWithRuleErrors() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<StringHolder> givenRule = rule.given(StringHolder::value);

            // Act
            Validation<StringHolder> result = givenRule.test(new StringHolder("12"));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("too.short");
        }
    }

    @Nested
    class WithErrorKey {

        @Test
        void withErrorKey_whenRuleIsInvalid_replacesErrorsWithNewErrorKey() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 5, "too.short");
            Rule<String> describedRule = rule.withErrorKey("invalid.input");

            // Act
            Validation<String> result = describedRule.test("abc");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorKeys("invalid.input");

            assertThat(result.errors().map(ErrorMessage::key))
                    .doesNotContain("too.short");
        }

        @Test
        void withErrorKey_whenRuleIsValid_preservesValidResult() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 5, "too.short");
            Rule<String> describedRule = rule.withErrorKey("invalid.input");

            // Act
            Validation<String> result = describedRule.test("abcdef");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo("abcdef");
        }
    }

    @Nested
    class When {

        @Test
        void when_whenConditionIsTrue_appliesRule() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> conditionalRule = Rule.when(true, rule);

            // Act
            Validation<String> result = conditionalRule.test("hello");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo("hello");
        }

        @Test
        void when_whenConditionIsTrue_appliesRuleReturnsInvalid() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> conditionalRule = Rule.when(true, rule);

            // Act
            Validation<String> result = conditionalRule.test("he");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("too.short");
        }

        @Test
        void when_whenConditionIsFalse_skipsRule() {
            // Arrange
            Rule<String> rule = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> conditionalRule = Rule.when(false, rule);

            // Act
            Validation<String> result = conditionalRule.test("hi");

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo("hi");
        }
    }

    @Nested
    class Choose {

        @Test
        void choose_whenConditionIsTrue_returnsFirstRuleResult() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> chosenRule = Rule.choose(true, rule1, rule2);

            // Act
            Validation<String> result = chosenRule.test("hi");

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("too.short");
        }

        @Test
        void choose_whenConditionIsFalse_returnsSecondRuleResult() {
            // Arrange
            Rule<String> rule1 = Rule.of(s -> s.length() > 3, "too.short");
            Rule<String> rule2 = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> chosenRule = Rule.choose(false, rule1, rule2);

            // Act
            Validation<String> result = chosenRule.test("hi");

            // Assert
            assertThatValidation(result).isValid().isEqualTo("hi");
        }
    }
}