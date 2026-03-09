package net.vanfleteren.fv.rules;

import io.vavr.collection.List;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.Validation;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.API.*;
import static net.vanfleteren.fv.assertj.ValidationAssert.assertThatValidation;
import static net.vanfleteren.fv.rules.CollectionRules.collections;
import static net.vanfleteren.fv.rules.StringRules.strings;

public class ListValidationDslTest {

    @Test
    void validateList_whenMultipleRulesFail_shouldCollectAllErrors() {
        // Arrange
        List<String> roles = List.of("A"); // too short (minLength 2) AND list too short (minSize 2)
        
        // Act
        Validation<List<String>> result = validateThatList(roles, "roles")
                .satisfying(collections().minSize(2))
                .each(strings().minLength(2))
                .mapsTo(s -> s);

        // Assert
        assertThatValidation(result)
                .isInvalid()
                .hasErrorMessages(
                        "roles.min.size",
                        "roles[0].min.length"
                );
    }

    @Test
    void validateList_whenUsingCompactIs_shouldCollectAllErrors() {
        // Arrange
        List<String> roles = List.of("A");

        // Act
        Validation<List<Role>> result = validateThatList(roles, "roles")
                .is(collections().minSize(2), strings().minLength(2), Role::new);

        // Assert
        assertThatValidation(result)
                .isInvalid()
                .hasErrorMessages(
                        "roles.min.size",
                        "roles[0].min.length"
                );
    }

    record Role(String value) {}
}
