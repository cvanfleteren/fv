package be.iffy.fv.jakarta.support;

import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import be.iffy.fv.jakarta.FvRule;

import static be.iffy.fv.dsl.DSL.*;

/**
 * Test model: a plain record (no self-validating constructor) annotated with @FvRule.
 */
@FvRule(Person.Validator.class)
public record Person(String name, int age) {

    public static class Validator implements Rule<Person> {
        private static final Rule<Person> IMPL = Rule.all(
            strings.minLength(2).on(Person::name),
            ints.atLeast(18).on(Person::age)
        );

        @Override
        public Validation<Person> apply(Person p) {
            return IMPL.apply(p);
        }
    }
}
