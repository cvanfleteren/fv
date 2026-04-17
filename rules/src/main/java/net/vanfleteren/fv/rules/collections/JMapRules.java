package net.vanfleteren.fv.rules.collections;

import io.vavr.collection.HashSet;
import io.vavr.collection.Set;
import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.Validation;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Validation rules for {@link java.util.Map} values.
 */
public class JMapRules {

    /**
     * Singleton instance of {@link JMapRules}.
     */
    public static final JMapRules jMaps = new JMapRules();

    /**
     * Returns the singleton instance of {@link JMapRules}.
     */
    public static JMapRules jMaps() {
        return jMaps;
    }

    /**
     * Fails if the map is {@code null} or empty.
     * <p>
     * Error key: {@code must.not.be.empty}
     *
     * @param <K> the type of keys in the map.
     * @param <V> the type of values in the map.
     * @return a {@link Rule} checking if the map is not empty.
     */
    public static <K, V> Rule<Map<K, V>> notEmpty() {
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
     * @param <K> the type of keys in the map.
     * @param <V> the type of values in the map.
     * @param key the required key.
     * @return a {@link Rule} checking for the presence of the key.
     */
    public static <K, V> Rule<Map<K, V>> containsKey(K key) {
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
     * @param <K>  the type of keys in the map.
     * @param <V>  the type of values in the map.
     * @param keys the required keys.
     * @return a {@link Rule} checking for the presence of all specified keys.
     */
    @SafeVarargs
    public static <K, V> Rule<Map<K, V>> containsKeys(K... keys) {
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
     *
     * @param <K> the type of keys in the map.
     * @param <V> the type of values in the map.
     * @return a {@link Rule} checking that all values in the map are non-null.
     */
    public static <K, V> Rule<Map<K, V>> valuesNotNull() {
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
     * <p>
     * Usage example:
     * {@snippet file = "net/vanfleteren/fv/rules/collections/JMapRulesSnippets.java" region = "validate-values-with-example"}
     *
     * @param <K>  the type of keys in the map.
     * @param <V>  the type of values in the map.
     * @param rule the rule to apply to each value.
     */
    public static <K, V> Rule<Map<K, V>> validateValuesWith(Rule<? super V> rule) {
        return Rule.notNull().and(map -> {
            Rule<V> castedRule = (Rule<V>) rule;
            Rule<Map<K, V>> rule2 = castedRule.liftToJMap();
            return rule2.test(map);
        });
    }

}
