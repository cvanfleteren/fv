package be.iffy.fv;

import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import lombok.With;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Represents an error message with a unique key, paths, and optional arguments.
 * It is immutable and provides methods to build structured error paths and include dynamic parameters.
 *
 * @param errorKey   the unique key for the error message (e.g., {@code "invalid.input"}).
 * @param paths      the list of {@link Path} segments leading to the erroneous value.
 * @param parameters a map of dynamic parameters for the error message.
 */
public record ErrorMessage(String errorKey, List<Path> paths, Map<String, @Nullable Object> parameters) {

    /**
     * Creates a new {@link ErrorMessage}.
     *
     */
    public ErrorMessage {
        Objects.requireNonNull(errorKey, "errorKey cannot be null");
        Objects.requireNonNull(paths, "paths cannot be null");
        Objects.requireNonNull(parameters, "parameters cannot be null");
    }

    /**
     * Creates an {@link ErrorMessage} with the given key.
     *
     * @param errorKey the error errorKey key.
     */
    public static ErrorMessage of(String errorKey) {
        return new ErrorMessage(errorKey, List.of(), HashMap.empty());
    }

    /**
     * Creates an {@link ErrorMessage} with the given key and parameters.
     *
     * @param message    the error message key.
     * @param parameters the dynamic parameters.
     */
    public static ErrorMessage of(String message, Map<String, Object> parameters) {
        return new ErrorMessage(message, List.of(), parameters);
    }


    /**
     * Creates an {@link ErrorMessage} with the given key and parameters.
     *
     * @param message    the error message key.
     * @param parameters the dynamic parameters.
     */
    public static ErrorMessage of(String message, java.util.Map<String, Object> parameters) {
        return new ErrorMessage(message, List.of(), HashMap.ofAll(parameters));
    }

    /**
     * Creates an {@link ErrorMessage} with the given key and a single parameter.
     *
     * @param message the error message key.
     * @param key     the parameter name.
     * @param value   the parameter value.
     */
    public static ErrorMessage of(String message, String key, @Nullable Object value) {
        return of(message, HashMap.of(key, value));
    }

    /**
     * Prepends a {@link Path} segment to this error message.
     *
     * @param path the path segment to prepend.
     */
    public ErrorMessage prepend(Path path) {
        if (!paths.isEmpty() && paths.head().index.isDefined() && paths.head().text.isEmpty() && path.index.isEmpty()) {
            // previous path was just an index, and this one hasn't got one, combine them
            return this.withPaths(paths.tail().prepend(path.withIndex(paths.head().index).withText(path.text)));
        } else {
            return this.withPaths(paths.prepend(path));
        }
    }

    /**
     * Associates an index with the current head path segment.
     * If no paths exist, a new empty path segment with the given index is prepended.
     *
     * @param index the index (e.g., a collection index or map key).
     */
    public ErrorMessage atIndex(Object index) {
        if (paths.isEmpty()) {
            return prepend(new Path("", Option.of(index)));
        } else {
            return this.withPaths(paths.update(0, paths.head().withIndex(Option.of(index))));
        }
    }

    /**
     * Returns the full error message string, including all path segments and the key.
     *
     * @return the formatted error message.
     */
    public String message() {
        return paths.map(Path::formatted).append(errorKey).mkString(".");
    }

    /**
     * Returns the error key.
     *
     * @return the error key string.
     */
    public String key() {
        return errorKey;
    }

    /**
     * Returns a string representation of the error message including its parameters.
     *
     * <p>Example:
     * <pre>{@code
     * ErrorMessage error = ErrorMessage.of("min.length", "min", 3);
     * String formatted = error.formatted(); // "min.length:{min:3}"
     *
     * // depending on the path and index, you could end up with a formatted string like
     * String s = "root.items.field[1].error.key:{val:foo}";
     * }</pre>
     *
     * @return the formatted error message with parameters.
     */
    public String formatted() {
        if (parameters.isEmpty()) {
            return message();
        } else {
            return message() + ":" + parameters.map(tuple -> tuple._1 + ":" + formatValues(tuple._2)).mkString("{", ",", "}");
        }
    }

    private String formatValues(Object values) {
        return switch (values) {
            case List<?> l -> l.mkString("[", ",", "]");
            default -> values.toString();
        };
    }

    ErrorMessage withPaths(List<Path> paths) {
        return new ErrorMessage(this.errorKey, paths, this.parameters);
    }

    /**
     * Represents a single segment in an error path, optionally including an index.
     * The index would usually be used for array or list indices, or map keys.
     *
     * @param text  the name of the path segment (e.g., field name).
     * @param index an optional index or key within that segment.
     */
    @With
    public record Path(String text, Option<Object> index) {

        /**
         * Creates a {@link Path} segment with the given text and no index.
         *
         * @param path the path segment name.
         * @return a new {@link Path} instance.
         */
        public static Path of(String path) {
            return new Path(path, Option.none());
        }

        /**
         * Returns the formatted string for this path segment.
         *
         * @return the formatted path segment.
         */
        public String formatted() {
            return text + index.map(i -> "[" + i + "]").getOrElse("");
        }
    }
}
