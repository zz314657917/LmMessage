package com.lmmessage.action;

public final class CommandActionParseResult {
    private final CommandAction action;
    private final String rejectionReason;

    private CommandActionParseResult(CommandAction action, String rejectionReason) {
        this.action = action;
        this.rejectionReason = rejectionReason;
    }

    public static CommandActionParseResult accepted(CommandAction action) {
        return new CommandActionParseResult(action, "");
    }

    public static CommandActionParseResult rejected(String rejectionReason) {
        return new CommandActionParseResult(null, rejectionReason == null ? "rejected" : rejectionReason);
    }

    public boolean accepted() {
        return action != null;
    }

    public CommandAction action() {
        return action;
    }

    public String rejectionReason() {
        return rejectionReason;
    }
}
