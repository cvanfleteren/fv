package be.iffy.fv.dsl.impl;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Transformation;
import be.iffy.fv.Validation;

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
public class AfterDSL<T> {
    private final Transformation<T> transformer;

    public AfterDSL(Transformation<T> transformer) {
        this.transformer = in -> {
            if (in == null) {
                return null;
            } else {
                return transformer.apply(in);
            }
        };
    }

    public Rule<T> is(Rule<T> rule) {
        return input -> {
            T transformed = transformer.apply(input);
            return rule.test(transformed);
        };
    }

    public <R> MappingRule<T, R> is(Function<? super T, ? extends Validation<R>> rule) {
        return input -> {
            T transformed = transformer.apply(input);
            return rule.apply(transformed);
        };
    }
}
