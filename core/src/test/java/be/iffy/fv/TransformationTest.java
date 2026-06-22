package be.iffy.fv;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class TransformationTest {

    @Nested
    class Sequence {

        @Test
        void sequence_withTwoTransformations_appliesBothInOrder() {
            Transformation<String> trim = String::trim;
            Transformation<String> toUpperCase = String::toUpperCase;

            Transformation<String> result = Transformation.sequence(trim, toUpperCase);

            assertThat(result.apply("  hello  ")).isEqualTo("HELLO");
        }

        @Test
        void sequence_withAdditionalTransformations_appliesAllInOrder() {
            Transformation<String> trim = String::trim;
            Transformation<String> toUpperCase = String::toUpperCase;
            Transformation<String> addExclamation = s -> s + "!";

            Transformation<String> result = Transformation.sequence(trim, toUpperCase, addExclamation);

            assertThat(result.apply("  hello  ")).isEqualTo("HELLO!");
        }

        @Test
        void sequence_appliesTransformationsInCorrectOrder() {
            Transformation<String> addA = s -> s + "A";
            Transformation<String> addB = s -> s + "B";
            Transformation<String> addC = s -> s + "C";

            Transformation<String> result = Transformation.sequence(addA, addB, addC);

            assertThat(result.apply("")).isEqualTo("ABC");
        }

        @Test
        void sequence_whenFirstIsNull_throwsNullPointerException() {
            Transformation<String> t = s -> s;
            assertThatCode(() -> Transformation.sequence(null, t))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void sequence_whenSecondIsNull_throwsNullPointerException() {
            Transformation<String> t = s -> s;
            assertThatCode(() -> Transformation.sequence(t, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void sequence_whenRestContainsNull_throwsNullPointerException() {
            Transformation<String> t = s -> s;
            assertThatCode(() -> Transformation.sequence(t, t, (Transformation<String>) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
