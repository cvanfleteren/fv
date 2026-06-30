package be.iffy.fv.jakarta;

import be.iffy.fv.Rule;

import io.vavr.control.Try;

import java.lang.reflect.InvocationTargetException;

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
        return Try.of(() -> cls.getDeclaredConstructor().newInstance())
            .recoverWith(
                NoSuchMethodException.class,
                e -> {
                    throw new IllegalArgumentException(
                        cls.getName() + " has no public no-arg constructor; "
                        + "use @FvRuleBean for injected dependencies or @FvStaticRule for a static field rule",
                        e);
                }
            )
            .recoverWith(
                InvocationTargetException.class,
                e -> {
                    Throwable cause = e.getCause();
                    throw new IllegalArgumentException(
                        cls.getName() + " constructor threw " + (cause != null ? cause.getClass().getSimpleName() : "an exception"),
                        cause != null ? cause : e);
                }
            )
            .getOrElseThrow(t -> {
                String error = """
                    Cannot instantiate %s - ensure it has a public no-arg constructor that does not throw.
                    If the class needs injected dependencies, use @FvRuleBean instead.
                    If the rule is already stored as a static field, use @FvStaticRule instead.
                    """.formatted(cls.getName());
                throw new IllegalArgumentException(error, t);
            });
    }
}
