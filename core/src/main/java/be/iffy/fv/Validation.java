package be.iffy.fv;

import io.vavr.collection.Iterator;
import io.vavr.collection.List;
import io.vavr.control.Option;

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
 * <h2>Exception handling</h2>
 *
 * <p>Most {@code Validation} operations do not catch unexpected runtime exceptions.
 * Methods such as {@link #map(Function)}, {@link #flatMap(Function)},
 * {@code mapN(...)} and {@code flatMapN(...)} are intended for regular functional
 * composition. If a mapper throws, the exception is propagated.
 * <p>
 * Use methods named {@code mapCatching}, {@code flatMapCatching}, or {@code fromCatching}
 * when you intentionally want to convert {@link ValidationException} into validation errors.
 * <p>
 * Use methods whose names contain {@code CatchingAll} when you intentionally want to convert
 * {@link ValidationException} and other {@link Exception}s thrown by user code into validation errors.
 * These methods do not catch {@link Error}s.
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
     * Converts this validation into a Java {@link Optional}.
     * Returns {@code Optional.of(value)} when valid, otherwise {@code Optional.empty()}.
     */
    default Optional<T> toOptional() {
        return this.fold(
                e -> Optional.empty(),
                Optional::ofNullable
        );
    }

    /**
     * Converts this validation into a Vavr {@link Option}.
     * Returns {@code Some(value)} when valid, otherwise {@code None}.
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
     * Like {@link #map(Function)}, but catches only {@link ValidationException}
     * and returns an invalid validation with the exception's errors.
     * Other {@link RuntimeException}s are propagated.
     *
     * <p>This method does not have pure map semantics.
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
     * Does not catch {@link Error}s.
     */
    default <R> Validation<R> mapCatchingAll(Function<? super T, ? extends R> mapper, ErrorMessage errorMessage) {
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
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
                    yield Validation.invalid(
                            Objects.requireNonNull(errorMessageMaker.apply(e), "errorMessageMaker result cannot be null")
                    );
                }
            }
            default -> (Validation<R>) this;
        };
    }

    /**
     * Maps a valid value to a new validation, or returns this if invalid.  If the mapper throws any {@link RuntimeException}s, they will be rethrown.
     * Use {@link #flatMapCatching(Function)} if you want to handle {@link ValidationException}s thrown by the mapper.
     */
    default <R> Validation<R> flatMap(Function<? super T, ? extends Validation<? extends R>> flatMapper) {
        Objects.requireNonNull(flatMapper, "flatMapper cannot be null");
        return switch (this) {
            case Valid(var value) -> Validation.narrow(
                    Objects.requireNonNull(flatMapper.apply(value),"flatMapper cannot return null Validation")
            );
            default -> (Validation<R>) this;
        };
    }

    /**
     * Like {@link #flatMap(Function)}, but catches {@link ValidationException}s thrown by the mapper and turns them into an invalid validation.
     */
    default <R> Validation<R> flatMapCatching(Function<? super T, ? extends Validation<? extends R>> flatMapper) {
        Objects.requireNonNull(flatMapper, "flatMapper cannot be null");
        return switch (this) {
            case Valid(var value) -> {
                try {
                    yield Validation.narrow(
                            Objects.requireNonNull(flatMapper.apply(value),"flatMapper cannot return null Validation")
                    );
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
     * Does not catch {@link Error}s.
     */
    default <R> Validation<R> flatMapCatchingAll(Function<? super T, ? extends Validation<? extends R>> flatMapper, ErrorMessage errorMessage) {
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        return flatMapCatchingAll(flatMapper, e -> errorMessage);
    }

    /**
     * Like {@link #flatMapCatchingAll(Function, ErrorMessage)}, but uses the provided error key.
     */
    default <R> Validation<R> flatMapCatchingAll(Function<? super T, ? extends Validation<? extends R>> flatMapper, String errorKey) {
        return flatMapCatchingAll(flatMapper, ErrorMessage.of(errorKey));
    }

    /**
     * Like {@link #flatMapCatchingAll(Function, ErrorMessage)}, but uses the provided mapper to create an {@link ErrorMessage}.
     */
    default <R> Validation<R> flatMapCatchingAll(Function<? super T, ? extends Validation<? extends R>> flatMapper, Function<Exception, ErrorMessage> errorMessageMaker) {
        Objects.requireNonNull(flatMapper, "flatMapper cannot be null");
        Objects.requireNonNull(errorMessageMaker, "errorMessageMaker cannot be null");
        return switch (this) {
            case Valid(var value) -> {
                try {
                    yield Validation.narrow(
                            Objects.requireNonNull(flatMapper.apply(value),"flatMapper cannot return null Validation")
                    );
                } catch (ValidationException e) {
                    yield Validation.invalid(e.errors());
                } catch (Exception e) {
                    yield Validation.invalid(
                            Objects.requireNonNull(errorMessageMaker.apply(e), "errorMessageMaker result cannot be null")
                    );
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
    default <R> Validation<R> refine(Function<? super T, ? extends Validation<? extends R>> refinement) {
        Objects.requireNonNull(refinement, "refinement cannot be null");
        return this.flatMap(refinement);
    }

    /**
     * If this validation is valid, applies the predicate to its value.
     * If the predicate returns false, returns Invalid(errorMessage).
     * If this validation is already invalid, returns it unchanged.
     */
    default Validation<T> filter(Predicate<? super T> predicate, ErrorMessage errorMessage) {
        Rule<T> rule = Rule.of(predicate, errorMessage);
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
     * The mapper must return a non-null, non-empty list.
     */
    default Validation<T> mapErrors(Function<List<ErrorMessage>, List<ErrorMessage>> mapper) {
        Objects.requireNonNull(mapper, "mapper cannot be null");
        return switch (this) {
            case Valid<T> v -> v;
            case Invalid(var errors) -> {
                List<ErrorMessage> mapped = Objects.requireNonNull(mapper.apply(errors), "mapper result cannot be null");
                if (mapped.isEmpty()) {
                    throw new IllegalArgumentException("mapper result cannot be empty");
                }
                yield invalid(mapped);
            }
        };
    }

    /**
     * Maps error messages by prepending an index to the segments of each error message.
     *
     * @param index the index (e.g., collection index or map key).
     */
    default Validation<T> atIndex(Object index) {
        Objects.requireNonNull(index, "index cannot be null");
        return mapErrors(errors -> errors.map(error -> error.atIndex(index)));
    }

    /**
     * Maps error messages by prepending the given name to the segments of each error message.
     *
     * @param name a logical name for the value being validated (e.g., the name of the field).
     * @return a new {@link Validation} instance.
     */
    default Validation<T> at(String name) {
        Objects.requireNonNull(name, "name cannot be null");
        return mapErrors(errors -> errors.map(error -> error.prepend(ErrorMessage.Path.of(name))));
    }

    /**
     * Maps error messages by prepending the propertyName for the given selector to the segments of each error message.
     *
     * @param selector The selector for the value that was validated (e.g., SomeRecord::someField or SomeBean::getProperty).
     */
    default <S> Validation<T> at(PropertySelector<S, T> selector) {
        Objects.requireNonNull(selector, "selector cannot be null");
        return at(selector.getPropertyName());
    }
    //endregion

    //region factory methods for known values

    static ValidationFactory from() {
        return ValidationFactory.instance;
    }

    /**
     * Creates a validation containing the provided value.
     * Any non-null values are considered Valid.
     */
    static <T> Validation<T> fromNullable(T value) {
        if(value == null) {
            return Invalid.notNull();
        } else {
            return new Valid<>(value);
        }
    }

    /**
     * Creates a successful validation containing the provided value.
     * Null is not allowed as a valid value.
     */
    static <T> Validation<T> valid(T value) {
        return new Valid<>(value);
    }

    /**
     * Creates an invalid validation with the provided error messages.
     */
    @SuppressWarnings("unchecked")
    static <T> Validation<T> invalid(ErrorMessage error, ErrorMessage... moreErrors) {
        Objects.requireNonNull(error, "error cannot be null");
        Objects.requireNonNull(moreErrors, "moreErrors cannot be null");
        return new Invalid<>(List.of(error).appendAll(List.of(moreErrors)));
    }

    /**
     * Creates an invalid validation with a single error message key.
     */
    @SuppressWarnings("unchecked")
    static <T> Validation<T> invalid(String errorKey) {
        return new Invalid<>(List.of(ErrorMessage.of(errorKey)));
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

    //endregion

    //region casting

    /**
     * Narrows a {@code Validation<? extends T>} to a {@code Validation<T>}.
     *
     */
    @SuppressWarnings("unchecked")
    static <T> Validation<T> narrow(Validation<? extends T> validation) {
        Objects.requireNonNull(validation, "validation cannot be null");
        return (Validation<T>) validation;
    }

    /**
     * Narrows a {@code Validation<? super T>} to a {@code Validation<T>}.
     */
    @SuppressWarnings("unchecked")
    static <T> Validation<T> narrowSuper(Validation<? super T> validation) {
        Objects.requireNonNull(validation, "validation cannot be null");
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
     * If this validation is valid, return a new Valid with the passed, non-null value.
     * If this validation is already invalid, returns it unchanged.
     */
    default <U> Validation<U> mapTo(U value) {
        Objects.requireNonNull(value, "value cannot be null");
        return map(ignored -> value);
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

        public Iterator<T> iterator() {
            return Iterator.of(value);
        }
    }

    /**
     * Represents an invalid validation.
     *
     * @param errors the list of error messages that describe the validation failure. Errors cannot be empty, cannot contain nulls and will be deduplicated.
     */
    record Invalid<T>(List<ErrorMessage> errors) implements Validation<T> {
        private static final Invalid notNull = new Invalid<>(List.of(ErrorMessage.of("must.not.be.null")));

        public Invalid {
            Objects.requireNonNull(errors, "errors cannot be null");
            if (errors.isEmpty()) {
                throw new IllegalArgumentException("errors must be non-empty");
            }
            errors.forEach(error -> Objects.requireNonNull(error, "errors cannot contain null"));
            errors = errors.distinct();
        }

        @Override
        public boolean isValid() {
            return false;
        }

        @Override
        public Iterator<T> iterator() {
            return Iterator.empty();
        }

        @SuppressWarnings("unchecked")
        static <T> Invalid<T> notNull() {
            return (Invalid<T>) notNull;
        }
    }
}
