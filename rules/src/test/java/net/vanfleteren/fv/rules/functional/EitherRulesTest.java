package net.vanfleteren.fv.rules.functional;

import io.vavr.control.Either;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.rules.numbers.IntegerRules;
import net.vanfleteren.fv.rules.text.StringRules;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.API.validateThat;
import static net.vanfleteren.fv.assertj.ValidationAssert.assertThatValidation;
import static net.vanfleteren.fv.rules.functional.EitherRules.eithers;
import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;

class EitherRulesTest {

    @Nested
    class IsRightTests {
        @Test
        void isRight_whenRight_succeeds() {
            validTest(Either.right("ok"), eithers().isRight());
        }

        @Test
        void isRight_whenLeft_fails() {
            invalidTest(Either.left("error"), eithers().isRight(), "must.be.right");
            invalidTest(null, eithers().isRight(), "must.not.be.null");
        }

        @Test
        void isRightWithRule_whenRightMatchesRule_succeeds() {
            validTest(Either.<String, Integer>right(10), EitherRules.<String, Integer>eithers().isRight(IntegerRules.ints().positive()));
        }

        @Test
        void isRightWithRule_whenRightFailsRule_fails() {
            Rule<Either<String, Integer>> rule = EitherRules.<String, Integer>eithers().isRight(IntegerRules.ints().positive());
            assertThatValidation(validateThat(Either.<String, Integer>right(-1), "value").is(rule))
                    .isInvalid()
                    .hasErrorMessages("value.must.be.positive");
        }

        @Test
        void isRightWithRule_whenLeft_failsWithMustBeRight() {
            Rule<Either<String, Integer>> rule = EitherRules.<String, Integer>eithers().isRight(IntegerRules.ints().positive());
            invalidTest(Either.left("error"), rule, "must.be.right");
        }
    }

    @Nested
    class IsLeftTests {
        @Test
        void isLeft_whenLeft_succeeds() {
            validTest(Either.left("error"), eithers().isLeft());
        }

        @Test
        void isLeft_whenRight_fails() {
            invalidTest(Either.right("ok"), eithers().isLeft(), "must.be.left");
            invalidTest(null, eithers().isLeft(), "must.not.be.null");
        }

        @Test
        void isLeftWithRule_whenLeftMatchesRule_succeeds() {
            validTest(Either.<String, Integer>left("error"), EitherRules.<String, Integer>eithers().isLeft(StringRules.strings().notEmpty()));
        }

        @Test
        void isLeftWithRule_whenLeftFailsRule_fails() {
            Rule<Either<String, Integer>> rule = EitherRules.<String, Integer>eithers().isLeft(StringRules.strings().notEmpty());
            assertThatValidation(validateThat(Either.<String, Integer>left(""), "value").is(rule))
                    .isInvalid()
                    .hasErrorMessages("value.must.not.be.empty");
        }

        @Test
        void isLeftWithRule_whenRight_failsWithMustBeLeft() {
            Rule<Either<String, Integer>> rule = EitherRules.<String, Integer>eithers().isLeft(StringRules.strings().notEmpty());
            invalidTest(Either.right(10), rule, "must.be.left");
        }
    }

    @Nested
    class ValidateLeftWithTests {
        @Test
        void validateLeftWith_whenRight_succeeds() {
            validTest(Either.<String, Integer>right(10), EitherRules.<String, Integer>eithers().validateLeftWith(StringRules.strings().notEmpty()));
        }

        @Test
        void validateLeftWith_whenLeftMatchesRule_succeeds() {
            validTest(Either.<String, Integer>left("error"), EitherRules.<String, Integer>eithers().validateLeftWith(StringRules.strings().notEmpty()));
        }

        @Test
        void validateLeftWith_whenLeftFailsRule_fails() {
            Rule<Either<String, Integer>> rule = EitherRules.<String, Integer>eithers().validateLeftWith(StringRules.strings().notEmpty());
            assertThatValidation(validateThat(Either.<String, Integer>left(""), "value").is(rule))
                    .isInvalid()
                    .hasErrorMessages("value.must.not.be.empty");
            invalidTest(null, rule, "must.not.be.null");
        }
    }

    @Nested
    class ValidateRightWithTests {
        @Test
        void validateRightWith_whenLeft_succeeds() {
            validTest(Either.<String, Integer>left("error"), EitherRules.<String, Integer>eithers().validateRightWith(IntegerRules.ints().positive()));
        }

        @Test
        void validateRightWith_whenRightMatchesRule_succeeds() {
            validTest(Either.<String, Integer>right(10), EitherRules.<String, Integer>eithers().validateRightWith(IntegerRules.ints().positive()));
        }

        @Test
        void validateRightWith_whenRightFailsRule_fails() {
            Rule<Either<String, Integer>> rule = EitherRules.<String, Integer>eithers().validateRightWith(IntegerRules.ints().positive());
            assertThatValidation(validateThat(Either.<String, Integer>right(-1), "value").is(rule))
                    .isInvalid()
                    .hasErrorMessages("value.must.be.positive");
            invalidTest(null, rule, "must.not.be.null");
        }
    }
}
