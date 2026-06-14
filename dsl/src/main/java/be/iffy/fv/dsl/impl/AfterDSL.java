package be.iffy.fv.dsl.impl;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Transformation;
import be.iffy.fv.Validation;

import java.util.Objects;
import java.util.function.Function;


/**
 * A tiny DSL for helping to define Rules that transform their input before running their actual logic on it.
 * Usage would look like this:
 * <pre>
 *{@code
 * ...
 * Rule<String> originalRule = after(StringOps.trim()).is(strings.length(5));
 * ...
 * }
 * </pre>
 */
public final class AfterDSL<T> {
    private final Transformation<T> transformer;

    public AfterDSL(Transformation<T> transformer) {
        Objects.requireNonNull(transformer, "transformer cannot be null");
        this.transformer = in -> {
            if (in == null) {
                return null;
            } else {
                return transformer.apply(in);
            }
        };
    }

    /**
     * Creates a rule that applies the transformation to the input before evaluating it against the specified rule.
     *
     * @param rule the rule to apply to the transformed input
     */
    public Rule<T> is(Rule<T> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return input -> {
            T transformed = transformer.apply(input);
            return rule.apply(transformed);
        };
    }

    /**
     * Creates a mapping rule that applies the transformation to the input before applying the provided rule function.
     *
     * @param ruleFunction the function to apply to the transformed input
     */
    public <R> MappingRule<T, R> is(Function<? super T, ? extends Validation<R>> ruleFunction) {
        Objects.requireNonNull(ruleFunction, "ruleFunction cannot be null");
        return input -> {
            T transformed = transformer.apply(input);
            return ruleFunction.apply(transformed);
        };
    }
}
