package be.iffy.fv;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

/**
 * Technical interface for handing a Rule, MappingRule, and method references that share the same signature alike.
 *
 */
@FunctionalInterface
public interface RuleLike<T, R> {

    @Contract(pure = true)
    R apply(@Nullable T input);
}
