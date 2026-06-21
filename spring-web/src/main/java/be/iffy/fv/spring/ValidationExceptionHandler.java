package be.iffy.fv.spring;

import be.iffy.fv.ValidationException;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Objects;

/**
 * Catches {@link ValidationException} thrown from Spring MVC controllers and converts it to an
 * HTTP response via the configured {@link ValidationResponseFactory}.
 *
 * <p>By default produces HTTP 422 Unprocessable Entity in Problem Details format (RFC 9457).
 * To change the response body, headers, or status code, provide a {@link ValidationResponseFactory}
 * {@code @Bean} — it is called by all four error paths:
 * <ul>
 *   <li>{@link ValidationException} thrown directly from a controller or service</li>
 *   <li>{@code Validation.Invalid} return values from controller methods</li>
 *   <li>{@code @RequestBody} constructor failures (Jackson deserialization unwrapping)</li>
 *   <li>{@code @RequestParam}/{@code @PathVariable} converter type-mismatch unwrapping</li>
 * </ul>
 * The last behavior can be configured to return to default Spring handling (returning a default 400 response) by setting
 * {@code fv.spring.handle-type-mismatch} to {@code false}.
 *
 * <p>Registered automatically via Spring Boot autoconfiguration when this module is on the
 * classpath. To suppress the autoconfigured bean entirely, define any bean of this type.
 */
@RestControllerAdvice
public class ValidationExceptionHandler extends ResponseEntityExceptionHandler {

    protected final FvSpringWebProperties properties;
    protected final ValidationResponseFactory responseFactory;

    public ValidationExceptionHandler(FvSpringWebProperties properties, ValidationResponseFactory responseFactory) {
        this.properties = Objects.requireNonNull(properties);
        this.responseFactory = Objects.requireNonNull(responseFactory);
    }

    /**
     * The case when a {@link ValidationException} is directly thrown through a controller method.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidationException(ValidationException ex, WebRequest request) {
        return responseFactory.create(ex, new HttpHeaders(), request);
    }

    /**
     * When a self-validating domain object throws {@link ValidationException} from its constructor
     * during request-body deserialization, the JSON library wraps it and Spring rethrows it as
     * {@link HttpMessageNotReadableException}. We search the cause chain for a
     * {@link ValidationException} so these failures produce the same response as a directly-thrown
     * {@link ValidationException}. Non-validation read failures (malformed JSON, wrong type, etc.)
     * fall through to the default 400.
     */
    @Override
    protected @Nullable ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        ValidationException ve = findValidationException(ex);
        if (ve != null) {
            return responseFactory.create(ve, headers, request);
        }
        return super.handleHttpMessageNotReadable(ex, headers, status, request);
    }

    /**
     * When a {@code @RequestParam} or {@code @PathVariable} uses a custom
     * {@link org.springframework.core.convert.converter.Converter} that throws
     * {@link ValidationException}, Spring wraps it in a {@link TypeMismatchException}.
     * We search the cause chain and delegate to the configured factory.
     * Other type-mismatch failures fall through to the default 400.
     * When {@code fv.spring.handle-type-mismatch} is {@code false}, all type mismatches
     * fall through to the default 400, even if caused by a {@link ValidationException}.
     */
    @Override
    protected @Nullable ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        if (properties.handleTypeMismatch()) {
            ValidationException ve = findValidationException(ex);
            if (ve != null) {
                return responseFactory.create(ve, headers, request);
            }
        }
        return super.handleTypeMismatch(ex, headers, status, request);
    }

    // looks for ValidationException anywhere in the cause chain
    private static @Nullable ValidationException findValidationException(Throwable t) {
        for (Throwable cause = t.getCause(); cause != null; cause = cause.getCause()) {
            if (cause instanceof ValidationException ve) {
                return ve;
            }
        }
        return null;
    }
}
