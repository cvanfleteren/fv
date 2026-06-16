package be.iffy.fv.rules;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import be.iffy.fv.Validation.Invalid;
import io.vavr.control.Try;

import java.util.Objects;
import java.util.function.Function;

/**
 * Implementation of {@link IObjectRules} for generic {@link Object} validation.
 */
public class ObjectRules implements IObjectRules<Object> {

    /**
     * Singleton instance of {@link ObjectRules}.
     */
    public static final ObjectRules objects = new ObjectRules();


    /**
     * Acts the same as {@link #notNull()}, but takes a Class parameter to help the java compiler
     * with type inference. Can be used to use something like
     * {@code Rule<String> rule = objects.notNull(String.class).and(...);}
     * instead of
     * {@code Rule<String> rule = objects.<String>notNull.and(...);}
     * which some people prefer.
     * <p>
     * Error key: {@code must.not.be.null}
     */
    public <T> Rule<T> notNull(Class<T> clazz) {
        return (Rule<T>) notNull();
    }

    /**
     * Converts the object to a string.
     * <p>
     * Fails if the object is {@code null}.
     * <p>
     * Error key: {@code must.not.be.null}
     */
    public MappingRule<Object, String> asString() {
        return MappingRule.notNull().map(String::valueOf);
    }

    /**
     * Fails if the input is not an instance of the given class.
     * <p>
     * Error key: {@code must.be.instance.of}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code type}: the class name ({@link String})</li>
     * </ul>
     */
    public <T, R> MappingRule<T, R> isInstanceOf(Class<R> clazz) {
        Objects.requireNonNull(clazz, "clazz cannot be null");
        return input -> {
            if (clazz.isInstance(input)) {
                return Validation.valid(clazz.cast(input));
            } else if (input == null) {
                return Invalid.notNull();
            } else {
                return Validation.invalid(ErrorMessage.of("must.be.instance.of", "type", clazz.getSimpleName()));
            }
        };
    }

    /**
     * Fails if the passed constructor Function doesn't apply successfully.
     * Will catch all possible exceptions thrown by the function.
     * If the function throws ValidationException, its errors will be used instead of the passed error.
     */
    public <T,R> MappingRule<T, R> construct(Function<T,R> constructor, ErrorMessage errorMessage) {
        Objects.requireNonNull(constructor, "constructor cannot be null");
        return MappingRule.fromTry(input ->
                Try.of(() ->
                        constructor.apply(input)
                ),
                errorMessage
        );
    }

    /**
     * Fails if the passed constructor Function doesn't apply successfully.
     * Will catch all possible exceptions thrown by the function.
     * If the function throws ValidationException, its errors will be used instead of the passed error.
     */
    public <T,R> MappingRule<T, R> construct(Function<T,R> constructor, String errorKey) {
        return construct(constructor, ErrorMessage.of(errorKey));
    }

    /**
     * Fails if the passed constructor Function doesn't apply successfully.
     * Will catch all possible exceptions thrown by the function.
     * If the function throws ValidationException, its errors will be used.
     * Error key: {@code could.not.construct}
     */
    public <T,R> MappingRule<T, R> construct(Function<T,R> constructor) {
        return construct(constructor, ErrorMessage.of("could.not.construct"));
    }

}
