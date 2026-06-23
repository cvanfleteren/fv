package be.iffy.fv.spring.support;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Validation;
import be.iffy.fv.ValidationException;
import io.vavr.collection.List;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static be.iffy.fv.dsl.DSL.assertThat;
import static be.iffy.fv.dsl.DSL.strings;

@RestController
public class TestController {

    public record ValidatedId(String value) {
        public ValidatedId {
            assertThat(value,"value").is(strings.minLength(3));
        }
    }

    @GetMapping("/get-with-validated-param")
    public String getWithValidatedParam(@RequestParam("id") ValidatedId id) {
        return "ok: " + id.value();
    }

    @GetMapping("/get-with-validated-path/{id}")
    public String getWithValidatedPathVariable(@PathVariable("id") ValidatedId id) {
        return "ok: " + id.value();
    }

    record SelfValidatingBody(String name, String email) {
        SelfValidatingBody {
            List<ErrorMessage> errors = List.empty();
            if (name == null || name.length() < 3) {
                errors = errors.append(ErrorMessage.of("min.length", "min", 3).prepend(ErrorMessage.Path.of("name")));
            }
            if (email == null || email.isBlank()) {
                errors = errors.append(ErrorMessage.of("must.not.be.blank").prepend(ErrorMessage.Path.of("email")));
            }
            if (!errors.isEmpty()) {
                throw new ValidationException(errors);
            }
        }
    }

    @PostMapping("/post-self-validating")
    public SelfValidatingBody postSelfValidating(@RequestBody SelfValidatingBody body) {
        return body;
    }

    record OuterBody(String outerName, SelfValidatingBody inner) {}

    @PostMapping("/post-with-nested-self-validating")
    public OuterBody postWithNestedSelfValidating(@RequestBody OuterBody body) {
        return body;
    }

    @GetMapping("/throw-single")
    public String throwSingle() {
        throw new ValidationException("must.not.be.blank");
    }

    @GetMapping("/throw-with-path-and-params")
    public String throwWithPathAndParams() {
        throw new ValidationException(List.of(
                ErrorMessage.of("min.length", "min", 3)
                        .prepend(ErrorMessage.Path.of("name"))
        ));
    }

    @GetMapping("/throw-multiple")
    public String throwMultiple() {
        throw new ValidationException(List.of(
                ErrorMessage.of("min.length", "min", 3)
                        .prepend(ErrorMessage.Path.of("name")),
                ErrorMessage.of("must.not.be.blank")
                        .prepend(ErrorMessage.Path.of("email"))
        ));
    }

    @GetMapping("/return-valid")
    public Validation<Map<String, String>> returnValid() {
        return new Validation.Valid<>(Map.of("message", "hello"));
    }

    @GetMapping("/return-invalid")
    public Validation<Map<String, String>> returnInvalid() {
        return new Validation.Invalid<>(List.of(
                ErrorMessage.of("must.not.be.blank")
                        .prepend(ErrorMessage.Path.of("email"))
        ));
    }

    @GetMapping("/return-invalid-multiple")
    public Validation<Map<String, String>> returnInvalidMultiple() {
        return new Validation.Invalid<>(List.of(
                ErrorMessage.of("min.length", "min", 3)
                        .prepend(ErrorMessage.Path.of("name")),
                ErrorMessage.of("must.not.be.blank")
                        .prepend(ErrorMessage.Path.of("email"))
        ));
    }

}
