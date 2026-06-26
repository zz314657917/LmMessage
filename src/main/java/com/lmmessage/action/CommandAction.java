package com.lmmessage.action;

public final class CommandAction {
    private final CommandPrefix prefix;
    private final String command;

    public CommandAction(CommandPrefix prefix, String command) {
        this.prefix = prefix;
        this.command = command == null ? "" : command;
    }

    public CommandPrefix prefix() {
        return prefix;
    }

    public String command() {
        return command;
    }
}
