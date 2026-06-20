package be.iffy.fv.spring;

import be.iffy.fv.spring.support.TestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ValidationExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new ValidationExceptionHandler())
                .build();
    }

    @Nested
    class WhenSingleErrorIsThrown {

        @Test
        void handleValidationException_returns422() throws Exception {
            mockMvc.perform(get("/throw-single"))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        void handleValidationException_setsStatusFieldInBody() throws Exception {
            mockMvc.perform(get("/throw-single"))
                    .andExpect(jsonPath("$.status").value(422));
        }

        @Test
        void handleValidationException_setsTitleField() throws Exception {
            mockMvc.perform(get("/throw-single"))
                    .andExpect(jsonPath("$.title").value("Validation Failed"));
        }

        @Test
        void handleValidationException_setsErrorKeyInErrorsField() throws Exception {
            mockMvc.perform(get("/throw-single"))
                    .andExpect(jsonPath("$.errors[0].key").value("must.not.be.blank"));
        }

        @Test
        void handleValidationException_noPath_setsEmptyPathInErrorsField() throws Exception {
            mockMvc.perform(get("/throw-single"))
                    .andExpect(jsonPath("$.errors[0].path").value(""));
        }
    }

    @Nested
    class WhenErrorHasPathAndParameters {

        @Test
        void handleValidationException_mapsPathFromErrorMessage() throws Exception {
            mockMvc.perform(get("/throw-with-path-and-params"))
                    .andExpect(jsonPath("$.errors[0].path").value("name"));
        }

        @Test
        void handleValidationException_mapsKeyWithoutPath() throws Exception {
            mockMvc.perform(get("/throw-with-path-and-params"))
                    .andExpect(jsonPath("$.errors[0].key").value("min.length"));
        }

        @Test
        void handleValidationException_mapsParameters() throws Exception {
            mockMvc.perform(get("/throw-with-path-and-params"))
                    .andExpect(jsonPath("$.errors[0].parameters.min").value(3));
        }
    }

    @Nested
    class WhenMultipleErrorsAreThrown {

        @Test
        void handleValidationException_returnsAllErrors() throws Exception {
            mockMvc.perform(get("/throw-multiple"))
                    .andExpect(jsonPath("$.errors.length()").value(2));
        }

        @Test
        void handleValidationException_firstErrorHasCorrectKeyAndPath() throws Exception {
            mockMvc.perform(get("/throw-multiple"))
                    .andExpect(jsonPath("$.errors[0].key").value("min.length"))
                    .andExpect(jsonPath("$.errors[0].path").value("name"));
        }

        @Test
        void handleValidationException_secondErrorHasCorrectKeyAndPath() throws Exception {
            mockMvc.perform(get("/throw-multiple"))
                    .andExpect(jsonPath("$.errors[1].key").value("must.not.be.blank"))
                    .andExpect(jsonPath("$.errors[1].path").value("email"));
        }
    }

    @Nested
    class WhenRequestBodyDeserializationFails {

        @Test
        void selfValidatingConstructorFails_returns422() throws Exception {
            mockMvc.perform(post("/post-self-validating")
                            .contentType("application/json")
                            .content("""
                                    {"name": "Al", "email": ""}
                                    """))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        void selfValidatingConstructorFails_returnsProblemDetail() throws Exception {
            mockMvc.perform(post("/post-self-validating")
                            .contentType("application/json")
                            .content("""
                                    {"name": "Al", "email": ""}
                                    """))
                    .andExpect(jsonPath("$.title").value("Validation Failed"))
                    .andExpect(jsonPath("$.errors.length()").value(2))
                    .andExpect(jsonPath("$.errors[0].key").value("min.length"))
                    .andExpect(jsonPath("$.errors[0].path").value("name"))
                    .andExpect(jsonPath("$.errors[1].key").value("must.not.be.blank"))
                    .andExpect(jsonPath("$.errors[1].path").value("email"));
        }

        @Test
        void malformedJson_returns400() throws Exception {
            mockMvc.perform(post("/post-self-validating")
                            .contentType("application/json")
                            .content("not json at all"))
                    .andExpect(status().isBadRequest());
        }
    }
}
