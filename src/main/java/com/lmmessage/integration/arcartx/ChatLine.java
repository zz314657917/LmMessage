package com.lmmessage.integration.arcartx;

public final class ChatLine {
    private final String source;
    private final String message;

    public ChatLine(String source, String message) {
        this.source = source == null ? "" : source;
        this.message = message == null ? "" : message;
    }

    public String source() {
        return source;
    }

    public String message() {
        return message;
    }

    public String display() {
        if (source.isEmpty()) {
            return message;
        }
        return "[" + source + "] " + message;
    }
}
