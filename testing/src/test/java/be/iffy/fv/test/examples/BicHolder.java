package be.iffy.fv.test.examples;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Validation;

import java.util.Optional;
import static be.iffy.fv.dsl.DSL.optionals;
import static be.iffy.fv.dsl.DSL.validateThat;

public class BicHolder {

    record HasBic(Bic bic){}

    record HasOptionalBicString(Optional<String> bic){}

    public void foo() {
        Validation<Bic> bicV = Bic.from("123").at(HasBic::bic);

        MappingRule<String, Bic> b = Bic::from;

        var bicHolder = new HasOptionalBicString(Optional.empty());

        Validation<Bic> bicV2 = validateThat(bicHolder.bic()).is(optionals.required(String.class).then(Bic::from));
    }

}
