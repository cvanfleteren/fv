package be.iffy.fv.dsl.impl;

import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import io.vavr.collection.List;

import java.util.Objects;

/**
 * DSL class for validating a collection of values.
 *
 */
public class ValidateAllDSL<T> {
    private final Iterable<T> values;

    public ValidateAllDSL(Iterable<T> values) {
        this.values = Objects.requireNonNull(values);
    }

    /**
     * Validates that all values in the collection satisfy the given rule.
     * The result is a validation containing the list of valid values, or all errors encountered.
     *
     * @param rule the rule to apply to each value.
     */
    public Validation<List<T>> areAll(Rule<? super T> rule) {
        Objects.requireNonNull(rule, "Rule cannot be null");
        return Validation.transpose(List.ofAll(values).map(v -> Validation.narrowSuper(rule.test(v))));
    }
}
