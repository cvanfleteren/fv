package be.iffy.fv.jakarta.bval;

import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import be.iffy.fv.jakarta.FvRule;
import be.iffy.fv.jakarta.FvStaticRule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static be.iffy.fv.dsl.DSL.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the jakarta-validation bridge works correctly when Hibernate Validator is absent
 * and Apache BVal is the BV provider. The two most important things being checked:
 *
 * <ol>
 *   <li>No {@code NoClassDefFoundError} for Spring or HV classes — the bridge must not hard-depend
 *       on optional classpath entries in any code path triggered by a plain validator call.</li>
 *   <li>The non-HV fallback in {@code AbstractFvValidator}: violations are produced with a bare
 *       {@code {key}} message template, since parameter interpolation is HV-specific.</li>
 * </ol>
 */
class BValCompatibilityTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        // These guards make the test self-documenting: if HV or Spring ends up on the classpath
        // (e.g. via a transitive dep change), the test becomes meaningless and should fail loudly.
        assertThat(isPresent("org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext"))
            .as("HV must be absent — if it appeared, a transitive dep changed and this test is no longer meaningful")
            .isFalse();
        assertThat(isPresent("org.springframework.util.ClassUtils"))
            .as("spring-core must be absent — if it appeared, a transitive dep changed and this test is no longer meaningful")
            .isFalse();

        // Explicitly bootstrap BVal to avoid any accidental SPI-based HV discovery at runtime
        ValidatorFactory factory = jakarta.validation.Validation
            .byProvider(org.apache.bval.jsr.ApacheValidationProvider.class)
            .configure()
            .buildValidatorFactory();
        validator = factory.getValidator();
    }

    // -------------------------------------------------------------------------
    // Test fixtures
    // -------------------------------------------------------------------------

    @FvStaticRule(on = Item.class, field = "RULE")
    record Item(String name) {
        static final Rule<Item> RULE = strings.minLength(3).on(Item::name);
    }

    @FvRule(Parcel.Validator.class)
    record Parcel(String code, int weight) {
        public static class Validator implements Rule<Parcel> {
            private static final Rule<Parcel> IMPL = Rule.all(
                strings.minLength(3).on(Parcel::code),
                ints.atLeast(1).on(Parcel::weight)
            );

            @Override
            public Validation<Parcel> apply(Parcel p) { return IMPL.apply(p); }
        }
    }

    @FvRule(Crate.Validator.class)
    record Crate(String ref, List<BigDecimal> amounts) {
        public static class Validator implements Rule<Crate> {
            @Override
            public Validation<Crate> apply(Crate c) {
                return Rule.all(
                    strings.notBlank().on(Crate::ref),
                    lists.validateValuesWith(bigDecimals.positive()).on(Crate::amounts)
                ).apply(c);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Nested
    class WhenUsingFvStaticRule {

        @Test
        void valid_noViolations() {
            assertThat(validator.validate(new Item("abc"))).isEmpty();
        }

        @Test
        void invalid_violationWithBareKeyTemplate() {
            Set<ConstraintViolation<Item>> violations = validator.validate(new Item("ab"));

            assertThat(violations).hasSize(1);
            var v = violations.iterator().next();
            // Fallback path: no HV param interpolation, bare {key} template is the message
            assertThat(v.getMessage()).isEqualTo("{must.have.min.length}");
            assertThat(v.getPropertyPath().toString()).isEqualTo("name");
        }
    }

    @Nested
    class WhenUsingFvRule {

        @Test
        void valid_noViolations() {
            assertThat(validator.validate(new Parcel("abc", 5))).isEmpty();
        }

        @Test
        void singleViolation_correctPathAndMessage() {
            Set<ConstraintViolation<Parcel>> violations = validator.validate(new Parcel("ab", 5));

            assertThat(violations).hasSize(1);
            var v = violations.iterator().next();
            assertThat(v.getMessage()).isEqualTo("{must.have.min.length}");
            assertThat(v.getPropertyPath().toString()).isEqualTo("code");
        }

        @Test
        void multipleViolations_allReported() {
            Set<ConstraintViolation<Parcel>> violations = validator.validate(new Parcel("ab", 0));

            assertThat(violations).hasSize(2);
            assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("code", "weight");
        }

        @Test
        void nullValue_treatedAsValid() {
            record Wrapper(@FvRule(Parcel.Validator.class) Parcel p) {}
            assertThat(validator.validate(new Wrapper(null))).isEmpty();
        }
    }

    @Nested
    class WhenPathContainsListIndex {

        @Test
        void valid_noViolations() {
            assertThat(validator.validate(new Crate("REF", List.of(BigDecimal.ONE, BigDecimal.TEN)))).isEmpty();
        }

        @Test
        void negativeAmountAtIndex_violationPathIncludesIndex() {
            var crate = new Crate("REF", List.of(BigDecimal.ONE, new BigDecimal("-5"), BigDecimal.TEN));
            Set<ConstraintViolation<Crate>> violations = validator.validate(crate);

            assertThat(violations).hasSize(1);
            var v = violations.iterator().next();
            assertThat(v.getMessage()).isEqualTo("{must.be.positive}");
            assertThat(v.getPropertyPath().toString()).isEqualTo("amounts[1]");
        }

        @Test
        void multipleNegativeAmounts_allViolationsReported() {
            var crate = new Crate("REF", List.of(new BigDecimal("-1"), new BigDecimal("-2")));
            Set<ConstraintViolation<Crate>> violations = validator.validate(crate);

            assertThat(violations).hasSize(2);
            assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("amounts[0]", "amounts[1]");
        }

        @Test
        void blankRef_violationOnRefField() {
            var crate = new Crate("", List.of(BigDecimal.ONE));
            Set<ConstraintViolation<Crate>> violations = validator.validate(crate);

            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("ref");
        }
    }

    // -------------------------------------------------------------------------

    private static boolean isPresent(String className) {
        try {
            Class.forName(className, false, BValCompatibilityTest.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
