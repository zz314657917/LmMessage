package com.lmmessage.integration.playerchat;

import cn.handyplus.chat.api.PlayerChatApi;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

public final class PlayerChatGateway {
    private final Plugin plugin;
    private final Logger logger;

    public PlayerChatGateway(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public boolean available() {
        Plugin playerChat = plugin.getServer().getPluginManager().getPlugin("PlayerChat");
        return playerChat != null && playerChat.isEnabled();
    }

    public boolean sendMessage(Player player, String channel, String source, String message) {
        if (player == null || message == null || message.trim().isEmpty()) {
            return false;
        }
        try {
            return PlayerChatApi.getInstance().sendMessage(player, channel, source, message);
        } catch (Throwable throwable) {
            logger.warning("PlayerChatApi.sendMessage failed: player=" + player.getName()
                    + ", channel=" + channel + ", reason=" + safeMessage(throwable));
            return false;
        }
    }

    private String safeMessage(Throwable throwable) {
        if (throwable == null) {
            return "unknown";
        }
        Throwable current = throwable.getCause() == null ? throwable : throwable.getCause();
        String message = current.getMessage();
        return message == null || message.trim().isEmpty() ? current.getClass().getSimpleName() : message.trim();
    }
}
