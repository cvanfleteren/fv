package be.iffy.fv.dsl.experimental;

import io.vavr.Function1;
import io.vavr.Lazy;
import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import be.iffy.fv.dsl.PropertySelector;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static io.vavr.API.Map;

public class FieldsDSL {

    public static <T> RootBuilder<T> ruleFor(Class<T> clazz) {
        return new Fields<>(clazz);
    }

    public interface RootBuilder<T> {

        WhenBuilder<T> when(Predicate<T> function);

        <V extends Comparable<V>> CompareBuilder<T, V> comparing(Function<T, V> left, String fieldName);

        <V extends Comparable<V>> CompareBuilder<T, V> comparing(PropertySelector<T, V> left);

    }

    public interface CompareBuilder<T, V extends Comparable<V>> {

        Rule<T> isEqualTo(Function<T, V> get, String fieldName);

        Rule<T> isEqualTo(PropertySelector<T, V> get);

        Rule<T> isLessThan(Function<T, V> get, String fieldName);

        Rule<T> isLessThan(PropertySelector<T, V> get);
    }


    public interface RequireBuilder<T> {
        default <V> Rule<T> require(Function<T, V> get, Rule<V> rule, String fieldName) {
            return input -> rule.test(get.apply(input)).mapTo(input).at(fieldName);
        }
    }

    public interface WhenBuilder<T> {

        <V extends Comparable<V>> CompareBuilder<T, V> comparing(Function<T, V> left, String fieldName);

        Rule<T> then(Function<RequireBuilder<T>, Rule<T>> ruleFunction);


        <V> EqualBuilder<T, V> thenField(Function1<T, V> ruleFunction, String fieldName);

    }

    public interface EqualBuilder<T, V> {

        Rule<T> isEqualTo(Function<T, V> get, String fieldName);

    }

    public static class Fields<T> implements RootBuilder<T>, WhenBuilder<T>, RequireBuilder<T> {
        Class<T> clazz;
        Predicate<T> when = i -> true;

        public Fields(Class<T> clazz) {
            this.clazz = clazz;
        }

        public Fields(Class<T> clazz, Predicate<T> when) {
            this.clazz = clazz;
            this.when = when;
        }

        @Override
        public <V extends Comparable<V>> CompareBuilder<T, V> comparing(Function<T, V> left, String fieldName) {
            return new CompareBuilderImpl<>(left, Lazy.of(() -> fieldName));
        }

        @Override
        public <V extends Comparable<V>> CompareBuilder<T, V> comparing(PropertySelector<T, V> left) {
            return new CompareBuilderImpl<>(left, Lazy.of(left::getPropertyName));
        }

        class CompareBuilderImpl<V extends Comparable<V>> implements CompareBuilder<T, V> {

            private final Function<T, V> left;
            private final Lazy<String> fieldName;

            public CompareBuilderImpl(Function<T, V> left, Lazy<String> fieldName) {
                this.left = left;
                this.fieldName = fieldName;
            }

            @Override
            public Rule<T> isEqualTo(Function<T, V> get, String otherFieldName) {
                return isEqualTo(get, Lazy.of(() -> otherFieldName));
            }

            @Override
            public Rule<T> isEqualTo(PropertySelector<T, V> get) {
               return isEqualTo(get, Lazy.of(get::getPropertyName));
            }

            Rule<T> isEqualTo(Function<T, V> get, Lazy<String> otherFieldName) {
                return input -> {
                    if (!when.test(input)) {
                        return Validation.valid(input);
                    }
                    if (Objects.equals(left.apply(input), get.apply(input))) {
                        return Validation.valid(input);
                    } else {
                        return Validation.invalid(ErrorMessage.of("fields.must.be.equal", Map("field1", fieldName.get(), "field2", otherFieldName.get())));
                    }
                };
            }

            @Override
            public Rule<T> isLessThan(Function<T, V> get, String otherFieldName) {
                return isLessThan(get, Lazy.of(() -> otherFieldName));
            }

            Rule<T> isLessThan(Function<T, V> get, Lazy<String> otherFieldName) {
                return input -> {
                    if (!when.test(input)) {
                        return Validation.valid(input);
                    }

                    if (left.apply(input).compareTo(get.apply(input)) < 0) {
                        return Validation.valid(input);
                    } else {
                        return Validation.invalid(ErrorMessage.of("must.be.less.than", Map("field1", fieldName.get(), "field2", otherFieldName.get())));
                    }
                };
            }

            @Override
            public Rule<T> isLessThan(PropertySelector<T, V> get) {
              return isLessThan(get, Lazy.of(get::getPropertyName));
            }
        }


        @Override
        public <V> EqualBuilder<T, V> thenField(Function1<T, V> ruleFunction, String fieldName) {
            return new EqualBuilder<T, V>() {
                @Override
                public Rule<T> isEqualTo(Function<T, V> get, String otherFieldName) {
                    return input -> {
                        if (Objects.equals(ruleFunction.apply(input), get.apply(input))) {
                            return Validation.valid(input);
                        } else {
                            return Validation.<T>invalid(ErrorMessage.of("fields.must.be.equal", Map("field1", fieldName, "field2", otherFieldName)));
                        }
                    };
                }
            };
        }

        @Override
        public WhenBuilder<T> when(Predicate<T> function) {
            return new Fields<>(clazz, function);
        }

        @Override
        public Rule<T> then(Function<RequireBuilder<T>, Rule<T>> ruleFunction) {
            Rule<T> ruleX = ruleFunction.apply(this);
            return input -> {
                if (when.test(input)) {
                    return ruleX.test(input).mapTo(input);
                } else {
                    return Validation.valid(input);
                }
            };
        }
    }
}
