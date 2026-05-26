package be.iffy.fv.test.examples;

import be.iffy.fv.MappingRule;
import be.iffy.fv.Validation;
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.util.List;

import static be.iffy.fv.dsl.DSL.validateThat;
import static be.iffy.fv.dsl.experimental.ValidatingDSL.validating;
import static be.iffy.fv.rules.Rules.strings;
import static be.iffy.fv.rules.collections.ListRules.lists;

@NullMarked
public record QueueMessage(Debtor debtor, String kboNumber, List<Transaction> transactions) {

    public record Debtor(String enterpriseNumber, String bic, String name) {
    }

    public record Transaction(BigDecimal amount) {
    }

    public Validation<Command> validate() {
        return validating(
                validateThat(this.debtor, "debtor").is(this::validateDebtor),
                validateThat(this.kboNumber, "kboNumber").mapsTo(KboNumber::new),
                validateThat(this.transactions, "transactions").is(
                        MappingRule.asMappingRule(this::validateTransaction)
                                .liftToList()
                                .andThen(lists.notEmpty())
                )
        ).map(Command::new);
    }

    Validation<Command.Transaction> validateTransaction(QueueMessage.Transaction transaction) {
        return Validation.from(() -> {
            MonetaryAmount amount = validateThat(transaction.amount, "amount").mapsTo(MonetaryAmount::new).getOrElseThrow();
            return new Command.Transaction(amount);
        });
    }

    Validation<Command.Debtor> validateDebtor(QueueMessage.Debtor debtor) {
        return validating(
                validateThat(debtor.enterpriseNumber, "enterpriseNumber").is(EnterpriseNumber::from),
                validateThat(debtor.bic, "bic").is(Bic::from),
                validateThat(debtor.name, "name").is(strings.notBlank())
        ).map(Command.Debtor::new);
    }
}
