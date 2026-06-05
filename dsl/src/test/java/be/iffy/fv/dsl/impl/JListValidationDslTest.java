package be.iffy.fv.dsl.impl;

import be.iffy.fv.Validation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static be.iffy.fv.dsl.DSL.validateThatList;
import static be.iffy.fv.rules.Rules.lists;
import static be.iffy.fv.rules.text.StringRules.strings;

public class JListValidationDslTest {

    @Test
    void validateList_whenMultipleRulesFail_shouldCollectAllErrors() {
        // Arrange
        List<String> roles = List.of("A"); // too short (minLength 2) AND list too short (minSize 2)
        
        // Act
        Validation<List<String>> result = validateThatList(roles, "roles")
                .is(lists.minSize(2))
                .eachIs(strings.minLength(2))
                .validate();

        // Assert
        assertThatValidation(result)
                .isInvalid()
                .hasErrorMessages(
                        "roles.must.have.min.size",
                        "roles[0].must.have.min.length"
                );
    }

    @Test
    void validateList_whenUsingCompactIs_shouldCollectAllErrors() {
        // Arrange
        List<String> roles = List.of("A");

        // Act
        Validation<List<Role>> result = validateThatList(roles, "roles")
                .is(lists.minSize(2))
                .eachIs(strings.minLength(2).map(Role::new))
                .validate();

        // Assert
        assertThatValidation(result)
                .isInvalid()
                .hasErrorMessages(
                        "roles.must.have.min.size",
                        "roles[0].must.have.min.length"
                );
    }

    record Role(String value) {}
}
