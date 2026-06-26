package com.lmmessage.integration.protocollib;

import org.bukkit.plugin.Plugin;

public final class ProtocolLibStatus {
    private final Plugin plugin;

    public ProtocolLibStatus(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean available() {
        Plugin protocolLib = plugin.getServer().getPluginManager().getPlugin("ProtocolLib");
        return protocolLib != null && protocolLib.isEnabled();
    }

    public String statusLine() {
        return available() ? "ProtocolLib available; fallback packet intercept can be enabled later."
                : "ProtocolLib not installed; PlayerChat main route remains available.";
    }
}
