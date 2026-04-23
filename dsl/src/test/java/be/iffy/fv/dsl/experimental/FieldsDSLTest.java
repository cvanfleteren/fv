package be.iffy.fv.dsl.experimental;

import be.iffy.fv.Rule;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static io.vavr.API.Map;
import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static be.iffy.fv.dsl.experimental.FieldsDSL.ruleFor;

class FieldsDSLTest {

    Rule<String> isLongEnough = Rule.of(s -> s.length() > 3, "too.short");


    @Test
    void fieldsDSL_onlyValidateWhen() {
        record UserDTO(boolean subscribed, String email) {
        }

        Rule<UserDTO> rule = ruleFor(UserDTO.class)
                .when(UserDTO::subscribed)
                .then(b -> b.require(UserDTO::email, isLongEnough, "email"));

        assertThatValidation(rule.test(new UserDTO(true, "foo@bar.com"))).isValid();
        assertThatValidation(rule.test(new UserDTO(false, "bla"))).isValid();

        assertThatValidation(rule.test(new UserDTO(true, "bla"))).isInvalid().hasErrorMessages("email.too.short");
    }

    @Test
    void fieldsDSL_compare() {
        LocalDate from = LocalDate.of(2026, 3, 1);
        LocalDate to = LocalDate.of(2026, 4, 1);

        record UserDTO(LocalDate from, LocalDate to) {
        }

        Rule<UserDTO> rule = ruleFor(UserDTO.class).comparing(UserDTO::from, "from").isLessThan(UserDTO::to, "to");

        assertThatValidation(rule.test(new UserDTO(from, to))).isValid();

        assertThatValidation(rule.test(new UserDTO(to, from))).isInvalid().hasErrorMessage("must.be.less.than", Map("field1", "from", "field2", "to"));
    }

    @Test
    void fieldsDSL_compare_noFieldNames() {
        LocalDate from = LocalDate.of(2026, 3, 1);
        LocalDate to = LocalDate.of(2026, 4, 1);

        record UserDTO(LocalDate from, LocalDate to) {
        }

        Rule<UserDTO> rule = ruleFor(UserDTO.class).comparing(UserDTO::from).isLessThan(UserDTO::to);

        assertThatValidation(rule.test(new UserDTO(from, to))).isValid();

        assertThatValidation(rule.test(new UserDTO(to, from))).isInvalid().hasErrorMessage("must.be.less.than", Map("field1", "from", "field2", "to"));
    }

    @Test
    void fieldsDSL_whenThenCompare() {
        LocalDate from = LocalDate.of(2026, 3, 1);
        LocalDate to = LocalDate.of(2026, 4, 1);

        record UserDTO(boolean member, LocalDate from, LocalDate to) {
        }

        Rule<UserDTO> rule = ruleFor(UserDTO.class).when(UserDTO::member).comparing(UserDTO::from, "from").isLessThan(UserDTO::to, "to");

        assertThatValidation(rule.test(new UserDTO(true, from, to))).isValid();
        assertThatValidation(rule.test(new UserDTO(false, to, from))).isValid();

        assertThatValidation(rule.test(new UserDTO(true, to, from))).isInvalid().hasErrorMessage("must.be.less.than", Map("field1", "from", "field2", "to"));
    }

    @Test
    void fieldsDSL_theField_isEqualTo() {
        record UserDTO(boolean subscribed, String email, String email2) {
        }

        Rule<UserDTO> rule = ruleFor(UserDTO.class)
                .when(UserDTO::subscribed)
                .thenField(UserDTO::email, "email").isEqualTo(UserDTO::email2, "email2");

        assertThatValidation(rule.test(new UserDTO(true, "foo@bar.com", "foo@bar.com"))).isValid();
        assertThatValidation(rule.test(new UserDTO(true, "foo@bar.com2", "foo@bar.com")))
                .isInvalid()
                .hasErrorMessage("fields.must.be.equal", Map("field1", "email", "field2", "email2"));
    }
}