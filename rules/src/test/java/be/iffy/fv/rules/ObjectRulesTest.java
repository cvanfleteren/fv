package be.iffy.fv.rules;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.ValidationException;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static be.iffy.fv.rules.ObjectRules.objects;
import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;
import static be.iffy.fv.rules.text.StringRules.strings;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObjectRulesTest {

    @Nested
    class NotNull {

        @Test
        void valid() {
            validTest("hello", objects.notNull());
            validTest("hello", objects.notNull(String.class));
            validTest("hello", strings.notNull());
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
    class AsOptional {

        @Test
        void valid() {
            assertThatValidation(objects.asOptional().apply("a"))
                    .isValid()
                    .isEqualTo(java.util.Optional.of("a"));

            assertThatValidation(objects.asOptional().apply(null))
                    .isValid()
                    .isEqualTo(java.util.Optional.empty());
        }
    }

    @Nested
    class AsOption {

        @Test
        void valid() {
            assertThatValidation(objects.asOption().apply("a"))
                    .isValid()
                    .isEqualTo(io.vavr.control.Option.of("a"));

            assertThatValidation(objects.asOption().apply(null))
                    .isValid()
                    .isEqualTo(io.vavr.control.Option.none());
        }
    }

    @Nested
    class EqualTo {

        @Test
        void valid() {

            Rule<String> same = objects.equalTo("a");

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
    class SameAs {

        @Test
        void valid() {
            String s = "a";
            validTest(s, objects.sameAs(s));
        }

        @Test
        void invalid() {
            invalidTest("a", objects.sameAs(new String("a")), "must.be.same");
            invalidTest(null, objects.sameAs("a"), "must.not.be.null");
        }

        @Test
        void requiresNonNullValue() {
            assertThatThrownBy(() -> objects.sameAs(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("value cannot be null");
        }
    }

    @Nested
    class NotSameAs {

        @Test
        void valid() {
            validTest("a", objects.notSameAs(new String("a")));
        }

        @Test
        void invalid() {
            String s = "a";
            invalidTest(s, objects.notSameAs(s), "must.not.be.same");
            invalidTest(null, objects.notSameAs("a"), "must.not.be.null");
        }

        @Test
        void requiresNonNullValue() {
            assertThatThrownBy(() -> objects.notSameAs(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("value cannot be null");
        }
    }

    @Nested
    class AsString {

        @Test
        void valid() {
            validTest(123, "123", objects.asString());
            validTest("abc", "abc", objects.asString());
        }

        @Test
        void invalid() {
            invalidTest(null, objects.asString(), "must.not.be.null");
        }
    }

    @Nested
    class IsInstanceOf {

        @Test
        void isInstanceOf_whenValidType_returnsValid() {
            validTest("hello", "hello", objects.isInstanceOf(String.class));
            validTest(123, 123, objects.isInstanceOf(Integer.class));
        }

        @Test
        void isInstanceOf_whenValidTypeWithActualSuperType_returnsValid() {
            MappingRule<Number,BigDecimal> rule = objects.isInstanceOf(BigDecimal.class);
            validTest(BigDecimal.ZERO,  BigDecimal.ZERO, rule);
        }

        @Test
        void isInstanceOf_whenInvalidType_returnsInvalid() {
            invalidTest(BigDecimal.ZERO, objects.isInstanceOf(String.class), "must.be.instance.of", HashMap.of("type", "String"));
        }

        @Test
        void isInstanceOf_whenNull_returnsNotNullError() {
            assertThatValidation(objects.isInstanceOf(String.class).apply(null))
                    .isInvalid()
                    .hasErrorMessages("must.not.be.null");
        }
    }

    @Nested
    class Construct {

        MappingRule<String, BigDecimal> rule = objects.construct(BigDecimal::new, ErrorMessage.of("invalid.number"));

        @Test
        void construct_withErrorMessage_whenValid_returnsValid() {
            assertThatValidation(rule.apply("123.45"))
                    .isValid()
                    .isEqualTo(new BigDecimal("123.45"));
        }

        @Test
        void construct_withErrorMessage_whenInvalid_returnsInvalid() {
            assertThatValidation(rule.apply("not-a-number"))
                    .isInvalid()
                    .hasErrorMessage("invalid.number");
        }

        @Test
        void construct_withErrorKey_whenValid_returnsValid() {
            assertThatValidation(rule.apply("123.45"))
                    .isValid()
                    .isEqualTo(new BigDecimal("123.45"));
        }

        @Test
        void construct_withErrorKey_whenInvalid_returnsInvalid() {
            assertThatValidation(rule.apply("not-a-number"))
                    .isInvalid()
                    .hasErrorMessage("invalid.number");
        }

        @Test
        void construct_withoutErrorMessage_whenValidationExceptionThrown_usesThoseErrors() {
            MappingRule<String, String> throwingRule = objects.construct(input -> {
                throw new ValidationException(List.of(ErrorMessage.of("custom.error")));
            });

            assertThatValidation(throwingRule.apply("any"))
                    .isInvalid()
                    .hasErrorMessage("custom.error");
        }

        @Test
        void construct_withoutErrorMessage_whenOtherExceptionThrown_usesDefaultError() {
            MappingRule<String, String> throwingRule = objects.construct(input -> {
                throw new RuntimeException("boom");
            });

            assertThatValidation(throwingRule.apply("any"))
                    .isInvalid()
                    .hasErrorMessage("could.not.construct");
        }

        @Test
        void construct_withoutErrorMessage_whenValid_returnsValid() {
            MappingRule<String, String> successRule = objects.construct(input -> "prefixed-" + input);

            assertThatValidation(successRule.apply("value"))
                    .isValid()
                    .isEqualTo("prefixed-value");
        }
    }
}