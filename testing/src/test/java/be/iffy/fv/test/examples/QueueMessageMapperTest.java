package be.iffy.fv.test.examples;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Validation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static be.iffy.fv.assertj.ValidationAssert.assertInvalid;
import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static org.assertj.core.api.Assertions.assertThat;

public class QueueMessageMapperTest {

    @Nested
    class validate {

        QueueMessage.Address validAddress = new QueueMessage.Address("some street", "123", "Melville", Optional.of("BE"));
        QueueMessage.MandateInfo validMandateInfo = new QueueMessage.MandateInfo("some-id", LocalDate.now(), true, Optional.of("CREDITORID"), Optional.of("foo"));

        QueueMessage.Debtor validDebtor = new QueueMessage.Debtor(
                "0831776978", "ABCDEF12XXX", "John Doe",
                validAddress,
                validMandateInfo
        );

        @Test
        void validate_whenValidInput_returnsValidCommand() {
            // arrange
            QueueMessage.Transaction transaction1 = new QueueMessage.Transaction(new BigDecimal("1000.00"));
            QueueMessage.Transaction transaction2 = new QueueMessage.Transaction(new BigDecimal("200.00"));
            QueueMessage message = new QueueMessage(validDebtor, "0123456789", List.of(transaction1, transaction2));

            // act
            Validation<Command> validation = message.validate();

            // assert
            assertThatValidation(validation).isValid().satisfies(command -> {
                        assertThat(command.debtor().name()).isEqualTo("John Doe");
                        assertThat(command.kboNumber().value()).isEqualTo("0123456789");
                        assertThat(command.transactions()).hasSize(2);
                        assertThat(command.transactions().get(0).amount().value()).isEqualByComparingTo("1000.00");
                    }
            );
        }

        @Test
        void validate_whenAllNull_fails() {
            // arrange
            QueueMessage.Debtor debtor = new QueueMessage.Debtor(null, null, null, null, null);
            QueueMessage message = new QueueMessage(debtor, "0123456789", null);

            // act & assert
            assertInvalid(message.validate())
                    .errorMessages()
                    .containsExactlyInAnyOrder(
                            "debtor.enterpriseNumber.must.not.be.null",
                            "debtor.bic.must.not.be.null",
                            "debtor.name.must.not.be.null",
                            "debtor.address.must.not.be.null",
                            "debtor.mandateInfo.must.not.be.null",
                            "transactions.must.not.be.null"
                    );
        }

        @Test
        void validate_whenLeafsInvalid_fails() {
            // arrange
            QueueMessage.Debtor debtor = new QueueMessage.Debtor("", "", "",
                    new QueueMessage.Address("", "", "", Optional.of("BE")),
                    new QueueMessage.MandateInfo("", null, null, Optional.empty(), null)
            );
            QueueMessage message = new QueueMessage(debtor, "", List.of(new QueueMessage.Transaction(new BigDecimal("-200.00"))));

            // act & assert
            assertInvalid(message.validate())
                    .errorMessages().containsExactlyInAnyOrder(
                            "debtor.enterpriseNumber.value.must.not.be.blank",
                            "debtor.bic.value.length.must.be.8.or.11",
                            "debtor.name.must.not.be.blank",
                            "debtor.address.street.must.not.be.blank",
                            "debtor.address.houseNumber.must.not.be.blank",
                            "debtor.address.city.must.not.be.blank",
                            "debtor.mandateInfo.amendmentIndicator.must.not.be.null",
                            "kboNumber.value.must.have.length",
                            "kboNumber.value.must.start.with",
                            "transactions[0].amount.must.be.positive"
                    );
        }

        @Test
        void validate_whenAEmptyList_fails() {
            // arrange
            QueueMessage message = new QueueMessage(validDebtor, "0123456789", List.of());

            // act & assert
            assertInvalid(message.validate())
                    .errorMessages().containsExactlyInAnyOrder(
                            "transactions.must.not.be.empty"
                    );
        }

        @Test
        void validate_whenInvalidDebtor_returnsInvalidWithErrors() {
            QueueMessage.Debtor debtor = new QueueMessage.Debtor("", "INVALID", " ", new QueueMessage.Address(" ", " ", "a", Optional.of("BE")), validMandateInfo);
            QueueMessage.Transaction transaction = new QueueMessage.Transaction(new BigDecimal("100.00"));
            QueueMessage message = new QueueMessage(debtor, "0123456789", List.of(transaction));

            Validation<Command> validation = message.validate();

            assertInvalid(validation)
                    .formattedMessages()
                    .containsExactlyInAnyOrder(
                            "debtor.enterpriseNumber.value.must.not.be.blank",
                            "debtor.bic.value.length.must.be.8.or.11",
                            "debtor.name.must.not.be.blank",
                            "debtor.address.street.must.not.be.blank",
                            "debtor.address.houseNumber.must.not.be.blank",
                            "debtor.address.city.must.have.min.length:{min:2}",
                            "transactions.one.must.be.at.least:{min:1000}"
                    );
        }

        @Test
        void validate_whenInvalidTransaction_returnsInvalidWithErrors() {
            QueueMessage.Debtor debtor = new QueueMessage.Debtor("0831776978", "ABCDEF12XXX", "John Doe", validAddress, validMandateInfo);
            // Command.Transaction has an internal assertThat(...) which throws if invalid.
            // When MappingRule.asRule(this::validateTransaction) is used, validateTransaction
            // calls Command.Transaction constructor.
            QueueMessage.Transaction transaction = new QueueMessage.Transaction(new BigDecimal("-10.00"));
            QueueMessage message = new QueueMessage(debtor, "0123456789", List.of(transaction));

            Validation<Command> validation = message.validate();

            assertInvalid(validation)
                    .formattedMessages()
                    .containsExactlyInAnyOrder(
                            "transactions[0].amount.must.be.positive"
                    );
        }

        @Test
        void validate_whenKboNumberBlank_returnsInvalidWithErrors() {

            QueueMessage.Debtor debtor = new QueueMessage.Debtor("0831776978", "ABCDEF12XXX", "John Doe", validAddress, validMandateInfo);
            QueueMessage message = new QueueMessage(debtor, "", List.of());

            Validation<Command> validation = message.validate();

            assertThat(validation.isInvalid()).isTrue();
            assertThatValidation(validation).isInvalid().hasErrorMessages(
                    "kboNumber.value.must.have.length",
                    "kboNumber.value.must.start.with"
            );
        }
    }
}
