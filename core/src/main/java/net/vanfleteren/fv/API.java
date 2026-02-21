package net.vanfleteren.fv;

import io.vavr.collection.List;

import java.util.Objects;

public class API {


    public static <T> ValidateAllDSL<T> validateAll(List<T> values) {
        return new ValidateAllDSL<>(values);
    }

    public static class ValidateAllDSL<T> {
        private final List<T> values;

        public ValidateAllDSL(List<T> values) {
            this.values = Objects.requireNonNull(values);
        }

        public Validation<List<T>> areAll(Rule<T> rule) {
            Objects.requireNonNull(rule, "Rule cannot be null");
            return Validation.sequence(values.map(rule::test));
        }
    }

    public static <T> ValidationDSL<T> validateThat(T value) {
        return new ValidationDSL<>(value);
    }

    public static <T> ValidationDSL<T> validateThat(T value,String name) {
        return new ValidationDSL<>(value,name);
    }

    public static class ValidationDSL<T> {

        private final T value;
        private String name = "";

        public ValidationDSL(T value) {
            this.value = value;
        }

        public ValidationDSL(T value, String name) {
            this.value = value;
            this.name = name;
        }

        public Validation<T> is(Rule<T> rule) {
            return rule.test(value).at(name);
        }
    }

}
