package be.iffy.fv.test;

import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.Tuple4;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static be.iffy.fv.dsl.DSL.*;

public class ValidatingDSLTest {

    enum SomeStatus {
        READY, SUCCESS, FAILURE
    }

    record SomeClass(SomeStatus status, List<String> errors, Option<Instant> startedAt,
                     Option<Instant> finishedAt) {

        public SomeClass {
            asserting(
                    validateThat(status, SomeClass::status).isNotNull(),
                    validateThat(errors, SomeClass::errors).isNotNull(),
                    validateThat(startedAt, SomeClass::startedAt).isNotNull(),
                    validateThat(finishedAt, SomeClass::finishedAt).isNotNull()
            );

            switch (status) {
                case READY -> asserting(
                        validateThat(startedAt, SomeClass::startedAt).is(options.empty()),
                        validateThat(finishedAt, SomeClass::finishedAt).is(options.empty()),
                        validateThat(errors, SomeClass::errors).is(vavrLists.empty())
                );

                case SUCCESS -> asserting(
                        validating(
                                validateThat(startedAt, SomeClass::startedAt).is(options.required()),
                                validateThat(finishedAt, SomeClass::finishedAt).is(options.required())
                        ).flatMap((s, f) -> validateThat(s).is(instants.isBefore(f))),
                        validateThat(errors, SomeClass::errors).is(vavrLists.empty())
                );

                case FAILURE -> asserting(
                        validating(
                                validateThat(startedAt, SomeClass::startedAt).is(options.required()),
                                validateThat(finishedAt, SomeClass::finishedAt).is(options.required())
                        ).flatMap((s, f) -> validateThat(s).is(instants.isBefore(f))),
                        validateThat(errors, SomeClass::errors).is(
                                vavrLists.<String>notEmpty().and(vavrLists.allMatchRule(strings.notEmpty()))
                        )
                );
            }
        }
    }

