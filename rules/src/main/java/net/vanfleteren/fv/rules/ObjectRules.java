package net.vanfleteren.fv.rules;

import net.vanfleteren.fv.Rule;

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
        return Rule.of(Objects::nonNull, "cannot.be.null");
    }

}
