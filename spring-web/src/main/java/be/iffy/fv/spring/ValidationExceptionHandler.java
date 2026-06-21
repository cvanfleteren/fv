package be.iffy.fv.spring;

import be.iffy.fv.ValidationException;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
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
 * <p>Registered automatically via Spring Boot autoconfiguration when this module is on the
 * classpath. Override by defining your own {@link ValidationExceptionHandler} bean.
 */
@RestControllerAdvice
public class ValidationExceptionHandler extends ResponseEntityExceptionHandler {

    static final URI PROBLEM_TYPE =
            URI.create("https://github.com/cvanfleteren/fv/problems/validation-failed");

    /**
     * The case when a {@link ValidationException} is directly thrown through a controller method.
     */
    @ExceptionHandler(ValidationException.class)
    public final ProblemDetail handleValidationException(ValidationException ex) {
        return toProblemDetail(ex);
    }

    /**
     * When a self-validating domain object throws {@link ValidationException} from its constructor
     * during request-body deserialization, the JSON library wraps it and Spring rethrows it as
     * {@link HttpMessageNotReadableException}. We unwrap so these failures produce the same 422
     * Problem Details body as a directly-thrown {@link ValidationException}.
     * Non-validation read failures (malformed JSON, wrong type, etc.) fall through to the default 400.
     */
    @Override
    protected @Nullable ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        ValidationException ve = findValidationException(ex);
        if (ve != null) {
            return handleExceptionInternal(
                    ex, toProblemDetail(ve), headers, HttpStatus.UNPROCESSABLE_ENTITY, request);
        }
        return super.handleHttpMessageNotReadable(ex, headers, status, request);
    }

    /**
     * When a {@code @RequestParam} uses a custom {@link org.springframework.core.convert.converter.Converter}
     * that throws {@link ValidationException}, Spring wraps it in a {@link TypeMismatchException}
     * (via {@link org.springframework.core.convert.ConversionFailedException}).
     * We unwrap the cause chain to produce the same 422 Problem Details body.
     * Other type-mismatch failures fall through to the default 400.
     */
    @Override
    protected @Nullable ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        ValidationException ve = findValidationExceptionFromTypeMismatch(ex);
        if (ve != null) {
            return handleExceptionInternal(
                    ex, toProblemDetail(ve), headers, HttpStatus.UNPROCESSABLE_ENTITY, request);
        }
        return super.handleTypeMismatch(ex, headers, status, request);
    }

    protected ProblemDetail toProblemDetail(ValidationException ex) {
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

    private static ValidationException findValidationException(HttpMessageNotReadableException ex) {
        // this is very implementation-dependent
        // the alternative would be to search every exception for a ValidationException in the cause chain,
        // but then programmer-declared exceptions that purposefully wrapped ValidationException would still get
        // handled by this code, which probably would be surprising for the programmer.
        Throwable cause = ex.getCause();
        if (cause != null) {
            Throwable nested = cause.getCause();
            if (nested instanceof ValidationException ve) {
                return ve;
            }
        }
        return null;
    }

    private static @Nullable ValidationException findValidationExceptionFromTypeMismatch(TypeMismatchException ex) {
        // Spring MVC catches ConversionFailedException and rethrows as MethodArgumentTypeMismatchException,
        // passing the ConversionFailedException's cause (the original converter exception) as the new cause.
        // So the ValidationException may be one or two levels deep depending on Spring version.
        Throwable cause = ex.getCause();
        if (cause instanceof ValidationException ve) {
            return ve;
        }
        if (cause != null && cause.getCause() instanceof ValidationException ve) {
            return ve;
        }
        return null;
    }
}
