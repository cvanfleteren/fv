package be.iffy.fv.rules.collections;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Predicate;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;
import static be.iffy.fv.rules.collections.ListRules.lists;
import static be.iffy.fv.rules.collections.SetRules.sets;
import static be.iffy.fv.rules.numbers.IntegerRules.ints;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SetRulesTest {

    @Nested
    class NotEmpty {

        @Test
        void valid() {
            validTest(Set.of("x"), sets.notEmpty());
        }

        @Test
        void invalid() {
            invalidTest(new java.util.HashSet<>(), sets.notEmpty(), "must.not.be.empty");
        }
    }

    @Nested
    class Empty {

        @Test
        void valid() {
            validTest(Set.of(), sets.empty());
            validTest(new java.util.HashSet<>(), sets.empty());
        }

        @Test
        void invalid() {
            invalidTest(Set.of("x"), sets.empty(), "must.be.empty");
        }
    }

    @Nested
    class MinSize {

        @Test
        void valid() {
            validTest(Set.of(), sets.minSize(0));
            validTest(Set.of("x"), sets.minSize(1));
            validTest(Set.of("a", "b"), sets.minSize(1));
        }

        @Test
        void invalid() {
            invalidTest(null, sets.minSize(1), "must.not.be.null");
            invalidTest(Set.of(), sets.minSize(1), "must.have.min.size", HashMap.of("min", 1));
            invalidTest(Set.of("x"), sets.minSize(2), "must.have.min.size", HashMap.of("min", 2));
        }
    }

    @Nested
    class MaxSize {

        @Test
        void valid() {
            validTest(Set.of(), sets.maxSize(0));
            validTest(Set.of("x"), sets.maxSize(1));
            validTest(Set.of("a", "b"), sets.maxSize(2));
        }

        @Test
        void invalid() {
            invalidTest(null, sets.maxSize(0), "must.not.be.null");
            invalidTest(Set.of("x"), sets.maxSize(0), "must.have.max.size", HashMap.of("max", 0));
            invalidTest(Set.of("a", "b", "c"), sets.maxSize(2), "must.have.max.size", HashMap.of("max", 2));
        }
    }

    @Nested
    class SizeEquals {

        @Test
        void valid() {
            validTest(Set.of(), sets.sizeEquals(0));
            validTest(Set.of("x"), sets.sizeEquals(1));
            validTest(Set.of("a", "b"), sets.sizeEquals(2));
        }

        @Test
        void invalid() {
            invalidTest(null, sets.sizeEquals(1), "must.not.be.null");
            invalidTest(Set.of(), sets.sizeEquals(1), "must.have.exact.size", HashMap.of("equal", 1));
            invalidTest(Set.of("x"), sets.sizeEquals(0), "must.have.exact.size", HashMap.of("equal", 0));
        }
    }

    @Nested
    class SizeBetween {

        @Test
        void valid() {
            validTest(Set.of(), sets.sizeBetween(0, 0));
            validTest(Set.of("x"), sets.sizeBetween(0, 1));
            validTest(Set.of("a", "b"), sets.sizeBetween(1, 2));
        }

        @Test
        void invalid() {
            invalidTest(null, sets.sizeBetween(1, 2), "must.not.be.null");
            invalidTest(
                    Set.of(),
                    sets.sizeBetween(1, 2),
                    "must.have.size.between",
                    HashMap.of("min", 1, "max", 2)
            );
            invalidTest(
                    Set.of("a", "b", "c"),
                    sets.sizeBetween(1, 2),
                    "must.have.size.between",
                    HashMap.of("min", 1, "max", 2)
            );
        }
    }

    @Nested
    class NoNullElements {

        @Test
        void valid() {
            Rule<Set<String>> noNulls = sets.noNullElements();
            validTest(Set.of("a", "b", "c"), noNulls);
            validTest(Set.of(), sets.noNullElements());
        }

        @Test
        void invalid() {
            Set<String> withNulls = new java.util.LinkedHashSet<>();
            withNulls.add("a");
            withNulls.add(null);
            withNulls.add("c");

            assertThatValidation(sets.<String>noNullElements().test(withNulls).at("value"))
                    .isInvalid()
                    .hasErrorMessages("value[1].must.not.be.null");
        }
    }

    @Nested
    class AllMatch {

        @Test
        void valid() {
            Rule<Set<Integer>> even = sets.allMatch(n -> n % 2 == 0);
            validTest(Set.of(2, 4, 6), even);
            validTest(Set.of(), sets.allMatch((Predicate<Integer>) (n -> n % 2 == 0)));
            validTest(Set.of(), sets.allMatchRule(ints().even()));
        }

        @Test
        void invalid() {
            invalidTest(null, sets.allMatch((Predicate<Integer>) (n -> n % 2 == 0)), "must.not.be.null");
            invalidTest(new LinkedHashSet<>(List.of(2, 3, 4)), sets.allMatchRule(ints().even()), "must.be.even");

            invalidTest(
                    new LinkedHashSet<>(List.of("a", "bb", "c")),
                    sets.allMatch(s -> s.length() == 1, ErrorMessage.of("len.must.be.one")),
                    "len.must.be.one"
            ).errorMessages().contains("[1].len.must.be.one");

            invalidTest(
                    new LinkedHashSet<>(List.of("a", "bb")),
                    sets.allMatch(s -> s.length() == 1, ErrorMessage.of("len.must.be", "len", 1)),
                    "[1].len.must.be",
                    HashMap.of("len", 1)
            );
        }

        @Test
        void throws_whenPredicateIsNull() {
            assertThatThrownBy(() ->
                    sets.allMatch(null)
            ).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class NoneMatch {

        @Test
        void valid() {
            Rule<Set<Integer>> noEvens = sets.noneMatch(n -> n % 2 == 0);
            validTest(Set.of(1, 3, 5), noEvens);
            validTest(Set.<Integer>of(), sets.noneMatch(n -> n % 2 == 0));
        }

        @Test
        void invalid() {
            invalidTest(null, sets.noneMatch((Predicate<Integer>) (n -> n % 2 == 0)), "must.not.be.null");

            invalidTest(new LinkedHashSet<>(List.of(1, 2, 3)), sets.noneMatch(n -> n % 2 == 0), "must.none.match");


            assertThatValidation(
                    sets.noneMatch((Predicate<String>) s -> s.length() == 2, ErrorMessage.of("len.must.not.be.two")).test(new LinkedHashSet<>(List.of("a", "bb", "c"))).at("value")
            )
                    .isInvalid()
                    .hasErrorMessages("value[1].len.must.not.be.two");

            invalidTest(
                    new LinkedHashSet<>(List.of("a", "bb")),
                    sets.noneMatch(s -> s.length() == 2, ErrorMessage.of("len.must.not.be", "len", 2)),
                    "[1].len.must.not.be",
                    HashMap.of("len", 2)
            );
        }

        @Test
        void throws_whenPredicateIsNull() {
            assertThatThrownBy(() ->
                    sets.noneMatch(null)
            ).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class AnyMatch {

        @Test
        void valid() {
            Rule<Set<Integer>> hasEven = sets.anyMatch(n -> n % 2 == 0);
            validTest(Set.of(1, 2, 3), hasEven);
        }

        @Test
        void invalid() {
            invalidTest(null, sets.anyMatch((Predicate<Integer>) (n -> n % 2 == 0)), "must.not.be.null");
            invalidTest(Set.of(1, 3, 5), sets.anyMatch(n -> n % 2 == 0), "must.at.least.one.match");
            invalidTest(Set.<Integer>of(), sets.anyMatch(n -> n % 2 == 0), "must.at.least.one.match");
            invalidTest(
                    Set.of("a", "bb", "ccc"),
                    sets.anyMatch(s -> s.length() == 4, ErrorMessage.of("len.must.be.four")),
                    "len.must.be.four"
            );
            invalidTest(
                    Set.of("a", "bb"),
                    sets.anyMatch(s -> s.length() == 3, ErrorMessage.of("len.must.be", "len", 3)),
                    "len.must.be",
                    HashMap.of("len", 3)
            );
        }

        @Test
        void throws_whenPredicateIsNull() {
            assertThatThrownBy(() -> sets.anyMatch(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("predicate cannot be null");
        }
    }

    @Nested
    class Contains {

        @Test
        void valid() {
            validTest(Set.of("a", "b", "c"), sets.contains("b"));
        }

        @Test
        void invalid() {
            invalidTest(null, sets.contains("b"), "must.not.be.null");
            invalidTest(
                    Set.of("a", "b", "c"),
                    sets.contains("x"),
                    "must.contain",
                    HashMap.of("element", "x")
            );
        }
    }

    @Nested
    class ContainsAll {

        @Test
        void valid() {
            validTest(Set.of("a", "b", "c"), sets.containsAll(Set.of("a", "c")));
            validTest(Set.of("a", "b"), sets.containsAll(Set.of()));
            validTest(Set.of("a", "b", "c"), sets.containsAll(List.of("a", "a", "c")));
        }

        @Test
        void invalid() {
            invalidTest(null, sets.containsAll(Set.of("a")), "must.not.be.null");
            invalidTest(
                    Set.of("a", "b"),
                    sets.containsAll(Set.of("a", "c")),
                    "must.contain.all",
                    HashMap.of("required", HashSet.of("a", "c"))
            );
        }

        @Test
        void throws_whenRequiredIsNull() {
            assertThatThrownBy(() -> sets.containsAll(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("required cannot be null");
        }
    }

    @Nested
    class ContainsAnyOf {

        @Test
        void valid() {
            validTest(Set.of("a", "b", "c"), sets.containsAnyOf(Set.of("x", "b")));
        }

        @Test
        void invalid() {
            invalidTest(null, sets.containsAnyOf(Set.of("x")), "must.not.be.null");
            invalidTest(
                    Set.of("a", "b", "c"),
                    sets.containsAnyOf(Set.of("x", "y")),
                    "must.contain.any.of",
                    HashMap.of("candidates", HashSet.of("x", "y"))
            );
            invalidTest(
                    Set.of("a", "b"),
                    sets.containsAnyOf(Set.of()),
                    "must.contain.any.of",
                    HashMap.of("candidates", HashSet.empty())
            );
        }

        @Test
        void throws_whenCandidatesIsNull() {
            assertThatThrownBy(() -> sets.containsAnyOf(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("candidates cannot be null");
        }
    }

    @Nested
    class UniqueBy {

        record Person(String email, String name) { }

        @Test
        void valid() {
            Set<Person> people = Set.of(
                    new Person("a@example.com", "Alice"),
                    new Person("b@example.com", "Bob"),
                    new Person("c@example.com", "Carol")
            );

            validTest(people, sets.uniqueBy(Person::email, "email"));
        }

        @Test
        void invalid() {
            invalidTest(null, sets.uniqueBy(Person::email, "email"), "must.not.be.null");
            Set<Person> people = new LinkedHashSet<>();
            people.add(new Person("a@example.com", "Alice"));   // idx 0
            people.add(new Person("b@example.com", "Bob"));     // idx 1
            people.add(new Person("a@example.com", "Alicia"));  // idx 2 (duplicate of idx 0)
            people.add(new Person("b@example.com", "Bobby"));    // idx 3 (duplicate of idx 1)

            invalidTest(
                    people,
                    sets.uniqueBy(Person::email, "email"),
                    "must.be.unique.by.key",
                    HashMap.of(
                            "key", "email",
                            "duplicates", HashMap.of(
                                    "a@example.com", io.vavr.collection.List.of(0, 2),
                                    "b@example.com", io.vavr.collection.List.of(1, 3)
                            )
                    )
            );

            Set<Person> morePeople = new LinkedHashSet<>(List.of(
                    new Person("dup@example.com", "A"), // 0
                    new Person("x@example.com", "X"),   // 1
                    new Person("dup@example.com", "B"), // 2
                    new Person("dup@example.com", "C")  // 3
            ));

            invalidTest(
                    morePeople,
                    sets.uniqueBy(Person::email, "email"),
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
    class ValidateValuesWith {

        @Test
        void valid() {
            Rule<Number> rule = Rule.of(n -> n.doubleValue() > 0, "must.be.positive");
            validTest(Set.of(1, 10, 2), sets.validateValuesWith(rule));
        }

        @Test
        void invalid() {
            invalidTest(null, sets.validateValuesWith(Rule.of(n -> true, "")), "must.not.be.null");
            // Arrange
            Rule<Number> rule = Rule.of(n -> n.doubleValue() > 0, "must.be.positive");
            Rule<Set<Integer>> listRule = sets.validateValuesWith(rule);

            Set<Integer> input = new LinkedHashSet<>(List.of(-1, 10, 0));

            // Act
            var result = listRule.test(input).at("value");

            // Assert: failures are attributed to their indices in the path
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("value[0].must.be.positive", "value[2].must.be.positive");
        }
    }
}