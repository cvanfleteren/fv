package net.vanfleteren.fv.rules;

import net.vanfleteren.fv.Rule;

import java.util.Objects;

public class ObjectRules implements IObjectRules<Object> {

    public static final ObjectRules objects = new ObjectRules();

    public static ObjectRules objects() {
        return objects;
    }

    static <T> Rule<T> notNull() {
        return Rule.of(Objects::nonNull, "cannot.be.null");
    }

}
