package com.lmmessage.config;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public final class SecuritySettings {
    public static final int DEFAULT_MAX_MESSAGE_CHARS = 512;
    public static final int DEFAULT_MAX_MESSAGE_BYTES = 1024;
    public static final int DEFAULT_MAX_SPLIT_TOKENS = 32;
    public static final int DEFAULT_MAX_RULES = 100;
    public static final int DEFAULT_MAX_TRIGGER_LENGTH = 64;
    public static final int DEFAULT_MAX_ACTION_COMMANDS_PER_RULE = 5;
    public static final int DEFAULT_MAX_COMMAND_LENGTH = 256;
    public static final int DEFAULT_MAX_PACKET_ITEMS = 8;

    private final Set<String> allowedCommandPrefixes;
    private final boolean opPrefixEnabled;
    private final boolean logCommandActions;
    private final int maxMessageChars;
    private final int maxMessageBytes;
    private final int maxSplitTokens;
    private final int maxRules;
    private final int maxTriggerLength;
    private final int maxActionCommandsPerRule;
    private final int maxCommandLength;
    private final int maxPacketItems;

    public SecuritySettings(
            Set<String> allowedCommandPrefixes,
            boolean opPrefixEnabled,
            boolean logCommandActions,
            int maxMessageChars,
            int maxMessageBytes,
            int maxSplitTokens,
            int maxRules,
            int maxTriggerLength,
            int maxActionCommandsPerRule,
            int maxCommandLength,
            int maxPacketItems
    ) {
        LinkedHashSet<String> normalized = new LinkedHashSet<String>();
        if (allowedCommandPrefixes != null) {
            for (String prefix : allowedCommandPrefixes) {
                if (prefix == null || prefix.trim().isEmpty()) {
                    continue;
                }
                String normalizedPrefix = prefix.trim().toLowerCase(Locale.ROOT);
                if (!"op".equals(normalizedPrefix)) {
                    normalized.add(normalizedPrefix);
                }
            }
        }
        this.allowedCommandPrefixes = Collections.unmodifiableSet(normalized);
        this.opPrefixEnabled = false;
        this.logCommandActions = logCommandActions;
        this.maxMessageChars = clamp(maxMessageChars, 1, 4096, DEFAULT_MAX_MESSAGE_CHARS);
        this.maxMessageBytes = clamp(maxMessageBytes, 1, 8192, DEFAULT_MAX_MESSAGE_BYTES);
        this.maxSplitTokens = clamp(maxSplitTokens, 1, 128, DEFAULT_MAX_SPLIT_TOKENS);
        this.maxRules = clamp(maxRules, 1, 500, DEFAULT_MAX_RULES);
        this.maxTriggerLength = clamp(maxTriggerLength, 1, 256, DEFAULT_MAX_TRIGGER_LENGTH);
        this.maxActionCommandsPerRule = clamp(maxActionCommandsPerRule, 0, 20, DEFAULT_MAX_ACTION_COMMANDS_PER_RULE);
        this.maxCommandLength = clamp(maxCommandLength, 1, 2048, DEFAULT_MAX_COMMAND_LENGTH);
        this.maxPacketItems = clamp(maxPacketItems, 1, 32, DEFAULT_MAX_PACKET_ITEMS);
    }

    public Set<String> allowedCommandPrefixes() {
        return allowedCommandPrefixes;
    }

    public boolean opPrefixEnabled() {
        return opPrefixEnabled;
    }

    public boolean logCommandActions() {
        return logCommandActions;
    }

    public int maxMessageChars() {
        return maxMessageChars;
    }

    public int maxMessageBytes() {
        return maxMessageBytes;
    }

    public int maxSplitTokens() {
        return maxSplitTokens;
    }

    public int maxRules() {
        return maxRules;
    }

    public int maxTriggerLength() {
        return maxTriggerLength;
    }

    public int maxActionCommandsPerRule() {
        return maxActionCommandsPerRule;
    }

    public int maxCommandLength() {
        return maxCommandLength;
    }

    public int maxPacketItems() {
        return maxPacketItems;
    }

    private int clamp(int value, int min, int max, int fallback) {
        int effective = value <= 0 ? fallback : value;
        if (effective < min) {
            return min;
        }
        if (effective > max) {
            return max;
        }
        return effective;
    }
}
