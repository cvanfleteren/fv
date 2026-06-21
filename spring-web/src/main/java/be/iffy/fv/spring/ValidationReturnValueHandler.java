package be.iffy.fv.spring;

import be.iffy.fv.Validation;
import be.iffy.fv.ValidationException;
import io.vavr.Lazy;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.List;

/**
 * Handles controller methods that return {@link Validation}{@code <T>}:
 * <ul>
 *     <li>{@link Validation.Valid}: unwraps the value and serializes it as if the method had
 *         returned {@code T} directly (same behaviour as {@code @ResponseBody}).</li>
 *     <li>{@link Validation.Invalid}: throws {@link ValidationException} so that
 *         {@link ValidationExceptionHandler} converts it to an HTTP 422 response with the same
 *         Problem Details body shape.</li>
 * </ul>
 *
 * <p>Registered automatically via Spring Boot autoconfiguration when this module is on the
 * classpath. Override by defining your own {@link ValidationReturnValueHandler} bean.
 */
public class ValidationReturnValueHandler implements HandlerMethodReturnValueHandler {

    private final Lazy<RequestResponseBodyMethodProcessor> delegate;

    public ValidationReturnValueHandler(RequestMappingHandlerAdapter handlerAdapter) {
        // Resolve lazily so we don't touch the adapter until it is fully initialized.
        // Reusing the existing processor preserves ResponseBodyAdvice configured on the adapter.
        this.delegate = Lazy.of(() -> resolveDelegate(handlerAdapter));
    }

    /**
     * Reuses the {@link RequestResponseBodyMethodProcessor} already in the adapter's handler chain
     * so that {@code ResponseBodyAdvice} beans apply to the {@code Valid<T>} path the same way
     * they do for a plain {@code @ResponseBody} return. Falls back to a fresh processor (converters
     * only, no advice) if none is found.
     */
    private static RequestResponseBodyMethodProcessor resolveDelegate(RequestMappingHandlerAdapter handlerAdapter) {
        List<HandlerMethodReturnValueHandler> handlers = handlerAdapter.getReturnValueHandlers();
        if (handlers != null) {
            for (HandlerMethodReturnValueHandler handler : handlers) {
                if (handler instanceof RequestResponseBodyMethodProcessor processor) {
                    return processor;
                }
            }
        }
        return new RequestResponseBodyMethodProcessor(handlerAdapter.getMessageConverters());
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return Validation.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest) throws Exception {
        if (returnValue == null) {
            mavContainer.setRequestHandled(true);
            return;
        }

        Validation<?> validation = (Validation<?>) returnValue;
        if (validation.isValid()) {
            delegate.get().handleReturnValue(
                ((Validation.Valid<?>) validation).value(),
                returnType.nested(),
                mavContainer,
                webRequest
            );
        } else {
            throw new ValidationException(validation.errors());
        }
    }

}
