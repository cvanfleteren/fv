package be.iffy.fv.dsl.experimental;

import io.vavr.Function2;
import io.vavr.Function3;
import io.vavr.Function4;
import io.vavr.Function5;
import io.vavr.Function6;
import io.vavr.Function7;
import io.vavr.Function8;
import be.iffy.fv.MappingRule;
import be.iffy.fv.Validation;
import be.iffy.fv.dsl.PropertySelector;

import java.util.function.Function;
import java.util.function.Predicate;

public class Validator {

    static <T, V> ThatBuilder<T, V> property(PropertySelector<T, V> propertySelector) {
        return new ThatBuilder<>(propertySelector);
    }

    static class ThatBuilder<T, V> {
        private final PropertySelector<T, V> propertySelector;

        public ThatBuilder(PropertySelector<T, V> propertySelector) {
            this.propertySelector = propertySelector;
        }

        public <R> MappingRule<T, R> is(MappingRule<V, R> rule) {
            return input -> rule.test(propertySelector.apply(input)).at(propertySelector.getPropertyName());
        }
    }


    public static <T> ValidatorBuilder<T> validatorFor(Class<T> clazz) {
        return new ValidatorBuilder<>();
    }


    public static class ValidatorBuilder<T> {
        private final MappingRule<T, ?> predicate;

        public ValidatorBuilder() {
            this((MappingRule<T, ?>) Validation::valid);
        }

        public ValidatorBuilder(Predicate<T> predicate) {
            this((MappingRule<T, ?>) input -> predicate.test(input) ? Validation.valid(input) : Validation.invalid("Predicate failed"));
        }

        public ValidatorBuilder(MappingRule<T, ?> predicate) {
            this.predicate = predicate;
        }

        public ValidatorBuilder<T> when(Predicate<T> predicate) {
            return new ValidatorBuilder<>(predicate);
        }

        public ValidatorBuilder<T> when(MappingRule<T, ?> predicate) {
            return new ValidatorBuilder<>(predicate);
        }

        public <R, V> ValidatorBuilder1<T, R> where(PropertySelector<T, V> selector, MappingRule<V, R> rule) {
            return new ValidatorBuilder1<>(predicate, property(selector).is(rule));
        }

        public <R> ValidatorBuilder1<T, R> where(MappingRule<T, R> rule) {
            return new ValidatorBuilder1<>(predicate, rule);
        }
    }

    public static class ValidatorBuilder1<T, R1> {
        private final MappingRule<T, ?> predicate;
        private final MappingRule<T, R1> rule1;

        public ValidatorBuilder1(MappingRule<T, ?> predicate, MappingRule<T, R1> rule1) {
            this.predicate = predicate;
            this.rule1 = rule1;
        }

        public <R2, V> ValidatorBuilder2<T, R1, R2> where(PropertySelector<T, V> selector, MappingRule<V, R2> rule) {
            return new ValidatorBuilder2<>(predicate, rule1, property(selector).is(rule));
        }

        public <R2> ValidatorBuilder2<T, R1, R2> where(MappingRule<T, R2> rule) {
            return new ValidatorBuilder2<>(predicate, rule1, rule);
        }

        public <V> MappingRule<T, V> builds(Function<R1, V> mapper) {
            return input -> predicate.test(input).flatMapCatching(ignore -> {
                return rule1.test(input).map(mapper);
            });
        }
    }

    public static class ValidatorBuilder2<T, R1, R2> {
        private final MappingRule<T, ?> predicate;
        private final MappingRule<T, R1> rule1;
        private final MappingRule<T, R2> rule2;

        public ValidatorBuilder2(MappingRule<T, ?> predicate, MappingRule<T, R1> rule1, MappingRule<T, R2> rule2) {
            this.predicate = predicate;
            this.rule1 = rule1;
            this.rule2 = rule2;
        }

        public <R3, V> ValidatorBuilder3<T, R1, R2, R3> where(PropertySelector<T, V> selector, MappingRule<V, R3> rule) {
            return new ValidatorBuilder3<>(predicate, rule1, rule2, property(selector).is(rule));
        }

