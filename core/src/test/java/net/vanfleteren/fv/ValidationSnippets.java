package net.vanfleteren.fv;

import io.vavr.collection.List;
import org.junit.jupiter.api.Test;

public class ValidationSnippets {

    void getOrElseThrow_success() {
        // @start region="getOrElseThrow_success"
        Validation<String> v = Validation.valid("Hello");
        String value = v.getOrElseThrow(); // returns "Hello"
        // @end
    }

    void getOrElseThrow_failure() {
        // @start region="getOrElseThrow_failure"
        Validation<String> v = Validation.invalid("error.message");
        try {
            v.getOrElseThrow(); // throws ValidationException
        } catch (ValidationException e) {
            // handle exception
        }
        // @end
    }

    void orElse_value() {
        // @start region="orElse_value"
        Validation<String> v1 = Validation.invalid("error");
        Validation<String> v2 = Validation.valid("Alternative");
        Validation<String> result = v1.orElse(v2); // returns v2
        // @end
    }

    void orElse_supplier() {
        // @start region="orElse_supplier"
        Validation<String> v1 = Validation.invalid("error");
        Validation<String> result = v1.orElse(() -> Validation.valid("Alternative")); // returns v2
        // @end
    }

    void whenValid() {
        // @start region="whenValid"
        Validation<String> v = Validation.valid("Hello");
        v.whenValid(System.out::println); // prints "Hello"
        // @end
    }

    void whenInvalid() {
        // @start region="whenInvalid"
        Validation<String> v = Validation.invalid("error.key");
        v.whenInvalid(errors -> {
            // handle errors
        });
        // @end
    }

    void map() {
        // @start region="map"
        Validation<String> v = Validation.valid("123");
        Validation<Integer> result = v.map(Integer::parseInt); // returns Valid(123)

        Validation<String> errorV = Validation.invalid("error");
        Validation<Integer> errorResult = errorV.map(Integer::parseInt); // returns Invalid("error")
        // @end
    }

    void mapCatching_success() {
        // @start region="mapCatching_success"
        Validation<String> v = Validation.valid("42");
        Validation<Integer> result = v.mapCatching(Integer::parseInt); // returns Valid(42)
        // @end
    }

    void mapCatching_runtimeException() {
        // @start region="mapCatching_runtimeException"
        Validation<String> v = Validation.valid("NaN");
        Validation<Integer> result = v.mapCatching(Integer::parseInt); // returns Invalid("could.not.be.mapped")
        // @end
    }

    void mapCatching_customError() {
        // @start region="mapCatching_customError"
        Validation<String> v = Validation.valid("NaN");
        Validation<Integer> result = v.mapCatching(Integer::parseInt, "not.a.number"); // returns Invalid("not.a.number")
        // @end
    }

    void flatMap() {
        // @start region="flatMap"
        Validation<String> v = Validation.valid("42");
        Validation<Integer> result = v.flatMap(s -> Validation.valid(Integer.parseInt(s))); // returns Valid(42)

        Validation<String> errorV = Validation.invalid("error");
        Validation<Integer> errorResult = errorV.flatMap(s -> Validation.valid(Integer.parseInt(s))); // returns Invalid("error")
        // @end
    }

    void flatMapCatching_success() {
        // @start region="flatMapCatching_success"
        Validation<String> v = Validation.valid("42");
        Validation<Integer> result = v.flatMapCatching(s -> Validation.valid(Integer.parseInt(s))); // returns Valid(42)
        // @end
    }

    void flatMapCatching_runtimeException() {
        // @start region="flatMapCatching_runtimeException"
        Validation<String> v = Validation.valid("NaN");
        Validation<Integer> result = v.flatMapCatching(s -> Validation.valid(Integer.parseInt(s))); // returns Invalid("could.not.be.mapped")
        // @end
    }

    void flatMapCatching_customError() {
        // @start region="flatMapCatching_customError"
        Validation<String> v = Validation.valid("NaN");
        Validation<Integer> result = v.flatMapCatching(s -> Validation.valid(Integer.parseInt(s)), "not.a.number"); // returns Invalid("not.a.number")
        // @end
    }

    void fold() {
        // @start region="fold"
        Validation<String> v = Validation.valid("123");
        String result = v.fold(
                errors -> "Invalid: " + errors.head().message(),
                value -> "Valid: " + value
        ); // returns "Valid: 123"
        // @end
    }

    void sequence_seq() {
        // @start region="sequence_seq"
        io.vavr.collection.List<Validation<Integer>> list = io.vavr.collection.List.of(
            Validation.valid(1),
            Validation.valid(2),
            Validation.valid(3)
        );
        Validation<io.vavr.collection.List<Integer>> result = Validation.sequence(list); // returns Valid(List(1, 2, 3))

        io.vavr.collection.List<Validation<Integer>> mixed = io.vavr.collection.List.of(
            Validation.valid(1),
            Validation.invalid("error.1"),
            Validation.invalid("error.2")
        );
        Validation<io.vavr.collection.List<Integer>> mixedResult = Validation.sequence(mixed); // returns Invalid("error.1", "error.2")
        List<String> errors = mixedResult.errors().map(ErrorMessage::message); // List([1].error.1, [2].error.2)
        // @end
    }

    void sequence_collection() {
        // @start region="sequence_collection"
        java.util.List<Validation<Integer>> list = java.util.List.of(
            Validation.valid(1),
            Validation.valid(2)
        );
        Validation<java.util.List<Integer>> result = Validation.sequence(list); // returns Valid(java.util.List.of(1, 2))
        // @end
    }

    void getOrElse() {
        // @start region="getOrElse"
        Validation<String> v = Validation.valid("Hello");
        String value = v.getOrElse("Fallback"); // returns "Hello"

        Validation<String> errorV = Validation.invalid("error");
        String fallbackValue = errorV.getOrElse("Fallback"); // returns "Fallback"
        // @end
    }

}
