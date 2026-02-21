package net.vanfleteren.fv;

import io.vavr.collection.List;
import io.vavr.control.Option;
import lombok.With;

import java.util.Objects;

/**
 * Represents an error message with a string message.
 */
@With
public class ErrorMessage {
    private final String message;
    private final List<Path> paths;

    public ErrorMessage(String message, List<Path> paths) {
        this.message = Objects.requireNonNull(message, "Message cannot be null");
        this.paths = Objects.requireNonNull(paths, "Paths cannot be null");
    }


    public static ErrorMessage of(String message) {
        return new ErrorMessage(message, List.of());
    }

    public ErrorMessage prepend(Path path) {
        if(!paths.isEmpty() && paths.head().index.isDefined() && paths.head().text.isEmpty() && path.index.isEmpty()) {
            // previous path was just an index, an this one hasn't got one, combine them
            return new ErrorMessage(message, paths.tail().prepend(path.withIndex(paths.head().index).withText(path.text)));
        } else {
            return new ErrorMessage(message, paths.prepend(path));
        }
    }

    public ErrorMessage atIndex(int index) {
        if (paths.isEmpty()) {
            return prepend(new Path("", Option.of(index)));
        } else {
            return this.withPaths(List.of(paths.head().withIndex(Option.of(index))).appendAll(paths.tail()));
        }
    }

    public String message() {
        return paths.map(Path::formatted).append(message).mkString(".");
    }

    @With
    public record Path(String text, Option<Integer> index) {

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
