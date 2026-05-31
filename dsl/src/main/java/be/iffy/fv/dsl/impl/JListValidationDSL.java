package be.iffy.fv.dsl;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * DSL class for validating a vavr List, allowing you to easily express rules on the list itself
 * and on the elements within the list.
 */
public class JListValidationDSL<L, E> {
    private final Validation<List<L>> listValidation;
    private final Validation<List<E>> elementValidation;
    private final String name;

    public JListValidationDSL(List<L> value, String name) {
        this.listValidation = Rule.<List<L>>notNull().test(value);
        this.elementValidation = listValidation.mapTo((List<E>) value);
        this.name = name;
    }

    private JListValidationDSL(Validation<List<L>> listValidation, Validation<List<E>> elementValidation, String name) {
        this.listValidation = Objects.requireNonNull(listValidation, "validation cannot be null");
        this.elementValidation = Objects.requireNonNull(elementValidation, "validation cannot be null");
        this.name = name;
    }

    public Validation<List<E>> validate() {
        return Validation
                .mapN(listValidation.at(name), elementValidation.at(name), (list, elements) -> elements)
                // make errors unique because something like a null list would appear in both validations
                .mapErrors(io.vavr.collection.List::distinct);
    }

    public <Z> JListValidationDSL<Z, Z> eachMapsTo(MappingRule<E, Z> rule) {
        Validation<List<Z>> newElements = elementValidation.refine(list -> rule.liftToList().test(list));
        Validation<List<Z>> newList = listValidation.flatMap(ignore -> newElements);
        return new JListValidationDSL<>(
                newList,
                newElements,
                name
        );
    }

    public <Z> JListValidationDSL<Z, Z> each(Function<E, Validation<Z>> rule) {
        return eachMapsTo(MappingRule.of(rule));
    }

    public JListValidationDSL<E, E> eachIs(Rule<E> rule) {
        return eachMapsTo(rule);
    }

    /**
     * Validates that the list satisfies the given rule.
     */
    public JListValidationDSL<L, E> satisfies(Rule<List<L>> rule) {
        Validation<List<L>> ruleValidation = Validation.narrowSuper(listValidation.refine(rule));
        return new JListValidationDSL<>(ruleValidation, elementValidation, name);
    }
}
