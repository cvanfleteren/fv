package net.vanfleteren.fv.rules;

import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.*;
import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.Validation;

import java.util.Objects;
import java.util.function.Predicate;

public class CollectionRules {

    public static Rule<Iterable<?>> notEmpty = Rule.of(value -> value.iterator().hasNext(), "cannot.be.empty");

    public static Rule<Traversable<?>> minSize(int size) {
        return Rule.of(value -> value.size() >= size, ErrorMessage.of("min.size", "min", size));
    }

    public static Rule<Traversable<?>> maxSize(int size) {
        return Rule.of(value -> value.size() <= size, ErrorMessage.of("max.size", "max", size));
    }

    public static Rule<Traversable<?>> sizeEquals(int size) {
        return Rule.of(value -> value.size() == size, ErrorMessage.of("equal.size", "equal", size));
    }

    public static Rule<Traversable<?>> sizeBetween(int min, int max) {
        return Rule.of(
                value -> {
                    int size = value.size();
                    return size >= min && size <= max;
                },
                ErrorMessage.of("equal.size", HashMap.of("min", min, "max", max))
        );
    }

    /**
     * Checks if the collection contains no null elements.
     * @return A rule that validates if the collection has no null elements.
     */
    public static <T> Rule<List<T>> noNullElements() {
        Rule<T> notNull = ObjectRules.notNull();
        return notNull.liftToList();
    }

    /**
     * Checks if all elements in the collection match the given predicate.
     * @param predicate The predicate to test each element against.
     * @return A rule that validates if all elements match the predicate.
     */
    public static <T> Rule<List<T>> allMatch(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return allMatch(predicate, ErrorMessage.of("all.should.match"));
    }

    /**
     * Checks if all elements in the collection match the given predicate.
     * @param predicate The predicate to test each element against.
     * @param errorMessage The error message to use if validation fails.
     * @return A rule that validates if all elements match the predicate.
     */
    public static <T> Rule<List<T>> allMatch(Predicate<T> predicate, ErrorMessage errorMessage) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return value -> Rule.of(predicate, errorMessage).liftToList().test(value);
    }

    /**
     * Checks if none of the elements in the collection match the given predicate.
     * @param predicate The predicate to test each element against.
     * @return A rule that validates if none of the elements match the predicate.
     */
    public static <T> Rule<List<T>> noneMatch(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return noneMatch(predicate, ErrorMessage.of("none.should.match"));
    }

    /**
     * Checks if  none of the elements in the collection match the given predicate.
     * @param predicate The predicate to test each element against.
     * @param errorMessage The error message to use if validation fails.
     * @return A rule that validates if none of the elements match the predicate.
     */
    public static <T> Rule<List<T>> noneMatch(Predicate<T> predicate, ErrorMessage errorMessage) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return value -> Rule.of(predicate.negate(), errorMessage).liftToList().test(value);
    }

    /**
     * Checks if at least one of the elements in the collection match the given predicate.
     * @param predicate The predicate to test each element against.
     * @return A rule that validates if none of the elements match the predicate.
     */
    public static <T> Rule<List<T>> anyMatch(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return anyMatch(predicate, ErrorMessage.of("atleast.one.should.match"));
    }

    /**
     * Checks if at least one of the elements in the collection match the given predicate.
     * @param predicate The predicate to test each element against.
     * @param errorMessage The error message to use if validation fails.
     * @return A rule that validates if at least one of the elements match the predicate.
     */
    public static <T> Rule<List<T>> anyMatch(Predicate<T> predicate, ErrorMessage errorMessage) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        return value -> value.exists(predicate) ? Validation.valid(value) : Validation.invalid(errorMessage);
    }

    /**
     * Checks if the collection contains the given element.
     * @param element The element to check for.
     * @return A rule that validates if the collection contains the element.
     * @param <T> The type of elements in the collection.
     */
    public static <T> Rule<Iterable<T>> contains(T element) {
        return Rule.of(
                values -> Iterator.ofAll(values).contains(element),
                ErrorMessage.of("must.contain", HashMap.of("element", element))
        );
    }

    /**
     * Semantics: every element from {@code required} must appear at least once in {@code values}.
     * Note: duplicates in {@code required} are ignored (treated as a set).
     */
    public static <T> Rule<Iterable<T>> containsAll(Iterable<? extends T> required) {
        Objects.requireNonNull(required, "required cannot be null");

        Set<T> requiredSet = HashSet.ofAll(required);

        return Rule.of(
                values -> Iterator.ofAll(values).containsAll(requiredSet),
                ErrorMessage.of("must.contain.all", HashMap.of("required", requiredSet))
        );
    }

    /**
     * Semantics: at least one element from {@code candidates} must appear in {@code values}.
     * If {@code candidates} is empty, the rule fails (there is no acceptable element to match).
     */
    public static <T> Rule<Iterable<T>> containsAnyOf(Iterable<? extends T> candidates) {
        Objects.requireNonNull(candidates, "candidates cannot be null");

        Set<T> candidateSet = HashSet.ofAll(candidates);

        return Rule.of(
                values -> {
                    if (candidateSet.isEmpty()) {
                        return false;
                    }
                    return Iterator.ofAll(values).exists(candidateSet::contains);
                },
                ErrorMessage.of("must.contain.any.of", HashMap.of("candidates", candidateSet))
        );
    }

    /**
     * Ensures the extracted key is unique within the iterable.
     *
     * Accumulates duplicates and includes the duplicate keys in the error args.
     *
     * Error args shape:
     * - "key": {@code key} parameter (a label, e.g. "email")
     * - "duplicates": Map<K, List<Integer>> where each key maps to the indices where it occurred (size >= 2)
     */
    public static <T, K> Rule<Iterable<T>> uniqueBy(Function1<T, K> keyExtractor, String key) {
        Objects.requireNonNull(keyExtractor, "keyExtractor cannot be null");
        Objects.requireNonNull(key, "key cannot be null");

        return values -> {
            Objects.requireNonNull(values, "values cannot be null");

            Tuple2<Map<K, Integer>, Map<K, List<Integer>>> state = Iterator.ofAll(values)
                    .zipWithIndex()
                    .foldLeft(
                            Tuple.of(HashMap.empty(), HashMap.empty()),
                            (acc, t) -> {
                                Map<K, Integer> firstIndexByKey = acc._1;
                                Map<K, List<Integer>> duplicateIndicesByKey = acc._2;

                                T value = t._1;
                                int idx = t._2;
                                K keyValue = keyExtractor.apply(value);

                                if (firstIndexByKey.containsKey(keyValue)) {
                                    int firstIdx = firstIndexByKey.get(keyValue).get();

                                    List<Integer> indices = duplicateIndicesByKey
                                            .get(keyValue)
                                            .getOrElse(List.of(firstIdx))
                                            .append(idx);

                                    return Tuple.of(firstIndexByKey, duplicateIndicesByKey.put(keyValue, indices));
                                } else {
                                    return Tuple.of(firstIndexByKey.put(keyValue, idx), duplicateIndicesByKey);
                                }
                            }
                    );

            Map<K, List<Integer>> duplicateIndicesByKey = state._2;

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
     * The invidiual ErrorMessages are passed to the final Validation.
     */
    public static <T> Rule<List<T>> validateValuesWith(Rule<? super T> rule) {
        return list -> {
            Rule<T> castedRule = (Rule<T>) rule;
            Rule<List<T>> rule2 =  castedRule.liftToList();
            return rule2.test(list);
        };
    }


}
