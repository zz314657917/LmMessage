package com.lmmessage.rule;

public final class RuleMatch {
    private final RuleDefinition rule;
    private final String outputMessage;
    private final RuleVariables variables;

    public RuleMatch(RuleDefinition rule, String outputMessage, RuleVariables variables) {
        this.rule = rule;
        this.outputMessage = outputMessage == null ? "" : outputMessage;
        this.variables = variables;
    }

    public RuleDefinition rule() {
        return rule;
    }

    public String outputMessage() {
        return outputMessage;
    }

    public RuleVariables variables() {
        return variables;
    }
}
