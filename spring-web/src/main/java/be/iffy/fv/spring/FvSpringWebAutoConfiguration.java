package be.iffy.fv.spring;

import be.iffy.fv.ValidationException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring Boot autoconfiguration that registers {@link ValidationExceptionHandler},
 * {@link DefaultValidationResponseFactory} and {@link ValidationReturnValueHandler} when:
 * <ul>
 *     <li>The application is a Servlet-based web application.</li>
 *     <li>{@link ValidationException} is on the classpath.</li>
 *     <li>No user-defined bean of the respective type already exists.</li>
 * </ul>
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({DispatcherServlet.class, ValidationException.class})
@EnableConfigurationProperties(FvSpringWebProperties.class)
public class FvSpringWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ValidationResponseFactory.class)
    public DefaultValidationResponseFactory defaultValidationResponseFactory(FvSpringWebProperties properties) {
        return new DefaultValidationResponseFactory(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ValidationExceptionHandler validationExceptionHandler(
            FvSpringWebProperties properties, ValidationResponseFactory responseFactory) {
        return new ValidationExceptionHandler(properties, responseFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public ValidationReturnValueHandler validationReturnValueHandler(
            @Lazy RequestMappingHandlerAdapter handlerAdapter) {
        return new ValidationReturnValueHandler(handlerAdapter);
    }

    /**
     * Inserts {@link ValidationReturnValueHandler} immediately before
     * {@link RequestResponseBodyMethodProcessor} in the adapter's handler chain.
     *
     * <p>{@code WebMvcConfigurer.addReturnValueHandlers} appends after built-in handlers,
     * so {@code RequestResponseBodyMethodProcessor} would otherwise claim
     * {@code Validation<T>} first (because {@code @RestController} adds {@code @ResponseBody}).
     * Using {@link SmartInitializingSingleton} lets us modify the fully-initialized adapter
     * list after all beans are ready.
     */
    @Bean
    public SmartInitializingSingleton fvReturnValueHandlerInstaller(
            @Lazy RequestMappingHandlerAdapter handlerAdapter,
            ValidationReturnValueHandler returnValueHandler) {
        return () -> {
            List<HandlerMethodReturnValueHandler> current = handlerAdapter.getReturnValueHandlers();
            if (current == null) {
                return;
            }
            List<HandlerMethodReturnValueHandler> handlers = new ArrayList<>(current);
            for (int i = 0; i < handlers.size(); i++) {
                if (handlers.get(i) instanceof RequestResponseBodyMethodProcessor) {
                    handlers.add(i, returnValueHandler);
                    handlerAdapter.setReturnValueHandlers(handlers);
                    return;
                }
            }
            handlers.add(returnValueHandler);
            handlerAdapter.setReturnValueHandlers(handlers);
        };
    }
}
