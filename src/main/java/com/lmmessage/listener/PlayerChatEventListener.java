package com.lmmessage.listener;

import cn.handyplus.chat.event.PlayerChannelChatEvent;
import cn.handyplus.chat.event.PlayerChannelTellEvent;
import com.lmmessage.service.ChatSubmissionService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class PlayerChatEventListener implements Listener {
    private final ChatSubmissionService chatSubmissionService;

    public PlayerChatEventListener(ChatSubmissionService chatSubmissionService) {
        this.chatSubmissionService = chatSubmissionService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChannelChat(PlayerChannelChatEvent event) {
        chatSubmissionService.handlePlayerChatEvent(
                event.getPlayer(),
                event.getChannel(),
                event.getSource(),
                event.getOriginalMessage(),
                true
        );
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChannelTell(PlayerChannelTellEvent event) {
        chatSubmissionService.handlePlayerTellEvent(
                event.getPlayer(),
                event.getTellPlayerName(),
                event.getOriginalMessage(),
                true
        );
    }
}
