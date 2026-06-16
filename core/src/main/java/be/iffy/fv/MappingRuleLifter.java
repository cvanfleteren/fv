package be.iffy.fv;

import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;

import java.util.Optional;
import java.util.function.Function;

import static be.iffy.fv.MappingRule.of;

/**
 * Public only because of necessity, is meant to be used from {@link MappingRule#lift()}
 */
public class MappingRuleLifter<T, R> extends Lifter<T,R> {

    private final MappingRule<T, R> rule;

    MappingRuleLifter(MappingRule<T, R> rule) {
        this.rule = rule;
    }

    @Override
    public Validation<R> test(T value) {
        return rule.apply(value);
    }

    /**
     * Lifts a {@link MappingRule} so it applies to a {@link List} of T instead of a single T.
     * If the List is empty, the List is considered valid.
     */
    public MappingRule<List<T>, List<R>> toVavrList() {
        return MappingRule.of(super.toVavrList());
    }

    /**
     * Lifts a {@link MappingRule} so it applies to a {@link java.util.List} of T instead of a single T.
     * If the List is empty, the List is considered valid.
     */
    public MappingRule<java.util.List<T>, java.util.List<R>> toList() {
        return of(super.toList());
    }

    /**
     * Lifts the current mapping rule to operate on the content of {@link Option} containers.
     * Empty Options (None) are considered to be valid.
     */
    public MappingRule<Option<T>, Option<R>> toOption() {
        return of(super.toOption());
    }

    /**
     * Lifts the current mapping rule to operate on the content of {@link Optional} containers.
     * Empty Optionals are considered to be valid.
     */
    public MappingRule<Optional<T>, Optional<R>> toOptional() {
        return of(super.toOptional());
    }

    /**
     * Lifts this {@link MappingRule} so it applies to a {@link Map} of K to T.
     * <p>
     * Be careful, the key {@code key.toString()} will be used as part of the path segment.
     * Make sure to have a key that has a meaningful string representation for this.
     * If you can't guarantee this, use the version of {@link #toVavrMap(Function)} that takes a keyExtractor function instead.
     * <p>
     * Semantics:
     * - Each value in the map is validated, and the resulting validations are collected.
     * - If any validation fails, the entire map is considered invalid.
     * - If all validations pass, the map is considered valid.
     */
    public <K> MappingRule<Map<K, T>, Map<K, R>> toVavrMap() {
        return of(super.toVavrMap());
    }

    /**
     * Lifts this {@link MappingRule} so it applies to a {@link Map} of K to T.
     * <p>
     * Behaves the same as {@link #toVavrMap()} ()}, but uses the keyExtractor function to generate the path segment.
     * <p>
     * Semantics:
     * - If the Map is empty, the map is considered valid.
     * - Each value in the map is validated, and the resulting validations are collected.
     * - If any validation fails, the entire map is considered invalid.
     * - If all validations pass, the map is considered valid.
     *
     * @param keyExtractor the function to extract a path segment from the key.
     */
    public <K> MappingRule<Map<K, T>, Map<K, R>> toVavrMap(Function<K, Object> keyExtractor) {
        return of(super.toVavrMap(keyExtractor));
    }

    /**
     * Lifts this {@link MappingRule} so it applies to a {@link java.util.Map} of K to T.
     * <p>
     * Be careful, the key {@code key.toString()} will be used as part of the path segment.
     * Make sure to have a key that has a meaningful string representation for this.
     * If you can't guarantee this, use the version of {@link #toMap(Function)} that takes a keyExtractor function instead.
     * <p>
     * Semantics:
     * - Each value in the map is validated, and the resulting validations are collected.
     * - If any validation fails, the entire map is considered invalid.
     * - If all validations pass, the map is considered valid.
     */
    public <K> MappingRule<java.util.Map<K, T>, java.util.Map<K, R>>toMap() {
        return of(super.toMap());
    }

    /**
     * Lifts this {@link MappingRule} so it applies to a {@link java.util.Map} of K to T.
     * <p>
     * Behaves the same as {@link #toMap()}, but uses the keyExtractor function to generate the path segment.
     * <p>
     * Semantics:
     * - If the Map is empty, the map is considered valid.
     * - Each value in the map is validated, and the resulting validations are collected.
     * - If any validation fails, the entire map is considered invalid.
     * - If all validations pass, the map is considered valid.
     *
     * @param keyExtractor the function to extract a path segment from the key.
     */
    public <K> MappingRule<java.util.Map<K, T>, java.util.Map<K, R>> toMap(Function<K, Object> keyExtractor) {
        return of(super.toMap(keyExtractor));
    }

}
