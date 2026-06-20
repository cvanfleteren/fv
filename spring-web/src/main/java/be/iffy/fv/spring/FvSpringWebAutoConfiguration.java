package be.iffy.fv.spring;

import be.iffy.fv.ValidationException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Spring Boot auto-configuration that registers {@link ValidationExceptionHandler} when:
 * <ul>
 *     <li>The application is a Servlet-based web application.</li>
 *     <li>{@link ValidationException} is on the classpath.</li>
 *     <li>No user-defined {@link ValidationExceptionHandler} bean already exists.</li>
 * </ul>
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({DispatcherServlet.class, ValidationException.class})
public class FvSpringWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ValidationExceptionHandler validationExceptionHandler() {
        return new ValidationExceptionHandler();
    }
}
