package be.iffy.fv.test;

import io.vavr.collection.List;
import io.vavr.control.Option;
import be.iffy.fv.Validation;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static be.iffy.fv.dsl.DSL.*;
import static be.iffy.fv.dsl.experimental.ValidatingDSL.validating;
import static be.iffy.fv.rules.collections.CollectionRules.collections;
import static be.iffy.fv.rules.time.InstantRules.instants;
import static be.iffy.fv.rules.functional.OptionRules.options;
import static be.iffy.fv.rules.text.StringRules.*;

public class ValidationDSLTest {

    enum SomeStatus {
        READY, SUCCESS, FAILURE
    }

    record SomeClass(SomeStatus status, List<String> errors, Option<Instant> startedAt,
                     Option<Instant> finishedAt) {

        public SomeClass {
            assertAllValid(
                    notNull(status, SomeClass::status),
                    notNull(errors, SomeClass::errors),
                    notNull(startedAt, SomeClass::startedAt),
                    notNull(finishedAt, SomeClass::finishedAt)
            );

            switch (status) {
                case READY -> assertAllValid(
                        validateThat(startedAt, SomeClass::startedAt).is(options.empty()),
                        validateThat(finishedAt, SomeClass::finishedAt).is(options.empty()),
                        validateThat(errors, SomeClass::errors).is(collections().empty())
                );

                case SUCCESS -> assertAllValid(
                        validating(
                                validateThat(startedAt, SomeClass::startedAt).is(options.required()),
                                validateThat(finishedAt, SomeClass::finishedAt).is(options.required())
                        ).flatMap((s, f) -> validateThat(s).is(instants.isBefore(f))),
                        validateThat(errors, SomeClass::errors).is(collections().empty())
                );

                case FAILURE -> assertAllValid(
                        validating(
                                validateThat(startedAt, SomeClass::startedAt).is(options.required()),
                                validateThat(finishedAt, SomeClass::finishedAt).is(options.required())
                        ).flatMap((s, f) -> validateThat(s).is(instants.isBefore(f))),
                        validateThat(errors, SomeClass::errors).is(
                                collections().notEmpty().and(collections().allMatchRule(strings().notEmpty()))
                        )
                );
            }
        }
    }

    @Test
    void scenario() {
        assertThatValidation(Validation.from(() ->
                        new SomeClass(SomeStatus.FAILURE, List.of("some failure"), Option.of(Instant.now()), Option.of(Instant.now().plusSeconds(1)))
                )
        ).isValid();

        assertThatValidation(Validation.from(() ->
                        new SomeClass(SomeStatus.SUCCESS, List.of("some failure"), Option.of(Instant.now()), Option.of(Instant.now().plusSeconds(1)))
                )
        ).isInvalid().hasErrorMessages("errors.must.be.empty");
    }


    @Test
    void validating2_whenAllValid_returnsValidResult() {
        Validation<Integer> v1 = Validation.valid(1);
        Validation<Integer> v2 = Validation.valid(2);

        Validation<Integer> result = validating(v1, v2)
                .map(Integer::sum);

        assertThatValidation(result)
                .isValid()
                .hasValue(3);
    }

    @Test
    void validating3_whenAllValid_returnsValidResult() {
        Validation<Integer> v1 = Validation.valid(1);
        Validation<Integer> v2 = Validation.valid(2);
        Validation<Integer> v3 = Validation.valid(3);

        Validation<Integer> result = validating(v1, v2, v3)
                .map((a, b, c) -> a + b + c);

        assertThatValidation(result)
                .isValid()
                .hasValue(6);
    }


    @Test
    void validating4_whenAllValid_returnsValidResult() {
        Validation<Integer> v1 = Validation.valid(1);
        Validation<Integer> v2 = Validation.valid(2);
        Validation<Integer> v3 = Validation.valid(3);
        Validation<Integer> v4 = Validation.valid(4);

        Validation<Integer> result = validating(v1, v2, v3, v4)
                .map((a, b, c, d) -> a + b + c + d);

        assertThatValidation(result)
                .isValid()
                .hasValue(10);
    }

