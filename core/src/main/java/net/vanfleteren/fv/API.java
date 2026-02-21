package net.vanfleteren.fv;

public class API {


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
