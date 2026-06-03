package be.iffy.fv.dsl.impl;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Transformation;
import be.iffy.fv.Validation;

import java.util.Objects;
import java.util.function.Function;

public class AssertDSL<T> {
    private final String name;
    private final Validation<T> validation;

    public AssertDSL(T value, String name) {
        this.validation = Validation.valid(value);
        this.name = name;
    }

    AssertDSL(Validation<T> validation, String name) {
        this.validation = validation;
        this.name = name;
    }

    public AssertDSL<T> map(Transformation<T> transform) {
        return new AssertDSL<>(this.validation.mapCatching(transform::apply), name);
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
