package net.vanfleteren.fv;

import io.vavr.Function1;
import io.vavr.collection.List;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Represents a validation rule that can be applied to a value.
 *
 * @param <T> The type of the value to be validated.
 */
@FunctionalInterface
public interface Rule<T> {

    /**
     * Tests the given value against the rule.
     *
     * @param value The value to be validated.
     * @return A Validation object indicating the result of the test.
     */
    Validation<T> test(T value);

    static <T> Rule<T> of(Predicate<T> predicate, String errorMessage) {
        return of(predicate, ErrorMessage.of(errorMessage));
    }

    static <T> Rule<T> of(Predicate<T> predicate, ErrorMessage errorMessage) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        return value -> predicate.test(value) ? Validation.valid(value) : Validation.invalid(errorMessage);
    }

    @SuppressWarnings("unchecked")
    default <S extends T> Rule<S> and(Rule<? super S> other) {
        Objects.requireNonNull(other, "other rule cannot be null");
        return value -> test(value).flatMap(v -> other.test(value).map(o -> (S) v));
    }

    @SuppressWarnings("unchecked")
    default <S extends T> Rule<S> or(Rule<? super S> other) {
        Objects.requireNonNull(other, "other rule cannot be null");
        return input -> {
            Validation<S> first = (Validation<S>) this.test(input);
            if (first.isValid()) {
                return first;
            }

            Validation<S> second = (Validation<S>) other.test(input);
            if (second.isValid()) {
                return second;
            }

            return Validation.invalid(first.errors().appendAll(second.errors()));
        };
    }

    /**
     * Negates this rule. The caller must provide the error message to use when the negated rule fails.
     *
     * Semantics:
     * - if this rule is valid => negated rule is invalid (with {@code negatedError})
     * - if this rule is invalid => negated rule is valid
     */
    default Rule<T> not(String negatedErrorKey) {
        Objects.requireNonNull(negatedErrorKey, "negatedErrorKey cannot be null");
        return not(ErrorMessage.of(negatedErrorKey));
    }

    default Rule<T> not(ErrorMessage negatedError) {
        Objects.requireNonNull(negatedError, "negatedError cannot be null");
        return value -> {
            Validation<T> original = this.test(value);
            return original.isValid()
                    ? Validation.invalid(negatedError)
                    : Validation.valid(value);
        };
    }

    /**
     * Negates this rule and derives the negated error from the original rule's first error message.
     * Useful if you want conventions like prefixing keys, or to preserve args.
     */
    default Rule<T> not(Function1<ErrorMessage, ErrorMessage> errorMapper) {
        Objects.requireNonNull(errorMapper, "errorMapper cannot be null");
        return value -> {
            Validation<T> original = this.test(value);
            if (original.isValid()) {
                // original passed => negation fails; we need an error
                // we don't have one, so we manufacture it from the original rule's error "template"
                // NOTE: since Rule doesn't expose its "default" ErrorMessage, we use a conservative default key.
                // If you want richer behavior, prefer not(String)/not(ErrorMessage) or extend Rule to expose metadata.
                ErrorMessage fallback = ErrorMessage.of("must.not.satisfy.rule");
                return Validation.invalid(errorMapper.apply(fallback));
            }
            return Validation.valid(value);
        };
    }

    /**
     * Adapts a Rule so it applies to a List of T instead of a single T.
     */
    default Rule<List<T>> adaptToList() {
        return values -> {
            List<Validation<T>> validations = values.map(this::test);
            // Validation.sequence already adds the [index] path segment, so we don't do it here.
            return Validation.sequence(validations);
        };
    }

    /**
     * Narrows a {@code Rule<? super T>} to a {@code Rule<T>}.
     *
     * @param rule The rule to narrow.
     * @param <T>  The target type.
     * @return The narrowed rule.
     */
    @SuppressWarnings("unchecked")
    static <T> Rule<T> narrow(Rule<? super T> rule) {
        return (Rule<T>) rule;
    }

    static <T> Rule<T> notNull() {
        return Rule.of(Objects::nonNull, "cannot.be.null");
    }

}
