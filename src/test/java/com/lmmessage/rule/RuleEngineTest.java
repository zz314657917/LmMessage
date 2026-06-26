package com.lmmessage.rule;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RuleEngineTest {
    private final RuleEngine ruleEngine = new RuleEngine();

    @Test
    void startRuleMatchesAndReplacesPrefix() {
        RuleDefinition rule = RuleDefinition.builder(RuleTriggerType.START)
                .name("notice")
                .triggerText("[提示]")
                .replacePrefix("[提示]")
                .channel("notice")
                .cancelOriginal(true)
                .build();

        RuleEvaluationResult result = ruleEngine.evaluate(
                "Steve",
                "[提示] hello world",
                Collections.singletonList(rule),
                Collections.<RuleDefinition>emptyList()
        );

        assertTrue(result.matched());
        assertTrue(result.cancelOriginal());
        assertEquals("hello world", result.outputMessage());
        assertEquals("hello", result.variables().get("split0"));
        assertEquals("world", result.variables().get("split1"));
    }

    @Test
    void containRuleMatchesOriginalMessageAfterMessageReplacement() {
        RuleDefinition messageRule = RuleDefinition.builder(RuleTriggerType.START)
                .triggerText("!")
                .replacePrefix("!")
                .build();
        RuleDefinition actionRule = RuleDefinition.builder(RuleTriggerType.CONTAIN)
                .triggerText("run")
                .commands(Collections.singletonList("console:say {split0}"))
                .build();

        RuleEvaluationResult result = ruleEngine.evaluate(
                "Alex",
                "!run now",
                Collections.singletonList(messageRule),
                Collections.singletonList(actionRule)
        );

        assertTrue(result.matched());
        assertEquals("run now", result.outputMessage());
        assertEquals(1, result.commands().size());
        assertEquals("run", result.variables().get("split0"));
    }

    @Test
    void disabledOrEmptyRulesDoNotMatch() {
        RuleDefinition disabled = RuleDefinition.builder(RuleTriggerType.START)
                .enabled(false)
                .triggerText("!")
                .build();
        RuleDefinition empty = RuleDefinition.builder(RuleTriggerType.CONTAIN)
                .triggerText("")
                .build();

        RuleEvaluationResult result = ruleEngine.evaluate(
                "Steve",
                "!hello",
                Arrays.asList(disabled),
                Arrays.asList(empty)
        );

        assertFalse(result.matched());
        assertFalse(empty.isValid());
        assertEquals("!hello", result.outputMessage());
    }
}
