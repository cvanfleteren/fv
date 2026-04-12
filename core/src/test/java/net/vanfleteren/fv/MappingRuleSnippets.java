package net.vanfleteren.fv;

import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.HashMap;
import io.vavr.control.Option;
import io.vavr.control.Try;
import java.util.Optional;
import java.util.function.Function;
import net.vanfleteren.fv.Validation.Valid;
import net.vanfleteren.fv.Validation.Invalid;

public class MappingRuleSnippets {

    void testExample() {
        // @start region="test-example"
        // 1. A rule that transforms a String into an Integer
        // If parsing fails, it returns an Invalid validation with the specified error key
        MappingRule<String, Integer> parseInt = MappingRule.ofTry(
            s -> Try.of(() -> Integer.parseInt(s)),
            "not.a.number"
        );

        // 2. Successful transformation: String "123" -> Integer 123
        Validation<Integer> success = parseInt.test("123");
        // Returns Valid(123)

        // 3. Failed transformation: String "abc" -> Invalid
        Validation<Integer> failure = parseInt.test("abc");
        // Returns Invalid(ErrorMessage("not.a.number"))
        // @end
    }

    void ofStringExample() {
        // @start region="of-string-example"
        // 1. A mapper that might throw an exception (e.g., parsing an integer)
        Function<String, Integer> parser = Integer::parseInt;

        // 2. Create a rule that catches exceptions and uses a specific error message
        MappingRule<String, Integer> rule = MappingRule.of(parser, "invalid.number");

        // 3. Usage
        rule.test("123");  // Returns Valid(123)
        rule.test("abc");  // Returns Invalid(ErrorMessage("invalid.number"))
        // @end
    }

    void ofTryErrorExample() {
        // @start region="of-try-error-example"
        // 1. A mapper that returns a Try (e.g., parsing an integer which might throw)
        Function<String, Try<Integer>> parser = s -> Try.of(() -> Integer.parseInt(s));

        // 2. Create a rule that handles the Try and uses a specific error key
        MappingRule<String, Integer> rule = MappingRule.ofTry(parser, ErrorMessage.of("invalid.number"));

        // 3. Usage
        rule.test("123");  // Returns Valid(123)
        rule.test("abc");  // Returns Invalid(ErrorMessage("invalid.number"))
        // @end
    }

    void ofTryStringExample() {
        // @start region="of-try-string-example"
        // 1. A mapper that returns a Try (e.g., parsing an integer which might throw)
        Function<String, Try<Integer>> parser = s -> Try.of(() -> Integer.parseInt(s));

        // 2. Create a rule that handles the Try and uses a specific error key
        MappingRule<String, Integer> rule = MappingRule.ofTry(parser, "invalid.number");

        // 3. Usage
        rule.test("123");  // Returns Valid(123)
        rule.test("abc");  // Returns Invalid(ErrorMessage("invalid.number"))
        // @end
    }

    void ofErrorExample() {
        // @start region="of-error-example"
        // 1. A mapper that might throw an exception (e.g., parsing an integer)
        Function<String, Integer> parser = Integer::parseInt;

        // 2. Create a rule that catches exceptions and uses a specific error message
        ErrorMessage error = ErrorMessage.of("invalid.number");
        MappingRule<String, Integer> rule = MappingRule.of(parser, error);

        // 3. Usage
        rule.test("123");  // Returns Valid(123)
        rule.test("abc");  // Returns Invalid(ErrorMessage("invalid.number"))
        // @end
    }

