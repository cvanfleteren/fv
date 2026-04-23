package net.vanfleteren.fv.rules.collections;

import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.*;
import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.Validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static net.vanfleteren.fv.rules.ObjectRules.objects;

/**
 * Validation rules for {@link Collection} and {@link List} values.
 */
public class JCollectionRules {

    /**
     * Singleton instance of {@link JCollectionRules}.
     */
    public static final JCollectionRules jCollections = new JCollectionRules();

    /**
     * Returns the singleton instance of {@link JCollectionRules}.
     */
    public static JCollectionRules jCollections() {
        return jCollections;
    }

    /**
     * Fails if the collection is null or empty.
     * <p>
     * Error key: {@code must.not.be.empty}
     */
    public Rule<Collection<?>> notEmpty() {
        return Rule.notNull().and(value -> {
            if (!value.isEmpty()) {
                return Validation.valid(value);
            } else {
                return Validation.invalid(ErrorMessage.of("must.not.be.empty"));
            }
        });
    }

    /**
     * Fails if the collection is not empty.
     * <p>
     * Error key: {@code must.be.empty}
     */
    public Rule<Collection<?>> empty() {
        return Rule.notNull().and(value -> {
            if (value.isEmpty()) {
                return Validation.valid(value);
            } else {
                return Validation.invalid(ErrorMessage.of("must.be.empty"));
            }
        });
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
     *
     * @param size the minimum allowed size.
     * @return a {@link Rule} checking the minimum size.
     */
    public Rule<Collection<?>> minSize(int size) {
        return Rule.notNull().and(value -> {
            if (value.size() >= size) {
                return Validation.valid(value);
            } else {
                return Validation.invalid(ErrorMessage.of("must.have.min.size", "min", size));
            }
        });
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
     *
     * @param size the maximum allowed size.
     * @return a {@link Rule} checking the maximum size.
     */
    public Rule<Collection<?>> maxSize(int size) {
        return Rule.notNull().and(value -> {
            if (value.size() <= size) {
                return Validation.valid(value);
            } else {
                return Validation.invalid(ErrorMessage.of("must.have.max.size", "max", size));
            }
        });
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
     *
     * @param size the required size.
     * @return a {@link Rule} checking the exact size.
     */
    public Rule<Collection<?>> sizeEquals(int size) {
        return Rule.notNull().and(value -> {
            if (value.size() == size) {
                return Validation.valid(value);
            } else {
                return Validation.invalid(ErrorMessage.of("must.have.exact.size", "equal", size));
            }
        });
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
     * @return a {@link Rule} checking the size range.
     */
    public Rule<Collection<?>> sizeBetween(int min, int max) {
        return Rule.notNull().and(value -> {
            int size = value.size();
            if (size >= min && size <= max) {
                return Validation.valid(value);
            } else {
                return Validation.invalid(ErrorMessage.of("must.have.size.between", HashMap.of("min", min, "max", max)));
            }
        });
    }

    /**
     * Fails when the collection contains null elements.
     * <p>
     * Error key: {@code must.not.be.null} (applied to elements)
     *
     * @param <T> the type of elements in the collection.
     */
    public <T> Rule<List<T>> noNullElements() {
        return objects.<T>notNull().liftToJList();
    }

    /**
     * Fails if any element in the collection does not match the given predicate.
     * <p>
     * Error key: {@code must.all.match}
     *
     * @param <T> the type of elements in the collection.
     * @param predicate the predicate to test each element against.
     * @return a {@link Rule} that validates if all elements match the predicate.
     */
    public <T> Rule<List<T>> allMatch(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return allMatch(predicate, ErrorMessage.of("must.all.match"));
    }

    /**
     * Fails if any element in the collection does not match the given predicate.
     *
     * @param <T> the type of elements in the collection.
     * @param predicate the predicate to test each element against.
     * @param errorMessage the error message to use if validation fails.
     * @return a {@link Rule} that validates if all elements match the predicate.
     */
    public <T> Rule<List<T>> allMatch(Predicate<T> predicate, ErrorMessage errorMessage) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return Rule.notNull().and(value -> validateValuesWith(Rule.of(predicate, errorMessage)).test(value));
    }

    /**
     * Fails if one of the elements in the list does not match the passed {@link Rule}.
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/rules/collections/JCollectionRulesSnippets.java" region="all-match-rule-example"}
     *
     * @param <T> the type of elements in the collection.
     * @param rule the {@link Rule} to validate each element against.
     */
    public <T> Rule<List<T>> allMatchRule(Rule<T> rule) {
        return Rule.notNull().and(rule.liftToJList());
    }

    /**
     * Fails if any element in the collection matches the given {@link Rule}.
     * <p>
     * Error key: {@code must.none.match}
     *
     * @param <T> the type of elements in the collection.
     * @param rule the Rule to test each element against.
     * @return a {@link Rule} that validates if none of the elements match the {@link Rule}.
     */
    public <T> Rule<List<T>> noneMatchRule(Rule<T> rule) {
        return noneMatch(rule.toPredicate());
    }

    /**
     * Fails if any element in the collection matches the given predicate.
     * <p>
     * Error key: {@code must.none.match}
     *
     * @param <T> the type of elements in the collection.
     * @param predicate the predicate to test each element against.
     * @return a {@link Rule} that validates if none of the elements match the predicate.
     */
    public <T> Rule<List<T>> noneMatch(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return noneMatch(predicate, ErrorMessage.of("must.none.match"));
    }

    /**
     * Fails if any element in the collection matches the given predicate.
     *
     * @param <T> the type of elements in the collection.
     * @param predicate the predicate to test each element against.
     * @param errorMessage the error message to use if validation fails.
     * @return a {@link Rule} that validates if none of the elements match the predicate.
     */
    public <T> Rule<List<T>> noneMatch(Predicate<T> predicate, ErrorMessage errorMessage) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return Rule.notNull().and(value -> validateValuesWith(Rule.of(predicate.negate(), errorMessage)).test(value));
    }

    /**
     * Fails if no elements in the collection match the given predicate.
     * <p>
     * Error key: {@code must.at.least.one.match}
     *
     * @param <T> the type of elements in the collection.
     * @param predicate the predicate to test each element against.
     * @return a {@link Rule} that validates if at least one of the elements match the predicate.
     */
    public <T> Rule<List<T>> anyMatch(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return anyMatch(predicate, ErrorMessage.of("must.at.least.one.match"));
    }

    /**
     * Fails if no elements in the collection match the given predicate.
     *
     * @param <T> the type of elements in the collection.
     * @param predicate the predicate to test each element against.
     * @param errorMessage the error message to use if validation fails.
     * @return a {@link Rule} that validates if at least one of the elements match the predicate.
     */
    public <T> Rule<List<T>> anyMatch(Predicate<T> predicate, ErrorMessage errorMessage) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return Rule.notNull().and(value -> value.stream().anyMatch(predicate) ? Validation.valid(value) : Validation.invalid(errorMessage));
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
     *
     * @param <T> the type of elements in the collection.
     * @param element the element to check for.
     * @return a {@link Rule} that validates if the collection contains the element.
     */
    public <T> Rule<Collection<T>> contains(T element) {
        return Rule.notNull().and(values -> {
            if (values.contains(element)) {
                return Validation.valid(values);
            } else {
                return Validation.invalid(ErrorMessage.of("must.contain", HashMap.of("element", element)));
            }
        });
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
     *
     * @param <T> the type of elements in the collection.
     * @param required the elements that must be present.
     * @return a {@link Rule} checking for all required elements.
     */
    public <T> Rule<Collection<T>> containsAll(Iterable<? extends T> required) {
        Objects.requireNonNull(required, "required cannot be null");

        Set<T> requiredSet = HashSet.ofAll(required);

        return Rule.notNull().and(values -> {
            if (values.containsAll(requiredSet.toJavaSet())) {
                return Validation.valid(values);
            } else {
                return Validation.invalid(ErrorMessage.of("must.contain.all", HashMap.of("required", requiredSet)));
            }
        });
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
     *
     * @param <T> the type of elements in the collection.
     * @param candidates the candidate elements.
     * @return a {@link Rule} checking for any of the candidate elements.
     */
    public <T> Rule<Collection<T>> containsAnyOf(Iterable<? extends T> candidates) {
        Objects.requireNonNull(candidates, "candidates cannot be null");

        Set<T> candidateSet = HashSet.ofAll(candidates);

        return Rule.notNull().and(values -> {
            if (!candidateSet.isEmpty() && values.stream().anyMatch(candidateSet::contains)) {
                return Validation.valid(values);
            } else {
                return Validation.invalid(ErrorMessage.of("must.contain.any.of", HashMap.of("candidates", candidateSet)));
            }
        });
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
     * @param <T> the type of elements in the collection.
     * @param <K> the type of the extracted key.
     * @param keyExtractor the function to extract the unique key.
     * @param key the label for the key (e.g., "email").
     * <p>
     * Usage example:
     * {@snippet file="net/vanfleteren/fv/rules/collections/JCollectionRulesSnippets.java" region="unique-by-example"}
     *
     * @return a {@link Rule} checking for uniqueness by key.
     */
    public <T, K> Rule<Collection<T>> uniqueBy(Function1<T, K> keyExtractor, String key) {
        Objects.requireNonNull(keyExtractor, "keyExtractor cannot be null");
        Objects.requireNonNull(key, "key cannot be null");

        return Rule.notNull().and(values -> {
            java.util.List<T> list = new ArrayList<>(values);

            Tuple2<Map<K, Integer>, Map<K, io.vavr.collection.List<Integer>>> state = IntStream.range(0, list.size())
                    .boxed()
                    .reduce(
                            Tuple.of(HashMap.empty(), HashMap.empty()),
                            (acc, idx) -> {
                                Map<K, Integer> firstIndexByKey = acc._1;
                                Map<K, io.vavr.collection.List<Integer>> duplicateIndicesByKey = acc._2;

                                T value = list.get(idx);
                                K keyValue = keyExtractor.apply(value);

                                if (firstIndexByKey.containsKey(keyValue)) {
                                    int firstIdx = firstIndexByKey.get(keyValue).get();

                                    io.vavr.collection.List<Integer> indices = duplicateIndicesByKey
                                            .get(keyValue)
                                            .getOrElse(io.vavr.collection.List.of(firstIdx))
                                            .append(idx);

                                    return Tuple.of(firstIndexByKey, duplicateIndicesByKey.put(keyValue, indices));
                                } else {
                                    return Tuple.of(firstIndexByKey.put(keyValue, idx), duplicateIndicesByKey);
                                }
                            },
                            (acc1, acc2) -> {
                                throw new UnsupportedOperationException("Parallel stream not supported");
                            }
                    );

            Map<K, io.vavr.collection.List<Integer>> duplicateIndicesByKey = state._2;

            if (duplicateIndicesByKey.isEmpty()) {
                return Validation.valid(values);
            }

            return Validation.invalid(
                    ErrorMessage.of(
                            "must.be.unique.by.key",
                            HashMap.of(
                                    "key", key,
                                    "duplicates", duplicateIndicesByKey
                            )
                    )
            );
        });
    }

    /**
     * Creates a rule that validates that all values in a list satisfy a given rule.
     * The individual {@link ErrorMessage}s are passed to the final Validation.
     *
     * @param <T> the type of elements in the collection.
     * @param rule the rule to apply to each element.
     * @return a {@link Rule} that applies the given rule to all elements in the list.
     * @see Rule#liftToJList()
     */
    public <T> Rule<List<T>> validateValuesWith(Rule<? super T> rule) {
        return Rule.notNull().and(list -> {
            Rule<T> castedRule = Rule.narrow(rule);
            Rule<List<T>> rule2 = castedRule.liftToJList();
            return rule2.test(list);
        });
    }

}
