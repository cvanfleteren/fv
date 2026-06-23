package be.iffy.fv.jakarta.support;

import be.iffy.fv.Rule;
import be.iffy.fv.jakarta.FvRule;
import jakarta.validation.Valid;

import static be.iffy.fv.dsl.DSL.strings;

/**
 * Test model with a nested @FvRule-annotated field using standard BV @Valid cascade.
 * Shipment's rule only validates trackingNumber; Person is validated via BV cascade
 * because @Valid on the recipient component triggers Person's own @FvRule.
 */
@FvRule(on = Shipment.class, field = "RULE")
public record Shipment(String trackingNumber, @Valid Person recipient) {

    static Rule<Shipment> RULE = strings.minLength(5).on(Shipment::trackingNumber);
}
