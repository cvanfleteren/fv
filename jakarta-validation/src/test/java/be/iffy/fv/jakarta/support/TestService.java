package be.iffy.fv.jakarta.support;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class TestService {

    public String enroll(@Valid Person person) {
        return "enrolled: " + person.name();
    }

    public String enrollWithDegrees(@Valid Person person, @Max(2) int degrees) {
        return "enrolled: " + person.name() + " with " + degrees + " degrees";
    }

    public String placeOrder(@Valid Order order) {
        return "placed: " + order.reference();
    }

    public String processSpringThing(@Valid SpringThing thing) {
        return "processed: " + thing.label();
    }
}