        public <R3> ValidatorBuilder3<T, R1, R2, R3> where(MappingRule<T, R3> rule) {
            return new ValidatorBuilder3<>(predicate, rule1, rule2, rule);
        }

        public <V> MappingRule<T, V> builds(Function2<R1, R2, V> mapper) {
            return input -> predicate.test(input).flatMapCatching(ignore -> {
                Validation<R1> r1 = rule1.test(input);
                Validation<R2> r2 = rule2.test(input);
                return Validation.mapN(r1, r2, mapper);
            });
        }
    }

    public static class ValidatorBuilder3<T, R1, R2, R3> {
        private final MappingRule<T, ?> predicate;
        private final MappingRule<T, R1> rule1;
        private final MappingRule<T, R2> rule2;
        private final MappingRule<T, R3> rule3;

        public ValidatorBuilder3(MappingRule<T, ?> predicate, MappingRule<T, R1> rule1, MappingRule<T, R2> rule2, MappingRule<T, R3> rule3) {
            this.predicate = predicate;
            this.rule1 = rule1;
            this.rule2 = rule2;
            this.rule3 = rule3;
        }

        public <V> MappingRule<T, V> builds(Function3<R1, R2, R3, V> mapper) {
            return input -> predicate.test(input).flatMapCatching(ignore -> {
                Validation<R1> r1 = rule1.test(input);
                Validation<R2> r2 = rule2.test(input);
                Validation<R3> r3 = rule3.test(input);
                return Validation.mapN(r1, r2, r3, mapper);
            });
        }

        public <R4, V> ValidatorBuilder4<T, R1, R2, R3, R4> constraint(PropertySelector<T, V> selector, MappingRule<V, R4> rule) {
            return new ValidatorBuilder4<>(predicate, rule1, rule2, rule3, property(selector).is(rule));
        }

        public <R4> ValidatorBuilder4<T, R1, R2, R3, R4> constraint(MappingRule<T, R4> rule) {
            return new ValidatorBuilder4<>(predicate, rule1, rule2, rule3, rule);
        }
    }

    public static class ValidatorBuilder4<T, R1, R2, R3, R4> {
        private final MappingRule<T, ?> predicate;
        private final MappingRule<T, R1> rule1;
        private final MappingRule<T, R2> rule2;
        private final MappingRule<T, R3> rule3;
        private final MappingRule<T, R4> rule4;

        public ValidatorBuilder4(MappingRule<T, ?> predicate, MappingRule<T, R1> rule1, MappingRule<T, R2> rule2, MappingRule<T, R3> rule3, MappingRule<T, R4> rule4) {
            this.predicate = predicate;
            this.rule1 = rule1;
            this.rule2 = rule2;
            this.rule3 = rule3;
            this.rule4 = rule4;
        }

        public <V> MappingRule<T, V> builds(Function4<R1, R2, R3, R4, V> mapper) {
            return input -> predicate.test(input).flatMapCatching(ignore -> {
                Validation<R1> r1 = rule1.test(input);
                Validation<R2> r2 = rule2.test(input);
                Validation<R3> r3 = rule3.test(input);
                Validation<R4> r4 = rule4.test(input);
                return Validation.mapN(r1, r2, r3, r4, mapper);
            });
        }

        public <R5, V> ValidatorBuilder5<T, R1, R2, R3, R4, R5> constraint(PropertySelector<T, V> selector, MappingRule<V, R5> rule) {
            return new ValidatorBuilder5<>(predicate, rule1, rule2, rule3, rule4, property(selector).is(rule));
        }

        public <R5> ValidatorBuilder5<T, R1, R2, R3, R4, R5> constraint(MappingRule<T, R5> rule) {
            return new ValidatorBuilder5<>(predicate, rule1, rule2, rule3, rule4, rule);
        }
    }

    public static class ValidatorBuilder5<T, R1, R2, R3, R4, R5> {
        private final MappingRule<T, ?> predicate;
        private final MappingRule<T, R1> rule1;
        private final MappingRule<T, R2> rule2;
        private final MappingRule<T, R3> rule3;
        private final MappingRule<T, R4> rule4;
        private final MappingRule<T, R5> rule5;

