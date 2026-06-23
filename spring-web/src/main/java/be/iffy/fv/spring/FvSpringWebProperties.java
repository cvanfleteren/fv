package be.iffy.fv.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration properties for the FV Spring Web integration.
 *
 * <p>These flags influence how validation failures are translated into HTTP responses.
 *
 * <ul>
 *   <li><b>fv.spring.status-code</b> (default: {@code 422}) —
 *       The HTTP status code used for responses produced from validation failures.
 *       This status is applied when a validation error is detected and converted to a Problem Details body.
 *       Set this if you prefer a different non-2xx/4xx code (e.g. {@code 400}) for invalid inputs.</li>
 *
 *   <li><b>fv.spring.handle-type-mismatch</b> (default: {@code true}) —
 *       When {@code true}, type-mismatch errors originating from request parameter conversion that
 *       ultimately stem from a validation failure are unwrapped and returned using the same Problem
 *       Details response and {@code fv.spring.status-code}. When {@code false}, such mismatches fall
 *       back to Spring MVC’s default handling (typically HTTP 400), even if they were caused by a ValidationException.</li>
 * </ul>
 *
 * <pre>{@code
 * fv.spring.status-code=422
 * fv.spring.handle-type-mismatch=true
 * }</pre>
 */
@ConfigurationProperties(prefix = "fv.spring")
public record FvSpringWebProperties(
        @DefaultValue("422") int statusCode,
        @DefaultValue("true") boolean handleTypeMismatch
) {

    public FvSpringWebProperties {
        if (statusCode < 100 || statusCode > 599) {
            throw new IllegalArgumentException(
                    "fv.spring.status-code must be a valid HTTP status code (100–599), got: " + statusCode);
        }
    }

    public static FvSpringWebProperties defaultProperties() {
        return new FvSpringWebProperties(422, true);
    }

}
