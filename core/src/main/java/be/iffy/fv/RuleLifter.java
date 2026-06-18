package be.iffy.fv;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Seq;
import io.vavr.control.Option;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class RuleLifter<T> extends Lifter<T,T> {

    private final Rule<T> rule;

    RuleLifter(Rule<T> rule) {
        this.rule = rule;
    }

    @Override
    public Validation<T> test(T value) {
        return rule.apply(value);
    }

    /**
     * Lifts this {@link Rule} so it applies to a {@link List} of T instead of a single T.
     */
    public Rule<List<T>> toVavrList() {
        return Rule.of(super.toVavrList());
    }

    /**
     * Lifts this {@link Rule} so it applies to a {@link java.util.List} of T instead of a single T.
     */
    @Override
    public Rule<java.util.List<T>> toList() {
        return values -> {
            if (values == null) {
                return Validation.Invalid.notNull();
            }
            java.util.List<Validation<T>> validations = values.stream().map(this::test).toList();
            // Validation.sequence already adds the [index] path segment, so we don't do it here.
            return Validations.sequence(validations);
        };
    }

    /**
     * Lifts this {@link Rule} so it applies to an {@link Option} of T.
     * <p>
     * Semantics:
     *   - if the Option is None, it is considered to be Valid
     *   - if the Option is Some, the content is validated either a {@code Valid<Option<T>>}
     *   or an Invalid with the errors is returned
     */
    @Override
    public Rule<Option<T>> toOption() {
        return Rule.of(super.toOption());
    }

    /**
     * Lifts this {@link Rule} so it applies to an {@link Optional} of T.
     * <p>
     * Semantics:
     *   - if the Option is empty, it is considered to be Valid
     *   - if the Option is defined, the content is validated either a {@code Valid<Optional<T>>}
     *   or an Invalid with the errors is returned
     */
    @Override
    public Rule<Optional<T>> toOptional() {
        return Rule.of(super.toOptional());
    }

    /**
     * Lifts this {@link Rule} so it applies to a {@link Map} of K to T.
     * <p>
     * Be careful, the key {@code value.toString()} will be used as part of the path segment.
     * Make sure to have a key that has a meaningful string representation for this.
     * If you can't guarantee this, use the version of {@link #toVavrMap(Function)} that takes a keyExtractor function instead.
     * <p>
     * Semantics:
     * - If the Map is empty, the map is considered valid.
     * - Each value in the map is validated, and the resulting validations are collected.
     * - If any validation fails, the entire map is considered invalid with all errors accumulated.
     * - If all validations pass, the map is considered valid.
     */
    @Override
    public <K> Rule<Map<K, T>> toVavrMap() {
        return Rule.of(super.toVavrMap());
    }

    /**
     * Lifts this {@link Rule} so it applies to a {@link Map} of K to T.
     * <p>
     * Behaves the same as {@link #toVavrMap()}, but uses the keyExtractor function to generate the path segment.
     */
    @Override
    public <K> Rule<Map<K, T>> toVavrMap(Function<K, Object> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor cannot be null");
        return map -> {
            if (map == null) {
                return Validation.Invalid.notNull();
            }
            Seq<Tuple2<K, Validation<T>>> validations = map.map(tuple ->
                    Tuple.of(tuple._1, this.test(tuple._2).mapErrors(errors ->
                            errors.map(e -> e.atIndex(keyExtractor.apply(tuple._1)))
                    ))
            );

            var validAndInvalid = validations.partition(t -> t._2.isValid());
            if (validAndInvalid._2.nonEmpty()) {
                return Validation.invalid(validAndInvalid._2.flatMap(t -> t._2.errors()).toList());
            } else {
                return Validation.valid(
                        validAndInvalid._1.toMap(
                                Tuple2::_1,
                                t ->
                                        t._2.getOrElseThrow()
                        )
                );
            }
        };
    }

    /**
     * Lifts this {@link Rule} so it applies to a {@link java.util.Map} of K to T.
     * <p>
     * Be careful, the key {@code value.toString()} will be used as part of the path segment.
     * Make sure to have a key that has a meaningful string representation for this.
     * If you can't guarantee this, use the version of {@link #toMap(Function)} that takes a keyExtractor function instead.
     * <p>
     * Semantics:
     * - If the Map is empty, the map is considered valid.
     * - Each value in the map is validated, and the resulting validations are collected.
     * - If any validation fails, the entire map is considered invalid.
     * - If all validations pass, the map is considered valid.
     */
    @Override
    public <K> Rule<java.util.Map<K, T>> toMap() {
        return Rule.of(super.toMap());
    }

    /**
     * Lifts this {@link Rule} so it applies to a {@link java.util.Map} of K to T.
     * <p>
     * Behaves the same as {@link #toMap()}, but uses the keyExtractor function to generate the path segment.
     */
    @Override
    public <K> Rule<java.util.Map<K, T>> toMap(Function<K, Object> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor cannot be null");
        return Rule.of(
                (java.util.Map<K, T> map) -> {
                    Seq<Tuple2<K, Validation<T>>> validations = HashMap.ofAll(map).map(tuple ->
                            Tuple.of(tuple._1, this.test(tuple._2).mapErrors(errors ->
                                    errors.map(e -> e.atIndex(keyExtractor.apply(tuple._1)))
                            ))
                    );

                    var validAndInvalid = validations.partition(t -> t._2.isValid());
                    if (validAndInvalid._2.nonEmpty()) {
                        return Validation.invalid(validAndInvalid._2.flatMap(t -> t._2.errors()).toList());
                    } else {
                        return Validation.valid(
                                validAndInvalid._1.toMap(
                                        Tuple2::_1,
                                        t ->
                                                t._2.getOrElseThrow()
                                ).toJavaMap()
                        );
                    }
                }
        );
    }

}
