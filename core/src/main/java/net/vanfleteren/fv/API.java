package net.vanfleteren.fv;

import io.vavr.collection.Iterator;
import io.vavr.collection.List;

import java.util.Objects;

public class API {


    public static <T> ValidateAllDSL<T> validateAll(Iterable<T> values) {
        return new ValidateAllDSL<>(values);
    }

    public static class ValidateAllDSL<T> {
        private final Iterable<T> values;

        public ValidateAllDSL(Iterable<T> values) {
            this.values = Objects.requireNonNull(values);
        }

        public Validation<List<T>> areAll(Rule<? super T> rule) {
            Objects.requireNonNull(rule, "Rule cannot be null");
            return Validation.sequence(List.ofAll(values).map(v -> Validation.narrowSuper(rule.test(v))));
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

        public Validation<T> is(Rule<? super T> rule) {
            return Validation.narrowSuper(rule.test(value).at(name));
        }
    }

}
