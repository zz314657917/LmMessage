package com.lmmessage.integration.arcartx;

import com.lmmessage.config.ArcartXSettings;
import com.lmmessage.util.ResourceSync;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ArcartXMessageUiBridge {
    public interface SubmitHandler {
        void onSubmit(Player player, String input);
    }

    private static final int MAX_CHAT_LINES = 8;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final JavaPlugin plugin;
    private final ArcartXRuntime runtime;
    private final ResourceSync resourceSync;
    private final Map<UUID, ArrayDeque<ChatLine>> chatLines = new LinkedHashMap<UUID, ArrayDeque<ChatLine>>();

    private ArcartXSettings settings;
    private SubmitHandler submitHandler;

    public ArcartXMessageUiBridge(JavaPlugin plugin, ArcartXRuntime runtime) {
        this.plugin = plugin;
        this.runtime = runtime;
        this.resourceSync = new ResourceSync(plugin);
    }

    public boolean initialize(ArcartXSettings settings, SubmitHandler submitHandler) {
        this.settings = settings;
        this.submitHandler = submitHandler;
        if (!runtime.initialize()) {
            plugin.getLogger().warning("ArcartX 初始化失败: " + runtime.getUnavailableReason());
            return false;
        }
        return syncAndRegister(true);
    }

    public boolean reload(ArcartXSettings settings) {
        this.settings = settings;
        return syncAndRegister(true);
    }

    public void shutdown() {
        if (settings == null) {
            return;
        }
        runtime.unregister(settings.chatUiId());
        runtime.unregister(settings.hudUiId());
        chatLines.clear();
    }

    public boolean available() {
        return runtime.isAvailable();
    }

    public String unavailableReason() {
        return runtime.getUnavailableReason();
    }

    public boolean openChat(Player player) {
        if (player == null || settings == null) {
            return false;
        }
        boolean opened = runtime.open(player, settings.chatUiId());
        if (opened) {
            sendChatState(player);
        }
        return opened;
    }

    public void appendChat(Player player, String source, String message) {
        if (player == null || message == null || message.trim().isEmpty()) {
            return;
        }
        ArrayDeque<ChatLine> lines = chatLines.get(player.getUniqueId());
        if (lines == null) {
            lines = new ArrayDeque<ChatLine>();
            chatLines.put(player.getUniqueId(), lines);
        }
        lines.addLast(new ChatLine(source, message));
        while (lines.size() > MAX_CHAT_LINES) {
            lines.removeFirst();
        }
        sendChatState(player);
    }

    public void broadcastChat(String source, String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            appendChat(player, source, message);
        }
    }

    public void showHud(Player player, String channel, String message) {
        if (player == null || settings == null || message == null || message.trim().isEmpty()) {
            return;
        }
        if (!runtime.open(player, settings.hudUiId())) {
            plugin.getLogger().fine("AX HUD open failed: " + runtime.getUnavailableReason());
            return;
        }
        Map<String, Object> packet = new LinkedHashMap<String, Object>();
        Map<String, Object> summary = new LinkedHashMap<String, Object>();
        summary.put("title", "LmMessage");
        summary.put("status", LocalTime.now().format(TIME_FORMATTER));
        summary.put("detail", message);
        Map<String, Object> fixedVariables = new LinkedHashMap<String, Object>();
        fixedVariables.put("hudVisible", "true");
        fixedVariables.put("hudChannel", channel == null || channel.trim().isEmpty() ? "default" : channel.trim());
        fixedVariables.put("hudMessage", message);
        packet.put("summary", summary);
        packet.put("fixedVariables", fixedVariables);
        runtime.sendPacket(player, settings.hudUiId(), "hud", packet);
        scheduleHudHide(player);
    }

    private boolean syncAndRegister(boolean reloadExisting) {
        if (settings == null) {
            return false;
        }
        File chatFile = resourceSync.sync(settings.chatResource(), true);
        File hudFile = resourceSync.sync(settings.hudResource(), true);
        boolean chatRegistered = reloadExisting
                ? runtime.reload(settings.chatUiId(), chatFile)
                : runtime.register(settings.chatUiId(), chatFile);
        boolean hudRegistered = reloadExisting
                ? runtime.reload(settings.hudUiId(), hudFile)
                : runtime.register(settings.hudUiId(), hudFile);
        if (chatRegistered) {
            chatRegistered = runtime.registerPacketCallback(settings.chatUiId(), new ArcartXRuntime.PacketCallback() {
                @Override
                public void onPacket(Player player, String uiId, String identifier, List<String> data) {
                    handlePacket(player, identifier, data);
                }
            });
        }
        plugin.getLogger().info("ArcartX UI registration complete: chat=" + chatRegistered
                + ", hud=" + hudRegistered);
        return chatRegistered && hudRegistered;
    }

    private void handlePacket(Player player, String identifier, List<String> data) {
        if (player == null || identifier == null) {
            return;
        }
        if (!"lmmessage_chat_submit".equalsIgnoreCase(identifier)) {
            return;
        }
        String input = parsePacketInput(data);
        if (submitHandler != null) {
            submitHandler.onSubmit(player, input);
        }
    }

    private String parsePacketInput(List<String> data) {
        if (data == null || data.isEmpty()) {
            return "";
        }
        for (String item : data) {
            if (item == null) {
                continue;
            }
            String trimmed = item.trim();
            if (trimmed.startsWith("chat_input=")) {
                return trimmed.substring("chat_input=".length()).trim();
            }
            if (trimmed.startsWith("chat_input:")) {
                return trimmed.substring("chat_input:".length()).trim();
            }
        }
        return data.get(0) == null ? "" : data.get(0).trim();
    }

    private void sendChatState(Player player) {
        if (player == null || settings == null) {
            return;
        }
        Map<String, Object> packet = new LinkedHashMap<String, Object>();
        Map<String, Object> summary = new LinkedHashMap<String, Object>();
        summary.put("title", "LmMessage");
        summary.put("status", LocalTime.now().format(TIME_FORMATTER));
        summary.put("detail", "PlayerChat backend");
        packet.put("summary", summary);
        packet.put("fixedVariables", fixedVariables(player));
        runtime.sendPacket(player, settings.chatUiId(), "state", packet);
    }

    private Map<String, String> fixedVariables(Player player) {
        List<ChatLine> lines = snapshotLines(player.getUniqueId());
        Map<String, String> fixedVariables = new LinkedHashMap<String, String>();
        for (int index = 0; index < MAX_CHAT_LINES; index++) {
            String value = index < lines.size() ? lines.get(index).display() : "";
            fixedVariables.put("chatMessage" + index, value);
        }
        return fixedVariables;
    }

    private List<ChatLine> snapshotLines(UUID playerId) {
        ArrayDeque<ChatLine> lines = chatLines.get(playerId);
        if (lines == null || lines.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<ChatLine>(lines);
    }

    private void scheduleHudHide(final Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                if (!player.isOnline() || settings == null) {
                    return;
                }
                Map<String, Object> packet = new LinkedHashMap<String, Object>();
                Map<String, Object> summary = new LinkedHashMap<String, Object>();
                summary.put("title", "LmMessage");
                summary.put("status", "");
                summary.put("detail", "");
                Map<String, Object> fixedVariables = new LinkedHashMap<String, Object>();
                fixedVariables.put("hudVisible", "false");
                fixedVariables.put("hudChannel", "");
                fixedVariables.put("hudMessage", "");
                packet.put("summary", summary);
                packet.put("fixedVariables", fixedVariables);
                runtime.sendPacket(player, settings.hudUiId(), "hud", packet);
            }
        }, settings.hudVisibleTicks());
    }
}
