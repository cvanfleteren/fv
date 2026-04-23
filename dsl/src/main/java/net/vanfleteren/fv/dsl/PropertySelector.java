package net.vanfleteren.fv.dsl;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Represents a selector for extracting a property value from an object.
 * Expects to be used in the form of a methodReference, like <code>SomeRecord::aField</code> or <code>SomeBean::getProperty</code>
 */
@FunctionalInterface
public interface PropertySelector<T, V> extends Serializable, Function<T, V> {

    V apply(T input);

    default String getPropertyName() {
        return PropertySelectorSupport.getImplMethodName(this);
    }
}
