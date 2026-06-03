package be.iffy.fv.dsl.impl;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import be.iffy.fv.rules.text.StringOps;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static be.iffy.fv.dsl.DSL.after;
import static be.iffy.fv.rules.Rules.strings;

class AfterDSLTest {

    @Nested
    class IsRule {

        @Test
        void is_whenTransformedInputIsValid_returnsValid() {
            Rule<String> rule = after(StringOps.trim()).is(strings.maxLength(3));
            
            Validation<String> result = rule.test(" abc ");

            assertThatValidation(result).isValid().isEqualTo("abc");
        }

        @Test
        void is_whenTransformedInputIsInvalid_returnsInvalid() {
            Rule<String> rule = after(StringOps.trim()).is(strings.maxLength(3));
            
            Validation<String> result = rule.test(" abcd ");

            assertThatValidation(result).isInvalid().hasErrorKeys("must.have.max.length");
        }

        @Test
        void is_whenInputIsNull_handlesNullSafely() {
            Rule<String> rule = after(StringOps.trim()).is(strings.maxLength(3));
            
            Validation<String> result = rule.test(null);

            assertThatValidation(result).isInvalid().hasErrorKeys("must.not.be.null");
        }
    }

    @Nested
    class IsMappingRule {

        MappingRule<String, Integer> mappingRule = after(StringOps::trim).is(strings.asInteger());

        @Test
        void is_whenTransformedInputIsValid_returnsValidMappedValue() {
            Validation<Integer> result = mappingRule.test(" 123 ");

            assertThatValidation(result).isValid().isEqualTo(123);
        }

        @Test
        void is_whenTransformedInputIsInvalid_returnsInvalid() {
            Validation<Integer> result = mappingRule.test(" abc ");

            assertThatValidation(result).isInvalid().hasErrorKeys("must.be.integer");
        }
        
        @Test
        void is_whenInputIsNull_handlesNullSafely() {
             Validation<Integer> result = mappingRule.test(null);

            assertThatValidation(result).isInvalid().hasErrorKeys("must.not.be.null");
        }
    }
}
