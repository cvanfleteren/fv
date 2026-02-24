package net.vanfleteren.fv.rules;

import io.vavr.collection.HashSet;
import io.vavr.collection.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static net.vanfleteren.fv.API.validateThat;
import static net.vanfleteren.fv.assertj.ValidationAssert.assertThatValidation;
import static net.vanfleteren.fv.rules.RulesTest.invalidTest;
import static net.vanfleteren.fv.rules.RulesTest.validTest;

import static net.vanfleteren.fv.rules.CollectionRules.*;

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
            validTest(java.util.List.of(), minSize(0));
            validTest(java.util.List.of("x"), minSize(1));
            validTest(java.util.List.of("a", "b"), minSize(1));
        }

        @Test
        void invalid() {
            invalidTest(java.util.List.of(), minSize(1), "min.size", io.vavr.collection.HashMap.of("min", 1));
            invalidTest(java.util.List.of("x"), minSize(2), "min.size", io.vavr.collection.HashMap.of("min", 2));
        }
    }

    @Nested
    class MaxSize {

        @Test
        void valid() {
            validTest(java.util.List.of(), maxSize(0));
            validTest(java.util.List.of("x"), maxSize(1));
            validTest(java.util.List.of("a", "b"), maxSize(2));
        }

        @Test
        void invalid() {
            invalidTest(java.util.List.of("x"), maxSize(0), "max.size", io.vavr.collection.HashMap.of("max", 0));
            invalidTest(java.util.List.of("a", "b", "c"), maxSize(2), "max.size", io.vavr.collection.HashMap.of("max", 2));
        }
    }

    @Nested
    class SizeEquals {

        @Test
        void valid() {
            validTest(java.util.List.of(), sizeEquals(0));
            validTest(java.util.List.of("x"), sizeEquals(1));
            validTest(java.util.List.of("a", "b"), sizeEquals(2));
        }

        @Test
        void invalid() {
            invalidTest(java.util.List.of(), sizeEquals(1), "equal.size", io.vavr.collection.HashMap.of("equal", 1));
            invalidTest(java.util.List.of("x"), sizeEquals(0), "equal.size", io.vavr.collection.HashMap.of("equal", 0));
        }
    }

    @Nested
    class SizeBetween {

        @Test
        void valid() {
            validTest(java.util.List.of(), sizeBetween(0, 0));
            validTest(java.util.List.of("x"), sizeBetween(0, 1));
            validTest(java.util.List.of("a", "b"), sizeBetween(1, 2));
        }

        @Test
        void invalid() {
            invalidTest(
                    java.util.List.of(),
                    sizeBetween(1, 2),
                    "equal.size",
                    io.vavr.collection.HashMap.of("min", 1, "max", 2)
            );
            invalidTest(
                    java.util.List.of("a", "b", "c"),
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
            validTest(List.of("a", "b", "c"), noNullElements());
            validTest(List.empty(), noNullElements());
        }

        @Test
        void invalid() {
            assertThatValidation(validateThat(List.of("a", null, "c"), "value").is(noNullElements()))
                    .isInvalid()
                    .hasErrorMessages("value[1].cannot.be.null");
        }
    }
}