package com.lmmessage.action;

import java.util.Locale;
import java.util.Set;

public final class CommandActionParser {
    public CommandActionParseResult parse(
            String rawCommand,
            Set<String> allowedPrefixes,
            boolean opPrefixEnabled,
            int maxCommandLength
    ) {
        if (rawCommand == null || rawCommand.trim().isEmpty()) {
            return CommandActionParseResult.rejected("empty command");
        }
        if (rawCommand.length() > maxCommandLength) {
            return CommandActionParseResult.rejected("command exceeds max length");
        }
        int separator = rawCommand.indexOf(':');
        if (separator <= 0 || separator + 1 >= rawCommand.length()) {
            return CommandActionParseResult.rejected("missing command prefix");
        }
        String prefixName = rawCommand.substring(0, separator).trim().toLowerCase(Locale.ROOT);
        CommandPrefix prefix = CommandPrefix.fromConfigName(prefixName);
        if (prefix == null) {
            return CommandActionParseResult.rejected("unknown command prefix: " + prefixName);
        }
        if (prefix == CommandPrefix.OP) {
            return CommandActionParseResult.rejected("op command prefix is permanently disabled");
        }
        if (allowedPrefixes == null || !allowedPrefixes.contains(prefix.configName())) {
            return CommandActionParseResult.rejected("command prefix is not allowed: " + prefix.configName());
        }
        String command = rawCommand.substring(separator + 1).trim();
        if (command.startsWith("/")) {
            command = command.substring(1).trim();
        }
        if (command.isEmpty()) {
            return CommandActionParseResult.rejected("empty command body");
        }
        if (command.length() > maxCommandLength) {
            return CommandActionParseResult.rejected("command exceeds max length");
        }
        if (command.indexOf('\n') >= 0 || command.indexOf('\r') >= 0) {
            return CommandActionParseResult.rejected("command contains line break");
        }
        return CommandActionParseResult.accepted(new CommandAction(prefix, command));
    }

    public CommandActionParseResult parse(String rawCommand, Set<String> allowedPrefixes, boolean opPrefixEnabled) {
        return parse(rawCommand, allowedPrefixes, opPrefixEnabled, SecurityDefaults.MAX_COMMAND_LENGTH);
    }

    private static final class SecurityDefaults {
        private static final int MAX_COMMAND_LENGTH = 256;
    }
}
