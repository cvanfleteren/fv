package be.iffy.fv.test.examples;

import be.iffy.fv.Rule;

import static be.iffy.fv.dsl.DSL.after;
import static be.iffy.fv.dsl.DSL.strings;
import static be.iffy.fv.rules.text.StringOps.stringOps;

//example of composing rules combined with transforming input
public class SepaRules {

    public static final Rule<String> validSepaPartyName = after(stringOps.trim()).is(strings.maxLength(70));

    public static final Rule<String> noDoubleSlash = Rule.of(input -> input.contains("//"), "double.slash.not.allowed");

    public static final Rule<String> noSlashAtEdges = Rule.both(
            Rule.of(input -> input.startsWith("/"), "starting.slash.not.allowed"),
            Rule.of(input -> input.endsWith("/"), "ending.slash.not.allowed")
    );

    public static final Rule<String> sepaSafe =after(stringOps.trim()).is(Rule.both(noSlashAtEdges, noDoubleSlash));

    public static final Rule<String> sepaSafeId = sepaSafe(35);

    public static Rule<String> sepaSafe(int maxLength) {
        return after(stringOps.trim()).is(Rule.all(
                strings.maxLength(maxLength),
                sepaSafe
        ));
    }

}