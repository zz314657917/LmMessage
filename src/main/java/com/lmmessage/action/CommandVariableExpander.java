package com.lmmessage.action;

import com.lmmessage.rule.RuleVariables;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CommandVariableExpander {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([A-Za-z0-9_]+)}");
    private static final int MAX_VALUE_LENGTH = 128;

    public String expand(String command, RuleVariables variables) {
        if (command == null || command.isEmpty()) {
            return "";
        }
        Matcher matcher = VARIABLE_PATTERN.matcher(command);
        StringBuffer buffer = new StringBuffer();
        Map<String, String> values = variables == null ? java.util.Collections.<String, String>emptyMap() : variables.asMap();
        while (matcher.find()) {
            String value = values.get(matcher.group(1));
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(escapeCommandArgument(value)));
        }
        matcher.appendTail(buffer);
        return buffer.toString().trim();
    }

    String escapeCommandArgument(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (int index = 0; index < value.length() && count < MAX_VALUE_LENGTH; index++) {
            char ch = value.charAt(index);
            if (isAllowed(ch)) {
                builder.append(ch);
            } else if (!Character.isISOControl(ch)) {
                builder.append('_');
            }
            count++;
        }
        return builder.toString();
    }

    private boolean isAllowed(char ch) {
        return Character.isLetterOrDigit(ch)
                || ch == '_' || ch == '-' || ch == '.' || ch == ':' || ch == '/'
                || ch == '@' || ch == '#' || ch == '[' || ch == ']';
    }
}
