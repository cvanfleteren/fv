package net.vanfleteren.fv.rules;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import net.vanfleteren.fv.Rule;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static net.vanfleteren.fv.assertj.ValidationAssert.assertThatValidation;
import static net.vanfleteren.fv.rules.ObjectRules.objects;
import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObjectRulesTest {

    @Nested
    class NotNull {

        @Test
        void valid() {
            validTest("hello", objects.notNull());
            validTest(123, objects.notNull());
            validTest(new Object(), objects.notNull());
        }

        @Test
        void invalid() {
            invalidTest(null, objects.notNull(), "cannot.be.null");
        }
    }

    @Nested
    class EqualTo {

        @Test
        void valid() {
            validTest("a", objects.equalTo("a"));
            validTest(123, objects.equalTo(123));
        }

        @Test
        void invalid() {
            invalidTest("a", objects.equalTo("b"), "must.be.equal");
            invalidTest(123, objects.equalTo(456), "must.be.equal");
        }

        @Test
        void requiresNonNullValue() {
            assertThatThrownBy(() -> objects.equalTo(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("value cannot be null");
        }
    }

    @Nested
    class NotEqualTo {

        @Test
        void valid() {
            validTest("a", objects.notEqualTo("b"));
            validTest(123, objects.notEqualTo(456));
        }

        @Test
        void invalid() {
            invalidTest("a", objects.notEqualTo("a"), "must.not.be.equal");
            invalidTest(123, objects.notEqualTo(123), "must.not.be.equal");
        }

        @Test
        void requiresNonNullValue() {
            assertThatThrownBy(() -> objects.notEqualTo(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("value cannot be null");
        }
    }

    @Nested
    class OneOf {

        @Test
        void valid() {
            validTest("a", objects.oneOf("a", "b"));
            validTest(1, objects.oneOf(1, 2, 3));
        }

        @Test
        void invalid() {
            invalidTest("c", objects.oneOf("a", "b"), "must.be.one.of",
                    HashMap.of("values", HashSet.of("a", "b"))
            );
            invalidTest(4, objects.oneOf(1, 2, 3), "must.be.one.of",
                    HashMap.of("values", HashSet.of(1, 2, 3))
            );
        }
    }

    @Nested
    class NotOneOf {

        @Test
        void valid() {
            validTest("c", objects.notOneOf("a", "b"));
            validTest(4, objects.notOneOf(1, 2, 3));
        }

        @Test
        void invalid() {
            invalidTest("a", objects.notOneOf("a", "b"), "must.not.be.one.of",
                    HashMap.of("values", HashSet.of("a", "b"))
            );
            invalidTest(2, objects.notOneOf(1, 2, 3), "must.not.be.one.of",
                    HashMap.of("values", HashSet.of(1, 2, 3))
            );
        }
    }

    @Nested
    class InstanceOf {

        @Test
        void valid() {
            validTest("hello", objects.instanceOf(String.class));
            validTest(123, objects.instanceOf(Integer.class));
        }

        @Test
        void invalid() {
            Rule<Object> stringRule = objects.instanceOf(String.class);

            assertThatValidation(stringRule.test(BigDecimal.ZERO))
                    .isInvalid()
                    .hasErrorMessages("must.be.instance");
        }
    }
}