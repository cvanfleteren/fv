package net.vanfleteren.fv.rules;

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
     *
     * @return the {@link JCollectionRules} instance.
     */
    public static JCollectionRules jCollections() {
        return jCollections;
    }

    /**
     * Fails if the collection is null or empty.
     * <p>
     * Error key: {@code cannot.be.empty}
     */
    public Rule<Collection<?>> notEmpty = Rule.of(value -> value != null && !value.isEmpty(), "cannot.be.empty");

    /**
     * Fails if the collection size is less than the specified minimum.
     * <p>
     * Error key: {@code min.size}
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
        return Rule.of(value -> value.size() >= size, ErrorMessage.of("min.size", "min", size));
    }

    /**
     * Fails if the collection size is greater than the specified maximum.
     * <p>
     * Error key: {@code max.size}
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
        return Rule.of(value -> value.size() <= size, ErrorMessage.of("max.size", "max", size));
    }

    /**
     * Fails if the collection size is not equal to the specified size.
     * <p>
     * Error key: {@code size.exact}
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
        return Rule.of(value -> value.size() == size, ErrorMessage.of("size.exact", "equal", size));
    }

    /**
     * Fails if the collection size is not between the specified bounds (inclusive).
     * <p>
     * Error key: {@code size.between}
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
        return Rule.of(
                value -> {
                    int size = value.size();
                    return size >= min && size <= max;
                },
                ErrorMessage.of("size.between", HashMap.of("min", min, "max", max))
        );
    }

    /**
     * Checks if the collection contains no null elements.
     * <p>
     * Error key: {@code value.cannot.be.null} (applied to elements)
     *
     * @param <T> the type of elements in the collection.
     * @return a {@link Rule} that validates if the collection has no null elements.
     */
    public <T> Rule<List<T>> noNullElements() {
        return validateValuesWith(objects.notNull());
    }

    /**
     * Checks if all elements in the collection match the given predicate.
     * <p>
     * Error key: {@code all.should.match}
     *
     * @param <T> the type of elements in the collection.
     * @param predicate the predicate to test each element against.
     * @return a {@link Rule} that validates if all elements match the predicate.
     */
    public <T> Rule<List<T>> allMatch(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return allMatch(predicate, ErrorMessage.of("all.should.match"));
    }

    /**
     * Checks if all elements in the collection match the given predicate.
     *
     * @param <T> the type of elements in the collection.
     * @param predicate the predicate to test each element against.
     * @param errorMessage the error message to use if validation fails.
     * @return a {@link Rule} that validates if all elements match the predicate.
     */
    public <T> Rule<List<T>> allMatch(Predicate<T> predicate, ErrorMessage errorMessage) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return value -> validateValuesWith(Rule.of(predicate, errorMessage)).test(value);
    }

    /**
     * Checks if none of the elements in the collection match the given predicate.
     * <p>
     * Error key: {@code none.should.match}
     *
     * @param <T> the type of elements in the collection.
     * @param predicate the predicate to test each element against.
     * @return a {@link Rule} that validates if none of the elements match the predicate.
     */
    public <T> Rule<List<T>> noneMatch(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return noneMatch(predicate, ErrorMessage.of("none.should.match"));
    }

    /**
     * Checks if none of the elements in the collection match the given predicate.
     *
     * @param <T> the type of elements in the collection.
     * @param predicate the predicate to test each element against.
     * @param errorMessage the error message to use if validation fails.
     * @return a {@link Rule} that validates if none of the elements match the predicate.
     */
    public <T> Rule<List<T>> noneMatch(Predicate<T> predicate, ErrorMessage errorMessage) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return value -> validateValuesWith(Rule.of(predicate.negate(), errorMessage)).test(value);
    }

    /**
     * Checks if at least one of the elements in the collection match the given predicate.
     * <p>
     * Error key: {@code atleast.one.should.match}
     *
     * @param <T> the type of elements in the collection.
     * @param predicate the predicate to test each element against.
     * @return a {@link Rule} that validates if at least one of the elements match the predicate.
     */
    public <T> Rule<List<T>> anyMatch(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return anyMatch(predicate, ErrorMessage.of("atleast.one.should.match"));
    }

    /**
     * Checks if at least one of the elements in the collection match the given predicate.
     *
     * @param <T> the type of elements in the collection.
     * @param predicate the predicate to test each element against.
     * @param errorMessage the error message to use if validation fails.
     * @return a {@link Rule} that validates if at least one of the elements match the predicate.
     */
    public <T> Rule<List<T>> anyMatch(Predicate<T> predicate, ErrorMessage errorMessage) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return value -> value.stream().anyMatch(predicate) ? Validation.valid(value) : Validation.invalid(errorMessage);
    }

    /**
     * Checks if the collection contains the given element.
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
        return Rule.of(
                values -> values.contains(element),
                ErrorMessage.of("must.contain", HashMap.of("element", element))
        );
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

        return Rule.of(
                values -> values.containsAll(requiredSet.toJavaSet()),
                ErrorMessage.of("must.contain.all", HashMap.of("required", requiredSet))
        );
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

        return Rule.of(
                values -> {
                    if (candidateSet.isEmpty()) {
                        return false;
                    }
                    return values.stream().anyMatch(candidateSet::contains);
                },
                ErrorMessage.of("must.contain.any.of", HashMap.of("candidates", candidateSet))
        );
    }

    /**
     * Ensures the extracted key is unique within the collection.
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
     * @return a {@link Rule} checking for uniqueness by key.
     */
    public <T, K> Rule<Collection<T>> uniqueBy(Function1<T, K> keyExtractor, String key) {
        Objects.requireNonNull(keyExtractor, "keyExtractor cannot be null");
        Objects.requireNonNull(key, "key cannot be null");

        return values -> {
            Objects.requireNonNull(values, "values cannot be null");

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
                            (acc1, acc2) -> { throw new UnsupportedOperationException("Parallel stream not supported"); }
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
        };
    }

    /**
     * Creates a rule that validates that all values in a list satisfy a given rule.
     * The individual {@link ErrorMessage}s are passed to the final Validation.
     *
     * @param <T> the type of elements in the collection.
     * @param rule the rule to apply to each element.
     * @return a {@link Rule} that applies the given rule to all elements in the list.
     */
    public <T> Rule<List<T>> validateValuesWith(Rule<? super T> rule) {
        return list -> {
            var validations = IntStream.range(0, list.size()).mapToObj(idx ->
                    rule.test(list.get(idx))
                            .mapErrors(errors ->
                                    errors.map(e -> e.atIndex(idx))
                            )
            ).toList();

            var invalidValidations = validations.stream().filter(v -> !v.isValid()).toList();
            if (!invalidValidations.isEmpty()) {
                return Validation.invalid(io.vavr.collection.List.ofAll(invalidValidations).flatMap(Validation::errors));
            } else {
                return Validation.valid(list);
            }
        };
    }

}
