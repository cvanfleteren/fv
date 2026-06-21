package be.iffy.fv.jakarta;

import be.iffy.fv.Rule;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Bridges an FV {@link Rule} into Jakarta Bean Validation.
 *
 * <p>Annotate a type with {@code @FvRule(MyRule.class)} where {@code MyRule} implements
 * {@link Rule} and has a public no-arg constructor. When a BV-aware framework (Spring
 * {@code @Validated}, JPA, etc.) encounters {@code @Valid} on a parameter or field of that
 * type, it will invoke the FV rule and translate any {@link be.iffy.fv.Validation.Invalid}
 * result into {@link jakarta.validation.ConstraintViolation}s — one per FV error,
 * with the FV error key as the violation message and the FV path mapped to BV property nodes.
 *
 * <p><strong>Usage:</strong>
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
 *
 * @Validated
 * public class MyService {
 *     public void enroll(@Valid Person person) { ... }
 * }
 * }</pre>
 *
 * <p>A null value is treated as valid — pair with {@code @NotNull} if needed.
 *
 * <p>The rule class must have a public no-arg constructor so that the BV runtime can
 * instantiate it. For parameterized rules, define a wrapper class (or a nested static
 * class on the record) that hard-codes the parameters:
 * <pre>{@code
 * public static class Validator implements Rule<MyType> {
 *     private static final Rule<MyType> IMPL = strings.minLength(3).on(MyType::name);
 *     public Validation<MyType> apply(MyType v) { return IMPL.apply(v); }
 * }
 * }</pre>
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FvRuleValidator.class)
@Documented
public @interface FvRule {

    /** The {@link Rule} class to apply. Must have a public no-arg constructor. */
    Class<? extends Rule<?>> value();

    /** Unused — violations use the FV error key as their message directly. */
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
