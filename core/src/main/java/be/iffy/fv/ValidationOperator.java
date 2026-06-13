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
import java.util.function.Predicate;

import static be.iffy.fv.Validation.invalid;

/**
 * Internal interface mostly meant for sharing implementations between Rule and MappingRule.
 * Clients of the library should never directly see this interface.
 * All methods defined here should be overridden in the subclasses with a more appropriate return type.
 */
@FunctionalInterface
interface ValidationOperator<T, R> extends Function<T, Validation<R>> {

    Validation<R> test(T value);

    @Override
    default Validation<R> apply(T value) {
        return test(value);
    }

    default <S extends T> Predicate<S> toPredicate() {
        return value -> test(value).isValid();
    }

    default Function<T, Validation<R>> withErrorKey(String errorKey) {
        Objects.requireNonNull(errorKey, "errorKey cannot be null");
        return input ->
                this.test(input).mapErrors(ignore -> List.of(ErrorMessage.of(errorKey)));
    }

    default Function<List<T>, Validation<List<R>>> liftToVavrList() {
        return values -> {
            if (values == null) {
                return Validation.Invalid.notNull();
            }
            List<Validation<R>> validations = values.map(this::test);
            // Validation.sequence already adds the [index] path segment, so we don't do it here.
            return Validations.transpose(validations);
        };
    }

    default Function<java.util.List<T>, Validation<java.util.List<R>>> liftToList() {
        return values -> {
            if (values == null) {
                return Validation.Invalid.notNull();
            }
            java.util.List<Validation<R>> validations = values.stream().map(this::test).toList();
            // Validation.sequence already adds the [index] path segment, so we don't do it here.
            return Validations.transpose(validations);
        };
    }

    default Function<Option<T>, Validation<Option<R>>> liftToOption() {
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
    default Function<Optional<T>, Validation<Optional<R>>> liftToOptional() {
        return opt -> {
            if (opt == null) {
                return Validation.Invalid.notNull();
            }
            return opt
                    .map(v -> this.test(v).map(Optional::of))
                    .orElse(Validation.valid(Optional.empty()));
        };
    }

    default <K> Function<Map<K, T>, Validation<Map<K, R>>> liftToVavrMap() {
        return liftToVavrMap(Objects::toString);
    }

    default <K> Function<Map<K, T>, Validation<Map<K, R>>> liftToVavrMap(Function<K, Object> keyExtractor) {
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

    default <K> Function<java.util.Map<K, T>, Validation<java.util.Map<K, R>>> liftToMap() {
        return liftToMap(Objects::toString);
    }

    default <K> Function<java.util.Map<K, T>, Validation<java.util.Map<K, R>>> liftToMap(Function<K, Object> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor cannot be null");
        //TODO make native version
        return value -> liftToVavrMap(keyExtractor).apply(HashMap.ofAll(value)).map(Map::toJavaMap);
    }

}
