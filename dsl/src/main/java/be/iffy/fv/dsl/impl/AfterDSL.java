package be.iffy.fv.dsl.impl;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Transformation;
import be.iffy.fv.Validation;
import org.jetbrains.annotations.Contract;

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
 *
 * The main entry point <em>after</em> is defined in {@link be.iffy.fv.dsl.DSL#after(Transformation)}
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
     */
    @Contract(pure = true)
    public Rule<T> is(Rule<T> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return input -> {
            T transformed = transformer.apply(input);
            return rule.apply(transformed);
        };
    }

    /**
     * Creates a mapping rule that applies the transformation to the input before applying the provided rule function.
     */
    @Contract(pure = true)
    public <R> MappingRule<T, R> is(Function<? super T, ? extends Validation<R>> ruleFunction) {
        Objects.requireNonNull(ruleFunction, "ruleFunction cannot be null");
        return input -> {
            T transformed = transformer.apply(input);
            return ruleFunction.apply(transformed);
        };
    }
}
