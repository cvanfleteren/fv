package be.iffy.fv.rules;

import io.vavr.control.Try;
import be.iffy.fv.ErrorMessage;
import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;

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
     * Fails if the object is {@code null}.
     * <p>
     * Error key: {@code must.not.be.null}
     */
    public <T> Rule<T> notNull() {
        return MappingRule.<T>notNull()::test;
    }

    /**
     * Acts the same as {@link #notNull()}, but takes a Class parameter to help the java compiler
     * with type inference. Can be used to use something like
     * {@code Ruyle<String> rule = objects.notNull(String.class).and(...);}
     * instead of
     * {@code Ruyle<String> rule = objects.<String>notNull.and(...);}
     * which some people prefer.
     * <p>
     * Error key: {@code must.not.be.null}
     */
    public <T> Rule<T> notNull(Class<T> clazz) {
        return notNull();
    }

    /**
     * Fails if the input string is not a valid enum value for the given enum class while mappping to the enum.
     * <p>
     * Usage example:
     * {@snippet file="be/iffy/fv/rules/ObjectRulesSnippets.java" region="is-enum-example"}
     * <p>
     * Error key: {@code must.be.valid.enum.value}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code value}: the input string ({@link String})</li>
     * </ul>
     */
    public <E extends Enum<E>> MappingRule<String, E> isEnum(Class<E> clazz) {
        return input -> Try.of(() -> Enum.valueOf(clazz, input))
                .fold(
                        f -> Validation.invalid(ErrorMessage.of("must.be.valid.enum.value", "value", input)),
                        Validation::valid
                );
    }


    /**
     * Fails if the input string is not a valid enum value for the given enum class.
     * <p>
     * Error key: {@code must.be.valid.enum.value}
     * <p>
     * Parameters:
     * <ul>
     *     <li>{@code value}: the input string ({@link String})</li>
     * </ul>
     *
     */
    public <E extends Enum<E>> Rule<String> canBeEnum(Class<E> clazz) {
        return input -> Try.of(() -> Enum.valueOf(clazz, input))
                .fold(
                        f -> Validation.invalid(ErrorMessage.of("must.be.valid.enum.value", "value", input)),
                        v -> Validation.valid(input)
                );
    }

    /**
     * Fails if the passed constructor Function doesn't apply successfully.
     * Will catch all possible exceptions thrown by the function.
     */
    public <T,R> MappingRule<T, R> canBe(Function<T,R> constructor, ErrorMessage errorMessage) {
        return input ->  Try.of(() -> constructor.apply(input))
                .fold(
                        f -> Validation.invalid(errorMessage),
                        v -> Validation.valid(v)
                );
    }

    /**
     * Fails if the passed constructor Function doesn't apply successfully.
     * Will catch all possible exceptions thrown by the function.
     */
    public <T,R> MappingRule<T, R> canBe(Function<T,R> constructor, String errorKey) {
        return canBe(constructor, ErrorMessage.of(errorKey));
    }

}
