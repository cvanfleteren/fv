package be.iffy.fv.dsl.impl;

import be.iffy.fv.MappingRules;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import be.iffy.fv.Validations;
import io.vavr.collection.List;

import java.util.Objects;
import java.util.function.Function;

/**
 * DSL class for validating a vavr List, allowing you to easily express rules on the list itself
 * and on the elements within the list.
 */
public final class VListValidationDSL<L, E> {
    private final Validation<List<L>> listValidation;
    private final Validation<List<E>> elementValidation;
    private final String name;

    public VListValidationDSL(List<L> value, String name) {
        this(
                Rule.<List<L>>notNull().test(value),
                //E and L start out the same
                Rule.<List<E>>notNull().test((List<E>) value),
                name
        );
    }

    private VListValidationDSL(Validation<List<L>> listValidation, Validation<List<E>> elementValidation, String name) {
        this.listValidation = Objects.requireNonNull(listValidation, "validation cannot be null");
        this.elementValidation = Objects.requireNonNull(elementValidation, "validation cannot be null");
        this.name = name;
    }

    public Validation<List<E>> validate() {
        return Validations
                .combine(listValidation.at(name), elementValidation.at(name)).map((list, elements) -> elements)
                // make errors unique because something like a null list would appear in both validations
                .mapErrors(List::distinct);
    }

    public <R> VListValidationDSL<R, R> eachIs(Function<E, Validation<R>> rule) {
        Validation<List<R>> newElements = elementValidation.refine(list -> MappingRules.fromValidation(rule).liftToVavrList().test(list));
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
     *
     * @param rule the rule for the list.
     */
    public VListValidationDSL<L, E> is(Function<List<L>, Validation<List<L>>> rule) {
        Validation<List<L>> ruleValidation = Validation.narrowSuper(listValidation.refine(MappingRules.fromValidation(rule)));
        return new VListValidationDSL<>(ruleValidation, elementValidation, name);
    }
}
