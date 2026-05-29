package be.iffy.fv.rules.collections;

import be.iffy.fv.MappingRule;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.function.Predicate;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;
import static be.iffy.fv.rules.collections.ListRules.lists;
import static be.iffy.fv.rules.collections.VavrListRules.vavrLists;
import static be.iffy.fv.rules.numbers.IntegerRules.ints;
import static be.iffy.fv.rules.text.StringRules.strings;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VavrListRulesTest {

    @Nested
    class NotEmpty {

        @Test
        void valid() {
            validTest(List.of("x"), vavrLists.notEmpty());
        }

        @Test
        void invalid() {
            invalidTest(List.of(), vavrLists.notEmpty(), "must.not.be.empty");
        }
    }

    @Nested
    class Empty {

        @Test
        void valid() {
            validTest(List.of(), vavrLists.empty());
        }

        @Test
        void invalid() {
            invalidTest(List.of("x"), vavrLists.empty(), "must.be.empty");
        }
    }

    @Nested
    class MinSize {

        @Test
        void valid() {
            validTest(List.of(), vavrLists.minSize(0));
            validTest(List.of("x"), vavrLists.minSize(1));
            validTest(List.of("a", "b"), vavrLists.minSize(1));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrLists.minSize(1), "must.not.be.null");
            invalidTest(List.of(), vavrLists.minSize(1), "must.have.min.size", io.vavr.collection.HashMap.of("min", 1));
            invalidTest(List.of("x"), vavrLists.minSize(2), "must.have.min.size", io.vavr.collection.HashMap.of("min", 2));
        }
    }

    @Nested
    class MaxSize {

        @Test
        void valid() {
            validTest(List.of(), vavrLists.maxSize(0));
            validTest(List.of("x"), vavrLists.maxSize(1));
            validTest(List.of("a", "b"), vavrLists.maxSize(2));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrLists.maxSize(0), "must.not.be.null");
            invalidTest(List.of("x"), vavrLists.maxSize(0), "must.have.max.size", io.vavr.collection.HashMap.of("max", 0));
            invalidTest(List.of("a", "b", "c"), vavrLists.maxSize(2), "must.have.max.size", io.vavr.collection.HashMap.of("max", 2));
        }
    }

    @Nested
    class SizeEquals {

        @Test
        void valid() {
            validTest(List.of(), vavrLists.sizeEquals(0));
            validTest(List.of("x"), vavrLists.sizeEquals(1));
            validTest(List.of("a", "b"), vavrLists.sizeEquals(2));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrLists.sizeEquals(1), "must.not.be.null");
            invalidTest(List.of(), vavrLists.sizeEquals(1), "must.have.exact.size", io.vavr.collection.HashMap.of("equal", 1));
            invalidTest(List.of("x"), vavrLists.sizeEquals(0), "must.have.exact.size", io.vavr.collection.HashMap.of("equal", 0));
        }
    }

    @Nested
    class SizeBetween {

        @Test
        void valid() {
            validTest(List.of(), vavrLists.sizeBetween(0, 0));
            validTest(List.of("x"), vavrLists.sizeBetween(0, 1));
            validTest(List.of("a", "b"), vavrLists.sizeBetween(1, 2));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrLists.sizeBetween(1, 2), "must.not.be.null");
            invalidTest(
                    List.of(),
                    vavrLists.sizeBetween(1, 2),
                    "must.have.size.between",
                    io.vavr.collection.HashMap.of("min", 1, "max", 2)
            );
            invalidTest(
                    List.of("a", "b", "c"),
                    vavrLists.sizeBetween(1, 2),
                    "must.have.size.between",
                    io.vavr.collection.HashMap.of("min", 1, "max", 2)
            );
        }
    }

    @Nested
    class NoNullElements {

        @Test
        void valid() {
            Rule<List<String>> noNulls = vavrLists.noNullElements();
            validTest(List.of("a", "b", "c"), noNulls);
            validTest(List.empty(), vavrLists.noNullElements());
        }

        @Test
        void invalid() {
            invalidTest(List.of("a", null, "c"), vavrLists.noNullElements(), "must.not.be.null").hasErrorMessage("[1].must.not.be.null");
        }
    }

    @Nested
    class AllMatch {

        @Test
        void valid() {
            Rule<List<Integer>> even = vavrLists.allMatch(n -> n % 2 == 0);
            validTest(List.of(2, 4, 6), even);
            validTest(List.<Integer>of(), vavrLists.allMatch(n -> n % 2 == 0));
            validTest(List.of("a", "b", "c"), vavrLists.allMatchRule(strings.length(1)));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrLists.allMatch((Predicate<Integer>) (n -> n % 2 == 0)), "must.not.be.null");
            invalidTest(List.of(2, 3, 4), vavrLists.allMatch(n -> n % 2 == 0), "must.all.match");

            invalidTest(
                    List.of("a", "bb", "c"),
                    vavrLists.allMatch((String s) -> s.length() == 1, ErrorMessage.of("len.must.be.one")),
                    "len.must.be.one"
            ).hasErrorMessages("[1].len.must.be.one");

            invalidTest(
                    List.of("a", "bb"),
                    vavrLists.allMatch(s -> s.length() == 1, ErrorMessage.of("len.must.be", "len", 1)),
                    "[1].len.must.be",
                    HashMap.of("len", 1)
            );

            invalidTest(
                    List.of("a", "bb", "c"),
                    vavrLists.allMatchRule(strings.length(1)),
                    "must.have.length"
            ).hasErrorMessage("[1].must.have.length");
        }

        @Test
        void throws_whenPredicateIsNull() {
            assertThatThrownBy(() -> vavrLists.allMatch(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        void throws_whenRuleIsNull() {
                assertThatThrownBy(() -> vavrLists.allMatchRule(null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class NoneMatch {

        @Test
        void valid() {
            Rule<List<Integer>> noEvens = vavrLists.noneMatch(ints.even().toPredicate());
            validTest(List.of(1, 3, 5), noEvens);
            validTest(List.of(), vavrLists.noneMatch(ints.even().toPredicate()));
            validTest(List.of("a", "bbb", "cccc"), vavrLists.noneMatchRule(strings.length(2)));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrLists.noneMatch(ints.even().toPredicate()), "must.not.be.null");
            invalidTest(List.of(1, 2, 3), vavrLists.noneMatch(ints.even().toPredicate()), "must.none.match");
            invalidTest(
                    List.of("a", "bb", "c"),
                    vavrLists.noneMatch((String s) -> s.length() == 2, ErrorMessage.of("len.must.not.be.two")),
                    "len.must.not.be.two"
            ).hasErrorMessage("[1].len.must.not.be.two");

            invalidTest(
                    List.of("a", "bb"),
                    vavrLists.noneMatch(s -> s.length() == 2, ErrorMessage.of("len.must.not.be", "len", 2)),
                    "[1].len.must.not.be",
                    HashMap.of("len", 2)
            );

            invalidTest(
                    List.of("a", "bb", "c"),
                    vavrLists.noneMatchRule(strings.length(2)),
                    "must.none.match"
            ).hasErrorMessages("[1].must.none.match");
        }

        @Test
        void throws_whenPredicateIsNull() {
            assertThatThrownBy(() ->
                    vavrLists.noneMatch(null)
            ).isInstanceOf(NullPointerException.class);
        }

        @Test
        void throws_whenRuleIsNull() {
            assertThatThrownBy(() ->
                    vavrLists.noneMatchRule(null)
            ).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class AnyMatch {

        @Test
        void valid() {
            Rule<List<Integer>> hasEven = vavrLists.anyMatch(n -> n % 2 == 0);
            validTest(List.of(1, 2, 3), hasEven);
        }

        @Test
        void invalid() {
            invalidTest(null, vavrLists.anyMatch((Integer n) -> n % 2 == 0), "must.not.be.null");
            invalidTest(List.of(1, 3, 5), vavrLists.anyMatch((Integer n) -> n % 2 == 0), "must.at.least.one.match");
            invalidTest(List.of(), vavrLists.anyMatch((Integer n) -> n % 2 == 0), "must.at.least.one.match");
            invalidTest(
                    List.of("a", "bb", "ccc"),
                    vavrLists.anyMatch((String s) -> s.length() == 4, ErrorMessage.of("len.must.be.four")),
                    "len.must.be.four"
            );
            invalidTest(
                    List.of("a", "bb"),
                    vavrLists.anyMatch((String s) -> s.length() == 3, ErrorMessage.of("len.must.be", "len", 3)),
                    "len.must.be",
                    HashMap.of("len", 3)
            );
        }

        @Test
        void throws_whenPredicateIsNull() {
            assertThatThrownBy(() -> vavrLists.anyMatch(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("predicate cannot be null");
        }
    }

    @Nested
    class Contains {

        @Test
        void valid() {
            validTest(List.of("a", "b", "c"), vavrLists.contains("b"));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrLists.contains("b"), "must.not.be.null");
            invalidTest(
                    List.of("a", "b", "c"),
                    vavrLists.contains("x"),
                    "must.contain",
                    HashMap.of("element", "x")
            );
        }
    }

    @Nested
    class ContainsAll {

        @Test
        void valid() {
            validTest(List.of("a", "b", "c"), vavrLists.containsAll(List.of("a", "c")));
            validTest(List.of("a", "b"), vavrLists.containsAll(List.empty()));
            validTest(List.of("a", "b", "c"), vavrLists.containsAll(List.of("a", "a", "c")));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrLists.containsAll(List.of("a")), "must.not.be.null");
            invalidTest(
                    List.of("a", "b"),
                    vavrLists.containsAll(List.of("a", "c")),
                    "must.contain.all",
                    HashMap.of("required", HashSet.of("a", "c"))
            );
        }

        @Test
        void throws_whenRequiredIsNull() {
            assertThatThrownBy(() -> vavrLists.containsAll(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("required cannot be null");
        }
    }

    @Nested
    class ContainsAnyOf {

        @Test
        void valid() {
            validTest(List.of("a", "b", "c"), vavrLists.containsAnyOf(List.of("x", "b")));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrLists.containsAnyOf(List.of("x")), "must.not.be.null");
            invalidTest(
                    List.of("a", "b", "c"),
                    vavrLists.containsAnyOf(List.of("x", "y")),
                    "must.contain.any.of",
                    HashMap.of("candidates", HashSet.of("x", "y"))
            );
            invalidTest(
                    List.of("a", "b"),
                    vavrLists.containsAnyOf(List.empty()),
                    "must.contain.any.of",
                    HashMap.of("candidates", HashSet.empty())
            );
        }

        @Test
        void throws_whenCandidatesIsNull() {
            assertThatThrownBy(() -> vavrLists.containsAnyOf(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("candidates cannot be null");
        }
    }

    @Nested
    class UniqueBy {

        record Person(String email, String name) {
        }

        @Test
        void valid() {
            List<Person> people = List.of(
                    new Person("a@example.com", "Alice"),
                    new Person("b@example.com", "Bob"),
                    new Person("c@example.com", "Carol")
            );

            validTest(people, vavrLists.uniqueBy(Person::email, "email"));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrLists.uniqueBy(Person::email, "email"), "must.not.be.null");
            List<Person> people = List.of(
                    new Person("a@example.com", "Alice"),   // idx 0
                    new Person("b@example.com", "Bob"),     // idx 1
                    new Person("a@example.com", "Alicia"),  // idx 2 (duplicate of idx 0)
                    new Person("b@example.com", "Bobby")    // idx 3 (duplicate of idx 1)
            );

            invalidTest(
                    people,
                    vavrLists.uniqueBy(Person::email, "email"),
                    "must.be.unique.by.key",
                    HashMap.of(
                            "key", "email",
                            "duplicates", HashMap.of(
                                    "a@example.com", List.of(0, 2),
                                    "b@example.com", List.of(1, 3)
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
                    vavrLists.uniqueBy(Person::email, "email"),
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
    class MapTests {

        @Test
        void map_withValidInput_returnsMappedValues() {
            MappingRule<String, Integer> toInt = MappingRule.of(Integer::parseInt, "must.be.integer");
            List<Integer> expected = List.of(1, 2, 3);

            assertThatValidation(vavrLists.map(toInt).test(List.of("1", "2", "3")).at("value"))
                    .isValid()
                    .isEqualTo(expected);
        }

        @Test
        void map_withInvalidInput_returnsErrorsAtCorrectIndices() {
            MappingRule<String, Integer> toInt = MappingRule.of(Integer::parseInt, "must.be.integer");

            assertThatValidation(vavrLists.map(toInt).test(List.of("1", "abc", "3")).at("value"))
                    .isInvalid()
                    .hasErrorMessages("value[1].must.be.integer");
        }
    }

    @Nested
    class ValidateValuesWith {

        @Test
        void valid() {
            Rule<Number> rule = Rule.of(n -> n.doubleValue() > 0, "must.be.positive");
            validTest(List.of(1, 10, 2), vavrLists.validateValuesWith(rule));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrLists.validateValuesWith(Rule.of(n -> true, "")), "must.not.be.null");

            Rule<List<Integer>> listRule = vavrLists.validateValuesWith(ints.positive());

            invalidTest(List.of(-1, 10), listRule, "must.be.positive").hasErrorMessages("[0].must.be.positive");
            invalidTest(List.of(10, 0), listRule, "must.be.positive").hasErrorMessages("[1].must.be.positive");
        }
    }
}