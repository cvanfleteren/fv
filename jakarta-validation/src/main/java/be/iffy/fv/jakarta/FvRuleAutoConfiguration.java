package be.iffy.fv.jakarta;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

/**
 * Spring Boot autoconfiguration that registers {@link FvRuleStartupValidator} to eagerly
 * validate all {@link FvRule}-annotated types at startup.
 *
 * <p>Active only when Spring's classpath scanning infrastructure is present.
 * Can be disabled via {@code fv.rule.startup-scan.enabled=false}.
 */
@AutoConfiguration
@ConditionalOnClass(ClassPathScanningCandidateComponentProvider.class)
public class FvRuleAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "fv.rule", name = "startup-scan.enabled", matchIfMissing = true)
    public FvRuleStartupValidator fvRuleStartupValidator() {
        return new FvRuleStartupValidator();
    }
}
