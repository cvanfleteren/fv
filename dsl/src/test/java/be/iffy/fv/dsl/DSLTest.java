package be.iffy.fv.dsl;

import be.iffy.fv.*;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Function;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static be.iffy.fv.dsl.DSL.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DSLTest {

    record Person(String name, int age) {
    }

    Rule<Number> positive = Rule.of(n -> n.doubleValue() > 0, "must.be.positive");
    Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "must.not.be.empty");

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

        @Nested
        class InputIsNull {

            @Test
            void isNotNull_whenValueIsNotNull_returnsValid() {
                Validation<String> v = validateThat((String) null, "field").map(stringOps.trim()).is(strings.minLength(4));
                assertThatValidation(v).isInvalid().hasErrorMessage("field.must.not.be.null");
            }
        }

        @Nested
        class IsNotNull {
            @Test
            void isNotNull_whenValueIsNotNull_returnsValid() {
                Validation<String> v = validateThat("test").isNotNull();
                assertThatValidation(v).isValid().isEqualTo("test");
            }

            @Test
            void isNotNull_whenValueIsNull_returnsInvalid() {
                Validation<String> v = validateThat((String) null, "foo").isNotNull();
                assertThatValidation(v).isInvalid().hasErrorMessage("foo.must.not.be.null");
            }
        }

        @Test
        void liftToMap_whenAllValuesAreValid_returnsValidResult() {
            // Arrange
            Map<Integer, String> input = HashMap.of(1, " hello ", 2, "world");


            MappingRule<String, String> mr = after(stringOps.trim()).is(strings.maxLength(5));

            Validation<String> foo = mr.test("12345 ");

            MappingRule<Map<Integer, String>, Map<Integer, String>> mappingRule = mr.liftToVavrMap();

            Validation<Map<Integer, String>> foo2 = mappingRule.test(HashMap.of(1, "12345 "));

            Validation<Map<Integer, String>> result = validateThat(input, "value").is(mappingRule);

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(HashMap.of(1, "hello", 2, "world"));
        }

        @Nested
        class Is {

            @Test
            void is_withFunction_whenValid_returnsValid() {
                // Arrange
                Function<String, Validation<Integer>> func = s -> Validation.valid(Integer.parseInt(s));

                // Act
                Validation<Integer> result = validateThat("123").is(func);

                // Assert
                assertThatValidation(result)
                        .isValid()
                        .isEqualTo(123);
            }

            @Test
            void is_withFunction_whenInvalid_returnsInvalid() {
                // Arrange
                ErrorMessage error = ErrorMessage.of("invalid.input");
                Function<String, Validation<Integer>> func = s -> Validation.invalid(error);

                // Act
                Validation<Integer> result = validateThat("abc").is(func);

                // Assert
                assertThatValidation(result)
                        .isInvalid()
                        .hasErrorKeys("invalid.input");
            }

            @Test
            void is_withFunction_whenValueIsNull_returnsInvalidWithNullError() {
                // Arrange
                Function<String, Validation<Integer>> func = s -> Validation.valid(1);

                // Act
                Validation<Integer> result = validateThat((String) null, "foo").is(func);

                // Assert
                assertThatValidation(result)
                        .isInvalid()
                        .hasErrorMessage("foo.must.not.be.null");
            }
        }

        @Test
        public void test() {
            Rule<String> startsWithH = Rule.of(s -> s.startsWith("h"), "must.start.with.h");
            Rule<String> compliant = notEmpty.and(startsWithH);


            Person p = new Person("hugh", 30);

            Validation<String> v = validateThat(p.name()).is(compliant);

            assertThatValidation(v).isValid();
        }

        @Test
        public void map_whenUsed_passedMappedValueToValidation() {
            Rule<String> startsWithH = Rule.of(s -> s.startsWith("h"), "must.start.with.h");


            Rule<String> compliant = notEmpty.and(startsWithH);


            Person p = new Person("  hugh", 30);

            Validation<String> v = validateThat(p.name()).map(String::trim).is(compliant);

            assertThatValidation(v).isValid().isEqualTo("hugh");
        }

        @Test
        public void map_whenNullIsGiven_becomesInvalid() {
            Rule<String> startsWithH = Rule.of(s -> s.startsWith("h"), "must.start.with.h");


            Rule<String> compliant = notEmpty.and(startsWithH);


            Person p = new Person(null, 30);

            Validation<String> v = validateThat(p.name()).map(String::trim).is(compliant);

            assertThatValidation(v).isInvalid().hasErrorKeys("must.not.be.null");
        }

        @Test
        public void map_whenMultipleMapsAreChained_appliesAllMappers() {
            // Arrange
            var value = "  123  ";

            // Act
            var result = validateThat(value)
                    .map(String::trim)
                    .is(strings.asInteger().then(Rule.of(i -> i == 123, "must.be.123")));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(123);
        }

        @Test
        public void map_whenMapFails_becomesInvalidWithErrorMessage() {
            // Arrange
            var value = "  abc  ";

            // Act
            Validation<Integer> result = validateThat(value, "foo")
                    .map(strings.asInteger())
                    .is(Rule.of(i -> i == 246, "must.be.246"));

            // Assert
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("foo.must.be.integer");
        }

        @Test
        public void map_whenMappingToDifferentType_worksCorrectly() {
            // Arrange
            var value = " 123a ";

            // Act
            var result = validateThat(value)
                    .map(stringOps.digits())
                    .map(strings.asInteger())
                    .is(Rule.of(i -> i > 100, "must.be.greater.than.100"));

            // Assert
            assertThatValidation(result)
                    .isValid()
                    .isEqualTo(123);
        }

        @Test
        public void validationDsl_invalid() {
            Person p = new Person("john", 0);

            Validation<Integer> v = validateThat(p.age(), "age").is(positive);

            assertThatValidation(v).isInvalid().hasErrorMessage("age.must.be.positive");
        }
    }

    @Nested
    class AssertThat {

        @Test
        void assertThat_whenValid_returnsValue() {
            // Act
            DSL.validateThat("ok","field").is(Rule.notNull()).getOrElseThrow();
            String result = DSL.assertThat("ok", "field").is(Rule.notNull());

            // Assert
            assertThat(result).isEqualTo("ok");
        }

        @Test
        void assertThat_map_whenValid_returnsMappedValue() {
            // Act
            String result = DSL.assertThat(" ok ", "field").map(String::trim).is(Rule.notNull());

            // Assert
            assertThat(result).isEqualTo("ok");
        }

        @Test
        void assertThat_whenInvalid_throwsValidationException() {
            // Act & Assert
            assertThatThrownBy(() -> DSL.assertThat((String) null, "field").is(Rule.notNull()))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException ve = (ValidationException) ex;
                        assertThat(ve.errors().head().message()).isEqualTo("field.must.not.be.null");
                    });
        }

        @Test
        void assertThat_withPropertySelector_whenValid_returnsValue() {
            // Act
            String result = DSL.assertThat("john", Person::name).is(Rule.notNull());

            // Assert
            assertThat(result).isEqualTo("john");
        }

        @Test
        void assertThat_withPropertySelector_whenInvalid_throwsValidationException() {
            // Arrange
            String name = null;
            // Act & Assert
            assertThatThrownBy(() -> DSL.assertThat(name, Person::name).is(Rule.notNull()))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException ve = (ValidationException) ex;
                        assertThat(ve.errors().head().message()).isEqualTo("name.must.not.be.null");
                    });
        }
    }

    @Nested
    class AssertAllValid {

        @Test
        void assertAllValid_whenAllValidationsAreValid_doesNotThrow() {
            // Arrange
            Validation<String> v1 = Validation.valid("ok");
            Validation<Integer> v2 = Validation.valid(123);

            // Act & Assert
            assertThatCode(() -> DSL.assertAllValid(v1, v2))
                    .doesNotThrowAnyException();
        }

        @Test
        void assertAllValid_whenTwoValidationsAreValid_returnsTuple2() {
            // Arrange
            Validation<String> v1 = Validation.valid("ok");
            Validation<Integer> v2 = Validation.valid(123);

            // Act
            var result = DSL.assertAllValid(v1, v2);

            // Assert
            assertThat(result).isEqualTo(io.vavr.Tuple.of("ok", 123));
        }

        @Test
        void assertAllValid_whenThreeValidationsAreValid_returnsTuple3() {
            // Arrange
            Validation<String> v1 = Validation.valid("ok");
            Validation<Integer> v2 = Validation.valid(123);
            Validation<Double> v3 = Validation.valid(1.0);

            // Act
            var result = DSL.assertAllValid(v1, v2, v3);

            // Assert
            assertThat(result).isEqualTo(io.vavr.Tuple.of("ok", 123, 1.0));
        }

        @Test
        void assertAllValid_whenFourValidationsAreValid_returnsTuple4() {
            // Arrange
            Validation<String> v1 = Validation.valid("v1");
            Validation<String> v2 = Validation.valid("v2");
            Validation<String> v3 = Validation.valid("v3");
            Validation<String> v4 = Validation.valid("v4");

            // Act
            var result = DSL.assertAllValid(v1, v2, v3, v4);

            // Assert
            assertThat(result).isEqualTo(io.vavr.Tuple.of("v1", "v2", "v3", "v4"));
        }

        @Test
        void assertAllValid_whenFiveValidationsAreValid_returnsTuple5() {
            // Arrange
            Validation<String> v1 = Validation.valid("v1");
            Validation<String> v2 = Validation.valid("v2");
            Validation<String> v3 = Validation.valid("v3");
            Validation<String> v4 = Validation.valid("v4");
            Validation<String> v5 = Validation.valid("v5");

            // Act
            var result = DSL.assertAllValid(v1, v2, v3, v4, v5);

            // Assert
            assertThat(result).isEqualTo(io.vavr.Tuple.of("v1", "v2", "v3", "v4", "v5"));
        }

        @Test
        void assertAllValid_whenSixValidationsAreValid_returnsTuple6() {
            // Arrange
            Validation<String> v1 = Validation.valid("v1");
            Validation<String> v2 = Validation.valid("v2");
            Validation<String> v3 = Validation.valid("v3");
            Validation<String> v4 = Validation.valid("v4");
            Validation<String> v5 = Validation.valid("v5");
            Validation<String> v6 = Validation.valid("v6");

            // Act
            var result = DSL.assertAllValid(v1, v2, v3, v4, v5, v6);

            // Assert
            assertThat(result).isEqualTo(io.vavr.Tuple.of("v1", "v2", "v3", "v4", "v5", "v6"));
        }

        @Test
        void assertAllValid_whenSevenValidationsAreValid_returnsTuple7() {
            // Arrange
            Validation<String> v1 = Validation.valid("v1");
            Validation<String> v2 = Validation.valid("v2");
            Validation<String> v3 = Validation.valid("v3");
            Validation<String> v4 = Validation.valid("v4");
            Validation<String> v5 = Validation.valid("v5");
            Validation<String> v6 = Validation.valid("v6");
            Validation<String> v7 = Validation.valid("v7");

            // Act
            var result = DSL.assertAllValid(v1, v2, v3, v4, v5, v6, v7);

            // Assert
            assertThat(result).isEqualTo(io.vavr.Tuple.of("v1", "v2", "v3", "v4", "v5", "v6", "v7"));
        }

        @Test
        void assertAllValid_whenEightValidationsAreValid_returnsTuple8() {
            // Arrange
            Validation<String> v1 = Validation.valid("v1");
            Validation<String> v2 = Validation.valid("v2");
            Validation<String> v3 = Validation.valid("v3");
            Validation<String> v4 = Validation.valid("v4");
            Validation<String> v5 = Validation.valid("v5");
            Validation<String> v6 = Validation.valid("v6");
            Validation<String> v7 = Validation.valid("v7");
            Validation<String> v8 = Validation.valid("v8");

            // Act
            var result = DSL.assertAllValid(v1, v2, v3, v4, v5, v6, v7, v8);

            // Assert
            assertThat(result).isEqualTo(io.vavr.Tuple.of("v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8"));
        }

        @Test
        void assertAllValid_whenSomeValidationsInvalid_throwsValidationExceptionWithAllErrors() {
            // Arrange
            Validation<String> v1 = Validation.invalid("error1");
            Validation<Integer> v2 = Validation.valid(123);
            Validation<String> v3 = Validation.invalid("error2");

            // Act & Assert
            assertThatThrownBy(() -> DSL.assertAllValid(v1, v2, v3))
                    .isInstanceOf(ValidationException.class)
                    .satisfies(ex -> {
                        ValidationException ve = (ValidationException) ex;
                        assertThat(ve.errors())
                                .isEqualTo(List.of(ErrorMessage.of("error1"), ErrorMessage.of("error2")));
                    });
        }

        @Test
        void assertAllValid_whenNoValidationsProvided_doesNotThrow() {
            // Act & Assert
            assertThatCode(DSL::assertAllValid).doesNotThrowAnyException();
        }
    }
}
