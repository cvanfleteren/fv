package be.iffy.fv.jakarta;

import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Eagerly validates all {@link FvRule}-, {@link FvStaticRule}-, and {@link FvRuleBean}-annotated
 * types found on the classpath at Spring Boot startup, so misconfiguration (wrong field name,
 * missing constructor, null provider result, etc.) is reported immediately rather than on the
 * first incoming request that triggers validation.
 *
 * <p>Registered automatically via {@link FvRuleAutoConfiguration}. Scans the packages reported
 * by {@link AutoConfigurationPackages} — which is the base package set by
 * {@code @SpringBootApplication} — so it covers the same scope as component scanning.
 *
 * <p>All problems are collected before throwing, so a single startup failure lists every
 * misconfigured type rather than stopping at the first one.
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
                "FV rule annotation misconfiguration detected at startup — fix the following before the application can start:\n  - "
                + errors.mkString("\n  - ")
            );
        }
    }

    /**
     * Scans the given base packages for types annotated with {@link FvRule}, {@link FvStaticRule},
     * or {@link FvRuleBean} and attempts to resolve each annotation's rule configuration.
     * Returns one error string per misconfigured type; returns an empty list if everything is valid.
     *
     * <p>Pass {@code null} for {@code beanFactory} when no Spring context is available (e.g.
     * in unit tests); {@link FvRuleBean} annotations are skipped in that case.
     */
    static List<String> scanAndValidate(List<String> basePackages, @Nullable BeanFactory beanFactory) {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(FvRule.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(FvStaticRule.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(FvRuleBean.class));

        return basePackages
            .flatMap(pkg -> List.ofAll(scanner.findCandidateComponents(pkg)))
            .map(BeanDefinition::getBeanClassName)
            .filter(Objects::nonNull)
            .flatMap(className -> validate(className, beanFactory));
    }

    private static Option<String> validate(String className, @Nullable BeanFactory beanFactory) {
        return Try.run(() -> {
                Class<?> type = Class.forName(className);

                FvRule fvRule = type.getAnnotation(FvRule.class);
                if (fvRule != null) {
                    FvRuleValidator.resolveRule(fvRule.value());
                }

                FvStaticRule staticRule = type.getAnnotation(FvStaticRule.class);
                if (staticRule != null) {
                    FvStaticRuleValidator.resolveRule(staticRule.on(), staticRule.field()).get();
                }

                FvRuleBean ruleBean = type.getAnnotation(FvRuleBean.class);
                if (ruleBean != null && beanFactory != null) {
                    FvRuleBeanValidator.resolveBean(ruleBean.value(), beanFactory);
                }
            })
            .map(ignored -> Option.<String>none())
            .recover(ClassNotFoundException.class,
                e -> Option.some(className + ": could not load class (" + e.getMessage() + ")"))
            .recover(IllegalArgumentException.class,
                e -> Option.some(className + ": " + e.getMessage()))
            .get();
    }
}
