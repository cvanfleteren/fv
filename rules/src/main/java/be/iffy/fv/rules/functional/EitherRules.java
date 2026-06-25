package be.iffy.fv.rules.functional;

import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import be.iffy.fv.rules.IObjectRules;
import io.vavr.control.Either;

import java.util.Objects;

/**
 * Validation rules for {@link Either} values.
 *
 */
public final class EitherRules<L, R> implements IObjectRules<Either<L, R>> {

    private EitherRules() {}

    /**
     * Singleton instance of {@link EitherRules}.
     */
    private static final EitherRules<?, ?> eithers = new EitherRules<>();

    /**
     * Returns the singleton instance of {@link EitherRules}.
     *
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
     * If it's a Right, the error key and parameters from the provided rule are used.
     *
     * @param rule the rule to apply to the right value.
     */
    public Rule<Either<L, R>> isRight(Rule<? super R> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return isRight().then(validateRightWith(rule));
    }

    /**
     * Fails if the {@link Either} is not a {@link Either.Left}.
     * <p>
     * Error key: {@code must.be.left}
     */
    public Rule<Either<L, R>> isLeft() {
        return Rule.of(
                Either::isLeft,
                "must.be.left"
        );
    }

    /**
     * Fails if the {@link Either} is not a {@link Either.Left} or if the left value fails the specified rule.
     * <p>
     * Error key: {@code must.be.left} if it's a right.
     * If it's a left, the error key and parameters from the provided rule are used.
     *
     * @param rule the rule to apply to the left value.
     */
    public Rule<Either<L, R>> isLeft(Rule<? super L> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return isLeft().then(validateLeftWith(rule));
    }

    /**
     * Fails if the value in the {@link Either#left()}  doesn't pass the {@code rule}.
     * <p>
     * If the {@link Either} is a {@link Either.Right}, the validation succeeds.
     *
     * @param rule the rule to apply to the left value.
     */
    public Rule<Either<L, R>> validateLeftWith(Rule<? super L> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return Rule.of((Either<L,R> either) -> {
            if (either.isLeft()) {
                return rule.apply(either.getLeft()).map(ignore -> either);
            }
            return Validation.valid(either);
        });
    }

    /**
     * Fails if the value in the {@link Either#right()}  doesn't pass the {@code rule}.
     * <p>
     * If the {@link Either} is a {@link Either.Left}, the validation succeeds.
     *
     * @param rule the rule to apply to the right value.
     */
    public Rule<Either<L, R>> validateRightWith(Rule<? super R> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return Rule.of(either -> {
            if (either.isRight()) {
                return rule.apply(either.get()).map(ignore -> either);
            }
            return Validation.valid(either);
        });
    }

}
