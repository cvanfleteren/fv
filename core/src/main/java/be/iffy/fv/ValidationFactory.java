package be.iffy.fv;

import io.vavr.Function1;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class ValidationFactory {

    private ValidationFactory() {}

    static final ValidationFactory instance = new ValidationFactory();

    /**
     * Creates a {@link Validation} from a {@link Supplier}.
     * If the supplier throws a {@link ValidationException}, the returned validation will be invalid with the same errors
     * as the thrown exception.
     * If the supplier throws any other exception, the exception is propagated.
     * If the supplier returns null, NullPointerException is thrown.
     * Use {@link #catchingAll(Supplier, ErrorMessage)} when you intentionally want
     * to convert other exceptions into validation errors, or create a {@link Try}
     * yourself and pass it to {@link #_try(Try, ErrorMessage)}.
     */
    public <T> Validation<T> catching(Supplier<? extends T> supplier) {
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
     * If the supplier returns null, the resulting NullPointerException is converted to an invalid validation using the provided errorMessageMaker function.
     * This method is meant for interoperability with code that can throw any {@link Exception}, but is also somewhat dangerous
     * as it can hide issues like {@link NullPointerException} and so on.
     */
    public <T> Validation<T> catchingAll(Supplier<? extends T> supplier, Function<Exception, ErrorMessage> errorMessageMaker) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        Objects.requireNonNull(errorMessageMaker, "errorMessageMaker cannot be null");
        try {
            return Validation.valid(supplier.get());
        } catch (ValidationException e) {
            return Validation.invalid(e.errors());
        } catch (Exception e) {
            return Validation.invalid(
                    Objects.requireNonNull(errorMessageMaker.apply(e), "errorMessageMaker result cannot be null")
            );
        }
    }

    /**
     * Creates a {@link Validation} from a {@link Supplier}.
     * If the supplier throws a {@link ValidationException}, the returned validation will be invalid with the same errors
     * as the thrown exception.
     * If the supplier throws any other exception, the exception will also be converted to an Invalid, with the passed {@link ErrorMessage}
     * This method is meant for interoperability with code that can throw any {@link Exception}, but is also somewhat dangerous
     * as it can hide issues like {@link NullPointerException} and so on.
     * Does not catch {@link Error}s.
     */
    public <T> Validation<T> catchingAll(Supplier<? extends T> supplier, ErrorMessage errorMessage) {
       Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
       return catchingAll(supplier, e -> errorMessage);
    }

    /**
     * Creates a {@link Validation} from a {@link Supplier}.
     * If the supplier throws a {@link ValidationException}, the returned validation will be invalid with the same errors
     * as the thrown exception.
     * If the supplier throws any other exception, the exception will also be converted to an Invalid, with the passed {@link ErrorMessage}
     * This method is meant for interoperability with code that can throw any {@link Exception}, but is also somewhat dangerous
     * as it can hide issues like {@link NullPointerException} and so on.
     */
    public <T> Validation<T> catchingAll(Supplier<? extends T> supplier, String errorKey) {
        return catchingAll(supplier, ErrorMessage.of(errorKey));
    }

    /**
     * Creates a {@link Validation} from a {@link Try}.
     * If the Try throws a {@link ValidationException}, the returned validation will be invalid with the same errors
     * as the thrown exception.
     * If the {@link Try} is successful, the returned validation will be valid with the value.
     * If the {@link Try} is failed, the returned validation will be invalid with the provided error message.
     */
    public <T> Validation<T> _try(Try<? extends T> _try, ErrorMessage errorMessage) {
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
    public <T> Validation<T> _try(Try<? extends T> _try, String errorKey) {
        return _try(_try, ErrorMessage.of(errorKey));
    }

    /**
     * Creates a {@link Validation} from a {@link Try}.
     * If the Try failed with a {@link ValidationException}, the returned validation will be invalid with the same errors
     * as the thrown ValidationException.
     * If the {@link Try} is successful, the returned validation will be valid with the value.
     * But if the Try succeeds with null, the return validation will be invalid with the key "must.not.be.null".
     * If the {@link Try} is failed, the returned validation will be invalid with the error key "failed.from.try"
     * and param "message" representing the message of the exception
     * <p>
     * Error key: {@code failed.from.try}
     */
    public <T> Validation<T> _try(Try<? extends T> _try) {
        Objects.requireNonNull(_try, "_try cannot be null");
        return _try.fold(
                e -> e instanceof ValidationException ve
                        ? Validation.invalid(ve.errors())
                        : Validation.invalid(ErrorMessage.of("failed.from.try", "message", e.getMessage())),
                Validation::fromNullable
        );
    }

    /**
     * Creates a {@link Validation} from an {@link Option}.
     * If the {@link Option} is defined, the returned validation will be valid with the value provided the value is not null.
     * If the {@link Option} is empty or contains null, the returned validation will be invalid with the provided error message.
     */
    public <T> Validation<T> option(Option<? extends T> option, ErrorMessage errorMessage) {
        Objects.requireNonNull(option, "option cannot be null");
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        return option.fold(
                () -> Validation.invalid(errorMessage),
                Validation::fromNullable
        );
    }

    /**
     * Creates a {@link Validation} from an {@link Option}.
     * If the {@link Option} is defined, the returned validation will be valid with the value.
     * If the {@link Option} is empty or contains null, the returned validation will be invalid with the provided error key.
     */
    public <T> Validation<T> option(Option<? extends T> option, String errorKey) {
        Objects.requireNonNull(option, "option cannot be null");
        return option(option, ErrorMessage.of(errorKey));
    }

    /**
     * Creates a {@link Validation} from an {@link Option}.
     * If the {@link Option} is defined, the returned validation will be valid with the value.
     * If the {@link Option} is empty or contains null, the returned validation will be invalid with the default error message {@code "value.is.none"}.
     * <p>
     * Error key: {@code value.is.none}
     */
    public <T> Validation<T> option(Option<? extends T> option) {
        Objects.requireNonNull(option, "option cannot be null");
        return option(option, "value.is.none");
    }

    /**
     * Creates a {@link Validation} from an {@link Either}.
     * If the {@link Either} is right, the returned validation will be valid with the value.
     * If the {@link Either} is left, the returned validation will be invalid with the error message mapped from the left value.
     */
    public <L, R> Validation<R> either(Either<L, ? extends R> either, Function1<? super L, ErrorMessage> errorMapper) {
        Objects.requireNonNull(either, "either cannot be null");
        Objects.requireNonNull(errorMapper, "errorMapper cannot be null");
        return either.fold(
                l -> Validation.invalid(
                        Objects.requireNonNull(
                                errorMapper.apply(l),
                                "errorMapper result cannot be null"
                        )
                ),
                Validation::valid
        );
    }

    /**
     * Creates a {@link Validation} from a standard Java {@link Optional}.
     * If the optional is present, the returned validation will be valid with the value.
     * If the optional is empty, the returned validation will be invalid with the default error message {@code "value.is.none"}.
     *
     */
    public <T> Validation<T> optional(Optional<? extends T> optional) {
        Objects.requireNonNull(optional, "optional cannot be null");
        return option(Option.ofOptional(optional));
    }

    /**
     * Creates a {@link Validation} from a standard Java {@link Optional}.
     * If the optional is present, the returned validation will be valid with the value.
     * If the optional is empty, the returned validation will be invalid with the provided error key.
     */
    public <T> Validation<T> optional(Optional<? extends T> optional, String errorKey) {
        Objects.requireNonNull(optional, "optional cannot be null");
        return option(Option.ofOptional(optional), errorKey);
    }

    /**
     * Creates a {@link Validation} from a standard Java {@link Optional}.
     * If the optional is present, the returned validation will be valid with the value.
     * If the optional is empty, the returned validation will be invalid with the provided error message.
     */
    public <T> Validation<T> optional(Optional<? extends T> optional, ErrorMessage errorMessage) {
        Objects.requireNonNull(optional, "optional cannot be null");
        Objects.requireNonNull(errorMessage, "errorMessage cannot be null");
        return option(Option.ofOptional(optional), errorMessage);
    }
}
