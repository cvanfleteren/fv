package be.iffy.fv.spring;

import be.iffy.fv.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.List;

/**
 * Catches {@link ValidationException} thrown from Spring MVC controllers and converts it to an
 * HTTP 422 Unprocessable Entity response using the Problem Details format (RFC 7807 / RFC 9457).
 *
 * <p>The response body is a {@link ProblemDetail} with an {@code errors} extension field
 * containing the list of validation errors.
 *
 * <p>Registered automatically via Spring Boot auto-configuration when this module is on the
 * classpath. Override by defining your own {@link ValidationExceptionHandler} bean.
 */
@RestControllerAdvice
public class ValidationExceptionHandler extends ResponseEntityExceptionHandler {

    static final URI PROBLEM_TYPE =
            URI.create("https://github.com/cvanfleteren/fv/problems/validation-failed");

    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleValidationException(ValidationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Validation failed with " + ex.errors().size() + " error(s)"
        );
        problem.setType(PROBLEM_TYPE);
        problem.setTitle("Validation Failed");

        List<ValidationErrorMessage> errors = ex.errors()
                .map(ValidationErrorMessage::from)
                .toJavaList();
        problem.setProperty("errors", errors);

        return problem;
    }
}
