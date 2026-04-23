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

import static be.iffy.fv.dsl.DSL.validateThat;
import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;
import static be.iffy.fv.rules.collections.CollectionRules.collections;
import static be.iffy.fv.rules.numbers.IntegerRules.ints;
import static be.iffy.fv.rules.text.StringRules.strings;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollectionRulesTest {

    @Nested
    class NotEmpty {

        @Test
        void valid() {
            validTest(List.of("x"), collections().notEmpty());
            validTest(HashSet.of(1), collections().notEmpty());
        }

        @Test
        void invalid() {
            invalidTest(new ArrayList<>(), collections().notEmpty(), "must.not.be.empty");
            invalidTest(HashSet.of(), collections().notEmpty(), "must.not.be.empty");
        }
    }

    @Nested
    class Empty {

        @Test
        void valid() {
            validTest(new ArrayList<>(), collections().empty());
            validTest(HashSet.of(), collections().empty());
        }

        @Test
        void invalid() {
            invalidTest(List.of("x"), collections().empty(), "must.be.empty");
            invalidTest(HashSet.of(1), collections().empty(), "must.be.empty");
        }
    }

    @Nested
    class MinSize {

        @Test
        void valid() {
            validTest(List.of(), collections.minSize(0));
            validTest(List.of("x"), collections.minSize(1));
            validTest(List.of("a", "b"), collections.minSize(1));
        }

        @Test
        void invalid() {
            invalidTest(null, collections.minSize(1), "must.not.be.null");
            invalidTest(List.of(), collections.minSize(1), "must.have.min.size", io.vavr.collection.HashMap.of("min", 1));
            invalidTest(List.of("x"), collections.minSize(2), "must.have.min.size", io.vavr.collection.HashMap.of("min", 2));
        }
    }

    @Nested
    class MaxSize {

        @Test
        void valid() {
            validTest(List.of(), collections.maxSize(0));
            validTest(List.of("x"), collections.maxSize(1));
            validTest(List.of("a", "b"), collections.maxSize(2));
        }

        @Test
        void invalid() {
            invalidTest(null, collections.maxSize(0), "must.not.be.null");
            invalidTest(List.of("x"), collections.maxSize(0), "must.have.max.size", io.vavr.collection.HashMap.of("max", 0));
            invalidTest(List.of("a", "b", "c"), collections.maxSize(2), "must.have.max.size", io.vavr.collection.HashMap.of("max", 2));
        }
    }

    @Nested
    class SizeEquals {

        @Test
        void valid() {
            validTest(List.of(), collections.sizeEquals(0));
            validTest(List.of("x"), collections.sizeEquals(1));
            validTest(List.of("a", "b"), collections.sizeEquals(2));
        }

        @Test
        void invalid() {
            invalidTest(null, collections.sizeEquals(1), "must.not.be.null");
            invalidTest(List.of(), collections.sizeEquals(1), "must.have.exact.size", io.vavr.collection.HashMap.of("equal", 1));
            invalidTest(List.of("x"), collections.sizeEquals(0), "must.have.exact.size", io.vavr.collection.HashMap.of("equal", 0));
        }
    }

    @Nested
    class SizeBetween {

        @Test
        void valid() {
            validTest(List.of(), collections.sizeBetween(0, 0));
            validTest(List.of("x"), collections.sizeBetween(0, 1));
            validTest(List.of("a", "b"), collections.sizeBetween(1, 2));
        }

        @Test
        void invalid() {
            invalidTest(null, collections.sizeBetween(1, 2), "must.not.be.null");
            invalidTest(
                    List.of(),
                    collections.sizeBetween(1, 2),
                    "must.have.size.between",
                    io.vavr.collection.HashMap.of("min", 1, "max", 2)
            );
            invalidTest(
                    List.of("a", "b", "c"),
                    collections.sizeBetween(1, 2),
                    "must.have.size.between",
                    io.vavr.collection.HashMap.of("min", 1, "max", 2)
            );
        }
    }

    @Nested
    class NoNullElements {

        @Test
        void valid() {
            Rule<List<String>> noNulls = collections.noNullElements();
            validTest(List.of("a", "b", "c"), noNulls);
            validTest(List.empty(), collections.noNullElements());
        }

        @Test
        void invalid() {
            invalidTest(List.of("a", null, "c"), collections.noNullElements(), "must.not.be.null").hasErrorMessage("value[1].must.not.be.null");
        }
    }

    @Nested
    class AllMatch {

        @Test
        void valid() {
            Rule<List<Integer>> even = collections.allMatch(n -> n % 2 == 0);
            validTest(List.of(2, 4, 6), even);
            validTest(List.<Integer>of(), collections.allMatch(n -> n % 2 == 0));
            validTest(List.of("a", "b", "c"), collections.allMatchRule(strings().exactLength(1)));
        }

        @Test
        void invalid() {
            invalidTest(null, collections.allMatch((Predicate<Integer>) (n -> n % 2 == 0)), "must.not.be.null");
            invalidTest(List.of(2, 3, 4), collections.allMatch(n -> n % 2 == 0), "must.all.match");

            invalidTest(
                    List.of("a", "bb", "c"),
                    collections.allMatch((String s) -> s.length() == 1, ErrorMessage.of("len.must.be.one")),
                    "len.must.be.one"
            ).hasErrorMessages("value[1].len.must.be.one");

            invalidTest(
                    List.of("a", "bb"),
                    collections.allMatch(s -> s.length() == 1, ErrorMessage.of("len.must.be", "len", 1)),
                    "len.must.be",
                    HashMap.of("len", 1)
            );

            invalidTest(
                    List.of("a", "bb", "c"),
                    collections.allMatchRule(strings().exactLength(1)),
                    "must.have.exact.length"
            ).hasErrorMessage("value[1].must.have.exact.length");
        }

        @Test
        void throws_whenPredicateIsNull_andRuleIsEvaluated() {
            assertThatThrownBy(() ->
                    validateThat(List.of(1), "value").is(collections.allMatch(null)).getOrElseThrow()
            ).isInstanceOf(NullPointerException.class);
        }

        @Test
        void throws_whenRuleIsNull_andRuleIsEvaluated() {
            assertThatThrownBy(() ->
                    validateThat(List.of("x"), "value").is(collections.allMatchRule(null)).getOrElseThrow()
            ).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class NoneMatch {

        @Test
        void valid() {
            Rule<List<Integer>> noEvens = collections.noneMatch(ints().even().toPredicate());
            validTest(List.of(1, 3, 5), noEvens);
            validTest(List.of(), collections.noneMatch(ints().even().toPredicate()));
            validTest(List.of("a", "bbb", "cccc"), collections.noneMatchRule(strings.exactLength(2)));
        }

        @Test
        void invalid() {
            invalidTest(null, collections.noneMatch(ints().even().toPredicate()), "must.not.be.null");
            invalidTest(List.of(1, 2, 3), collections.noneMatch(ints().even().toPredicate()), "must.none.match");
            invalidTest(
                    List.of("a", "bb", "c"),
                    collections.noneMatch((String s) -> s.length() == 2, ErrorMessage.of("len.must.not.be.two")),
                    "len.must.not.be.two"
            ).hasErrorMessage("value[1].len.must.not.be.two");

            invalidTest(
                    List.of("a", "bb"),
                    collections.noneMatch(s -> s.length() == 2, ErrorMessage.of("len.must.not.be", "len", 2)),
                    "len.must.not.be",
                    HashMap.of("len", 2)
            );

            invalidTest(
                    List.of("a", "bb", "c"),
                    collections.noneMatchRule(strings().exactLength(2)),
                    "must.none.match"
            ).hasErrorMessages("value[1].must.none.match");
        }

        @Test
        void throws_whenPredicateIsNull_andRuleIsEvaluated() {
            assertThatThrownBy(() ->
                    validateThat(List.of(1), "value").is(collections.noneMatch(null)).getOrElseThrow()
            ).isInstanceOf(NullPointerException.class);
        }

        @Test
        void throws_whenRuleIsNull_andRuleIsEvaluated() {
            assertThatThrownBy(() ->
                    validateThat(List.of("x"), "value").is(collections.noneMatchRule(null)).getOrElseThrow()
            ).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class AnyMatch {

        @Test
        void valid() {
            Rule<List<Integer>> hasEven = collections.anyMatch(n -> n % 2 == 0);
            validTest(List.of(1, 2, 3), hasEven);
        }

        @Test
        void invalid() {
            invalidTest(null, collections.anyMatch((Integer n) -> n % 2 == 0), "must.not.be.null");
            invalidTest(List.of(1, 3, 5), collections.anyMatch((Integer n) -> n % 2 == 0), "must.at.least.one.match");
            invalidTest(List.<Integer>of(), collections.anyMatch((Integer n) -> n % 2 == 0), "must.at.least.one.match");
            invalidTest(
                    List.of("a", "bb", "ccc"),
                    collections.anyMatch((String s) -> s.length() == 4, ErrorMessage.of("len.must.be.four")),
                    "len.must.be.four"
            );
            invalidTest(
                    List.of("a", "bb"),
                    collections.anyMatch((String s) -> s.length() == 3, ErrorMessage.of("len.must.be", "len", 3)),
                    "len.must.be",
                    HashMap.of("len", 3)
            );
        }

        @Test
        void throws_whenPredicateIsNull() {
            assertThatThrownBy(() -> collections.anyMatch(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("predicate cannot be null");
        }
    }

    @Nested
    class Contains {

        @Test
        void valid() {
            validTest(List.of("a", "b", "c"), collections.contains("b"));
        }

        @Test
        void invalid() {
            invalidTest(null, collections.contains("b"), "must.not.be.null");
            invalidTest(
                    List.of("a", "b", "c"),
                    collections.contains("x"),
                    "must.contain",
                    HashMap.of("element", "x")
            );
        }
    }

    @Nested
    class ContainsAll {

        @Test
        void valid() {
            validTest(List.of("a", "b", "c"), collections.containsAll(List.of("a", "c")));
            validTest(List.of("a", "b"), collections.containsAll(List.empty()));
            validTest(List.of("a", "b", "c"), collections.containsAll(List.of("a", "a", "c")));
        }

        @Test
        void invalid() {
            invalidTest(null, collections.containsAll(List.of("a")), "must.not.be.null");
            invalidTest(
                    List.of("a", "b"),
                    collections.containsAll(List.of("a", "c")),
                    "must.contain.all",
                    HashMap.of("required", HashSet.of("a", "c"))
            );
        }

        @Test
        void throws_whenRequiredIsNull() {
            assertThatThrownBy(() -> collections.containsAll(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("required cannot be null");
        }
    }

    @Nested
    class ContainsAnyOf {

        @Test
        void valid() {
            validTest(List.of("a", "b", "c"), collections.containsAnyOf(List.of("x", "b")));
        }

        @Test
        void invalid() {
            invalidTest(null, collections.containsAnyOf(List.of("x")), "must.not.be.null");
            invalidTest(
                    List.of("a", "b", "c"),
                    collections.containsAnyOf(List.of("x", "y")),
                    "must.contain.any.of",
                    HashMap.of("candidates", HashSet.of("x", "y"))
            );
            invalidTest(
                    List.of("a", "b"),
                    collections.containsAnyOf(List.empty()),
                    "must.contain.any.of",
                    HashMap.of("candidates", HashSet.empty())
            );
        }

        @Test
        void throws_whenCandidatesIsNull() {
            assertThatThrownBy(() -> collections.containsAnyOf(null))
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

            validTest(people, collections.uniqueBy(Person::email, "email"));
        }

        @Test
        void invalid() {
            invalidTest(null, collections.uniqueBy(Person::email, "email"), "must.not.be.null");
            List<Person> people = List.of(
                    new Person("a@example.com", "Alice"),   // idx 0
                    new Person("b@example.com", "Bob"),     // idx 1
                    new Person("a@example.com", "Alicia"),  // idx 2 (duplicate of idx 0)
                    new Person("b@example.com", "Bobby")    // idx 3 (duplicate of idx 1)
            );

            invalidTest(
                    people,
                    collections.uniqueBy(Person::email, "email"),
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
                    collections.uniqueBy(Person::email, "email"),
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
            validTest(List.of(1, 10, 2), collections.validateValuesWith(rule));
        }

        @Test
        void invalid() {
            invalidTest(null, collections.validateValuesWith(Rule.of(n -> true, "")), "must.not.be.null");

            Rule<List<Integer>> listRule = collections.validateValuesWith(ints().positive());

            invalidTest(List.of(-1, 10), listRule, "must.be.positive").hasErrorMessages("value[0].must.be.positive");
            invalidTest(List.of(10, 0), listRule, "must.be.positive").hasErrorMessages("value[1].must.be.positive");
        }
    }
}