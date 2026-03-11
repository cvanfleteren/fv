package net.vanfleteren.fv.rules;

import io.vavr.control.Try;
import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.MappingRule;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.Validation;

import java.util.Objects;

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
     *
     * @return the {@link ObjectRules} instance.
     */
    public static ObjectRules objects() {
        return objects;
    }

    /**
     * Fails if the object is {@code null}.
     * <p>
     * Error key: {@code cannot.be.null}
     *
     * @param <T> the type of the object.
     * @return a {@link Rule} checking for non-null values.
     */
    public <T> Rule<T> notNull() {
        return MappingRule.<T>notNull()::test;
    }

    /**
     * Fails if the input string is not a valid enum value for the given enum class.
     * <p>
     * Error key: {@code invalid.enum.value}
     *
     * @param <E> the type of the enum.
     * @return a {@link MappingRule} checking for valid enum values.
     */
    public <E extends Enum<E>> MappingRule<String, E> isEnum(Class<E> clazz) {
        return input -> Try.of(() -> Enum.valueOf(clazz, input))
                .fold(
                        f -> Validation.invalid(ErrorMessage.of("invalid.enum.value", "value", input)),
                        Validation::valid
                );
    }

}
