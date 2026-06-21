package be.iffy.fv.spring;

import be.iffy.fv.ValidationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

/**
 * Strategy for converting a {@link ValidationException} into an HTTP {@link ResponseEntity}.
 *
 * <p>The autoconfigured default ({@link DefaultValidationResponseFactory}) returns an HTTP 422
 * body in Problem Details (RFC 9457) format with an {@code errors} extension field.
 * Define a {@code @Bean} of this type to replace the default status code, headers, body shape, or content type.
 *
 * <p>Called by all error paths:
 * <ul>
 *   <li>{@link ValidationException} thrown directly from a controller or service</li>
 *   <li>{@code @RequestBody} constructor failures (Jackson deserialization unwrapping)</li>
 *   <li>{@code @RequestParam}/{@code @PathVariable} converter type-mismatch unwrapping</li>
 *   <li>{@code Validation.Invalid} return values from controller methods</li>
 * </ul>
 */
@FunctionalInterface
public interface ValidationResponseFactory {
    ResponseEntity<Object> create(ValidationException ex, HttpHeaders headers, WebRequest request);
}
