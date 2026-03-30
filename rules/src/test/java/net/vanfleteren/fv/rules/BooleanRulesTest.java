package net.vanfleteren.fv.rules;

import net.vanfleteren.fv.Validation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.rules.BooleanRules.booleans;
import static org.assertj.core.api.Assertions.assertThat;

class BooleanRulesTest {

    @Nested
    class IsTrue {

        @Test
        void isTrue_whenGivenTrue_returnsValid() {
            Validation<Boolean> v = booleans.isTrue.test(true);

            assertThat(v.isValid()).isTrue();
        }

        @Test
        void isTrue_whenGivenFalse_returnsInvalidWithErrorKey() {
            Validation<Boolean> v = booleans.isTrue.test(false);

            assertThat(v.isInvalid()).isTrue();
            assertThat(v.errors().map(e -> e.key()).toJavaList())
                    .containsExactly("must.be.true");
        }

        @Test
        void isTrue_whenGivenNull_returnsInvalidWithErrorKey() {
            Validation<Boolean> v = booleans.isTrue.test(null);

            assertThat(v.isInvalid()).isTrue();
            assertThat(v.errors().map(e -> e.key()).toJavaList())
                    .containsExactly("must.be.true");
        }
    }

    @Nested
    class IsFalse {

        @Test
        void isFalse_whenGivenFalse_returnsValid() {
            Validation<Boolean> v = booleans.isFalse.test(false);

            assertThat(v.isValid()).isTrue();
        }

        @Test
        void isFalse_whenGivenTrue_returnsInvalidWithErrorKey() {
            Validation<Boolean> v = booleans.isFalse.test(true);

            assertThat(v.isInvalid()).isTrue();
            assertThat(v.errors().map(e -> e.key()).toJavaList())
                    .containsExactly("must.be.false");
        }

        @Test
        void isFalse_whenGivenNull_returnsInvalidWithErrorKey() {
            Validation<Boolean> v = booleans.isFalse.test(null);

            assertThat(v.isInvalid()).isTrue();
            assertThat(v.errors().map(e -> e.key()).toJavaList())
                    .containsExactly("must.be.false");
        }
    }

    @Nested
    class NotNull {

        @Test
        void notNull_whenGivenNonNull_returnsValid() {
            assertThat(booleans.notNull.test(Boolean.TRUE).isValid()).isTrue();
            assertThat(booleans.notNull.test(Boolean.FALSE).isValid()).isTrue();
        }

        @Test
        void notNull_whenGivenNull_returnsInvalidWithErrorKey() {
            Validation<Boolean> v = booleans.notNull.test(null);

            assertThat(v.isInvalid()).isTrue();
            assertThat(v.errors().map(e -> e.key()).toJavaList())
                    .containsExactly("must.not.be.null");
        }
    }
}
