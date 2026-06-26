package com.lmmessage.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RuleEvaluationResult {
    private final String originalMessage;
    private final String outputMessage;
    private final boolean cancelOriginal;
    private final List<RuleMatch> matches;
    private final RuleVariables variables;

    public RuleEvaluationResult(
            String originalMessage,
            String outputMessage,
            boolean cancelOriginal,
            List<RuleMatch> matches,
            RuleVariables variables
    ) {
        this.originalMessage = originalMessage == null ? "" : originalMessage;
        this.outputMessage = outputMessage == null ? "" : outputMessage;
        this.cancelOriginal = cancelOriginal;
        this.matches = Collections.unmodifiableList(new ArrayList<RuleMatch>(matches));
        this.variables = variables;
    }

    public String originalMessage() {
        return originalMessage;
    }

    public String outputMessage() {
        return outputMessage;
    }

    public boolean cancelOriginal() {
        return cancelOriginal;
    }

    public boolean matched() {
        return !matches.isEmpty();
    }

    public List<RuleMatch> matches() {
        return matches;
    }

    public RuleVariables variables() {
        return variables;
    }

    public List<String> commands() {
        List<String> commands = new ArrayList<String>();
        for (RuleMatch match : matches) {
            commands.addAll(match.rule().commands());
        }
        return commands;
    }
}
