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

import static be.iffy.fv.Validation.invalid;

abstract class Lifter<T, R> {

    abstract Validation<R> test(T value);

    protected RuleLike<List<T>, Validation<List<R>>> toVavrList() {
        return values -> {
            if (values == null) {
                return Validation.Invalid.notNull();
            }
            List<Validation<R>> validations = values.map(this::test);
            // Validation.sequence already adds the [index] path segment, so we don't do it here.
            return Validations.sequence(validations);
        };
    }

    protected RuleLike<java.util.List<T>, Validation<java.util.List<R>>> toList() {
        return values -> {
            if (values == null) {
                return Validation.Invalid.notNull();
            }
            java.util.List<Validation<R>> validations = values.stream().map(this::test).toList();
            // Validation.sequence already adds the [index] path segment, so we don't do it here.
            return Validations.sequence(validations);
        };
    }

    protected RuleLike<Option<T>, Validation<Option<R>>> toOption() {
        return opt -> {
            if (opt == null) {
                return Validation.Invalid.notNull();
            }
            return opt.map(v -> this.test(v).map(Option::of))
                    .getOrElse(() -> Validation.valid(Option.none()));
        };
    }

    /**
     * Lifts the current mapping rule to operate on the content of {@link Optional} containers.
     * Empty Optionals are considered to be valid.
     */
    protected RuleLike<Optional<T>, Validation<Optional<R>>> toOptional() {
        return opt -> {
            if (opt == null) {
                return Validation.Invalid.notNull();
            }
            return opt
                    .map(v -> this.test(v).map(Optional::of))
                    .orElse(Validation.valid(Optional.empty()));
        };
    }

    protected <K> RuleLike<Map<K, T>, Validation<Map<K, R>>> toVavrMap() {
        return toVavrMap(Objects::toString);
    }

    protected <K> RuleLike<Map<K, T>, Validation<Map<K, R>>> toVavrMap(Function<K, Object> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor cannot be null");
        return map -> {
            if(map == null) {
                return Validation.Invalid.notNull();
            }
            Seq<Tuple2<K, Validation<R>>> validations = map.map(tuple ->
                    Tuple.of(tuple._1, this.test(tuple._2).mapErrors(errors ->
                            errors.map(e -> e.atIndex(keyExtractor.apply(tuple._1)))
                    ))
            );

            var validAndInvalid = validations.partition(t -> t._2.isValid());
            if (validAndInvalid._2.nonEmpty()) {
                return invalid(validAndInvalid._2.flatMap(t -> t._2.errors()).toList());
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

    protected <K> RuleLike<java.util.Map<K, T>, Validation<java.util.Map<K, R>>> toMap() {
        return toMap(Objects::toString);
    }

    protected <K> RuleLike<java.util.Map<K, T>, Validation<java.util.Map<K, R>>> toMap(Function<K, Object> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor cannot be null");
        //TODO make native version
        return value -> toVavrMap(keyExtractor).apply(HashMap.ofAll(value)).map(Map::toJavaMap);
    }

}
