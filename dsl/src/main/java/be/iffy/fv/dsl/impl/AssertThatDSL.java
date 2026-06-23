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
public final class AssertThatDSL<T> {

    // since AssertDSL mirrors ValidationsDSL, but returning the T instead of Validation<T>
    // delegate all methods to a ValidationDSL and just call .getOrElseThrow
    private final ValidateThatDSL<T> validateThatDSL;

    public AssertThatDSL(T value, Option<String> name) {
        this.validateThatDSL = new ValidateThatDSL<>(value, name);
    }

    private AssertThatDSL(ValidateThatDSL<T> validateThatDSL) {
        this.validateThatDSL = validateThatDSL;
    }

    /**
     * Transforms the value being validated using the provided transformation function.
     * If the current validation is already invalid, the transformation is not applied.
     * No exceptions are caught, use {@link #map(MappingRule)} if you have a mapper that could throw.
     */
    @Contract(pure = true)
    public AssertThatDSL<T> after(be.iffy.fv.Transformation<T> transformation) {
        return new AssertThatDSL<>(validateThatDSL.after(transformation));
    }

    /**
     * Like {@link #after(Transformation)}, but takes multiple Transformations and applies them in sequence.
     */
    @SafeVarargs
    @Contract(pure = true)
    public final AssertThatDSL<T> after(be.iffy.fv.Transformation<T> first, be.iffy.fv.Transformation<T>... rest) {
        return new AssertThatDSL<>(validateThatDSL.after(first, rest));
    }

    /**
     * Maps the validation from type T to type R using the provided mapping rule.
     */
    @Contract(pure = true)
    public <R> AssertThatDSL<R> map(MappingRule<T, R> mapper) {
        return new AssertThatDSL<>(validateThatDSL.map(mapper));
    }

    /**
     * Asserts that the value satisfies the given rule.
     */
    public <R> R is(RuleLike<? super T, ? extends Validation<R>> rule) throws ValidationException {
        return validateThatDSL.is(rule).getOrElseThrow();
    }

    /**
     * Asserts that the value is not null.
     */
    public T isNotNull() throws ValidationException {
        return validateThatDSL.isNotNull().getOrElseThrow();
    }
}
