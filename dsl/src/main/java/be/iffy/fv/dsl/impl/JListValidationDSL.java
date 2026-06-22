package be.iffy.fv.dsl.impl;

import be.iffy.fv.*;
import io.vavr.control.Option;
import org.jetbrains.annotations.Contract;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * DSL class for validating a vavr List, allowing you to easily express rules on the list itself
 * and on the elements within the list.
 */
public final class JListValidationDSL<L, E> {
    private final Validation<List<L>> listValidation;
    private final Validation<List<E>> elementValidation;
    private final Option<String> name;

    @SuppressWarnings("unchecked")
    public JListValidationDSL(List<L> value, String name) {
        this(
            Validation.fromNullable(value),
            //E and L start out the same
            Validation.fromNullable((List<E>) value),
            Option.of(name)
        );
    }

    private JListValidationDSL(Validation<List<L>> listValidation, Validation<List<E>> elementValidation, Option<String> name) {
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
    public <R> JListValidationDSL<R, R> eachIs(RuleLike<E, Validation<R>> rule) {
        Validation<List<R>> newElements = elementValidation.refine(list -> MappingRule.of(rule).lift().toList().apply(list));
        Validation<List<R>> newList = listValidation.flatMap(ignore -> newElements);
        return new JListValidationDSL<>(
                newList,
                newElements,
                name
        );
    }

    /**
     * Validates that the list satisfies the given rule.
     */
    @Contract(pure = true)
    public JListValidationDSL<L, E> is(Rule<List<L>> rule) {
        Validation<List<L>> ruleValidation = Validation.narrowSuper(listValidation.refine(rule));
        return new JListValidationDSL<>(ruleValidation, elementValidation, name);
    }

    // adds the name to the Validation if present
    private <R> Validation<R> validationAtName(Validation<R> refined) {
        return name.fold(
            () -> refined,
            refined::at
        );
    }
}
