package be.iffy.fv.jakarta.support;

import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import be.iffy.fv.jakarta.FvRule;

import java.util.List;

import static be.iffy.fv.dsl.DSL.*;

/**
 * Test model with doubly-nested lists validated via FV rules — exercises 3-segment indexed paths
 * (e.g. "shelves[0].items[1].name") where two non-terminal segments carry indices.
 */
@FvRule(Depot.Validator.class)
public record Depot(List<Depot.Shelf> shelves) {

    public record Shelf(List<Depot.Item> items) {}
    public record Item(String name) {}

    public static class Validator implements Rule<Depot> {

        @Override
        public Validation<Depot> apply(Depot d) {
            return validateThatList(d.shelves(), "shelves")
                .eachIs(shelf ->
                    validateThatList(shelf.items(), "items")
                        .eachIs(strings.minLength(3).on(Depot.Item::name))
                        .validate()
                        .map(x -> shelf))
                .validate()
                .map(x -> d);
        }
    }
}
