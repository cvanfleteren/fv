package net.vanfleteren.fv;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
}