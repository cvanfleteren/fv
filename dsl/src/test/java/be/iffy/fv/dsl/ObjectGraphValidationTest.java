package be.iffy.fv.dsl;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Validation;
import be.iffy.fv.Validations;
import be.iffy.fv.rules.collections.VavrListRules;
import io.vavr.collection.List;
import org.junit.jupiter.api.Test;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static be.iffy.fv.dsl.DSL.*;
import static org.assertj.core.api.Assertions.assertThat;

class ObjectGraphValidationTest {

    // --- The Domain Records (The "Proper" objects) ---

    record Email(String value) {
    }

    record User(Username username, Email email, Address address, List<Role> roles) {
    }

    record Address(String street, String city, String zipCode) {
    }

    enum Role {
        USER, ADMIN
    }

    record Username(String value) {
        Username {
            asserting(
                    validateThat(value, "value").is(strings.minLength(3))
            );
        }
    }

    // --- The DTOs (The "Raw" input) ---

    static class UserDTO {
        String username;
        String email;
        AddressDTO address;
        List<String> roles;

        UserDTO(String username, String email, AddressDTO address, List<String> roles) {
            this.username = username;
            this.email = email;
            this.address = address;
            this.roles = roles;
        }
    }

    static class AddressDTO {
        String street;
        String city;
        String zipCode;

        AddressDTO(String street, String city, String zipCode) {
            this.street = street;
            this.city = city;
            this.zipCode = zipCode;
        }
    }

    // --- The Rules / Validators ---

    static class UserValidator {
        private static final VavrListRules collections = VavrListRules.vavrLists;

        static Validation<Address> validateAddress(AddressDTO addressDTO) {

            return validateThat(addressDTO).isNotNull().flatMap(dto -> Validations.combine(
                    validateThat(dto.street, "street").is(strings.minLength(1)),
                    validateThat(dto.city, "city").is(strings.minLength(1)),
                    validateThat(dto.zipCode, "zipCode").is(strings.minLength(4))
            ).map(Address::new));
        }

        static Validation<User> fromDto(UserDTO dto) {

            MappingRule<String, Role> canBeRole = strings.asEnum(Role.class);
            MappingRule<String, Email> canBeEmail = strings.minLength(2).and(strings.contains("@")).then(MappingRule.catching(Email::new, "must.be.email"));

            return Validations.combine(
                    validateThat(dto.username, "username").is(objects.construct(Username::new, "must.be.username")),
                    validateThat(dto.email, "email").is(canBeEmail),
                    validateAddress(dto.address).at("address"),
                    validateThatList(dto.roles, "roles").is(collections.notEmpty()).eachIs(canBeRole).validate()
            ).map(User::new);
        }
    }

    @Test
    void validateUser_whenInputIsValid_returnsProperRecord() {
        // Arrange
        AddressDTO addressDto = new AddressDTO("Main St 1", "Brussels", "1000");
        UserDTO userDto = new UserDTO("jdoe", "john.doe@example.com", addressDto, List.of("USER", "ADMIN"));

        // Act
        Validation<User> result = UserValidator.fromDto(userDto);

        // Assert
        assertThatValidation(result).isValid();
        User user = result.getOrElseThrow();
        assertThat(user.username().value()).isEqualTo("jdoe");
        assertThat(user.email().value()).isEqualTo("john.doe@example.com");
        assertThat(user.address().city()).isEqualTo("Brussels");
        assertThat(user.roles()).containsExactly(Role.USER, Role.ADMIN);
    }

    @Test
    void validateUser_whenInputIsInvalid_collectsAllErrorsWithPaths() {
        // Arrange
        // Username too short, email invalid, address missing street, zip too short, roles empty
        AddressDTO addressDto = new AddressDTO("", "Brussels", "123");
        UserDTO userDto = new UserDTO("jd", "invalid-email", addressDto, List.empty());

        // Act
        Validation<User> result = UserValidator.fromDto(userDto);

        // Assert
        assertThatValidation(result)
                .isInvalid()
                .hasErrorMessages(
                        "username.value.must.have.min.length",
                        "email.must.contain",
                        "address.street.must.have.min.length",
                        "address.zipCode.must.have.min.length",
                        "roles.must.not.be.empty"
                );
    }

    @Test
    void validateUser_whenRolesIsInvalid_collectsAllErrorsWithPaths() {
        // Arrange
        AddressDTO addressDto = new AddressDTO("street", "Brussels", "1021");
        UserDTO userDto = new UserDTO("jaydee", "foo@bar.com", addressDto, List.of("A", "BB"));

        // Act
        Validation<User> result = UserValidator.fromDto(userDto);

        // Assert
        assertThatValidation(result)
                .isInvalid()
                .hasErrorMessages(
                        "roles[0].must.be.valid.enum.value",
                        "roles[1].must.be.valid.enum.value"
                );
    }

    @Test
    void validateUser_whenAddressIsNull_returnsAddressNullError() {
        // Arrange
        UserDTO userDto = new UserDTO("jdoe", "john.doe@example.com", null, List.of("USER"));

        // Act
        Validation<User> result = UserValidator.fromDto(userDto);

        // Assert
        assertThatValidation(result)
                .isInvalid()
                .hasErrorMessage("address.must.not.be.null");
    }

}
