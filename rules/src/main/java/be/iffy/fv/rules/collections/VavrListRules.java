package be.iffy.fv.rules.collections;

import be.iffy.fv.*;
import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;
import io.vavr.collection.Traversable;

import java.util.function.Predicate;

/**
 * Validation rules for {@link Traversable} and {@link Iterable} collections.
 */
public final class VavrListRules {

    private static class InnerRules<T> extends BaseCollectionRules<T, List<T>>  {

        @SuppressWarnings("rawtypes")
        private static final VavrListRules.InnerRules INSTANCE = new VavrListRules.InnerRules();

        @SuppressWarnings("unchecked")
        public static <T> VavrListRules.InnerRules<T> inner() {
            return INSTANCE;
        }

        @Override
        protected int getSize(List<T> c) {
            return c.size();
        }

        @Override
        protected boolean isEmpty(List<T> ts) {
            return ts.isEmpty();
        }

        @Override
        protected boolean contains(List<T> ts, T t) {
            return ts.contains(t);
        }
    }

    public static final VavrListRules vavrLists = new VavrListRules();

    /**
     * Fails if the collection is null or empty.
     * <p>
     * Error key: {@code must.not.be.empty}
     */
    public <T> Rule<List<T>> notEmpty() {
        return VavrListRules.InnerRules.<T>inner().notEmpty();
    }

    /**
     * Fails if the collection is not empty.
     * <p>
     * Error key: {@code must.be.empty}
     */
    public <T> Rule<List<T>> empty() {
        return VavrListRules.InnerRules.<T>inner().empty();
    }

    /**
     * Fails if the collection size is less than the specified minimum.
     * <p>
     * Error key: {@code must.have.min.size}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code min}: the minimum allowed size ({@code int})</li>
     * </ul>
     */
    public <T> Rule<List<T>> minSize(int size) {
        return VavrListRules.InnerRules.<T>inner().minSize(size);
    }

