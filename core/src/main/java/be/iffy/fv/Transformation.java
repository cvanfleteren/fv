package be.iffy.fv;

/**
 * A transformation is a function on a type T that is guaranteed to always succeed.
 * Implementations need to be able to handle null values and make sure they don't fail otherwise.
 * It is allowed to return null if receiving a null input. It is discouraged to return null on a non-null input.
 * <p>
 * Think of Transformation as a MappingRule on the same type that never fails, so there is
 * no need to return a Validation.
 * <p>
 * A good example of a Transformation is String.toUpperCase on a non-null String, or most of the commons-lang StringUtils
 * methods.
 * </p>
 */
@FunctionalInterface
public interface Transformation<T> {

    T apply(T value);

}
