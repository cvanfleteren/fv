package net.vanfleteren.fv.rules.functional;

import io.vavr.control.Either;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.rules.IObjectRules;

import java.util.Objects;

/**
 * Validation rules for {@link Either} values.
 *
 * @param <L> the type of the left value.
 * @param <R> the type of the right value.
 */
public class EitherRules<L, R> implements IObjectRules<Either<L, R>> {

    /**
     * Singleton instance of {@link EitherRules}.
     */
    private static final EitherRules<?, ?> eithers = new EitherRules<>();

    /**
     * Returns the singleton instance of {@link EitherRules}.
     *
     * @param <L> the type of the left value.
     * @param <R> the type of the right value.
     * @return the {@link EitherRules} instance.
     */
    @SuppressWarnings("unchecked")
    public static <L, R> EitherRules<L, R> eithers() {
        return (EitherRules<L, R>) eithers;
    }

    /**
     * Fails if the {@link Either} is not a {@link Either.Right}.
     * <p>
     * Error key: {@code must.be.right}
     *
     * @return a {@link Rule} checking if the either is a right.
     */
    public Rule<Either<L, R>> isRight() {
        return Rule.of(Either::isRight, "must.be.right");
    }

    /**
     * Fails if the {@link Either} is not a {@link Either.Right} or if the right value fails the specified rule.
     * <p>
     * Error key: {@code must.be.right} if it's a left.
     * If it's a right, the error key and parameters from the provided rule are used.
     *
     * @param rule the rule to apply to the right value.
     * @return a {@link Rule} that validates if the either is a right and matches the rule.
     */
    public Rule<Either<L, R>> isRight(Rule<? super R> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return isRight().and(validateRightWith(rule));
    }

    /**
     * Fails if the {@link Either} is not a {@link Either.Left}.
     * <p>
     * Error key: {@code must.be.left}
     *
     * @return a {@link Rule} checking if the either is a left.
     */
    public Rule<Either<L, R>> isLeft() {
        return Rule.of(Either::isLeft, "must.be.left");
    }

    /**
     * Fails if the {@link Either} is not a {@link Either.Left} or if the left value fails the specified rule.
     * <p>
     * Error key: {@code must.be.left} if it's a right.
     * If it's a left, the error key and parameters from the provided rule are used.
     *
     * @param rule the rule to apply to the left value.
     * @return a {@link Rule} that validates if the either is a left and matches the rule.
     */
    public Rule<Either<L, R>> isLeft(Rule<? super L> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return isLeft().and(validateLeftWith(rule));
    }

    /**
     * Validates the value of a {@link Either.Left} using the specified rule.
     * <p>
     * If the {@link Either} is a {@link Either.Right}, the validation succeeds.
     * If the {@link Either} is a {@link Either.Left}, the rule is applied to its value.
     *
     * @param rule the rule to apply to the left value.
     * @return a {@link Rule} that validates the left value.
     */
    public Rule<Either<L, R>> validateLeftWith(Rule<? super L> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return either -> {
            if (either.isLeft()) {
                return rule.test(either.getLeft()).map(ignore -> either);
            }
            return net.vanfleteren.fv.Validation.valid(either);
        };
    }

    /**
     * Validates the value of a {@link Either.Right} using the specified rule.
     * <p>
     * If the {@link Either} is a {@link Either.Left}, the validation succeeds.
     * If the {@link Either} is a {@link Either.Right}, the rule is applied to its value.
     *
     * @param rule the rule to apply to the right value.
     * @return a {@link Rule} that validates the right value.
     */
    public Rule<Either<L, R>> validateRightWith(Rule<? super R> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return either -> {
            if (either.isRight()) {
                return rule.test(either.get()).map(ignore -> either);
            }
            return net.vanfleteren.fv.Validation.valid(either);
        };
    }

}
