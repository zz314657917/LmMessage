package com.lmmessage.config;

public final class LmMessageSettings {
    private final boolean debug;
    private final PlayerChatSettings playerChatSettings;
    private final ArcartXSettings arcartXSettings;
    private final RuleSettings ruleSettings;
    private final SecuritySettings securitySettings;

    public LmMessageSettings(
            boolean debug,
            PlayerChatSettings playerChatSettings,
            ArcartXSettings arcartXSettings,
            RuleSettings ruleSettings,
            SecuritySettings securitySettings
    ) {
        this.debug = debug;
        this.playerChatSettings = playerChatSettings;
        this.arcartXSettings = arcartXSettings;
        this.ruleSettings = ruleSettings;
        this.securitySettings = securitySettings;
    }

    public boolean debug() {
        return debug;
    }

    public PlayerChatSettings playerChat() {
        return playerChatSettings;
    }

    public ArcartXSettings arcartX() {
        return arcartXSettings;
    }

    public RuleSettings rules() {
        return ruleSettings;
    }

    public SecuritySettings security() {
        return securitySettings;
    }
}
