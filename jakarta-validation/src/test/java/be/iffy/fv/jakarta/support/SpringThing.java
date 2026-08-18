package be.iffy.fv.jakarta.support;

import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import be.iffy.fv.jakarta.FvRuleBean;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.springframework.stereotype.Component;

import static be.iffy.fv.dsl.DSL.strings;

/**
 * Test fixture for @FvRuleBean: the rule validator is a Spring {@code @Component} that receives
 * constructor-injected dependencies from the Spring context.
 */
@FvRuleBean(SpringThing.Validator.class)
public record SpringThing(String label) {

    @Component
    @RequiredArgsConstructor
    public static class Validator implements Rule<SpringThing> {

        private final TestService testService;

        private static final Rule<SpringThing> IMPL = strings.minLength(3).on(SpringThing::label);

        @Override
        public Validation<SpringThing> apply(SpringThing t) {
            // pretend we need the testService somehow
            Assertions.assertThat(testService).isNotNull();
            return IMPL.apply(t);
        }
    }
}
