package be.iffy.fv.dsl.impl;

import be.iffy.fv.*;

import java.util.Objects;
import java.util.function.Function;

public final class AssertDSL<T> {
    private final String name;
    private final Validation<T> validation;

    public AssertDSL(T value, String name) {
        this.validation = Rule.<T>notNull().test(value);
        this.name = name;
    }

    AssertDSL(Validation<T> validation, String name) {
        this.validation = validation;
        this.name = name;
    }
    /**
     * Maps the value being validated using the provided mapper function.
     * If the current validation is already invalid, the mapper is not applied.
     * If the mapper throws an Exception, it is caught and the validation becomes {@link Validation.Invalid}
     */
    public AssertDSL<T> map(Transformation<T> mapper) {
        return new AssertDSL<>(validation.mapCatching(mapper::apply), name);
    }

    public <R> AssertDSL<R> map(MappingRule<T, R> mapper) {
        return new AssertDSL<>(validation.refine(mapper), name);
    }

    /**
     * Validates that the value satisfies the given rule.
     */
    public <R> R is(Function<? super T, ? extends Validation<R>> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return validation.refine(MappingRule.of(rule)).at(name).getOrElseThrow();
    }

    public T isNotNull() {
        return validation.refine(Rule.notNull()).at(name).getOrElseThrow();
    }
}
