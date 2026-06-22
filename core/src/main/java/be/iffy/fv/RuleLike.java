package be.iffy.fv;

import org.jetbrains.annotations.Contract;

/**
 * Technical interface for handing a Rule, MappingRule, and method references that share the same signature alike.
 *
 */
@FunctionalInterface
public interface RuleLike<T, R> {

    @Contract(pure = true)
    R apply(T input);
}
