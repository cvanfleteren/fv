package be.iffy.fv.test.examples;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static be.iffy.fv.dsl.DSL.*;
import static be.iffy.fv.rules.Rules.*;

@NullMarked
public record QueueMessage(Debtor debtor, String kboNumber, List<Transaction> transactions) {

    public record Debtor(String enterpriseNumber, String bic, String name, Address address, MandateInfo mandateInfo) {
    }

    public record Address(String street, String houseNumber, String city, Optional<String> country) {
    }

    public record Transaction(BigDecimal amount) {
    }

    public record MandateInfo(String mandateId,
                              LocalDate dateOfSignature,
                              Boolean amendmentIndicator,
                              Optional<String> amendmentType,
                              Optional<String> amendmentOriginalValue
    ) {
    }

    public enum Country {
        BE, NL, LU
    }

    public static final Rule<MonetaryAmount> atLeast1000 = Rule.with(MonetaryAmount::value, bigDecimals.atLeast(new BigDecimal(1000)));

    public Validation<Command> validate() {
        return validating(
                validateThat(this.debtor, "debtor").is(this::validateDebtor),
                validateThat(this.kboNumber, "kboNumber").is(objects.canBe(KboNumber::new)),
                validateThatList(this.transactions, "transactions")
                        .is(lists.notEmpty())
                        .eachIs(this::validateTransaction)
                        .is(lists.anyMatch(
                                        Rule.with(Command.Transaction::amount, atLeast1000).toPredicate(),
                                        ErrorMessage.of("one.must.be.at.least", "min", 1000)
                                )
                        ).validate()
        ).map(Command::new);
    }

    Validation<Command.Transaction> validateTransaction(QueueMessage.Transaction transaction) {
        return Validation.from(() -> {
            MonetaryAmount amount = assertThat(transaction.amount, "amount").is(objects.canBe(MonetaryAmount::new, "must.be.monetaryAmount"));
            return new Command.Transaction(amount);
        });
    }

    Validation<Command.Address> validateAddress(QueueMessage.Address address) {
        return validateThat(address.country, "country")
                .is(optionals.matches(strings.asEnum(Country.class)))
                .flatMap(country ->
                        Validation.from(() -> new Command.Address(address.street, address.houseNumber, address.city))
                );
    }

    Validation<Command.Debtor> validateDebtor(QueueMessage.Debtor debtor) {
        return validating(
                validateThat(debtor.enterpriseNumber, "enterpriseNumber").is(EnterpriseNumber::from),
                validateThat(debtor.bic, "bic").is(Bic::from),
                validateThat(debtor.name, "name").is(strings.notBlank()),
                validateThat(debtor.address, "address").is(objects.notNull(Address.class).then(this::validateAddress)),
                validateThat(debtor.mandateInfo, "mandateInfo").is(this::validateMandateInfo)
        ).map(Command.Debtor::new);
    }

    Validation<Command.MandateInfo> validateMandateInfo(QueueMessage.MandateInfo mandateInfo) {

        Validation<Boolean> bV = validateThat(mandateInfo.amendmentIndicator(), QueueMessage.MandateInfo::amendmentIndicator).isNotNull();

        return bV.flatMap(amendmentIndicator -> {
            if (amendmentIndicator) {
                // two optional fields, but in this context they are required
                return validating(
                        validateThat(mandateInfo.amendmentType(), "amendmentType").is(optionals.required(strings.asEnum(Command.MandateInfo.AmendmentType.class))),
                        validateThat(mandateInfo.amendmentOriginalValue(), "amendmentOriginalValue").is(optionals.required())
                ).map((amendmentType, originalValue) ->
                        new Command.MandateInfo(mandateInfo.mandateId(),
                                mandateInfo.dateOfSignature(),
                                Optional.of(new Command.MandateInfo.MandateAmendment(amendmentType, originalValue))
                        )
                );
            } else {
                // no amendment indicator, ignore the other amendment fields
                return Validation.from(() -> new Command.MandateInfo(mandateInfo.mandateId(), mandateInfo.dateOfSignature(), Optional.empty()));
            }
        });
    }
}
