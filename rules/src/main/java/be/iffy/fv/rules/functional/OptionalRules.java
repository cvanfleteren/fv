package be.iffy.fv.rules.functional;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;

import java.util.Optional;

public class OptionalRules {

    /**
     * Singleton instance of {@link OptionalRules}.
     */
    public static final OptionalRules optionals = new OptionalRules();

    /**
     * Returns the singleton instance of {@link OptionalRules}.
     */
    public static OptionalRules optionals() {
        return optionals;
    }

    /**
     * Fails is the {@link Optional} is empty while extracting the value from the {@link Optional}.
     * <p>
     * Error key: {@code must.not.be.empty}
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/rules/functional/OptionalSnippets.java" region = "required-example"}
     *
     * @param <T> the type of the value contained in the Optional
     */
    public <T> MappingRule<Optional<T>, T> required() {
        return MappingRule.<Optional<T>>notNull().andThen(input ->
                input.map(Validation::valid).orElse(Validation.invalid("must.not.be.empty"))
        );
    }

    /**
     * Fails is the {@link Optional} is empty.
     * <p>
     * Error key: {@code must.not.be.empty}
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/rules/functional/OptionalSnippets.java" region = "not-empty-example"}
     *
     * @param <T> the type of the value contained in the Optional
     */
    public <T> Rule<Optional<T>> notEmpty() {
        return Rule.notNull().and(input ->
                input.isEmpty() ? Validation.invalid("must.not.be.empty") : Validation.valid(input)
        );
    }

    /**
     * Fails is the {@link Optional} is not empty.
     * <p>
     * Error key: {@code must.be.empty}
     *
     * @param <T> the type of the value contained in the Optional
     */
    public <T> Rule<Optional<T>> empty() {
        return Rule.notNull().and(input ->
                input.isEmpty() ? Validation.valid(input) : Validation.invalid("must.be.empty")
        );
    }

}
