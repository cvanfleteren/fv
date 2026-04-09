package net.vanfleteren.fv;

import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import lombok.EqualsAndHashCode;
import lombok.With;

import java.util.Objects;

/**
 * Represents an error message with a unique key, paths, and optional arguments.
 * It is immutable and provides methods to build structured error paths and include dynamic parameters.
 */
@With
@EqualsAndHashCode
public class ErrorMessage {
    private final String errorKey;
    private final List<Path> paths;
    private final Map<String,Object> parameters;

    /**
     * Creates a new {@link ErrorMessage}.
     *
     * @param errorKey   the unique key for the error message (e.g., {@code "invalid.input"}).
     * @param paths      the list of {@link Path} segments leading to the erroneous value.
     * @param parameters a map of dynamic parameters for the error message.
     * @throws NullPointerException if any argument is null.
     */
    public ErrorMessage(String errorKey, List<Path> paths, Map<String, Object> parameters) {
        this.errorKey = Objects.requireNonNull(errorKey, "errorKey cannot be null");
        this.paths = Objects.requireNonNull(paths, "paths cannot be null");
        this.parameters = Objects.requireNonNull(parameters, "parameters cannot be null");
    }

    /**
     * Creates an {@link ErrorMessage} with the given key.
     *
     * @param message the error message key.
     * @return a new {@link ErrorMessage} instance.
     * @throws NullPointerException if {@code message} is null.
     */
    public static ErrorMessage of(String message) {
        return new ErrorMessage(message, List.of(), HashMap.empty());
    }

    /**
     * Creates an {@link ErrorMessage} with the given key and parameters.
     *
     * @param message    the error message key.
     * @param parameters the dynamic parameters.
     * @return a new {@link ErrorMessage} instance.
     * @throws NullPointerException if any argument is null.
     */
    public static ErrorMessage of(String message, Map<String, Object> parameters) {
        return new ErrorMessage(message, List.of(), parameters);
    }

    /**
     * Creates an {@link ErrorMessage} with the given key and a single parameter.
     *
     * @param message the error message key.
     * @param key     the parameter name.
     * @param value   the parameter value.
     * @return a new {@link ErrorMessage} instance.
     * @throws NullPointerException if {@code message} or {@code key} is null.
     */
    public static ErrorMessage of(String message, String key, Object value) {
        return of(message, HashMap.of(key, value));
    }

    /**
     * Prepends a {@link Path} segment to this error message.
     *
     * @param path the path segment to prepend.
     * @return a new {@link ErrorMessage} with the prepended path.
     */
    public ErrorMessage prepend(Path path) {
        if(!paths.isEmpty() && paths.head().index.isDefined() && paths.head().text.isEmpty() && path.index.isEmpty()) {
            // previous path was just an index, an this one hasn't got one, combine them
            return this.withPaths(paths.tail().prepend(path.withIndex(paths.head().index).withText(path.text)));
        } else {
            return this.withPaths(paths.prepend(path));
        }
    }

    /**
     * Associates an index with the current head path segment.
     * If no paths exist, a new empty path segment with the given index is prepended.
     *
     * @param index the index (e.g., collection index or map key).
     * @return a new {@link ErrorMessage} with the index applied.
     */
    public ErrorMessage atIndex(Object index) {
        if (paths.isEmpty()) {
            return prepend(new Path("", Option.of(index)));
        } else {
            return this.withPaths(paths.update(0, paths.head().withIndex(Option.of(index))));
        }
    }

    /**
     * Returns the dynamic parameters for this error message.
     *
     * @return a {@link Map} of parameters.
     */
    public Map<String,Object> parameters() {
        return parameters;
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
        if(parameters.isEmpty()) {
            return message();
        } else {
            return message()+":"+ parameters.map(tuple  -> tuple._1+":"+tuple._2).mkString("{",",","}");
        }
    }

    @Override
    public String toString() {
        return "ErrorMessage{" +
                "errorKey='" + errorKey + '\'' +
                ", paths=" + paths +
                ", parameters=" + parameters +
                '}';
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
