package be.iffy.fv.rules.collections;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;
import static be.iffy.fv.rules.collections.ListRules.lists;
import static be.iffy.fv.rules.numbers.IntegerRules.ints;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ListRulesTest {

    @Nested
    class NotEmpty {

        @Test
        void valid() {
            validTest(List.of("x"), lists.notEmpty());
        }

        @Test
        void invalid() {
            invalidTest(new ArrayList<>(), lists.notEmpty(), "must.not.be.empty");
        }
    }

    @Nested
    class Empty {

        @Test
        void valid() {
            validTest(List.of(), lists.empty());
            validTest(new ArrayList<>(), lists.empty());
        }

        @Test
        void invalid() {
            invalidTest(List.of("x"), lists.empty(), "must.be.empty");
        }
    }

    @Nested
    class MinSize {

        @Test
        void valid() {
            validTest(List.of(), lists.minSize(0));
            validTest(List.of("x"), lists.minSize(1));
            validTest(List.of("a", "b"), lists.minSize(1));
        }

        @Test
        void invalid() {
            invalidTest(null, lists.minSize(1), "must.not.be.null");
            invalidTest(List.of(), lists.minSize(1), "must.have.min.size", HashMap.of("min", 1));
            invalidTest(List.of("x"), lists.minSize(2), "must.have.min.size", HashMap.of("min", 2));
        }
    }

    @Nested
    class MaxSize {

        @Test
        void valid() {
            validTest(List.of(), lists.maxSize(0));
            validTest(List.of("x"), lists.maxSize(1));
            validTest(List.of("a", "b"), lists.maxSize(2));
        }

        @Test
        void invalid() {
            invalidTest(null, lists.maxSize(0), "must.not.be.null");
            invalidTest(List.of("x"), lists.maxSize(0), "must.have.max.size", HashMap.of("max", 0));
            invalidTest(List.of("a", "b", "c"), lists.maxSize(2), "must.have.max.size", HashMap.of("max", 2));
        }
    }

    @Nested
    class SizeEquals {

        @Test
        void valid() {
            validTest(List.of(), lists.sizeEquals(0));
            validTest(List.of("x"), lists.sizeEquals(1));
            validTest(List.of("a", "b"), lists.sizeEquals(2));
        }

        @Test
        void invalid() {
            invalidTest(null, lists.sizeEquals(1), "must.not.be.null");
            invalidTest(List.of(), lists.sizeEquals(1), "must.have.exact.size", HashMap.of("equal", 1));
            invalidTest(List.of("x"), lists.sizeEquals(0), "must.have.exact.size", HashMap.of("equal", 0));
        }
    }

    @Nested
    class SizeBetween {

        @Test
        void valid() {
            validTest(List.of(), lists.sizeBetween(0, 0));
            validTest(List.of("x"), lists.sizeBetween(0, 1));
            validTest(List.of("a", "b"), lists.sizeBetween(1, 2));
        }

        @Test
        void invalid() {
            invalidTest(null, lists.sizeBetween(1, 2), "must.not.be.null");
            invalidTest(
                    List.of(),
                    lists.sizeBetween(1, 2),
                    "must.have.size.between",
                    HashMap.of("min", 1, "max", 2)
            );
            invalidTest(
                    List.of("a", "b", "c"),
                    lists.sizeBetween(1, 2),
                    "must.have.size.between",
                    HashMap.of("min", 1, "max", 2)
            );
        }
    }

    @Nested
    class NoNullElements {

        @Test
        void valid() {
            Rule<List<String>> noNulls = lists.noNullElements();
            validTest(List.of("a", "b", "c"), noNulls);
            validTest(List.of(), lists.noNullElements());
        }

        @Test
        void invalid() {
            assertThatValidation(lists.noNullElements().test(Arrays.asList("a", null, "c")).at("value"))
                    .isInvalid()
                    .hasErrorMessages("value[1].must.not.be.null");
        }
    }

    @Nested
    class AllMatch {

        @Test
        void valid() {
            Rule<List<Integer>> even = lists.allMatch(n -> n % 2 == 0);
            validTest(List.of(2, 4, 6), even);
            validTest(List.of(), lists.allMatch((Predicate<Integer>) (n -> n % 2 == 0)));
            validTest(List.of(), lists.allMatchRule(ints.even()));
        }

        @Test
        void invalid() {
            invalidTest(null, lists.allMatch((Predicate<Integer>) (n -> n % 2 == 0)), "must.not.be.null");
            invalidTest(List.of(2, 3, 4), lists.allMatchRule(ints.even()), "must.be.even");

            invalidTest(
                    List.of("a", "bb", "c"),
                    lists.allMatch(s -> s.length() == 1, ErrorMessage.of("len.must.be.one")),
                    "len.must.be.one"
            ).errorMessages().contains("[1].len.must.be.one");

            invalidTest(
                    List.of("a", "bb"),
                    lists.allMatch(s -> s.length() == 1, ErrorMessage.of("len.must.be", "len", 1)),
                    "[1].len.must.be",
                    HashMap.of("len", 1)
            );
        }

        @Test
        void throws_whenPredicateIsNull() {
            assertThatThrownBy(() ->
                    lists.allMatch(null)
            ).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class NoneMatch {

        @Test
        void valid() {
            Rule<List<Integer>> noEvens = lists.noneMatch(n -> n % 2 == 0);
            validTest(List.of(1, 3, 5), noEvens);
            validTest(List.<Integer>of(), lists.noneMatch(n -> n % 2 == 0));
        }

        @Test
        void invalid() {
            invalidTest(null, lists.noneMatch((Predicate<Integer>) (n -> n % 2 == 0)), "must.not.be.null");
            invalidTest(List.of(1, 2, 3), lists.noneMatch(n -> n % 2 == 0), "must.none.match");
            assertThatValidation(
                    lists.noneMatch((Predicate<String>) s -> s.length() == 2, ErrorMessage.of("len.must.not.be.two")).test(List.of("a", "bb", "c")).at("value")
            )
                    .isInvalid()
                    .hasErrorMessages("value[1].len.must.not.be.two");
            invalidTest(
                    List.of("a", "bb"),
                    lists.noneMatch(s -> s.length() == 2, ErrorMessage.of("len.must.not.be", "len", 2)),
                    "[1].len.must.not.be",
                    HashMap.of("len", 2)
            );
        }

        @Test
        void throws_whenPredicateIsNull() {
            assertThatThrownBy(() ->
                    lists.noneMatch(null)
            ).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class AnyMatch {

        @Test
        void valid() {
            Rule<List<Integer>> hasEven = lists.anyMatch(n -> n % 2 == 0);
            validTest(List.of(1, 2, 3), hasEven);
        }

        @Test
        void invalid() {
            invalidTest(null, lists.anyMatch((Predicate<Integer>) (n -> n % 2 == 0)), "must.not.be.null");
            invalidTest(List.of(1, 3, 5), lists.anyMatch(n -> n % 2 == 0), "must.at.least.one.match");
            invalidTest(List.<Integer>of(), lists.anyMatch(n -> n % 2 == 0), "must.at.least.one.match");
            invalidTest(
                    List.of("a", "bb", "ccc"),
                    lists.anyMatch(s -> s.length() == 4, ErrorMessage.of("len.must.be.four")),
                    "len.must.be.four"
            );
            invalidTest(
                    List.of("a", "bb"),
                    lists.anyMatch(s -> s.length() == 3, ErrorMessage.of("len.must.be", "len", 3)),
                    "len.must.be",
                    HashMap.of("len", 3)
            );
        }

        @Test
        void throws_whenPredicateIsNull() {
            assertThatThrownBy(() -> lists.anyMatch(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("predicate cannot be null");
        }
    }

    @Nested
    class Contains {

        @Test
        void valid() {
            validTest(List.of("a", "b", "c"), lists.contains("b"));
        }

        @Test
        void invalid() {
            invalidTest(null, lists.contains("b"), "must.not.be.null");
            invalidTest(
                    List.of("a", "b", "c"),
                    lists.contains("x"),
                    "must.contain",
                    HashMap.of("element", "x")
            );
        }
    }

    @Nested
    class ContainsAll {

        @Test
        void valid() {
            validTest(List.of("a", "b", "c"), lists.containsAll(List.of("a", "c")));
            validTest(List.of("a", "b"), lists.containsAll(List.of()));
            validTest(List.of("a", "b", "c"), lists.containsAll(List.of("a", "a", "c")));
        }

        @Test
        void invalid() {
            invalidTest(null, lists.containsAll(List.of("a")), "must.not.be.null");
            invalidTest(
                    List.of("a", "b"),
                    lists.containsAll(List.of("a", "c")),
                    "must.contain.all",
                    HashMap.of("required", HashSet.of("a", "c"))
            );
        }

        @Test
        void throws_whenRequiredIsNull() {
            assertThatThrownBy(() -> lists.containsAll(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("required cannot be null");
        }
    }

    @Nested
    class ContainsAnyOf {

        @Test
        void valid() {
            validTest(List.of("a", "b", "c"), lists.containsAnyOf(List.of("x", "b")));
        }

        @Test
        void invalid() {
            invalidTest(null, lists.containsAnyOf(List.of("x")), "must.not.be.null");
            invalidTest(
                    List.of("a", "b", "c"),
                    lists.containsAnyOf(List.of("x", "y")),
                    "must.contain.any.of",
                    HashMap.of("candidates", HashSet.of("x", "y"))
            );
            invalidTest(
                    List.of("a", "b"),
                    lists.containsAnyOf(List.of()),
                    "must.contain.any.of",
                    HashMap.of("candidates", HashSet.empty())
            );
        }

        @Test
        void throws_whenCandidatesIsNull() {
            assertThatThrownBy(() -> lists.containsAnyOf(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("candidates cannot be null");
        }
    }

    @Nested
    class UniqueBy {

        record Person(String email, String name) { }

        @Test
        void valid() {
           List<Person> people = List.of(
                    new Person("a@example.com", "Alice"),
                    new Person("b@example.com", "Bob"),
                    new Person("c@example.com", "Carol")
            );

            validTest(people, lists.uniqueBy(Person::email, "email"));
        }

        @Test
        void invalid() {
            invalidTest(null, lists.uniqueBy(Person::email, "email"), "must.not.be.null");
            List<Person> people = List.of(
                    new Person("a@example.com", "Alice"),   // idx 0
                    new Person("b@example.com", "Bob"),     // idx 1
                    new Person("a@example.com", "Alicia"),  // idx 2 (duplicate of idx 0)
                    new Person("b@example.com", "Bobby")    // idx 3 (duplicate of idx 1)
            );

            invalidTest(
                    people,
                    lists.uniqueBy(Person::email, "email"),
                    "must.be.unique.by.key",
                    HashMap.of(
                            "key", "email",
                            "duplicates", HashMap.of(
                                    "a@example.com", io.vavr.collection.List.of(0, 2),
                                    "b@example.com", io.vavr.collection.List.of(1, 3)
                            )
                    )
            );

            List<Person> morePeople = List.of(
                    new Person("dup@example.com", "A"), // 0
                    new Person("x@example.com", "X"),   // 1
                    new Person("dup@example.com", "B"), // 2
                    new Person("dup@example.com", "C")  // 3
            );

            invalidTest(
                    morePeople,
                    lists.uniqueBy(Person::email, "email"),
                    "must.be.unique.by.key",
                    HashMap.of(
                            "key", "email",
                            "duplicates", HashMap.of(
                                    "dup@example.com", io.vavr.collection.List.of(0, 2, 3)
                            )
                    )
            );
        }
    }

    @Nested
    class MapTests {

        @Test
        void map_withValidInput_returnsMappedValues() {
            MappingRule<String, Integer> toInt = MappingRule.of(Integer::parseInt, "must.be.integer");
            List<Integer> expected = List.of(1, 2, 3);
            
            assertThatValidation(lists.map(toInt).test(List.of("1", "2", "3")).at("value"))
                    .isValid()
                    .isEqualTo(expected);
        }

        @Test
        void map_withInvalidInput_returnsErrorsAtCorrectIndices() {
            MappingRule<String, Integer> toInt = MappingRule.of(Integer::parseInt, "must.be.integer");

            assertThatValidation(lists.map(toInt).test(List.of("1", "abc", "3")).at("value"))
                    .isInvalid()
                    .hasErrorMessages("value[1].must.be.integer");
        }
    }

    @Nested
    class ValidateValuesWith {

        @Test
        void valid() {
            Rule<Number> rule = Rule.of(n -> n.doubleValue() > 0, "must.be.positive");
            validTest(List.of(1, 10, 2), lists.validateValuesWith(rule));
        }

        @Test
        void invalid() {
            invalidTest(null, lists.validateValuesWith(Rule.of(n -> true, "")), "must.not.be.null");
            // Arrange
            Rule<Number> rule = Rule.of(n -> n.doubleValue() > 0, "must.be.positive");
            Rule<List<Integer>> listRule = lists.validateValuesWith(rule);

            List<Integer> input = List.of(-1, 10, 0);

            // Act
            var result = listRule.test(input).at("value");

            // Assert: failures are attributed to their indices in the path
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("value[0].must.be.positive", "value[2].must.be.positive");
        }
    }
}