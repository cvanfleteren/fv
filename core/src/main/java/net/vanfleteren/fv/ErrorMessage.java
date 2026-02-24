package net.vanfleteren.fv;

import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import lombok.EqualsAndHashCode;
import lombok.With;

import java.util.Objects;

/**
 * Represents an error message with a string message.
 */
@With
@EqualsAndHashCode
public class ErrorMessage {
    private final String errorKey;
    private final List<Path> paths;
    private final Map<String,Object> args;

    public ErrorMessage(String errorKey, List<Path> paths, Map<String,Object> args) {
        this.errorKey = Objects.requireNonNull(errorKey, "Message cannot be null");
        this.paths = Objects.requireNonNull(paths, "Paths cannot be null");
        this.args = Objects.requireNonNull(args, "Args cannot be null");
    }

    public static ErrorMessage of(String message) {
        return new ErrorMessage(message, List.of(), HashMap.empty());
    }

    public static ErrorMessage of(String message, Map<String,Object> args) {
        return new ErrorMessage(message, List.of(), args);
    }

    public static ErrorMessage of(String message, String key, Object value) {
        return of(message, HashMap.of(key, value));
    }

    public ErrorMessage prepend(Path path) {
        if(!paths.isEmpty() && paths.head().index.isDefined() && paths.head().text.isEmpty() && path.index.isEmpty()) {
            // previous path was just an index, an this one hasn't got one, combine them
            return this.withPaths(paths.tail().prepend(path.withIndex(paths.head().index).withText(path.text)));
        } else {
            return this.withPaths(paths.prepend(path));
        }
    }

    public ErrorMessage atIndex(Object index) {
        if (paths.isEmpty()) {
            return prepend(new Path("", Option.of(index)));
        } else {
            return this.withPaths(List.of(paths.head().withIndex(Option.of(index))).appendAll(paths.tail()));
        }
    }

    public Map<String,Object> args() {
        return args;
    }

    public String message() {
        return paths.map(Path::formatted).append(errorKey).mkString(".");
    }

    public String key() {
        return errorKey;
    }

    public String formatted() {
        if(args.isEmpty()) {
            return message();
        } else {
            return message()+":"+args.map(tuple  -> tuple._1+":"+tuple._2).mkString("{",",","}");
        }
    }

    @With
    public record Path(String text, Option<Object> index) {

        public static Path of(String path) {
            return new Path(path, Option.none());
        }

        public String formatted() {
            // no specific name was given
            if ("".equals(text)) {
                return index.map(i -> "[" + i + "]").getOrElse("");
            } else {
                return text + index.map(i -> "[" + i + "]").getOrElse("");
            }
        }
    }
}
