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
        void allAnnotationTypes_noErrors() {
            // null BeanFactory: @FvRuleBean annotations are skipped, all others are validated
            assertThat(FvRuleStartupValidator.scanAndValidate(List.of(GOOD_PACKAGE), null)).isEmpty();
        }

        @Test
        void emptyPackageList_noErrors() {
            assertThat(FvRuleStartupValidator.scanAndValidate(List.of(), null)).isEmpty();
        }
    }

    @Nested
    class WhenAnnotationsAreMisconfigured {

        @Test
        void allErrorsCollectedInSinglePass() {
            List<String> errors = FvRuleStartupValidator.scanAndValidate(List.of(BAD_PACKAGE), null);

            assertThat(errors).hasSize(4);
        }

        @Test
        void wrongFieldName_errorMentionsClassName() {
            List<String> errors = FvRuleStartupValidator.scanAndValidate(List.of(BAD_PACKAGE), null);

            assertThat(errors)
                .anyMatch(e -> e.contains("WrongFieldName") && e.contains("NONEXISTENT"));
        }

        @Test
        void missingConstructor_errorMentionsClassName() {
            List<String> errors = FvRuleStartupValidator.scanAndValidate(List.of(BAD_PACKAGE), null);

            assertThat(errors)
                .anyMatch(e -> e.contains("MissingConstructor"));
        }

        @Test
        void fieldOrParameterAnnotation_errorMentionsLocation() {
            List<String> errors = FvRuleStartupValidator.scanAndValidate(List.of(BAD_PACKAGE), null);

            assertThat(errors)
                .anyMatch(e -> e.contains("BadParamAnnotation") && e.contains("must implement Rule or RuleProvider"));
        }

        @Test
        void methodReturnAnnotation_errorMentionsMethod() {
            List<String> errors = FvRuleStartupValidator.scanAndValidate(List.of(BAD_PACKAGE), null);

            assertThat(errors)
                .anyMatch(e -> e.contains("BadReturnAnnotation") && e.contains("badMethod") && e.contains("must implement Rule or RuleProvider"));
        }
    }

    @Nested
    class WhenScanningMultiplePackages {

        @Test
        void goodAndBadPackageTogether_onlyBadPackageProducesErrors() {
            List<String> errors = FvRuleStartupValidator.scanAndValidate(
                List.of(GOOD_PACKAGE, BAD_PACKAGE), null
            );

            assertThat(errors).hasSize(4);
            assertThat(errors).noneMatch(e ->
                e.contains("Person") || e.contains("Order") || e.contains("Widget")
                    || e.contains("Gadget") || e.contains("Shipment")
            );
        }
    }
}