    /**
     * Fails if the collection size is greater than the specified maximum.
     * <p>
     * Error key: {@code must.have.max.size}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code max}: the maximum allowed size ({@code int})</li>
     * </ul>
     */
    public <T> Rule<List<T>> maxSize(int size) {
        return VavrListRules.InnerRules.<T>inner().maxSize(size);
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
    public <T> Rule<List<T>> sizeEquals(int size) {
        return VavrListRules.InnerRules.<T>inner().sizeEquals(size);
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
    public <T> Rule<List<T>> sizeBetween(int min, int max) {
        return VavrListRules.InnerRules.<T>inner().sizeBetween(min, max);
    }

    /**
     * Fails when the collection contains null elements.
     * <p>
     * Error key: {@code must.not.be.null} (applied to elements)
     */
    public <T> Rule<List<T>> noNullElements() {
        return VavrListRules.InnerRules.<T>inner().noNullElements();
    }

    /**
     * Fails if any element in the collection does not match the given predicate.
     * <p>
     * Error key: {@code must.all.match}
     */
    public <T> Rule<List<T>> allMatch(Predicate<T> predicate) {
        return VavrListRules.InnerRules.<T>inner().allMatch(predicate);
    }

    /**
     * Fails if any element in the collection does not match the given predicate.
     */
    public <T> Rule<List<T>> allMatch(Predicate<T> predicate, ErrorMessage errorMessage) {
        return VavrListRules.InnerRules.<T>inner().allMatch(predicate, errorMessage);
    }

    /**
     * Fails if one of the elements in the list does not match the passed {@link Rule}.
     */
    public <T> Rule<List<T>> allMatchRule(Rule<T> rule) {
        return VavrListRules.InnerRules.<T>inner().allMatchRule(rule);
    }

    /**
     * Fails if any element in the collection matches the given {@link Rule}.
     * <p>
     * Error key: {@code must.none.match}
     *

     * @param rule the Rule to test each element against.
     * @return a {@link Rule} that validates if none of the elements match the {@link Rule}.
     */
    public <T> Rule<List<T>> noneMatchRule(Rule<T> rule) {
        return VavrListRules.InnerRules.<T>inner().noneMatchRule(rule);
    }

    /**
     * Fails if any element in the collection matches the given predicate.
     * <p>
     * Error key: {@code must.none.match}
     */
    public <T> Rule<List<T>> noneMatch(Predicate<T> predicate) {
        return VavrListRules.InnerRules.<T>inner().noneMatch(predicate);
    }

    /**
     * Fails if any element in the collection matches the given predicate.
     */
    public <T> Rule<List<T>> noneMatch(Predicate<T> predicate, ErrorMessage errorMessage) {
        return VavrListRules.InnerRules.<T>inner().noneMatch(predicate, errorMessage);
    }

    /**
     * Fails if no elements in the collection match the given predicate.
     * <p>
     * Error key: {@code must.at.least.one.match}
     */
    public <T> Rule<List<T>> anyMatch(Predicate<T> predicate) {
        return VavrListRules.InnerRules.<T>inner().anyMatch(predicate);
    }

    /**
     * Fails if no elements in the collection match the given predicate.
     */
    public <T> Rule<List<T>> anyMatch(Predicate<T> predicate, ErrorMessage errorMessage) {
        return VavrListRules.InnerRules.<T>inner().anyMatch(predicate, errorMessage);
    }

    /**
     * Fails if the collection does not contain the given element.
     * <p>
     * Error key: {@code must.contain}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code element}: the required element ({@code T})</li>
     * </ul>
     */
    public <T> Rule<List<T>> contains(T element) {
        return VavrListRules.InnerRules.<T>inner().contains(element);
    }

    /**
     * Semantics: every element from {@code required} must appear at least once in {@code values}.
     * Note: duplicates in {@code required} are ignored (treated as a set).
     * <p>
     * Error key: {@code must.contain.all}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code required}: the set of required elements ({@link Set})</li>
     * </ul>
     */
    public <T> Rule<List<T>> containsAll(Iterable<? extends T> required) {
        return VavrListRules.InnerRules.<T>inner().containsAll(required);
    }

    /**
     * Semantics: at least one element from {@code candidates} must appear in {@code values}.
     * If {@code candidates} is empty, the rule fails (there is no acceptable element to match).
     * <p>
     * Error key: {@code must.contain.any.of}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code candidates}: the set of candidate elements ({@link Set})</li>
     * </ul>
     */
    public <T> Rule<List<T>> containsAnyOf(Iterable<? extends T> candidates) {
        return VavrListRules.InnerRules.<T>inner().containsAnyOf(candidates);
    }

    /**
     * Fails if the extracted key is not unique within the collection.
     * <p>
     * Accumulates duplicates and includes the duplicate keys in the error args.
     * <p>
     * Error key: {@code must.be.unique.by.key}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code key}: the key label ({@link String})</li>
     *     <li>{@code duplicates}: the duplicate keys and their indices ({@link Map})</li>
     * </ul>
     *
     * @param keyExtractor the function to extract the unique key, e.g., SomeRecord::email
     * @param key the label for the key (e.g., "email").
     */
    public <T, K> Rule<List<T>> uniqueBy(Function1<T, K> keyExtractor, String key) {
        return VavrListRules.InnerRules.<T>inner().uniqueBy(keyExtractor, key);
    }

    /**
     * Fails if the List contains duplicates.
     * <p>
     * Error key: {@code must.be.unique}
     */
    public <T> Rule<List<T>> allUnique() {
        return VavrListRules.InnerRules.<T>inner().allUnique();
    }

    /**
     * Fails if one or more entries in the collection fail the passed MappingRule.
     * <p>
     * Error key: whatever the key(s) are that the passed mappingRule returns.
     *
     * @see MappingRule#lift()
     */
    public <T, R> MappingRule<List<T>, List<R>> map(RuleLike<T, ? extends Validation<R>> mappingRule) {
        return MappingRule.of(mappingRule).lift().toVavrList();
    }

    /**
     * Creates a rule that validates that all values in a list satisfy a given rule.
     * The individual {@link ErrorMessage}s are passed to the final Validation.
     */
    public <T> Rule<List<T>> validateValuesWith(Rule<? super T> rule) {
        Rule<T> tRule = rule.narrow();
        return tRule.lift().toVavrList();
    }

}
