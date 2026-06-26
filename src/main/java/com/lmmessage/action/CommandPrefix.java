package com.lmmessage.action;

public enum CommandPrefix {
    CONSOLE("console"),
    PLAYER("player"),
    OP("op");

    private final String configName;

    CommandPrefix(String configName) {
        this.configName = configName;
    }

    public String configName() {
        return configName;
    }

    public static CommandPrefix fromConfigName(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase();
        for (CommandPrefix prefix : values()) {
            if (prefix.configName.equals(normalized)) {
                return prefix;
            }
        }
        return null;
    }
}
