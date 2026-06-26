package be.iffy.fv.jakarta;

import be.iffy.fv.Rule;

/**
 * BV {@link jakarta.validation.ConstraintValidator} that delegates to an FV {@link Rule}.
 *
 * <p>Instantiated by the BV runtime for each {@link FvRule}-annotated element.
 * Instantiates the class specified by {@link FvRule#value()} and checks whether it implements
 * {@link Rule} or {@link RuleProvider}.
 */
public class FvRuleValidator extends AbstractFvValidator<FvRule> {

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(FvRule annotation) {
        rule = (Rule<Object>) resolveRule(annotation.value());
    }

    @SuppressWarnings("unchecked")
    static Rule<?> resolveRule(Class<?> cls) {
        Object instance = instantiate(cls);
        return getRule(cls, instance);
    }

    static Object instantiate(Class<?> cls) {
        try {
            return cls.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            String error = """
                Cannot instantiate %s - ensure it has a public no-arg constructor.
                If you need to instantiate a class with a constructor that takes arguments, use FvRuleBean or FvRuleProvider.
                """.formatted(cls.getName());
            throw new IllegalArgumentException(error, e);
        }
    }
}
