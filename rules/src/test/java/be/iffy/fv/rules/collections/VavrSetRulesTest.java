package be.iffy.fv.rules.collections;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import io.vavr.collection.LinkedHashSet;
import io.vavr.collection.Set;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Predicate;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;
import static be.iffy.fv.rules.collections.VavrSetRules.vavrSets;
import static be.iffy.fv.rules.numbers.IntegerRules.ints;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VavrSetRulesTest {

    @Nested
    class NotEmpty {

        @Test
        void valid() {
            validTest(HashSet.of("x"), vavrSets.notEmpty());
        }

        @Test
        void invalid() {
            invalidTest(HashSet.empty(), vavrSets.notEmpty(), "must.not.be.empty");
        }
    }

    @Nested
    class Empty {

        @Test
        void valid() {
            validTest(HashSet.of(), vavrSets.empty());
            validTest(HashSet.empty(), vavrSets.empty());
        }

        @Test
        void invalid() {
            invalidTest(HashSet.of("x"), vavrSets.empty(), "must.be.empty");
        }
    }

    @Nested
    class MinSize {

        @Test
        void valid() {
            validTest(HashSet.of(), vavrSets.minSize(0));
            validTest(HashSet.of("x"), vavrSets.minSize(1));
            validTest(HashSet.of("a", "b"), vavrSets.minSize(1));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrSets.minSize(1), "must.not.be.null");
            invalidTest(HashSet.of(), vavrSets.minSize(1), "must.have.min.size", HashMap.of("min", 1));
            invalidTest(HashSet.of("x"), vavrSets.minSize(2), "must.have.min.size", HashMap.of("min", 2));
        }
    }

    @Nested
    class MaxSize {

        @Test
        void valid() {
            validTest(HashSet.of(), vavrSets.maxSize(0));
            validTest(HashSet.of("x"), vavrSets.maxSize(1));
            validTest(HashSet.of("a", "b"), vavrSets.maxSize(2));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrSets.maxSize(0), "must.not.be.null");
            invalidTest(HashSet.of("x"), vavrSets.maxSize(0), "must.have.max.size", HashMap.of("max", 0));
            invalidTest(HashSet.of("a", "b", "c"), vavrSets.maxSize(2), "must.have.max.size", HashMap.of("max", 2));
        }
    }

    @Nested
    class SizeEquals {

        @Test
        void valid() {
            validTest(HashSet.of(), vavrSets.sizeEquals(0));
            validTest(HashSet.of("x"), vavrSets.sizeEquals(1));
            validTest(HashSet.of("a", "b"), vavrSets.sizeEquals(2));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrSets.sizeEquals(1), "must.not.be.null");
            invalidTest(HashSet.of(), vavrSets.sizeEquals(1), "must.have.exact.size", HashMap.of("equal", 1));
            invalidTest(HashSet.of("x"), vavrSets.sizeEquals(0), "must.have.exact.size", HashMap.of("equal", 0));
        }
    }

    @Nested
    class SizeBetween {

        @Test
        void valid() {
            validTest(HashSet.of(), vavrSets.sizeBetween(0, 0));
            validTest(HashSet.of("x"), vavrSets.sizeBetween(0, 1));
            validTest(HashSet.of("a", "b"), vavrSets.sizeBetween(1, 2));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrSets.sizeBetween(1, 2), "must.not.be.null");
            invalidTest(
                    HashSet.of(),
                    vavrSets.sizeBetween(1, 2),
                    "must.have.size.between",
                    HashMap.of("min", 1, "max", 2)
            );
            invalidTest(
                    HashSet.of("a", "b", "c"),
                    vavrSets.sizeBetween(1, 2),
                    "must.have.size.between",
                    HashMap.of("min", 1, "max", 2)
            );
        }
    }

    @Nested
    class NoNullElements {

        @Test
        void valid() {
            Rule<Set<String>> noNulls = vavrSets.noNullElements();
            validTest(HashSet.of("a", "b", "c"), noNulls);
            validTest(HashSet.of(), vavrSets.noNullElements());
        }

        @Test
        void invalid() {
            Set<String> withNulls = LinkedHashSet.of("a", null, "c");

            assertThatValidation(vavrSets.<String>noNullElements().test(withNulls).at("value"))
                    .isInvalid()
                    .hasErrorMessages("value[1].must.not.be.null");
        }
    }

    @Nested
    class AllMatch {

        @Test
        void valid() {
            Rule<Set<Integer>> even = vavrSets.allMatch(n -> n % 2 == 0);
            validTest(HashSet.of(2, 4, 6), even);
            validTest(HashSet.of(), vavrSets.allMatch((Predicate<Integer>) (n -> n % 2 == 0)));
            validTest(HashSet.of(), vavrSets.allMatchRule(ints().even()));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrSets.allMatch((Predicate<Integer>) (n -> n % 2 == 0)), "must.not.be.null");
            invalidTest(LinkedHashSet.of(2, 3, 4), vavrSets.allMatchRule(ints().even()), "must.be.even");

            invalidTest(
                    LinkedHashSet.of("a", "bb", "c"),
                    vavrSets.allMatch(s -> s.length() == 1, ErrorMessage.of("len.must.be.one")),
                    "len.must.be.one"
            ).errorMessages().contains("[1].len.must.be.one");

            invalidTest(
                    LinkedHashSet.of("a", "bb"),
                    vavrSets.allMatch(s -> s.length() == 1, ErrorMessage.of("len.must.be", "len", 1)),
                    "[1].len.must.be",
                    HashMap.of("len", 1)
            );
        }

        @Test
        void throws_whenPredicateIsNull() {
            assertThatThrownBy(() ->
                    vavrSets.allMatch(null)
            ).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class NoneMatch {

        @Test
        void valid() {
            Rule<Set<Integer>> noEvens = vavrSets.noneMatch(n -> n % 2 == 0);
            validTest(HashSet.of(1, 3, 5), noEvens);
            validTest(HashSet.<Integer>of(), vavrSets.noneMatch(n -> n % 2 == 0));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrSets.noneMatch((Predicate<Integer>) (n -> n % 2 == 0)), "must.not.be.null");

            invalidTest(LinkedHashSet.of(1, 2, 3), vavrSets.noneMatch(n -> n % 2 == 0), "must.none.match");


            assertThatValidation(
                    vavrSets.noneMatch((Predicate<String>) s -> s.length() == 2, ErrorMessage.of("len.must.not.be.two")).test(LinkedHashSet.of("a", "bb", "c")).at("value")
            )
                    .isInvalid()
                    .hasErrorMessages("value[1].len.must.not.be.two");

            invalidTest(
                    LinkedHashSet.of("a", "bb"),
                    vavrSets.noneMatch(s -> s.length() == 2, ErrorMessage.of("len.must.not.be", "len", 2)),
                    "[1].len.must.not.be",
                    HashMap.of("len", 2)
            );
        }

        @Test
        void throws_whenPredicateIsNull() {
            assertThatThrownBy(() ->
                    vavrSets.noneMatch(null)
            ).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class AnyMatch {

        @Test
        void valid() {
            Rule<Set<Integer>> hasEven = vavrSets.anyMatch(n -> n % 2 == 0);
            validTest(HashSet.of(1, 2, 3), hasEven);
        }

        @Test
        void invalid() {
            invalidTest(null, vavrSets.anyMatch((Predicate<Integer>) (n -> n % 2 == 0)), "must.not.be.null");
            invalidTest(HashSet.of(1, 3, 5), vavrSets.anyMatch(n -> n % 2 == 0), "must.at.least.one.match");
            invalidTest(HashSet.<Integer>of(), vavrSets.anyMatch(n -> n % 2 == 0), "must.at.least.one.match");
            invalidTest(
                    HashSet.of("a", "bb", "ccc"),
                    vavrSets.anyMatch(s -> s.length() == 4, ErrorMessage.of("len.must.be.four")),
                    "len.must.be.four"
            );
            invalidTest(
                    HashSet.of("a", "bb"),
                    vavrSets.anyMatch(s -> s.length() == 3, ErrorMessage.of("len.must.be", "len", 3)),
                    "len.must.be",
                    HashMap.of("len", 3)
            );
        }

        @Test
        void throws_whenPredicateIsNull() {
            assertThatThrownBy(() -> vavrSets.anyMatch(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("predicate cannot be null");
        }
    }

    @Nested
    class Contains {

        @Test
        void valid() {
            validTest(HashSet.of("a", "b", "c"), vavrSets.contains("b"));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrSets.contains("b"), "must.not.be.null");
            invalidTest(
                    HashSet.of("a", "b", "c"),
                    vavrSets.contains("x"),
                    "must.contain",
                    HashMap.of("element", "x")
            );
        }
    }

    @Nested
    class ContainsAll {

        @Test
        void valid() {
            validTest(HashSet.of("a", "b", "c"), vavrSets.containsAll(HashSet.of("a", "c")));
            validTest(HashSet.of("a", "b"), vavrSets.containsAll(HashSet.of()));
            validTest(HashSet.of("a", "b", "c"), vavrSets.containsAll(List.of("a", "a", "c")));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrSets.containsAll(HashSet.of("a")), "must.not.be.null");
            invalidTest(
                    HashSet.of("a", "b"),
                    vavrSets.containsAll(HashSet.of("a", "c")),
                    "must.contain.all",
                    HashMap.of("required", HashSet.of("a", "c"))
            );
        }

        @Test
        void throws_whenRequiredIsNull() {
            assertThatThrownBy(() -> vavrSets.containsAll(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("required cannot be null");
        }
    }

    @Nested
    class ContainsAnyOf {

        @Test
        void valid() {
            validTest(HashSet.of("a", "b", "c"), vavrSets.containsAnyOf(HashSet.of("x", "b")));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrSets.containsAnyOf(HashSet.of("x")), "must.not.be.null");
            invalidTest(
                    HashSet.of("a", "b", "c"),
                    vavrSets.containsAnyOf(HashSet.of("x", "y")),
                    "must.contain.any.of",
                    HashMap.of("candidates", HashSet.of("x", "y"))
            );
            invalidTest(
                    HashSet.of("a", "b"),
                    vavrSets.containsAnyOf(HashSet.of()),
                    "must.contain.any.of",
                    HashMap.of("candidates", HashSet.empty())
            );
        }

        @Test
        void throws_whenCandidatesIsNull() {
            assertThatThrownBy(() -> vavrSets.containsAnyOf(null))
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
            Set<Person> people = HashSet.of(
                    new Person("a@example.com", "Alice"),
                    new Person("b@example.com", "Bob"),
                    new Person("c@example.com", "Carol")
            );

            validTest(people, vavrSets.uniqueBy(Person::email, "email"));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrSets.uniqueBy(Person::email, "email"), "must.not.be.null");
            Set<Person> people = LinkedHashSet.of(
                    new Person("a@example.com", "Alice"),   // idx 0
                    new Person("b@example.com", "Bob"),     // idx 1
                    new Person("a@example.com", "Alicia"),  // idx 2 (duplicate of idx 0)
                    new Person("b@example.com", "Bobby") // idx 3 (duplicate of idx 1)
            );

            invalidTest(
                    people,
                    vavrSets.uniqueBy(Person::email, "email"),
                    "must.be.unique.by.key",
                    HashMap.of(
                            "key", "email",
                            "duplicates", HashMap.of(
                                    "a@example.com", io.vavr.collection.List.of(0, 2),
                                    "b@example.com", io.vavr.collection.List.of(1, 3)
                            )
                    )
            );

            Set<Person> morePeople = LinkedHashSet.of(
                    new Person("dup@example.com", "A"), // 0
                    new Person("x@example.com", "X"),   // 1
                    new Person("dup@example.com", "B"), // 2
                    new Person("dup@example.com", "C")  // 3
            );

            invalidTest(
                    morePeople,
                    vavrSets.uniqueBy(Person::email, "email"),
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
            validTest(HashSet.of(1, 10, 2), vavrSets.validateValuesWith(rule));
        }

        @Test
        void invalid() {
            invalidTest(null, vavrSets.validateValuesWith(Rule.of(n -> true, "")), "must.not.be.null");
            // Arrange
            Rule<Number> rule = Rule.of(n -> n.doubleValue() > 0, "must.be.positive");
            Rule<Set<Integer>> listRule = vavrSets.validateValuesWith(rule);

            Set<Integer> input = LinkedHashSet.of(-1, 10, 0);

            // Act
            var result = listRule.test(input).at("value");

            // Assert: failures are attributed to their indices in the path
            assertThatValidation(result)
                    .isInvalid()
                    .hasErrorMessages("value[0].must.be.positive", "value[2].must.be.positive");
        }
    }
}