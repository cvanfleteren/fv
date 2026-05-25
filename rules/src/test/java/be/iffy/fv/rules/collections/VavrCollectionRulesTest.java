package be.iffy.fv.rules.collections;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.function.Predicate;

import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;
import static be.iffy.fv.rules.collections.VavrCollectionRules.vavrCollections;
import static be.iffy.fv.rules.numbers.IntegerRules.ints;
import static be.iffy.fv.rules.text.StringRules.strings;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VavrCollectionRulesTest {

    @Nested
    class NotEmpty {

        @Test
        void valid() {
            validTest(List.of("x"), vavrCollections.notEmpty());
            validTest(HashSet.of(1), vavrCollections.notEmpty());
        }

        @Test
        void invalid() {
            invalidTest(new ArrayList<>(), vavrCollections.notEmpty(), "must.not.be.empty");
            invalidTest(HashSet.of(), vavrCollections.notEmpty(), "must.not.be.empty");
        }
    }

    @Nested
    class Empty {

        @Test
        void valid() {
            validTest(new ArrayList<>(), vavrCollections.empty());
            validTest(HashSet.of(), vavrCollections.empty());
        }

        @Test
        void invalid() {
            invalidTest(List.of("x"), vavrCollections.empty(), "must.be.empty");
            invalidTest(HashSet.of(1), vavrCollections.empty(), "must.be.empty");
        }
    }

    @Nested
    class MinSize {

        @Test
        void valid() {
            validTest(List.of(), vavrCollections.minSize(0));
            validTest(List.of("x"), vavrCollections.minSize(1));
            validTest(List.of("a", "b"), vavrCollections.minSize(1));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrCollections.minSize(1), "must.not.be.null");
            invalidTest(List.of(), vavrCollections.minSize(1), "must.have.min.size", io.vavr.collection.HashMap.of("min", 1));
            invalidTest(List.of("x"), vavrCollections.minSize(2), "must.have.min.size", io.vavr.collection.HashMap.of("min", 2));
        }
    }

    @Nested
    class MaxSize {

        @Test
        void valid() {
            validTest(List.of(), vavrCollections.maxSize(0));
            validTest(List.of("x"), vavrCollections.maxSize(1));
            validTest(List.of("a", "b"), vavrCollections.maxSize(2));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrCollections.maxSize(0), "must.not.be.null");
            invalidTest(List.of("x"), vavrCollections.maxSize(0), "must.have.max.size", io.vavr.collection.HashMap.of("max", 0));
            invalidTest(List.of("a", "b", "c"), vavrCollections.maxSize(2), "must.have.max.size", io.vavr.collection.HashMap.of("max", 2));
        }
    }

    @Nested
    class SizeEquals {

        @Test
        void valid() {
            validTest(List.of(), vavrCollections.sizeEquals(0));
            validTest(List.of("x"), vavrCollections.sizeEquals(1));
            validTest(List.of("a", "b"), vavrCollections.sizeEquals(2));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrCollections.sizeEquals(1), "must.not.be.null");
            invalidTest(List.of(), vavrCollections.sizeEquals(1), "must.have.exact.size", io.vavr.collection.HashMap.of("equal", 1));
            invalidTest(List.of("x"), vavrCollections.sizeEquals(0), "must.have.exact.size", io.vavr.collection.HashMap.of("equal", 0));
        }
    }

    @Nested
    class SizeBetween {

        @Test
        void valid() {
            validTest(List.of(), vavrCollections.sizeBetween(0, 0));
            validTest(List.of("x"), vavrCollections.sizeBetween(0, 1));
            validTest(List.of("a", "b"), vavrCollections.sizeBetween(1, 2));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrCollections.sizeBetween(1, 2), "must.not.be.null");
            invalidTest(
                    List.of(),
                    vavrCollections.sizeBetween(1, 2),
                    "must.have.size.between",
                    io.vavr.collection.HashMap.of("min", 1, "max", 2)
            );
            invalidTest(
                    List.of("a", "b", "c"),
                    vavrCollections.sizeBetween(1, 2),
                    "must.have.size.between",
                    io.vavr.collection.HashMap.of("min", 1, "max", 2)
            );
        }
    }

    @Nested
    class NoNullElements {

        @Test
        void valid() {
            Rule<List<String>> noNulls = vavrCollections.noNullElements();
            validTest(List.of("a", "b", "c"), noNulls);
            validTest(List.empty(), vavrCollections.noNullElements());
        }

        @Test
        void invalid() {
            invalidTest(List.of("a", null, "c"), vavrCollections.noNullElements(), "must.not.be.null").hasErrorMessage("[1].must.not.be.null");
        }
    }

    @Nested
    class AllMatch {

        @Test
        void valid() {
            Rule<List<Integer>> even = vavrCollections.allMatch(n -> n % 2 == 0);
            validTest(List.of(2, 4, 6), even);
            validTest(List.<Integer>of(), vavrCollections.allMatch(n -> n % 2 == 0));
            validTest(List.of("a", "b", "c"), vavrCollections.allMatchRule(strings.length(1)));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrCollections.allMatch((Predicate<Integer>) (n -> n % 2 == 0)), "must.not.be.null");
            invalidTest(List.of(2, 3, 4), vavrCollections.allMatch(n -> n % 2 == 0), "must.all.match");

            invalidTest(
                    List.of("a", "bb", "c"),
                    vavrCollections.allMatch((String s) -> s.length() == 1, ErrorMessage.of("len.must.be.one")),
                    "len.must.be.one"
            ).hasErrorMessages("[1].len.must.be.one");

            invalidTest(
                    List.of("a", "bb"),
                    vavrCollections.allMatch(s -> s.length() == 1, ErrorMessage.of("len.must.be", "len", 1)),
                    "[1].len.must.be",
                    HashMap.of("len", 1)
            );

            invalidTest(
                    List.of("a", "bb", "c"),
                    vavrCollections.allMatchRule(strings.length(1)),
                    "must.have.length"
            ).hasErrorMessage("[1].must.have.length");
        }

        @Test
        void throws_whenPredicateIsNull() {
            assertThatThrownBy(() -> vavrCollections.allMatch(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        void throws_whenRuleIsNull() {
                assertThatThrownBy(() -> vavrCollections.allMatchRule(null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class NoneMatch {

        @Test
        void valid() {
            Rule<List<Integer>> noEvens = vavrCollections.noneMatch(ints().even().toPredicate());
            validTest(List.of(1, 3, 5), noEvens);
            validTest(List.of(), vavrCollections.noneMatch(ints().even().toPredicate()));
            validTest(List.of("a", "bbb", "cccc"), vavrCollections.noneMatchRule(strings.length(2)));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrCollections.noneMatch(ints().even().toPredicate()), "must.not.be.null");
            invalidTest(List.of(1, 2, 3), vavrCollections.noneMatch(ints().even().toPredicate()), "must.none.match");
            invalidTest(
                    List.of("a", "bb", "c"),
                    vavrCollections.noneMatch((String s) -> s.length() == 2, ErrorMessage.of("len.must.not.be.two")),
                    "len.must.not.be.two"
            ).hasErrorMessage("[1].len.must.not.be.two");

            invalidTest(
                    List.of("a", "bb"),
                    vavrCollections.noneMatch(s -> s.length() == 2, ErrorMessage.of("len.must.not.be", "len", 2)),
                    "[1].len.must.not.be",
                    HashMap.of("len", 2)
            );

            invalidTest(
                    List.of("a", "bb", "c"),
                    vavrCollections.noneMatchRule(strings.length(2)),
                    "must.none.match"
            ).hasErrorMessages("[1].must.none.match");
        }

        @Test
        void throws_whenPredicateIsNull() {
            assertThatThrownBy(() ->
                    vavrCollections.noneMatch(null)
            ).isInstanceOf(NullPointerException.class);
        }

        @Test
        void throws_whenRuleIsNull() {
            assertThatThrownBy(() ->
                    vavrCollections.noneMatchRule(null)
            ).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class AnyMatch {

        @Test
        void valid() {
            Rule<List<Integer>> hasEven = vavrCollections.anyMatch(n -> n % 2 == 0);
            validTest(List.of(1, 2, 3), hasEven);
        }

        @Test
        void invalid() {
            invalidTest(null, vavrCollections.anyMatch((Integer n) -> n % 2 == 0), "must.not.be.null");
            invalidTest(List.of(1, 3, 5), vavrCollections.anyMatch((Integer n) -> n % 2 == 0), "must.at.least.one.match");
            invalidTest(List.of(), vavrCollections.anyMatch((Integer n) -> n % 2 == 0), "must.at.least.one.match");
            invalidTest(
                    List.of("a", "bb", "ccc"),
                    vavrCollections.anyMatch((String s) -> s.length() == 4, ErrorMessage.of("len.must.be.four")),
                    "len.must.be.four"
            );
            invalidTest(
                    List.of("a", "bb"),
                    vavrCollections.anyMatch((String s) -> s.length() == 3, ErrorMessage.of("len.must.be", "len", 3)),
                    "len.must.be",
                    HashMap.of("len", 3)
            );
        }

        @Test
        void throws_whenPredicateIsNull() {
            assertThatThrownBy(() -> vavrCollections.anyMatch(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("predicate cannot be null");
        }
    }

    @Nested
    class Contains {

        @Test
        void valid() {
            validTest(List.of("a", "b", "c"), vavrCollections.contains("b"));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrCollections.contains("b"), "must.not.be.null");
            invalidTest(
                    List.of("a", "b", "c"),
                    vavrCollections.contains("x"),
                    "must.contain",
                    HashMap.of("element", "x")
            );
        }
    }

    @Nested
    class ContainsAll {

        @Test
        void valid() {
            validTest(List.of("a", "b", "c"), vavrCollections.containsAll(List.of("a", "c")));
            validTest(List.of("a", "b"), vavrCollections.containsAll(List.empty()));
            validTest(List.of("a", "b", "c"), vavrCollections.containsAll(List.of("a", "a", "c")));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrCollections.containsAll(List.of("a")), "must.not.be.null");
            invalidTest(
                    List.of("a", "b"),
                    vavrCollections.containsAll(List.of("a", "c")),
                    "must.contain.all",
                    HashMap.of("required", HashSet.of("a", "c"))
            );
        }

        @Test
        void throws_whenRequiredIsNull() {
            assertThatThrownBy(() -> vavrCollections.containsAll(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("required cannot be null");
        }
    }

    @Nested
    class ContainsAnyOf {

        @Test
        void valid() {
            validTest(List.of("a", "b", "c"), vavrCollections.containsAnyOf(List.of("x", "b")));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrCollections.containsAnyOf(List.of("x")), "must.not.be.null");
            invalidTest(
                    List.of("a", "b", "c"),
                    vavrCollections.containsAnyOf(List.of("x", "y")),
                    "must.contain.any.of",
                    HashMap.of("candidates", HashSet.of("x", "y"))
            );
            invalidTest(
                    List.of("a", "b"),
                    vavrCollections.containsAnyOf(List.empty()),
                    "must.contain.any.of",
                    HashMap.of("candidates", HashSet.empty())
            );
        }

        @Test
        void throws_whenCandidatesIsNull() {
            assertThatThrownBy(() -> vavrCollections.containsAnyOf(null))
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

            validTest(people, vavrCollections.uniqueBy(Person::email, "email"));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrCollections.uniqueBy(Person::email, "email"), "must.not.be.null");
            List<Person> people = List.of(
                    new Person("a@example.com", "Alice"),   // idx 0
                    new Person("b@example.com", "Bob"),     // idx 1
                    new Person("a@example.com", "Alicia"),  // idx 2 (duplicate of idx 0)
                    new Person("b@example.com", "Bobby")    // idx 3 (duplicate of idx 1)
            );

            invalidTest(
                    people,
                    vavrCollections.uniqueBy(Person::email, "email"),
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
                    vavrCollections.uniqueBy(Person::email, "email"),
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
        void valid() {
            Rule<Number> rule = Rule.of(n -> n.doubleValue() > 0, "must.be.positive");
            validTest(List.of(1, 10, 2), vavrCollections.validateValuesWith(rule));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrCollections.validateValuesWith(Rule.of(n -> true, "")), "must.not.be.null");

            Rule<List<Integer>> listRule = vavrCollections.validateValuesWith(ints().positive());

            invalidTest(List.of(-1, 10), listRule, "must.be.positive").hasErrorMessages("[0].must.be.positive");
            invalidTest(List.of(10, 0), listRule, "must.be.positive").hasErrorMessages("[1].must.be.positive");
        }
    }
}