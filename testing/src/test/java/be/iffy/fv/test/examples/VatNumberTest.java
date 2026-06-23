package be.iffy.fv.test.examples;

import be.iffy.fv.Validation;
import io.vavr.control.Try;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static org.assertj.core.api.Assertions.assertThat;

class VatNumberTest {

    @Nested
    class Of {

        @Test
        void of_whenValidVat_returnsValidVatNumber() {
            Validation<VatNumber> validation = VatNumber.of("BE12345678");

            assertThatValidation(validation).isValid().satisfies(vatNumber -> {
                assertThat(vatNumber.countrycode()).isEqualTo(CountryCode.BE);
                assertThat(vatNumber.value()).isEqualTo("12345678");
            });
        }

        @Test
        void of_whenInvalidCountryCode_isInvalid() {
            Validation<VatNumber> validation = VatNumber.of("ZZ12345678");

            assertThatValidation(validation)
                    .isInvalid()
                    .hasFormattedMessage("must.be.valid.enum.value:{value:ZZ}");
        }

        @Test
        void of_whenTooShortAfterSkip_isInvalid() {
            // skips first 2 ("BE"), then checks minLength(2). "BE1" -> "1" (length 1)
            Validation<VatNumber> validation = VatNumber.of("BE1");

            assertThatValidation(validation)
                    .isInvalid()
                    .hasFormattedMessage("must.have.min.length:{min:4}");
        }

        @Test
        void of_whenTooShortAfterSkipWithWhitespace_isInvalid() {
            Validation<VatNumber> validation = VatNumber.of("BE    1");

            assertThatValidation(validation)
                    .isInvalid()
                    .hasFormattedMessage("must.have.min.length:{min:4}");
        }

        @Test
        void of_whenNotAlphaNumeric_isStillValid() {
            Validation<VatNumber> validation = VatNumber.of("BE123-456");

            assertThatValidation(validation)
                    .isValid()
                    .isEqualTo(new VatNumber("123456",CountryCode.BE));
        }
    }

    @Nested
    class Constructor {

        @Test
        void constructor_whenValid_createsInstance() {
            VatNumber vat = new VatNumber("12345678", CountryCode.BE);
            assertThat(vat.value()).isEqualTo("12345678");
            assertThat(vat.countrycode()).isEqualTo(CountryCode.BE);
        }

        @Test
        void constructor_whenValueBlank_isInvalid() {
            assertThatValidation(
                    Validation.from().attempt(Try.of(() -> new VatNumber("", CountryCode.BE)))
            )
                    .isInvalid()
                    .hasErrorMessages("value.must.not.be.blank");
        }

        @Test
        void constructor_whenValueNotAlphaNumeric_isInvalid() {
            assertThatValidation(
                    Validation.from().attempt(Try.of(() -> new VatNumber("123-456", CountryCode.BE)))
            )
                    .isInvalid()
                    .hasErrorMessages("value.must.be.ascii.alphanumeric.only");
        }

        @Test
        void constructor_whenCountryCodeNull_isInvalid() {
            assertThatValidation(
                    Validation.from().attempt(Try.of(() -> new VatNumber("12345678", null)))
            )
                    .isInvalid()
                    .hasErrorMessages("countrycode.must.not.be.null");
        }
    }
}
