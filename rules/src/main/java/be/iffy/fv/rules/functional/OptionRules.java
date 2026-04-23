package be.iffy.fv.rules.functional;

import io.vavr.control.Option;
import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;

import java.util.Optional;

public class OptionRules {

    /**
     * Singleton instance of {@link OptionRules}.
     */
    public static final OptionRules options = new OptionRules();

    /**
     * Returns the singleton instance of {@link OptionRules}.
     */
    public static OptionRules options() {
        return options;
    }

    /**
     * Fails is the {@link Option} is empty while extracting the value from the {@link Option}.
     * <p>
     * Error key: {@code must.not.be.empty}
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/rules/functional/OptionSnippets.java" region = "required-example"}
     *
     * @param <T> the type of the value contained in the Option
     */
    public <T> MappingRule<Option<T>, T> required() {
        return MappingRule.<Option<T>>notNull().andThen(input -> input.fold(
                () -> Validation.invalid("must.not.be.empty"),
                Validation::valid
        ));
    }

    /**
     * Fails is the {@link Optional} is empty.
     * <p>
     * Error key: {@code must.not.be.empty}
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/rules/functional/OptionSnippets.java" region = "required-option-example"}
     *
     * @param <T> the type of the value contained in the Option
     */
    public <T> Rule<Option<T>> requiredOption() {
        return Rule.notNull().and(input ->
                input.isEmpty() ? Validation.invalid("must.not.be.empty") : Validation.valid(input)
        );
    }

    /**
     * Fails is the {@link Option} is not empty.
     * <p>
     * Error key: {@code must.be.empty}
     *
     * @param <T> the type of the value contained in the Option
     */
    public <T> Rule<Option<T>> empty() {
        return Rule.notNull().and(input ->
                input.isEmpty() ? Validation.valid(input) : Validation.invalid("must.be.empty")
        );
    }

}
