package net.vanfleteren.fv.rules;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;

import java.util.Objects;

public interface IObjectRules<T> {

    default Rule<T> equalTo(T value) {
        Objects.requireNonNull(value, "value cannot be null");
        return Rule.of(o -> Objects.equals(o, value), "must.be.equal");
    }

    default Rule<T> notEqualTo(T value) {
        Objects.requireNonNull(value, "value cannot be null");
        return Rule.of(o -> !Objects.equals(o, value), "must.not.be.equal");
    }

    default Rule<T> oneOf(T... values) {
       return oneOf(HashSet.of(values));
    }

    default Rule<T> oneOf(Set<T> values) {
        return Rule.of(values::contains, ErrorMessage.of("must.be.one.of", HashMap.of("values", values)));
    }

    default Rule<T> notOneOf(T... values) {
      return notOneOf(HashSet.of(values));
    }

    default Rule<T> notOneOf(Set<T> values) {
        return Rule.of(o -> !values.contains(o), ErrorMessage.of("must.not.be.one.of", HashMap.of("values", values)));
    }

    default <T> Rule<Object> instanceOf(Class<T> clazz) {
        return Rule.of(clazz::isInstance, ErrorMessage.of("must.be.instance", HashMap.of("of", clazz)));
    }

}
