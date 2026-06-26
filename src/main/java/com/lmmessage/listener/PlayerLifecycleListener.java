package com.lmmessage.listener;

import com.lmmessage.config.LmMessageSettings;
import com.lmmessage.integration.arcartx.ArcartXMessageUiBridge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerLifecycleListener implements Listener {
    private final ArcartXMessageUiBridge uiBridge;
    private LmMessageSettings settings;

    public PlayerLifecycleListener(ArcartXMessageUiBridge uiBridge, LmMessageSettings settings) {
        this.uiBridge = uiBridge;
        this.settings = settings;
    }

    public void updateSettings(LmMessageSettings settings) {
        this.settings = settings;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if (settings != null && settings.arcartX().autoOpenChatOnJoin()) {
            uiBridge.openChat(event.getPlayer());
        }
    }
}
