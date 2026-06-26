package com.lmmessage.config;

public final class PlayerChatSettings {
    private final String defaultChannel;
    private final String source;

    public PlayerChatSettings(String defaultChannel, String source) {
        this.defaultChannel = defaultChannel == null || defaultChannel.trim().isEmpty() ? "default" : defaultChannel.trim();
        this.source = source == null || source.trim().isEmpty() ? "LmMessage" : source.trim();
    }

    public String defaultChannel() {
        return defaultChannel;
    }

    public String source() {
        return source;
    }
}
