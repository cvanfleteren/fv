package be.iffy.fv.jakarta;

import be.iffy.fv.Rule;
import be.iffy.fv.jakarta.support.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static be.iffy.fv.dsl.DSL.ints;
import static be.iffy.fv.dsl.DSL.strings;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link FvRuleValidator}, {@link FvStaticRuleValidator}, and
 * {@link FvRuleBeanValidator} using Hibernate Validator directly (no Spring).
 */
class FvRuleValidatorTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Nested
    class WhenObjectIsValid {

        @Test
        void validPerson_noViolations() {
            var violations = validator.validate(new Person("Alice", 25));
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    class WhenObjectIsInvalid {

        @Test
        void singleViolation_nameTooShort() {
            Set<ConstraintViolation<Person>> violations = validator.validate(new Person("A", 25));

            assertThat(violations).hasSize(1);
            var v = violations.iterator().next();
            assertThat(v.getMessage()).isEqualTo("{must.have.min.length}");
            assertThat(v.getPropertyPath().toString()).isEqualTo("name");
        }

        @Test
        void multipleViolations_allReported() {
            Set<ConstraintViolation<Person>> violations = validator.validate(new Person("A", 16));

            assertThat(violations).hasSize(2);
            assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("name", "age");
            assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .containsExactlyInAnyOrder("{must.have.min.length}", "{must.be.at.least}");
        }
    }

    @Nested
    class WhenNullIsProvided {

        @Test
        void nullValue_treatedAsValid() {
            // @FvRule delegates null handling to @NotNull — null itself is valid per BV convention
            record NullableWrapper(@FvRule(Person.Validator.class) Person person) {}
            var violations = validator.validate(new NullableWrapper(null));
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    class WhenPathContainsIndex {

        @Test
        void invalidAmountInList_violationIncludesIndex() {
            var order = new Order("REF-001", List.of(BigDecimal.ONE, new BigDecimal("-5"), BigDecimal.TEN));
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            assertThat(violations).hasSize(1);
            var v = violations.iterator().next();
            assertThat(v.getMessage()).isEqualTo("{must.be.positive}");
            // BV path: amounts[1]
            assertThat(v.getPropertyPath().toString()).isEqualTo("amounts[1]");
        }

        @Test
        void blankReference_violationOnReferenceField() {
            var order = new Order("", List.of(BigDecimal.ONE));
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("reference");
        }

        @Test
        void multipleInvalidAmounts_allReported() {
            var order = new Order("REF-002", List.of(new BigDecimal("-1"), new BigDecimal("-2")));
            Set<ConstraintViolation<Order>> violations = validator.validate(order);

            assertThat(violations).hasSize(2);
            assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("amounts[0]", "amounts[1]");
        }
    }

    @Nested
    class WhenRuleProviderIsUsedWithFvRule {

        @Test
        void validWidget_noViolations() {
            assertThat(validator.validate(new Widget("Cog", 5))).isEmpty();
        }

        @Test
        void nameTooShort_violation() {
            Set<ConstraintViolation<Widget>> violations = validator.validate(new Widget("Co", 5));

            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("name");
        }

        @Test
        void multipleInvalid_allViolationsReported() {
            Set<ConstraintViolation<Widget>> violations = validator.validate(new Widget("Co", 0));

            assertThat(violations).hasSize(2);
            assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("name", "weight");
        }
    }

    @Nested
    class WhenFvStaticRuleIsUsed {

        @Test
        void validGadget_noViolations() {
            assertThat(validator.validate(new Gadget("ABC", 3))).isEmpty();
        }

        @Test
        void codeTooShort_violation() {
            Set<ConstraintViolation<Gadget>> violations = validator.validate(new Gadget("AB", 3));

            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("code");
        }

        @Test
        void multipleInvalid_allViolationsReported() {
            Set<ConstraintViolation<Gadget>> violations = validator.validate(new Gadget("AB", 0));

            assertThat(violations).hasSize(2);
            assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("code", "quantity");
        }
    }

    @Nested
    class WhenObjectHasNestedFvRuleObject {

        @Test
        void validShipment_noViolations() {
            var violations = validator.validate(new Shipment("TRK-001", new Person("Alice", 25)));
            assertThat(violations).isEmpty();
        }

        @Test
        void invalidNestedPerson_violationsIncludeNestedPath() {
            Set<ConstraintViolation<Shipment>> violations = validator.validate(
                new Shipment("TRK-001", new Person("A", 16)));

            assertThat(violations).hasSize(2);
            assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("recipient.name", "recipient.age");
        }

        @Test
        void bothLevelsInvalid_allViolationsReported() {
            Set<ConstraintViolation<Shipment>> violations = validator.validate(
                new Shipment("TRK", new Person("A", 16)));

            assertThat(violations).hasSize(3);
            assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("trackingNumber", "recipient.name", "recipient.age");
        }

        @Test
        void validNestedPerson_outerViolationOnly() {
            Set<ConstraintViolation<Shipment>> violations = validator.validate(
                new Shipment("TRK", new Person("Alice", 25)));

            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("trackingNumber");
        }
    }

    @Nested
    class WhenAtValidCascadesToCollection {

        @Test
        void invalidElementInList_violationIncludesIndex() {
            record Roster(@Valid List<@Valid Person> members) {}

            var violations = validator.validate(new Roster(List.of(
                new Person("Alice", 25),
                new Person("A", 16)
            )));

            assertThat(violations).hasSize(2);
            assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("members[1].name", "members[1].age");
        }

        @Test
        void allValidElements_noViolations() {
            record Roster(@Valid List<@Valid Person> members) {}

            var violations = validator.validate(new Roster(List.of(
                new Person("Alice", 25),
                new Person("Bob", 30)
            )));

            assertThat(violations).isEmpty();
        }
    }

    @Nested
    class WhenFvRuleAnnotationIsMisconfigured {

        @Test
        void classNeitherRuleNorRuleProvider_throwsOnInitialization() {
            @FvRule(Object.class)
            record BadClass(String x) {}

            assertThatThrownBy(() -> validator.validate(new BadClass("hi")))
                .cause()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must implement Rule or RuleProvider");
        }

        @Test
        void privateConstructor_throwsOnInitialization() {
            assertThatThrownBy(() -> FvRuleValidator.resolveRule(PrivateCtorRule.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no-arg constructor");
        }
    }

    @Nested
    class WhenFvStaticRuleAnnotationIsMisconfigured {

        @Test
        void emptyFieldName_throwsOnInitialization() {
            @FvStaticRule(on = Gadget.class, field = "")
            record EmptyField(String x) {}

            assertThatThrownBy(() -> validator.validate(new EmptyField("hi")))
                .cause()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("non-empty field name");
        }

        @Test
        void fieldWithUnknownName_throwsOnInitialization() {
            @FvStaticRule(on = Gadget.class, field = "NONEXISTENT")
            record BadField(String x) {}

            assertThatThrownBy(() -> validator.validate(new BadField("hi")))
                .cause()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NONEXISTENT");
        }
    }

    @Nested
    class WhenFvRuleBeanAnnotationIsMisconfigured {

        @Test
        void beanModeWithoutSpring_throwsRequiresBeanFactory() {
            // In plain BV (no Spring), beanFactory is null — must fail with a clear message
            @FvRuleBean(SpringThing.Validator.class)
            record NoBeanFactory(String x) {}

            assertThatThrownBy(() -> validator.validate(new NoBeanFactory("hi")))
                .cause()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("BeanFactory");
        }
    }

    @Nested
    class WhenMultipleFvAnnotationsAreOnSameType {

        @FvRule(TwoRules.NameValidator.class)
        @FvStaticRule(on = TwoRules.class, field = "AGE_RULE")
        record TwoRules(String name, int age) {
            static final Rule<TwoRules> AGE_RULE = ints.atLeast(18).on(TwoRules::age);

            static class NameValidator implements Rule<TwoRules> {
                private static final Rule<TwoRules> IMPL = strings.minLength(2).on(TwoRules::name);

                @Override
                public be.iffy.fv.Validation<TwoRules> apply(TwoRules t) { return IMPL.apply(t); }
            }
        }

        @Test
        void bothViolated_violationsFromBothAccumulated() {
            Set<ConstraintViolation<TwoRules>> violations = validator.validate(new TwoRules("A", 16));

            assertThat(violations).hasSize(2);
            assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("name", "age");
        }

        @Test
        void onlyOneViolated_onlyItsViolationReported() {
            Set<ConstraintViolation<TwoRules>> violations = validator.validate(new TwoRules("A", 25));

            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("name");
        }

        @Test
        void bothPass_noViolations() {
            assertThat(validator.validate(new TwoRules("Alice", 25))).isEmpty();
        }
    }

    @Nested
    class WhenMessageParametersAreInterpolated {

        private final Validator interpolatingValidator = Validation
            .byProvider(HibernateValidator.class)
            .configure()
            .messageInterpolator(new ResourceBundleMessageInterpolator(
                new PlatformResourceBundleLocator("TestMessages")))
            .buildValidatorFactory()
            .getValidator();

        @Test
        void nameTooShort_messageIncludesMinParameter() {
            // Person.Validator uses strings.minLength(2), which produces param {min: 2}
            Set<ConstraintViolation<Person>> violations = interpolatingValidator.validate(new Person("A", 25));

            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Must have at least 2 character(s)");
        }

        @Test
        void ageTooLow_messageIncludesMinParameter() {
            // ints.atLeast(18) produces param {min: 18}
            Set<ConstraintViolation<Person>> violations = interpolatingValidator.validate(new Person("Alice", 16));

            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Must be at least 18");
        }

        @Test
        void negativeAmount_messageIsPositive() {
            var order = new Order("REF-001", List.of(new BigDecimal("-5")));
            Set<ConstraintViolation<Order>> violations = interpolatingValidator.validate(order);

            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Must be positive");
        }
    }

    /** Helper: a Rule implementation with a private constructor, used to test instantiation errors. */
    private static class PrivateCtorRule implements Rule<Object> {
        private PrivateCtorRule() {}

        @Override
        public be.iffy.fv.Validation<Object> apply(Object o) {
            return be.iffy.fv.Validation.valid(o);
        }
    }
}
