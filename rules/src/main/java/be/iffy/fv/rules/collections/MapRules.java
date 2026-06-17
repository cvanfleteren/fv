package be.iffy.fv.rules.collections;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
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
        return Rule.notNull().and(map -> {
            if (map.isEmpty()) {
                return Validation.invalid(ErrorMessage.of("must.not.be.empty"));
            } else {
                return Validation.valid(map);
            }
        });
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
        return Rule.notNull().and(map -> {
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
        return Rule.notNull().and(map -> {
            if (map.keySet().containsAll(keySet.toJavaSet())) {
                return Validation.valid(map);
            } else {
                return Validation.invalid(ErrorMessage.of("must.contain.keys", "keys", keySet));
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
        return Rule.notNull().and(map -> {
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
        return Rule.notNull().and(map -> {
            Rule<V> castedRule = rule.narrow();
            Rule<Map<K, V>> rule2 = castedRule.lift().toMap();
            return rule2.apply(map);
        });
    }

}
