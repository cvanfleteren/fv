package net.vanfleteren.fv;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Represents a validation rule that can be applied to a value.
 * @param <T> The type of the value to be validated.
 */
@FunctionalInterface
public interface Rule<T> {

    /**
     * Tests the given value against the rule.
     * @param value The value to be validated.
     * @return A Validation object indicating the result of the test.
     */
    Validation<T> test(T value);

    static <T> Rule<T> of(Predicate<T> predicate, String errorMessage) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        return value -> predicate.test(value) ? Validation.valid(value) : Validation.invalid(ErrorMessage.of(errorMessage));
    }

    default Rule<T> and(Rule<? super T> other) {
        Objects.requireNonNull(other, "other rule cannot be null");
        return value -> test(value).flatMap(v -> other.test(value).map(o -> v));
    }

    default Rule<T> or(Rule<? super T> other) {
        Objects.requireNonNull(other, "other rule cannot be null");
        return input -> {
            Validation<T> first = this.test(input);
            if (first.isValid()) {
                return first;
            }

            Validation<T> second = Validation.narrowSuper(other.test(input));
            if (second.isValid()) {
                return second;
            }

            return Validation.invalid(first.errors().appendAll(second.errors()));
        };
    }


    /**
     * Narrows a {@code Rule<? super T>} to a {@code Rule<T>}.
     * @param rule The rule to narrow.
     * @param <T> The target type.
     * @return The narrowed rule.
     */
    @SuppressWarnings("unchecked")
    static <T> Rule<T> narrow(Rule<? super T> rule) {
        return (Rule<T>) rule;
    }

}
