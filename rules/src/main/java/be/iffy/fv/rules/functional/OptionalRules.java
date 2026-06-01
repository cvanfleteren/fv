package be.iffy.fv.rules.functional;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class OptionalRules {

    /**
     * Singleton instance of {@link OptionalRules}.
     */
    public static final OptionalRules optionals = new OptionalRules();

    /**
     * Fails if the {@link Optional} is empty while extracting the value from the {@link Optional}.
     * <p>
     * Error key: {@code must.not.be.empty}
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/rules/functional/OptionalSnippets.java" region = "required-example"}
     */
    public <T> MappingRule<Optional<T>, T> required() {
        return MappingRule.<Optional<T>>notNull().andThen(input ->
                input.map(Validation::valid).orElse(Validation.invalid("must.not.be.empty"))
        );
    }


    /**
     * Applies the given {@link MappingRule} to the {@link Optional} if it is present. If the Optional is empty, the result
     * is considered to be valid.
     */
    public <T, R> MappingRule<Optional<T>, Optional<R>> matches(MappingRule<T,R> rule) {
        return input -> {
            Optional<Validation<R>> res = input.map(rule::test);
            return Validation.transpose(res);
        };
    }

    /**
     * Applies the given MappingRule like Function to the {@link Optional} if it is present. If the Optional is empty, the result
     * is considered to be valid.
     */
    public <T, R> MappingRule<Optional<T>, Optional<R>> matches(Function<T,Validation<R>> ruleLike) {
        return matches(MappingRule.of(ruleLike));
    }

    /**
     * Acts the same as {@link #required()}, but takes a Class parameter to help the java compiler
     * with type inference. Can be used to use something like
     * {@code Validation<Bic> bic = validateThat(bicHolder.bic()).is(optionals.required(String.class).andThen(Bic::validate));}
     * instead of
     * {@code Validation<Bic> bic = validateThat(bicHolder.bic()).is(optionals.<String>required().andThen(Bic::validate));}
     * which some people prefer.
     */
    public <T> MappingRule<Optional<T>, T> required(Class<T> clazz) {
        return required();
    }

    /**
     * Fails if the {@link Optional} is empty while or doesn't contain a value that passes the passed rule.
     * Return a {@link be.iffy.fv.Validation.Valid} with the contained value otherwise.
     *
     * @param rule the rule to apply to the value inside the {@link Optional}
     */
    public <T, Z> MappingRule<Optional<T>, Z> required(MappingRule<T, Z> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return rule.liftToOptional().andThen(opt -> opt.map(Validation::valid).orElse(Validation.invalid("must.not.be.empty")));
    }

    /**
     * Fails if the {@link Optional} is empty while or doesn't contain a value that passes the passed rule.
     * <p>
     * Error key: {@code must.not.be.empty} or the key of the passed rule
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/rules/functional/OptionalSnippets.java" region = "contains-example"}
     */
    public <T> Rule<Optional<T>> contains(Rule<T> rule) {
        return Rule.both(notEmpty(), rule.liftToOptional());
    }

    /**
     * Fails if the {@link Optional} is empty.
     * <p>
     * Error key: {@code must.not.be.empty}
     * <p>
     * Usage example:
     * {@snippet file = "be/iffy/fv/rules/functional/OptionalSnippets.java" region = "not-empty-example"}
     */
    public <T> Rule<Optional<T>> notEmpty() {
        return Rule.of(
                Optional::isPresent,
                "must.not.be.empty"
        );
    }

    /**
     * Fails if the {@link Optional} is not empty.
     * <p>
     * Error key: {@code must.be.empty}
     */
    public <T> Rule<Optional<T>> empty() {
        return Rule.of(
                Optional::isEmpty,
                "must.be.empty"
        );
    }

}
