package com.lmmessage.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class RuleDefinition {
    private final String name;
    private final boolean enabled;
    private final RuleTriggerType triggerType;
    private final String triggerText;
    private final String replacePrefix;
    private final String channel;
    private final String sound;
    private final boolean cancelOriginal;
    private final List<String> commands;

    private RuleDefinition(Builder builder) {
        this.name = defaultString(builder.name, "unnamed");
        this.enabled = builder.enabled;
        this.triggerType = Objects.requireNonNull(builder.triggerType, "triggerType");
        this.triggerText = defaultString(builder.triggerText, "");
        this.replacePrefix = defaultString(builder.replacePrefix, "");
        this.channel = defaultString(builder.channel, "");
        this.sound = defaultString(builder.sound, "");
        this.cancelOriginal = builder.cancelOriginal;
        this.commands = Collections.unmodifiableList(new ArrayList<String>(builder.commands));
    }

    public static Builder builder(RuleTriggerType triggerType) {
        return new Builder(triggerType);
    }

    public boolean isValid() {
        return !triggerText.trim().isEmpty();
    }

    public boolean matches(String message) {
        if (!enabled || message == null || triggerText.trim().isEmpty()) {
            return false;
        }
        if (triggerType == RuleTriggerType.START) {
            return message.startsWith(triggerText);
        }
        return message.contains(triggerText);
    }

    public String applyReplacement(String message) {
        if (message == null) {
            return "";
        }
        if (replacePrefix.trim().isEmpty()) {
            return message;
        }
        if (message.startsWith(replacePrefix)) {
            return message.substring(replacePrefix.length()).trim();
        }
        return message;
    }

    public String name() {
        return name;
    }

    public boolean enabled() {
        return enabled;
    }

    public RuleTriggerType triggerType() {
        return triggerType;
    }

    public String triggerText() {
        return triggerText;
    }

    public String replacePrefix() {
        return replacePrefix;
    }

    public String channel() {
        return channel;
    }

    public String sound() {
        return sound;
    }

    public boolean cancelOriginal() {
        return cancelOriginal;
    }

    public List<String> commands() {
        return commands;
    }

    private static String defaultString(String value, String fallback) {
        return value == null ? fallback : value;
    }

    public static final class Builder {
        private String name = "unnamed";
        private boolean enabled = true;
        private final RuleTriggerType triggerType;
        private String triggerText = "";
        private String replacePrefix = "";
        private String channel = "";
        private String sound = "";
        private boolean cancelOriginal;
        private final List<String> commands = new ArrayList<String>();

        private Builder(RuleTriggerType triggerType) {
            this.triggerType = Objects.requireNonNull(triggerType, "triggerType");
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder triggerText(String triggerText) {
            this.triggerText = triggerText;
            return this;
        }

        public Builder replacePrefix(String replacePrefix) {
            this.replacePrefix = replacePrefix;
            return this;
        }

        public Builder channel(String channel) {
            this.channel = channel;
            return this;
        }

        public Builder sound(String sound) {
            this.sound = sound;
            return this;
        }

        public Builder cancelOriginal(boolean cancelOriginal) {
            this.cancelOriginal = cancelOriginal;
            return this;
        }

        public Builder commands(List<String> commands) {
            this.commands.clear();
            if (commands != null) {
                for (String command : commands) {
                    if (command != null && !command.trim().isEmpty()) {
                        this.commands.add(command.trim());
                    }
                }
            }
            return this;
        }

        public RuleDefinition build() {
            return new RuleDefinition(this);
        }
    }
}
