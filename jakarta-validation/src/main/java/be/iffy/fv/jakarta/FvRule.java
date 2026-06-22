package be.iffy.fv.jakarta;

import be.iffy.fv.Rule;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Bridges an FV {@link Rule} into Jakarta Bean Validation.
 *
 * <p>Annotate a type with {@code @FvRule} and specify the rule using <em>exactly one</em> of the
 * three supported modes:
 *
 * <h3>1. Rule class (original mode)</h3>
 * <p>Point to a class that directly implements {@link Rule} and has a public no-arg constructor:
 * <pre>{@code
 * @FvRule(Person.Validator.class)
 * record Person(String name, int age) {
 *
 *     public static class Validator implements Rule<Person> {
 *         private static final Rule<Person> IMPL = Rule.all(
 *             strings.minLength(2).on(Person::name),
 *             ints.atLeast(18).on(Person::age)
 *         );
 *
 *         @Override public Validation<Person> apply(Person p) { return IMPL.apply(p); }
 *     }
 * }
 * }</pre>
 *
 * <h3>2. RuleProvider class</h3>
 * <p>Point to a class that implements {@link RuleProvider} and has a public no-arg constructor.
 * The class does not need to implement {@link Rule} itself:
 * <pre>{@code
 * @FvRule(provider = Person.Rules.class)
 * record Person(String name, int age) {
 *
 *     public static class Rules implements RuleProvider<Person> {
 *         private static final Rule<Person> IMPL = Rule.all(
 *             strings.minLength(2).on(Person::name),
 *             ints.atLeast(18).on(Person::age)
 *         );
 *
 *         @Override public Rule<Person> provide() { return IMPL; }
 *     }
 * }
 * }</pre>
 *
 * <h3>3. Static field reference</h3>
 * <p>Point directly to a {@code public static} field of type {@link Rule} on any class.
 * This is the most concise option when rules are already defined as static constants:
 * <pre>{@code
 * @FvRule(on = Person.class, field = "RULE")
 * record Person(String name, int age) {
 *     public static final Rule<Person> RULE = Rule.all(
 *         strings.minLength(2).on(Person::name),
 *         ints.atLeast(18).on(Person::age)
 *     );
 * }
 * }</pre>
 *
 * <p>When a BV-aware framework (Spring {@code @Validated}, JPA, etc.) encounters {@code @Valid}
 * on a parameter or field of the annotated type, it invokes the FV rule and translates any
 * {@link be.iffy.fv.Validation.Invalid} result into {@link jakarta.validation.ConstraintViolation}s —
 * one per FV error, with the FV error key as the violation message and the FV path mapped to BV
 * property nodes.
 *
 * <p>A null value is treated as valid — pair with {@code @NotNull} if needed.
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FvRuleValidator.class)
@Documented
public @interface FvRule {

    /**
     * Mode 1: a class implementing {@link Rule} with a public no-arg constructor.
     * Mutually exclusive with {@link #provider()} and {@link #on()}/{@link #field()}.
     */
    Class<? extends Rule<?>> value() default NoneRule.class;

    /**
     * Mode 2: a class implementing {@link RuleProvider} with a public no-arg constructor.
     * Mutually exclusive with {@link #value()} and {@link #on()}/{@link #field()}.
     */
    Class<? extends RuleProvider<?>> provider() default NoneRuleProvider.class;

    /**
     * Mode 3 (part 1): the class that declares the static {@link Rule} field.
     * Must be combined with {@link #field()}; mutually exclusive with {@link #value()} and
     * {@link #provider()}.
     */
    Class<?> on() default Void.class;

    /**
     * Mode 3 (part 2): the name of the {@code public static} field of type {@link Rule} on
     * the class specified by {@link #on()}.
     */
    String field() default "";

    //region properties required by bean validation
    /** Unused — violations use the FV error key as their message directly. */
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
    //endregion
}
