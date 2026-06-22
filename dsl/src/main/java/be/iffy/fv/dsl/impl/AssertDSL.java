package be.iffy.fv.dsl.impl;

import be.iffy.fv.*;
import io.vavr.control.Option;
import org.jetbrains.annotations.Contract;

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

    // since AssertDSL mirrors ValidationsDSL, but returning the T instead of Validation<T>
    // delegate all methods to a ValidationDSL and just call .getOrElseThrow
    private final ValidationDSL<T> validationDSL;

    public AssertDSL(T value, Option<String> name) {
        this.validationDSL = new ValidationDSL<>(value, name);
    }

    private AssertDSL(ValidationDSL<T> validationDSL) {
        this.validationDSL = validationDSL;
    }

    /**
     * Transforms the value being validated using the provided transformation function.
     * If the current validation is already invalid, the transformation is not applied.
     * No exceptions are caught, use {@link #map(MappingRule)} if you have a mapper that could throw.
     */
    @Contract(pure = true)
    public AssertDSL<T> after(be.iffy.fv.Transformation<T> transformation) {
        return new AssertDSL<>(validationDSL.after(transformation));
    }

    /**
     * Like {@link #after(Transformation)}, but takes multiple Transformations and applies them in sequence.
     */
    @SafeVarargs
    @Contract(pure = true)
    public final AssertDSL<T> after(be.iffy.fv.Transformation<T> first, be.iffy.fv.Transformation<T>... rest) {
        return new AssertDSL<>(validationDSL.after(first, rest));
    }

    /**
     * Maps the validation from type T to type R using the provided mapping rule.
     */
    @Contract(pure = true)
    public <R> AssertDSL<R> map(MappingRule<T, R> mapper) {
        return new AssertDSL<>(validationDSL.map(mapper));
    }

    /**
     * Asserts that the value satisfies the given rule.
     */
    public <R> R is(RuleLike<? super T, ? extends Validation<R>> rule) throws ValidationException {
        return validationDSL.is(rule).getOrElseThrow();
    }

    /**
     * Asserts that the value is not null.
     */
    public T isNotNull() throws ValidationException {
        return validationDSL.isNotNull().getOrElseThrow();
    }
}
