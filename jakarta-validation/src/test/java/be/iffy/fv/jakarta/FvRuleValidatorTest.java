package be.iffy.fv.jakarta;

import be.iffy.fv.jakarta.support.Gadget;
import be.iffy.fv.jakarta.support.Order;
import be.iffy.fv.jakarta.support.Person;
import be.iffy.fv.jakarta.support.Shipment;
import be.iffy.fv.jakarta.support.Widget;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link FvRuleValidator} using Hibernate Validator directly (no Spring).
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

        @Test
        void edgeCasePerson_exactMinValues_noViolations() {
            var violations = validator.validate(new Person("Al", 18));
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
    class WhenRuleProviderModeIsUsed {

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
    class WhenStaticFieldModeIsUsed {

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
    class WhenAnnotationConfigurationIsInvalid {

        @Test
        void noModeSpecified_throwsOnInitialization() {
            @FvRule
            record Empty(String x) {}

            assertThatThrownBy(() -> validator.validate(new Empty("hi")))
                .cause()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exactly one");
        }

        @Test
        void bothValueAndProvider_throwsOnInitialization() {
            @FvRule(value = Person.Validator.class, provider = Widget.Rules.class)
            record Conflicting(String x) {}

            assertThatThrownBy(() -> validator.validate(new Conflicting("hi")))
                .cause()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exactly one");
        }

        @Test
        void onWithoutField_throwsOnInitialization() {
            @FvRule(on = Gadget.class)
            record MissingField(String x) {}

            assertThatThrownBy(() -> validator.validate(new MissingField("hi")))
                .cause()
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void fieldWithUnknownName_throwsOnInitialization() {
            @FvRule(on = Gadget.class, field = "NONEXISTENT")
            record BadField(String x) {}

            assertThatThrownBy(() -> validator.validate(new BadField("hi")))
                .cause()
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NONEXISTENT");
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
}
