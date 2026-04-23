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
 * @param <T> the type of objects.
 */
public interface IObjectRules<T> {

    /**
     * Fails if the object is not equal to the specified value.
     * <p>
     * Error key: {@code must.be.equal}
     *
     * @param value the required value.
     * @return a {@link Rule} checking for equality.
     */
    default Rule<T> equalTo(T value) {
        Objects.requireNonNull(value, "value cannot be null");
        return Rule.notNull().and(Rule.of(o -> Objects.equals(o, value), "must.be.equal"));
    }

    /**
     * Fails if the object is equal to the specified value.
     * <p>
     * Error key: {@code must.not.be.equal}
     *
     * @param value the forbidden value.
     * @return a {@link Rule} checking for inequality.
     */
    default Rule<T> notEqualTo(T value) {
        Objects.requireNonNull(value, "value cannot be null");
        return Rule.notNull().and(Rule.of(o -> !Objects.equals(o, value), "must.not.be.equal"));
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
     * @return a {@link Rule} checking if the value is one of the allowed values.
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
     * @return a {@link Rule} checking if the value is one of the allowed values.
     */
    default Rule<T> oneOf(Set<T> values) {
        return Rule.notNull().and(Rule.of(values::contains, ErrorMessage.of("must.be.one.of", HashMap.of("values", values))));
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
     *
     * @param values the forbidden values.
     * @return a {@link Rule} checking if the value is not one of the forbidden values.
     */
    default Rule<T> notOneOf(Set<T> values) {
        return Rule.notNull().and(Rule.of(o -> !values.contains(o), ErrorMessage.of("must.not.be.one.of", HashMap.of("values", values))));
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
     *
     * @param <U>   the type of the class.
     * @param clazz the required class.
     * @return a {@link Rule} checking the object's type.
     */
    default <U> MappingRule<Object, U> instanceOf(Class<U> clazz) {
        return input -> MappingRule.notNull().test(input).flatMap(i -> {
            if (clazz.isInstance(i)) {
                return Validation.valid(clazz.cast(i));
            } else {
                return Validation.invalid(ErrorMessage.of("must.be.instance", HashMap.of("of", clazz)));
            }
        });
    }

}