    @Test
    void scenario() {
        assertThatValidation(Validation.from().catching(() ->
                        new SomeClass(SomeStatus.FAILURE, List.of("some failure"), Option.of(Instant.now()), Option.of(Instant.now().plusSeconds(1)))
                )
        ).isValid();

        assertThatValidation(Validation.from().catching(() ->
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
                .isEqualTo(3);
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
                .isEqualTo(6);
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
                .isEqualTo(10);
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
                .isEqualTo(15);
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
                .isEqualTo(21);
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
                .isEqualTo(28);
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
                .isEqualTo(36);
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
                .isEqualTo(36);
    }

    @Test
    void validating2_is_whenBothValidAndRuleSatisfied_returnsValidResult() {
        Validation<LocalDate> v1 = Validation.valid(LocalDate.of(2024, 1, 1));
        Validation<LocalDate> v2 = Validation.valid(LocalDate.of(2024, 1, 2));

        Validation<Tuple2<LocalDate, LocalDate>> result = validating(v1, v2)
                .is(pairs.<LocalDate>strictlyOrdered());

        assertThatValidation(result)
                .isValid()
                .isEqualTo(io.vavr.Tuple.of(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2)));
    }

    @Test
    void validating2_is_whenBothValidButRuleNotSatisfied_returnsInvalidResult() {
        Validation<LocalDate> v1 = Validation.valid(LocalDate.of(2024, 1, 2));
        Validation<LocalDate> v2 = Validation.valid(LocalDate.of(2024, 1, 1));

        Validation<Tuple2<LocalDate, LocalDate>> result = validating(v1, v2)
                .is(pairs.<LocalDate>strictlyOrdered());

        assertThatValidation(result)
                .isInvalid()
                .hasErrorKeys("first.must.be.before.second");
    }

    @Test
    void validating2_is_whenOneInvalid_neverEvaluatesRule_returnsInvalidResult() {
        Validation<LocalDate> v1 = Validation.invalid("date1.error");
        Validation<LocalDate> v2 = Validation.valid(LocalDate.of(2024, 1, 1));

        Validation<Tuple2<LocalDate, LocalDate>> result = validating(v1, v2)
                .is(pairs.<LocalDate>strictlyOrdered());

        assertThatValidation(result)
                .isInvalid()
                .hasErrorKeys("date1.error");
    }

    @Test
    void validating2_is_withAt_prependsNameToErrors() {
        Validation<LocalDate> v1 = Validation.valid(LocalDate.of(2024, 1, 2));
        Validation<LocalDate> v2 = Validation.valid(LocalDate.of(2024, 1, 1));

        Validation<Tuple2<LocalDate, LocalDate>> result = validating(v1, v2)
                .is(pairs.strictlyOrdered()).at("dateRange");

        assertThatValidation(result)
                .isInvalid()
                .hasErrorMessages("dateRange.first.must.be.before.second");
    }

    @Test
    void validating3_is_whenAllValidAndRuleSatisfied_returnsValidResult() {
        Validation<Integer> v1 = Validation.valid(10);
        Validation<Integer> v2 = Validation.valid(20);
        Validation<Integer> v3 = Validation.valid(30);

        Rule<Tuple3<Integer, Integer, Integer>> sumRule = satisfies((a, b, c) -> a + b == c, "sum.must.match");

        Validation<Tuple3<Integer, Integer, Integer>> result = validating(v1, v2, v3).is(sumRule);

        assertThatValidation(result)
                .isValid()
                .isEqualTo(io.vavr.Tuple.of(10, 20, 30));
    }

    @Test
    void validating3_is_whenAllValidButRuleNotSatisfied_returnsInvalidResult() {
        Validation<Integer> v1 = Validation.valid(10);
        Validation<Integer> v2 = Validation.valid(20);
        Validation<Integer> v3 = Validation.valid(999);

        Rule<Tuple3<Integer, Integer, Integer>> sumRule = satisfies((a, b, c) -> a + b == c, "sum.must.match");

        Validation<Tuple3<Integer, Integer, Integer>> result = validating(v1, v2, v3).is(sumRule);

        assertThatValidation(result)
                .isInvalid()
                .hasErrorKeys("sum.must.match");
    }

    @Test
    void validating3_is_whenOneInvalid_neverEvaluatesRule_returnsInvalidResult() {
        Validation<Integer> v1 = Validation.invalid("v1.error");
        Validation<Integer> v2 = Validation.valid(20);
        Validation<Integer> v3 = Validation.valid(30);

        Rule<Tuple3<Integer, Integer, Integer>> sumRule = satisfies((a, b, c) -> a + b == c, "sum.must.match");

        Validation<Tuple3<Integer, Integer, Integer>> result = validating(v1, v2, v3).is(sumRule);

        assertThatValidation(result)
                .isInvalid()
                .hasErrorKeys("v1.error");
    }

    @Test
    void validating4_is_whenAllValidAndRuleSatisfied_returnsValidResult() {
        Validation<Integer> v1 = Validation.valid(1);
        Validation<Integer> v2 = Validation.valid(2);
        Validation<Integer> v3 = Validation.valid(3);
        Validation<Integer> v4 = Validation.valid(6);

        Rule<Tuple4<Integer, Integer, Integer, Integer>> sumRule = Rule.of(
                t -> t._1 + t._2 + t._3 == t._4,
                "sum.must.match"
        );

        Validation<Tuple4<Integer, Integer, Integer, Integer>> result = validating(v1, v2, v3, v4).is(sumRule);

        assertThatValidation(result)
                .isValid()
                .isEqualTo(io.vavr.Tuple.of(1, 2, 3, 6));
    }

    @Test
    void validating4_is_whenAllValidButRuleNotSatisfied_returnsInvalidResult() {
        Validation<Integer> v1 = Validation.valid(1);
        Validation<Integer> v2 = Validation.valid(2);
        Validation<Integer> v3 = Validation.valid(3);
        Validation<Integer> v4 = Validation.valid(999);

        Rule<Tuple4<Integer, Integer, Integer, Integer>> sumRule = Rule.of(
                t -> t._1 + t._2 + t._3 == t._4,
                "sum.must.match"
        );

        Validation<Tuple4<Integer, Integer, Integer, Integer>> result = validating(v1, v2, v3, v4).is(sumRule);

        assertThatValidation(result)
                .isInvalid()
                .hasErrorKeys("sum.must.match");
    }

    @Test
    void satisfies3_withErrorKey_whenPredicateSatisfied_returnsValidResult() {
        Rule<Tuple3<Integer, Integer, Integer>> rule = satisfies((a, b, c) -> a + b == c, "sum.must.match");

        assertThatValidation(rule.apply(io.vavr.Tuple.of(1, 2, 3)))
                .isValid()
                .isEqualTo(io.vavr.Tuple.of(1, 2, 3));
    }

    @Test
    void satisfies3_withErrorKey_whenPredicateNotSatisfied_returnsInvalidResult() {
        Rule<Tuple3<Integer, Integer, Integer>> rule = satisfies((a, b, c) -> a + b == c, "sum.must.match");

        assertThatValidation(rule.apply(io.vavr.Tuple.of(1, 2, 999)))
                .isInvalid()
                .hasErrorKeys("sum.must.match");
    }

    @Test
    void satisfies3_withErrorMessage_whenPredicateSatisfied_returnsValidResult() {
        Rule<Tuple3<Integer, Integer, Integer>> rule =
                satisfies((a, b, c) -> a + b == c, be.iffy.fv.ErrorMessage.of("sum.must.match", "expected", "a+b"));

        assertThatValidation(rule.apply(io.vavr.Tuple.of(1, 2, 3)))
                .isValid()
                .isEqualTo(io.vavr.Tuple.of(1, 2, 3));
    }

    @Test
    void satisfies3_withErrorMessage_whenPredicateNotSatisfied_returnsInvalidResult() {
        Rule<Tuple3<Integer, Integer, Integer>> rule =
                satisfies((a, b, c) -> a + b == c, be.iffy.fv.ErrorMessage.of("sum.must.match", "expected", "a+b"));

        assertThatValidation(rule.apply(io.vavr.Tuple.of(1, 2, 999)))
                .isInvalid()
                .hasErrorKeys("sum.must.match");
    }
}
