package be.iffy.fv.test.examples;

import be.iffy.fv.Validation;
import io.vavr.control.Try;
import org.junit.jupiter.api.Test;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;

class KboNumberTest {

    @Test
    void constructor_whenNull_invalid() {

        assertThatValidation(
                Validation.from(Try.of(() -> new KboNumber(null)))
        )
                .isInvalid()
                .hasErrorMessages("value.must.not.be.null");
    }

}