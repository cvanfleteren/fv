package be.iffy.fv.jakarta.support;

import be.iffy.fv.Rule;
import be.iffy.fv.jakarta.FvRule;
import be.iffy.fv.jakarta.RuleProvider;

import static be.iffy.fv.dsl.DSL.*;

/**
 * Test model using @FvRule with a RuleProvider: the Rules class implements RuleProvider, not Rule.
 */
@FvRule(Widget.Rules.class)
public record Widget(String name, int weight) {

    public static class Rules implements RuleProvider<Widget> {
        private static final Rule<Widget> IMPL = Rule.all(
            strings.minLength(3).on(Widget::name),
            ints.atLeast(1).on(Widget::weight)
        );

        @Override
        public Rule<Widget> provide() {
            return IMPL;
        }
    }
}
