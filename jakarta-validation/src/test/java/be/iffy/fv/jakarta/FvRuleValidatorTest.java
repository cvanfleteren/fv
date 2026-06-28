package be.iffy.fv.jakarta;

import be.iffy.fv.Rule;
import be.iffy.fv.jakarta.support.*;
import jakarta.validation.*;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.HibernateMessageInterpolatorContext;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.*;
import java.math.BigDecimal;
import java.util.*;

import static be.iffy.fv.dsl.DSL.*;
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

        @Test
        void nonStaticField_clearlySaysFieldMustBeStatic() {
            class Holder {
                @SuppressWarnings("unused")
                public Rule<Object> INSTANCE_RULE = Rule.of(o -> true, "irrelevant");
            }

            assertThatThrownBy(() -> FvStaticRuleValidator.resolveRule(Holder.class, "INSTANCE_RULE").get())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be static");
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
    class WhenSameAnnotationIsRepeated {

        @FvRule(TwoFvRules.CodeValidator.class)
        @FvRule(TwoFvRules.CountValidator.class)
        record TwoFvRules(String code, int count) {
            static class CodeValidator implements Rule<TwoFvRules> {
                private static final Rule<TwoFvRules> IMPL = strings.minLength(3).on(TwoFvRules::code);
                @Override public be.iffy.fv.Validation<TwoFvRules> apply(TwoFvRules t) { return IMPL.apply(t); }
            }
            static class CountValidator implements Rule<TwoFvRules> {
                private static final Rule<TwoFvRules> IMPL = ints.atLeast(1).on(TwoFvRules::count);
                @Override public be.iffy.fv.Validation<TwoFvRules> apply(TwoFvRules t) { return IMPL.apply(t); }
            }
        }

        @Test
        void bothViolated_violationsFromBothAccumulated() {
            Set<ConstraintViolation<TwoFvRules>> violations = validator.validate(new TwoFvRules("AB", 0));

            assertThat(violations).hasSize(2);
            assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("code", "count");
        }

        @Test
        void onlyFirstViolated_onlyItsViolationReported() {
            Set<ConstraintViolation<TwoFvRules>> violations = validator.validate(new TwoFvRules("AB", 5));

            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("code");
        }

        @Test
        void bothPass_noViolations() {
            assertThat(validator.validate(new TwoFvRules("ABC", 1))).isEmpty();
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

    @Nested
    class WhenGroupsParameterIsUsed {

        interface StrictGroup {}

        @FvRule(value = GroupedItem.Validator.class, groups = StrictGroup.class)
        record GroupedItem(String code) {
            static class Validator implements Rule<GroupedItem> {
                private static final Rule<GroupedItem> IMPL = strings.minLength(3).on(GroupedItem::code);

                @Override
                public be.iffy.fv.Validation<GroupedItem> apply(GroupedItem g) { return IMPL.apply(g); }
            }
        }

        @Test
        void invalidObject_defaultGroup_constraintSkipped() {
            assertThat(validator.validate(new GroupedItem("A"))).isEmpty();
        }

        @Test
        void invalidObject_strictGroup_violationReported() {
            Set<ConstraintViolation<GroupedItem>> violations = validator.validate(new GroupedItem("A"), StrictGroup.class);

            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("code");
        }

        @Test
        void validObject_strictGroup_noViolations() {
            assertThat(validator.validate(new GroupedItem("ABC"), StrictGroup.class)).isEmpty();
        }
    }

    @Nested
    class WhenMessageParametersAreForwardedToHibernateValidator {

        private Validator capturingValidator(Map<String, Object> capture) {
            return Validation.byProvider(HibernateValidator.class)
                .configure()
                .messageInterpolator(new MessageInterpolator() {
                    @Override
                    public String interpolate(String template, MessageInterpolator.Context context) {
                        if (context instanceof HibernateMessageInterpolatorContext hCtx) {
                            capture.putAll(hCtx.getMessageParameters());
                        }
                        return template;
                    }

                    @Override
                    public String interpolate(String template, MessageInterpolator.Context context, Locale locale) {
                        return interpolate(template, context);
                    }
                })
                .buildValidatorFactory()
                .getValidator();
        }

        @Test
        void minLengthViolation_minParameterIsForwarded() {
            var captured = new HashMap<String, Object>();
            capturingValidator(captured).validate(new Person("A", 25));

            assertThat(captured).containsEntry("min", 2);
        }

        @Test
        void atLeastViolation_minParameterIsForwarded() {
            var captured = new HashMap<String, Object>();
            capturingValidator(captured).validate(new Person("Alice", 16));

            assertThat(captured).containsEntry("min", 18);
        }

        @Test
        void twoViolationsBothWithMinParameter_eachGetsOwnValue() {
            // Person("A", 16) produces two violations, both with a "min" parameter:
            //   name too short → min=2, age too low → min=18
            // Verifies HV snapshots parameters per violation rather than sharing mutable state.
            var capturedByTemplate = new HashMap<String, Object>();

            Validation.byProvider(HibernateValidator.class)
                .configure()
                .messageInterpolator(new MessageInterpolator() {
                    @Override
                    public String interpolate(String template, MessageInterpolator.Context context) {
                        if (context instanceof HibernateMessageInterpolatorContext hCtx) {
                            capturedByTemplate.put(template, hCtx.getMessageParameters().get("min"));
                        }
                        return template;
                    }

                    @Override
                    public String interpolate(String template, MessageInterpolator.Context context, Locale locale) {
                        return interpolate(template, context);
                    }
                })
                .buildValidatorFactory()
                .getValidator()
                .validate(new Person("A", 16));

            assertThat(capturedByTemplate)
                .containsEntry("{must.have.min.length}", 2)
                .containsEntry("{must.be.at.least}", 18);
        }
    }

    @Nested
    class WhenPathHasNonIntegerIndexedSegment {
        @Test
        void fullNested_invalidElementInList_pathIncludesIndexOnContainerNotLeaf() {
            var cart = new CartMap(Map.of(
                "a", new CartMap.Line("A", 5),
                "b", new CartMap.Line("B", 0)  // index 1, qty fails
            ));

            Set<ConstraintViolation<CartMap>> violations = validator.validate(cart);

            assertThat(violations).hasSize(1);
            var v = violations.iterator().next();
            assertThat(v.getPropertyPath().toString()).isEqualTo("map[b].qty");
        }

    }

    @Nested
    class WhenPathHasIntermediateIndexedSegment {

        @FvStaticRule(on=Carts.class, field="RULE")
        record Carts(@Valid List<Cart> carts) {

            public static Rule<Carts> RULE = lists.<Cart>minSize(1).on(Carts::carts);

        }

        // Regression test: the bridge previously dropped the index on non-terminal segments,
        // producing "lines.qty" instead of "lines[1].qty" for a List<Line> validated per-element.

        @Test
        void fullNested_invalidElementInList_pathIncludesIndexOnContainerNotLeaf() {
            var cart = new Cart(List.of(
                new Cart.Line("A", 5),
                new Cart.Line("B", 0)  // index 1, qty fails
            ));
            var carts = new Carts(List.of(cart));
            Set<ConstraintViolation<Carts>> violations = validator.validate(carts);

            assertThat(violations).hasSize(1);
            var v = violations.iterator().next();
            assertThat(v.getPropertyPath().toString()).isEqualTo("carts[0].lines[1].qty");
        }

        @Test
        void invalidElementInList_pathIncludesIndexOnContainerNotLeaf() {
            var cart = new Cart(List.of(
                new Cart.Line("A", 5),
                new Cart.Line("B", 0)  // index 1, qty fails
            ));
            Set<ConstraintViolation<Cart>> violations = validator.validate(cart);

            assertThat(violations).hasSize(1);
            var v = violations.iterator().next();
            assertThat(v.getPropertyPath().toString()).isEqualTo("lines[1].qty");
        }

        @Test
        void multipleInvalidElements_allPathsIncludeIndex() {
            var cart = new Cart(List.of(
                new Cart.Line("A", 0),
                new Cart.Line("B", 5),
                new Cart.Line("C", 0)
            ));
            Set<ConstraintViolation<Cart>> violations = validator.validate(cart);

            assertThat(violations).hasSize(2);
            assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("lines[0].qty", "lines[2].qty");
        }

        @Test
        void allElementsValid_noViolations() {
            var cart = new Cart(List.of(new Cart.Line("A", 1), new Cart.Line("B", 2)));
            assertThat(validator.validate(cart)).isEmpty();
        }
    }

    @Nested
    class WhenFvStaticRuleOmitsOn {

        // on() omitted: the annotated type is used automatically as the rule holder.
        @FvStaticRule(field = "RULE")
        record Snippet(String text, int count) {
            static final Rule<Snippet> RULE = Rule.all(
                strings.minLength(3).on(Snippet::text),
                ints.atLeast(1).on(Snippet::count)
            );
        }

        @Test
        void validSnippet_noViolations() {
            assertThat(validator.validate(new Snippet("abc", 1))).isEmpty();
        }

        @Test
        void textTooShort_violationReported() {
            Set<ConstraintViolation<Snippet>> violations = validator.validate(new Snippet("ab", 1));

            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("text");
        }

        @Test
        void multipleInvalid_allViolationsReported() {
            Set<ConstraintViolation<Snippet>> violations = validator.validate(new Snippet("ab", 0));

            assertThat(violations).hasSize(2);
            assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("text", "count");
        }
    }

    @Nested
    class WhenComposedAnnotationIsUsed {

        // A composed annotation: @ValidTag is just a shorthand for @FvRule(Tag.Validator.class).
        @FvRule(Tag.Validator.class)
        @Constraint(validatedBy = {})
        @Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
        @Retention(RetentionPolicy.RUNTIME)
        @interface ValidTag {
            String message() default "";
            Class<?>[] groups() default {};
            Class<? extends Payload>[] payload() default {};
        }

        @ValidTag
        record Tag(String name, int priority) {
            static class Validator implements Rule<Tag> {
                private static final Rule<Tag> IMPL = Rule.all(
                    strings.minLength(2).on(Tag::name),
                    ints.atLeast(1).on(Tag::priority)
                );
                @Override public be.iffy.fv.Validation<Tag> apply(Tag t) { return IMPL.apply(t); }
            }
        }

        @Test
        void validTag_noViolations() {
            assertThat(validator.validate(new Tag("ok", 1))).isEmpty();
        }

        @Test
        void nameTooShort_violationReported() {
            Set<ConstraintViolation<Tag>> violations = validator.validate(new Tag("x", 1));

            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("name");
        }

        @Test
        void multipleInvalid_allViolationsReported() {
            Set<ConstraintViolation<Tag>> violations = validator.validate(new Tag("x", 0));

            assertThat(violations).hasSize(2);
            assertThat(violations)
                .extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("name", "priority");
        }
    }

    @Nested
    class WhenAnnotationIsOnConstructorReturnValue {

        // Compact canonical constructor annotated for return-value validation.
        // @FvStaticRule on a constructor validates the constructed object, not a parameter.
        record Box(String label) {
            @FvStaticRule(on = Box.class, field = "RULE")
            Box {}

            static final Rule<Box> RULE = strings.minLength(3).on(Box::label);
        }

        @Test
        void invalidObject_violationsReported() throws NoSuchMethodException {
            var ctor = Box.class.getDeclaredConstructor(String.class);
            var violations = validator.forExecutables()
                .validateConstructorReturnValue(ctor, new Box("AB"));

            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getPropertyPath().toString()).endsWith("label");
        }

        @Test
        void validObject_noViolations() throws NoSuchMethodException {
            var ctor = Box.class.getDeclaredConstructor(String.class);
            var violations = validator.forExecutables()
                .validateConstructorReturnValue(ctor, new Box("ABC"));

            assertThat(violations).isEmpty();
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
