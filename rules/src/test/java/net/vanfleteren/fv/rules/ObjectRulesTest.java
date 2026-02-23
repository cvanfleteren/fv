package net.vanfleteren.fv.rules;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.rules.RulesTest.validTest;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObjectRulesTest {

    @Nested
    class NotNull {

        @Test
        void valid() {
            validTest("hello", ObjectRules.notNull);
            validTest(123, ObjectRules.notNull);
            validTest(new Object(), ObjectRules.notNull);
        }

        @Test
        void invalid() {
            RulesTest.invalidTest(null, ObjectRules.notNull, "cannot.be.null");
        }
    }

    @Nested
    class EqualTo {

        @Test
        void valid() {
            validTest("a", ObjectRules.equalTo("a"));
            validTest(123, ObjectRules.equalTo(123));
        }

        @Test
        void invalid() {
            RulesTest.invalidTest("a", ObjectRules.equalTo("b"), "must.be.equal");
            RulesTest.invalidTest(123, ObjectRules.equalTo(456), "must.be.equal");
        }

        @Test
        void requiresNonNullValue() {
            assertThatThrownBy(() -> ObjectRules.equalTo(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("value cannot be null");
        }
    }

    @Nested
    class NotEqualTo {

        @Test
        void valid() {
            validTest("a", ObjectRules.notEqualTo("b"));
            validTest(123, ObjectRules.notEqualTo(456));
        }

        @Test
        void invalid() {
            RulesTest.invalidTest("a", ObjectRules.notEqualTo("a"), "must.not.be.equal");
            RulesTest.invalidTest(123, ObjectRules.notEqualTo(123), "must.not.be.equal");
        }

        @Test
        void requiresNonNullValue() {
            assertThatThrownBy(() -> ObjectRules.notEqualTo(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("value cannot be null");
        }
    }

    @Nested
    class OneOf {

        @Test
        void valid() {
            validTest("a", ObjectRules.oneOf("a", "b"));
            validTest(1, ObjectRules.oneOf(1, 2, 3));
        }

        @Test
        void invalid() {
            RulesTest.invalidTest("c", ObjectRules.oneOf("a", "b"), "must.be.one.of",
                    HashMap.of("values", HashSet.of("a", "b"))
            );
            RulesTest.invalidTest(4, ObjectRules.oneOf(1, 2, 3), "must.be.one.of",
                    HashMap.of("values", HashSet.of(1, 2, 3))
            );
        }
    }

    @Nested
    class NotOneOf {

        @Test
        void valid() {
            validTest("c", ObjectRules.notOneOf("a", "b"));
            validTest(4, ObjectRules.notOneOf(1, 2, 3));
        }

        @Test
        void invalid() {
            RulesTest.invalidTest("a", ObjectRules.notOneOf("a", "b"), "must.not.be.one.of",
                    HashMap.of("values", HashSet.of("a", "b"))
            );
            RulesTest.invalidTest(2, ObjectRules.notOneOf(1, 2, 3), "must.not.be.one.of",
                    HashMap.of("values", HashSet.of(1, 2, 3))
            );
        }
    }

    @Nested
    class InstanceOf {

        @Test
        void valid() {
            validTest("hello", ObjectRules.instanceOf(String.class));
            validTest(123, ObjectRules.instanceOf(Integer.class));
        }

        @Test
        void invalid() {
            RulesTest.invalidTest(123, ObjectRules.instanceOf(String.class), "must.be.instance",
                    HashMap.of("of", String.class)
            );
            RulesTest.invalidTest("hello", ObjectRules.instanceOf(Integer.class), "must.be.instance",
                    HashMap.of("of", Integer.class)
            );
        }
    }
}