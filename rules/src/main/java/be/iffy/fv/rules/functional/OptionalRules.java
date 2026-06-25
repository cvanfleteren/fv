package be.iffy.fv.rules.functional;

import be.iffy.fv.*;
import be.iffy.fv.Validation.Invalid;

import java.util.Objects;
import java.util.Optional;

public final class OptionalRules {

    /**
     * Singleton instance of {@link OptionalRules}.
     */
    public static final OptionalRules optionals = new OptionalRules();

    /**
     * Fails if the {@link Optional} is empty while extracting the value from the {@link Optional}.
     * <p>
     * Error key: {@code must.not.be.empty}
     */
    public <T> MappingRule<Optional<T>, T> required() {
        return MappingRule.<Optional<T>>notNull().then(input ->
                input.map(Validation::valid).orElse(Validation.invalid("must.not.be.empty"))
        );
    }

    /**
     * Applies the given MappingRule like Function to the {@link Optional} if it is present. If the Optional is empty, the result
     * is considered to be valid.
     */
    public <T, R> MappingRule<Optional<T>, Optional<R>> matches(RuleLike<? super T, Validation<R>> mappingRuleLike) {
        return input -> {
            if(input == null) {
                return Invalid.notNull();
            }
            Optional<Validation<R>> res = input.map(mappingRuleLike::apply);
            return Validations.sequence(res);
        };
    }

    /**
     * Acts the same as {@link #required()}, but takes a Class parameter to help the java compiler
     * with type inference. Can be used to use something like
     * {@code Validation<Bic> bic = validateThat(bicHolder.bic()).is(optionals.required(String.class).then(Bic::validate));}
     * instead of
     * {@code Validation<Bic> bic = validateThat(bicHolder.bic()).is(optionals.<String>required().then(Bic::validate));}
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
    public <T, Z> MappingRule<Optional<T>, Z> required(RuleLike<? super T, ? extends Validation<Z>> rule) {
        Objects.requireNonNull(rule, "rule cannot be null");
        return input -> {
            if(input == null) {
                return Invalid.notNull();
            }
            if(input.isEmpty()) {
                return Validation.invalid("must.not.be.empty");
            } else {
                return rule.apply(input.get());
            }
        };
    }

    /**
     * Fails if the {@link Optional} is empty while or doesn't contain a value that passes the passed rule.
     * <p>
     * Error key: {@code must.not.be.empty} or the key of the passed rule
     */
    public <T> Rule<Optional<T>> contains( RuleLike<? super T, ? extends Validation<? extends T>> rule) {
        return Rule.all(notEmpty(), Rule.of(rule).lift().toOptional());
    }

    /**
     * Fails if the {@link Optional} is empty.
     * <p>
     * Error key: {@code must.not.be.empty}
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
