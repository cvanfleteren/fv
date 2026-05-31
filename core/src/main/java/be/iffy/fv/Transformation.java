package be.iffy.fv;

/**
 * A transformation is a function on a type T that is guaranteed to always succeed.
 * Implementations need to be able to handle null values and make sure they don't fail otherwise.
 * <p>
 * Think of Transformation as a MappingRule on the same type that never fails, so there is
 * no need to return a Validation.
 */
public interface Transformation<T> {

    T apply(T value);

}
