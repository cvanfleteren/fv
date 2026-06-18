package be.iffy.fv.dsl.impl;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Validation;
import be.iffy.fv.Validations;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.jetbrains.annotations.Contract;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * DSL class for validating a vavr List, allowing you to easily express rules on the list itself
 * and on the elements within the list.
 */
public final class VListValidationDSL<L, E> {
    private final Validation<List<L>> listValidation;
    private final Validation<List<E>> elementValidation;
    private final Option<String> name;

    @SuppressWarnings("unchecked")
    public VListValidationDSL(List<L> value, String name) {
        this(
                Validation.fromNullable(value),
                //E and L start out the same
                Validation.fromNullable((List<E>) value),
            Option.of(name)
        );
    }

    private VListValidationDSL(Validation<List<L>> listValidation, Validation<List<E>> elementValidation, Option<String> name) {
        this.listValidation = Objects.requireNonNull(listValidation, "validation cannot be null");
        this.elementValidation = Objects.requireNonNull(elementValidation, "validation cannot be null");
        this.name = name.filter(Predicate.not(String::isBlank));
    }

    /**
     * Completes the validation process and returns the final {@link Validation} result.
     * This combines both the list-level validations and the element-level validations.
     */
    @Contract(pure = true)
    public Validation<List<E>> validate() {
        return Validations
                .combine(
                    validationAtName(listValidation),
                    validationAtName(elementValidation)
                ).map((list, elements) -> elements);
    }

    /**
     * Applies the given validation rule to each element in the list.
     */
    @Contract(pure = true)
    public <R> VListValidationDSL<R, R> eachIs(Function<E, Validation<R>> rule) {
        Validation<List<R>> newElements = elementValidation.refine(list -> MappingRule.of(rule).lift().toVavrList().apply(list));
        Validation<List<R>> newList = listValidation.flatMap(ignore -> newElements);
        return new VListValidationDSL<>(
                newList,
                newElements,
                name
        );
    }

    /**
     * Validates that the list satisfies the given rule.
     * This method is non-short-circuiting and will collect errors even if the list is already invalid.
     */
    @Contract(pure = true)
    public VListValidationDSL<L, E> is(Function<List<L>, Validation<List<L>>> rule) {
        Validation<List<L>> ruleValidation = Validation.narrowSuper(listValidation.refine(MappingRule.of(rule)));
        return new VListValidationDSL<>(ruleValidation, elementValidation, name);
    }

    // adds the name to the Validation if present
    private <R> Validation<R> validationAtName(Validation<R> refined) {
        return name.fold(
            () -> refined,
            refined::at
        );
    }
}
