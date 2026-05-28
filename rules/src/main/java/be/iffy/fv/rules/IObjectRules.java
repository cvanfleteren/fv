package be.iffy.fv.rules;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import be.iffy.fv.ErrorMessage;
import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;

import java.util.Objects;

/**
 * Common validation rules for any {@link Object}.
 *
 */
public interface IObjectRules<T> {

    /**
     * Fails if the object is not equal to the specified value.
     * <p>
     * Error key: {@code must.be.equal}
     *
     * @param value the required value.
     */
    default Rule<T> equalTo(T value) {
        Objects.requireNonNull(value, "value cannot be null");
        return Rule.of(
                o -> Objects.equals(o, value),
                "must.be.equal"
        );
    }

    /**
     * Fails if the object is equal to the specified value.
     * <p>
     * Error key: {@code must.not.be.equal}
     *
     * @param value the forbidden value.
     */
    default Rule<T> notEqualTo(T value) {
        Objects.requireNonNull(value, "value cannot be null");
        return Rule.of(
                o -> !Objects.equals(o, value),
                "must.not.be.equal"
        );
    }

    /**
     * Fails if the object is not one of the specified values.
     * <p>
     * Error key: {@code must.be.one.of}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code values}: the set of allowed values ({@link Set})</li>
     * </ul>
     *
     * @param values the allowed values.
     */
    default Rule<T> oneOf(T... values) {
        return oneOf(HashSet.of(values));
    }

    /**
     * Fails if the object is not one of the specified values.
     * <p>
     * Error key: {@code must.be.one.of}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code values}: the set of allowed values ({@link Set})</li>
     * </ul>
     *
     * @param values the allowed values.
     */
    default Rule<T> oneOf(Set<T> values) {
        return Rule.of(
                values::contains,
                ErrorMessage.of("must.be.one.of", HashMap.of("values", values))
        );
    }

    /**
     * Fails if the object is one of the specified values.
     * <p>
     * Error key: {@code must.not.be.one.of}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code values}: the set of forbidden values ({@link Set})</li>
     * </ul>
     *
     * @param values the forbidden values.
     */
    default Rule<T> notOneOf(T... values) {
        return notOneOf(HashSet.of(values));
    }

    /**
     * Fails if the object is one of the specified values.
     * <p>
     * Error key: {@code must.not.be.one.of}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code values}: the set of forbidden values ({@link Set})</li>
     * </ul>
     */
    default Rule<T> notOneOf(Set<T> values) {
        return Rule.of(
                o -> !values.contains(o),
                ErrorMessage.of("must.not.be.one.of", HashMap.of("values", values))
        );
    }

    /**
     * Fails if the object is not an instance of the specified class.
     * <p>
     * Error key: {@code must.be.instance}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code of}: the required class ({@link Class})</li>
     * </ul>
     */
    default <U> MappingRule<Object, U> instanceOf(Class<U> clazz) {
        return input -> {
            if (clazz.isInstance(input)) {
                return Validation.valid(clazz.cast(input));
            } else if (input == null) {
                return Validation.invalid("must.not.be.null");
            } else {
                return Validation.invalid(ErrorMessage.of("must.be.instance", HashMap.of("of", clazz)));
            }
        };
    }

}
