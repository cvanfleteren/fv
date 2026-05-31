package be.iffy.fv.rules;

import be.iffy.fv.ValidationException;
import io.vavr.collection.List;
import be.iffy.fv.ErrorMessage;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import be.iffy.fv.MappingRule;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static be.iffy.fv.rules.ObjectRules.objects;
import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObjectRulesTest {

    @Nested
    class NotNull {

        @Test
        void valid() {
            validTest("hello", objects.notNull());
            validTest("hello", objects.notNull(String.class));
            validTest(123, objects.notNull());
            validTest(123, objects.notNull(Integer.class));
            validTest(new Object(), objects.notNull());
        }

        @Test
        void invalid() {
            invalidTest(null, objects.notNull(), "must.not.be.null");
            invalidTest(null, objects.notNull(String.class), "must.not.be.null");
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
            invalidTest(null, objects.equalTo("a"), "must.not.be.null");
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
            invalidTest(null, objects.notEqualTo("a"), "must.not.be.null");
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
            invalidTest(null, objects.oneOf(1, 2, 3), "must.not.be.null");
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
            invalidTest(null, objects.notOneOf(1, 2, 3), "must.not.be.null");
        }
    }

    @Nested
    class InstanceOf {

        @Test
        void valid() {
            validTest("hello", "hello", objects.instanceOf(String.class));
            validTest(123, 123, objects.instanceOf(Integer.class));
        }

        @Test
        void invalid() {
            MappingRule<Object, String> stringRule = objects.instanceOf(String.class);

            assertThatValidation(stringRule.test(BigDecimal.ZERO))
                    .isInvalid()
                    .hasErrorMessages("must.be.instance");

            assertThatValidation(stringRule.test(null))
                    .isInvalid()
                    .hasErrorMessages("must.not.be.null");
        }
    }

    @Nested
    class IsEnum {

        enum TestEnum {
            A, B
        }

        @Test
        void isEnum_whenValidEnumValue_returnsValid() {
            assertThatValidation(objects.isEnum(TestEnum.class).test("A"))
                    .isValid()
                    .isEqualTo(TestEnum.A);
        }

        @Test
        void isEnum_whenInvalidEnumValue_returnsInvalid() {
            assertThatValidation(objects.isEnum(TestEnum.class).test("C"))
                    .isInvalid()
                    .hasErrorMessage("must.be.valid.enum.value", HashMap.of("value", "C"));
        }
    }

    @Nested
    class CanBeEnum {

        enum TestEnum {
            A, B
        }

        @Test
        void canBeEnum_whenValidEnumValue_returnsValid() {
            assertThatValidation(objects.canBeEnum(TestEnum.class).test("A"))
                    .isValid()
                    .isEqualTo("A");
        }

        @Test
        void canBeEnum_whenInvalidEnumValue_returnsInvalid() {
            assertThatValidation(objects.canBeEnum(TestEnum.class).test("C"))
                    .isInvalid()
                    .hasErrorMessage("must.be.valid.enum.value", HashMap.of("value", "C"));
        }
    }

    @Nested
    class CanBe {

        MappingRule<String, BigDecimal> rule = objects.canBe(BigDecimal::new, ErrorMessage.of("invalid.number"));

        @Test
        void canBe_withErrorMessage_whenValid_returnsValid() {
            assertThatValidation(rule.test("123.45"))
                    .isValid()
                    .isEqualTo(new BigDecimal("123.45"));
        }

        @Test
        void canBe_withErrorMessage_whenInvalid_returnsInvalid() {
            assertThatValidation(rule.test("not-a-number"))
                    .isInvalid()
                    .hasErrorMessage("invalid.number");
        }

        @Test
        void canBe_withErrorKey_whenValid_returnsValid() {
            assertThatValidation(rule.test("123.45"))
                    .isValid()
                    .isEqualTo(new BigDecimal("123.45"));
        }

        @Test
        void canBe_withErrorKey_whenInvalid_returnsInvalid() {
            assertThatValidation(rule.test("not-a-number"))
                    .isInvalid()
                    .hasErrorMessage("invalid.number");
        }

        @Test
        void canBe_withoutErrorMessage_whenValidationExceptionThrown_usesThoseErrors() {
            MappingRule<String, String> throwingRule = objects.canBe(input -> {
                throw new ValidationException(List.of(ErrorMessage.of("custom.error")));
            });

            assertThatValidation(throwingRule.test("any"))
                    .isInvalid()
                    .hasErrorMessage("custom.error");
        }

        @Test
        void canBe_withoutErrorMessage_whenOtherExceptionThrown_usesDefaultError() {
            MappingRule<String, String> throwingRule = objects.canBe(input -> {
                throw new RuntimeException("boom");
            });

            assertThatValidation(throwingRule.test("any"))
                    .isInvalid()
                    .hasErrorMessage("could.not.construct");
        }

        @Test
        void canBe_withoutErrorMessage_whenValid_returnsValid() {
            MappingRule<String, String> successRule = objects.canBe(input -> "prefixed-" + input);

            assertThatValidation(successRule.test("value"))
                    .isValid()
                    .isEqualTo("prefixed-value");
        }
    }
}