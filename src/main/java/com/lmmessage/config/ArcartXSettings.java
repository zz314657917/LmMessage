package com.lmmessage.config;

public final class ArcartXSettings {
    private final String chatUiId;
    private final String hudUiId;
    private final String chatResource;
    private final String hudResource;
    private final boolean autoOpenChatOnJoin;
    private final int hudVisibleTicks;

    public ArcartXSettings(
            String chatUiId,
            String hudUiId,
            String chatResource,
            String hudResource,
            boolean autoOpenChatOnJoin,
            int hudVisibleTicks
    ) {
        this.chatUiId = defaultString(chatUiId, "LmMessage:chat");
        this.hudUiId = defaultString(hudUiId, "LmMessage:hud");
        this.chatResource = defaultString(chatResource, "arcartx/ui/lmmessage_chat.yml");
        this.hudResource = defaultString(hudResource, "arcartx/ui/lmmessage_hud.yml");
        this.autoOpenChatOnJoin = autoOpenChatOnJoin;
        this.hudVisibleTicks = Math.max(20, hudVisibleTicks);
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

    private String defaultString(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}
