package com.lmmessage.action;

import com.lmmessage.config.SecuritySettings;
import com.lmmessage.rule.RuleVariables;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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
                    securitySettings.opPrefixEnabled()
            );
            if (!parsed.accepted()) {
                logger.warning("拒绝规则命令: player=" + player.getName()
                        + ", reason=" + parsed.rejectionReason()
                        + ", raw=" + rawCommand);
                continue;
            }
            CommandAction action = parsed.action();
            String command = expander.expand(action.command(), variables);
            if (command.isEmpty()) {
                logger.warning("拒绝空规则命令: player=" + player.getName() + ", raw=" + rawCommand);
                continue;
            }
            if (securitySettings.logCommandActions()) {
                logger.info("执行规则命令: player=" + player.getName()
                        + ", prefix=" + action.prefix().configName()
                        + ", command=" + command);
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
        executeAsTemporaryOp(player, command);
    }

    private void executeAsTemporaryOp(Player player, String command) {
        boolean wasOp = player.isOp();
        try {
            if (!wasOp) {
                player.setOp(true);
            }
            player.performCommand(command);
        } finally {
            if (!wasOp && player.isOnline()) {
                player.setOp(false);
            }
        }
    }
}
