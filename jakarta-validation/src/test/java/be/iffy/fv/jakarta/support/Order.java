package be.iffy.fv.jakarta.support;

import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import be.iffy.fv.jakarta.FvRule;

import java.math.BigDecimal;
import java.util.List;

import static be.iffy.fv.dsl.DSL.*;

/**
 * Test model with nested paths and a list — exercises index path mapping.
 */
@FvRule(Order.Validator.class)
public record Order(String reference, List<BigDecimal> amounts) {

    public static class Validator implements Rule<Order> {

        @Override
        public Validation<Order> apply(Order o) {
            return Rule.all(
                Rule.on(Order::reference, strings.notBlank()),
                lists.validateValuesWith(bigDecimals.positive()).on(Order::amounts)
            ).apply(o);
        }
    }
}
