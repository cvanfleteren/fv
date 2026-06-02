package be.iffy.fv.rules.numbers;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static be.iffy.fv.rules.RulesTest.invalidTest;
import static be.iffy.fv.rules.RulesTest.validTest;

class NumberRulesTest {

    private static final BigDecimalRules bigDecimals = BigDecimalRules.bigDecimals;

    private static final BigIntegerRules bigIntegers = BigIntegerRules.bigInts;

    @Nested
    class PrecisionEdgeCases {

        @Test
        void positive_whenGivenVerySmallBigDecimal_isValid() {
            validTest(new BigDecimal("1e-400"), bigDecimals.positive());
        }

        @Test
        void min_whenGivenVeryLargeBigIntegerDifference_isInvalid() {
            BigInteger base = BigInteger.TWO.pow(200);
            BigInteger input = base.add(BigInteger.ONE);
            BigInteger minInclusive = base.add(BigInteger.TWO);

            invalidTest(input, bigIntegers.min(minInclusive), "must.be.at.least");
        }
    }
}