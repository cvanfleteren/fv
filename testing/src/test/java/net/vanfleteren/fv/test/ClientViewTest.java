package net.vanfleteren.fv.test;

import io.vavr.Function2;
import io.vavr.Function3;
import io.vavr.control.Option;
import net.vanfleteren.fv.MappingRule;
import net.vanfleteren.fv.Validation;
import net.vanfleteren.fv.dsl.DSL;
import net.vanfleteren.fv.dsl.PropertySelector;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Function;

import static net.vanfleteren.fv.MappingRule.requiredOption;
import static net.vanfleteren.fv.dsl.DSL.assertAllValid;
import static net.vanfleteren.fv.dsl.DSL.validateThat;
import static net.vanfleteren.fv.dsl.experimental.ValidatingDSL.validating;
import static net.vanfleteren.fv.dsl.experimental.Validator.validatorFor;
import static net.vanfleteren.fv.rules.BooleanRules.booleans;
import static net.vanfleteren.fv.rules.ObjectRules.objects;
import static net.vanfleteren.fv.rules.text.StringRules.strings;

public class ClientViewTest {

    record TestDTO(LocalDate date, Boolean amendment, Option<String> amendmentType,
                   Option<String> originalValue) {
    }

    record MandateInfo(AmendmentType type, String originalValue) {
        enum AmendmentType {
            FOO, BAR;

            public static Validation<AmendmentType> from(String name) {
                return objects.isEnum(AmendmentType.class).test(name);
            }
        }
    }

    record Mandate(LocalDate date, Option<MandateInfo> mandateInfo) {
    }

    @Test
    void scenario_0() {
        record MonetaryAmount(BigDecimal value) {
            public MonetaryAmount {
                assertAllValid(
                    DSL.notNull(value, "value")
                );
            }
        }
    }

    @Test
    void scenario_1() {

        TestDTO testDTO = new TestDTO(LocalDate.now(), true, Option.of("FOO"), Option.of("original"));


        Function<TestDTO, Validation<Mandate>> mapper = testDto -> {
            MappingRule<Option<String>, MandateInfo.AmendmentType> amendmentTypeRule = requiredOption(objects().isEnum(MandateInfo.AmendmentType.class));
            MappingRule<Option<String>, String> originalValueRule = requiredOption(strings().notBlank());

            Validation<Boolean> amendmentV = objects().<Boolean>notNull().test(testDTO.amendment);


            return amendmentV.flatMap(value -> {
                if (value) {
                    Validation<Option<MandateInfo>> mandateInfo = validating(
                            validateThat(testDto.amendmentType).is(amendmentTypeRule),
                            validateThat(testDto.originalValue).is(originalValueRule)
                    ).map(MandateInfo::new).map(Option::of);

                    return validating(
                            validateThat(testDTO.date).is(objects.notNull()),
                            mandateInfo
                    ).map(Mandate::new);

                } else {
                    return Validation.from(() -> new Mandate(testDTO.date, Option.none()));
                }
            });
        };

    }

    @Test
    void scenario_2() {

        TestDTO testDTO = new TestDTO(LocalDate.now(), true, Option.of("FOO"), Option.of("original"));


        Function<TestDTO, Validation<Mandate>> mapper = testDto -> {
            MappingRule<Option<String>, MandateInfo.AmendmentType> amendmentTypeRule = requiredOption(objects().isEnum(MandateInfo.AmendmentType.class));
            MappingRule<Option<String>, String> originalValueRule = requiredOption(strings().notBlank());


            MappingRule<TestDTO, Option<MandateInfo>> withMandateInfo = properties(
                    property(TestDTO::amendment).is(booleans().isTrue()),
                    property(TestDTO::amendmentType).is(amendmentTypeRule),
                    property(TestDTO::originalValue).is(originalValueRule)
            ).map((a, type, original) -> new MandateInfo(type, original)).map(Option::of);

            MappingRule<TestDTO, Option<MandateInfo>> withoutMandateInfo = property(TestDTO::amendment).is(booleans().isFalse()).mapTo(Option.none());

            MappingRule<TestDTO, Mandate> foo = properties(
                    property(TestDTO::date).is(objects().notNull()),
                    withMandateInfo.orElse(withoutMandateInfo)
            ).map(Mandate::new);

            return foo.test(testDTO);
        };

    }

