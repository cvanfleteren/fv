package net.vanfleteren.fv.rules;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;

import java.util.Objects;

public class ObjectRules {

    public static final Rule<Object> notNull = Rule.of(Objects::nonNull, "cannot.be.null");

    public static Rule<Object> equalTo(Object value) {
        Objects.requireNonNull(value, "value cannot be null");
        return Rule.of(o -> Objects.equals(o, value), "must.be.equal");
    }

    public static Rule<Object> notEqualTo(Object value) {
        Objects.requireNonNull(value, "value cannot be null");
        return Rule.of(o -> !Objects.equals(o, value), "must.not.be.equal");
    }

    public static Rule<Object> oneOf(Object... values) {
        Set<Object> set = HashSet.of(values);
        return Rule.of(set::contains, ErrorMessage.of("must.be.one.of", HashMap.of("values", set)));
    }

    public static Rule<Object> notOneOf(Object... values) {
        Set<Object> set = HashSet.of(values);
        return Rule.of(o -> !set.contains(o), ErrorMessage.of("must.not.be.one.of", HashMap.of("values", set)));
    }

    public static <T> Rule<Object> instanceOf(Class<T> clazz) {
      return Rule.of(clazz::isInstance, ErrorMessage.of("must.be.instance", HashMap.of("of", clazz)));
    }

}
