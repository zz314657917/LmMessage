package com.lmmessage.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RuleEngine {
    public RuleEvaluationResult evaluate(
            String playerName,
            String message,
            List<RuleDefinition> messageRules,
            List<RuleDefinition> actionRules
    ) {
        return evaluate(playerName, message, messageRules, actionRules, RuleVariables.DEFAULT_MAX_SPLIT_TOKENS);
    }

    public RuleEvaluationResult evaluate(
            String playerName,
            String message,
            List<RuleDefinition> messageRules,
            List<RuleDefinition> actionRules,
            int maxSplitTokens
    ) {
        String original = message == null ? "" : message;
        String output = original;
        List<RuleMatch> matches = new ArrayList<RuleMatch>();
        boolean cancelOriginal = false;

        for (RuleDefinition rule : safeRules(messageRules)) {
            if (!rule.matches(original)) {
                continue;
            }
            output = rule.applyReplacement(output);
            RuleVariables variables = RuleVariables.create(playerName, original, output, maxSplitTokens);
            matches.add(new RuleMatch(rule, output, variables));
            cancelOriginal = cancelOriginal || rule.cancelOriginal();
            break;
        }

        for (RuleDefinition rule : safeRules(actionRules)) {
            if (!rule.matches(original)) {
                continue;
            }
            RuleVariables variables = RuleVariables.create(playerName, original, output, maxSplitTokens);
            matches.add(new RuleMatch(rule, output, variables));
            cancelOriginal = cancelOriginal || rule.cancelOriginal();
        }

        RuleVariables variables = RuleVariables.create(playerName, original, output, maxSplitTokens);
        return new RuleEvaluationResult(original, output, cancelOriginal, matches, variables);
    }

    private List<RuleDefinition> safeRules(List<RuleDefinition> rules) {
        return rules == null ? Collections.<RuleDefinition>emptyList() : rules;
    }
}
