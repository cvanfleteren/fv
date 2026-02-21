package net.vanfleteren.fv;

import io.vavr.collection.List;

import java.util.Objects;

/**
 * Represents an error message with a string message.
 */
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
        return new ErrorMessage(message, paths.prepend(path));
    }

    public String message() {
        return paths.map(Path::text).append(message).mkString(".");
    }

    public record Path(String text) {

        public static Path of(String path) {
            return new Path(path);
        }
    }

}
