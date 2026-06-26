package com.lmmessage.rule;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RuleVariables {
    private final Map<String, String> values;

    private RuleVariables(Map<String, String> values) {
        this.values = Collections.unmodifiableMap(new LinkedHashMap<String, String>(values));
    }

    public static RuleVariables create(String playerName, String originalMessage, String outputMessage) {
        String original = originalMessage == null ? "" : originalMessage;
        String output = outputMessage == null ? "" : outputMessage;
        Map<String, String> values = new LinkedHashMap<String, String>();
        values.put("player", playerName == null ? "" : playerName);
        values.put("message", output);
        values.put("original", original);
        String[] split = output.trim().isEmpty() ? new String[0] : output.trim().split("\\s+");
        for (int index = 0; index < split.length; index++) {
            values.put("split" + index, split[index]);
        }
        return new RuleVariables(values);
    }

    public String get(String key) {
        String value = values.get(key);
        return value == null ? "" : value;
    }

    public Map<String, String> asMap() {
        return values;
    }
}
