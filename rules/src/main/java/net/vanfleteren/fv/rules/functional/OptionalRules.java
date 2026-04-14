package net.vanfleteren.fv.rules.functional;

import net.vanfleteren.fv.MappingRule;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.Validation;

import java.util.Optional;

public class OptionalRules {

    /**
     * Singleton instance of {@link OptionalRules}.
     */
    public static final OptionalRules optionals = new OptionalRules();

    /**
     * Returns the singleton instance of {@link OptionalRules}.
     *
     * @return the {@link OptionalRules} instance.
     */
    public static OptionalRules optionals() {
        return optionals;
    }

    /**
     * Returns a {@link MappingRule} that expects the input {@link Optional} to be non-empty.
     * <p>
     * Error key: {@code must.not.be.empty}
     *
     * @param <T> the type of the value contained in the Optional
     */
    public <T> MappingRule<Optional<T>, T> required() {
        return input -> input.map(Validation::valid).orElse(Validation.invalid("must.not.be.empty"));
    }

    /**
     * Returns a {@link Rule} that expects the input {@link Optional} to be non-empty.
     * <p>
     * Error key: {@code must.not.be.empty}
     *
     * @param <T> the type of the value contained in the Optional
     */
    public <T> Rule<Optional<T>> requiredOptional() {
        return input -> input.isEmpty() ? Validation.invalid("must.not.be.empty") : Validation.valid(input);
    }

    /**
     * Returns a {@link MappingRule} that expects the input {@link Optional} to be empty.
     * <p>
     * Error key: {@code must.be.empty}
     *
     * @param <T> the type of the value contained in the Optional
     */
    public <T> Rule<Optional<T>> empty() {
        return input -> input.isEmpty() ? Validation.valid(input) : Validation.invalid("must.be.empty");
    }

}
