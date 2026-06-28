package be.iffy.fv.jakarta.support;

import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import be.iffy.fv.jakarta.FvRule;

import java.util.List;

import static be.iffy.fv.dsl.DSL.*;

/**
 * Test model with a list of objects validated per-element — exercises intermediate indexed paths
 * (e.g. "lines[1].qty") where the index is not on the terminal segment.
 */
@FvRule(Cart.Validator.class)
public record Cart(List<Cart.Line> lines) {

    public record Line(String sku, int qty) {}

    public static class Validator implements Rule<Cart> {

        @Override
        public Validation<Cart> apply(Cart c) {
            return validateThatList(c.lines(), "lines")
                .eachIs(ints.atLeast(1).on(Line::qty))
                .validate()
                .map(x -> c);
        }
    }
}
