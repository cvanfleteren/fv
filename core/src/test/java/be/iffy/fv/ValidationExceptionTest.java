package be.iffy.fv;

import io.vavr.collection.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationExceptionTest {

    @Nested
    class SingleErrorMessage {

        @Test
        void containsSingleError() {
            ErrorMessage error = ErrorMessage.of("field.required");
            ValidationException ex = new ValidationException(error);
            assertThat(ex.errors()).containsExactly(error);
        }

        @Test
        void messageIsErrorKey() {
            ValidationException ex = new ValidationException(ErrorMessage.of("field.required"));
            assertThat(ex.getMessage()).isEqualTo("field.required");
        }
    }

    @Nested
    class JavaUtilList {

        @Test
        void containsAllErrors() {
            ErrorMessage e1 = ErrorMessage.of("field.required");
            ErrorMessage e2 = ErrorMessage.of("field.too_long");
            ValidationException ex = new ValidationException(java.util.List.of(e1, e2));
            assertThat(ex.errors()).containsExactly(e1, e2);
        }

        @Test
        void acceptsMutableList() {
            ErrorMessage error = ErrorMessage.of("field.required");
            java.util.List<ErrorMessage> list = new ArrayList<>();
            list.add(error);
            ValidationException ex = new ValidationException(list);
            assertThat(ex.errors()).containsExactly(error);
        }

        @Test
        void messageIsJoinedErrorKeys() {
            ValidationException ex = new ValidationException(java.util.List.of(
                    ErrorMessage.of("field.required"), ErrorMessage.of("field.too_long")));
            assertThat(ex.getMessage()).isEqualTo("field.required, field.too_long");
        }
    }

    @Nested
    class StringErrorKey {

        @Test
        void containsSingleErrorWithKey() {
            ValidationException ex = new ValidationException("field.required");
            assertThat(ex.errors()).hasSize(1);
            assertThat(ex.errors().head().errorKey()).isEqualTo("field.required");
        }

        @Test
        void messageIsKey() {
            ValidationException ex = new ValidationException("field.required");
            assertThat(ex.getMessage()).isEqualTo("field.required");
        }
    }

    @Nested
    class VavrListConstructor {

        @Test
        void emptyListThrows() {
            assertThatThrownBy(() -> new ValidationException(List.<ErrorMessage>of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Errors must be non-empty");
        }
    }
}
