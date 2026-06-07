package be.iffy.fv.rules.collections;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import io.vavr.Function1;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Validation rules for {@link Collection} values.
 */
public class CollectionRules {

    private static class InnerRules<T>extends BaseCollectionRules<T, Collection<T>>  {

        @SuppressWarnings("rawtypes")
        private static final InnerRules INSTANCE = new CollectionRules.InnerRules();

        @SuppressWarnings("unchecked")
        public static <T> CollectionRules.InnerRules<T> inner() {
            return INSTANCE;
        }

        @Override
        protected int getSize(Collection<T> c) {
            return c.size();
        }

        @Override
        protected boolean isEmpty(Collection<T> ts) {
            return ts.isEmpty();
        }

        @Override
        protected boolean contains(Collection<T> ts, T t) {
            return ts.contains(t);
        }
    }

    public static final CollectionRules collections = new CollectionRules();

    /**
     * Fails if the collection is null or empty.
     * <p>
     * Error key: {@code must.not.be.empty}
     */
    public <T> Rule<Collection<T>> notEmpty() {
        return CollectionRules.InnerRules.<T>inner().notEmpty();
    }

    /**
     * Fails if the collection is not empty.
     * <p>
     * Error key: {@code must.be.empty}
     */
    public <T> Rule<Collection<T>> empty() {
        return CollectionRules.InnerRules.<T>inner().empty();
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
    public <T> Rule<Collection<T>> minSize(int size) {
        return CollectionRules.InnerRules.<T>inner().minSize(size);
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
    public <T> Rule<Collection<T>> maxSize(int size) {
        return CollectionRules.InnerRules.<T>inner().maxSize(size);
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
    public <T> Rule<Collection<T>> sizeEquals(int size) {
        return CollectionRules.InnerRules.<T>inner().sizeEquals(size);
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
    public <T> Rule<Collection<T>> sizeBetween(int min, int max) {
        return CollectionRules.InnerRules.<T>inner().sizeBetween(min, max);
    }

    /**
     * Fails when the collection contains null elements.
     * <p>
     * Error key: {@code must.not.be.null} (applied to elements)
     */
    public <T> Rule<Collection<T>> noNullElements() {
        return CollectionRules.InnerRules.<T>inner().noNullElements();
    }

    /**
     * Fails if any element in the collection does not match the given predicate.
     * <p>
     * Error key: {@code must.all.match}
     */
    public <T> Rule<Collection<T>> allMatch(Predicate<T> predicate) {
        return CollectionRules.InnerRules.<T>inner().allMatch(predicate);
    }

    /**
     * Fails if any element in the collection does not match the given predicate.
     */
    public <T> Rule<Collection<T>> allMatch(Predicate<T> predicate, ErrorMessage errorMessage) {
        return CollectionRules.InnerRules.<T>inner().allMatch(predicate, errorMessage);
    }

    /**
     * Fails if one of the elements in the list does not match the passed {@link Rule}.
     */
    public <T> Rule<Collection<T>> allMatchRule(Rule<T> rule) {
        return CollectionRules.InnerRules.<T>inner().allMatchRule(rule);
    }

    /**
     * Fails if any element in the collection matches the given {@link Rule}.
     * <p>
     * Error key: {@code must.none.match}
     */
    public <T> Rule<Collection<T>> noneMatchRule(Rule<T> rule) {
        return CollectionRules.InnerRules.<T>inner().noneMatchRule(rule);
    }

    /**
     * Fails if any element in the collection matches the given predicate.
     * <p>
     * Error key: {@code must.none.match}
     */
    public <T> Rule<Collection<T>> noneMatch(Predicate<T> predicate) {
        return CollectionRules.InnerRules.<T>inner().noneMatch(predicate);
    }

    /**
     * Fails if any element in the collection matches the given predicate.
     */
    public <T> Rule<Collection<T>> noneMatch(Predicate<T> predicate, ErrorMessage errorMessage) {
        return CollectionRules.InnerRules.<T>inner().noneMatch(predicate, errorMessage);
    }

    /**
     * Fails if no elements in the collection match the given predicate.
     * <p>
     * Error key: {@code must.at.least.one.match}
     */
    public <T> Rule<Collection<T>> anyMatch(Predicate<T> predicate) {
        return CollectionRules.InnerRules.<T>inner().anyMatch(predicate);
    }

    /**
     * Fails if no elements in the collection match the given predicate.
     */
    public <T> Rule<Collection<T>> anyMatch(Predicate<T> predicate, ErrorMessage errorMessage) {
        return CollectionRules.InnerRules.<T>inner().anyMatch(predicate, errorMessage);
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
    public <T> Rule<Collection<T>> contains(T element) {
        return CollectionRules.InnerRules.<T>inner().contains(element);
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
    public <T> Rule<Collection<T>> containsAll(Iterable<? extends T> required) {
        return CollectionRules.InnerRules.<T>inner().containsAll(required);
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
    public <T> Rule<Collection<T>> containsAnyOf(Iterable<? extends T> candidates) {
        return CollectionRules.InnerRules.<T>inner().containsAnyOf(candidates);
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
    public <T, K> Rule<Collection<T>> uniqueBy(Function1<T, K> keyExtractor, String key) {
        return CollectionRules.InnerRules.<T>inner().uniqueBy(keyExtractor, key);
    }

    /**
     * Creates a rule that validates that all values in a list satisfy a given rule.
     * The individual {@link ErrorMessage}s are passed to the final Validation.
     */
    public <T> Rule<Collection<T>> validateValuesWith(Rule<? super T> rule) {
        return CollectionRules.InnerRules.<T>inner().validateValuesWith(rule);
    }

}
