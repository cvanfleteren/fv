package be.iffy.fv.dsl.impl;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Transformation;

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

    public <R> MappingRule<T, R> is(MappingRule<T, R> rule) {
        return input -> {
            T transformed = transformer.apply(input);
            return rule.test(transformed);
        };
    }
}
