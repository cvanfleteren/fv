package be.iffy.fv.jakarta;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.vavr.collection.List;

import static org.assertj.core.api.Assertions.assertThat;

class FvRuleStartupValidatorTest {

    private static final String GOOD_PACKAGE = "be.iffy.fv.jakarta.support";
    private static final String BAD_PACKAGE  = "be.iffy.fv.jakarta.bad";

    @Nested
    class WhenAllAnnotationsAreCorrect {

        @Test
        void ruleClassMode_noErrors() {
            assertThat(FvRuleStartupValidator.scanAndValidate(List.of(GOOD_PACKAGE))).isEmpty();
        }

        @Test
        void emptyPackageList_noErrors() {
            assertThat(FvRuleStartupValidator.scanAndValidate(List.of())).isEmpty();
        }
    }

    @Nested
    class WhenAnnotationsAreMisconfigured {

        @Test
        void allErrorsCollectedInSinglePass() {
            List<String> errors = FvRuleStartupValidator.scanAndValidate(List.of(BAD_PACKAGE));

            assertThat(errors).hasSize(2);
        }

        @Test
        void wrongFieldName_errorMentionsClassName() {
            List<String> errors = FvRuleStartupValidator.scanAndValidate(List.of(BAD_PACKAGE));

            assertThat(errors)
                .anyMatch(e -> e.contains("WrongFieldName") && e.contains("NONEXISTENT"));
        }

        @Test
        void missingConstructor_errorMentionsClassName() {
            List<String> errors = FvRuleStartupValidator.scanAndValidate(List.of(BAD_PACKAGE));

            assertThat(errors)
                .anyMatch(e -> e.contains("MissingConstructor"));
        }
    }

    @Nested
    class WhenScanningMultiplePackages {

        @Test
        void goodAndBadPackageTogether_onlyBadPackageProducesErrors() {
            List<String> errors = FvRuleStartupValidator.scanAndValidate(
                List.of(GOOD_PACKAGE, BAD_PACKAGE)
            );

            assertThat(errors).hasSize(2);
            assertThat(errors).noneMatch(e ->
                e.contains("Person") || e.contains("Order") || e.contains("Widget") || e.contains("Gadget")
            );
        }
    }
}
