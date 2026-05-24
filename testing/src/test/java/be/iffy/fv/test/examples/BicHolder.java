package be.iffy.fv.test.examples;

import be.iffy.fv.Validation;

public class BicHolder {

    record HasBic(Bic bic){}

    public void foo() {
        Validation<Bic> bicV = Bic.validate("123").at(HasBic::bic);
    }

}