        public ValidatorBuilder5(MappingRule<T, ?> predicate, MappingRule<T, R1> rule1, MappingRule<T, R2> rule2, MappingRule<T, R3> rule3, MappingRule<T, R4> rule4, MappingRule<T, R5> rule5) {
            this.predicate = predicate;
            this.rule1 = rule1;
            this.rule2 = rule2;
            this.rule3 = rule3;
            this.rule4 = rule4;
            this.rule5 = rule5;
        }

        public <V> MappingRule<T, V> builds(Function5<R1, R2, R3, R4, R5, V> mapper) {
            return input -> predicate.test(input).flatMapCatching(ignore -> {
                Validation<R1> r1 = rule1.test(input);
                Validation<R2> r2 = rule2.test(input);
                Validation<R3> r3 = rule3.test(input);
                Validation<R4> r4 = rule4.test(input);
                Validation<R5> r5 = rule5.test(input);
                return Validation.mapN(r1, r2, r3, r4, r5, mapper);
            });
        }

        public <R6, V> ValidatorBuilder6<T, R1, R2, R3, R4, R5, R6> constraint(PropertySelector<T, V> selector, MappingRule<V, R6> rule) {
            return new ValidatorBuilder6<>(predicate, rule1, rule2, rule3, rule4, rule5, property(selector).is(rule));
        }

        public <R6> ValidatorBuilder6<T, R1, R2, R3, R4, R5, R6> constraint(MappingRule<T, R6> rule) {
            return new ValidatorBuilder6<>(predicate, rule1, rule2, rule3, rule4, rule5, rule);
        }
    }

    public static class ValidatorBuilder6<T, R1, R2, R3, R4, R5, R6> {
        private final MappingRule<T, ?> predicate;
        private final MappingRule<T, R1> rule1;
        private final MappingRule<T, R2> rule2;
        private final MappingRule<T, R3> rule3;
        private final MappingRule<T, R4> rule4;
        private final MappingRule<T, R5> rule5;
        private final MappingRule<T, R6> rule6;

        public ValidatorBuilder6(MappingRule<T, ?> predicate, MappingRule<T, R1> rule1, MappingRule<T, R2> rule2, MappingRule<T, R3> rule3, MappingRule<T, R4> rule4, MappingRule<T, R5> rule5, MappingRule<T, R6> rule6) {
            this.predicate = predicate;
            this.rule1 = rule1;
            this.rule2 = rule2;
            this.rule3 = rule3;
            this.rule4 = rule4;
            this.rule5 = rule5;
            this.rule6 = rule6;
        }

        public <V> MappingRule<T, V> builds(Function6<R1, R2, R3, R4, R5, R6, V> mapper) {
            return input -> predicate.test(input).flatMapCatching(ignore -> {
                Validation<R1> r1 = rule1.test(input);
                Validation<R2> r2 = rule2.test(input);
                Validation<R3> r3 = rule3.test(input);
                Validation<R4> r4 = rule4.test(input);
                Validation<R5> r5 = rule5.test(input);
                Validation<R6> r6 = rule6.test(input);
                return Validation.mapN(r1, r2, r3, r4, r5, r6, mapper);
            });
        }

        public <R7, V> ValidatorBuilder7<T, R1, R2, R3, R4, R5, R6, R7> constraint(PropertySelector<T, V> selector, MappingRule<V, R7> rule) {
            return new ValidatorBuilder7<>(predicate, rule1, rule2, rule3, rule4, rule5, rule6, property(selector).is(rule));
        }

        public <R7> ValidatorBuilder7<T, R1, R2, R3, R4, R5, R6, R7> constraint(MappingRule<T, R7> rule) {
            return new ValidatorBuilder7<>(predicate, rule1, rule2, rule3, rule4, rule5, rule6, rule);
        }
    }

    public static class ValidatorBuilder7<T, R1, R2, R3, R4, R5, R6, R7> {
        private final MappingRule<T, ?> predicate;
        private final MappingRule<T, R1> rule1;
        private final MappingRule<T, R2> rule2;
        private final MappingRule<T, R3> rule3;
        private final MappingRule<T, R4> rule4;
        private final MappingRule<T, R5> rule5;
        private final MappingRule<T, R6> rule6;
        private final MappingRule<T, R7> rule7;

