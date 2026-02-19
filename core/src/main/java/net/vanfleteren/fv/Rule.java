package net.vanfleteren.fv;

/**
 * Represents a validation rule that can be applied to a value.
 * @param <T> The type of the value to be validated.
 */
public interface Rule<T> {

    /**
     * Tests the given value against the rule.
     * @param value The value to be validated.
     * @return A Validation object indicating the result of the test.
     */
    Validation<T> test(T value);

}
