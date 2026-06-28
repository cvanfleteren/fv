package be.iffy.fv.jakarta;

import be.iffy.fv.Rule;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Bridges an FV {@link Rule} into Jakarta Bean Validation.
 *
 * <p>Point at a class that implements {@link Rule} or {@link RuleProvider} and has a public
 * no-arg constructor. BV instantiates it once per validator lifecycle.
 *
 * <p><b>Rule class:</b> the class implements {@link Rule} directly:
 *
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
 * <p><b>RuleProvider class:</b> the class implements {@link RuleProvider} — a factory that returns
 * the rule via {@code provide()}. Useful when the class serves as a namespace for multiple related
 * rules and does not need to implement {@link Rule} directly:
 *
 * <pre>{@code
 * @FvRule(Person.Rules.class)
 * record Person(String name, int age) {
 *
 *     public static class Rules implements RuleProvider<Person> {
 *         private static final Rule<Person> PERSON = Rule.all(
 *             strings.minLength(2).on(Person::name),
 *             ints.atLeast(18).on(Person::age)
 *         );
 *
 *         @Override public Rule<Person> provide() { return PERSON; }
 *     }
 * }
 * }</pre>
 *
 * <p>For rules stored in a {@code public static} field, use {@link FvStaticRule} instead.
 * For Spring-managed beans that need injection, use {@link FvRuleBean} instead.
 *
 * <p>This annotation is {@link Repeatable}: two or more {@code @FvRule} annotations may appear
 * on the same element; BV runs each independently and accumulates all violations.
 *
 * <p>The annotation may also be placed on another annotation type to create a composed annotation
 * (a shorthand alias). See the module docs for details.
 *
 * <p>When a BV-aware framework (Spring {@code @Validated}, JPA, etc.) encounters {@code @Valid}
 * on a parameter or field of the annotated type, it invokes the FV rule and translates any
 * {@link be.iffy.fv.Validation.Invalid} result into {@link jakarta.validation.ConstraintViolation}s —
 * one per FV error, with the FV error key as the violation message and the FV path mapped to BV
 * property nodes.
 *
 * <p>A null value is treated as valid — pair with {@code @NotNull} if needed.
 */
@Repeatable(FvRule.List.class)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FvRuleValidator.class)
@Documented
public @interface FvRule {

    /**
     * A class implementing {@link Rule} or {@link RuleProvider} with a public no-arg constructor.
     */
    Class<?> value();

    /**
     * Required by the Bean Validation spec but intentionally not honored.
     *
     * <p>{@code @FvRule} maps one annotation to <em>N</em> violations with <em>N</em> distinct
     * error keys, so a single template here cannot cover them individually. Override messages
     * per error key via {@code ValidationMessages.properties} instead — see the module docs.
     */
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /** Container for repeating {@link FvRule} on the same element. */
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        FvRule[] value();
    }
}
