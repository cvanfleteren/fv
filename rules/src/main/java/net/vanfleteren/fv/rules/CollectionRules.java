package net.vanfleteren.fv.rules;

import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;

import java.util.Collection;

public class CollectionRules {

    public static Rule<Iterable<?>> notEmpty = Rule.of(value -> value.iterator().hasNext(), "cannot.be.empty");

    public static Rule<Collection<?>> minSize(int size) {
        return Rule.of(value -> value.size() >= size, ErrorMessage.of("min.size", "min", size));
    }

    public static Rule<Collection<?>> maxSize(int size) {
        return Rule.of(value -> value.size() <= size, ErrorMessage.of("max.size", "max", size));
    }

    public static Rule<Collection<?>> sizeEquals(int size) {
        return Rule.of(value -> value.size() == size, ErrorMessage.of("equal.size", "equal", size));
    }

    public static Rule<Collection<?>> sizeBetween(int min, int max) {
        return Rule.of(
                value -> {
                    int size = value.size();
                    return size >= min && size <= max;
                },
                ErrorMessage.of("equal.size", HashMap.of("min", min, "max", max))
        );
    }

    public static <T> Rule<List<T>> noNullElements() {
        Rule<T> notNull = ObjectRules.notNull();
        return notNull.liftToList();
    }

}