    @Test
    void validating5_whenAllValid_returnsValidResult() {
        Validation<Integer> v1 = Validation.valid(1);
        Validation<Integer> v2 = Validation.valid(2);
        Validation<Integer> v3 = Validation.valid(3);
        Validation<Integer> v4 = Validation.valid(4);
        Validation<Integer> v5 = Validation.valid(5);

        Validation<Integer> result = validating(v1, v2, v3, v4, v5)
                .map((a, b, c, d, e) -> a + b + c + d + e);

        assertThatValidation(result)
                .isValid()
                .hasValue(15);
    }

    @Test
    void validating6_whenAllValid_returnsValidResult() {
        Validation<Integer> v1 = Validation.valid(1);
        Validation<Integer> v2 = Validation.valid(2);
        Validation<Integer> v3 = Validation.valid(3);
        Validation<Integer> v4 = Validation.valid(4);
        Validation<Integer> v5 = Validation.valid(5);
        Validation<Integer> v6 = Validation.valid(6);

        Validation<Integer> result = validating(v1, v2, v3, v4, v5, v6)
                .map((a, b, c, d, e, f) -> a + b + c + d + e + f);

        assertThatValidation(result)
                .isValid()
                .hasValue(21);
    }

    @Test
    void validating7_whenAllValid_returnsValidResult() {
        Validation<Integer> v1 = Validation.valid(1);
        Validation<Integer> v2 = Validation.valid(2);
        Validation<Integer> v3 = Validation.valid(3);
        Validation<Integer> v4 = Validation.valid(4);
        Validation<Integer> v5 = Validation.valid(5);
        Validation<Integer> v6 = Validation.valid(6);
        Validation<Integer> v7 = Validation.valid(7);

        Validation<Integer> result = validating(v1, v2, v3, v4, v5, v6, v7)
                .map((a, b, c, d, e, f, g) -> a + b + c + d + e + f + g);

        assertThatValidation(result)
                .isValid()
                .hasValue(28);
    }

    @Test
    void validating8_whenAllValid_returnsValidResult() {
        Validation<Integer> v1 = Validation.valid(1);
        Validation<Integer> v2 = Validation.valid(2);
        Validation<Integer> v3 = Validation.valid(3);
        Validation<Integer> v4 = Validation.valid(4);
        Validation<Integer> v5 = Validation.valid(5);
        Validation<Integer> v6 = Validation.valid(6);
        Validation<Integer> v7 = Validation.valid(7);
        Validation<Integer> v8 = Validation.valid(8);

        Validation<Integer> result = validating(v1, v2, v3, v4, v5, v6, v7, v8)
                .map((a, b, c, d, e, f, g, h) -> a + b + c + d + e + f + g + h);

        assertThatValidation(result)
                .isValid()
                .hasValue(36);
    }

    @Test
    void validating8_whenOneInvalid_returnsInvalidResult() {
        Validation<Integer> v1 = Validation.valid(1);
        Validation<Integer> v2 = Validation.valid(2);
        Validation<Integer> v3 = Validation.valid(3);
        Validation<Integer> v4 = Validation.valid(4);
        Validation<Integer> v5 = Validation.valid(5);
        Validation<Integer> v6 = Validation.valid(6);
        Validation<Integer> v7 = Validation.valid(7);
        Validation<Integer> v8 = Validation.invalid("error");

        Validation<Integer> result = validating(v1, v2, v3, v4, v5, v6, v7, v8)
                .map((a, b, c, d, e, f, g, h) -> a + b + c + d + e + f + g + h);

        assertThatValidation(result)
                .isInvalid()
                .hasErrorKeys("error");
    }

    @Test
    void validating8_flatMap_whenAllValid_returnsValidResult() {
        Validation<Integer> v1 = Validation.valid(1);
        Validation<Integer> v2 = Validation.valid(2);
        Validation<Integer> v3 = Validation.valid(3);
        Validation<Integer> v4 = Validation.valid(4);
        Validation<Integer> v5 = Validation.valid(5);
        Validation<Integer> v6 = Validation.valid(6);
        Validation<Integer> v7 = Validation.valid(7);
        Validation<Integer> v8 = Validation.valid(8);

        Validation<Integer> result = validating(v1, v2, v3, v4, v5, v6, v7, v8)
                .flatMap((a, b, c, d, e, f, g, h) -> Validation.valid(a + b + c + d + e + f + g + h));

        assertThatValidation(result)
                .isValid()
                .hasValue(36);
    }
}
