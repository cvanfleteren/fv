package net.vanfleteren.fv.rules.functional;

import io.vavr.control.Option;
import net.vanfleteren.fv.MappingRule;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.Validation;

public class OptionRules {

    /**
     * Singleton instance of {@link OptionRules}.
     */
    public static final OptionRules options = new OptionRules();

    /**
     * Returns the singleton instance of {@link OptionRules}.
     *
     * @return the {@link OptionRules} instance.
     */
    public static OptionRules options() {
        return options;
    }

    /**
     * Returns a {@link MappingRule} that expects the input Option to be non-empty.
     * <p>
     * Error key: {@code must.not.be.empty}
     *
     * @param <T> the type of the value contained in the Option
     */
    public <T> MappingRule<Option<T>, T> required() {
        return input -> input.fold(
                () -> Validation.invalid("must.not.be.empty"),
                Validation::valid
        );
    }

    /**
     * Returns a {@link Rule} that expects the input Option to be non-empty.
     * <p>
     * Error key: {@code must.not.be.empty}
     *
     * @param <T> the type of the value contained in the Option
     */
    public <T> Rule<Option<T>> requiredOption() {
        return input -> input.isEmpty() ? Validation.invalid("must.not.be.empty") : Validation.valid(input);
    }

    /**
     * Returns a {@link MappingRule} that expects the input {@link Option} to be empty.
     * <p>
     * Error key: {@code must.be.empty}
     *
     * @param <T> the type of the value contained in the Option
     */
    public <T> Rule<Option<T>> empty() {
        return input -> input.isEmpty() ? Validation.valid(input) : Validation.invalid("must.be.empty");
    }

}
