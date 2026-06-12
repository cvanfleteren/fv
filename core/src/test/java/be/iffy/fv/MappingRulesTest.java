package be.iffy.fv;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;

class MappingRulesTest {

    @Nested
    class Combine2 {

        @Test
        void combine_whenBothRulesAreValid_returnsCombinedResult() {
            MappingRule<String, Integer> rule1 = s -> Validation.valid(s.length());
            MappingRule<String, String> rule2 = s -> Validation.valid(s.toUpperCase());

            MappingRule<String, String> combined = MappingRules.combine(rule1, rule2).map((length, upper) -> length + ":" + upper);

            assertThatValidation(combined.test("hello"))
                    .isValid()
                    .isEqualTo("5:HELLO");
        }

        @Test
        void combine_whenBothRulesAreInvalid_returnsCombinedErrors() {
            MappingRule<String, Integer> rule1 = s -> Validation.invalid("error.one");
            MappingRule<String, String> rule2 = s -> Validation.invalid("error.two");

            MappingRule<String, String> combined = MappingRules.combine(rule1, rule2).map((length, upper) -> length + ":" + upper);

            assertThatValidation(combined.test("hello"))
                    .isInvalid()
                    .hasErrorMessage("error.one")
                    .hasErrorMessage("error.two");
        }

        @Test
        void combine_whenOneRuleIsInvalid_returnsInvalidWithCombinedErrors() {
            MappingRule<String, Integer> rule1 = s -> Validation.valid(s.length());
            MappingRule<String, String> rule2 = s -> Validation.invalid("error.two");

            MappingRule<String, String> combined = MappingRules.combine(rule1, rule2).map((length, upper) -> length + ":" + upper);

            assertThatValidation(combined.test("hello"))
                    .isInvalid()
                    .hasErrorMessage("error.two");
        }

        @Test
        void into_isAliasForMap() {
            MappingRule<String, Integer> rule1 = s -> Validation.valid(s.length());
            MappingRule<String, String> rule2 = s -> Validation.valid(s.toUpperCase());

            MappingRule<String, String> combinedMap = MappingRules.combine(rule1, rule2).map((l, u) -> l + ":" + u);
            MappingRule<String, String> combinedInto = MappingRules.combine(rule1, rule2).into((l, u) -> l + ":" + u);

            assertThatValidation(combinedMap.test("hello"))
                    .isValid()
                    .isEqualTo("5:HELLO");
            assertThatValidation(combinedInto.test("hello"))
                    .isValid()
                    .isEqualTo("5:HELLO");
        }
    }

    @Nested
    class Combine3 {
        @Test
        void combine_whenAllRulesAreValid_returnsCombinedResult() {
            MappingRule<String, Integer> rule1 = s -> Validation.valid(s.length());
            MappingRule<String, String> rule2 = s -> Validation.valid(s.toUpperCase());
            MappingRule<String, String> rule3 = s -> Validation.valid("static");

            MappingRule<String, String> combined = MappingRules.combine(rule1, rule2, rule3).map((l, u, s) -> l + ":" + u + ":" + s);

            assertThatValidation(combined.test("hello"))
                    .isValid()
                    .isEqualTo("5:HELLO:static");
        }
    }

    @Nested
    class Combine4 {
        @Test
        void combine_whenAllRulesAreValid_returnsCombinedResult() {
            MappingRule<String, Integer> rule1 = s -> Validation.valid(s.length());
            MappingRule<String, String> rule2 = s -> Validation.valid(s.toUpperCase());
            MappingRule<String, String> rule3 = s -> Validation.valid("a");
            MappingRule<String, String> rule4 = s -> Validation.valid("b");

            MappingRule<String, String> combined = MappingRules.combine(rule1, rule2, rule3, rule4).map((l, u, a, b) -> l + ":" + u + ":" + a + ":" + b);

            assertThatValidation(combined.test("hello"))
                    .isValid()
                    .isEqualTo("5:HELLO:a:b");
        }
    }

