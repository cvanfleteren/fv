package be.iffy.fv.jakarta;

import be.iffy.fv.Rule;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Bridges an FV {@link Rule} stored in a {@code public static} field into Jakarta Bean Validation.
 *
 * <p>Point directly at a field of type {@link Rule} on any class. This is the natural FV idiom:
 * rules are plain static constants, and the annotation is just a pointer — no wrapper class or
 * interface required.
 *
 * <pre>{@code
 * @FvStaticRule(on = Person.class, field = "RULE")
 * record Person(String name, int age) {
 *
 *     public static final Rule<Person> RULE = Rule.all(
 *         strings.minLength(2).on(Person::name),
 *         ints.atLeast(18).on(Person::age)
 *     );
 * }
 * }</pre>
 *
 * <p>The rule can live in a separate class when you want to keep the type clean or share rules
 * across modules:
 *
 * <pre>{@code
 * @FvStaticRule(on = PersonRules.class, field = "VALIDATE")
 * record Person(String name, int age) {}
 *
 * public class PersonRules {
 *     public static final Rule<Person> VALIDATE = Rule.all(
 *         strings.minLength(2).on(Person::name),
 *         ints.atLeast(18).on(Person::age)
 *     );
 * }
 * }</pre>
 *
 * <p>The field name is validated eagerly at startup — a typo or missing field is caught before the
 * first request (see {@code FvRuleStartupValidator}).
 *
 * <p>A null value is treated as valid — pair with {@code @NotNull} if needed.
 */
@Repeatable(FvStaticRule.List.class)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FvStaticRuleValidator.class)
@Documented
public @interface FvStaticRule {

    /** The class that declares the static {@link Rule} field. */
    Class<?> on();

    /** The name of the {@code public static} field of type {@link Rule} on {@link #on()}. */
    String field();

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /** Container for repeating {@link FvStaticRule} on the same element. */
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        FvStaticRule[] value();
    }
}
