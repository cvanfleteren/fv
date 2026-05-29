package be.iffy.fv.rules.collections;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.*;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static be.iffy.fv.rules.ObjectRules.objects;

/**
 *
 * Abstract class holding the implementations of collection related Rules, written in
 * a collection type (List, Set, vavr List, Collection, ...) agnostic way.
 *
 
 
 */
abstract class BaseCollectionRules<T, C extends Iterable<T>> {

    abstract protected int getSize(C c);

    abstract protected boolean isEmpty(C c);

    abstract protected boolean contains(C c, T t);


    /**
     * Fails if the collection is null or empty.
     * <p>
     * Error key: {@code must.not.be.empty}
     */
    public Rule<C> notEmpty() {
        return Rule.of(
                value -> !isEmpty(value),
                ErrorMessage.of("must.not.be.empty")
        );
    }

    /**
     * Fails if the collection is not empty.
     * <p>
     * Error key: {@code must.be.empty}
     */
    public Rule<C> empty() {
        return Rule.of(
                value -> isEmpty(value),
                ErrorMessage.of("must.be.empty")
        );
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
    public Rule<C> minSize(int size) {
        return Rule.of(
                value -> getSize(value) >= size,
                ErrorMessage.of("must.have.min.size", "min", size)
        );
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
    public Rule<C> maxSize(int size) {
        return Rule.of(
                value -> getSize(value) <= size,
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
     *
     * @param size the required size.
     * @return a {@link Rule} checking the exact size.
     */
    public Rule<C> sizeEquals(int size) {
        return Rule.of(
                value -> getSize(value) == size,
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
     *
     * @param min the minimum allowed size (inclusive).
     * @param max the maximum allowed size (inclusive).
     */
    public Rule<C> sizeBetween(int min, int max) {
        return Rule.of(
                value -> {
                    int size = getSize(value);
                    return (size >= min && size <= max);
                },
                ErrorMessage.of("must.have.size.between", HashMap.of("min", min, "max", max))
        );
    }

    /**
     * Fails when the collection contains null elements.
     * <p>
     * Error key: {@code must.not.be.null} (applied to elements)
     */
    public Rule<C> noNullElements() {
        return validateValuesWith(objects.notNull());
    }

    /**
     * Fails if any element in the collection does not match the given predicate.
     * <p>
     * Error key: {@code must.all.match}
     */
    public Rule<C> allMatch(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return allMatch(predicate, ErrorMessage.of("must.all.match"));
    }

    /**
     * Fails if any element in the collection does not match the given predicate.
     *
     
     * @param predicate    the predicate to test each element against.
     * @param errorMessage the error message to use if validation fails.
     * @return a {@link Rule} that validates if all elements match the predicate.
     */
    public Rule<C> allMatch(Predicate<T> predicate, ErrorMessage errorMessage) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return validateValuesWith(Rule.of(predicate, errorMessage));
    }

    /**
     * Fails if one of the elements in the list does not match the passed {@link Rule}.
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/rules/collections/CollectionRulesSnippets.java" region = "all-match-rule-example"}
     *
     */
    public Rule<C> allMatchRule(Rule<T> rule) {
        return validateValuesWith(rule);
    }

    /**
     * Fails if any element in the collection matches the given {@link Rule}.
     * <p>
     * Error key: {@code must.none.match}
     *
     
     * @param rule the Rule to test each element against.
     * @return a {@link Rule} that validates if none of the elements match the {@link Rule}.
     */
    public Rule<C> noneMatchRule(Rule<T> rule) {
        return noneMatch(rule.toPredicate());
    }

    /**
     * Fails if any element in the collection matches the given predicate.
     * <p>
     * Error key: {@code must.none.match}
     */
    public Rule<C> noneMatch(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return noneMatch(predicate, ErrorMessage.of("must.none.match"));
    }

    /**
     * Fails if any element in the collection matches the given predicate.
     */
    public Rule<C> noneMatch(Predicate<T> predicate, ErrorMessage errorMessage) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return validateValuesWith(Rule.of(predicate.negate(), errorMessage));
    }

    /**
     * Fails if no elements in the collection match the given predicate.
     * <p>
     * Error key: {@code must.at.least.one.match}
     */
    public Rule<C> anyMatch(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return anyMatch(predicate, ErrorMessage.of("must.at.least.one.match"));
    }

    /**
     * Fails if no elements in the collection match the given predicate.
     */
    public Rule<C> anyMatch(Predicate<T> predicate, ErrorMessage errorMessage) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return Rule.notNull().and(value -> Iterator.ofAll(value).exists(predicate) ? Validation.valid(value) : Validation.invalid(errorMessage));
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
     * @param element the element to check for.
     * @return a {@link Rule} that validates if the collection contains the element.
     */
    public Rule<C> contains(T element) {
        return Rule.of(
                values -> contains(values, element),
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
     */
    public Rule<C> containsAll(Iterable<? extends T> required) {
        Objects.requireNonNull(required, "required cannot be null");

        Set<T> requiredSet = HashSet.ofAll(required);

        return Rule.of(
                values -> requiredSet.forAll(req -> contains(values, req)),
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
     */
    public Rule<C> containsAnyOf(Iterable<? extends T> candidates) {
        Objects.requireNonNull(candidates, "candidates cannot be null");

        Set<T> candidateSet = HashSet.ofAll(candidates);

        return Rule.of(
                values -> !candidateSet.isEmpty() && candidateSet.exists(candidate -> contains(values, candidate)),
                ErrorMessage.of("must.contain.any.of", HashMap.of("candidates", candidateSet))
        );
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
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/rules/collections/CollectionRulesSnippets.java" region = "unique-by-example"}
     *
     
     * @param keyExtractor the function to extract the unique key.
     * @param key          the label for the key (e.g., "email").
     * @return a {@link Rule} checking for uniqueness by key.
     */
    public <K> Rule<C> uniqueBy(Function1<T, K> keyExtractor, String key) {
        Objects.requireNonNull(keyExtractor, "keyExtractor cannot be null");
        Objects.requireNonNull(key, "key cannot be null");

        return Rule.notNull().and(values -> {
            io.vavr.collection.List<T> list = io.vavr.collection.List.ofAll(values);

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
     */
    public Rule<C> validateValuesWith(Rule<? super T> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return Rule.notNull().and(collection -> {
            Rule<T> castedRule = Rule.narrow(rule);

            io.vavr.collection.List<Validation<T>> validations = io.vavr.collection.List.ofAll(collection)
                    .map(castedRule::test)
                    .zipWithIndex((validation, index) -> validation.mapErrors(errors -> errors.map(e -> e.atIndex(index))));

            io.vavr.collection.List<ErrorMessage> allErrors = validations.flatMap(Validation::errors);
            if (allErrors.isEmpty()) {
                return Validation.valid(collection);
            } else {
                return Validation.invalid(allErrors);
            }
        });
    }
}
