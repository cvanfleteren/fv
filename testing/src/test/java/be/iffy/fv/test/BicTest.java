package be.iffy.fv.test;

import be.iffy.fv.test.examples.Bic;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static be.iffy.fv.dsl.DSL.stringOps;
import static org.assertj.core.api.Assertions.assertThat;

public class BicTest {

    @ParameterizedTest
    @ValueSource(strings = {" KREBBE2 L123 ", "KREBBE22", "KREBBE2L", "KREBBE22XXX", "KREBBE2L123"})
    void valid_bic_isAccepted(String validBic) {

        assertThat( new Bic(validBic))
                .extracting(Bic::value)
                .isEqualTo(stringOps.removeWhitespace().apply(validBic));
    }

}
