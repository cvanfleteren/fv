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
public final class ValidationDSL<T> {

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
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return new ValidationDSL<>(validation.mapCatching(mapper::apply), name);
    }

    /**
     * Transforms the validation from type T to type R using the provided mapping rule.
     *
     * @param mapper the mapping rule used to refine and transform the validation
     * @return a new ValidationDSL instance representing the transformed validation
     */
    public <R> ValidationDSL<R> map(MappingRule<T, R> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return new ValidationDSL<>(validation.refine(mapper), name);
    }

    /**
     * Validates that the value satisfies the given rule.
     * If the value is {@code null}, an error "must.not.be.null" is automatically added.
     */
    public Validation<T> is(Rule<? super T> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        if (name.isEmpty()) {
            return validation.refine(rule.narrow());
        } else {
            return validation.refine(rule.narrow()).at(name);
        }
    }

    /**
     * Validates that the value satisfies the given rule.
     */
    public <R> Validation<R> is(Function<? super T, ? extends Validation<? extends R>> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        if (name.isEmpty()) {
            return Validation.narrow(
                    validation.flatMap(value ->
                            Validation.narrow(
                                    Objects.requireNonNull(rule.apply(value), "rule cannot return null Validation")
                            )
                    )
            );
        } else {
            return Validation.narrow(
                    validation.flatMap(value ->
                            Validation.narrow(
                                    Objects.requireNonNull(rule.apply(value), "rule cannot return null Validation")
                            )
                    ).at(name)
            );
        }
    }

    /**
     * Validates that the value is not null.
     */
    public Validation<T> isNotNull() {
        return validation.refine(Rule.notNull()).at(name);
    }
}
