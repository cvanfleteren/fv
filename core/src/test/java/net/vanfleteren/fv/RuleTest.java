package net.vanfleteren.fv;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static net.vanfleteren.fv.assertj.ValidationAssert.assertThatValidation;
import static org.assertj.core.api.Assertions.assertThatCode;

class RuleTest {

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
}