    void andThenExample() {
        // @start region="and-then-example"
        // 1. A rule that parses a String to an Integer
        MappingRule<String, Integer> parseInt = s -> {
            try {
                return Validation.valid(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                return Validation.invalid("not.a.number");
            }
        };

        // 2. A rule that validates if an Integer is positive
        MappingRule<Integer, Integer> isPositive = i ->
            i > 0 ? Validation.valid(i) : Validation.invalid("not.positive");

        // 3. Chain them: Parse the string, then check if the resulting number is positive
        MappingRule<String, Integer> parseAndCheckPositive = parseInt.andThen(isPositive);

        // 4. Usage
        Validation<Integer> valid = parseAndCheckPositive.test("10");  // Returns Valid(10)
        Validation<Integer> notPositive = parseAndCheckPositive.test("-5");  // Returns Invalid("not.positive")
        Validation<Integer> notANumber = parseAndCheckPositive.test("abc"); // Returns Invalid("not.a.number")
        // @end
    }

    void recoverWithExample() {
        // @start region="recover-with-example"
        // 1. A rule that maps the string "A" to 1
        MappingRule<String, Integer> ruleA = s ->
            "A".equals(s) ? Validation.valid(1) : Validation.invalid("not.A");

        // 2. A rule that maps the string "B" to 2
        MappingRule<String, Integer> ruleB = s ->
            "B".equals(s) ? Validation.valid(2) : Validation.invalid("not.B");

        // 3. Use recoverWith to try ruleA, and fall back to ruleB if ruleA fails
        MappingRule<String, Integer> combined = ruleA.recoverWith(ruleB);

        // 4. Usage
        Validation<Integer> validA = combined.test("A"); // Returns Valid(1)
        Validation<Integer> validB = combined.test("B"); // Returns Valid(2)
        Validation<Integer> invalid = combined.test("C"); // Returns Invalid("not.B")
        // @end
    }

    void liftToListExample() {
        // @start region="lift-to-list-example"
        // 1. Define a mapping rule
        MappingRule<String, Integer> toInt =  MappingRule.of(s -> Integer.parseInt(s), "not.a.number");

        // 2. Lift it to apply to a list
        MappingRule<List<String>, List<Integer>> listRule = toInt.liftToList();

        // 3. Usage
        listRule.test(List.of("1", "2")); // Returns Valid(List(1, 2))
        listRule.test(List.of("1", "a")); // Returns Invalid(ErrorMessage("not.a.number").atIndex(1))
        // @end
    }

    void liftToOptionExample() {
        // @start region="lift-to-option-example"
        MappingRule<String, Integer> toInt =  MappingRule.of(s -> Integer.parseInt(s), "not.a.number");

        MappingRule<Option<String>, Option<Integer>> optionRule = toInt.liftToOption();

        optionRule.test(Option.some("1")); // Returns Valid(Some(1))
        optionRule.test(Option.none());     // Returns Valid(None)
        optionRule.test(Option.some("a")); // Returns Invalid("not.a.number")
        // @end
    }

    void liftToOptionalExample() {
        // @start region="lift-to-optional-example"
        MappingRule<String, Integer> toInt =  MappingRule.of(s -> Integer.parseInt(s), "not.a.number");

        MappingRule<Optional<String>, Optional<Integer>> optionalRule = toInt.liftToOptional();

        optionalRule.test(Optional.of("1")); // Returns Valid(Optional(1))
        optionalRule.test(Optional.empty());   // Returns Valid(Optional.empty)
        optionalRule.test(Optional.of("a")); // Returns Invalid("not.a.number")
        // @end
    }

    void liftToMapExample() {
        // @start region="lift-to-map-example"
        MappingRule<String, Integer> toInt =  MappingRule.of(s -> Integer.parseInt(s), "not.a.number");

        MappingRule<Map<String, String>, Map<String, Integer>> mapRule = toInt.liftToMap();

        mapRule.test(HashMap.of("k1", "1")); // Returns Valid(Map("k1", 1))
        mapRule.test(HashMap.of("k1", "a")); // Returns Invalid(ErrorMessage("not.a.number").atIndex("k1"))
        // @end
    }

    void liftToMapExtractorExample() {
        // @start region="lift-to-map-extractor-example"
        MappingRule<String, Integer> toInt =  MappingRule.of(s -> Integer.parseInt(s), "not.a.number");

        MappingRule<Map<Integer, String>, Map<Integer, Integer>> mapRule = toInt.liftToMap(k -> "item-" + k);

        mapRule.test(HashMap.of(1, "a")); // Returns Invalid(ErrorMessage("not.a.number").atIndex("item-1"))
        // @end
    }

    void requiredOptionExample() {
        // @start region="required-option-example"
        // 1. A rule that checks if a string is not empty
        MappingRule<String, String> notEmpty = s ->
            s.isEmpty() ? Validation.invalid("not.empty") : Validation.valid(s);

        // 2. A rule that requires the Option to be present before applying the rule
        MappingRule<Option<String>, String> requiredString = MappingRule.requiredOption(notEmpty);

        // 3. Usage
        Validation<String> valid = requiredString.test(Option.of("hello")); // Returns Valid("hello")
        Validation<String> invalid = requiredString.test(Option.none());      // Returns Invalid("must.not.be.empty")
        // @end
    }

    void requiredOptionalExample() {
        // @start region="required-optional-example"
        // 1. A rule that checks if a string is not empty
        MappingRule<String, String> notEmpty = s ->
            s.isEmpty() ? Validation.invalid("not.empty") : Validation.valid(s);

        // 2. A rule that requires the Option to be present before applying the rule
        MappingRule<Optional<String>, String> requiredString = MappingRule.requiredOptional(notEmpty);

        // 3. Usage
        Validation<String> valid = requiredString.test(Optional.of("hello")); // Returns Valid("hello")
        Validation<String> invalid = requiredString.test(Optional.empty());      // Returns Invalid("must.not.be.empty")
        // @end
    }

    record User(String name) {}

    void withExample() {
        // @start region="with-example"
        // 1. A rule that validates a String and returns its length
        MappingRule<String, Integer> lengthRule = s ->
            s.isEmpty() ? Validation.invalid("not.empty") : Validation.valid(s.length());

        // 2. A rule that applies 'lengthRule' to the 'name' property of a User
        MappingRule<User, Integer> userLengthRule = MappingRule.with(User::name, lengthRule);

        // 3. Usage
        userLengthRule.test(new User("Alice")); // Returns Valid(5)
        userLengthRule.test(new User(""));      // Returns Invalid("not.empty")
        // @end
    }
}