    @Test
    void scenario1() {

    }

    @Test
    void scenario_3() {

        TestDTO testDTO = new TestDTO(LocalDate.now(), true, Option.of("FOO"), Option.of("original"));

        Function<TestDTO, Validation<Mandate>> mapper = testDto -> {
            MappingRule<Option<String>, MandateInfo.AmendmentType> amendmentTypeRule = requiredOption(objects().isEnum(MandateInfo.AmendmentType.class));
            MappingRule<Option<String>, String> originalValueRule = requiredOption(strings().notBlank());

            MappingRule<TestDTO, Option<MandateInfo>> withMandateInfo = validatorFor(TestDTO.class)
                    .when(property(TestDTO::amendment).is(booleans().isTrue()))
                    .where(TestDTO::amendmentType, amendmentTypeRule)
                    .where(TestDTO::originalValue, originalValueRule)
                    .builds(MandateInfo::new).map(Option::of);

            MappingRule<TestDTO, Option<MandateInfo>> withoutMandateInfo = property(TestDTO::amendment).is(booleans().notNull()).mapTo(Option.none());

            MappingRule<TestDTO, Mandate> foo = validatorFor(TestDTO.class)
                    .where(TestDTO::date, objects().notNull())
                    .where(withMandateInfo.orElse(withoutMandateInfo))
                    .builds(Mandate::new);

            return foo.test(testDTO);
        };

        Validation<Mandate> result = mapper.apply(testDTO);
        assertAllValid(result);
    }


    static class PropertiesBuilder2<T, R1, R2> {
        MappingRule<T, R1> rule1;
        MappingRule<T, R2> rule2;

        public PropertiesBuilder2(MappingRule<T, R1> rule1, MappingRule<T, R2> rule2) {
            this.rule1 = rule1;
            this.rule2 = rule2;
        }

        public <X> MappingRule<T, X> map(Function2<R1, R2, X> mapper) {
            return t -> {
                Validation<R1> v1 = rule1.test(t);
                Validation<R2> v2 = rule2.test(t);
                return Validation.mapN(v1, v2, mapper);
            };
        }
    }

    static class PropertiesBuilder3<T, R1, R2, R3> {
        MappingRule<? super T, ? extends R1> rule1;
        MappingRule<? super T, ? extends R2> rule2;
        MappingRule<? super T, ? extends R3> rule3;

        public PropertiesBuilder3(MappingRule<? super T, ? extends R1> rule1,
                                  MappingRule<? super T, ? extends R2> rule2,
                                  MappingRule<? super T, ? extends R3> rule3) {
            this.rule1 = rule1;
            this.rule2 = rule2;
            this.rule3 = rule3;
        }

        public <X> MappingRule<T, X> map(Function3<? super R1, ? super R2, ? super R3, ? extends X> mapper) {
            return t -> {
                Validation<? extends R1> v1 = rule1.test(t);
                Validation<? extends R2> v2 = rule2.test(t);
                Validation<? extends R3> v3 = rule3.test(t);
                // Validation.mapN should handle the wildcards
                return Validation.mapN(v1, v2, v3, mapper);
            };
        }
    }


    private <T, R1, R2> PropertiesBuilder2<T, R1, R2> properties(MappingRule<T, R1> r1, MappingRule<T, R2> r2) {
        return new PropertiesBuilder2<>(r1, r2);
    }

    private <T, R1, R2, R3> PropertiesBuilder3<T, R1, R2, R3> properties(MappingRule<? super T, R1> r1,
                                                                         MappingRule<? super T, R2> r2,
                                                                         MappingRule<? super T, R3> r3) {
        return new PropertiesBuilder3<>(r1, r2, r3);
    }

    static class ThatBuilder<T, V> {
        final PropertySelector<T, V> propertySelector;

        public ThatBuilder(PropertySelector<T, V> propertySelector) {
            this.propertySelector = propertySelector;
        }

        public <R> MappingRule<T, R> is(MappingRule<V, ? super R> rule) {
            return input -> {
                V v = propertySelector.apply(input);
                return Validation.narrowSuper(rule.test(v).at(propertySelector.getPropertyName()));
            };
        }
    }

    static <T, V> ThatBuilder<T, V> property(PropertySelector<T, V> propertySelector) {
        return new ThatBuilder<>(propertySelector);
    }
}

