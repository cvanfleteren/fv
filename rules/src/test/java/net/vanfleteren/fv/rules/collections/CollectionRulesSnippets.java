package net.vanfleteren.fv.rules.collections;

import io.vavr.collection.List;
import net.vanfleteren.fv.Rule;
import net.vanfleteren.fv.Validation;

import static net.vanfleteren.fv.rules.collections.CollectionRules.collections;
import static net.vanfleteren.fv.rules.text.StringRules.strings;

class CollectionRulesSnippets {

    void uniqueByExample() {
        // @start region="unique-by-example"
        record Person(String email, String name) {}

        List<Person> people = List.of(
                new Person("alice@example.com", "Alice"),
                new Person("bob@example.com", "Bob"),
                new Person("alice@example.com", "Alicia")
        );

        Rule<Iterable<Person>> rule = collections().uniqueBy(Person::email, "email");
        Validation<Iterable<Person>> result = rule.test(people); // Invalid("must.be.unique.by.key")
        // @end
    }

    void allMatchRuleExample() {
        // @start region="all-match-rule-example"
        List<String> names = List.of("Alice", "Bob", "Charlie");
        Rule<List<String>> rule = collections().allMatchRule(strings().minLength(3));

        Validation<List<String>> result = rule.test(names); // Valid
        // @end
    }

}
