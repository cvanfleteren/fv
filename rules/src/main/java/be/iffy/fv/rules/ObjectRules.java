package be.iffy.fv.rules;

import io.vavr.control.Try;
import be.iffy.fv.ErrorMessage;
import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;

/**
 * Implementation of {@link IObjectRules} for generic {@link Object} validation.
 */
public class ObjectRules implements IObjectRules<Object> {

    /**
     * Singleton instance of {@link ObjectRules}.
     */
    public static final ObjectRules objects = new ObjectRules();

    /**
     * Returns the singleton instance of {@link ObjectRules}.
     */
    public static ObjectRules objects() {
        return objects;
    }

    /**
     * Fails if the object is {@code null}.
     * <p>
     * Error key: {@code must.not.be.null}
     */
    public <T> Rule<T> notNull() {
        return MappingRule.<T>notNull()::test;
    }

    /**
     * Fails if the input string is not a valid enum value for the given enum class while mappping to the enum.
     * <p>
     * Usage example:
     * {@snippet file="be/iffy/fv/rules/ObjectRulesSnippets.java" region="is-enum-example"}
     * <p>
     * Error key: {@code must.be.valid.enum.value}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code value}: the input string ({@link String})</li>
     * </ul>
     */
    public <E extends Enum<E>> MappingRule<String, E> isEnum(Class<E> clazz) {
        return input -> Try.of(() -> Enum.valueOf(clazz, input))
                .fold(
                        f -> Validation.invalid(ErrorMessage.of("must.be.valid.enum.value", "value", input)),
                        Validation::valid
                );
    }


    /**
     * Fails if the input string is not a valid enum value for the given enum class.
     * <p>
     * Error key: {@code must.be.valid.enum.value}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code value}: the input string ({@link String})</li>
     * </ul>
     *
     */
    public <E extends Enum<E>> Rule<String> canBeEnum(Class<E> clazz) {
        return input -> Try.of(() -> Enum.valueOf(clazz, input))
                .fold(
                        f -> Validation.invalid(ErrorMessage.of("must.be.valid.enum.value", "value", input)),
                        v -> Validation.valid(input)
                );
    }

}
