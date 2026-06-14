package be.iffy.fv.dsl.impl;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static be.iffy.fv.dsl.DSL.*;

class AfterDSLTest {

    @Nested
    class IsRule {

        @Test
        void is_whenTransformedInputIsValid_returnsValid() {
            Rule<String> rule = after(stringOps.trim()).is(strings.maxLength(3));
            
            Validation<String> result = rule.apply(" abc ");

            assertThatValidation(result).isValid().isEqualTo("abc");
        }

        @Test
        void is_whenTransformedInputIsInvalid_returnsInvalid() {
            Rule<String> rule = after(stringOps.trim()).is(strings.maxLength(3));
            
            Validation<String> result = rule.apply(" abcd ");

            assertThatValidation(result).isInvalid().hasErrorKeys("must.have.max.length");
        }

        @Test
        void is_whenInputIsNull_handlesNullSafely() {
            Rule<String> rule = after(stringOps.trim()).is(strings.maxLength(3));
            
            Validation<String> result = rule.apply(null);

            assertThatValidation(result).isInvalid().hasErrorKeys("must.not.be.null");
        }
    }

    @Nested
    class IsMappingRule {

        MappingRule<String, Integer> mappingRule = after(stringOps::trim).is(strings.asInteger());

        @Test
        void is_whenTransformedInputIsValid_returnsValidMappedValue() {
            Validation<Integer> result = mappingRule.apply(" 123 ");

            assertThatValidation(result).isValid().isEqualTo(123);
        }

        @Test
        void is_whenTransformedInputIsInvalid_returnsInvalid() {
            Validation<Integer> result = mappingRule.apply(" abc ");

            assertThatValidation(result).isInvalid().hasErrorKeys("must.be.integer");
        }
        
        @Test
        void is_whenInputIsNull_handlesNullSafely() {
             Validation<Integer> result = mappingRule.apply(null);

            assertThatValidation(result).isInvalid().hasErrorKeys("must.not.be.null");
        }
    }
}
