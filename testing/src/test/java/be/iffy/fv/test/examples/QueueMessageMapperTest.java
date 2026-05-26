package be.iffy.fv.test.examples;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Validation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static be.iffy.fv.assertj.ValidationAssert.assertInvalid;
import static be.iffy.fv.assertj.ValidationAssert.assertThatValidation;
import static org.assertj.core.api.Assertions.assertThat;

public class QueueMessageMapperTest {

    @Nested
    class validate {

        QueueMessage.Debtor validDebtor = new QueueMessage.Debtor("BE0123456789", "ABCDEF12XXX", "John Doe");

        @Test
        void validate_whenValidInput_returnsValidCommand() {
            // arrange

            QueueMessage.Transaction transaction1 = new QueueMessage.Transaction(new BigDecimal("100.00"));
            QueueMessage.Transaction transaction2 = new QueueMessage.Transaction(new BigDecimal("200.00"));
            QueueMessage message = new QueueMessage(validDebtor, "KBO123", List.of(transaction1, transaction2));

            // act
            Validation<Command> validation = message.validate();

            // assert
            assertThatValidation(validation).isValid().satisfies(command -> {
                        assertThat(command.debtor().name()).isEqualTo("John Doe");
                        assertThat(command.kboNumber().value()).isEqualTo("KBO123");
                        assertThat(command.transactions()).hasSize(2);
                        assertThat(command.transactions().get(0).amount().value()).isEqualByComparingTo("100.00");
                    }
            );
        }


        @Test
        void validate_whenAllNull_fails() {
            // arrange
            QueueMessage.Debtor debtor = new QueueMessage.Debtor(null,null,null);
            QueueMessage message = new QueueMessage(debtor, "KBO123", null);

            // act & assert
            assertInvalid(message.validate())
                    .hasErrorCount(4)
                    .errorMessages().containsExactlyInAnyOrder(
                            "debtor.enterpriseNumber.must.not.be.null",
                            "debtor.bic.must.not.be.null",
                            "debtor.name.must.not.be.null",
                            "transactions.must.not.be.null"
                    );
        }

        @Test
        void validate_whenAllInvalid_fails() {
            // arrange
            QueueMessage.Debtor debtor = new QueueMessage.Debtor("","","");
            QueueMessage message = new QueueMessage(debtor, "", List.of(new QueueMessage.Transaction(new BigDecimal("-200.00"))));

            // act & assert
            assertInvalid(message.validate())
                    .errorMessages().containsExactlyInAnyOrder(
                            "debtor.enterpriseNumber.must.not.be.blank",
                            "debtor.bic.length.must.be.8.or.11",
                            "debtor.name.must.not.be.blank",
                            "kboNumber.must.not.be.blank",
                            "transactions[0].must.be.positive"
                    );
        }

        @Test
        void validate_whenAEmptyList_fails() {
            // arrange
            QueueMessage message = new QueueMessage(validDebtor, "kbo", List.of());

            // act & assert
            assertInvalid(message.validate())
                    .errorMessages().containsExactlyInAnyOrder(
                            "transactions.must.not.be.empty"
                    );
        }

        @Test
        void validate_whenInvalidDebtor_returnsInvalidWithErrors() {
            QueueMessage.Debtor debtor = new QueueMessage.Debtor("", "INVALID", " ");
            QueueMessage.Transaction transaction = new QueueMessage.Transaction(new BigDecimal("100.00"));
            QueueMessage message = new QueueMessage(debtor, "KBO123", List.of(transaction));

            Validation<Command> validation = message.validate();

            assertThat(validation.isInvalid()).isTrue();
            io.vavr.collection.List<ErrorMessage> errors = validation.errors();

            assertThat(errors.asJava()).extracting(ErrorMessage::key).containsExactlyInAnyOrder(
                    "must.not.be.blank", // enterpriseNumber
                    "length.must.be.8.or.11", // bic
                    "must.not.be.blank" // name
            );
        }

        @Test
        void validate_whenInvalidTransaction_returnsInvalidWithErrors() {
            QueueMessage.Debtor debtor = new QueueMessage.Debtor("BE0123456789", "ABCDEF12XXX", "John Doe");
            // Command.Transaction has an internal assertThat(...) which throws if invalid.
            // When MappingRule.asRule(this::validateTransaction) is used, validateTransaction
            // calls Command.Transaction constructor.
            QueueMessage.Transaction transaction = new QueueMessage.Transaction(new BigDecimal("-10.00"));
            QueueMessage message = new QueueMessage(debtor, "KBO123", java.util.List.of(transaction));

            Validation<Command> validation = message.validate();

            assertThat(validation.isInvalid()).isTrue();
            io.vavr.collection.List<ErrorMessage> errors = validation.errors();

            assertThat(errors.asJava()).extracting(ErrorMessage::key).contains(
                    "must.be.positive"
            );
        }

        @Test
        void validate_whenKboNumberBlank_returnsInvalidWithErrors() {
            QueueMessage.Debtor debtor = new QueueMessage.Debtor("BE0123456789", "ABCDEF12XXX", "John Doe");
            QueueMessage message = new QueueMessage(debtor, "", List.of());

            Validation<Command> validation = message.validate();

            assertThat(validation.isInvalid()).isTrue();
            assertThat(validation.errors().asJava()).extracting(ErrorMessage::key).contains("must.not.be.blank");
        }
    }
}
