package be.iffy.fv.test.examples;

import be.iffy.fv.Rule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static be.iffy.fv.dsl.DSL.*;

public record Command(Debtor debtor, KboNumber kboNumber, List<Transaction> transactions) {

    public record Debtor(EnterpriseNumber enterpriseNumber, Bic bic, String name, Address address,
                         MandateInfo mandateInfo) {
        public Debtor {
            assertValid(
                    notNull(enterpriseNumber, Debtor::enterpriseNumber),
                    notNull(bic, Debtor::bic),
                    notNull(name, Debtor::name),
                    notNull(address, Debtor::address)
            );
        }
    }

    public record Address(String street, String houseNumber, String city) {
        public Address {
            var result = assertValid(
                    validateThat(street, QueueMessage.Address::street).map(stringOps.trim()).is(strings.notBlank()),
                    validateThat(houseNumber, QueueMessage.Address::houseNumber).map(stringOps.trim()).is(strings.notBlank()),
                    validateThat(city, QueueMessage.Address::city).map(stringOps.trim()).is(strings.notBlank().and(strings.minLength(2)))
            );
            street = result._1;
            houseNumber = result._2;
            city = result._3;
        }
    }

    public record Transaction(MonetaryAmount amount) {

        public static final Rule<MonetaryAmount> positive = Rule.on(MonetaryAmount::value, bigDecimals.positive());

        public Transaction {
            amount = assertThat(amount, "amount").is(positive);
        }

    }

    public record MandateInfo(String id, LocalDate dateOfSignature, Optional<MandateInfo.MandateAmendment> amendment) {

        public record MandateAmendment(MandateInfo.AmendmentType amendmentType, String originalValue) {
        }

        public enum AmendmentType {
            CREDITORID, DEBTORACCOUNT
        }

    }

}
