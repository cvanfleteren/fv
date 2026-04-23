package net.vanfleteren.fv.dsl.experimental;

import net.vanfleteren.fv.MappingRule;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static net.vanfleteren.fv.assertj.ValidationAssert.assertThatValidation;
import static net.vanfleteren.fv.dsl.experimental.Validator.validatorFor;

class ValidatorArityTest {

    record Input(String f1, String f2, String f3, String f4, String f5, String f6, String f7, String f8) {}

    MappingRule<String, String> ok = MappingRule.of(s -> s, "ok");

    @Nested
    class Validator1 {
        @Test
        void builds_withOneValidField_returnsValid() {
            var validator = validatorFor(Input.class)
                    .where(Input::f1, ok)
                    .builds(f1 -> f1);

            var input = new Input("v1", null, null, null, null, null, null, null);
            assertThatValidation(validator.test(input)).isValid().hasValue("v1");
        }
    }

    @Nested
    class Validator2 {
        @Test
        void builds_withTwoValidFields_returnsValid() {
            var validator = validatorFor(Input.class)
                    .where(Input::f1, ok)
                    .where(Input::f2, ok)
                    .builds((f1, f2) -> f1 + f2);

            var input = new Input("v1", "v2", null, null, null, null, null, null);
            assertThatValidation(validator.test(input)).isValid().hasValue("v1v2");
        }
    }

    @Nested
    class Validator3 {
        @Test
        void builds_withThreeValidFields_returnsValid() {
            var validator = validatorFor(Input.class)
                    .where(Input::f1, ok)
                    .where(Input::f2, ok)
                    .where(Input::f3, ok)
                    .builds((f1, f2, f3) -> f1 + f2 + f3);

            var input = new Input("v1", "v2", "v3", null, null, null, null, null);
            assertThatValidation(validator.test(input)).isValid().hasValue("v1v2v3");
        }
    }

    @Nested
    class Validator4 {
        @Test
        void builds_withFourValidFields_returnsValid() {
            var validator = validatorFor(Input.class)
                    .where(Input::f1, ok)
                    .where(Input::f2, ok)
                    .where(Input::f3, ok)
                    .constraint(Input::f4, ok)
                    .builds((f1, f2, f3, f4) -> f1 + f2 + f3 + f4);

            var input = new Input("v1", "v2", "v3", "v4", null, null, null, null);
            assertThatValidation(validator.test(input)).isValid().hasValue("v1v2v3v4");
        }
    }

    @Nested
    class Validator5 {
        @Test
        void builds_withFiveValidFields_returnsValid() {
            var validator = validatorFor(Input.class)
                    .where(Input::f1, ok)
                    .where(Input::f2, ok)
                    .where(Input::f3, ok)
                    .constraint(Input::f4, ok)
                    .constraint(Input::f5, ok)
                    .builds((f1, f2, f3, f4, f5) -> f1 + f2 + f3 + f4 + f5);

            var input = new Input("v1", "v2", "v3", "v4", "v5", null, null, null);
            assertThatValidation(validator.test(input)).isValid().hasValue("v1v2v3v4v5");
        }
    }

    @Nested
    class Validator6 {
        @Test
        void builds_withSixValidFields_returnsValid() {
            var validator = validatorFor(Input.class)
                    .where(Input::f1, ok)
                    .where(Input::f2, ok)
                    .where(Input::f3, ok)
                    .constraint(Input::f4, ok)
                    .constraint(Input::f5, ok)
                    .constraint(Input::f6, ok)
                    .builds((f1, f2, f3, f4, f5, f6) -> f1 + f2 + f3 + f4 + f5 + f6);

            var input = new Input("v1", "v2", "v3", "v4", "v5", "v6", null, null);
            assertThatValidation(validator.test(input)).isValid().hasValue("v1v2v3v4v5v6");
        }
    }

    @Nested
    class Validator7 {
        @Test
        void builds_withSevenValidFields_returnsValid() {
            var validator = validatorFor(Input.class)
                    .where(Input::f1, ok)
                    .where(Input::f2, ok)
                    .where(Input::f3, ok)
                    .constraint(Input::f4, ok)
                    .constraint(Input::f5, ok)
                    .constraint(Input::f6, ok)
                    .constraint(Input::f7, ok)
                    .builds((f1, f2, f3, f4, f5, f6, f7) -> f1 + f2 + f3 + f4 + f5 + f6 + f7);

            var input = new Input("v1", "v2", "v3", "v4", "v5", "v6", "v7", null);
            assertThatValidation(validator.test(input)).isValid().hasValue("v1v2v3v4v5v6v7");
        }
    }

    @Nested
    class Validator8 {
        @Test
        void builds_withEightValidFields_returnsValid() {
            var validator = validatorFor(Input.class)
                    .where(Input::f1, ok)
                    .where(Input::f2, ok)
                    .where(Input::f3, ok)
                    .constraint(Input::f4, ok)
                    .constraint(Input::f5, ok)
                    .constraint(Input::f6, ok)
                    .constraint(Input::f7, ok)
                    .constraint(Input::f8, ok)
                    .builds((f1, f2, f3, f4, f5, f6, f7, f8) -> f1 + f2 + f3 + f4 + f5 + f6 + f7 + f8);

            var input = new Input("v1", "v2", "v3", "v4", "v5", "v6", "v7", "v8");
            assertThatValidation(validator.test(input)).isValid().hasValue("v1v2v3v4v5v6v7v8");
        }
    }
}
