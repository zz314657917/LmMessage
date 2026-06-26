package com.lmmessage.service;

import com.lmmessage.action.CommandActionDispatcher;
import com.lmmessage.config.LmMessageSettings;
import com.lmmessage.integration.arcartx.ArcartXMessageUiBridge;
import com.lmmessage.integration.playerchat.PlayerChatGateway;
import com.lmmessage.rule.RuleEvaluationResult;
import com.lmmessage.rule.RuleEngine;
import com.lmmessage.rule.RuleMatch;
import org.bukkit.Sound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.logging.Logger;

public final class ChatSubmissionService {
    private final Plugin plugin;
    private final PlayerChatGateway playerChatGateway;
    private final ArcartXMessageUiBridge uiBridge;
    private final CommandActionDispatcher commandActionDispatcher;
    private final RuleEngine ruleEngine = new RuleEngine();
    private final Logger logger;
    private LmMessageSettings settings;

    public ChatSubmissionService(
            Plugin plugin,
            PlayerChatGateway playerChatGateway,
            ArcartXMessageUiBridge uiBridge,
            CommandActionDispatcher commandActionDispatcher,
            LmMessageSettings settings
    ) {
        this.plugin = plugin;
        this.playerChatGateway = playerChatGateway;
        this.uiBridge = uiBridge;
        this.commandActionDispatcher = commandActionDispatcher;
        this.settings = settings;
        this.logger = plugin.getLogger();
    }

    public void updateSettings(LmMessageSettings settings) {
        this.settings = settings;
    }

    public void handleAxSubmit(Player player, String input) {
        if (player == null) {
            return;
        }
        String message = sanitizeInput(player, input, "ax");
        if (message.isEmpty()) {
            return;
        }
        RuleEvaluationResult result = evaluate(player, message);
        handleMatches(player, result, true);
        uiBridge.appendChat(player, "me", result.outputMessage());
        if (result.cancelOriginal()) {
            if (settings.debug()) {
                logger.info("LmMessage swallowed AX chat submit: player=" + player.getName()
                        + ", message=" + result.outputMessage());
            }
            return;
        }
        boolean sent = playerChatGateway.sendMessage(
                player,
                settings.playerChat().defaultChannel(),
                settings.playerChat().source(),
                result.outputMessage()
        );
        if (!sent) {
            player.sendMessage("§cLmMessage: PlayerChat 发送失败，请联系管理员。");
        }
    }

    public void handlePlayerChatEvent(Player player, String channel, String source, String message, boolean allowActions) {
        if (player == null) {
            return;
        }
        String safeMessage = sanitizeInput(player, message, "playerchat");
        if (safeMessage.isEmpty()) {
            return;
        }
        String displaySource = source == null || source.trim().isEmpty() ? channel : source;
        RuleEvaluationResult result = evaluate(player, safeMessage);
        handleMatches(player, result, allowActions && settings.rules().executeActionsOnPlayerChatEvents());
        if (!result.cancelOriginal()) {
            uiBridge.broadcastChat(displaySource, result.outputMessage());
        }
    }

    public void handlePlayerTellEvent(Player sender, String targetName, String message, boolean allowActions) {
        if (sender == null) {
            return;
        }
        String safeMessage = sanitizeInput(sender, message, "tell");
        if (safeMessage.isEmpty()) {
            return;
        }
        RuleEvaluationResult result = evaluate(sender, safeMessage);
        handleMatches(sender, result, allowActions && settings.rules().executeActionsOnPlayerChatEvents());
        if (result.cancelOriginal()) {
            return;
        }
        String displaySource = targetName == null || targetName.trim().isEmpty() ? "tell" : targetName.trim();
        uiBridge.appendChat(sender, displaySource, result.outputMessage());
        Player target = Bukkit.getPlayerExact(displaySource);
        if (target != null && target.isOnline() && !target.getUniqueId().equals(sender.getUniqueId())) {
            uiBridge.appendChat(target, sender.getName(), result.outputMessage());
        }
    }

    private RuleEvaluationResult evaluate(Player player, String message) {
        return ruleEngine.evaluate(
                player.getName(),
                message,
                settings.rules().messageRules(),
                settings.rules().actionRules(),
                settings.security().maxSplitTokens()
        );
    }

    private void handleMatches(Player player, RuleEvaluationResult result, boolean executeActions) {
        if (!result.matched()) {
            return;
        }
        for (RuleMatch match : result.matches()) {
            String channel = match.rule().channel();
            if (channel != null && !channel.trim().isEmpty()) {
                uiBridge.showHud(player, channel, match.outputMessage());
            }
            playSound(player, match.rule().sound());
        }
        if (executeActions) {
            commandActionDispatcher.dispatchAll(
                    player,
                    result.commands(),
                    result.variables(),
                    settings.security()
            );
        }
    }

    private void playSound(Player player, String soundName) {
        if (soundName == null || soundName.trim().isEmpty()) {
            return;
        }
        try {
            Sound sound = Sound.valueOf(soundName.trim().toUpperCase(Locale.ROOT));
            player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
        } catch (IllegalArgumentException exception) {
            logger.warning("未知声音配置: " + soundName);
        } catch (Throwable throwable) {
            logger.warning("播放声音失败: player=" + player.getName()
                    + ", sound=" + soundName + ", reason=" + throwable.getMessage());
        }
    }

    private String sanitizeInput(Player player, String input, String source) {
        String message = input == null ? "" : input.trim();
        if (message.isEmpty()) {
            return "";
        }
        if (message.length() > settings.security().maxMessageChars()
                || message.getBytes(StandardCharsets.UTF_8).length > settings.security().maxMessageBytes()) {
            logger.warning("拒绝超长消息: playerUuid=" + player.getUniqueId()
                    + ", source=" + source
                    + ", chars=" + message.length()
                    + ", maxChars=" + settings.security().maxMessageChars());
            return "";
        }
        return message;
    }
}
