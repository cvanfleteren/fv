package net.vanfleteren.fv;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static net.vanfleteren.fv.assertj.ValidationAssert.assertThatValidation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class RuleTest {

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
            assertThatCode(() -> Rule.of(s -> true, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("errorMessage cannot be null");
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
}