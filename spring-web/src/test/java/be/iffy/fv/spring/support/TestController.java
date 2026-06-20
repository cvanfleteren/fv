package be.iffy.fv.spring.support;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Validation;
import be.iffy.fv.ValidationException;
import io.vavr.collection.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestController {

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
