package be.iffy.fv.dsl;

import io.vavr.API;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;

import static be.iffy.fv.dsl.DSL.assertAllValid;
import static be.iffy.fv.dsl.DSL.validateThat;
import static be.iffy.fv.dsl.DSLSnippets.ExampleIntRules.ints;
import static be.iffy.fv.dsl.DSLSnippets.ExampleStringRules.strings;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DSLSnippets {

    static class ExampleStringRules {

        public static ExampleStringRules strings() {
            return API.TODO();
        }

        public Rule<String> notEmpty() {
            return API.TODO();
        }

        public Rule<String> minLength(int min) {
            return API.TODO();
        }
    }

    static class ExampleIntRules {

        public static ExampleIntRules ints() {
            return API.TODO();
        }

        public Rule<Integer> min(int min) {
            return API.TODO();
        }
    }


    void assertAllWithTupleExample() {
        // @start region="assert-all-tuple-example"
        record Person(String name, int age) {

            Person {
                // validate and assign results (e.g. if you want to trim the name)
                // will throw ValidationException with all errors if any Validation was invalid.
                var values = assertAllValid(
                        validateThat(name, "name").map(String::trim).is(strings().minLength(3)),
                        validateThat(age, "age").is(ints().min(18))
                );
                name = values._1;
                // name is now the trimmed version
            }
        }

        // name was validated and trimmed
        assertThat(new Person(" John ",57).name()).isEqualTo("John");
        // @end
    }

    void assertAllValidExample() {
        // @start region="assert-all-valid-example"
        record Person(String name, int age) {

            Person {
                assertAllValid(
                        validateThat(name, Person::name).is(strings().notEmpty()),
                        validateThat(age, Person::age).is(ints().min(18))
                );
            }
        }

        // will throw ValidationException with 2 ErrorMessages inside
        Person p = new Person("s", 12);
        // or do this in a Validation, will result in an Invalid<Person> with 2 ErrorMessages
        Validation<Person> v = Validation.from(() -> new Person("s", 12));
        // @end
    }
}
