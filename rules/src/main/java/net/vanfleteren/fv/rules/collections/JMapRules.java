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
     *
     * @return the {@link JMapRules} instance.
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
        return map -> {
            if (map == null) return Validation.invalid(ErrorMessage.of("must.not.be.null"));
            if (map.isEmpty()) return Validation.invalid(ErrorMessage.of("must.not.be.empty"));
            return Validation.valid(map);
        };
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
        return map -> {
            if (map == null) return Validation.invalid(ErrorMessage.of("must.not.be.null"));
            if (map.containsKey(key)) return Validation.valid(map);
            return Validation.invalid(ErrorMessage.of("must.contain.key", "key", key));
        };
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
     * @param <K> the type of keys in the map.
     * @param <V> the type of values in the map.
     * @param keys the required keys.
     * @return a {@link Rule} checking for the presence of all specified keys.
     */
    @SafeVarargs
    public static <K, V> Rule<Map<K, V>> containsKeys(K... keys) {
        Set<K> keySet = HashSet.of(keys);
        return map -> {
            if (map == null) return Validation.invalid(ErrorMessage.of("must.not.be.null"));
            if (map.keySet().containsAll(keySet.toJavaSet())) return Validation.valid(map);
            return Validation.invalid(ErrorMessage.of("must.contain.keys", "keys", keySet));
        };
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
        return map -> {
            if (map == null) return Validation.invalid(ErrorMessage.of("must.not.be.null"));
            Set<K> keysWithNulls = HashSet.ofAll(map.entrySet().stream()
                    .filter(entry -> entry.getValue() == null)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList()));
            if (keysWithNulls.nonEmpty()) {
                return Validation.invalid(ErrorMessage.of("must.not.contain.null.values", "keys", keysWithNulls));
            } else {
                return Validation.valid(map);
            }
        };
    }

    /**
     * Validates all values in the map using the specified rule.
     * <p>
     * If any value fails the rule, the resulting validation will contain errors with paths
     * corresponding to the keys of the failing values.
     *
     * @param <K>  the type of keys in the map.
     * @param <V>  the type of values in the map.
     * @param rule the rule to apply to each value.
     * @return a {@link Rule} that validates all map values.
     */
    public static <K, V> Rule<Map<K, V>> validateValuesWith(Rule<? super V> rule) {
        return map -> {
            var validations = map.entrySet().stream().map(entry ->
                    rule.test(entry.getValue())
                            .mapErrors(errors ->
                                    errors.map(e -> e.atIndex(Objects.toString(entry.getKey())))
                            )
            ).toList();

            var invalidValidations = validations.stream().filter(v -> !v.isValid()).collect(Collectors.toList());
            if (!invalidValidations.isEmpty()) {
                return Validation.invalid(io.vavr.collection.List.ofAll(invalidValidations).flatMap(Validation::errors));
            } else {
                return Validation.valid(map);
            }
        };
    }

}
