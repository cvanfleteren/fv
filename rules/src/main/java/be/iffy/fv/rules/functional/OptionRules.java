package be.iffy.fv.rules.functional;

import be.iffy.fv.*;
import be.iffy.fv.Validation.Invalid;
import io.vavr.control.Option;

import java.util.Objects;
import java.util.Optional;

public final class OptionRules {

    /**
     * Singleton instance of {@link OptionRules}.
     */
    public static final OptionRules options = new OptionRules();

    /**
     * Fails if the {@link Option} is empty while extracting the value from the {@link Option}.
     * <p>
     * Error key: {@code must.not.be.empty}
     */
    public <T> MappingRule<Option<T>, T> required() {
        return MappingRule.<Option<T>>notNull().then(input -> input.fold(
                () -> Validation.invalid("must.not.be.empty"),
                Validation::valid
        ));
    }

    /**
     * Applies the given MappingRule to the {@link Option} if it is present. If the Option is empty, the result
     * is considered to be valid.
     */
    public <T, R> MappingRule<Option<T>, Option<R>> matches(RuleLike<? super T, Validation<R>> mappingRuleLike) {
        return input -> {
            if(input == null) {
                return Invalid.notNull();
            }
            Option<Validation<R>> res = input.map(mappingRuleLike::apply);
            return Validations.sequence(res);
        };
    }

    /**
     * Acts the same as {@link #required()}, but takes a Class parameter to help the java compiler
     * with type inference. Can be used to use something like
     * {@code Validation<Bic> bic = validateThat(bicHolder.bic()).is(options.required(String.class).then(Bic::validate));}
     * instead of
     * {@code Validation<Bic> bic = validateThat(bicHolder.bic()).is(options.<String>required().then(Bic::validate));}
     * which some people prefer.
     */
    public <T> MappingRule<Option<T>, T> required(Class<T> clazz) {
        return required();
    }

    /**
     * Fails if the {@link Option} is empty or doesn't contain a value that passes the passed rule.
     * Return a {@link be.iffy.fv.Validation.Valid} with the contained value otherwise.
     *
     * @param rule the rule to apply to the value inside the {@link Option}
     */
    public <T, R> MappingRule<Option<T>, R> required(RuleLike<? super T, ? extends Validation<R>> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return input -> input.fold(
                () -> Validation.invalid("must.not.be.empty"),
                rule::apply
        );
    }

    /**
     * Fails if the {@link Option} is empty or doesn't contain a value that passes the passed rule.
     * <p>
     * Error key: {@code must.not.be.empty} or the key of the passed rule
     */
    public <T> Rule<Option<T>> contains(Rule<T> rule) {
        return Rule.all(notEmpty(), rule.lift().toOption());
    }

    /**
     * Fails if the {@link Optional} is empty.
     * <p>
     * Error key: {@code must.not.be.empty}
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
