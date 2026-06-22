package be.iffy.fv.jakarta;

import be.iffy.fv.Rule;

/** Sentinel used as the default value of {@link FvRule#provider()} — never instantiated. */
abstract class NoneRuleProvider implements RuleProvider<Object> {
    private NoneRuleProvider() {}

    @Override
    public Rule<Object> provide() {
        throw new UnsupportedOperationException();
    }
}
