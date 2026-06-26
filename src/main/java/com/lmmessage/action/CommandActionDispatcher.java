package com.lmmessage.action;

import com.lmmessage.config.SecuritySettings;
import com.lmmessage.rule.RuleVariables;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Logger;

public final class CommandActionDispatcher {
    private final Plugin plugin;
    private final Logger logger;
    private final CommandActionParser parser = new CommandActionParser();
    private final CommandVariableExpander expander = new CommandVariableExpander();

    public CommandActionDispatcher(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void dispatchAll(Player player, List<String> rawCommands, RuleVariables variables, SecuritySettings securitySettings) {
        if (player == null || rawCommands == null || rawCommands.isEmpty() || securitySettings == null) {
            return;
        }
        for (String rawCommand : rawCommands) {
            CommandActionParseResult parsed = parser.parse(
                    rawCommand,
                    securitySettings.allowedCommandPrefixes(),
                    securitySettings.opPrefixEnabled(),
                    securitySettings.maxCommandLength()
            );
            if (!parsed.accepted()) {
                logger.warning("拒绝规则命令: playerUuid=" + player.getUniqueId()
                        + ", reason=" + parsed.rejectionReason()
                        + ", rawLength=" + lengthOf(rawCommand)
                        + ", rawHash=" + hash(rawCommand));
                continue;
            }
            CommandAction action = parsed.action();
            String command = expander.expand(action.command(), variables);
            if (command.isEmpty()) {
                logger.warning("拒绝空规则命令: playerUuid=" + player.getUniqueId()
                        + ", rawHash=" + hash(rawCommand));
                continue;
            }
            if (command.length() > securitySettings.maxCommandLength()) {
                logger.warning("拒绝长度超限规则命令: playerUuid=" + player.getUniqueId()
                        + ", prefix=" + action.prefix().configName()
                        + ", commandLength=" + command.length()
                        + ", commandHash=" + hash(command));
                continue;
            }
            if (securitySettings.logCommandActions()) {
                logger.info("执行规则命令: playerUuid=" + player.getUniqueId()
                        + ", prefix=" + action.prefix().configName()
                        + ", commandLength=" + command.length()
                        + ", commandHash=" + hash(command));
            }
            execute(player, action.prefix(), command);
        }
    }

    private void execute(Player player, CommandPrefix prefix, String command) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    execute(player, prefix, command);
                }
            });
            return;
        }
        if (prefix == CommandPrefix.CONSOLE) {
            ConsoleCommandSender console = Bukkit.getConsoleSender();
            Bukkit.dispatchCommand(console, command);
            return;
        }
        if (prefix == CommandPrefix.PLAYER) {
            player.performCommand(command);
            return;
        }
        logger.warning("拒绝规则命令: playerUuid=" + player.getUniqueId()
                + ", reason=op command prefix is permanently disabled"
                + ", commandHash=" + hash(command));
    }

    private int lengthOf(String value) {
        return value == null ? 0 : value.length();
    }

    private String hash(String value) {
        String safeValue = value == null ? "" : value;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(safeValue.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(16);
            for (int index = 0; index < hashed.length && index < 8; index++) {
                builder.append(String.format("%02x", hashed[index]));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            return Integer.toHexString(safeValue.hashCode());
        }
    }
}
