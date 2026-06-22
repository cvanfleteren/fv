package be.iffy.fv.dsl.impl;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Transformation;
import be.iffy.fv.Validation;
import io.vavr.control.Option;
import org.jetbrains.annotations.Contract;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * DSL class for validating a single value.
 * {@snippet :
 *
 * record Record(String value, int number) {
 *
 *    public Record {
 *        value = assertValid(
 *             validateThat(value,"value").after(stringOps.trim()).is(strings.minLength(2)),
 *             validateThat(number,"number").is(ints.positive())
 *        )._1;
 *    }
 *
 * }
 *}
 */
public final class ValidationDSL<T> {

    private final Validation<T> validation;
    private final Option<String> name;

    public ValidationDSL(T value, Option<String> name) {
        this.validation = Validation.fromNullable(value);
        this.name = name.filter(Predicate.not(String::isBlank));
    }

    private ValidationDSL(Validation<T> validation, Option<String> name) {
        this.validation = Objects.requireNonNull(validation, "validation cannot be null");
        this.name = name;
    }

    /**
     * Transforms the value being validated using the provided transformation function.
     * If the current validation is already invalid, the transformation is not applied.
     * No exceptions are caught, use {@link #map(MappingRule)} if you have a mapper that could throw.
     */
    @Contract(pure = true)
    public ValidationDSL<T> after(be.iffy.fv.Transformation<T> transformation) {
        Objects.requireNonNull(transformation, "transformation cannot be null");
        return new ValidationDSL<>(validation.map(transformation::apply), name);
    }

    /**
     * Like {@link #after(Transformation)}, but takes multiple Transformations and applies them in sequence.
     */
    @SafeVarargs
    @Contract(pure = true)
    public final ValidationDSL<T> after(be.iffy.fv.Transformation<T> first, be.iffy.fv.Transformation<T>... rest) {
        return new ValidationDSL<>(
            validation.map(Transformation.sequence(first, rest)::apply),
            name
        );
    }

    /**
     * Maps the validation from type T to type R using the provided mapping rule.
     */
    @Contract(pure = true)
    public <R> ValidationDSL<R> map(MappingRule<T, R> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return new ValidationDSL<>(validation.refine(mapper), name);
    }

    /**
     * Validates that the value satisfies the given rule.
     * If the value is {@code null}, an error "must.not.be.null" is automatically added.
     */
    @Contract(pure = true)
    public Validation<T> is(Rule<? super T> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        Validation<T> refined = validation.refine(rule.narrow());
        return validationAtName(refined);
    }

    /**
     * Validates that the value satisfies the given rule.
     */
    @Contract(pure = true)
    public <R> Validation<R> is(Function<? super T, ? extends Validation<? extends R>> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");

        Validation<R> refined = validation.flatMap(value ->
            Validation.narrow(
                Objects.requireNonNull(rule.apply(value), "rule cannot return null Validation")
            )
        );

       return validationAtName(refined);
    }

    /**
     * Validates that the value is not null.
     */
    @Contract(pure = true)
    public Validation<T> isNotNull() {
        // not null is checked at construction
        return validationAtName(validation);
    }

    // adds the name to the Validation if present
    private <R> Validation<R> validationAtName(Validation<R> refined) {
        return name.fold(
            () -> refined,
            refined::at
        );
    }
}
