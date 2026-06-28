package be.iffy.fv.jakarta;

import be.iffy.fv.Rule;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Bridges a Spring-managed FV {@link Rule} bean into Jakarta Bean Validation.
 *
 * <p>Point at a class that is registered as a Spring bean. The bean may implement {@link Rule}
 * directly or implement {@link RuleProvider}. Unlike {@link FvRule}, the class does not need a
 * public no-arg constructor — Spring creates and injects it, so it can receive constructor-injected
 * or {@code @Autowired} dependencies like any other bean.
 *
 * <pre>{@code
 * @FvRuleBean(Order.Validator.class)
 * record Order(String ref, List<LineItem> items) {
 *
 *     @Component
 *     @RequiredArgsConstructor
 *     public static class Validator implements Rule<Order> {
 *
 *         private final PricingService pricingService;
 *
 *         @Override
 *         public Validation<Order> apply(Order order) {
 *             return Rule.all(
 *                 strings.notBlank().on(Order::ref),
 *                 rule(o -> pricingService.isWithinBudget(o.items()), "order.over.budget")
 *             ).apply(order);
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p>The bean is looked up by type from the Spring {@code BeanFactory}. This annotation requires
 * Spring — using it outside a Spring context throws an {@link IllegalArgumentException} at
 * validation time.
 *
 * <p>A null value is treated as valid — pair with {@code @NotNull} if needed.
 */
@Repeatable(FvRuleBean.List.class)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FvRuleBeanValidator.class)
@Documented
public @interface FvRuleBean {

    /**
     * A Spring bean type implementing {@link Rule} or {@link RuleProvider}.
     * Looked up from the Spring {@code BeanFactory} by type.
     */
    Class<?> value();

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /** Container for repeating {@link FvRuleBean} on the same element. */
    @Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        FvRuleBean[] value();
    }
}