    @Nested
    class Combine5 {
        @Test
        void combine_whenAllRulesAreValid_returnsCombinedResult() {
            MappingRule<String, Integer> rule1 = s -> Validation.valid(s.length());
            MappingRule<String, String> rule2 = s -> Validation.valid(s.toUpperCase());
            MappingRule<String, String> rule3 = s -> Validation.valid("a");
            MappingRule<String, String> rule4 = s -> Validation.valid("b");
            MappingRule<String, String> rule5 = s -> Validation.valid("c");

            MappingRule<String, String> combined = MappingRules.combine(rule1, rule2, rule3, rule4, rule5).map((l, u, a, b, c) -> l + ":" + u + ":" + a + ":" + b + ":" + c);

            assertThatValidation(combined.test("hello"))
                    .isValid()
                    .isEqualTo("5:HELLO:a:b:c");
        }
    }

    @Nested
    class Combine6 {
        @Test
        void combine_whenAllRulesAreValid_returnsCombinedResult() {
            MappingRule<String, Integer> rule1 = s -> Validation.valid(s.length());
            MappingRule<String, String> rule2 = s -> Validation.valid(s.toUpperCase());
            MappingRule<String, String> rule3 = s -> Validation.valid("a");
            MappingRule<String, String> rule4 = s -> Validation.valid("b");
            MappingRule<String, String> rule5 = s -> Validation.valid("c");
            MappingRule<String, String> rule6 = s -> Validation.valid("d");

            MappingRule<String, String> combined = MappingRules.combine(rule1, rule2, rule3, rule4, rule5, rule6).map((l, u, a, b, c, d) -> l + ":" + u + ":" + a + ":" + b + ":" + c + ":" + d);

            assertThatValidation(combined.test("hello"))
                    .isValid()
                    .isEqualTo("5:HELLO:a:b:c:d");
        }
    }

    @Nested
    class Combine7 {
        @Test
        void combine_whenAllRulesAreValid_returnsCombinedResult() {
            MappingRule<String, Integer> rule1 = s -> Validation.valid(s.length());
            MappingRule<String, String> rule2 = s -> Validation.valid(s.toUpperCase());
            MappingRule<String, String> rule3 = s -> Validation.valid("a");
            MappingRule<String, String> rule4 = s -> Validation.valid("b");
            MappingRule<String, String> rule5 = s -> Validation.valid("c");
            MappingRule<String, String> rule6 = s -> Validation.valid("d");
            MappingRule<String, String> rule7 = s -> Validation.valid("e");

            MappingRule<String, String> combined = MappingRules.combine(rule1, rule2, rule3, rule4, rule5, rule6, rule7).map((l, u, a, b, c, d, e) -> l + ":" + u + ":" + a + ":" + b + ":" + c + ":" + d + ":" + e);

            assertThatValidation(combined.test("hello"))
                    .isValid()
                    .isEqualTo("5:HELLO:a:b:c:d:e");
        }
    }

    @Nested
    class Combine8 {
        @Test
        void combine_whenAllRulesAreValid_returnsCombinedResult() {
            MappingRule<String, Integer> rule1 = s -> Validation.valid(s.length());
            MappingRule<String, String> rule2 = s -> Validation.valid(s.toUpperCase());
            MappingRule<String, String> rule3 = s -> Validation.valid("a");
            MappingRule<String, String> rule4 = s -> Validation.valid("b");
            MappingRule<String, String> rule5 = s -> Validation.valid("c");
            MappingRule<String, String> rule6 = s -> Validation.valid("d");
            MappingRule<String, String> rule7 = s -> Validation.valid("e");
            MappingRule<String, String> rule8 = s -> Validation.valid("f");

            MappingRule<String, String> combined = MappingRules.combine(rule1, rule2, rule3, rule4, rule5, rule6, rule7, rule8).map((l, u, a, b, c, d, e, f) -> l + ":" + u + ":" + a + ":" + b + ":" + c + ":" + d + ":" + e + ":" + f);

            assertThatValidation(combined.test("hello"))
                    .isValid()
                    .isEqualTo("5:HELLO:a:b:c:d:e:f");
        }
    }

}