package be.iffy.fv.rules.functional;

import io.vavr.control.Option;
import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;

import java.util.Objects;
import java.util.Optional;

public class OptionRules {

    /**
     * Singleton instance of {@link OptionRules}.
     */
    public static final OptionRules options = new OptionRules();

    /**
     * Fails if the {@link Option} is empty while extracting the value from the {@link Option}.
     * <p>
     * Error key: {@code must.not.be.empty}
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/rules/functional/OptionSnippets.java" region = "required-example"}
     */
    public <T> MappingRule<Option<T>, T> required() {
        return MappingRule.<Option<T>>notNull().andThen(input -> input.fold(
                () -> Validation.invalid("must.not.be.empty"),
                Validation::valid
        ));
    }

    /**
     * Acts the same as {@link #required()}, but takes a Class parameter to help the java compiler
     * with type inference. Can be used to use something like
     * {@code Validation<Bic> bic = validateThat(bicHolder.bic()).is(options.required(String.class).andThen(Bic::validate));}
     * instead of
     * {@code Validation<Bic> bic = validateThat(bicHolder.bic()).is(options.<String>required().andThen(Bic::validate));}
     * which some people prefer.
     */
    public <T> MappingRule<Option<T>, T> required(Class<T> clazz) {
        return required();
    }

    /**
     * Fails if the {@link Option} is empty while or doesn't contain a value that passes the passed rule.
     * Return a {@link be.iffy.fv.Validation.Valid} with the contained value otherwise.
     *
     * @param rule the rule to apply to the value inside the {@link Option}
     */
    public <T, Z> MappingRule<Option<T>, Z> required(MappingRule<T, Z> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return rule.liftToOption().andThen(opt -> opt.fold(() -> Validation.invalid("must.not.be.empty"), Validation::valid));
    }

    /**
     * Fails if the {@link Option} is empty or doesn't contain a value that passes the passed rule.
     * <p>
     * Error key: {@code must.not.be.empty} or the key of the passed rule
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/rules/functional/OptionSnippets.java" region = "contains-example"}
     */
    public <T> Rule<Option<T>> contains(Rule<T> rule) {
        return Rule.both(notEmpty(), rule.liftToOption());
    }

    /**
     * Fails if the {@link Optional} is empty.
     * <p>
     * Error key: {@code must.not.be.empty}
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/rules/functional/OptionSnippets.java" region = "not-empty-example"}
     *
     */
    public <T> Rule<Option<T>> notEmpty() {
        return Rule.of(
                input -> !input.isEmpty(),
                "must.not.be.empty"
        );
    }

    /**
     * Fails if the {@link Option} is not empty.
     * <p>
     * Error key: {@code must.be.empty}
     *
     */
    public <T> Rule<Option<T>> empty() {
        return Rule.of(
                input -> input.isEmpty(),
                "must.be.empty"
        );
    }

}
