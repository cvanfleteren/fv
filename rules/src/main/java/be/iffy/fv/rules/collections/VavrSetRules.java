package be.iffy.fv.rules.collections;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Set;

import java.util.function.Predicate;

/**
 * Important note: if your set doesn't have a fixed iteration order (e.g., is not a {@link io.vavr.collection.SortedSet} or a {@link io.vavr.collection.LinkedHashSet}),
 * the index generated in the errror messages for invalids is not deterministic. Validating the same Set multiple times might lead to
 * different error messages with different indexes.
 */
public class VavrSetRules {

    private static class InnerRules<T> extends BaseCollectionRules<T, Set<T>> {

        @SuppressWarnings("rawtypes")
        private static final InnerRules INSTANCE = new InnerRules();

        @SuppressWarnings("unchecked")
        public static <T> InnerRules<T> inner() {
            return INSTANCE;
        }

        @Override
        protected int getSize(Set<T> c) {
            return c.size();
        }

        @Override
        protected boolean isEmpty(Set<T> ts) {
            return ts.isEmpty();
        }

        @Override
        protected boolean contains(Set<T> ts, T t) {
            return ts.contains(t);
        }

        @Override
        public Rule<Set<T>> validateValuesWith(Rule<? super T> rule) {
            return Rule.notNull().and(set -> {
                Rule<T> castedRule = rule.narrow();

                List<ErrorMessage> allErrors = set
                        .toList()
                        .map(castedRule::test)
                        .zipWithIndex((validation, index) ->
                                validation.mapErrors(errors -> errors.map(e -> e.atIndex(index)))
                        ).flatMap(Validation::errors);

                if(allErrors.isEmpty()) {
                    return Validation.valid(set);
                } else {
                    return Validation.invalid(allErrors);
                }
            });
        }
    }

    public static final VavrSetRules vavrSets = new VavrSetRules();

    /**
     * Fails if the collection is null or empty.
     * <p>
     * Error key: {@code must.not.be.empty}
     */
    public <T> Rule<Set<T>> notEmpty() {
       return InnerRules.<T>inner().notEmpty();
    }

    /**
     * Fails if the collection is not empty.
     * <p>
     * Error key: {@code must.be.empty}
     */
    public <T> Rule<Set<T>> empty() {
        return InnerRules.<T>inner().empty();
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
    public <T> Rule<Set<T>> minSize(int size) {
        return InnerRules.<T>inner().minSize(size);
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
    public <T> Rule<Set<T>> maxSize(int size) {
        return InnerRules.<T>inner().maxSize(size);
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
    public <T> Rule<Set<T>> sizeEquals(int size) {
        return InnerRules.<T>inner().sizeEquals(size);
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
    public <T> Rule<Set<T>> sizeBetween(int min, int max) {
        return InnerRules.<T>inner().sizeBetween(min, max);
    }

    /**
     * Fails when the collection contains null elements.
     * <p>
     * Error key: {@code must.not.be.null} (applied to elements)
     */
    public <T> Rule<Set<T>> noNullElements() {
        return InnerRules.<T>inner().noNullElements();
    }

    /**
     * Fails if any element in the collection does not match the given predicate.
     * <p>
     * Error key: {@code must.all.match}
     */
    public <T> Rule<Set<T>> allMatch(Predicate<T> predicate) {
        return InnerRules.<T>inner().allMatch(predicate);
    }

    /**
     * Fails if any element in the collection does not match the given predicate.
     */
    public <T> Rule<Set<T>> allMatch(Predicate<T> predicate, ErrorMessage errorMessage) {
        return InnerRules.<T>inner().allMatch(predicate, errorMessage);
    }

    /**
     * Fails if one of the elements in the list does not match the passed {@link Rule}.
     */
    public <T> Rule<Set<T>> allMatchRule(Rule<T> rule) {
        return InnerRules.<T>inner().allMatchRule(rule);
    }

    /**
     * Fails if any element in the collection matches the given {@link Rule}.
     * <p>
     * Error key: {@code must.none.match}
     *
     */
    public <T> Rule<Set<T>> noneMatchRule(Rule<T> rule) {
        return InnerRules.<T>inner().noneMatchRule(rule);
    }

    /**
     * Fails if any element in the collection matches the given predicate.
     * <p>
     * Error key: {@code must.none.match}
     */
    public <T> Rule<Set<T>> noneMatch(Predicate<T> predicate) {
        return InnerRules.<T>inner().noneMatch(predicate);
    }

    /**
     * Fails if any element in the collection matches the given predicate.
     */
    public <T> Rule<Set<T>> noneMatch(Predicate<T> predicate, ErrorMessage errorMessage) {
        return InnerRules.<T>inner().noneMatch(predicate, errorMessage);
    }

    /**
     * Fails if no elements in the collection match the given predicate.
     * <p>
     * Error key: {@code must.at.least.one.match}
     */
    public <T> Rule<Set<T>> anyMatch(Predicate<T> predicate) {
        return InnerRules.<T>inner().anyMatch(predicate);
    }

    /**
     * Fails if no elements in the collection match the given predicate.
     */
    public <T> Rule<Set<T>> anyMatch(Predicate<T> predicate, ErrorMessage errorMessage) {
        return InnerRules.<T>inner().anyMatch(predicate, errorMessage);
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
    public <T> Rule<Set<T>> contains(T element) {
        return InnerRules.<T>inner().contains(element);
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
    public <T> Rule<Set<T>> containsAll(Iterable<? extends T> required) {
        return InnerRules.<T>inner().containsAll(required);
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
    public <T> Rule<Set<T>> containsAnyOf(Iterable<? extends T> candidates) {
        return InnerRules.<T>inner().containsAnyOf(candidates);
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
    public <T, K> Rule<Set<T>> uniqueBy(Function1<T, K> keyExtractor, String key) {
        return InnerRules.<T>inner().uniqueBy(keyExtractor, key);
    }

    /**
     * Creates a rule that validates that all values in a collection satisfy a given rule.
     * The individual {@link ErrorMessage}s are passed to the final Validation.
     */
    public <T> Rule<Set<T>> validateValuesWith(Rule<? super T> rule) {
        return InnerRules.<T>inner().validateValuesWith(rule);
    }

}
