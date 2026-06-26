package com.lmmessage.action;

import java.util.Locale;
import java.util.Set;

public final class CommandActionParser {
    public CommandActionParseResult parse(String rawCommand, Set<String> allowedPrefixes, boolean opPrefixEnabled) {
        if (rawCommand == null || rawCommand.trim().isEmpty()) {
            return CommandActionParseResult.rejected("empty command");
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
        if (allowedPrefixes == null || !allowedPrefixes.contains(prefix.configName())) {
            return CommandActionParseResult.rejected("command prefix is not allowed: " + prefix.configName());
        }
        if (prefix == CommandPrefix.OP && !opPrefixEnabled) {
            return CommandActionParseResult.rejected("op command prefix is disabled");
        }
        String command = rawCommand.substring(separator + 1).trim();
        if (command.startsWith("/")) {
            command = command.substring(1).trim();
        }
        if (command.isEmpty()) {
            return CommandActionParseResult.rejected("empty command body");
        }
        if (command.indexOf('\n') >= 0 || command.indexOf('\r') >= 0) {
            return CommandActionParseResult.rejected("command contains line break");
        }
        return CommandActionParseResult.accepted(new CommandAction(prefix, command));
    }
}
