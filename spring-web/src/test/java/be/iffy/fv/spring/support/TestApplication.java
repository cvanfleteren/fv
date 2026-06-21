package be.iffy.fv.spring.support;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;

@SpringBootApplication
public class TestApplication {

    @Bean
    public Converter<String, TestController.ValidatedId> validatedIdConverter() {
        return TestController.ValidatedId::new;
    }
}
