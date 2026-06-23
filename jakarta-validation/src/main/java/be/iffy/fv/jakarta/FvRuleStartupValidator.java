package be.iffy.fv.jakarta;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Eagerly validates all {@link FvRule}-annotated types found on the classpath at Spring Boot
 * startup, so misconfiguration (wrong field name, missing constructor, null provider result, etc.)
 * is reported immediately rather than on the first incoming request that triggers validation.
 *
 * <p>Registered automatically via {@link FvRuleAutoConfiguration}. Scans the packages reported
 * by {@link AutoConfigurationPackages} — which is the base package set by
 * {@code @SpringBootApplication} — so it covers the same scope as component scanning.
 *
 * <p>All problems are collected before throwing, so a single startup failure lists every
 * misconfigured type rather than stopping at the first one.
 */
public class FvRuleStartupValidator implements SmartInitializingSingleton, BeanFactoryAware {

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterSingletonsInstantiated() {
        List<String> packages;
        try {
            packages = AutoConfigurationPackages.get(beanFactory);
        } catch (IllegalStateException ignored) {
            return;
        }
        if (packages.isEmpty()) {
            return;
        }

        List<String> errors = scanAndValidate(packages);
        if (!errors.isEmpty()) {
            throw new IllegalStateException(
                "@FvRule misconfiguration detected at startup — fix the following before the application can start:\n  - "
                + String.join("\n  - ", errors)
            );
        }
    }

    /**
     * Scans the given base packages for types annotated with {@link FvRule} and attempts to
     * resolve each annotation's rule configuration. Returns one error string per misconfigured
     * type; returns an empty list if everything is valid.
     */
    static List<String> scanAndValidate(List<String> basePackages) {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(FvRule.class));

        var errors = new ArrayList<String>();

        for (String pkg : basePackages) {
            for (var bd : scanner.findCandidateComponents(pkg)) {
                String className = bd.getBeanClassName();
                try {
                    Class<?> type = Class.forName(className);
                    FvRule annotation = type.getAnnotation(FvRule.class);
                    if (annotation != null) {
                        FvRuleValidator.resolveRule(annotation);
                    }
                } catch (ClassNotFoundException e) {
                    errors.add(className + ": could not load class (" + e.getMessage() + ")");
                } catch (IllegalArgumentException e) {
                    errors.add(className + ": " + e.getMessage());
                }
            }
        }

        return errors;
    }
}
