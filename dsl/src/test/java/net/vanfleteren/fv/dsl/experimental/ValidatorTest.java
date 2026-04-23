package net.vanfleteren.fv.dsl.experimental;

import net.vanfleteren.fv.MappingRule;
import net.vanfleteren.fv.Rule;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static net.vanfleteren.fv.assertj.ValidationAssert.assertThatValidation;
import static net.vanfleteren.fv.dsl.experimental.Validator.validatorFor;

class ValidatorTest {

    record User(String firstName, String lastName, int age, String email) {
    }

    record ValidatedUser(String fullName, int age, String email) {
    }

    @Test
    void validatorBuilder4_shouldWorkWithFourRules() {
        MappingRule<String, String> notEmpty = MappingRule.of(s -> {
            if (s == null || s.isEmpty()) throw new IllegalArgumentException();
            return s;
        }, "must.not.be.empty");
        Rule<Integer> positive = Rule.of(i -> i > 0, "must.be.positive");

        MappingRule<User, ValidatedUser> validator = validatorFor(User.class)
                .where(User::firstName, notEmpty)
                .where(User::lastName, notEmpty)
                .where(User::age, positive)
                .constraint(User::email, notEmpty)
                .builds((fn, ln, age, email) -> new ValidatedUser(fn + " " + ln, age, email));

        User validUser = new User("John", "Doe", 30, "john.doe@example.com");
        assertThatValidation(validator.test(validUser)).isValid()
                .hasValue(new ValidatedUser("John Doe", 30, "john.doe@example.com"));

        User invalidUser = new User("", "", -1, "");
        assertThatValidation(validator.test(invalidUser)).isInvalid()
                .hasErrorMessages("firstName.must.not.be.empty", "lastName.must.not.be.empty", "age.must.be.positive", "email.must.not.be.empty");
    }

    @Test
    void validatorBuilder3_shouldHaveConstraintMethods() {
        MappingRule<String, String> notEmpty = MappingRule.of(s -> s, "must.not.be.empty");

        MappingRule<User, String> validator = validatorFor(User.class)
                .where(User::firstName, notEmpty)
                .where(User::lastName, notEmpty)
                .where(User::email, notEmpty)
                .builds((fn, ln, email) -> fn + " " + ln + " <" + email + ">");

        User validUser = new User("John", "Doe", 30, "john@doe.com");
        assertThatValidation(validator.test(validUser)).isValid()
                .hasValue("John Doe <john@doe.com>");
    }

    @Test
    void validator_conditionalAgeValidation() {
        record LocalUser(boolean subscribed, int age, String name) {
        }

        Rule<Integer> ageAtLeast18 = Rule.of(i -> i >= 18, "too.young");

        MappingRule<LocalUser, Integer> adultWhenSubscribed = validatorFor(LocalUser.class)
                .when(LocalUser::subscribed)
                .where(LocalUser::age, ageAtLeast18)
                .builds(age -> age);

        MappingRule<LocalUser, Integer> notSubscribed = validatorFor(LocalUser.class)
                .when(Predicate.not(LocalUser::subscribed))
                .where(LocalUser::age, Rule.ok())
                .builds(age -> age);


        var v = adultWhenSubscribed.orElse(notSubscribed);

        assertThatValidation(v.test(new LocalUser(true, 20, "John"))).isValid().hasValue(20);
        assertThatValidation(v.test(new LocalUser(true, 15, "Young John"))).isInvalid().hasErrorMessage("age.too.young");
        assertThatValidation(v.test(new LocalUser(false, 15, "Young John Unsubscribed"))).isValid().hasValue(15);
    }
}
