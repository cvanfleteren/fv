package be.iffy.fv;

import io.vavr.*;
import io.vavr.collection.Iterator;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A container representing the result of a validation process.
 *
 * <p>A {@code Validation} can be either:
 * <ul>
 *     <li>{@link Valid}: representing a successful validation and containing the valid value.</li>
 *     <li>{@link Invalid}: representing a failed validation and containing one or more {@link ErrorMessage} objects.</li>
 * </ul>
 *
 * <h2>Example: Basic usage and result handling</h2>
 * <pre>{@code
 * // 1. Create a validation (e.g., from a rule or a factory)
 * Validation<String> result = Validation.valid("Hello World");
 *
 * // 2. Transform the result if it is valid
 * Validation<Integer> lengthV = result.map(String::length);
 *
 * // 3. Handle the outcome with pattern matching
 * String outcome = switch (lengthV) {
 *     case Validation.Valid(var value) -> "Length is: " + value;
 *     case Validation.Invalid(var errors) -> "Validation failed with " + errors.size() + " errors";
 * };
 *
 * System.out.println(outcome);
 * }</pre>
 *
 * <h2>Example: Accumulating errors from multiple validations</h2>
 * <pre>{@code
 * Validation<String> v1 = Validation.valid("foo");
 * Validation<Integer> v2 = Validation.invalid("must.be.positive");
 *
 * // mapN combines results if all are valid, otherwise accumulates all errors
 * Validation<String> combined = Validation.mapN(v1, v2, (s, i) -> s + ":" + i);
 *
 * if (combined instanceof Validation.Invalid(var errors)) {
 *     // errors will contain "must.be.positive"
 * }
 * }</pre>
 *
 * <h2>Exception handling</h2>
 *
 * <p>Most {@code Validation} operations do not catch unexpected runtime exceptions.
 * Methods such as {@link #map(Function)}, {@link #flatMap(Function)},
 * {@code mapN(...)} and {@code flatMapN(...)} are intended for regular functional
 * composition. If a mapper throws, the exception is propagated.
 *
 * <p>Use methods whose names contain {@code Catching}, or {@link #from(Try, String)},
 * when you intentionally want to convert {@link ValidationException} into validation errors.
 *
 * <p>Use methods whose names contain {@code CatchingAll}, or {@link #fromCatchingAll(Supplier, String)},
 * when you intentionally want to convert <em>ANY Exception</em> into validation errors.
 *
 * <p>{@link ValidationException} is used by imperative validation APIs such as
 * {@code getOrElseThrow()} and constructor validation helpers. APIs that explicitly
 * catch validation failures preserve the errors contained in the exception.
 */
public sealed interface Validation<T> extends Iterable<T> {

    /**
     * Indicates whether the validation is successful.
     *
     * @return {@code true} if validation is successful, {@code false} otherwise.
     */
    boolean isValid();

    /**
     * Indicates whether the validation has failed.
     *
     * @return {@code true} if validation is invalid, {@code false} otherwise.
     */
    default boolean isInvalid() {
        return !isValid();
    }

    /**
     * Returns the error messages associated with this validation. Is empty when this Validation is valid.
     *
     * @return a {@link List} of {@link ErrorMessage} objects.
     */
    List<ErrorMessage> errors();

    //region value retrieval

    /**
     * Returns the valid value, or throws {@link ValidationException} if this validation is invalid.
     *
     * @throws ValidationException if this validation is invalid.
     */
    default T getOrElseThrow() {
        return switch (this) {
            case Valid(var value) -> value;
            case Invalid(var errors) -> throw new ValidationException(errors);
        };
    }

    /**
     * Returns the valid value, or the passed {@code defaultValue} if invalid.
     */
    default T getOrElse(T defaultValue) {
        return switch (this) {
            case Valid(var value) -> value;
            default -> defaultValue;
        };
    }

    /**
     * Returns this validation if it is valid; otherwise returns the specified alternative validation.
     */
    @SuppressWarnings("unchecked")
    default <U> Validation<U> orElse(Validation<? extends U> other) {
        Objects.requireNonNull(other, "other validation cannot be null");
        return isValid() ? (Validation<U>) this : Validation.narrow(other);
    }

    /**
     * Returns this validation if it is valid; otherwise returns the alternative validation
     * provided by the {@link Supplier}.
     *
     * @param supplier the alternative validation supplier.
     */
    @SuppressWarnings("unchecked")
    default <U> Validation<U> orElse(Supplier<? extends Validation<? extends U>> supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        return isValid() ? (Validation<U>) this : Validation.narrow(Objects.requireNonNull(supplier.get(), "supplier result cannot be null"));
    }

    /**
     * Converts the current result into an Optional containing the value if present,
     * or an empty Optional if the result represents an error
     */
    default Optional<T> toOptional() {
        return this.fold(
                e -> Optional.empty(),
                Optional::ofNullable
        );
    }

    /**
     * Converts the current result into an Optional containing the value if present,
     * or an empty Optional if the result represents an error
     */
    default Option<T> toOption() {
        return this.fold(
                e -> Option.none(),
                Option::of
        );
    }

    /**
     * Executes the given consumer on the contained value if this {@code Validation} is valid;
     * otherwise, does nothing. This method is an alias for {@link #peek(Consumer)}.
     *
     * @param consumer a consumer to apply to the contained value.
     * @return this {@code Validation} instance.
     * @see #peek(Consumer)
     */
    default Validation<T> whenValid(Consumer<T> consumer) {
        return peek(consumer);
    }

    /**
     * Executes the given consumer on the contained errors if this {@code Validation} is invalid;
     * otherwise, does nothing.
     */
    default Validation<T> whenInvalid(Consumer<List<ErrorMessage>> consumer) {
        Objects.requireNonNull(consumer, "consumer cannot be null");
        if (!isValid()) {
            consumer.accept(errors());
        }
        return this;
    }

    //endregion

    //region common functional operations on single validations

    /**
     * Transforms the valid value using the provided mapper. If the mapper throws any {@link RuntimeException}s, they will be rethrown.
     * Use {@link #mapCatching(Function)} if you want to handle {@link ValidationException}s thrown by the mapper.
     */
    default <R> Validation<R> map(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return (Validation<R>) switch (this) {
            case Valid(var value) -> Validation.<R>valid(mapper.apply(value));
            default -> (Validation<R>) this;
        };
    }

    /**
     * Like map, but catches only ValidationException and takes its errors to make an Invalid..
     * Other RuntimeExceptions are propagated.
     * So this method does not have pure map semantics.
     */
    default <R> Validation<R> mapCatching(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return switch (this) {
            case Valid(var value) -> {
                try {
                    yield new Valid<>(mapper.apply(value));
                } catch (ValidationException e) {
                    yield Validation.invalid(e.errors());
                }
            }
            default -> (Validation<R>) this;
        };
    }   

    /**
     * Like {@link #mapCatching(Function)}, but catches all {@link Exception}s thrown by the mapper.
     */
    default <R> Validation<R> mapCatchingAll(Function<? super T, ? extends R> mapper, ErrorMessage errorMessage) {
        return mapCatchingAll(mapper, e -> errorMessage);
    }

    /**
     * Like {@link #mapCatchingAll(Function, ErrorMessage)}, but uses the provided error key if an unexpected exception occurs.
     */
    default <R> Validation<R> mapCatchingAll(Function<? super T, ? extends R> mapper, String errorKey) {
        return mapCatchingAll(mapper, ErrorMessage.of(errorKey));
    }

    /**
     * Like {@link #mapCatchingAll(Function, ErrorMessage)}, but uses the provided mapper to create an {@link ErrorMessage} if an unexpected exception occurs.
     */
    default <R> Validation<R> mapCatchingAll(Function<? super T, ? extends R> mapper, Function<Exception, ErrorMessage> errorMessageMaker) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        Objects.requireNonNull(errorMessageMaker, "errorMessageMaker cannot be null");
        return switch (this) {
            case Valid(var value) -> {
                try {
                    yield new Valid<>(mapper.apply(value));
                } catch (ValidationException e) {
                    yield Validation.invalid(e.errors());
                } catch (Exception e) {
                    yield Validation.invalid(errorMessageMaker.apply(e));
                }
            }
            default -> (Validation<R>) this;
        };
    }

    /**
     * Maps a valid value to a new validation, or returns this if invalid.  If the mapper throws any {@link RuntimeException}s, they will be rethrown.
     * Use {@link #flatMapCatching(Function)} if you want to handle {@link ValidationException}s thrown by the mapper.
     */
    default <R> Validation<R> flatMap(Function<? super T, Validation<? extends R>> flatMapper) {
        Objects.requireNonNull(flatMapper, "flatMapper cannot be null");
        return switch (this) {
            case Valid(var value) -> Validation.narrow(flatMapper.apply(value));
            default -> (Validation<R>) this;
        };
    }

    /**
     * Like {@link #flatMap(Function)}, but catches {@link ValidationException}s thrown by the mapper and turns them into an invalid validation.
     */
    default <R> Validation<R> flatMapCatching(Function<? super T, Validation<? extends R>> flatMapper) {
        Objects.requireNonNull(flatMapper, "flatMapper cannot be null");
        return switch (this) {
            case Valid(var value) -> {
                try {
                    yield Validation.narrow(flatMapper.apply(value));
                } catch (ValidationException e) {
                    yield Validation.invalid(e.errors());
                }
            }
            default -> (Validation<R>) this;
        };
    }

    /**
     * Like {@link #flatMap(Function)}, but catches all {@link Exception}s thrown by the mapper and turns them into an invalid validation.
     * Like {@link #flatMapCatching(Function)}, if a {@link ValidationException} is thrown, its errors are used for the resulting Invalid.
     * If an exception other than {@link ValidationException} is thrown, the provided {@link ErrorMessage} is used.
     */
    default <R> Validation<R> flatMapCatchingAll(Function<? super T, Validation<? extends R>> flatMapper, ErrorMessage errorMessage) {
        return flatMapCatchingAll(flatMapper, e -> errorMessage);
    }

    /**
     * Like {@link #flatMapCatchingAll(Function, ErrorMessage)}, but uses the provided error key.
     */
    default <R> Validation<R> flatMapCatchingAll(Function<? super T, Validation<? extends R>> flatMapper, String errorKey) {
        return flatMapCatchingAll(flatMapper, ErrorMessage.of(errorKey));
    }

    /**
     * Like {@link #flatMapCatchingAll(Function, ErrorMessage)}, but uses the provided mapper to create an {@link ErrorMessage}.
     */
    default <R> Validation<R> flatMapCatchingAll(Function<? super T, Validation<? extends R>> flatMapper, Function<Exception, ErrorMessage> errorMessageMaker) {
        Objects.requireNonNull(flatMapper, "flatMapper cannot be null");
        Objects.requireNonNull(errorMessageMaker, "errorMessageMaker cannot be null");
        return switch (this) {
            case Valid(var value) -> {
                try {
                    yield Validation.narrow(flatMapper.apply(value));
                } catch (ValidationException e) {
                    yield Validation.invalid(e.errors());
                } catch (Exception e) {
                    yield Validation.invalid(errorMessageMaker.apply(e));
                }
            }
            default -> (Validation<R>) this;
        };
    }

    /**
     * Folds this validation into a single value by applying one of two functions.
     */
    default <R> R fold(Function<List<ErrorMessage>, ? extends R> whenInvalid, Function<? super T, ? extends R> whenValid) {
        Objects.requireNonNull(whenInvalid, "whenInvalid cannot be null");
        Objects.requireNonNull(whenValid, "whenValid cannot be null");
        return switch (this) {
            case Valid(var value) -> whenValid.apply(value);
            case Invalid(var errors) -> whenInvalid.apply(errors);
        };
    }

    /**
     * Alias for {@link #flatMap(Function)}.
     */
    default <R> Validation<R> refine(Function<? super T, ? extends Validation<R>> refinement) {
        Objects.requireNonNull(refinement, "refinement cannot be null");
        return this.flatMap(refinement::apply);
    }

    /**
     * If this validation is valid, applies the predicate to its value.
     * If the predicate returns false, returns Invalid(errorMessage).
     * If this validation is already invalid, returns it unchanged.
     */
    default Validation<T> filter(Predicate<? super T> predicate, ErrorMessage errorMessage) {
        Rule<T> rule = Rule.narrow(Rule.of(predicate, errorMessage));
        return refine(rule);
    }

    /**
     * If this validation is valid, applies the predicate to its value.
     * If the predicate returns false, returns Invalid(errorKey).
     * If this validation is already invalid, returns it unchanged.
     */
    default Validation<T> filter(Predicate<? super T> predicate, String errorKey) {
        return filter(predicate, ErrorMessage.of(errorKey));
    }
    //endregion

    //region Error handling

    /**
     * Maps the error messages of an invalid validation using the provided mapper function.
     * If this validation is valid, the mapper is not applied.
     * The mapper cannot return an empty List.
     */
    default Validation<T> mapErrors(Function<List<ErrorMessage>, List<ErrorMessage>> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return switch (this) {
            case Valid<T> v -> v;
            case Invalid(var errors) -> invalid(mapper.apply(errors));
        };
    }

    /**
     * Maps error messages by prepending the given name to the segments of each error message.
     *
     * @param name a logical name for the value being validated (e.g., the name of the field).
     * @return a new {@link Validation} instance.
     */
    default Validation<T> at(String name) {
        return mapErrors(errors -> errors.map(error -> error.prepend(ErrorMessage.Path.of(name))));
    }

    /**
     * Maps error messages by prepending the propertyName for the given selector to the segments of each error message.
     *
     * @param selector The selector for the value that was validated (e.g., SomeRecord::someField or SomeBean::getProperty).
     */
    default <ANY> Validation<T> at(PropertySelector<ANY, T> selector) {
        String name = selector.getPropertyName();
        return at(name);
    }
    //endregion

    //region common functional operations on multiple validations

    /**
     * Transforms a {@link Seq} of {@link Validation}s into a single {@code Validation} of a {@link List}.
     * If any validation is invalid, the result will contain all accumulated errors.
     *
     * @param validations the sequence of validations to sequence.
     */
    static <T> Validation<List<T>> transpose(Seq<? extends Validation<? extends T>> validations) {
        return transpose(validations, "");
    }

    /**
     * Transforms a {@link Seq} of {@link Validation}s into a single {@code Validation} of a {@link List}.
     * If any validation is invalid, the result will contain all accumulated errors.
     *
     * @param validations the sequence of validations to sequence.
     * @param name        the path entry under which the errors will be mapped. e.g., name "foo" will result in errormessages like "foo[1].some.message"
     *                    if the second entry in the list is invalid.
     */
    static <T> Validation<List<T>> transpose(Seq<? extends Validation<? extends T>> validations, String name) {
        return validations
                .zipWithIndex()
                .foldLeft(
                        Validation.valid(List.empty()),
                        (acc, validationWithIndex) -> {
                            if (acc instanceof Valid<List<T>>(var list)) {
                                Validation<T> v = Validation.narrow(validationWithIndex._1.at(name));
                                return switch (v) {
                                    case Valid<T>(var value) -> Validation.valid(list.append(value));
                                    case Invalid(var errors) ->
                                            Validation.invalid(errors.map(error -> error.atIndex(validationWithIndex._2)));
                                };
                            } else {
                                Validation<T> v = Validation.narrow(validationWithIndex._1.at(name));
                                return switch (v) {
                                    case Valid<T>(var value) -> acc;
                                    case Invalid(var errors) ->
                                            Validation.invalid(acc.errors().appendAll(errors.map(error -> error.atIndex(validationWithIndex._2))));
                                };
                            }
                        }
                );
    }

    /**
     * Transforms a {@code Option<Validation<T>>} into a {@code Validation<Option<T>>}.
     * If the Option is empty, the resulting Validation is considered to be {@link Valid}
     *
     */
    static <T> Validation<Option<T>> transpose(Option<? extends Validation<? extends T>> option) {
        return option.fold(
                () -> Validation.valid(Option.none()),
                validation -> validation.map(Option::of)
        );
    }

    /**
     * Transforms a {@code Optional<Validation<T>>} into a {@code Validation<Optional<T>>}.
     * If the Optional is empty, the resulting Validation is considered to be {@link Valid}
     *
     */
    static <T> Validation<Optional<T>> transpose(Optional<? extends Validation<? extends T>> option) {
        return option.<Validation<Optional<T>>>map(validation -> validation.map(Optional::ofNullable))
                .orElseGet(() -> Validation.valid(Optional.empty()));
    }

    /**
     * Transforms a {@link java.util.Collection} of {@link Validation}s into a single {@code Validation} of a {@link java.util.List}.
     * If any validation is invalid, the result will contain all accumulated errors.
     *
     * @param validations the collection of validations to sequence.
     * @return a {@code Validation} containing a list of values if all are valid, or all errors if any are invalid.
     */
    static <T> Validation<java.util.List<T>> transpose(java.util.Collection<? extends Validation<? extends T>> validations) {
        return transpose(List.ofAll(validations))
                .map(List::asJava);
    }

    /**
     * Transforms a {@link java.util.Collection} of {@link Validation}s into a single {@code Validation} of a {@link java.util.List}.
     * If any validation is invalid, the result will contain all accumulated errors.
     *
     * @param validations the collection of validations to sequence.
     * @return a {@code Validation} containing a list of values if all are valid, or all errors if any are invalid.
     */
    static <T> Validation<java.util.List<T>> transpose(java.util.Collection<? extends Validation<? extends T>> validations, String at) {
        return transpose(List.ofAll(validations), at)
                .map(List::asJava);
    }
    //endregion

    //region mapN / flatMapN

    /**
     * Maps two {@link Validation}s using the provided mapper function.
     * If all validations are valid, the result is a valid {@link Validation} containing the mapped value.
     * If any validation is invalid, the result is an invalid {@link Validation} containing all error messages.
     */
    static <R, T1, T2> Validation<R> mapN(Validation<? extends T1> v1, Validation<? extends T2> v2, Function2<? super T1, ? super T2, ? extends R> mapper) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(mapper, "mapper cannot be null");

        if (v1 instanceof Valid(var t1) && v2 instanceof Valid(var t2)) {
            return valid(mapper.apply(t1, t2));
        } else {
            return invalid(List.of(v1.errors(), v2.errors()).flatMap(Function.identity()));
        }
    }

    /**
     * Maps two {@link Validation}s to a new validation using the provided flatMapper function.
     * If all validations are valid, the result is the result of the flatMapper.
     * If any validation is invalid, the result is an invalid {@link Validation} containing all error messages.
     */
    static <R, T1, T2> Validation<R> flatMapN(Validation<? extends T1> v1, Validation<? extends T2> v2, Function2<? super T1, ? super T2, Validation<? extends R>> mapper) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(mapper, "mapper cannot be null");

        if (v1 instanceof Valid(var t1) && v2 instanceof Valid(var t2)) {
            return Validation.narrow(mapper.apply(t1, t2));
        } else {
            return invalid(List.of(v1.errors(), v2.errors()).flatMap(Function.identity()));
        }
    }

    /**
     * Maps three {@link Validation}s using the provided mapper function.
     * If all validations are valid, the result is a valid {@link Validation} containing the mapped value.
     * If any validation is invalid, the result is an invalid {@link Validation} containing all error messages.
     */
    static <R, T1, T2, T3> Validation<R> mapN(Validation<? extends T1> v1, Validation<? extends T2> v2, Validation<? extends T3> v3, Function3<? super T1, ? super T2, ? super T3, ? extends R> mapper) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        Objects.requireNonNull(mapper, "mapper cannot be null");

        if (v1 instanceof Valid(var t1) && v2 instanceof Valid(var t2) && v3 instanceof Valid(var t3)) {
            return valid(mapper.apply(t1, t2, t3));
        } else {
            return invalid(List.of(v1.errors(), v2.errors(), v3.errors()).flatMap(Function.identity()));
        }
    }

    /**
     * Maps three {@link Validation}s to a new validation using the provided flatMapper function.
     * If all validations are valid, the result is the result of the flatMapper.
     * If any validation is invalid, the result is an invalid {@link Validation} containing all error messages.
     */
    static <R, T1, T2, T3> Validation<R> flatMapN(Validation<? extends T1> v1, Validation<? extends T2> v2, Validation<? extends T3> v3, Function3<? super T1, ? super T2, ? super T3, Validation<? extends R>> mapper) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        Objects.requireNonNull(mapper, "mapper cannot be null");

        if (v1 instanceof Valid(var t1) && v2 instanceof Valid(var t2) && v3 instanceof Valid(var t3)) {
            return Validation.narrow(mapper.apply(t1, t2, t3));
        } else {
            return invalid(List.of(v1.errors(), v2.errors(), v3.errors()).flatMap(Function.identity()));
        }
    }

    /**
     * Maps four {@link Validation}s using the provided mapper function.
     * If all validations are valid, the result is a valid {@link Validation} containing the mapped value.
     * If any validation is invalid, the result is an invalid {@link Validation} containing all error messages.
     */
    static <R, T1, T2, T3, T4> Validation<R> mapN(Validation<? extends T1> v1, Validation<? extends T2> v2, Validation<? extends T3> v3, Validation<? extends T4> v4, Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> mapper) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        Objects.requireNonNull(v4, "v4 validation cannot be null");
        Objects.requireNonNull(mapper, "mapper cannot be null");

        if (v1 instanceof Valid(var t1) && v2 instanceof Valid(var t2) && v3 instanceof Valid(
                var t3
        ) && v4 instanceof Valid(var t4)) {
            return valid(mapper.apply(t1, t2, t3, t4));
        } else {
            return invalid(List.of(v1.errors(), v2.errors(), v3.errors(), v4.errors()).flatMap(Function.identity()));
        }
    }

    /**
     * Maps four {@link Validation}s to a new validation using the provided flatMapper function.
     * If all validations are valid, the result is the result of the flatMapper.
     * If any validation is invalid, the result is an invalid {@link Validation} containing all error messages.
     */
    static <R, T1, T2, T3, T4> Validation<R> flatMapN(Validation<? extends T1> v1, Validation<? extends T2> v2, Validation<? extends T3> v3, Validation<? extends T4> v4, Function4<? super T1, ? super T2, ? super T3, ? super T4, Validation<? extends R>> mapper) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        Objects.requireNonNull(v4, "v4 validation cannot be null");
        Objects.requireNonNull(mapper, "mapper cannot be null");

        if (v1 instanceof Valid(var t1) && v2 instanceof Valid(var t2) && v3 instanceof Valid(
                var t3
        ) && v4 instanceof Valid(var t4)) {
            return Validation.narrow(mapper.apply(t1, t2, t3, t4));
        } else {
            return invalid(List.of(v1.errors(), v2.errors(), v3.errors(), v4.errors()).flatMap(Function.identity()));
        }
    }

    /**
     * Maps five {@link Validation}s using the provided mapper function.
     * If all validations are valid, the result is a valid {@link Validation} containing the mapped value.
     * If any validation is invalid, the result is an invalid {@link Validation} containing all error messages.
     */
    static <R, T1, T2, T3, T4, T5> Validation<R> mapN(Validation<? extends T1> v1, Validation<? extends T2> v2, Validation<? extends T3> v3, Validation<? extends T4> v4, Validation<? extends T5> v5, Function5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> mapper) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        Objects.requireNonNull(v4, "v4 validation cannot be null");
        Objects.requireNonNull(v5, "v5 validation cannot be null");
        Objects.requireNonNull(mapper, "mapper cannot be null");

        if (v1 instanceof Valid(var t1) && v2 instanceof Valid(var t2) && v3 instanceof Valid(var t3)
                && v4 instanceof Valid(var t4) && v5 instanceof Valid(var t5)) {
            return valid(mapper.apply(t1, t2, t3, t4, t5));
        } else {
            return invalid(List.of(v1.errors(), v2.errors(), v3.errors(), v4.errors(), v5.errors()).flatMap(Function.identity()));
        }
    }

    /**
     * Maps five {@link Validation}s to a new validation using the provided flatMapper function.
     * If all validations are valid, the result is the result of the flatMapper.
     * If any validation is invalid, the result is an invalid {@link Validation} containing all error messages.
     */
    static <R, T1, T2, T3, T4, T5> Validation<R> flatMapN(Validation<? extends T1> v1, Validation<? extends T2> v2, Validation<? extends T3> v3, Validation<? extends T4> v4, Validation<? extends T5> v5, Function5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, Validation<? extends R>> mapper) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        Objects.requireNonNull(v4, "v4 validation cannot be null");
        Objects.requireNonNull(v5, "v5 validation cannot be null");
        Objects.requireNonNull(mapper, "mapper cannot be null");

        if (v1 instanceof Valid(var t1) && v2 instanceof Valid(var t2) && v3 instanceof Valid(
                var t3
        ) && v4 instanceof Valid(var t4) && v5 instanceof Valid(var t5)) {
            return Validation.narrow(mapper.apply(t1, t2, t3, t4, t5));
        } else {
            return invalid(List.of(v1.errors(), v2.errors(), v3.errors(), v4.errors(), v5.errors()).flatMap(Function.identity()));
        }
    }

    /**
     * Maps six {@link Validation}s using the provided mapper function.
     * If all validations are valid, the result is a valid {@link Validation} containing the mapped value.
     * If any validation is invalid, the result is an invalid {@link Validation} containing all error messages.
     */
    static <R, T1, T2, T3, T4, T5, T6> Validation<R> mapN(Validation<? extends T1> v1, Validation<? extends T2> v2, Validation<? extends T3> v3, Validation<? extends T4> v4, Validation<? extends T5> v5, Validation<? extends T6> v6, Function6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> mapper) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        Objects.requireNonNull(v4, "v4 validation cannot be null");
        Objects.requireNonNull(v5, "v5 validation cannot be null");
        Objects.requireNonNull(v6, "v6 validation cannot be null");
        Objects.requireNonNull(mapper, "mapper cannot be null");

        if (v1 instanceof Valid(var t1) && v2 instanceof Valid(var t2) && v3 instanceof Valid(
                var t3
        ) && v4 instanceof Valid(var t4) && v5 instanceof Valid(var t5) && v6 instanceof Valid(var t6)) {
            return valid(mapper.apply(t1, t2, t3, t4, t5, t6));
        } else {
            return invalid(List.of(v1.errors(), v2.errors(), v3.errors(), v4.errors(), v5.errors(), v6.errors()).flatMap(Function.identity()));
        }
    }

    /**
     * Maps six {@link Validation}s to a new validation using the provided flatMapper function.
     * If all validations are valid, the result is the result of the flatMapper.
     * If any validation is invalid, the result is an invalid {@link Validation} containing all error messages.
     */
    static <R, T1, T2, T3, T4, T5, T6> Validation<R> flatMapN(Validation<? extends T1> v1, Validation<? extends T2> v2, Validation<? extends T3> v3, Validation<? extends T4> v4, Validation<? extends T5> v5, Validation<? extends T6> v6, Function6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, Validation<? extends R>> mapper) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        Objects.requireNonNull(v4, "v4 validation cannot be null");
        Objects.requireNonNull(v5, "v5 validation cannot be null");
        Objects.requireNonNull(v6, "v6 validation cannot be null");
        Objects.requireNonNull(mapper, "mapper cannot be null");

        if (v1 instanceof Valid(var t1) && v2 instanceof Valid(var t2) && v3 instanceof Valid(
                var t3
        ) && v4 instanceof Valid(var t4) && v5 instanceof Valid(var t5) && v6 instanceof Valid(var t6)) {
            return Validation.narrow(mapper.apply(t1, t2, t3, t4, t5, t6));
        } else {
            return invalid(List.of(v1.errors(), v2.errors(), v3.errors(), v4.errors(), v5.errors(), v6.errors()).flatMap(Function.identity()));
        }
    }

    /**
     * Maps seven {@link Validation}s using the provided mapper function.
     * If all validations are valid, the result is a valid {@link Validation} containing the mapped value.
     * If any validation is invalid, the result is an invalid {@link Validation} containing all error messages.
     */
    static <R, T1, T2, T3, T4, T5, T6, T7> Validation<R> mapN(Validation<? extends T1> v1, Validation<? extends T2> v2, Validation<? extends T3> v3, Validation<? extends T4> v4, Validation<? extends T5> v5, Validation<? extends T6> v6, Validation<? extends T7> v7, Function7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? extends R> mapper) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        Objects.requireNonNull(v4, "v4 validation cannot be null");
        Objects.requireNonNull(v5, "v5 validation cannot be null");
        Objects.requireNonNull(v6, "v6 validation cannot be null");
        Objects.requireNonNull(v7, "v7 validation cannot be null");
        Objects.requireNonNull(mapper, "mapper cannot be null");

        if (v1 instanceof Valid(var t1) && v2 instanceof Valid(var t2) && v3 instanceof Valid(
                var t3
        ) && v4 instanceof Valid(var t4) && v5 instanceof Valid(var t5) && v6 instanceof Valid(
                var t6
        ) && v7 instanceof Valid(var t7)) {
            return valid(mapper.apply(t1, t2, t3, t4, t5, t6, t7));
        } else {
            return invalid(List.of(v1.errors(), v2.errors(), v3.errors(), v4.errors(), v5.errors(), v6.errors(), v7.errors()).flatMap(Function.identity()));
        }
    }

    /**
     * Maps seven {@link Validation}s to a new validation using the provided flatMapper function.
     * If all validations are valid, the result is the result of the flatMapper.
     * If any validation is invalid, the result is an invalid {@link Validation} containing all error messages.
     */
    static <R, T1, T2, T3, T4, T5, T6, T7> Validation<R> flatMapN(Validation<? extends T1> v1, Validation<? extends T2> v2, Validation<? extends T3> v3, Validation<? extends T4> v4, Validation<? extends T5> v5, Validation<? extends T6> v6, Validation<? extends T7> v7, Function7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, Validation<? extends R>> mapper) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        Objects.requireNonNull(v4, "v4 validation cannot be null");
        Objects.requireNonNull(v5, "v5 validation cannot be null");
        Objects.requireNonNull(v6, "v6 validation cannot be null");
        Objects.requireNonNull(v7, "v7 validation cannot be null");
        Objects.requireNonNull(mapper, "mapper cannot be null");

        if (v1 instanceof Valid(var t1) && v2 instanceof Valid(var t2) && v3 instanceof Valid(
                var t3
        ) && v4 instanceof Valid(var t4) && v5 instanceof Valid(var t5) && v6 instanceof Valid(
                var t6
        ) && v7 instanceof Valid(var t7)) {
            return Validation.narrow(mapper.apply(t1, t2, t3, t4, t5, t6, t7));
        } else {
            return invalid(List.of(v1.errors(), v2.errors(), v3.errors(), v4.errors(), v5.errors(), v6.errors(), v7.errors()).flatMap(Function.identity()));
        }
    }

    /**
     * Maps eight {@link Validation}s using the provided mapper function.
     * If all validations are valid, the result is a valid {@link Validation} containing the mapped value.
     * If any validation is invalid, the result is an invalid {@link Validation} containing all error messages.
     */
    static <R, T1, T2, T3, T4, T5, T6, T7, T8> Validation<R> mapN(Validation<? extends T1> v1, Validation<? extends T2> v2, Validation<? extends T3> v3, Validation<? extends T4> v4, Validation<? extends T5> v5, Validation<? extends T6> v6, Validation<? extends T7> v7, Validation<? extends T8> v8, Function8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R> mapper) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        Objects.requireNonNull(v4, "v4 validation cannot be null");
        Objects.requireNonNull(v5, "v5 validation cannot be null");
        Objects.requireNonNull(v6, "v6 validation cannot be null");
        Objects.requireNonNull(v7, "v7 validation cannot be null");
        Objects.requireNonNull(v8, "v8 validation cannot be null");
        Objects.requireNonNull(mapper, "mapper cannot be null");

        if (v1 instanceof Valid(var t1) && v2 instanceof Valid(var t2) && v3 instanceof Valid(
                var t3
        ) && v4 instanceof Valid(var t4) && v5 instanceof Valid(var t5) && v6 instanceof Valid(
                var t6
        ) && v7 instanceof Valid(var t7) && v8 instanceof Valid(var t8)) {
            return valid(mapper.apply(t1, t2, t3, t4, t5, t6, t7, t8));
        } else {
            return invalid(List.of(v1.errors(), v2.errors(), v3.errors(), v4.errors(), v5.errors(), v6.errors(), v7.errors(), v8.errors()).flatMap(Function.identity()));
        }
    }

    /**
     * Maps eight {@link Validation}s to a new validation using the provided flatMapper function.
     * If all validations are valid, the result is the result of the flatMapper.
     * If any validation is invalid, the result is an invalid {@link Validation} containing all error messages.
     */
    static <R, T1, T2, T3, T4, T5, T6, T7, T8> Validation<R> flatMapN(Validation<? extends T1> v1, Validation<? extends T2> v2, Validation<? extends T3> v3, Validation<? extends T4> v4, Validation<? extends T5> v5, Validation<? extends T6> v6, Validation<? extends T7> v7, Validation<? extends T8> v8, Function8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, Validation<? extends R>> mapper) {
        Objects.requireNonNull(v1, "v1 validation cannot be null");
        Objects.requireNonNull(v2, "v2 validation cannot be null");
        Objects.requireNonNull(v3, "v3 validation cannot be null");
        Objects.requireNonNull(v4, "v4 validation cannot be null");
        Objects.requireNonNull(v5, "v5 validation cannot be null");
        Objects.requireNonNull(v6, "v6 validation cannot be null");
        Objects.requireNonNull(v7, "v7 validation cannot be null");
        Objects.requireNonNull(v8, "v8 validation cannot be null");
        Objects.requireNonNull(mapper, "mapper cannot be null");

        if (v1 instanceof Valid(var t1) && v2 instanceof Valid(var t2) && v3 instanceof Valid(
                var t3
        ) && v4 instanceof Valid(var t4) && v5 instanceof Valid(var t5) && v6 instanceof Valid(
                var t6
        ) && v7 instanceof Valid(var t7) && v8 instanceof Valid(var t8)) {
            return Validation.narrow(mapper.apply(t1, t2, t3, t4, t5, t6, t7, t8));
        } else {
            return invalid(List.of(v1.errors(), v2.errors(), v3.errors(), v4.errors(), v5.errors(), v6.errors(), v7.errors(), v8.errors()).flatMap(Function.identity()));
        }
    }

    //endregion

    //region factory methods for known values

    /**
     * Creates a successful validation containing the provided value.
     * Null is technically allowed as a valid value but highly discouraged.
     */
    static <T> Validation<T> valid(T value) {
        return new Valid<>(value);
    }

    /**
     * Creates an invalid validation with the provided error messages.
     */
    @SuppressWarnings("unchecked")
    static <T> Validation<T> invalid(ErrorMessage error, ErrorMessage... moreErrors) {
        return (Validation<T>) new Invalid(List.of(error).appendAll(List.of(moreErrors)));
    }

    /**
     * Creates an invalid validation with a single error message key.
     */
    @SuppressWarnings("unchecked")
    static <T> Validation<T> invalid(String errorKey) {
        return (Validation<T>) new Invalid(List.of(ErrorMessage.of(errorKey)));
    }

    /**
     * Creates an invalid validation with the provided list of error messages.
     */
    @SuppressWarnings("unchecked")
    static <T> Validation<T> invalid(List<ErrorMessage> errors) {
        return (Validation<T>) new Invalid(errors);
    }
    //endregion

    //region factory methods for unknown values

    /**
     * Creates a {@link Validation} from a {@link Supplier}.
     * If the supplier throws a {@link ValidationException}, the returned validation will be invalid with the same errors
     * as the thrown exception.
     * If the supplier throws any other exception, the exception will be propagated. Use {@link Validation#fromCatchingAll(Supplier, ErrorMessage)} or {@link Validation#from(Try)} if you want to catch all possible exceptions.
     * This method is meant for interoperability with code that can throw {@link ValidationException}, for example
     * when using the "validate in constructor" pattern.
     */
    static <T> Validation<T> from(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        try {
            return Validation.valid(supplier.get());
        } catch (ValidationException e) {
            return Validation.invalid(e.errors());
        }
    }

    /**
     * Creates a {@link Validation} from a {@link Supplier}.
     * If the supplier throws a {@link ValidationException}, the returned validation will be invalid with the same errors
     * as the thrown exception.
     * If the supplier throws any other exception, the exception will also be converted to an Invalid, with the {@link ErrorMessage} created by the errorMessageMaker function.
     * This method is meant for interoperability with code that can throw any {@link Exception}, but is also somewhat dangerous
     * as it can hide issues like {@link NullPointerException} and so on.
     */
    static <T> Validation<T> fromCatchingAll(Supplier<? extends T> supplier, Function<Exception, ErrorMessage> errorMessageMaker) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        Objects.requireNonNull(errorMessageMaker, "errorMessageMaker cannot be null");
        try {
            return Validation.valid(supplier.get());
        } catch (ValidationException e) {
            return Validation.invalid(e.errors());
        } catch (Exception e) {
            return Validation.invalid(errorMessageMaker.apply(e));
        }
    }

    /**
     * Creates a {@link Validation} from a {@link Supplier}.
     * If the supplier throws a {@link ValidationException}, the returned validation will be invalid with the same errors
     * as the thrown exception.
     * If the supplier throws any other exception, the exception will also be converted to an Invalid, with the passed {@link ErrorMessage}
     * This method is meant for interoperability with code that can throw any {@link RuntimeException}, but is also somewhat dangerous
     * as it can hide issues like {@link NullPointerException} and so on.
     */
    static <T> Validation<T> fromCatchingAll(Supplier<? extends T> supplier, ErrorMessage errorMessage) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        try {
            return Validation.valid(supplier.get());
        } catch (ValidationException e) {
            return Validation.invalid(e.errors());
        } catch (Exception e) {
            return Validation.invalid(errorMessage);
        }
    }

    /**
     * Creates a {@link Validation} from a {@link Supplier}.
     * If the supplier throws a {@link ValidationException}, the returned validation will be invalid with the same errors
     * as the thrown exception.
     * If the supplier throws any other exception, the exception will also be converted to an Invalid, with the passed {@link ErrorMessage}
     * This method is meant for interoperability with code that can throw any {@link Exception}, but is also somewhat dangerous
     * as it can hide issues like {@link NullPointerException} and so on.
     */
    static <T> Validation<T> fromCatchingAll(Supplier<? extends T> supplier, String errorKey) {
        return fromCatchingAll(supplier, ErrorMessage.of(errorKey));
    }

    /**
     * Creates a {@link Validation} from a {@link Try}.
     * If the Try throws a {@link ValidationException}, the returned validation will be invalid with the same errors
     * as the thrown exception.
     * If the {@link Try} is successful, the returned validation will be valid with the value.
     * If the {@link Try} is failed, the returned validation will be invalid with the provided error message.
     */
    static <T> Validation<T> from(Try<? extends T> _try, ErrorMessage errorMessage) {
        Objects.requireNonNull(_try, "_try cannot be null");
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        return _try.fold(
                e -> (e instanceof ValidationException ve) ? Validation.invalid(ve.errors()) : Validation.invalid(errorMessage),
                Validation::valid
        );
    }

    /**
     * Creates a {@link Validation} from a {@link Try}.
     * If the {@link Try} is successful, the returned validation will be valid with the value.
     * If the {@link Try} is failed, the returned validation will be invalid with the provided error message.
     */
    static <T> Validation<T> from(Try<? extends T> _try, String errorKey) {
        return from(_try, ErrorMessage.of(errorKey));
    }

    /**
     * Creates a {@link Validation} from a {@link Try}.
     * If the Try failed with a {@link ValidationException}, the returned validation will be invalid with the same errors
     * as the thrown ValidationException.
     * If the {@link Try} is successful, the returned validation will be valid with the value.
     * If the {@link Try} is failed, the returned validation will be invalid with the error key "failed.from.try"
     * and param "message" representing the message of the exception
     */
    static <T> Validation<T> from(Try<? extends T> _try) {
        return _try.fold(
                e ->
                    (e instanceof ValidationException ve) ?
                    Validation.invalid(ve.errors()) :
                    Validation.invalid(ErrorMessage.of("failed.from.try", "message",e.getMessage()))
                ,
                Validation::valid
        );
    }

    /**
     * Creates a {@link Validation} from an {@link Option}.
     * If the {@link Option} is defined, the returned validation will be valid with the value.
     * If the {@link Option} is empty, the returned validation will be invalid with the provided error message.
     *
     */
    static <T> Validation<T> from(Option<? extends T> option, ErrorMessage errorMessage) {
        return option.fold(
                () -> Validation.invalid(errorMessage),
                Validation::valid
        );
    }

    /**
     * Creates a {@link Validation} from an {@link Option}.
     * If the {@link Option} is defined, the returned validation will be valid with the value.
     * If the {@link Option} is empty, the returned validation will be invalid with the provided error key.
     */
    static <T> Validation<T> from(Option<? extends T> option, String errorKey) {
        return from(option, ErrorMessage.of(errorKey));
    }

    /**
     * Creates a {@link Validation} from an {@link Option}.
     * If the {@link Option} is defined, the returned validation will be valid with the value.
     * If the {@link Option} is empty, the returned validation will be invalid with the default error message {@code "value.is.none"}.
     * <p>
     * Error key: {@code value.is.none}
     */
    static <T> Validation<T> from(Option<? extends T> option) {
        return from(option, "value.is.none");
    }

    /**
     * Creates a {@link Validation} from an {@link Either}.
     * If the {@link Either} is right, the returned validation will be valid with the value.
     * If the {@link Either} is left, the returned validation will be invalid with the error message mapped from the left value.
     */
    static <L, R> Validation<R> from(Either<L, ? extends R> either, Function1<? super L, ErrorMessage> errorMapper) {
        return either.fold(
                l -> Validation.invalid(errorMapper.apply(l)),
                Validation::valid
        );
    }

    /**
     * Creates a {@link Validation} from a standard Java {@link java.util.Optional}.
     * If the optional is present, the returned validation will be valid with the value.
     * If the optional is empty, the returned validation will be invalid with the default error message {@code "value.is.none"}.
     *
     */
    static <T> Validation<T> from(Optional<? extends T> optional) {
        return from(Option.ofOptional(optional));
    }

    /**
     * Creates a {@link Validation} from a standard Java {@link java.util.Optional}.
     * If the optional is present, the returned validation will be valid with the value.
     * If the optional is empty, the returned validation will be invalid with the provided error key.
     */
    static <T> Validation<T> from(Optional<? extends T> optional, String errorKey) {
        return from(Option.ofOptional(optional), errorKey);
    }

    /**
     * Creates a {@link Validation} from a standard Java {@link java.util.Optional}.
     * If the optional is present, the returned validation will be valid with the value.
     * If the optional is empty, the returned validation will be invalid with the provided error message.
     */
    static <T> Validation<T> from(Optional<? extends T> optional, ErrorMessage errorMessage) {
        return from(Option.ofOptional(optional), errorMessage);
    }

    //endregion

    //region casting

    /**
     * Narrows a {@code Validation<? extends T>} to a {@code Validation<T>}.
     *
     */
    @SuppressWarnings("unchecked")
    static <T> Validation<T> narrow(Validation<? extends T> validation) {
        return (Validation<T>) validation;
    }

    /**
     * Narrows a {@code Validation<? super T>} to a {@code Validation<T>}.
     */
    @SuppressWarnings("unchecked")
    static <T> Validation<T> narrowSuper(Validation<? super T> validation) {
        return (Validation<T>) validation;
    }
    //endregion

    //region Value methods

    /**
     * Executes the given action on the contained value if this {@code Validation} is valid;
     * otherwise, does nothing.
     */
    default Validation<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action cannot be null");
        if (isValid()) {
            action.accept(((Valid<T>) this).value());
        }
        return this;
    }

    /**
     * If this validation is valid, return a new Valid with the passed value.
     * If this validation is already invalid, returns it unchanged.
     */
    default <U> Validation<U> mapTo(U value) {
        return map(ignored -> value);
    }

    //endregion

    /**
     * Represents a successful validation.
     */
    record Valid<T>(T value) implements Validation<T> {

        @Override
        public List<ErrorMessage> errors() {
            return List.of();
        }

        @Override
        public boolean isValid() {
            return true;
        }

        public Iterator<T> iterator() {
            return Iterator.of(value);
        }
    }

    /**
     * Represents an invalid validation.
     *
     * @param errors the list of error messages that describe the validation failure. Errors cannot be empty and will be deduplicated.
     */
    record Invalid(List<ErrorMessage> errors) implements Validation<Object> {

        public Invalid {
            Objects.requireNonNull(errors, "errors cannot be null");
            if (errors.isEmpty()) {
                throw new IllegalArgumentException("errors must be non-empty");
            }
            errors = errors.distinct();
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public Iterator<Object> iterator() {
            return Iterator.empty();
        }
    }
}
