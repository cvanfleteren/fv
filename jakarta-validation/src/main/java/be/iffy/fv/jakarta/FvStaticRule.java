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
 * <p><b>Shorthand form</b> (when the rule lives on the annotated type itself): omit {@link #on()}
 * and the annotated type is used automatically:
 *
 * <pre>{@code
 * @FvStaticRule(field = "RULE")
 * record Person(String name, int age) {
 *
 *     public static final Rule<Person> RULE = Rule.all(
 *         strings.minLength(2).on(Person::name),
 *         ints.atLeast(18).on(Person::age)
 *     );
 * }
 * }</pre>
 *
 * <p><b>Explicit form</b> (when the rule lives on a different class): supply {@link #on()} to
 * point at the class that declares the field:
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
 * <p>This annotation is {@link Repeatable}: two or more {@code @FvStaticRule} annotations may
 * appear on the same element; BV runs each independently and accumulates all violations.
 *
 * <p>The field name is validated eagerly at startup — a typo or missing field is caught before the
 * first request (see {@code FvRuleStartupValidator}).
 *
 * <p>Placing this annotation on a method validates the <em>return value</em> — useful with
 * Spring {@code @Validated} AOP to enforce post-conditions on service methods:
 *
 * <pre>{@code
 * @Service
 * @Validated
 * public class PersonService {
 *
 *     @FvStaticRule(on = Person.class, field = "RULE")
 *     public Person findById(long id) { ... }
 * }
 * }</pre>
 *
 * <p>A null value is treated as valid — pair with {@code @NotNull} if needed.
 */
@Repeatable(FvStaticRule.List.class)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FvStaticRuleValidator.class)
@Documented
public @interface FvStaticRule {

    /**
     * The class that declares the static {@link Rule} field.
     *
     * <p>When this annotation is placed on a type and the rule lives on that same type, {@code on}
     * may be omitted: the annotated type is used automatically. Must be specified explicitly when
     * the rule lives on a different class, or when the annotation is on a field or parameter.
     */
    Class<?> on() default Void.class;

    /** The name of the {@code public static} field of type {@link Rule} on {@link #on()}. */
    String field();

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /** Container for repeating {@link FvStaticRule} on the same element. */
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        FvStaticRule[] value();
    }
}