        public ValidatorBuilder7(MappingRule<T, ?> predicate, MappingRule<T, R1> rule1, MappingRule<T, R2> rule2, MappingRule<T, R3> rule3, MappingRule<T, R4> rule4, MappingRule<T, R5> rule5, MappingRule<T, R6> rule6, MappingRule<T, R7> rule7) {
            this.predicate = predicate;
            this.rule1 = rule1;
            this.rule2 = rule2;
            this.rule3 = rule3;
            this.rule4 = rule4;
            this.rule5 = rule5;
            this.rule6 = rule6;
            this.rule7 = rule7;
        }

        public <V> MappingRule<T, V> builds(Function7<R1, R2, R3, R4, R5, R6, R7, V> mapper) {
            return input -> predicate.test(input).flatMapCatching(ignore -> {
                Validation<R1> r1 = rule1.test(input);
                Validation<R2> r2 = rule2.test(input);
                Validation<R3> r3 = rule3.test(input);
                Validation<R4> r4 = rule4.test(input);
                Validation<R5> r5 = rule5.test(input);
                Validation<R6> r6 = rule6.test(input);
                Validation<R7> r7 = rule7.test(input);
                return Validation.mapN(r1, r2, r3, r4, r5, r6, r7, mapper);
            });
        }

        public <R8, V> ValidatorBuilder8<T, R1, R2, R3, R4, R5, R6, R7, R8> constraint(PropertySelector<T, V> selector, MappingRule<V, R8> rule) {
            return new ValidatorBuilder8<>(predicate, rule1, rule2, rule3, rule4, rule5, rule6, rule7, property(selector).is(rule));
        }

        public <R8> ValidatorBuilder8<T, R1, R2, R3, R4, R5, R6, R7, R8> constraint(MappingRule<T, R8> rule) {
            return new ValidatorBuilder8<>(predicate, rule1, rule2, rule3, rule4, rule5, rule6, rule7, rule);
        }
    }

    public static class ValidatorBuilder8<T, R1, R2, R3, R4, R5, R6, R7, R8> {
        private final MappingRule<T, ?> predicate;
        private final MappingRule<T, R1> rule1;
        private final MappingRule<T, R2> rule2;
        private final MappingRule<T, R3> rule3;
        private final MappingRule<T, R4> rule4;
        private final MappingRule<T, R5> rule5;
        private final MappingRule<T, R6> rule6;
        private final MappingRule<T, R7> rule7;
        private final MappingRule<T, R8> rule8;

        public ValidatorBuilder8(MappingRule<T, ?> predicate, MappingRule<T, R1> rule1, MappingRule<T, R2> rule2, MappingRule<T, R3> rule3, MappingRule<T, R4> rule4, MappingRule<T, R5> rule5, MappingRule<T, R6> rule6, MappingRule<T, R7> rule7, MappingRule<T, R8> rule8) {
            this.predicate = predicate;
            this.rule1 = rule1;
            this.rule2 = rule2;
            this.rule3 = rule3;
            this.rule4 = rule4;
            this.rule5 = rule5;
            this.rule6 = rule6;
            this.rule7 = rule7;
            this.rule8 = rule8;
        }

        public <V> MappingRule<T, V> builds(Function8<R1, R2, R3, R4, R5, R6, R7, R8, V> mapper) {
            return input -> predicate.test(input).flatMapCatching(ignore -> {
                Validation<R1> r1 = rule1.test(input);
                Validation<R2> r2 = rule2.test(input);
                Validation<R3> r3 = rule3.test(input);
                Validation<R4> r4 = rule4.test(input);
                Validation<R5> r5 = rule5.test(input);
                Validation<R6> r6 = rule6.test(input);
                Validation<R7> r7 = rule7.test(input);
                Validation<R8> r8 = rule8.test(input);
                return Validation.mapN(r1, r2, r3, r4, r5, r6, r7, r8, mapper);
            });
        }
    }


}
