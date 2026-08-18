package be.iffy.fv.jakarta;

import be.iffy.fv.Rule;

/**
 * A factory that supplies a {@link Rule} for use with {@link FvRule}.
 *
 * <p>Use this when the class that holds your validation logic doesn't implement {@link Rule}
 * directly - for example, when it acts as a namespace for multiple rules, or when it needs
 * additional setup that doesn't belong in the rule itself:
 *
 * <pre>{@code
 * @FvRule(Person.Rules.class)
 * record Person(String name, int age) {
 *
 *     public static class Rules implements RuleProvider<Person> {
 *         private static final Rule<Person> IMPL = Rule.all(
 *             strings.minLength(2).on(Person::name),
 *             ints.atLeast(18).on(Person::age)
 *         );
 *
 *         @Override public Rule<Person> provide() { return IMPL; }
 *     }
 * }
 * }</pre>
 *
 * <p>The provider class must have a public no-arg constructor so the BV runtime can instantiate it.
 */
@FunctionalInterface
public interface RuleProvider<T> {

    Rule<T> provide();

}
