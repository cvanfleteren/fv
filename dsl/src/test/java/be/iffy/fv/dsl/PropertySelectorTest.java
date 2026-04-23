package be.iffy.fv.dsl;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class PropertySelectorTest {

    static class Person {
        private String name;
        private boolean active;

        public String getName() {
            return name;
        }

        public boolean isActive() {
            return active;
        }
    }

    record User(String name, boolean active, String getter) {}

    static class Device {
        public String getURL() { return ""; }
    }

    @Test
    void getPropertyName_whenMethodIsRegularGetter_removesGetPrefix() {
        PropertySelector<Person, String> selector = Person::getName;
        assertThat(selector.getPropertyName()).isEqualTo("name");
    }

    @Test
    void getPropertyName_whenMethodIsBooleanGetter_removesIsPrefix() {
        PropertySelector<Person, Boolean> selector = Person::isActive;
        assertThat(selector.getPropertyName()).isEqualTo("active");
    }

    @Test
    void getPropertyName_whenMethodIsRecordComponent_returnsComponentName() {
        PropertySelector<User, String> selector = User::name;
        assertThat(selector.getPropertyName()).isEqualTo("name");
    }

    @Test
    void getPropertyName_whenMethodIsRecordComponentStartingWithGet_returnsComponentName() {
        PropertySelector<User, String> selector = User::getter;
        assertThat(selector.getPropertyName()).isEqualTo("getter");
    }

    @Test
    void getPropertyName_whenMethodIsRecordBooleanComponent_returnsComponentName() {
        PropertySelector<User, Boolean> selector = User::active;
        assertThat(selector.getPropertyName()).isEqualTo("active");
    }

    @Test
    void getPropertyName_whenMethodIsUpperCaseGetter_doesNotDecapitalize() {
        PropertySelector<Device, String> selector = Device::getURL;
        assertThat(selector.getPropertyName()).isEqualTo("URL");
    }
}
