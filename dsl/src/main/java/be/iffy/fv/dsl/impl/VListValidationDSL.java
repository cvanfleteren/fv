package be.iffy.fv.dsl.impl;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import io.vavr.collection.List;

import java.util.Objects;
import java.util.function.Function;

/**
 * DSL class for validating a vavr List, allowing you to easily express rules on the list itself
 * and on the elements within the list.
 */
public class VListValidationDSL<L, E> {
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
        return Validation
                .mapN(listValidation.at(name), elementValidation.at(name), (list, elements) -> elements)
                // make errors unique because something like a null list would appear in both validations
                .mapErrors(List::distinct);
    }

    public <Z> VListValidationDSL<Z, Z> eachMapsTo(MappingRule<E, Z> rule) {
        Validation<List<Z>> newElements = elementValidation.refine(list -> rule.liftToVavrList().test(list));
        Validation<List<Z>> newList = listValidation.flatMap(ignore -> newElements);
        return new VListValidationDSL<>(
                newList,
                newElements,
                name
        );
    }

    public <Z> VListValidationDSL<Z, Z> each(Function<E, Validation<Z>> rule) {
        return eachMapsTo(MappingRule.of(rule));
    }

    public VListValidationDSL<E, E> eachIs(Rule<? super E> rule) {
        Rule<E> e = Rule.narrow(rule);
        return eachMapsTo(e);
    }

    /**
     * Validates that the list satisfies the given rule.
     * This method is non-short-circuiting and will collect errors even if the list is already invalid.
     *
     * @param rule the rule for the list.
     * @return a {@link VListValidationDSL} for chaining.
     */
    public VListValidationDSL<L, E> satisfies(Rule<List<L>> rule) {
        Validation<List<L>> ruleValidation = Validation.narrowSuper(listValidation.refine(rule));
        return new VListValidationDSL<>(ruleValidation, elementValidation, name);
    }
}
