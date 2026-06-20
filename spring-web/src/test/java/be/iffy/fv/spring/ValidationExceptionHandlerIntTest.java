package be.iffy.fv.spring;

import be.iffy.fv.spring.support.TestApplication;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
class ValidationExceptionHandlerIntTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext applicationContext;

    @Nested
    class AutoConfiguration {

        @Test
        void autoConfiguration_registersValidationExceptionHandlerBean() {
            assertThat(applicationContext.getBean(ValidationExceptionHandler.class)).isNotNull();
        }

        @Test
        void autoConfiguration_registersValidationReturnValueHandlerBean() {
            assertThat(applicationContext.getBean(ValidationReturnValueHandler.class)).isNotNull();
        }

        @Test
        void throwSingle_returns422WithProblemDetail() throws Exception {
            mockMvc.perform(get("/throw-single"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422))
                    .andExpect(jsonPath("$.title").value("Validation Failed"))
                    .andExpect(jsonPath("$.errors[0].key").value("must.not.be.blank"))
                    .andExpect(jsonPath("$.errors[0].path").value(""));
        }

        @Test
        void throwWithPathAndParams_mapsPathAndParametersCorrectly() throws Exception {
            mockMvc.perform(get("/throw-with-path-and-params"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.errors[0].key").value("min.length"))
                    .andExpect(jsonPath("$.errors[0].path").value("name"))
                    .andExpect(jsonPath("$.errors[0].parameters.min").value(3));
        }

        @Test
        void throwMultiple_returnsAllErrorsWithPaths() throws Exception {
            mockMvc.perform(get("/throw-multiple"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.errors.length()").value(2))
                    .andExpect(jsonPath("$.errors[0].key").value("min.length"))
                    .andExpect(jsonPath("$.errors[0].path").value("name"))
                    .andExpect(jsonPath("$.errors[1].key").value("must.not.be.blank"))
                    .andExpect(jsonPath("$.errors[1].path").value("email"));
        }
    }

    @Nested
    class WhenValidationIsReturnedFromController {

        @Test
        void returnValid_returns200WithSerializedValue() throws Exception {
            mockMvc.perform(get("/return-valid"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("hello"));
        }

        @Test
        void returnInvalid_returns422WithSameProblemDetailFormat() throws Exception {
            mockMvc.perform(get("/return-invalid"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422))
                    .andExpect(jsonPath("$.title").value("Validation Failed"))
                    .andExpect(jsonPath("$.errors[0].key").value("must.not.be.blank"))
                    .andExpect(jsonPath("$.errors[0].path").value("email"));
        }

        @Test
        void returnInvalidMultiple_returnsAllErrors() throws Exception {
            mockMvc.perform(get("/return-invalid-multiple"))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.errors.length()").value(2))
                    .andExpect(jsonPath("$.errors[0].key").value("min.length"))
                    .andExpect(jsonPath("$.errors[0].path").value("name"))
                    .andExpect(jsonPath("$.errors[1].key").value("must.not.be.blank"))
                    .andExpect(jsonPath("$.errors[1].path").value("email"));
        }
    }
}
