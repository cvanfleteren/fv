package be.iffy.fv;

import io.vavr.*;
import io.vavr.collection.Iterator;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;

import java.util.NoSuchElementException;
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
 * @param <T> the type of the value being validated.
 */
public sealed interface Validation<T> extends Value<T> {

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
     * Returns the error messages associated with this validation.
     *
     * @return a {@link List} of {@link ErrorMessage} objects.
     */
    List<ErrorMessage> errors();

    //region value retrieval

    /**
     * Returns the valid value, or throws {@link ValidationException} if this validation is invalid.
     *
     * <p>Example: successful retrieval
     * {@snippet file="be/iffy/fv/ValidationSnippets.java" region="getOrElseThrow_success"}
     *
     * <p>Example: handling validation failure
     * {@snippet file="be/iffy/fv/ValidationSnippets.java" region="getOrElseThrow_failure"}
     *
     * @return the valid value.
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
     *
     * <p>Example:
     * {@snippet file="be/iffy/fv/ValidationSnippets.java" region="getOrElse"}
     */
    default T getOrElse(T defaultValue) {
        return switch (this) {
            case Valid(var value) -> value;
            default -> defaultValue;
        };
    }

    /**
     * Returns this validation if it is valid; otherwise returns the specified alternative validation.
     *
     * <p>Example:
     * {@snippet file="be/iffy/fv/ValidationSnippets.java" region="orElse_value"}
     *
     * @param other the alternative validation.
     * @param <U>   the common type.
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
     * <p>Example:
     * {@snippet file="be/iffy/fv/ValidationSnippets.java" region="orElse_supplier"}
     *
     * @param supplier the alternative validation supplier.
     * @param <U>      the common type.
     */
    @SuppressWarnings("unchecked")
    default <U> Validation<U> orElse(Supplier<? extends Validation<? extends U>> supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        return isValid() ? (Validation<U>) this : Validation.narrow(supplier.get());
    }

    /**
     * Executes the given consumer on the contained value if this {@code Validation} is valid;
     * otherwise, does nothing. This method is an alias for {@link #peek(Consumer)}.
     *
     * <p>Example:
     * {@snippet file="be/iffy/fv/ValidationSnippets.java" region="whenValid"}
     *
     * @param consumer a consumer to apply to the contained value.
     * @return this {@code Validation} instance.
     * @throws NullPointerException if {@code consumer} is null.
     * @see #peek(Consumer)
     */
    default Validation<T> whenValid(Consumer<T> consumer) {
        return peek(consumer);
    }

    /**
     * Executes the given consumer on the contained errors if this {@code Validation} is invalid;
     * otherwise, does nothing.
     *
     * <p>Example:
     * {@snippet file="be/iffy/fv/ValidationSnippets.java" region="whenInvalid"}
     *
     * @param consumer a consumer to apply to the contained errors.
     * @return this {@code Validation} instance.
     * @throws NullPointerException if {@code consumer} is null.
     */
    default Validation<T> whenInvalid(Consumer<List<ErrorMessage>> consumer) {
        if (!isValid()) {
            consumer.accept(errors());
        }
        return this;
    }

    //endregion

    /**
     * Returns the error messages as a standard Java {@link java.util.List}.
     */
    default java.util.List<ErrorMessage> javaErrors() {
        return errors().asJava();
    }
    //endregion

    //region common functional operations on single validations

    /**
     * Transforms the valid value using the provided mapper.
     * {@snippet file="be/iffy/fv/ValidationSnippets.java" region="map"}
     *
     * @param mapper the function to apply to the valid value.
     * @param <R>    the type of the result of the mapper function.
     * @return a new {@link Validation} containing the mapped value if valid, or the original errors if invalid.
     */
    default <R> Validation<R> map(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return switch (this) {
            case Valid(var value) -> new Valid<>(mapper.apply(value));
            default -> (Validation<R>) this;
        };
    }

