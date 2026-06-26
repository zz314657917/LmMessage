package com.lmmessage.util;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public final class ResourceSync {
    private final JavaPlugin plugin;

    public ResourceSync(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public File sync(String resourcePath, boolean replace) {
        if (!isAllowedResource(resourcePath)) {
            plugin.getLogger().warning("拒绝同步未允许资源: " + resourcePath);
            return new File(plugin.getDataFolder(), "arcartx/ui");
        }
        File target = new File(plugin.getDataFolder(), resourcePath);
        try {
            File dataRoot = plugin.getDataFolder().getCanonicalFile();
            File canonicalTarget = target.getCanonicalFile();
            if (!canonicalTarget.toPath().startsWith(dataRoot.toPath())) {
                plugin.getLogger().warning("拒绝同步越界资源: " + resourcePath);
                return target;
            }
        } catch (IOException exception) {
            plugin.getLogger().warning("校验资源路径失败: " + resourcePath + ", reason=" + exception.getMessage());
            return target;
        }
        if (target.isFile() && !replace) {
            return target;
        }
        File parent = target.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
            plugin.getLogger().warning("无法创建资源目录: " + parent.getPath());
            return target;
        }
        try (InputStream input = plugin.getResource(resourcePath)) {
            if (input == null) {
                plugin.getLogger().warning("插件内缺少资源: " + resourcePath);
                return target;
            }
            Files.copy(input, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            plugin.getLogger().warning("同步资源失败: " + resourcePath + ", reason=" + exception.getMessage());
        }
        return target;
    }

    private boolean isAllowedResource(String resourcePath) {
        return "arcartx/ui/lmmessage_chat.yml".equals(resourcePath)
                || "arcartx/ui/lmmessage_hud.yml".equals(resourcePath);
    }
}
