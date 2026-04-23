package be.iffy.fv;

import io.vavr.collection.Map;
import io.vavr.collection.TreeMap;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorMessageTest {

    @Nested
    class Formatted {

        @Test
        void formatted_whenOnlyErrorKey_returnsErrorKey() {
            // Arrange
            ErrorMessage errorMessage = ErrorMessage.of("error.key");

            // Act & Assert
            assertThat(errorMessage.formatted()).isEqualTo("error.key");
            assertThat(errorMessage.key()).isEqualTo("error.key");
        }

        @Test
        void formatted_whenErrorKeyAndArgs_returnsFormattedMessageWithArgs() {
            // Arrange
            Map<String, Object> args = TreeMap.of("min", 5, "actual", 3);
            ErrorMessage errorMessage = ErrorMessage.of("error.key", args);

            // Act
            String result = errorMessage.formatted();

            // Assert
            assertThat(result).isEqualTo("error.key:{actual:3,min:5}");
            assertThat(errorMessage.key()).isEqualTo("error.key");
        }

        @Test
        void formatted_whenErrorKeyAndSingleArg_returnsFormattedMessageWithArg() {
            // Arrange
            ErrorMessage errorMessage = ErrorMessage.of("error.key", "min", 5);

            // Act
            String result = errorMessage.formatted();

            // Assert
            assertThat(result).isEqualTo("error.key:{min:5}");
            assertThat(errorMessage.key()).isEqualTo("error.key");
        }

        @Test
        void formatted_whenPathAndErrorKey_returnsDottedPathAndErrorKey() {
            // Arrange
            ErrorMessage errorMessage = ErrorMessage.of("error.key")
                    .prepend(ErrorMessage.Path.of("field"));

            // Act
            String result = errorMessage.formatted();

            // Assert
            assertThat(result).isEqualTo("field.error.key");
            assertThat(errorMessage.key()).isEqualTo("error.key");
        }

        @Test
        void formatted_whenMultiplePathsAndErrorKey_returnsDottedPathsAndErrorKey() {
            // Arrange
            ErrorMessage errorMessage = ErrorMessage.of("error.key")
                    .prepend(ErrorMessage.Path.of("field"))
                    .prepend(ErrorMessage.Path.of("parent"));

            // Act
            String result = errorMessage.formatted();

            // Assert
            assertThat(result).isEqualTo("parent.field.error.key");
            assertThat(errorMessage.key()).isEqualTo("error.key");
        }

        @Test
        void formatted_whenAnonymousIndexAndThenPath_returnsCombinedPathAndIndex() {
            // Arrange
            ErrorMessage errorMessage = ErrorMessage.of("error.key")
                    .atIndex(0)
                    .prepend(ErrorMessage.Path.of("list"));

            // Act
            String result = errorMessage.formatted();

            // Assert
            assertThat(result).isEqualTo("list[0].error.key");
            assertThat(errorMessage.key()).isEqualTo("error.key");
        }

        @Test
        void formatted_whenPathAndThenIndex_returnsCombinedPathAndIndex() {
            // Arrange
            ErrorMessage errorMessage = ErrorMessage.of("error.key")
                    .prepend(ErrorMessage.Path.of("list"))
                    .atIndex(0);

            // Act
            String result = errorMessage.formatted();

            // Assert
            assertThat(result).isEqualTo("list[0].error.key");
            assertThat(errorMessage.key()).isEqualTo("error.key");
        }

        @Test
        void formatted_whenComplexNesting_returnsFullFormattedString() {
            // Arrange
            ErrorMessage errorMessage = ErrorMessage.of("error.key", "val", "foo")
                    .prepend(ErrorMessage.Path.of("field"))
                    .atIndex(1)
                    .prepend(ErrorMessage.Path.of("items"))
                    .prepend(ErrorMessage.Path.of("root"));

            // Act
            String result = errorMessage.formatted();

            // Assert
            assertThat(result).isEqualTo("root.items.field[1].error.key:{val:foo}");
            assertThat(errorMessage.key()).isEqualTo("error.key");
        }
    }
}