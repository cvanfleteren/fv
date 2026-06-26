package be.iffy.fv.jakarta;

import be.iffy.fv.Rule;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * BV {@link jakarta.validation.ConstraintValidator} that resolves an FV {@link Rule} from a
 * Spring-managed bean, as specified by {@link FvRuleBean}.
 *
 * <p>The bean is looked up by type from the Spring {@code BeanFactory} and may implement
 * {@link Rule} directly or implement {@link RuleProvider}.
 */
public class FvRuleBeanValidator extends AbstractFvValidator<FvRuleBean> {

    @Autowired(required = false)
    @Nullable
    private BeanFactory beanFactory;

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(FvRuleBean annotation) {
        rule = (Rule<Object>) resolveBean(annotation.value(), beanFactory);
    }

    @SuppressWarnings("unchecked")
    static Rule<?> resolveBean(Class<?> beanType, @Nullable BeanFactory beanFactory) {
        if (beanFactory == null) {
            throw new IllegalArgumentException(
                "@FvRuleBean requires a Spring BeanFactory — not available in this context"
            );
        }
        Object bean;
        try {
            bean = beanFactory.getBean(beanType);
        } catch (NoSuchBeanDefinitionException e) {
            throw new IllegalArgumentException(
                "No Spring bean of type " + beanType.getName() + " found in the application context", e
            );
        }
        return getRule(beanType, bean);
    }
}
