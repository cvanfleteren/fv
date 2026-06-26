package be.iffy.fv.jakarta;

import be.iffy.fv.jakarta.support.Order;
import be.iffy.fv.jakarta.support.Person;
import be.iffy.fv.jakarta.support.SpringThing;
import be.iffy.fv.jakarta.support.TestApplication;
import be.iffy.fv.jakarta.support.TestService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = TestApplication.class)
class FvRuleSpringIntTest {

    @Autowired
    private TestService service;

    @Nested
    class WhenPersonIsValid {

        @Test
        void validPerson_returnsResult() {
            assertThat(service.enroll(new Person("Alice", 25))).isEqualTo("enrolled: Alice");
        }
    }

    @Nested
    class WhenPersonIsInvalid {

        @Test
        void singleViolation_throwsConstraintViolationException() {
            assertThatThrownBy(() -> service.enroll(new Person("A", 25)))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(ex -> {
                    var violations = ((ConstraintViolationException) ex).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    var v = violations.iterator().next();
                    assertThat(v.getMessage()).isEqualTo("{must.have.min.length}");
                    assertThat(v.getPropertyPath().toString()).endsWith("name");
                });
        }

        @Test
        void multipleViolations_allPropagatedTogether() {
            assertThatThrownBy(() -> service.enroll(new Person("A", 16)))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(ex -> {
                    var violations = ((ConstraintViolationException) ex).getConstraintViolations();
                    assertThat(violations).hasSize(2);
                    assertThat(violations)
                        .extracting(v -> v.getMessage())
                        .containsExactlyInAnyOrder("{must.have.min.length}", "{must.be.at.least}");
                });
        }
    }

    @Nested
    class WhenMixingFvRuleWithStandardBvConstraints {

        @Test
        void invalidPersonAndDegreesTooHigh_bothViolationsReported() {
            assertThatThrownBy(() -> service.enrollWithDegrees(new Person("A", 16), 5))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(ex -> {
                    var violations = ((ConstraintViolationException) ex).getConstraintViolations();
                    // 2 from @FvRule (name + age) and 1 from @Max(2)
                    assertThat(violations).hasSize(3);
                });
        }

        @Test
        void validPersonAndValidDegrees_noException() {
            assertThat(service.enrollWithDegrees(new Person("Alice", 25), 2))
                .isEqualTo("enrolled: Alice with 2 degrees");
        }

        @Test
        void validPersonAndDegreesTooHigh_onlyMaxViolation() {
            assertThatThrownBy(() -> service.enrollWithDegrees(new Person("Alice", 25), 5))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(ex -> {
                    var violations = ((ConstraintViolationException) ex).getConstraintViolations();
                    // Only the @Max violation on `degrees` — no @FvRule violations since Person is valid
                    assertThat(violations).hasSize(1);
                    assertThat(violations.iterator().next().getConstraintDescriptor().getAnnotation())
                        .isInstanceOf(jakarta.validation.constraints.Max.class);
                });
        }
    }

    @Nested
    class WhenSpringBeanModeIsUsed {

        @Test
        void validSpringThing_noException() {
            assertThat(service.processSpringThing(new SpringThing("hello"))).isEqualTo("processed: hello");
        }

        @Test
        void labelTooShort_violationReported() {
            assertThatThrownBy(() -> service.processSpringThing(new SpringThing("hi")))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(ex -> {
                    var violations = ((ConstraintViolationException) ex).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    assertThat(violations.iterator().next().getPropertyPath().toString()).endsWith("label");
                });
        }
    }

    @Nested
    class WhenOrderHasListWithInvalidElements {

        @Test
        void negativeAmount_violationIncludesListIndex() {
            var order = new Order("REF-001", List.of(BigDecimal.ONE, new BigDecimal("-5")));

            assertThatThrownBy(() -> service.placeOrder(order))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(ex -> {
                    var violations = ((ConstraintViolationException) ex).getConstraintViolations();
                    assertThat(violations).hasSize(1);
                    var v = violations.iterator().next();
                    assertThat(v.getMessage()).isEqualTo("{must.be.positive}");
                    assertThat(v.getPropertyPath().toString()).endsWith("amounts[1]");
                });
        }

        @Test
        void validOrder_noException() {
            assertThat(service.placeOrder(new Order("REF-001", List.of(BigDecimal.ONE, BigDecimal.TEN))))
                .isEqualTo("placed: REF-001");
        }
    }
}
