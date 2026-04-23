package be.iffy.fv.rules.collections;

import be.iffy.fv.Rule;
import be.iffy.fv.Validation;

import java.util.Collection;
import java.util.List;

import static be.iffy.fv.rules.collections.JCollectionRules.jCollections;
import static be.iffy.fv.rules.text.StringRules.strings;

class JCollectionRulesSnippets {

    void uniqueByExample() {
        // @start region="unique-by-example"
        record Person(String email, String name) {}

        List<Person> people = List.of(
                new Person("alice@example.com", "Alice"),
                new Person("bob@example.com", "Bob"),
                new Person("alice@example.com", "Alicia")
        );

        Rule<Collection<Person>> rule = jCollections().uniqueBy(Person::email, "email");
        Validation<Collection<Person>> result = rule.test(people); // Invalid("must.be.unique.by.key")
        // @end
    }

    void allMatchRuleExample() {
        // @start region="all-match-rule-example"
        List<String> names = List.of("Alice", "Bob", "Charlie");
        Rule<List<String>> rule = jCollections().allMatchRule(strings().minLength(3));

        Validation<List<String>> result = rule.test(names); // Valid
        // @end
    }

}
