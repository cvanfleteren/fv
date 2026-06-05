package be.iffy.fv.test.examples;

import be.iffy.fv.Rule;

import java.util.List;

import static be.iffy.fv.dsl.DSL.*;
import static be.iffy.fv.rules.Rules.lists;

record StepChanged(String source,
                   ProcessingStatus status,
                   List<String> errors) {

    public StepChanged {
        assertAllValid(
                notNull(source, StepChanged::source),
                validateThat(errors,"errors").is(Rule.when(status == ProcessingStatus.SUCCESS, lists.empty())),
                validateThat(errors,"errors").is(Rule.when(status == ProcessingStatus.FAILED, lists.notEmpty())),
                validateThat(errors,"errors").is(Rule.choose(status == ProcessingStatus.FAILED, lists.notEmpty(), lists.empty()))
        );
    }

    public  enum ProcessingStatus {
        SUCCESS, FAILED
    }

}

