package com.lmmessage.action;

import com.lmmessage.rule.RuleVariables;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CommandVariableExpanderTest {
    @Test
    void expandsSplitVariablesAndEscapesUnsafeCharacters() {
        CommandVariableExpander expander = new CommandVariableExpander();
        RuleVariables variables = RuleVariables.create("Steve", "raw", "hello; stop");

        String expanded = expander.expand("say {player} {split0} {split1}", variables);

        assertEquals("say Steve hello_ stop", expanded);
    }
}