    /**
     * Like {@link #map(Function)}, but catches runtime exceptions thrown by the mapper and turns them into an invalid validation.
     * So this method does NOT have pure map semantics but is an easy alternative to having to flatMap and handle errors yourself.
     * <p>
     * <b>This method is somewhat dangerous because if the mapper function starts throwing totally unexpected Exceptions, they might get buried as "failed validations".</b>
     * <p>
     * {@link ValidationException}s that are thrown are handled cleanly, accumulating the errors present.
     *
     * <p>Example: successful mapping
     * {@snippet file="be/iffy/fv/ValidationSnippets.java" region="mapCatching_success"}
     *
     * <p>Example: mapping that throws a RuntimeException
     * {@snippet file="be/iffy/fv/ValidationSnippets.java" region="mapCatching_runtimeException"}
     *
     * @param mapper the function to apply to the valid value.
     * @param <R>    the type of the result of the mapper function.
     * @return a new {@link Validation} containing the mapped value if valid, or this if invalid
     */
    default <R> Validation<R> mapCatching(Function<? super T, ? extends R> mapper) {
        return mapCatching(mapper, "could.not.be.mapped");
    }

    /**
     * Like {@link #map(Function)}, but catches runtime exceptions thrown by the mapper and turns them into an invalid validation.
     * So this method does NOT have pure map semantics but is an easy alternative to having to flatMap and handle errors yourself.
     * <p>
     * <b>This method is somewhat dangerous because if the mapper function starts throwing totally unexpected Exceptions, they might get buried as "failed validations".</b>
     * <p>
     * {@link ValidationException}s that are thrown are handled cleanly, accumulating the errors present.
     *
     * <p>Example: mapping that throws a RuntimeException with custom error message
     * {@snippet file="be/iffy/fv/ValidationSnippets.java" region="mapCatching_customError"}
     *
     * @param mapper       the function to apply to the valid value.
     * @param errorMessage the error message key to use if a {@link RuntimeException} is caught.
     * @param <R>          the type of the result of the mapper function.
     * @return a new {@link Validation} containing the mapped value if valid, or this if invalid
     */
    default <R> Validation<R> mapCatching(Function<? super T, ? extends R> mapper, String errorMessage) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        return switch (this) {
            case Valid(var value) -> {
                try {
                    yield new Valid<>(mapper.apply(value));
                } catch (ValidationException e) {
                    yield Validation.invalid(e.errors());
                } catch (RuntimeException e) {
                    yield Validation.invalid(errorMessage);
                }
            }
            default -> (Validation<R>) this;
        };
    }

    /**
     * Maps a valid value to a new validation, or returns this if invalid.
     * {@snippet file="be/iffy/fv/ValidationSnippets.java" region="flatMap"}
     *
     * @param flatMapper the function to apply to the valid value.
     * @param <R>        the type of the result of the flatMapper function.
     * @return the result of the flatMapper, or this if invalid.
     */
    default <R> Validation<R> flatMap(Function1<? super T, Validation<? extends R>> flatMapper) {
        Objects.requireNonNull(flatMapper, "flatMapper cannot be null");
        return switch (this) {
            case Valid(var value) -> Validation.narrow(flatMapper.apply(value));
            default -> (Validation<R>) this;
        };
    }

    /**
     * Like {@link #flatMap(Function1)}, but also catches runtime exceptions thrown by the mapper and turns them into an invalid validation.
     * <p>
     * <b>This method is somewhat dangerous because if the mapper function starts throwing totally unexpected Exceptions, they might get buried as "failed validations".</b>
     * <p>
     * {@link ValidationException}s that are thrown are handled cleanly, accumulating the errors present.
     *
     * <p>Example: successful flatMapping
     * {@snippet file="be/iffy/fv/ValidationSnippets.java" region="flatMapCatching_success"}
     *
     * <p>Example: flatMapping that throws a RuntimeException
     * {@snippet file="be/iffy/fv/ValidationSnippets.java" region="flatMapCatching_runtimeException"}
     *
     * @param flatMapper the function to apply to the valid value.
     * @param <R>        the type of the result of the flatMapper function.
     */
    default <R> Validation<R> flatMapCatching(Function1<? super T, Validation<? extends R>> flatMapper) {
        return flatMapCatching(flatMapper, "could.not.be.mapped");
    }

    /**
     * Like {@link #flatMap(Function1)}, but catches runtime exceptions thrown by the mapper and turns them into an invalid validation.
     * <p>
     * <b>This method is somewhat dangerous because if the mapper function starts throwing totally unexpected Exceptions, they might get buried as "failed validations".</b>
     * <p>
     * {@link ValidationException}s that are thrown are handled cleanly, accumulating the errors present.
     *
     * <p>Example: flatMapping that throws a RuntimeException with custom error message
     * {@snippet file="be/iffy/fv/ValidationSnippets.java" region="flatMapCatching_customError"}
     *
     * @param flatMapper   the function to apply to the valid value.
     * @param errorMessage the error message key to use if a {@link RuntimeException} is caught.
     * @param <R>          the type of the result of the flatMapper function.
     */
    default <R> Validation<R> flatMapCatching(Function1<? super T, Validation<? extends R>> flatMapper, String errorMessage) {
        Objects.requireNonNull(flatMapper, "flatMapper cannot be null");
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        return switch (this) {
            case Valid(var value) -> {
                try {
                    yield Validation.narrow(flatMapper.apply(value));
                } catch (ValidationException e) {
                    yield Validation.invalid(e.errors());
                } catch (RuntimeException e) {
                    yield Validation.invalid(errorMessage);
                }
            }
            default -> (Validation<R>) this;
        };
    }

    /**
     * Folds this validation into a single value by applying one of two functions.
     * {@snippet file="be/iffy/fv/ValidationSnippets.java" region="fold"}
     *
     * @param whenInvalid the function to apply if this validation is invalid.
     * @param whenValid   the function to apply if this validation is valid.
     * @param <R>         the type of the result.
     * @return the result of applying the appropriate function.
     */
    default <R> R fold(Function1<List<ErrorMessage>, ? extends R> whenInvalid, Function1<? super T, ? extends R> whenValid) {
        Objects.requireNonNull(whenInvalid, "whenInvalid cannot be null");
        Objects.requireNonNull(whenValid, "whenValid cannot be null");
        return switch (this) {
            case Valid(var value) -> whenValid.apply(value);
            case Invalid(var errors) -> whenInvalid.apply(errors);
        };
    }

    /**
     * Refines this validation allowing a Rule to test its value.
     * If the resulting Validation is valid, the original Validation is returned.
     * If the resulting Validation is invalid, that Validation is returned with the refined error messages.
     *
     * @param refinement the rule to apply to the validation.
     * @return a new {@link Validation} containing the refined result.
     */
    default Validation<T> refine(Rule<? super T> refinement) {
        return narrowSuper(this.flatMap(refinement::test));
    }

    /**
     * Applies a filter to the current validation logic using the specified predicate and error message.
     * This method refines the validation by adding a rule that fails if the provided predicate fails.
     *
     * @param predicate    the condition to be checked against the input
     * @param errorMessage the error message to be returned if the predicate fails
     * @return a new Validation instance with the applied filter
     */
    default Validation<T> filter(Predicate<? super T> predicate, ErrorMessage errorMessage) {
        return refine(Rule.of(predicate, errorMessage));
    }

    /**
     * Applies a filter to the current validation logic using the specified predicate and error message key.
     * This method refines the validation by adding a rule that fails if the provided predicate fails.
     *
     * @param predicate the condition to be checked against the input
     * @param errorKey  the error message key to be returned if the predicate fails
     * @return a new Validation instance with the applied filter
     */
    default Validation<T> filter(Predicate<? super T> predicate, String errorKey) {
        return filter(predicate, ErrorMessage.of(errorKey));
    }
    //endregion

    //region Error handling

    /**
     * Maps the error messages of an invalid validation using the provided mapper function.
     * If this validation is valid, the mapper is not applied.
     *
     * @param mapper the function to apply to the list of error messages.
     * @return a new {@link Validation} containing the mapped errors if invalid, or the original valid value.
     */
    default Validation<T> mapErrors(Function1<List<ErrorMessage>, List<ErrorMessage>> mapper) {
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
    //endregion

    //region common functional operations on multiple validations

    /**
     * Transforms a {@link Seq} of {@link Validation}s into a single {@code Validation} of a {@link List}.
     * If any validation is invalid, the result will contain all accumulated errors.
     * <p>
     * Usage example:
     * {@snippet file="be/iffy/fv/ValidationSnippets.java" region="sequence_seq"}
     *
     * @param validations the sequence of validations to sequence.
     * @param <T>         the value type.
     * @return a {@code Validation} containing a list of values if all are valid, or all errors if any are invalid.
     */
    static <T> Validation<List<T>> sequence(Seq<? extends Validation<? extends T>> validations) {
        return validations
                .zipWithIndex()
                .foldLeft(
                        Validation.valid(List.empty()),
                        (acc, validationWithIndex) -> {
                            if (acc instanceof Valid<List<T>>(var list)) {
                                Validation<T> v = Validation.narrow(validationWithIndex._1);
                                return switch (v) {
                                    case Valid<T>(var value) -> Validation.valid(list.append(value));
                                    case Invalid(var errors) ->
                                            Validation.invalid(errors.map(error -> error.atIndex(validationWithIndex._2)));
                                };
                            } else {
                                Validation<T> v = Validation.narrow(validationWithIndex._1);
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
     * Transforms a {@link java.util.Collection} of {@link Validation}s into a single {@code Validation} of a {@link java.util.List}.
     * If any validation is invalid, the result will contain all accumulated errors.
     * <p>
     * Usage example:
     * {@snippet file="be/iffy/fv/ValidationSnippets.java" region="sequence_collection"}
     *
     * @param validations the collection of validations to sequence.
     * @param <T>         the value type.
     * @return a {@code Validation} containing a list of values if all are valid, or all errors if any are invalid.
     */
    static <T> Validation<java.util.List<T>> sequence(java.util.Collection<Validation<T>> validations) {
        return sequence(List.ofAll(validations))
                .map(List::asJava);
    }
    //endregion

    //region mapN / flatMapN

    /**
     * Maps two {@link Validation}s using the provided mapper function.
     * If all validations are valid, the result is a valid {@link Validation} containing the mapped value.
     * If any validation is invalid, the result is an invalid {@link Validation} containing all error messages.
     *
     * @param v1     the first validation.
     * @param v2     the second validation.
     * @param mapper the mapper function.
     * @param <R>    the result type.
     * @param <T1>   the value type of the first validation.
     * @param <T2>   the value type of the second validation.
     * @return a new {@link Validation} instance.
     * @throws NullPointerException if any argument is null.
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
     *
     * @param v1     the first validation.
     * @param v2     the second validation.
     * @param mapper the flatMapper function.
     * @param <R>    the result type.
     * @param <T1>   the value type of the first validation.
     * @param <T2>   the value type of the second validation.
     * @return a new {@link Validation} instance.
     * @throws NullPointerException if any argument is null.
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
     *
     * @param v1     the first validation.
     * @param v2     the second validation.
     * @param v3     the third validation.
     * @param mapper the mapper function.
     * @param <R>    the result type.
     * @param <T1>   the value type of the first validation.
     * @param <T2>   the value type of the second validation.
     * @param <T3>   the value type of the third validation.
     * @return a new {@link Validation} instance.
     * @throws NullPointerException if any argument is null.
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
     *
     * @param v1     the first validation.
     * @param v2     the second validation.
     * @param v3     the third validation.
     * @param mapper the flatMapper function.
     * @param <R>    the result type.
     * @param <T1>   the value type of the first validation.
     * @param <T2>   the value type of the second validation.
     * @param <T3>   the value type of the third validation.
     * @return a new {@link Validation} instance.
     * @throws NullPointerException if any argument is null.
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
     *
     * @param v1     the first validation.
     * @param v2     the second validation.
     * @param v3     the third validation.
     * @param v4     the fourth validation.
     * @param mapper the mapper function.
     * @param <R>    the result type.
     * @param <T1>   the value type of the first validation.
     * @param <T2>   the value type of the second validation.
     * @param <T3>   the value type of the third validation.
     * @param <T4>   the value type of the fourth validation.
     * @return a new {@link Validation} instance.
     * @throws NullPointerException if any argument is null.
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
     *
     * @param v1     the first validation.
     * @param v2     the second validation.
     * @param v3     the third validation.
     * @param v4     the fourth validation.
     * @param mapper the flatMapper function.
     * @param <R>    the result type.
     * @param <T1>   the value type of the first validation.
     * @param <T2>   the value type of the second validation.
     * @param <T3>   the value type of the third validation.
     * @param <T4>   the value type of the fourth validation.
     * @return a new {@link Validation} instance.
     * @throws NullPointerException if any argument is null.
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
     *
     * @param v1     the first validation.
     * @param v2     the second validation.
     * @param v3     the third validation.
     * @param v4     the fourth validation.
     * @param v5     the fifth validation.
     * @param mapper the mapper function.
     * @param <R>    the result type.
     * @param <T1>   the value type of the first validation.
     * @param <T2>   the value type of the second validation.
     * @param <T3>   the value type of the third validation.
     * @param <T4>   the value type of the fourth validation.
     * @param <T5>   the value type of the fifth validation.
     * @return a new {@link Validation} instance.
     * @throws NullPointerException if any argument is null.
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
     *
     * @param v1     the first validation.
     * @param v2     the second validation.
     * @param v3     the third validation.
     * @param v4     the fourth validation.
     * @param v5     the fifth validation.
     * @param mapper the flatMapper function.
     * @param <R>    the result type.
     * @param <T1>   the value type of the first validation.
     * @param <T2>   the value type of the second validation.
     * @param <T3>   the value type of the third validation.
     * @param <T4>   the value type of the fourth validation.
     * @param <T5>   the value type of the fifth validation.
     * @return a new {@link Validation} instance.
     * @throws NullPointerException if any argument is null.
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
     *
     * @param v1     the first validation.
     * @param v2     the second validation.
     * @param v3     the third validation.
     * @param v4     the fourth validation.
     * @param v5     the fifth validation.
     * @param v6     the sixth validation.
     * @param mapper the mapper function.
     * @param <R>    the result type.
     * @param <T1>   the value type of the first validation.
     * @param <T2>   the value type of the second validation.
     * @param <T3>   the value type of the third validation.
     * @param <T4>   the value type of the fourth validation.
     * @param <T5>   the value type of the fifth validation.
     * @param <T6>   the value type of the sixth validation.
     * @return a new {@link Validation} instance.
     * @throws NullPointerException if any argument is null.
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
     *
     * @param v1     the first validation.
     * @param v2     the second validation.
     * @param v3     the third validation.
     * @param v4     the fourth validation.
     * @param v5     the fifth validation.
     * @param v6     the sixth validation.
     * @param mapper the flatMapper function.
     * @param <R>    the result type.
     * @param <T1>   the value type of the first validation.
     * @param <T2>   the value type of the second validation.
     * @param <T3>   the value type of the third validation.
     * @param <T4>   the value type of the fourth validation.
     * @param <T5>   the value type of the fifth validation.
     * @param <T6>   the value type of the sixth validation.
     * @return a new {@link Validation} instance.
     * @throws NullPointerException if any argument is null.
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
     *
     * @param v1     the first validation.
     * @param v2     the second validation.
     * @param v3     the third validation.
     * @param v4     the fourth validation.
     * @param v5     the fifth validation.
     * @param v6     the sixth validation.
     * @param v7     the seventh validation.
     * @param mapper the mapper function.
     * @param <R>    the result type.
     * @param <T1>   the value type of the first validation.
     * @param <T2>   the value type of the second validation.
     * @param <T3>   the value type of the third validation.
     * @param <T4>   the value type of the fourth validation.
     * @param <T5>   the value type of the fifth validation.
     * @param <T6>   the value type of the sixth validation.
     * @param <T7>   the value type of the seventh validation.
     * @return a new {@link Validation} instance.
     * @throws NullPointerException if any argument is null.
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
     *
     * @param v1     the first validation.
     * @param v2     the second validation.
     * @param v3     the third validation.
     * @param v4     the fourth validation.
     * @param v5     the fifth validation.
     * @param v6     the sixth validation.
     * @param v7     the seventh validation.
     * @param mapper the flatMapper function.
     * @param <R>    the result type.
     * @param <T1>   the value type of the first validation.
     * @param <T2>   the value type of the second validation.
     * @param <T3>   the value type of the third validation.
     * @param <T4>   the value type of the fourth validation.
     * @param <T5>   the value type of the fifth validation.
     * @param <T6>   the value type of the sixth validation.
     * @param <T7>   the value type of the seventh validation.
     * @return a new {@link Validation} instance.
     * @throws NullPointerException if any argument is null.
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
     *
     * @param v1     the first validation.
     * @param v2     the second validation.
     * @param v3     the third validation.
     * @param v4     the fourth validation.
     * @param v5     the fifth validation.
     * @param v6     the sixth validation.
     * @param v7     the seventh validation.
     * @param v8     the eighth validation.
     * @param mapper the mapper function.
     * @param <R>    the result type.
     * @param <T1>   the value type of the first validation.
     * @param <T2>   the value type of the second validation.
     * @param <T3>   the value type of the third validation.
     * @param <T4>   the value type of the fourth validation.
     * @param <T5>   the value type of the fifth validation.
     * @param <T6>   the value type of the sixth validation.
     * @param <T7>   the value type of the seventh validation.
     * @param <T8>   the value type of the eighth validation.
     * @return a new {@link Validation} instance.
     * @throws NullPointerException if any argument is null.
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
     *
     * @param v1     the first validation.
     * @param v2     the second validation.
     * @param v3     the third validation.
     * @param v4     the fourth validation.
     * @param v5     the fifth validation.
     * @param v6     the sixth validation.
     * @param v7     the seventh validation.
     * @param v8     the eighth validation.
     * @param mapper the flatMapper function.
     * @param <R>    the result type.
     * @param <T1>   the value type of the first validation.
     * @param <T2>   the value type of the second validation.
     * @param <T3>   the value type of the third validation.
     * @param <T4>   the value type of the fourth validation.
     * @param <T5>   the value type of the fifth validation.
     * @param <T6>   the value type of the sixth validation.
     * @param <T7>   the value type of the seventh validation.
     * @param <T8>   the value type of the eighth validation.
     * @return a new {@link Validation} instance.
     * @throws NullPointerException if any argument is null.
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
     *
     * @param value the value.
     * @param <T>   the value type.
     * @return a valid {@link Validation} instance.
     */
    static <T> Validation<T> valid(T value) {
        return new Valid<>(value);
    }

    /**
     * Creates an invalid validation with the provided error messages.
     *
     * @param error      the first error message.
     * @param moreErrors additional error messages.
     * @param <T>        the result type.
     * @return an invalid {@link Validation} instance.
     */
    @SuppressWarnings("unchecked")
    static <T> Validation<T> invalid(ErrorMessage error, ErrorMessage... moreErrors) {
        return (Validation<T>) new Invalid(List.of(error).appendAll(List.of(moreErrors)));
    }

    /**
     * Creates an invalid validation with a single error message key.
     *
     * @param errorMessage the error message key.
     * @param <T>          the result type.
     * @return an invalid {@link Validation} instance.
     */
    @SuppressWarnings("unchecked")
    static <T> Validation<T> invalid(String errorMessage) {
        return (Validation<T>) new Invalid(List.of(ErrorMessage.of(errorMessage)));
    }

    /**
     * Creates an invalid validation with the provided list of error messages.
     *
     * @param errors the list of error messages.
     * @param <T>    the result type.
     * @return an invalid {@link Validation} instance.
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
     * If the supplier throws any other exception, the exception will be propagated.
     * This method is meant for interoperability with code that can throw {@link ValidationException}, for example
     * when using the "validate in constructor" pattern.
     *
     * @param supplier the supplier of the value.
     * @param <T>      the value type.
     * @return a {@link Validation} instance.
     */
    static <T> Validation<T> from(Supplier<? extends T> supplier) {
        try {
            return Validation.valid(supplier.get());
        } catch (ValidationException e) {
            return Validation.invalid(e.errors());
        }
    }

    /**
     * Creates a {@link Validation} from a {@link Try}.
     * If the {@link Try} is successful, the returned validation will be valid with the value.
     * If the {@link Try} is failed, the returned validation will be invalid with the provided error message.
     *
     * @param _try         the try instance.
     * @param errorMessage the error message to use if failed.
     * @param <T>          the value type.
     * @return a {@link Validation} instance.
     */
    static <T> Validation<T> from(Try<? extends T> _try, ErrorMessage errorMessage) {
        return _try.fold(
                ignored -> Validation.invalid(errorMessage),
                Validation::valid
        );
    }

    /**
     * Creates a {@link Validation} from a {@link Try}.
     * If the {@link Try} is successful, the returned validation will be valid with the value.
     * If the {@link Try} is failed, the returned validation will be invalid with the message of the thrown exception.
     *
     * @param _try the try instance.
     * @param <T>  the value type.
     * @return a {@link Validation} instance.
     */
    static <T> Validation<T> from(Try<? extends T> _try) {
        return _try.fold(
                e -> (e instanceof ValidationException ve) ? Validation.invalid(ve.errors()) : Validation.invalid(e.getMessage()),
                Validation::valid
        );
    }

    /**
     * Creates a {@link Validation} from an {@link Option}.
     * If the {@link Option} is defined, the returned validation will be valid with the value.
     * If the {@link Option} is empty, the returned validation will be invalid with the provided error message.
     *
     * @param option       the option instance.
     * @param errorMessage the error message to use if empty.
     * @param <T>          the value type.
     * @return a {@link Validation} instance.
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
     * If the {@link Option} is empty, the returned validation will be invalid with the provided error message key.
     *
     * @param option       the option instance.
     * @param errorMessage the error message key to use if empty.
     * @param <T>          the value type.
     * @return a {@link Validation} instance.
     */
    static <T> Validation<T> from(Option<? extends T> option, String errorMessage) {
        return from(option, ErrorMessage.of(errorMessage));
    }

    /**
     * Creates a {@link Validation} from an {@link Option}.
     * If the {@link Option} is defined, the returned validation will be valid with the value.
     * If the {@link Option} is empty, the returned validation will be invalid with the default error message {@code "value.is.none"}.
     *
     * @param option the option instance.
     * @param <T>    the value type.
     * @return a {@link Validation} instance.
     */
    static <T> Validation<T> from(Option<? extends T> option) {
        return from(option, "value.is.none");
    }

    /**
     * Creates a {@link Validation} from an {@link Either}.
     * If the {@link Either} is right, the returned validation will be valid with the value.
     * If the {@link Either} is left, the returned validation will be invalid with the error message mapped from the left value.
     *
     * @param either      the either instance.
     * @param errorMapper the function to map the left value to an error message.
     * @param <L>         the left type.
     * @param <R>         the right type (value type).
     * @return a {@link Validation} instance.
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
     * @param optional the optional instance.
     * @param <T>      the value type.
     * @return a {@link Validation} instance.
     */
    static <T> Validation<T> from(Optional<? extends T> optional) {
        return from(Option.ofOptional(optional));
    }

    /**
     * Creates a {@link Validation} from a standard Java {@link java.util.Optional}.
     * If the optional is present, the returned validation will be valid with the value.
     * If the optional is empty, the returned validation will be invalid with the provided error message key.
     *
     * @param optional     the optional instance.
     * @param errorMessage the error message key to use if empty.
     * @param <T>          the value type.
     * @return a {@link Validation} instance.
     */
    static <T> Validation<T> from(Optional<? extends T> optional, String errorMessage) {
        return from(Option.ofOptional(optional), errorMessage);
    }

    /**
     * Creates a {@link Validation} from a standard Java {@link java.util.Optional}.
     * If the optional is present, the returned validation will be valid with the value.
     * If the optional is empty, the returned validation will be invalid with the provided error message.
     *
     * @param optional     the optional instance.
     * @param errorMessage the error message to use if empty.
     * @param <T>          the value type.
     * @return a {@link Validation} instance.
     */
    static <T> Validation<T> from(Optional<? extends T> optional, ErrorMessage errorMessage) {
        return from(Option.ofOptional(optional), errorMessage);
    }

    //endregion

    //region casting

    /**
     * Narrows a {@code Validation<? extends T>} to a {@code Validation<T>}.
     *
     * @param validation the validation to narrow.
     * @param <T>        the target type.
     * @return the narrowed validation.
     */
    @SuppressWarnings("unchecked")
    static <T> Validation<T> narrow(Validation<? extends T> validation) {
        return (Validation<T>) validation;
    }

    /**
     * Narrows a {@code Validation<? super T>} to a {@code Validation<T>}.
     *
     * @param validation the validation to narrow.
     * @param <T>        the target type.
     * @return the narrowed validation.
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
     *
     * @param action a consumer to apply to the contained value.
     * @return this {@code Validation} instance.
     * @throws NullPointerException if {@code action} is null.
     */
    @Override
    default Validation<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action cannot be null");
        if (isValid()) {
            action.accept(get());
        }
        return this;
    }

    @Override
    default <U> Validation<U> mapTo(U value) {
        return map(ignored -> value);
    }

    @Override
    default boolean isAsync() {
        return false;
    }

    @Override
    default boolean isEmpty() {
        return !isValid();
    }

    @Override
    default boolean isLazy() {
        return false;
    }

    @Override
    default boolean isSingleValued() {
        return true;
    }

    //endregion

    /**
     * Represents a successful validation.
     */
    record Valid<T>(T value) implements Validation<T> {
        public Valid {
            Objects.requireNonNull(value, "value cannot be null");
        }

        @Override
        public List<ErrorMessage> errors() {
            return List.of();
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public String stringPrefix() {
            return "Valid";
        }

        @Override
        public Iterator<T> iterator() {
            return Iterator.of(value);
        }
    }

    /**
     * Represents an invalid validation.
     *
     * @param errors the list of error messages that describe the validation failure.
     */
    record Invalid(List<ErrorMessage> errors) implements Validation<Object> {

        public Invalid {
            Objects.requireNonNull(errors, "errors cannot be null");
            if (errors.isEmpty()) {
                throw new IllegalStateException("errors cannot be empty");
            }
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public Iterator<Object> iterator() {
            return Iterator.empty();
        }

        @Override
        public String stringPrefix() {
            return "Invalid";
        }

        @Override
        public Object get() {
            throw new NoSuchElementException("No value present");
        }
    }
}
