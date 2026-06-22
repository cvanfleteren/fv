package be.iffy.fv;

import org.jetbrains.annotations.Contract;

import java.util.Arrays;
import java.util.Objects;

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

    @Contract(pure = true)
    T apply(T value);

    /**
     * Apply another {@link Transformation} after this {@link Transformation}
     */
    @Contract(pure = true)
    default Transformation<T> andThen(Transformation<T> after) {
        Objects.requireNonNull(after);
        return input -> {
            T first = this.apply(input);
            return after.apply(first);
        };
    }

    /**
     * Combines a sequence of {@link Transformation} objects into a single {@link Transformation}.
     * The resulting {@link Transformation} applies the transformations sequentially
     * using the {@link Transformation#andThen(Transformation)} method.
     */
    @SafeVarargs
    @Contract(pure = true)
    static <T> Transformation<T> sequence(Transformation<T> first, Transformation<T>... rest) {
        Objects.requireNonNull(first);
        Objects.requireNonNull(rest);
        Arrays.stream(rest).forEach(Objects::requireNonNull);

        //combine all transformations using andThen
        return Arrays.stream(rest).reduce(
            first,
            Transformation::andThen
        );
    }

}
