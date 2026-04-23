package be.iffy.fv;

import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.HashMap;
import io.vavr.control.Option;
import java.util.Optional;

public class RuleSnippets {

    void ruleExample() {
        // @start region="rule-example"
        // 1. Define a rule using a predicate and an error message key
        Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "string.cannot.be.empty");

        // 2. Use the rule to validate a value
        Validation<String> result = notEmpty.test("hello");

        // 3. Handle the result (classical approach)
        if (result.isValid()) {
            System.out.println("Success: " + result.getOrElseThrow());
        } else {
            System.err.println("Errors: " + result.errors().map(ErrorMessage::message).mkString(", "));
        }
        // @end
    }

    void testExample() {
        // @start region="test-example"
        // 1. A rule that validates if a String is not empty
        Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "not.empty");

        // 2. Successful validation: String "hello" -> Valid
        Validation<String> success = notEmpty.test("hello");
        // Returns Valid("hello")

        // 3. Failed validation: String "" -> Invalid
        Validation<String> failure = notEmpty.test("");
        // Returns Invalid(ErrorMessage("not.empty"))
        // @end
    }

    void ofStringExample() {
        // @start region="of-string-example"
        // 1. Create a rule using a predicate and an error key
        Rule<Integer> positive = Rule.of(i -> i > 0, "must.be.positive");

        // 2. Usage
        positive.test(10); // Returns Valid(10)
        positive.test(-5); // Returns Invalid(ErrorMessage("must.be.positive"))
        // @end
    }

    void ofErrorExample() {
        // @start region="of-error-example"
        // 1. Create a rule using a predicate and an ErrorMessage
        ErrorMessage error = ErrorMessage.of("must.be.positive");
        Rule<Integer> positive = Rule.of(i -> i > 0, error);

        // 2. Usage
        positive.test(10); // Returns Valid(10)
        positive.test(-5); // Returns Invalid(ErrorMessage("must.be.positive"))
        // @end
    }

    void andExample() {
        // @start region="and-example"
        // 1. Two rules to be combined
        Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "not.empty");
        Rule<String> atLeast5 = Rule.of(s -> s.length() >= 5, "at.least.5");

        // 2. Chain them: stop at the first failure
        Rule<String> combined = notEmpty.and(atLeast5);

        // 3. Usage
        combined.test("hello"); // Returns Valid("hello")
        combined.test("");      // Returns Invalid("not.empty")
        combined.test("abc");   // Returns Invalid("at.least.5")
        // @end
    }

    void andAlsoExample() {
        // @start region="and-also-example"
        // 1. Two rules to be combined
        Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "not.empty");
        Rule<String> atLeast5 = Rule.of(s -> s.length() >= 5, "at.least.5");

        // 2. Combine them: collect all failures
        Rule<String> combined = notEmpty.andAlso(atLeast5);

        // 3. Usage
        combined.test(""); // Returns Invalid("not.empty", "at.least.5")
        // @end
    }

    void orExample() {
        // @start region="or-example"
        // 1. Two rules to be combined with OR logic
        Rule<String> startsWithA = Rule.of(s -> s.startsWith("A"), "must.start.with.A");
        Rule<String> startsWithB = Rule.of(s -> s.startsWith("B"), "must.start.with.B");

        // 2. Combine them
        Rule<String> combined = startsWithA.or(startsWithB);

        // 3. Usage
        combined.test("Apple"); // Returns Valid("Apple")
        combined.test("Banana"); // Returns Valid("Banana")
        combined.test("Cherry"); // Returns Invalid("must.start.with.A", "must.start.with.B")
        // @end
    }

    void negateStringExample() {
        // @start region="negate-string-example"
        // 1. A rule that checks if a string starts with "A"
        Rule<String> startsWithA = Rule.of(s -> s.startsWith("A"), "must.start.with.A");

        // 2. Negate it: now it validates that it does NOT start with "A"
        Rule<String> doesNotStartWithA = startsWithA.negate("must.not.start.with.A");

        // 3. Usage
        doesNotStartWithA.test("Banana"); // Returns Valid("Banana")
        doesNotStartWithA.test("Apple");  // Returns Invalid(ErrorMessage("must.not.start.with.A"))
        // @end
    }

    void negateErrorExample() {
        // @start region="negate-error-example"
        // 1. A rule that checks if a string starts with "A"
        Rule<String> startsWithA = Rule.of(s -> s.startsWith("A"), "must.start.with.A");

        // 2. Negate it using an ErrorMessage
        ErrorMessage error = ErrorMessage.of("must.not.start.with.A");
        Rule<String> doesNotStartWithA = startsWithA.negate(error);

        // 3. Usage
        doesNotStartWithA.test("Banana"); // Returns Valid("Banana")
        doesNotStartWithA.test("Apple");  // Returns Invalid(ErrorMessage("must.not.start.with.A"))
        // @end
    }

    void onlyIfPredicateExample() {
        // @start region="only-if-predicate-example"
        // Only validate if the string is not empty
        Rule<String> minLength = Rule.of(s -> s.length() >= 5, "too.short");
        Rule<String> whenNotEmpty = minLength.onlyIf(s -> !s.isEmpty());

        whenNotEmpty.test("");      // Returns Valid("")
        whenNotEmpty.test("abc");   // Returns Invalid("too.short")
        whenNotEmpty.test("abcde"); // Returns Valid("abcde")
        // @end
    }

    interface Config { boolean isValidationEnabled(); }
    Config config;

    void onlyIfSupplierExample() {
        // @start region="only-if-supplier-example"
        // Only validate if a certain flag is enabled
        Rule<String> rule = Rule.of(s -> s.length() >= 5, "too.short");
        Rule<String> conditional = rule.onlyIf(() -> config.isValidationEnabled());

        // If isValidationEnabled() returns false:
        conditional.test("abc"); // Returns Valid("abc")
        // @end
    }

    void recoverWithRuleExample() {
        // @start region="recover-with-rule-example"
        // 1. Try to validate as admin, otherwise try as guest
        Rule<String> adminOnly = Rule.of(s -> s.equals("admin"), "not.admin");
        Rule<String> guestOk = Rule.of(s -> s.equals("guest"), "not.guest");

        // 2. Use recoverWithRule to fall back
        Rule<String> combined = adminOnly.recoverWithRule(guestOk);

        // 3. Usage
        combined.test("admin"); // Returns Valid("admin")
        combined.test("guest"); // Returns Valid("guest")
        combined.test("other"); // Returns Invalid("not.guest")
        // @end
    }

    void liftToListExample() {
        // @start region="lift-to-list-example"
        // 1. Define a rule for a single element
        Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "must.not.be.empty");

        // 2. Lift it to apply to a list
        Rule<List<String>> listRule = notEmpty.liftToList();

        // 3. Usage
        listRule.test(List.of("a", "b")); // Returns Valid(List("a", "b"))
        listRule.test(List.of("a", ""));  // Returns Invalid(ErrorMessage("must.not.be.empty").atIndex(1))
        // @end
    }

    void liftToJListExample() {
        // @start region="lift-to-jlist-example"
        // 1. Define a rule for a single element
        Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "must.not.be.empty");

        // 2. Lift it to apply to a list
        Rule<java.util.List<String>> listRule = notEmpty.liftToJList();

        // 3. Usage
        listRule.test(java.util.List.of("a", "b")); // Returns Valid(List("a", "b"))
        listRule.test(java.util.List.of("a", ""));  // Returns Invalid(ErrorMessage("must.not.be.empty").atIndex(1))
        // @end
    }

    void liftToOptionExample() {
        // @start region="lift-to-option-example"
        // 1. Define a rule for a single element
        Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "must.not.be.empty");

        // 2. Lift it to apply to an Option
        Rule<Option<String>> optionRule = notEmpty.liftToOption();

        // 3. Usage
        optionRule.test(Option.some("a")); // Returns Valid(Some("a"))
        optionRule.test(Option.none());     // Returns Valid(None)
        optionRule.test(Option.some(""));  // Returns Invalid("must.not.be.empty")
        // @end
    }

    void liftToOptionalExample() {
        // @start region="lift-to-optional-example"
        // 1. Define a rule for a single element
        Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "must.not.be.empty");

        // 2. Lift it to apply to an Optional
        Rule<Optional<String>> optionalRule = notEmpty.liftToOptional();

        // 3. Usage
        optionalRule.test(Optional.of("a")); // Returns Valid(Optional("a"))
        optionalRule.test(Optional.empty());   // Returns Valid(Optional.empty)
        optionalRule.test(Optional.of(""));  // Returns Invalid("must.not.be.empty")
        // @end
    }

    void liftToMapExample() {
        // @start region="lift-to-map-example"
        // 1. Define a rule for a single element
        Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "must.not.be.empty");

        // 2. Lift it to apply to a map
        Rule<Map<String, String>> mapRule = notEmpty.liftToMap();

        // 3. Usage
        mapRule.test(HashMap.of("key1", "val1")); // Returns Valid(Map("key1", "val1"))
        mapRule.test(HashMap.of("key1", ""));     // Returns Invalid(ErrorMessage("must.not.be.empty").atIndex("key1"))
        // @end
    }

    void liftToMapExtractorExample() {
        // @start region="lift-to-map-extractor-example"
        // 1. Define a rule for a single element
        Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "must.not.be.empty");

        // 2. Lift it to apply to a map with a custom key extractor for the error path
        Rule<Map<Integer, String>> mapRule = notEmpty.liftToMap(key -> "item-" + key);

        // 3. Usage
        mapRule.test(HashMap.of(1, "")); // Returns Invalid(ErrorMessage("must.not.be.empty").atIndex("item-1"))
        // @end
    }

    void liftToJMapExample() {
        // @start region="lift-to-jmap-example"
        // 1. Define a rule for a single element
        Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "must.not.be.empty");

        // 2. Lift it to apply to a map
        Rule<java.util.Map<String, String>> mapRule = notEmpty.liftToJMap();

        // 3. Usage
        mapRule.test(java.util.Map.of("key1", "val1")); // Returns Valid(Map("key1", "val1"))
        mapRule.test(java.util.Map.of("key1", ""));     // Returns Invalid(ErrorMessage("must.not.be.empty").atIndex("key1"))
        // @end
    }

    void liftToJMapExtractorExample() {
        // @start region="lift-to-jmap-extractor-example"
        // 1. Define a rule for a single element
        Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "must.not.be.empty");

        // 2. Lift it to apply to a map with a custom key extractor for the error path
        Rule<java.util.Map<Integer, String>> mapRule = notEmpty.liftToJMap(key -> "item-" + key);

        // 3. Usage
        mapRule.test(java.util.Map.of(1, "")); // Returns Invalid(ErrorMessage("must.not.be.empty").atIndex("item-1"))
        // @end
    }

    void bothExample() {
        // @start region="both-example"
        Rule<String> minLength = Rule.of(s -> s.length() >= 3, "too.short");
        Rule<String> containsAt = Rule.of(s -> s.contains("@"), "missing.at");
        Rule<String> both = Rule.both(minLength, containsAt);

        both.test("a"); // Returns Invalid("too.short", "missing.at")
        // @end
    }

    void allExample() {
        // @start region="all-example"
        Rule<String> rule1 = Rule.of(s -> s.length() >= 3, "too.short");
        Rule<String> rule2 = Rule.of(s -> s.contains("@"), "missing.at");
        Rule<String> rule3 = Rule.of(s -> s.endsWith(".com"), "wrong.domain");
        Rule<String> all = Rule.all(rule1, rule2, rule3);

        all.test("a"); // Returns Invalid("too.short", "missing.at", "wrong.domain")
        // @end
    }

    void anyExample() {
        // @start region="any-example"
        Rule<String> hasAt = Rule.of(s -> s.contains("@"), "missing.at");
        Rule<String> hasDot = Rule.of(s -> s.contains("."), "missing.dot");
        Rule<String> combined = Rule.any(hasAt, hasDot);

        combined.test("abc"); // Returns Invalid("missing.at", "missing.dot")
        combined.test("a.b"); // Returns Valid("a.b")
        // @end
    }

    void requiredOptionExample() {
        // @start region="required-option-example"
        // 1. A rule that checks if a string is not empty
        Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "not.empty");

        // 2. A rule that requires the Option to be present before applying the rule
        MappingRule<Option<String>, String> requiredString = Rule.requiredOption(notEmpty);

        // 3. Usage
        requiredString.test(Option.of("hello")); // Returns Valid("hello")
        requiredString.test(Option.none());      // Returns Invalid("must.not.be.empty")
        // @end
    }

    void requiredOptionalExample() {
        // @start region="required-optional-example"
        // 1. A rule that checks if a string is not empty
        Rule<String> notEmpty = Rule.of(s -> !s.isEmpty(), "not.empty");

        // 2. A rule that requires the Optional to be present before applying the rule
        MappingRule<Optional<String>, String> requiredString = Rule.requiredOptional(notEmpty);

        // 3. Usage
        requiredString.test(Optional.of("hello")); // Returns Valid("hello")
        requiredString.test(Optional.empty());      // Returns Invalid("must.not.be.empty")
        // @end
    }

    record User(String name) {}

    void withExample() {
        // @start region="with-example"
        // 1. Define a rule for a property
        Rule<String> nameRule = Rule.of(s -> s.length() > 3, "too.short");

        // 2. Apply it to a property of a record
        Rule<User> userRule = Rule.with(User::name, nameRule);

        // 3. Usage
        userRule.test(new User("Joe")); // Returns Invalid("too.short")
        userRule.test(new User("Alice")); // Returns Valid(User("Alice"))
        // @end
    }
}
