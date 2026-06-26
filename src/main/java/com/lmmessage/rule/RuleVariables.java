package com.lmmessage.rule;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RuleVariables {
    public static final int DEFAULT_MAX_SPLIT_TOKENS = 32;

    private final Map<String, String> values;

    private RuleVariables(Map<String, String> values) {
        this.values = Collections.unmodifiableMap(new LinkedHashMap<String, String>(values));
    }

    public static RuleVariables create(String playerName, String originalMessage, String outputMessage) {
        return create(playerName, originalMessage, outputMessage, DEFAULT_MAX_SPLIT_TOKENS);
    }

    public static RuleVariables create(
            String playerName,
            String originalMessage,
            String outputMessage,
            int maxSplitTokens
    ) {
        String original = originalMessage == null ? "" : originalMessage;
        String output = outputMessage == null ? "" : outputMessage;
        Map<String, String> values = new LinkedHashMap<String, String>();
        values.put("player", playerName == null ? "" : playerName);
        values.put("message", output);
        values.put("original", original);
        addSplitVariables(values, output, maxSplitTokens);
        return new RuleVariables(values);
    }

    public String get(String key) {
        String value = values.get(key);
        return value == null ? "" : value;
    }

    public Map<String, String> asMap() {
        return values;
    }

    private static void addSplitVariables(Map<String, String> values, String output, int maxSplitTokens) {
        if (output == null || output.trim().isEmpty() || maxSplitTokens <= 0) {
            return;
        }
        int tokenIndex = 0;
        StringBuilder token = new StringBuilder();
        for (int index = 0; index < output.length() && tokenIndex < maxSplitTokens; index++) {
            char ch = output.charAt(index);
            if (Character.isWhitespace(ch)) {
                if (token.length() > 0) {
                    values.put("split" + tokenIndex, token.toString());
                    tokenIndex++;
                    token.setLength(0);
                }
                continue;
            }
            token.append(ch);
        }
        if (token.length() > 0 && tokenIndex < maxSplitTokens) {
            values.put("split" + tokenIndex, token.toString());
        }
    }
}
