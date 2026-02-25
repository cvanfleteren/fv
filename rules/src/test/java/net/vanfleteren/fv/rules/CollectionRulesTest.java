package net.vanfleteren.fv.rules;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import net.vanfleteren.fv.ErrorMessage;
import net.vanfleteren.fv.Rule;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static net.vanfleteren.fv.API.validateThat;
import static net.vanfleteren.fv.assertj.ValidationAssert.assertThatValidation;
import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;

import static net.vanfleteren.fv.rules.CollectionRules.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollectionRulesTest {

    @Nested
    class NotEmpty {

        @Test
        void valid() {
            validTest(List.of("x"), notEmpty);
            validTest(HashSet.of(1), notEmpty);
        }

        @Test
        void invalid() {
            invalidTest(new ArrayList<>(), notEmpty, "cannot.be.empty");
            invalidTest(HashSet.of(), notEmpty, "cannot.be.empty");
        }
    }

    @Nested
    class MinSize {

        @Test
        void valid() {
            validTest(List.of(), minSize(0));
            validTest(List.of("x"), minSize(1));
            validTest(List.of("a", "b"), minSize(1));
        }

        @Test
        void invalid() {
            invalidTest(List.of(), minSize(1), "min.size", io.vavr.collection.HashMap.of("min", 1));
            invalidTest(List.of("x"), minSize(2), "min.size", io.vavr.collection.HashMap.of("min", 2));
        }
    }

    @Nested
    class MaxSize {

        @Test
        void valid() {
            validTest(List.of(), maxSize(0));
            validTest(List.of("x"), maxSize(1));
            validTest(List.of("a", "b"), maxSize(2));
        }

        @Test
        void invalid() {
            invalidTest(List.of("x"), maxSize(0), "max.size", io.vavr.collection.HashMap.of("max", 0));
            invalidTest(List.of("a", "b", "c"), maxSize(2), "max.size", io.vavr.collection.HashMap.of("max", 2));
        }
    }

    @Nested
    class SizeEquals {

        @Test
        void valid() {
            validTest(List.of(), sizeEquals(0));
            validTest(List.of("x"), sizeEquals(1));
            validTest(List.of("a", "b"), sizeEquals(2));
        }

        @Test
        void invalid() {
            invalidTest(List.of(), sizeEquals(1), "equal.size", io.vavr.collection.HashMap.of("equal", 1));
            invalidTest(List.of("x"), sizeEquals(0), "equal.size", io.vavr.collection.HashMap.of("equal", 0));
        }
    }

    @Nested
    class SizeBetween {

        @Test
        void valid() {
            validTest(List.of(), sizeBetween(0, 0));
            validTest(List.of("x"), sizeBetween(0, 1));
            validTest(List.of("a", "b"), sizeBetween(1, 2));
        }

        @Test
        void invalid() {
            invalidTest(
                    List.of(),
                    sizeBetween(1, 2),
                    "equal.size",
                    io.vavr.collection.HashMap.of("min", 1, "max", 2)
            );
            invalidTest(
                    List.of("a", "b", "c"),
                    sizeBetween(1, 2),
                    "equal.size",
                    io.vavr.collection.HashMap.of("min", 1, "max", 2)
            );
        }
    }

    @Nested
    class NoNullElements {

        @Test
        void valid() {
            Rule<List<String>> noNulls = noNullElements();
            validTest(List.of("a", "b", "c"), noNulls);
            validTest(List.empty(), noNullElements());
        }

        @Test
        void invalid() {
            assertThatValidation(validateThat(List.of("a", null, "c"), "value").is(noNullElements()))
                    .isInvalid()
                    .hasErrorMessages("value[1].cannot.be.null");
        }
    }

    @Nested
    class AllMatch {

        @Test
        void valid_whenAllElementsMatch() {
            Rule<List<Integer>> even = allMatch(n -> n % 2 == 0);
            validTest(List.of(2, 4, 6), even);
        }

        @Test
        void valid_whenEmptyCollection_vacuouslyTrue() {
            validTest(List.<Integer>of(), allMatch(n -> n % 2 == 0));
        }

        @Test
        void invalid_whenAnyElementDoesNotMatch_usesDefaultErrorKey() {
            invalidTest(List.of(2, 3, 4), allMatch(n -> n % 2 == 0), "all.should.match");
        }

        @Test
        void invalid_whenAnyElementDoesNotMatch_usesProvidedErrorMessage() {


            assertThatValidation(
                    validateThat(List.of("a", "bb", "c"), "value")
                            .is(allMatch((String s) -> s.length() == 1, ErrorMessage.of("len.must.be.one")))
                    )
                    .isInvalid()
                    .hasErrorMessages("value[1].len.must.be.one");
        }

        @Test
        void invalid_whenAnyElementDoesNotMatch_preservesProvidedErrorArgs() {
            invalidTest(
                    List.of("a", "bb"),
                    allMatch(s -> s.length() == 1, ErrorMessage.of("len.must.be", "len", 1)),
                    "len.must.be",
                    HashMap.of("len", 1)
            );
        }

        @Test
        void throws_whenPredicateIsNull_andRuleIsEvaluated() {
            assertThatThrownBy(() ->
                    validateThat(List.of(1), "value").is(allMatch(null)).getOrElseThrow()
            ).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class NoneMatch {

        @Test
        void valid_whenNoElementsMatchPredicate() {
            Rule<List<Integer>> noEvens = noneMatch(n -> n % 2 == 0);
            validTest(List.of(1, 3, 5), noEvens);
        }

        @Test
        void valid_whenEmptyCollection_vacuouslyTrue() {
            validTest(List.<Integer>of(), noneMatch(n -> n % 2 == 0));
        }

        @Test
        void invalid_whenAnyElementMatchesPredicate_usesDefaultErrorKey() {
            invalidTest(List.of(1, 2, 3), noneMatch(n -> n % 2 == 0), "none.should.match");
        }

        @Test
        void invalid_whenAnyElementMatchesPredicate_usesProvidedErrorMessage_andAddsIndexPath() {
            assertThatValidation(
                    validateThat(List.of("a", "bb", "c"), "value")
                            .is(noneMatch((String s) -> s.length() == 2, ErrorMessage.of("len.must.not.be.two")))
            )
                    .isInvalid()
                    .hasErrorMessages("value[1].len.must.not.be.two");
        }

        @Test
        void invalid_whenAnyElementMatchesPredicate_preservesProvidedErrorArgs() {
            invalidTest(
                    List.of("a", "bb"),
                    noneMatch(s -> s.length() == 2, ErrorMessage.of("len.must.not.be", "len", 2)),
                    "len.must.not.be",
                    HashMap.of("len", 2)
            );
        }

        @Test
        void throws_whenPredicateIsNull_andRuleIsEvaluated() {
            assertThatThrownBy(() ->
                    validateThat(List.of(1), "value").is(noneMatch(null)).getOrElseThrow()
            ).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class AnyMatch {

        @Test
        void valid_whenAtLeastOneElementMatchesPredicate() {
            Rule<List<Integer>> hasEven = anyMatch(n -> n % 2 == 0);
            validTest(List.of(1, 2, 3), hasEven);
        }

        @Test
        void invalid_whenNoElementsMatchPredicate_usesDefaultErrorKey() {
            invalidTest(List.of(1, 3, 5), anyMatch(n -> n % 2 == 0), "atleast.one.should.match");
        }

        @Test
        void invalid_whenEmptyCollection_usesDefaultErrorKey() {
            invalidTest(List.<Integer>of(), anyMatch(n -> n % 2 == 0), "atleast.one.should.match");
        }

        @Test
        void invalid_whenNoElementsMatchPredicate_usesProvidedErrorMessage() {
            invalidTest(
                    List.of("a", "bb", "ccc"),
                    anyMatch((String s) -> s.length() == 4, ErrorMessage.of("len.must.be.four")),
                    "len.must.be.four"
            );
        }

        @Test
        void invalid_whenNoElementsMatchPredicate_preservesProvidedErrorArgs() {
            invalidTest(
                    List.of("a", "bb"),
                    anyMatch((String s) -> s.length() == 3, ErrorMessage.of("len.must.be", "len", 3)),
                    "len.must.be",
                    HashMap.of("len", 3)
            );
        }

        @Test
        void throws_whenPredicateIsNull() {
            assertThatThrownBy(() -> anyMatch(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("predicate cannot be null");
        }
    }

    @Nested
    class Contains {

        @Test
        void valid_whenElementIsPresent() {
            validTest(List.of("a", "b", "c"), contains("b"));
        }

        @Test
        void invalid_whenElementIsNotPresent_includesElementArg() {
            invalidTest(
                    List.of("a", "b", "c"),
                    contains("x"),
                    "must.contain",
                    HashMap.of("element", "x")
            );
        }
    }

    @Nested
    class ContainsAll {

        @Test
        void valid_whenAllRequiredElementsArePresent() {
            validTest(List.of("a", "b", "c"), containsAll(List.of("a", "c")));
        }

        @Test
        void valid_whenRequiredIsEmpty() {
            validTest(List.of("a", "b"), containsAll(List.empty()));
        }

        @Test
        void valid_whenRequiredContainsDuplicates_duplicatesAreIgnored() {
            validTest(List.of("a", "b", "c"), containsAll(List.of("a", "a", "c")));
        }

        @Test
        void invalid_whenAnyRequiredElementIsMissing_includesRequiredSetArg() {
            invalidTest(
                    List.of("a", "b"),
                    containsAll(List.of("a", "c")),
                    "must.contain.all",
                    HashMap.of("required", HashSet.of("a", "c"))
            );
        }

        @Test
        void throws_whenRequiredIsNull() {
            assertThatThrownBy(() -> containsAll(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("required cannot be null");
        }
    }

    @Nested
    class ContainsAnyOf {

        @Test
        void valid_whenAtLeastOneCandidateIsPresent() {
            validTest(List.of("a", "b", "c"), containsAnyOf(List.of("x", "b")));
        }

        @Test
        void invalid_whenNoCandidatesArePresent_includesCandidatesSetArg() {
            invalidTest(
                    List.of("a", "b", "c"),
                    containsAnyOf(List.of("x", "y")),
                    "must.contain.any.of",
                    HashMap.of("candidates", HashSet.of("x", "y"))
            );
        }

        @Test
        void invalid_whenCandidatesIsEmpty() {
            invalidTest(
                    List.of("a", "b"),
                    containsAnyOf(List.empty()),
                    "must.contain.any.of",
                    HashMap.of("candidates", HashSet.empty())
            );
        }

        @Test
        void throws_whenCandidatesIsNull() {
            assertThatThrownBy(() -> containsAnyOf(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("candidates cannot be null");
        }
    }

    @Nested
    class UniqueBy {

        record Person(String email, String name) { }

        @Test
        void valid_whenAllKeysAreUnique() {
           List<Person> people = List.of(
                    new Person("a@example.com", "Alice"),
                    new Person("b@example.com", "Bob"),
                    new Person("c@example.com", "Carol")
            );

            validTest(people, uniqueBy(Person::email, "email"));
        }

        @Test
        void invalid_whenDuplicateKeysExist_accumulatesDuplicatesAndKeepsKeys() {
            List<Person> people = List.of(
                    new Person("a@example.com", "Alice"),   // idx 0
                    new Person("b@example.com", "Bob"),     // idx 1
                    new Person("a@example.com", "Alicia"),  // idx 2 (duplicate of idx 0)
                    new Person("b@example.com", "Bobby")    // idx 3 (duplicate of idx 1)
            );

            invalidTest(
                    people,
                    uniqueBy(Person::email, "email"),
                    "must.be.unique.by.key",
                    HashMap.of(
                            "key", "email",
                            "duplicates", HashMap.of(
                                    "a@example.com", List.of(0, 2),
                                    "b@example.com", List.of(1, 3)
                            )
                    )
            );
        }

        @Test
        void invalid_whenKeyAppearsMoreThanTwice_includesAllIndices() {
            List<Person> people = List.of(
                    new Person("dup@example.com", "A"), // 0
                    new Person("x@example.com", "X"),   // 1
                    new Person("dup@example.com", "B"), // 2
                    new Person("dup@example.com", "C")  // 3
            );

            invalidTest(
                    people,
                    uniqueBy(Person::email, "email"),
                    "must.be.unique.by.key",
                    HashMap.of(
                            "key", "email",
                            "duplicates", HashMap.of(
                                    "dup@example.com", List.of(0, 2, 3)
                            )
                    )
            );
        }
    }

    @Nested
    class ValidateValuesWith {

        @Test
        void validateValuesWith_whenSomeValuesFail_accumulatesErrorsAndAddsIndexToPath() {
            // Arrange
            Rule<Number> rule = Rule.of(n -> n.doubleValue() > 0, "must.be.positive");
            Rule<List<Integer>> listRule = CollectionRules.validateValuesWith(rule);

            List<Integer> input = List.of(-1, 10, 0);

            // Act
            var result = validateThat(input, "value").is(listRule);

            // Assert: failures are attributed to their indices in the path
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("value[0].must.be.positive", "value[2].must.be.positive");
        }
    }
}