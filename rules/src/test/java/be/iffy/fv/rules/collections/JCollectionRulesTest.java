package be.iffy.fv.rules.collections;

import io.vavr.collection.HashMap;
import io.vavr.collection.HashSet;
import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static be.iffy.fv.dsl.DSL.validateThat;
import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static be.iffy.fv.rules.collections.CollectionRules.collections;
import static be.iffy.fv.rules.collections.JCollectionRules.*;
import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;
import static be.iffy.fv.rules.numbers.IntegerRules.ints;
import static be.iffy.fv.rules.text.StringRules.strings;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JCollectionRulesTest {

    @Nested
    class NotEmpty {

        @Test
        void valid() {
            validTest(List.of("x"), jCollections.notEmpty());
            validTest(java.util.Set.of(1), jCollections.notEmpty());
        }

        @Test
        void invalid() {
            invalidTest(new ArrayList<>(), jCollections.notEmpty(), "must.not.be.empty");
            invalidTest(java.util.Set.of(), jCollections.notEmpty(), "must.not.be.empty");
        }
    }

    @Nested
    class Empty {

        @Test
        void valid() {
            validTest(List.of(), jCollections.empty());
            validTest(new ArrayList<>(), jCollections.empty());
        }

        @Test
        void invalid() {
            invalidTest(List.of("x"), jCollections.empty(), "must.be.empty");
            invalidTest(java.util.Set.of(1), jCollections.empty(), "must.be.empty");
        }
    }

    @Nested
    class MinSize {

        @Test
        void valid() {
            validTest(List.of(), jCollections.minSize(0));
            validTest(List.of("x"), jCollections.minSize(1));
            validTest(List.of("a", "b"), jCollections.minSize(1));
        }

        @Test
        void invalid() {
            invalidTest(null, jCollections.minSize(1), "must.not.be.null");
            invalidTest(List.of(), jCollections.minSize(1), "must.have.min.size", HashMap.of("min", 1));
            invalidTest(List.of("x"), jCollections.minSize(2), "must.have.min.size", HashMap.of("min", 2));
        }
    }

    @Nested
    class MaxSize {

        @Test
        void valid() {
            validTest(List.of(), jCollections.maxSize(0));
            validTest(List.of("x"), jCollections.maxSize(1));
            validTest(List.of("a", "b"), jCollections.maxSize(2));
        }

        @Test
        void invalid() {
            invalidTest(null, jCollections.maxSize(0), "must.not.be.null");
            invalidTest(List.of("x"), jCollections.maxSize(0), "must.have.max.size", HashMap.of("max", 0));
            invalidTest(List.of("a", "b", "c"), jCollections.maxSize(2), "must.have.max.size", HashMap.of("max", 2));
        }
    }

    @Nested
    class SizeEquals {

        @Test
        void valid() {
            validTest(List.of(), jCollections.sizeEquals(0));
            validTest(List.of("x"), jCollections.sizeEquals(1));
            validTest(List.of("a", "b"), jCollections.sizeEquals(2));
        }

        @Test
        void invalid() {
            invalidTest(null, jCollections.sizeEquals(1), "must.not.be.null");
            invalidTest(List.of(), jCollections.sizeEquals(1), "must.have.exact.size", HashMap.of("equal", 1));
            invalidTest(List.of("x"), jCollections.sizeEquals(0), "must.have.exact.size", HashMap.of("equal", 0));
        }
    }

    @Nested
    class SizeBetween {

        @Test
        void valid() {
            validTest(List.of(), jCollections.sizeBetween(0, 0));
            validTest(List.of("x"), jCollections.sizeBetween(0, 1));
            validTest(List.of("a", "b"), jCollections.sizeBetween(1, 2));
        }

        @Test
        void invalid() {
            invalidTest(null, jCollections.sizeBetween(1, 2), "must.not.be.null");
            invalidTest(
                    List.of(),
                    jCollections.sizeBetween(1, 2),
                    "must.have.size.between",
                    HashMap.of("min", 1, "max", 2)
            );
            invalidTest(
                    List.of("a", "b", "c"),
                    jCollections.sizeBetween(1, 2),
                    "must.have.size.between",
                    HashMap.of("min", 1, "max", 2)
            );
        }
    }

    @Nested
    class NoNullElements {

        @Test
        void valid() {
            Rule<List<String>> noNulls = jCollections.noNullElements();
            validTest(List.of("a", "b", "c"), noNulls);
            validTest(List.of(), jCollections.noNullElements());
        }

        @Test
        void invalid() {
            assertThatValidation(validateThat(Arrays.asList("a", null, "c"), "value").is(jCollections.noNullElements()))
                    .isInvalid()
                    .hasErrorMessages("value[1].must.not.be.null");
        }
    }

    @Nested
    class AllMatch {

        @Test
        void valid() {
            Rule<List<Integer>> even = jCollections.allMatch((Predicate<Integer>) (n -> n % 2 == 0));
            validTest(List.of(2, 4, 6), even);
            validTest(List.of(), jCollections.allMatch((Predicate<Integer>) (n -> n % 2 == 0)));
            validTest(List.of(), jCollections.allMatchRule(ints().even()));
        }

        @Test
        void invalid() {
            invalidTest(null, jCollections.allMatch((Predicate<Integer>) (n -> n % 2 == 0)), "must.not.be.null");
            invalidTest(List.of(2, 3, 4), jCollections.allMatchRule(ints().even()), "must.be.even");

            invalidTest(
                    List.of("a", "bb", "c"),
                    jCollections.allMatch((Predicate<String>) (s -> s.length() == 1), ErrorMessage.of("len.must.be.one")),
                    "len.must.be.one"
            ).hasErrorMessages("value[1].len.must.be.one");

            invalidTest(
                    List.of("a", "bb"),
                    jCollections.allMatch((Predicate<String>) (s -> s.length() == 1), ErrorMessage.of("len.must.be", "len", 1)),
                    "len.must.be",
                    HashMap.of("len", 1)
            );
        }

        @Test
        void throws_whenPredicateIsNull_andRuleIsEvaluated() {
            assertThatThrownBy(() ->
                    validateThat(List.of(1), "value").is(jCollections.allMatch(null)).getOrElseThrow()
            ).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class NoneMatch {

        @Test
        void valid() {
            Rule<List<Integer>> noEvens = jCollections.noneMatch(n -> n % 2 == 0);
            validTest(List.of(1, 3, 5), noEvens);
            validTest(List.<Integer>of(), jCollections.noneMatch(n -> n % 2 == 0));
        }

        @Test
        void invalid() {
            invalidTest(null, jCollections.noneMatch((Predicate<Integer>) (n -> n % 2 == 0)), "must.not.be.null");
            invalidTest(List.of(1, 2, 3), jCollections.noneMatch(n -> n % 2 == 0), "must.none.match");
            assertThatValidation(
                    validateThat(List.of("a", "bb", "c"), "value")
                            .is(jCollections.noneMatch(s -> s.length() == 2, ErrorMessage.of("len.must.not.be.two")))
            )
                    .isInvalid()
                    .hasErrorMessages("value[1].len.must.not.be.two");
            invalidTest(
                    List.of("a", "bb"),
                    jCollections.noneMatch(s -> s.length() == 2, ErrorMessage.of("len.must.not.be", "len", 2)),
                    "len.must.not.be",
                    HashMap.of("len", 2)
            );
        }

        @Test
        void throws_whenPredicateIsNull_andRuleIsEvaluated() {
            assertThatThrownBy(() -> {
                Rule<List<Integer>> rule = jCollections.noneMatch(null);
                validateThat(List.of(1), "value").is(rule).getOrElseThrow();
            }).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    class AnyMatch {

        @Test
        void valid() {
            Rule<List<Integer>> hasEven = jCollections.anyMatch(n -> n % 2 == 0);
            validTest(List.of(1, 2, 3), hasEven);
        }

        @Test
        void invalid() {
            invalidTest(null, jCollections.anyMatch((Predicate<Integer>) (n -> n % 2 == 0)), "must.not.be.null");
            invalidTest(List.of(1, 3, 5), jCollections.anyMatch(n -> n % 2 == 0), "must.at.least.one.match");
            invalidTest(List.<Integer>of(), jCollections.anyMatch(n -> n % 2 == 0), "must.at.least.one.match");
            invalidTest(
                    List.of("a", "bb", "ccc"),
                    jCollections.anyMatch(s -> s.length() == 4, ErrorMessage.of("len.must.be.four")),
                    "len.must.be.four"
            );
            invalidTest(
                    List.of("a", "bb"),
                    jCollections.anyMatch(s -> s.length() == 3, ErrorMessage.of("len.must.be", "len", 3)),
                    "len.must.be",
                    HashMap.of("len", 3)
            );
        }

        @Test
        void throws_whenPredicateIsNull() {
            assertThatThrownBy(() -> jCollections.anyMatch(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("predicate cannot be null");
        }
    }

    @Nested
    class Contains {

        @Test
        void valid() {
            validTest(List.of("a", "b", "c"), jCollections.contains("b"));
        }

        @Test
        void invalid() {
            invalidTest(null, jCollections.contains("b"), "must.not.be.null");
            invalidTest(
                    List.of("a", "b", "c"),
                    jCollections.contains("x"),
                    "must.contain",
                    HashMap.of("element", "x")
            );
        }
    }

    @Nested
    class ContainsAll {

        @Test
        void valid() {
            validTest(List.of("a", "b", "c"), jCollections.containsAll(List.of("a", "c")));
            validTest(List.of("a", "b"), jCollections.containsAll(List.of()));
            validTest(List.of("a", "b", "c"), jCollections.containsAll(List.of("a", "a", "c")));
        }

        @Test
        void invalid() {
            invalidTest(null, jCollections.containsAll(List.of("a")), "must.not.be.null");
            invalidTest(
                    List.of("a", "b"),
                    jCollections.containsAll(List.of("a", "c")),
                    "must.contain.all",
                    HashMap.of("required", HashSet.of("a", "c"))
            );
        }

        @Test
        void throws_whenRequiredIsNull() {
            assertThatThrownBy(() -> jCollections.containsAll(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("required cannot be null");
        }
    }

    @Nested
    class ContainsAnyOf {

        @Test
        void valid() {
            validTest(List.of("a", "b", "c"), jCollections.containsAnyOf(List.of("x", "b")));
        }

        @Test
        void invalid() {
            invalidTest(null, jCollections.containsAnyOf(List.of("x")), "must.not.be.null");
            invalidTest(
                    List.of("a", "b", "c"),
                    jCollections.containsAnyOf(List.of("x", "y")),
                    "must.contain.any.of",
                    HashMap.of("candidates", HashSet.of("x", "y"))
            );
            invalidTest(
                    List.of("a", "b"),
                    jCollections.containsAnyOf(List.of()),
                    "must.contain.any.of",
                    HashMap.of("candidates", HashSet.empty())
            );
        }

        @Test
        void throws_whenCandidatesIsNull() {
            assertThatThrownBy(() -> jCollections.containsAnyOf(null))
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

            validTest(people, jCollections.uniqueBy(Person::email, "email"));
        }

        @Test
        void invalid() {
            invalidTest(null, jCollections.uniqueBy(Person::email, "email"), "must.not.be.null");
            List<Person> people = List.of(
                    new Person("a@example.com", "Alice"),   // idx 0
                    new Person("b@example.com", "Bob"),     // idx 1
                    new Person("a@example.com", "Alicia"),  // idx 2 (duplicate of idx 0)
                    new Person("b@example.com", "Bobby")    // idx 3 (duplicate of idx 1)
            );

            invalidTest(
                    people,
                    jCollections.uniqueBy(Person::email, "email"),
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
                    jCollections.uniqueBy(Person::email, "email"),
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
            validTest(List.of(1, 10, 2), jCollections.validateValuesWith(rule));
        }

        @Test
        void invalid() {
            invalidTest(null, jCollections.validateValuesWith(Rule.of(n -> true, "")), "must.not.be.null");
            // Arrange
            Rule<Number> rule = Rule.of(n -> n.doubleValue() > 0, "must.be.positive");
            Rule<List<Integer>> listRule = jCollections.validateValuesWith(rule);

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
