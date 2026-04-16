package net.vanfleteren.fv.rules;

import io.vavr.collection.List;
import net.vanfleteren.fv.MappingRule;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.Validation;
import net.vanfleteren.fv.rules.collections.CollectionRules;
import net.vanfleteren.fv.rules.text.StringRules;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.API.*;
import static net.vanfleteren.fv.assertj.ValidationAssert.assertThatValidation;
import static net.vanfleteren.fv.rules.ObjectRules.objects;
import static net.vanfleteren.fv.rules.text.StringRules.strings;
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
        USER,ADMIN
   }

    record Username(String value) {
        Username {
            assertAllValid(
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
        private static final StringRules strings = strings();
        private static final CollectionRules collections = CollectionRules.collections();

        static Validation<Address> validateAddress(AddressDTO addressDTO) {

            return notNull(addressDTO, "address").flatMap(dto -> Validation.mapN(
                    validateThat(dto.street, "street").is(strings.minLength(1)),
                    validateThat(dto.city, "city").is(strings.minLength(1)),
                    validateThat(dto.zipCode, "zipCode").is(strings.minLength(4)),
                    Address::new
            ));
        }

        static Validation<User> fromDto(UserDTO dto) {

            Rule<String> canBeRole = strings.minLength(2);
            MappingRule<String, Email> canBeEmail = strings.minLength(2).and(strings.contains("@")).andThen(MappingRule.of(Email::new, "must.be.email"));

            return Validation.mapN(
                    validateThat(dto.username, "username").mapsTo(Username::new),
                    validateThat(dto.email, "email").is(canBeEmail),
                    validateAddress(dto.address).at("address"),
                    validateThatList(dto.roles, "roles").satisfying(collections.notEmpty()).each(canBeRole).mapsTo(Role::valueOf),
                    User::new
            );
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
                        "roles[0].must.have.min.length"
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
                .hasErrorMessage("address.address.must.not.be.null");
    }
}
