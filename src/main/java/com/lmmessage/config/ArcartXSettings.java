package com.lmmessage.config;

public final class ArcartXSettings {
    private final String chatUiId;
    private final String hudUiId;
    private final String chatResource;
    private final String hudResource;
    private final boolean autoOpenChatOnJoin;
    private final int hudVisibleTicks;
    private final int maxMessageChars;
    private final int maxPacketItems;

    public ArcartXSettings(
            String chatUiId,
            String hudUiId,
            String chatResource,
            String hudResource,
            boolean autoOpenChatOnJoin,
            int hudVisibleTicks,
            int maxMessageChars,
            int maxPacketItems
    ) {
        this.chatUiId = defaultString(chatUiId, "LmMessage:chat");
        this.hudUiId = defaultString(hudUiId, "LmMessage:hud");
        this.chatResource = defaultString(chatResource, "arcartx/ui/lmmessage_chat.yml");
        this.hudResource = defaultString(hudResource, "arcartx/ui/lmmessage_hud.yml");
        this.autoOpenChatOnJoin = autoOpenChatOnJoin;
        this.hudVisibleTicks = Math.min(20000, Math.max(20, hudVisibleTicks));
        this.maxMessageChars = Math.max(1, maxMessageChars);
        this.maxPacketItems = Math.max(1, maxPacketItems);
    }

    public String chatUiId() {
        return chatUiId;
    }

    public String hudUiId() {
        return hudUiId;
    }

    public String chatResource() {
        return chatResource;
    }

    public String hudResource() {
        return hudResource;
    }

    public boolean autoOpenChatOnJoin() {
        return autoOpenChatOnJoin;
    }

    public int hudVisibleTicks() {
        return hudVisibleTicks;
    }

    public int maxMessageChars() {
        return maxMessageChars;
    }

    public int maxPacketItems() {
        return maxPacketItems;
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}
