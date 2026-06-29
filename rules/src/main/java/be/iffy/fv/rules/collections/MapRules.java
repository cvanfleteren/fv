package be.iffy.fv.rules.collections;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Set;

import java.util.Map;

/**
 * Validation rules for {@link java.util.Map} values.
 */
public final class MapRules {

    /**
     * Singleton instance of {@link MapRules}.
     */
    public static final MapRules maps = new MapRules();

    /**
     * Fails if the map is {@code null} or empty.
     * <p>
     * Error key: {@code must.not.be.empty}
     *
     * @return a {@link Rule} checking if the map is not empty.
     */
    public <K, V> Rule<Map<K, V>> notEmpty() {
        return Rule.of((Map<K, V> map) -> {
            if (map.isEmpty()) {
                return Validation.invalid(ErrorMessage.of("must.not.be.empty"));
            } else {
                return Validation.valid(map);
            }
        });
    }

    /**
     * Fails if the map size is less than the specified minimum.
     * <p>
     * Error key: {@code must.have.min.size}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code min}: the minimum allowed size ({@code int})</li>
     * </ul>
     */
    public <K, V> Rule<Map<K, V>> minSize(int size) {
        return Rule.of(
            input -> input.size() >= size,
            ErrorMessage.of("must.have.min.size", "min", size)
        );
    }

    /**
     * Fails if the map size is greater than the specified maximum.
     * <p>
     * Error key: {@code must.have.max.size}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code max}: the maximum allowed size ({@code int})</li>
     * </ul>
     */
    public <K, V> Rule<Map<K, V>> maxSize(int size) {
        return Rule.of(
            input -> input.size() <= size,
            ErrorMessage.of("must.have.max.size", "max", size)
        );
    }

    /**
     * Fails if the collection size is not equal to the specified size.
     * <p>
     * Error key: {@code must.have.exact.size}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code equal}: the required size ({@code int})</li>
     * </ul>
     */
    public <K, V> Rule<Map<K, V>> sizeEquals(int size) {
        return Rule.of(
            input -> input.size() == size,
            ErrorMessage.of("must.have.exact.size", "equal", size)
        );
    }

    /**
     * Fails if the collection size is not between the specified bounds (inclusive).
     * <p>
     * Error key: {@code must.have.size.between}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code min}: the minimum allowed size ({@code int})</li>
     *     <li>{@code max}: the maximum allowed size ({@code int})</li>
     * </ul>
     *
     * @param min the minimum allowed size (inclusive).
     * @param max the maximum allowed size (inclusive).
     */
    public <K, V> Rule<Map<K, V>> sizeBetween(int min, int max) {
        return Rule.of(
            value -> {
                int size = value.size();
                return (size >= min && size <= max);
            },
            ErrorMessage.of("must.have.size.between", HashMap.of("min", min, "max", max))
        );
    }

    /**
     * Fails if the map does not contain the specified key.
     * <p>
     * Error key: {@code must.contain.key}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code key}: the required key ({@code K})</li>
     * </ul>
     *
     * @param key the required key.
     * @return a {@link Rule} checking for the presence of the key.
     */
    public <K, V> Rule<Map<K, V>> containsKey(K key) {
        return Rule.of((Map<K, V> map) -> {
            if (map.containsKey(key)) {
                return Validation.valid(map);
            } else {
                return Validation.invalid(ErrorMessage.of("must.contain.key", "key", key));
            }
        });
    }

    /**
     * Fails if the map does not contain all the specified keys.
     * <p>
     * Error key: {@code must.contain.keys}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code keys}: the set of required keys ({@link Set})</li>
     * </ul>
     *
     * @param keys the required keys.
     * @return a {@link Rule} checking for the presence of all specified keys.
     */
    public <K, V> Rule<Map<K, V>> containsKeys(K... keys) {
        Set<K> keySet = HashSet.of(keys);
        return Rule.of((Map<K, V> map) -> {
            if (map.keySet().containsAll(keySet.toJavaSet())) {
                return Validation.valid(map);
            } else {
                return Validation.invalid(ErrorMessage.of("must.contain.keys", "keys", keySet));
            }
        });
    }

    /**
     * Fails if the map contains the specified key.
     * <p>
     * Error key: {@code must.not.contain.key}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code key}: the disallowed key ({@code K})</li>
     * </ul>
     *
     * @param key the disallowed key.
     * @return a {@link Rule} checking that the key is absent.
     */
    public <K, V> Rule<Map<K, V>> doesNotContainKey(K key) {
        return Rule.of((Map<K, V> map) -> {
            if (!map.containsKey(key)) {
                return Validation.valid(map);
            } else {
                return Validation.invalid(ErrorMessage.of("must.not.contain.key", "key", key));
            }
        });
    }

    /**
     * Fails if the map contains ANY of the specified keys.
     * <p>
     * Error key: {@code must.not.contain.keys}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code keys}: the set of disallowed keys ({@link Set})</li>
     * </ul>
     *
     * @param keys the disallowed keys.
     * @return a {@link Rule} checking that none of the keys are present.
     */
    public <K, V> Rule<Map<K, V>> doesNotContainKeys(K... keys) {
        Set<K> keySet = HashSet.of(keys);
        return Rule.of((Map<K, V> map) -> {
            if (map.keySet().stream().anyMatch(keySet::contains)) {
                return Validation.invalid(ErrorMessage.of("must.not.contain.keys", "keys", keySet));
            } else {
                return Validation.valid(map);
            }
        });
    }

    /**
     * Fails if the map contains any {@code null} values.
     * <p>
     * Error key: {@code must.not.contain.null.values}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code keys}: the set of keys that have {@code null} values ({@link Set})</li>
     * </ul>
     */
    public <K, V> Rule<Map<K, V>> valuesNotNull() {
        return Rule.of((Map<K, V> map) -> {
            Set<K> keysWithNulls = HashSet.ofAll(
                    map.entrySet()
                            .stream()
                            .filter(entry -> entry.getValue() == null)
                            .map(Map.Entry::getKey)
                            .toList()
            );
            if (keysWithNulls.nonEmpty()) {
                return Validation.invalid(ErrorMessage.of("must.not.contain.null.values", "keys", keysWithNulls));
            } else {
                return Validation.valid(map);
            }
        });
    }

    /**
     * Fails if not all values pass the {@code rule}.
     */
    public <K, V> Rule<Map<K, V>> validateValuesWith(Rule<? super V> rule) {
        return Rule.of((Map<K, V> map) -> {
            Rule<V> castedRule = rule.narrow();
            Rule<Map<K, V>> rule2 = castedRule.lift().toMap();
            return rule2.apply(map);
        });
    }

}
