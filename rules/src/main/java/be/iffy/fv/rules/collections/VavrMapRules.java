package be.iffy.fv.rules.collections;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Validation rules for {@link Map} values.
 */
public final class VavrMapRules {

    /**
     * Singleton instance of {@link VavrMapRules}.
     */
    public static final VavrMapRules vavrMaps = new VavrMapRules();

    /**
     * Fails if the map is not empty.
     * <p>
     * Error key: {@code must.be.empty}
     */
    public <K, V> Rule<Map<K, V>> empty() {
        return Rule.of((Map<K, V> map) -> {
            if (map.isEmpty()) {
                return Validation.valid(map);
            } else {
                return Validation.invalid(ErrorMessage.of("must.be.empty"));
            }
        });
    }

    /**
     * Fails if the map is {@code null} or empty.
     * <p>
     * Error key: {@code must.not.be.empty}
     */
    public <K, V> Rule<Map<K, V>> notEmpty() {
        return Rule.of((Map<K,V> map) -> {
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
     */
    public <K, V> Rule<Map<K,V>> sizeBetween(int min, int max) {
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
     */
    public <K,V> Rule<Map<K,V>> containsKey(K key) {
        return Rule.of((Map<K,V> map) -> {
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
     */
    public <K,V> Rule<Map<K,V>> containsKeys(K... keys) {
        Set<K> keySet = HashSet.of(keys);
        return Rule.of((Map<K,V> map) -> {
            if (map.keySet().containsAll(keySet)) {
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
     */
    public <K,V> Rule<Map<K,V>> doesNotContainKey(K key) {
        return Rule.of((Map<K,V> map) -> {
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
     */
    public <K,V> Rule<Map<K,V>> doesNotContainKeys(K... keys) {
        Set<K> keySet = HashSet.of(keys);
        return Rule.of((Map<K,V> map) -> {
            if (map.keySet().exists(keySet::contains)) {
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
    public <K,V> Rule<Map<K,V>> valuesNotNull() {
        return Rule.of((Map<K,V> map) -> {
            Set<K> keysWithNulls = map.filter((key, value) -> value == null).keySet();
            if (keysWithNulls.nonEmpty()) {
                return Validation.invalid(ErrorMessage.of("must.not.contain.null.values", "keys", keysWithNulls));
            } else {
                return Validation.valid(map);
            }
        });
    }

    /**
     * Fails if not all values match the given predicate.
     * <p>
     * Error key: {@code must.all.match}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code predicate}: the predicate used for validation</li>
     * </ul>
     */
    public <K, V> Rule<Map<K, V>> allMatch(Predicate<V> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return allMatch(predicate, ErrorMessage.of("must.all.match"));
    }

    /**
     * Fails if not all values match the given predicate.
     */
    public <K, V> Rule<Map<K, V>> allMatch(Predicate<V> predicate, ErrorMessage errorMessage) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return Rule.of(
                (Map<K, V> map) -> map.values().forAll(v -> v != null && predicate.test(v)),
                errorMessage
        );
    }

    /**
     * Fails if not all values pass the passed {@link Rule}.
     * <p>
     * Error key: {@code must.all.match}
     */
    public <K, V> Rule<Map<K, V>> allMatchRule(Rule<V> rule) {
        return allMatchRule(rule, ErrorMessage.of("must.all.match"));
    }

    /**
     * Fails if not all values pass the passed {@link Rule}.
     *
     * @param rule the rule to validate all map values against.
     * @return a {@link Rule} that validates all values against the rule.
     */
    public <K, V> Rule<Map<K, V>> allMatchRule(Rule<V> rule, ErrorMessage errorMessage) {
        return allMatch(rule.toPredicate(), errorMessage);
    }

    /**
     * Fails if any value matches the passed {@link Rule}.
     * <p>
     * Error key: {@code must.none.match}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code rule}: the rule that must not match any value</li>
     * </ul>
     */
    public <K, V> Rule<Map<K, V>> noneMatchRule(Rule<V> rule) {
        return noneMatchRule(rule, ErrorMessage.of("must.none.match"));
    }

    /**
     * Fails if any value matches the passed {@link Rule}.
     */
    public <K, V> Rule<Map<K, V>> noneMatchRule(Rule<V> rule, ErrorMessage errorMessage) {
        return noneMatch(rule.toPredicate(), errorMessage);
    }

    /**
     * Fails if any value matches the given predicate.
     * <p>
     * Error key: {@code must.none.match}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code predicate}: the predicate to validate against</li>
     * </ul>
     */
    public <K, V> Rule<Map<K, V>> noneMatch(Predicate<V> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return noneMatch(predicate, ErrorMessage.of("must.none.match"));
    }

    /**
     * Fails if any value matches the given predicate.
     */
    public <K, V> Rule<Map<K, V>> noneMatch(Predicate<V> predicate, ErrorMessage errorMessage) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return Rule.of(
                (Map<K, V> map) -> !map.values().exists(v -> v != null && predicate.test(v)),
                errorMessage
        );
    }

    /**
     * Fails if no value matches the given predicate.
     * <p>
     * Error key: {@code must.at.least.one.match}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code predicate}: the predicate to validate against</li>
     * </ul>
     */
    public <K, V> Rule<Map<K, V>> anyMatch(Predicate<V> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return anyMatch(predicate, ErrorMessage.of("must.at.least.one.match"));
    }

    /**
     * Fails if no value matches the given predicate.
     */
    public <K, V> Rule<Map<K, V>> anyMatch(Predicate<V> predicate, ErrorMessage errorMessage) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return Rule.of(
                (Map<K, V> map) -> map.values().exists(v -> v != null && predicate.test(v)),
                errorMessage
        );
    }

    /**
     * Fails if not all values pass the {@code rule}.
     */
    public <K,V> Rule<Map<K,V>> validateValuesWith(Rule<? super V> rule) {
        return Rule.of((Map<K,V> map) -> {
            Rule<V> castedRule = (Rule<V>) rule;
            Rule<Map<K, V>> rule2 = castedRule.lift().toVavrMap();
            return rule2.apply(map);
        });
    }

}
