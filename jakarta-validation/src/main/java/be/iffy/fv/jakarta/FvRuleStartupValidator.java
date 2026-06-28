package be.iffy.fv.jakarta;

import io.vavr.CheckedRunnable;
import io.vavr.collection.List;
import io.vavr.control.Try;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Eagerly validates all {@link FvRule}-, {@link FvStaticRule}-, and {@link FvRuleBean}-annotated
 * elements found in the application's base packages at Spring Boot startup, so misconfiguration
 * (wrong field name, missing constructor, null provider result, etc.) is reported immediately
 * rather than on the first incoming request that triggers validation.
 *
 * <p>Registered automatically via {@link FvRuleAutoConfiguration}. Scans the packages reported
 * by {@link AutoConfigurationPackages} — which is the base package set by
 * {@code @SpringBootApplication} — and inspects every class found there for FV annotations on
 * types, fields, constructor parameters, and method parameters.
 *
 * <p>Annotations that appear in multiple locations on the same class (e.g. a record component
 * that propagates to both the field and the canonical constructor parameter) are validated only
 * once.
 *
 * <p>All problems are collected before throwing, so a single startup failure lists every
 * misconfigured annotation rather than stopping at the first one.
 */
public class FvRuleStartupValidator implements SmartInitializingSingleton {

    private final BeanFactory beanFactory;

    public FvRuleStartupValidator(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterSingletonsInstantiated() {
        List<String> packages = Try.of(() -> AutoConfigurationPackages.get(beanFactory))
            .map(List::ofAll)
            .recover(IllegalStateException.class, List.empty())
            .get();

        List<String> errors = scanAndValidate(packages, beanFactory);
        if (errors.nonEmpty()) {
            throw new IllegalStateException(
                "FV rule annotation misconfiguration detected at startup - fix the following before the application can start:\n  - "
                + errors.mkString("\n  - ")
            );
        }
    }

    /**
     * Scans all classes in the given base packages and validates every {@link FvRule},
     * {@link FvStaticRule}, and {@link FvRuleBean} annotation found on types, fields,
     * constructor parameters, and method parameters. Returns one error string per
     * misconfigured annotation; returns an empty list if everything is valid.
     *
     * <p>Pass {@code null} for {@code beanFactory} when no Spring context is available (e.g.
     * in unit tests); {@link FvRuleBean} annotations are skipped in that case.
     */
    static List<String> scanAndValidate(List<String> basePackages, @Nullable BeanFactory beanFactory) {
        var scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition sbd) {
                return true;
            }
        };
        scanner.addIncludeFilter((metadataReader, metadataReaderFactory) -> true);

        return basePackages
            .flatMap(pkg -> List.ofAll(scanner.findCandidateComponents(pkg)))
            .map(BeanDefinition::getBeanClassName)
            .filter(Objects::nonNull)
            .flatMap(className -> validate(className, beanFactory));
    }

    private static List<String> validate(String className, @Nullable BeanFactory beanFactory) {
        return Try.of(() -> {
            Class<?> type = Class.forName(className);
            Set<Annotation> seen = new HashSet<>();

            List<String> errors = checkElement(className, type, beanFactory, seen);

            for (Field field : type.getDeclaredFields()) {
                errors = errors.appendAll(
                    checkElement(className + "#" + field.getName(), field, beanFactory, seen));
            }
            for (Constructor<?> ctor : type.getDeclaredConstructors()) {
                String ctorLoc = className + ".new()";
                errors = errors.appendAll(checkElement(ctorLoc, ctor, beanFactory, seen));
                for (Parameter param : ctor.getParameters()) {
                    String loc = className + "(" + param.getType().getSimpleName() + " " + param.getName() + ")";
                    errors = errors.appendAll(checkElement(loc, param, beanFactory, seen));
                }
            }
            for (Method method : type.getDeclaredMethods()) {
                String methodLoc = className + "." + method.getName() + "()";
                errors = errors.appendAll(checkElement(methodLoc, method, beanFactory, seen));
                for (Parameter param : method.getParameters()) {
                    String loc = className + "." + method.getName() + "(" + param.getType().getSimpleName() + " " + param.getName() + ")";
                    errors = errors.appendAll(checkElement(loc, param, beanFactory, seen));
                }
            }
            return errors;
        })
        .recover(ClassNotFoundException.class,
            e -> List.of(className + ": could not load class (" + e.getMessage() + ")"))
        .get();
    }

    private static List<String> checkElement(
            String location, AnnotatedElement element, @Nullable BeanFactory beanFactory, Set<Annotation> seen) {
        List<String> errors = List.empty();

        for (FvRule fvRule : element.getAnnotationsByType(FvRule.class)) {
            if (seen.add(fvRule)) {
                errors = errors.appendAll(
                    tryResolve(location, () -> FvRuleValidator.resolveRule(fvRule.value())));
            }
        }

        for (FvStaticRule staticRule : element.getAnnotationsByType(FvStaticRule.class)) {
            if (seen.add(staticRule)) {
                Class<?> holder = staticRule.on() != Void.class ? staticRule.on() : inferHolderClass(element);
                if (holder != null) {
                    errors = errors.appendAll(
                        tryResolve(location, () -> FvStaticRuleValidator.resolveRule(holder, staticRule.field()).get()));
                }
            }
        }

        if (beanFactory != null) {
            for (FvRuleBean ruleBean : element.getAnnotationsByType(FvRuleBean.class)) {
                if (seen.add(ruleBean)) {
                    errors = errors.appendAll(
                        tryResolve(location, () -> FvRuleBeanValidator.resolveBean(ruleBean.value(), beanFactory)));
                }
            }
        }

        return errors;
    }

    /**
     * Infers the class to look up the static rule field on when {@link FvStaticRule#on()} is
     * omitted. Returns the class itself for type-level annotations, the declared type for field
     * and parameter annotations, and {@code null} for annotation types (where the target type
     * is only known at the use site, not at the annotation declaration).
     */
    @Nullable
    private static Class<?> inferHolderClass(AnnotatedElement element) {
        return switch (element) {
            case Class<?> cls when !cls.isAnnotation() -> cls;
            case Field f -> f.getType();
            case Parameter p -> p.getType();
            case Constructor<?> c -> c.getDeclaringClass();
            default -> null;
        };
    }

    private static List<String> tryResolve(String location, CheckedRunnable action) {
        return Try.run(action)
            .map(ignored -> List.<String>empty())
            .recover(IllegalArgumentException.class, e -> List.of(location + ": " + e.getMessage()))
            .get();
    }
}
