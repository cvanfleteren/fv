package be.iffy.fv.test.examples;

import be.iffy.fv.ErrorMessage;
import be.iffy.fv.Rule;
import be.iffy.fv.Validation;
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.util.List;

import static be.iffy.fv.dsl.DSL.validateThat;
import static be.iffy.fv.dsl.DSL.validateThatList;
import static be.iffy.fv.dsl.ValidatingDSL.validating;
import static be.iffy.fv.rules.Rules.*;

@NullMarked
public record QueueMessage(Debtor debtor, String kboNumber, List<Transaction> transactions) {

    public record Debtor(String enterpriseNumber, String bic, String name, Address address) {
    }

    public record Address(String street, String houseNumber, String city) {
    }

    public record Transaction(BigDecimal amount) {
    }

    public static final Rule<MonetaryAmount> atLeast1000 = Rule.with(MonetaryAmount::value, bigDecimals.atLeast(new BigDecimal(1000)));

    public Validation<Command> validate() {
        return validating(
                validateThat(this.debtor, "debtor").is(this::validateDebtor),
                validateThat(this.kboNumber, "kboNumber").mapsTo(KboNumber::new),
                validateThatList(this.transactions, "transactions")
                        .satisfies(lists.notEmpty())
                        .eachMapsTo(this::validateTransaction)
                        .satisfies(lists.anyMatch(
                                        Rule.with(Command.Transaction::amount, atLeast1000).toPredicate(),
                                        ErrorMessage.of("one.must.be.at.least", "min", 1000)
                                )
                        ).validate()
        ).map(Command::new);
    }

    Validation<Command.Transaction> validateTransaction(QueueMessage.Transaction transaction) {
        return Validation.from(() -> {
            MonetaryAmount amount = validateThat(transaction.amount, "amount").mapsTo(MonetaryAmount::new).getOrElseThrow();
            return new Command.Transaction(amount);
        });
    }

    Validation<Command.Address> validateAddress(QueueMessage.Address address) {
        return Validation.from(() -> new Command.Address(address.street, address.houseNumber, address.city));
    }

    Validation<Command.Debtor> validateDebtor(QueueMessage.Debtor debtor) {
        return validating(
                validateThat(debtor.enterpriseNumber, "enterpriseNumber").is(EnterpriseNumber::from),
                validateThat(debtor.bic, "bic").is(Bic::from),
                validateThat(debtor.name, "name").is(strings.notBlank()),
                validateThat(debtor.address, "address").is(this::validateAddress)
        ).map(Command.Debtor::new);
    }
}
