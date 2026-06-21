package be.iffy.fv.spring;

import be.iffy.fv.ValidationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpStatus.valueOf;

/**
 * Default {@link ValidationResponseFactory}: produces HTTP 422 Unprocessable Entity in
 * Problem Details (RFC 9457) format with an {@code errors} extension field containing
 * the list of validation errors.
 *
 * <p>Registered automatically via Spring Boot autoconfiguration. Suppressed when the application
 * context contains any other {@link ValidationResponseFactory} bean.
 */
public class DefaultValidationResponseFactory implements ValidationResponseFactory {

    public static final URI PROBLEM_TYPE =
            URI.create("https://github.com/cvanfleteren/fv/problems/validation-failed");

    private final FvSpringWebProperties properties;

    public DefaultValidationResponseFactory(FvSpringWebProperties properties) {
        this.properties = Objects.requireNonNull(properties);
    }

    @Override
    public ResponseEntity<Object> create(ValidationException ex, HttpHeaders headers, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                valueOf(properties.statusCode()),
                "Validation failed with " + ex.errors().size() + " error(s)"
        );
        problem.setType(PROBLEM_TYPE);
        problem.setTitle("Validation Failed");
        List<ValidationErrorMessage> errors = ex.errors()
                .map(ValidationErrorMessage::from)
                .toJavaList();
        problem.setProperty("errors", errors);
        return ResponseEntity.status(properties.statusCode()).headers(headers).body(problem);
    }
}
