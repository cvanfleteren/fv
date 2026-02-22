package net.vanfleteren.fv;

import net.vanfleteren.fv.assertj.ValidationAssert;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.API.assertAllValid;
import static net.vanfleteren.fv.API.validateThat;
import static org.assertj.core.api.Assertions.assertThat;

public class ClientViewTest {

    static Rule<String> minLength = Rule.of(s -> s.length() > 3, "too.short");
    static Rule<Integer> minAge = Rule.of(i -> i >= 18, "too.young");

    @Test
    void example_with_validation_outside_constructor() {

        record PersonDto(String name, int age) {
        }

        record Person(String name, int age) {
        }

        PersonDto dto = new PersonDto("John", 30);

        Validation<String> nameV = validateThat(dto.name, "name").is(minLength);
        Validation<Integer> ageV = validateThat(dto.age, "age").is(minAge);

        Validation<Person> personV = Validation.mapN(nameV, ageV, Person::new);

        ValidationAssert.assertThatValidation(personV)
                .isValid()
                .hasValue(new Person("John", 30));
    }


    @Test
    void constructorValidation_whenMappingValues_returnsTupleWithValidValues() {

        record Person(String name, int age) {
            Person {
                var values = assertAllValid(
                        validateThat(name, "name").map(String::trim).is(minLength),
                        validateThat(age, "age").is(minAge)
                );
                name = values._1();
            }
        }

        Person person = new Person(" John ", 18);
        // the name was trimmed
        assertThat(person.name).isEqualTo("John");
    }

    @Test
    void constructorValidation_whenRulesFail_returnsInvalidValidationWithFieldErrors() {

        record PersonDto(String name, int age) {
        }

        record Person(String name, int age) {
            Person {
                assertAllValid(
                    validateThat(name, "name").is(minLength),
                    validateThat(age, "age").is(minAge)
                );
            }
        }

        record Couple(Person a, Person b) {
        }

        PersonDto dtoA = new PersonDto("X", 18);
        PersonDto dtoB = new PersonDto("John", 16);

        Validation<Person> personAV = Validation.from(() -> new Person(dtoA.name, dtoA.age)).at("a");
        Validation<Person> personBV = Validation.from(() -> new Person(dtoB.name, dtoB.age)).at("b");

        Validation<Couple> coupleV = Validation.mapN(personAV, personBV, Couple::new);

        ValidationAssert.assertThatValidation(coupleV)
                .isInvalid()
                .hasErrorMessages("a.name.too.short", "b.age.too.young");
    }
}
