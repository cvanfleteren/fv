package be.iffy.fv.test.examples;

import be.iffy.fv.Rule;

import java.util.List;

import static be.iffy.fv.dsl.DSL.assertThat;
import static be.iffy.fv.rules.Rules.bigDecimals;

public record Command(Debtor debtor, KboNumber kboNumber, List<Transaction> transactions) {

    public record Debtor(EnterpriseNumber enterpriseNumber, Bic bic, String name) {}

    public record Transaction(MonetaryAmount amount) {

        static final Rule<MonetaryAmount> positive = Rule.with(MonetaryAmount::value, bigDecimals.positive());

        public Transaction {
            amount = assertThat(amount,"amount").is(positive);
        }

    }

}
