package be.iffy.fv.dsl.impl;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import io.vavr.control.Option;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A fluent API for performing assertions on a value.
 * This class allows for chaining transformations and rules to verify the state of a value.
 * <p>
 * {@snippet :
 *
 * record Record(String value) {
 *
 *    public Record {
 *        value = assertThat(value,"value").after(stringOps.trim()).is(strings.minLength(2));
 *    }
 *
 * }
 *}
 *
 */
public final class AssertDSL<T> {

    private final Option<String> name;
    private final Validation<T> validation;

    public AssertDSL(T value, String name) {
        this.validation = Rule.<T>notNull().apply(value);
        this.name = Option.of(name).filter(Predicate.not(String::isBlank));
    }

    AssertDSL(Validation<T> validation, Option<String> name) {
        this.validation = validation;
        this.name = name;
    }

    /**
     * Transforms the value being validated using the provided transformation function.
     * If the current validation is already invalid, the transformation is not applied.
     * No exceptions are caught, use #map({@link MappingRule} if you have a mapper that could throw.
     */
    public AssertDSL<T> after(be.iffy.fv.Transformation<T> transformation) {
        Objects.requireNonNull(transformation, "transformation cannot be null");
        return new AssertDSL<>(validation.map(transformation::apply), name);
    }

    /**
     * Maps the validation from type T to type R using the provided mapping rule.
     */
    public <R> AssertDSL<R> map(MappingRule<T, R> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return new AssertDSL<>(validation.refine(mapper), name);
    }

    /**
     * Asserts that the value satisfies the given rule.
     */
    public <R> R is(Function<? super T, ? extends Validation<R>> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        Validation<R> refined = validation.refine(MappingRule.of(rule));
        return name.fold(
            () -> refined,
            refined::at
        ).getOrElseThrow();
    }

    /**
     * Asserts that the value is not null.
     */
    public T isNotNull() {
        return is(Rule.notNull());
    }
}
