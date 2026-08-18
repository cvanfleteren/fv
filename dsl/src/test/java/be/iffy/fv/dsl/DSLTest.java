package be.iffy.fv.dsl;

import be.iffy.fv.*;
import io.vavr.Tuple;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static be.iffy.fv.dsl.DSL.*;
import static be.iffy.fv.rules.text.CharCategory.ASCII_DIGITS;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DSLTest {

    record Person(String name, int age) {
    }

    @Nested
    class ValidateThatList {

        @Nested
        class VavrList {
            @Test
            void validate_whenAllValid_returnsValid() {
                List<Integer> values = List.of(1, 2, 3);
                var result = validateThatList(values, "values")
                        .is(vavrLists.notEmpty())
                        .eachIs(ints.positive())
                        .validate();

                assertThatValidation(result).isValid().isEqualTo(values);
            }

            @Test
            void validate_whenListLevelRuleFails_returnsInvalid() {
                List<Integer> values = List.empty();
                var result = validateThatList(values, "values")
                        .is(vavrLists.notEmpty())
                        .validate();

                assertThatValidation(result)
                        .isInvalid()
                        .hasErrorMessage("values.must.not.be.empty");
            }

            @Test
            void validate_whenElementLevelRuleFails_returnsInvalidWithIndexedPath() {
                List<Integer> values = List.of(1, -1, 2);
                var result = validateThatList(values, "values")
                        .eachIs(ints.positive())
                        .validate();

                assertThatValidation(result)
                        .isInvalid()
                        .hasErrorMessage("values[1].must.be.positive");
            }

            @Test
            void validate_whenMappingElements_returnsMappedList() {
                List<String> values = List.of("1", "2");
                var result = validateThatList(values, "values")
                        .eachIs(strings.asInteger())
                        .validate();

                assertThatValidation(result).isValid().isEqualTo(List.of(1, 2));
            }

            @Test
            void validate_whenElementMappingFails_returnsInvalidWithIndexedPath() {
                List<String> values = List.of("1", "abc");
                var result = validateThatList(values, "values")
                        .eachIs(strings.asInteger())
                        .validate();

                assertThatValidation(result)
                        .isInvalid()
                        .hasErrorMessage("values[1].must.be.integer");
            }

            @Test
            void validate_whenUsingEachWithFunction_returnsMappedList() {
                List<String> values = List.of("1", "2");
                var result = validateThatList(values, "values")
                        .eachIs(s -> Validation.valid(Integer.parseInt(s)))
                        .validate();

                assertThatValidation(result).isValid().isEqualTo(List.of(1, 2));
            }

            @Test
            void validate_whenMultipleRulesFail_accumulatesAllErrors() {
                List<Integer> values = List.of(-1);
                var result = validateThatList(values, "values")
                        .is(vavrLists.minSize(2))
                        .eachIs(ints.positive())
                        .validate();

                assertThatValidation(result)
                        .isInvalid()
                        .hasErrorMessages("values.must.have.min.size", "values[0].must.be.positive");
            }
        }

        @Nested
        class JavaList {
            @Test
            void validate_whenAllValid_returnsValid() {
                java.util.List<Integer> values = Arrays.asList(1, 2, 3);
                var result = validateThatList(values, "values")
                        .is(lists.notEmpty())
                        .eachIs(ints.positive())
                        .validate();

                assertThatValidation(result).isValid().isEqualTo(values);
            }

            @Test
            void validate_whenListLevelRuleFails_returnsInvalid() {
                java.util.List<Integer> values = java.util.Collections.emptyList();
                var result = validateThatList(values, "values")
                        .is(lists.notEmpty())
                        .validate();

                assertThatValidation(result)
                        .isInvalid()
                        .hasErrorMessage("values.must.not.be.empty");
            }

            @Test
            void validate_whenElementLevelRuleFails_returnsInvalidWithIndexedPath() {
                java.util.List<Integer> values = Arrays.asList(1, -1, 2);
                var result = validateThatList(values, "values")
                        .eachIs(ints.positive())
                        .validate();

                assertThatValidation(result)
                        .isInvalid()
                        .hasErrorMessage("values[1].must.be.positive");
            }

            @Test
            void validate_whenMappingElements_returnsMappedList() {
                java.util.List<String> values = Arrays.asList("1", "2");
                Validation<java.util.List<Integer>> result = validateThatList(values, "values")
                        .eachIs(strings.asInteger())
                        .validate();

                assertThatValidation(result).isValid().isEqualTo(Arrays.asList(1, 2));
            }

            @Test
            void validate_whenElementMappingFails_returnsInvalidWithIndexedPath() {
                java.util.List<String> values = Arrays.asList("1", "abc");
                var result = validateThatList(values, "values")
                        .eachIs(strings.asInteger())
                        .validate();

                assertThatValidation(result)
                        .isInvalid()
                        .hasErrorMessage("values[1].must.be.integer");
            }
        }
    }

    @Nested
    class ValidateThat {

        @Test
        void liftToMap_whenAllValuesAreValid_returnsValidResult() {
            Map<Integer, String> input = HashMap.of(1, " hello ", 2, "world");
            MappingRule<String, String> mr = after(stringOps.trim()).is(strings.maxLength(5));

            Validation<Map<Integer, String>> result = validateThat(input, "value").is(mr.lift().toVavrMap());

            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(HashMap.of(1, "hello", 2, "world"));
        }

        @Test
        void map_whenMultipleMapsAreChained_appliesAllMappers() {
            var result = validateThat("  123  ")
                    .after(String::trim)
                    .is(strings.asInteger().then(Rule.of(i -> i == 123, "must.be.123")));

            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(123);
        }

        @Test
        void map_whenMapFails_becomesInvalidWithErrorMessage() {
            Validation<Integer> result = validateThat("  abc  ", "foo")
                    .map(strings.asInteger())
                    .is(Rule.of(i -> i == 246, "must.be.246"));

            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("foo.must.be.integer");
        }

        @Test
        void map_whenMappingToDifferentType_worksCorrectly() {
            var result = validateThat(" 123a ")
                    .after(stringOps.keep(ASCII_DIGITS))
                    .map(strings.asInteger())
                    .is(Rule.of(i -> i > 100, "must.be.greater.than.100"));

            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(123);
        }
    }

    @Nested
    class AssertThat {

        @Test
        void assertThat_withPropertySelector_whenValid_returnsValue() {
            String result = DSL.assertThat("john", Person::name).is(Rule.notNull());

            assertThat(result).isEqualTo("john");
        }

        @Test
        void assertThat_withPropertySelector_whenInvalid_throwsValidationException() {
            String name = null;
            assertThatThrownBy(() -> DSL.assertThat(name, Person::name).is(Rule.notNull()))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException ve = (ValidationException) ex;
                        assertThat(ve.errors().head().message()).isEqualTo("name.must.not.be.null");
                    });
        }
    }

    @Nested
    class NotNull {

        @Test
        void notNull_withStringName_whenValueIsNotNull_returnsValid() {
            Validation<String> result = DSL.notNull("hello", "name");

            assertThatValidation(result).isValid().isEqualTo("hello");
        }

        @Test
        void notNull_withStringName_whenValueIsNull_returnsInvalid() {
            String value = null;
            Validation<String> result = DSL.notNull(value, "name");

            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("name.must.not.be.null");
        }

        @Test
        void notNull_withPropertySelector_whenValueIsNotNull_returnsValid() {
            Validation<String> result = DSL.notNull("john", Person::name);

            assertThatValidation(result).isValid().isEqualTo("john");
        }

        @Test
        void notNull_withPropertySelector_whenValueIsNull_returnsInvalid() {
            String value = null;
            Validation<String> result = DSL.notNull(value, Person::name);

            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("name.must.not.be.null");
        }
    }

    @Nested
    class Catching {

        @Test
        void catching_whenSupplierReturnsValue_returnsValidWithThatValue() {
            Validation<String> result = catching(() -> "expected");

            assertThatValidation(result)
                    .isValid()
                    .isEqualTo("expected");
        }

        @Test
        void catching_whenSupplierThrowsValidationException_returnsInvalidWithSameErrors() {
            ErrorMessage e1 = ErrorMessage.of("name.too.short");
            ErrorMessage e2 = ErrorMessage.of("age.too.young");

            Validation<Object> result = catching(() -> {
                throw new ValidationException(List.of(e1, e2));
            });

            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("name.too.short", "age.too.young");
        }

        @Test
        void catching_whenSupplierThrowsOtherException_rethrows() {
            RuntimeException boom = new RuntimeException("boom");

            assertThatThrownBy(() -> catching(() -> {
                throw boom;
            })).isSameAs(boom);
        }
    }

    @Nested
    class Asserting {

        @Test
        void asserting_whenAllValid_doesNotThrow() {
            Validation<String> v1 = Validation.valid("ok");
            Validation<Integer> v2 = Validation.valid(123);

            assertThatCode(() -> DSL.asserting(new Validation<?>[]{v1, v2}))
                    .doesNotThrowAnyException();
        }

        @Test
        void asserting_whenSomeInvalid_throwsValidationExceptionWithAllErrors() {
            Validation<String> v1 = Validation.invalid("error1");
            Validation<Integer> v2 = Validation.valid(123);
            Validation<String> v3 = Validation.invalid("error2");

            assertThatThrownBy(() -> DSL.asserting(new Validation<?>[]{v1, v2, v3}))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException ve = (ValidationException) ex;
                        assertThat(ve.errors())
                                .containsExactly(ErrorMessage.of("error1"), ErrorMessage.of("error2"));
                    });
        }

        @Test
        void asserting_whenEmpty_doesNotThrow() {
            assertThatCode(() -> DSL.asserting())
                    .doesNotThrowAnyException();
        }

        @Test
        void asserting_whenNullArray_throwsNullPointerException() {
            assertThatThrownBy(() -> DSL.asserting((Validation<?>[]) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("validations is required");
        }

        @Test
        void asserting_whenArrayContainsNull_throwsNullPointerException() {
            Validation<String> v1 = Validation.valid("ok");

            assertThatThrownBy(() -> DSL.asserting(new Validation<?>[]{v1, null}))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("each validation is required");
        }

        @Test
        void asserting_whenAllValidationsAreValid_doesNotThrow() {
            Validation<String> v1 = Validation.valid("ok");
            Validation<Integer> v2 = Validation.valid(123);

            assertThatCode(() -> DSL.asserting(v1, v2))
                    .doesNotThrowAnyException();
        }

        @Test
        void asserting_whenTwoValidationsAreValid_returnsTuple2() {
            Validation<String> v1 = Validation.valid("ok");
            Validation<Integer> v2 = Validation.valid(123);

            var result = DSL.asserting(v1, v2);

            assertThat(result).isEqualTo(Tuple.of("ok", 123));
        }

        @Test
        void asserting_whenThreeValidationsAreValid_returnsTuple3() {
            Validation<String> v1 = Validation.valid("ok");
            Validation<Integer> v2 = Validation.valid(123);
            Validation<Double> v3 = Validation.valid(1.0);

            var result = DSL.asserting(v1, v2, v3);

            assertThat(result).isEqualTo(Tuple.of("ok", 123, 1.0));
        }

        @Test
        void asserting_whenFourValidationsAreValid_returnsTuple4() {
            Validation<String> v1 = Validation.valid("v1");
            Validation<String> v2 = Validation.valid("v2");
            Validation<String> v3 = Validation.valid("v3");
            Validation<String> v4 = Validation.valid("v4");

            var result = DSL.asserting(v1, v2, v3, v4);

            assertThat(result).isEqualTo(Tuple.of("v1", "v2", "v3", "v4"));
        }

        @Test
        void asserting_whenFiveValidationsAreValid_returnsTuple5() {
            Validation<String> v1 = Validation.valid("v1");
            Validation<String> v2 = Validation.valid("v2");
            Validation<String> v3 = Validation.valid("v3");
            Validation<String> v4 = Validation.valid("v4");
            Validation<String> v5 = Validation.valid("v5");

            var result = DSL.asserting(v1, v2, v3, v4, v5);

            assertThat(result).isEqualTo(Tuple.of("v1", "v2", "v3", "v4", "v5"));
        }

        @Test
        void asserting_whenSixValidationsAreValid_returnsTuple6() {
            Validation<String> v1 = Validation.valid("v1");
            Validation<String> v2 = Validation.valid("v2");
            Validation<String> v3 = Validation.valid("v3");
            Validation<String> v4 = Validation.valid("v4");
            Validation<String> v5 = Validation.valid("v5");
            Validation<String> v6 = Validation.valid("v6");

            var result = DSL.asserting(v1, v2, v3, v4, v5, v6);

            assertThat(result).isEqualTo(Tuple.of("v1", "v2", "v3", "v4", "v5", "v6"));
        }

        @Test
        void asserting_whenSevenValidationsAreValid_returnsTuple7() {
            Validation<String> v1 = Validation.valid("v1");
            Validation<String> v2 = Validation.valid("v2");
            Validation<String> v3 = Validation.valid("v3");
            Validation<String> v4 = Validation.valid("v4");
            Validation<String> v5 = Validation.valid("v5");
            Validation<String> v6 = Validation.valid("v6");
            Validation<String> v7 = Validation.valid("v7");

            var result = DSL.asserting(v1, v2, v3, v4, v5, v6, v7);

            assertThat(result).isEqualTo(Tuple.of("v1", "v2", "v3", "v4", "v5", "v6", "v7"));
        }

        @Test
        void asserting_whenEightValidationsAreValid_returnsTuple8() {
            Validation<String> v1 = Validation.valid("v1");
            Validation<String> v2 = Validation.valid("v2");
            Validation<String> v3 = Validation.valid("v3");
            Validation<String> v4 = Validation.valid("v4");
            Validation<String> v5 = Validation.valid("v5");
            Validation<String> v6 = Validation.valid("v6");
            Validation<String> v7 = Validation.valid("v7");
            Validation<String> v8 = Validation.valid("v8");

            var result = DSL.asserting(v1, v2, v3, v4, v5, v6, v7, v8);

            assertThat(result).isEqualTo(Tuple.of("v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8"));
        }

        @Test
        void asserting_whenSomeValidationsInvalid_throwsValidationExceptionWithAllErrors() {
            Validation<String> v1 = Validation.invalid("error1");
            Validation<Integer> v2 = Validation.valid(123);
            Validation<String> v3 = Validation.invalid("error2");

            assertThatThrownBy(() -> DSL.asserting(v1, v2, v3))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException ve = (ValidationException) ex;
                        assertThat(ve.errors())
                                .isEqualTo(List.of(ErrorMessage.of("error1"), ErrorMessage.of("error2")));
                    });
        }
    }

    @Nested
    class MultiField {

        record AtLeastOne(String first, String second) {

            AtLeastOne {
                asserting(
                    anyOf(
                        validateThat(first,"first").is(strings.notBlank()),
                        validateThat(second,"second").is(strings.notBlank())
                    ).orError("at.least.one.must.not.be.blank")
                );
            }

        }

        @Test
        void asserting_atLeastOne() {
            assertThatValidation(Validation.valid(new AtLeastOne("first", "second"))).isValid();
            assertThatValidation(Validation.valid(new AtLeastOne("", "second"))).isValid();
            assertThatValidation(Validation.valid(new AtLeastOne("first", ""))).isValid();

            assertThatValidation(
                Validation.from().catching(() -> new AtLeastOne("", ""))
            ).isInvalid()
                .hasFormattedMessage("at.least.one.must.not.be.blank");
        }

    }

    @Nested
    class AnyOf {

        @Test
        void anyOf_whenFirstIsValid_returnsFirst() {
            var result = anyOf(
                    validateThat("foo", "first").is(strings.notBlank()),
                    validateThat("", "second").is(strings.notBlank())
            );

            assertThatValidation(result)
                    .isValid()
                    .isEqualTo("foo");
        }

        @Test
        void anyOf_whenSecondIsValid_returnsSecond() {
            var result = anyOf(
                    validateThat("", "first").is(strings.notBlank()),
                    validateThat("bar", "second").is(strings.notBlank())
            );

            assertThatValidation(result)
                    .isValid()
                    .isEqualTo("bar");
        }

        @Test
        void anyOf_whenAllAreInvalid_accumulatesAllErrorsInOrder() {
            var result = anyOf(
                    validateThat("", "first").is(strings.notBlank()),
                    validateThat("", "second").is(strings.notBlank())
            );

            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("first.must.not.be.blank", "second.must.not.be.blank");
        }

        @Test
        void anyOf_whenAllAreInvalidWithOrError_replacesErrors() {
            var result = anyOf(
                    validateThat("", "first").is(strings.notBlank()),
                    validateThat("", "second").is(strings.notBlank())
            ).orError("contact.must.have.at.least.one");

            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessage("contact.must.have.at.least.one");
        }

        @Test
        void anyOf_withHeterogeneousTypes_compilesAndEvaluatesCorrectly() {
            Validation<String> stringV = validateThat("", "str").is(strings.notBlank());
            Validation<Integer> intV = validateThat(42, "num").is(ints.positive());

            Validation<Object> result = anyOf(stringV, intV);

            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(42);
        }

        @Test
        void anyOf_interoperabilityWithValidating() {
            var emailOrPhone = anyOf(
                    validateThat("", "email").is(strings.notBlank()),
                    validateThat("123456", "phone").is(strings.notBlank())
            );
            var name = validateThat("Alice", "name").is(strings.notBlank());

            var person = validating(name, emailOrPhone).map(Tuple::of);

            assertThatValidation(person)
                    .isValid()
                    .isEqualTo(Tuple.of("Alice", "123456"));
        }

        @Test
        void anyOf_withSeq_evaluatesCorrectly() {
            List<Validation<String>> validations = List.of(
                    validateThat("", "first").is(strings.notBlank()),
                    validateThat("secondVal", "second").is(strings.notBlank())
            );

            var result = anyOf(validations);

            assertThatValidation(result)
                    .isValid()
                    .isEqualTo("secondVal");
        }

        @Test
        void anyOf_withIterable_evaluatesCorrectly() {
            java.util.List<Validation<String>> validations = java.util.List.of(
                    validateThat("", "first").is(strings.notBlank()),
                    validateThat("secondVal", "second").is(strings.notBlank())
            );

            var result = anyOf(validations);

            assertThatValidation(result)
                    .isValid()
                    .isEqualTo("secondVal");
        }
    }
}
