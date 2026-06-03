package be.iffy.fv.dsl.impl;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;

import java.util.Objects;
import java.util.function.Function;

/**
 * DSL class for validating a single value.
 *
 */
public class ValidationDSL<T> {

    private final Validation<T> validation;
    private String name = "";

    public ValidationDSL(T value) {
        this.validation = Rule.<T>notNull().test(value);
    }

    public ValidationDSL(T value, String name) {
        if (value == null) {
            this.validation = Validation.invalid(ErrorMessage.of("must.not.be.null"));
        } else {
            this.validation = Validation.valid(value);
        }
        this.name = name;
    }

    private ValidationDSL(Validation<T> validation, String name) {
        this.validation = Objects.requireNonNull(validation, "validation cannot be null");
        this.name = name;
    }

    /**
     * Maps the value being validated using the provided mapper function.
     * If the current validation is already invalid, the mapper is not applied.
     * If the mapper throws an Exception, it is caught and the validation becomes {@link Validation.Invalid}
     */
    public ValidationDSL<T> map(be.iffy.fv.Transformation<T> mapper) {
        return new ValidationDSL<>(validation.mapCatching(mapper::apply), name);
    }

    public <R> ValidationDSL<R> map(MappingRule<T, R> mapper) {
        return new ValidationDSL<>(validation.refine(mapper), name);
    }

    /**
     * Validates that the value satisfies the given rule.
     * If the value is {@code null}, an error "must.not.be.null" is automatically added.
     */
    public Validation<T> is(Rule<? super T> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return validation.refine(Rule.narrow(rule)).at(name);
    }

    /**
     * Validates that the value satisfies the given rule.
     */
    public <R> Validation<R> mapsTo(Function<? super T, ? extends Validation<R>> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return Validation.narrow(validation.refine(rule::apply).at(name));
    }

    /**
     * Validates that the value is not null.
     */
    public Validation<T> isNotNull() {
        return validation.refine(Rule.notNull()).at(name);
    }
}
