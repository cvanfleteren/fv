package be.iffy.fv.spring;

import be.iffy.fv.ErrorMessage;

import java.util.Map;

/**
 * JSON-serializable representation of a single {@link ErrorMessage}.
 *
 * @param key        the error key without path (e.g. {@code "min.length"}).
 * @param path       the dotted path to the invalid field (e.g. {@code "person.name"}).
 *                   Empty string when no path is present.
 * @param parameters the constraint parameters (e.g. {@code {"min": 3}}).
 *                   Empty map when there are none.
 */
public record ValidationErrorMessage(String key, String path, Map<String, Object> parameters) {

    public static ValidationErrorMessage from(ErrorMessage error) {
        String path = error.paths().map(ErrorMessage.Path::formatted).mkString(".");
        return new ValidationErrorMessage(error.key(), path, error.parameters().toJavaMap());
    }
}
